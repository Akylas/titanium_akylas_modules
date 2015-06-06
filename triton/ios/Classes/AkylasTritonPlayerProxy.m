/**
 * Akylas
 * Copyright (c) 2014-2015 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import <MediaPlayer/MPNowPlayingInfoCenter.h>
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MPMusicPlayerController.h>

#import "AkylasTritonPlayerProxy.h"
#import "TiUtils.h"
#import "TiAudioSession.h"
#import "TiAudioSoundProxy.h"
#import "TiFile.h"
#import "TiApp.h"


#import <TritonPlayerSDK/TritonPlayerSDK.h>

typedef enum {
    STATE_INITIALIZED,
    STATE_ERROR,
    STATE_PLAYING,
    STATE_BUFFERING,
    STATE_STOPPED,
    STATE_PAUSED,
    STATE_CONNECTING
} PlaybackState;

@interface NSArray (ShuffledArray)
- (NSArray *)shuffled;
@end

@implementation NSArray (ShuffledArray)

- (NSArray *)shuffled {
    NSMutableArray *tmpArray = [NSMutableArray arrayWithCapacity:[self count]];
    
    for (id anObject in self) {
        NSUInteger randomPos = arc4random()%([tmpArray count]+1);
        [tmpArray insertObject:anObject atIndex:randomPos];
    }
    
    return [NSArray arrayWithArray:tmpArray];
}

@end


@interface AkylasTritonPlayerProxy()
@property (strong, nonatomic) NSArray *originalQueue;
@property (strong, nonatomic, readwrite) NSArray *queue;
@property (strong, nonatomic, readwrite) AVPlayerItem *nowPlayingItem;
@property (nonatomic, readwrite) NSUInteger indexOfNowPlayingItem;
@property (nonatomic) BOOL isLoadingAsset;
@property (nonatomic) BOOL shouldReturnToBeginningWhenSkippingToPreviousItem; // default YES

@end

@class TiAudioSoundProxy;
@implementation AkylasTritonPlayerProxy{
@private
    double _duration;
    double _currentProgress;
    NSMutableArray* _playlist;
    BOOL _playerInitialized;
    int _state;
    id _currentItem;
    CuePointEvent* _currentCuePoint;
    ImageLoaderRequest *urlRequest;
    TritonPlayer *player;
    float volume;
    id timeObserver;
    MPMusicRepeatMode _repeatMode; // note: MPMusicRepeatModeDefault is not supported
    MPMusicShuffleMode _shuffleMode; // note: only MPMusicShuffleModeOff and MPMusicShuffleModeSongs are supported
    BOOL _ignoreStateChange;
    BOOL _needsPlayAfterInterruption;
    
}
#pragma mark Internal

void tritonAudioRouteChangeListenerCallback (void *inUserData, AudioSessionPropertyID inPropertyID, UInt32 inPropertyValueSize, const void *inPropertyValue) {
    if (inPropertyID != kAudioSessionProperty_AudioRouteChange) return;
    
    AkylasTritonPlayerProxy* streamer = (__bridge AkylasTritonPlayerProxy *)inUserData;
    
    CFDictionaryRef routeChangeDictionary = inPropertyValue;
    
    CFNumberRef routeChangeReasonRef = CFDictionaryGetValue(routeChangeDictionary, CFSTR (kAudioSession_AudioRouteChangeKey_Reason));
    SInt32 routeChangeReason;
    CFNumberGetValue (routeChangeReasonRef, kCFNumberSInt32Type, &routeChangeReason);
    
    CFStringRef oldRouteRef = CFDictionaryGetValue(routeChangeDictionary, CFSTR (kAudioSession_AudioRouteChangeKey_OldRoute));
    NSString *oldRouteString = (__bridge NSString *)oldRouteRef;
    
    if (routeChangeReason == kAudioSessionRouteChangeReason_OldDeviceUnavailable) {
        if (([streamer playing]) &&
            (([oldRouteString isEqualToString:@"Headphone"]) ||
             ([oldRouteString isEqualToString:@"LineOut"])))
        {
            // Janking out the headphone will stop the audio.
            [streamer pause:nil];
        }
    }
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    _playerInitialized = NO;
    volume = 1.0f;
    _needsPlayAfterInterruption = NO;
    _ignoreStateChange = NO;
    self.indexOfNowPlayingItem = NSNotFound;
    _repeatMode = MPMusicRepeatModeNone;
    _shuffleMode = MPMusicShuffleModeOff;
    self.shouldReturnToBeginningWhenSkippingToPreviousItem = YES;
    _state = STATE_STOPPED;
    // Handle unplugging of headphones
    AudioSessionAddPropertyListener (kAudioSessionProperty_AudioRouteChange, tritonAudioRouteChangeListenerCallback, (__bridge void*)self);
    [self initializeProperty:@"volume" defaultValue:@(volume)];
    dispatch_async(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(remoteControlEvent:) name:kTiRemoteControlNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(paused:) name:kTiPausedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(resumed:) name:kTiResumedNotification object:nil];
    });
    [super _initWithProperties:properties];
}

-(void)paused:(id)sender
{
    [self removePlayerTimeObserver];
}

-(void)resumed:(id)sender
{
    if (self.nowPlayingItem) {
        [self initProgressTimer];
    }
}



-(void)dealloc {
    RELEASE_TO_NIL(_currentCuePoint);
    [super dealloc];
}

-(void)_destroy
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [[NSNotificationCenter defaultCenter] removeObserver:self];
    });
    
    if ([[self playing] boolValue]) {
        [self stop:nil];
        [[TiAudioSession sharedSession] stopAudioSession];
    }
    [self cleanPlayer];
    [super _destroy];
}


-(NSString*)apiName
{
    return @"Ti.Media.AudioStreamer";
}

-(void)skipToNextItem {
    if (self.indexOfNowPlayingItem == 0 && _state == STATE_STOPPED) {
        [self play:nil];
        return;
    }
    
    if (self.indexOfNowPlayingItem+1 < [self.queue count]) {
        // Play next track
        self.indexOfNowPlayingItem++;
    } else {
        if (_repeatMode == MPMusicRepeatModeAll) {
            if (_shuffleMode == MPMusicShuffleModeSongs) {
                self.queue = self.originalQueue;
            }
            // Wrap around back to the first track
            self.indexOfNowPlayingItem = 0;
        } else {
            if (_state == STATE_PLAYING || _state == STATE_PAUSED) {
                if (_nowPlayingItem != nil) {
                    [self fireEvent:@"end" withObject:nil checkForListener:YES];
                }
            }
            NSLog(@"TiMediaAudioStreamer: end of queue reached");
            [self stop:nil];
        }
    }
}

- (void)skipToBeginning {
    [self seek:@(0.0)];
}

- (void)skipToPreviousItem {
    if (self.indexOfNowPlayingItem > 0) {
        self.indexOfNowPlayingItem--;
    } else if (self.shouldReturnToBeginningWhenSkippingToPreviousItem) {
        [self skipToBeginning];
    }
}

#pragma mark - MPMediaPlayback

-(NSNumber*)repeatMode
{
    return @(_repeatMode);
}

-(void)setRepeatMode:(NSNumber*)value
{
    NSInteger mode = [TiUtils intValue:value];
    // Sanity check
    switch (mode) {
        case MPMusicRepeatModeAll:
        case MPMusicRepeatModeDefault:
        case MPMusicRepeatModeNone:
        case MPMusicRepeatModeOne:
            break;
        default:
            [self throwException:@"Invalid repeat mode"
                       subreason:nil
                        location:CODELOCATION];
    }
    _repeatMode = mode;
}

-(NSNumber*)shuffleMode
{
    return @(_shuffleMode);
}

-(void)setShuffleMode:(id)value
{
    NSInteger mode = [TiUtils intValue:value];
    // Sanity check
    switch (mode) {
        case MPMusicShuffleModeOff:
        case MPMusicShuffleModeSongs:
        case MPMusicShuffleModeDefault:
        case MPMusicShuffleModeAlbums:
            break;
        default:
            [self throwException:@"Invalid shuffle mode"
                       subreason:nil
                        location:CODELOCATION];
    }
    _shuffleMode = mode;
    id currentItem = [self getCurrentQueueItem];
    self.queue = self.originalQueue;
    if ([_queue count]) {
        if (![self stopped]) {
            self.indexOfNowPlayingItem = [_queue indexOfObject:currentItem];
        }
    } else {
        self.indexOfNowPlayingItem = NSNotFound;
    }
}

- (void)setOriginalQueue:(NSArray *)originalQueue {
    // The original queue never changes, while queue is shuffled
    RELEASE_TO_NIL(_originalQueue);
    _originalQueue = [originalQueue retain];
    self.queue = originalQueue;
}

-(void)addToPlaylist:(id)args {
    NSArray* toAdd = nil;
    if (IS_OF_CLASS(args, NSArray)) {
        toAdd =[NSArray arrayWithArray:args];
    }
    else if (args) {
        toAdd =[NSArray arrayWithObject:args];
    }
    if (toAdd) {
        NSArray* old = _originalQueue ;
        _originalQueue = [[_originalQueue arrayByAddingObjectsFromArray:toAdd] retain];
        RELEASE_TO_NIL(old)
        old = _queue ;
        _queue = [[_queue arrayByAddingObjectsFromArray:toAdd] retain];
    }
}

- (void)setQueue:(NSArray *)queue {
    RELEASE_TO_NIL(_queue);
    switch (_shuffleMode) {
        case MPMusicShuffleModeOff:
            _queue = [queue retain];
            break;
            
        case MPMusicShuffleModeSongs:
            _queue = [[queue shuffled] retain];
            break;
            
        default:
            NSLog(@"Only MPMusicShuffleModeOff and MPMusicShuffleModeSongs are supported");
            _queue = [[queue shuffled] retain];
            break;
    }
}

- (void)setIndexOfNowPlayingItem:(NSUInteger)indexOfNowPlayingItem {
    if (indexOfNowPlayingItem == NSNotFound) {
        return;
    }
    
    _indexOfNowPlayingItem = indexOfNowPlayingItem;
    _currentItem = [_queue objectAtIndex:self.indexOfNowPlayingItem];
    self.nowPlayingItem = [self.queue objectAtIndex:indexOfNowPlayingItem];
}

- (void)setNowPlayingItem:(id)item {
    
    if (IS_OF_CLASS(item, NSDictionary)) {
//        NSMutableDictionary* settings = [NSMutableDictionary dictionary];
        [[self playerWithSettings:item] play];
    }
}

- (void)playItemAtIndex:(NSUInteger)index {
    [self setIndexOfNowPlayingItem:index];
}



-(void)cleanPlayer {
    if (player != nil)
    {
        [self removePlayerTimeObserver];
        [player stop];
        RELEASE_TO_NIL(player)
    }
}

-(void)onTimerUpdate:(NSTimer *)timer {
    if (player.isExecuting) {
        _currentProgress ++;
        if (_duration > 0 && [self _hasListeners:@"progress"])
        {
            NSDictionary *event = @{
                                    @"progress":@(_currentProgress*1000),
                                    @"duration":@(_duration)
                                    };
            [self fireEvent:@"progress" withObject:event checkForListener:NO];
        }

    }
}

-(NSDictionary *)paramsConvert
{
    static NSDictionary *paramsConvert = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        paramsConvert = [@{
                             @"station_mount":SettingsMountKey,
                             @"station_name":SettingsStationNameKey,
                             @"station_broadcaster":SettingsBroadcasterKey,
                             @"enable_location_tracking":SettingsEnableLocationTrackingKey,
                             @"stream_params":SettingsStreamParamsExtraKey
                             }retain];
    });
    return paramsConvert;
}

-(TritonPlayer*)playerWithSettings:(NSDictionary *)settings {
    NSMutableDictionary* realSettings = [NSMutableDictionary dictionaryWithDictionary:settings];
    [[self paramsConvert] enumerateKeysAndObjectsUsingBlock:^(NSString* key, id obj, BOOL *stop) {
        id value = [realSettings objectForKey:key];
        if (value) {
            
            [realSettings setObject:value forKey:obj];
            [realSettings removeObjectForKey:key];
        }
    }];
    if ([[realSettings objectForKey:SettingsBroadcasterKey] isEqualToString:@"Triton Digital"]) {
        if ([[realSettings objectForKey:SettingsMountKey] rangeOfString:@"preprod"].location == NSNotFound) {
            [realSettings setObject:[[realSettings objectForKey:SettingsMountKey] stringByAppendingString:@".preprod"] forKey:SettingsMountKey];
        }
    }
    if (!player) {
        player = [[TritonPlayer alloc] initWithDelegate:(id<TritonPlayerDelegate>)self andSettings:realSettings];
    } else {
        [player updateSettings:realSettings];
    }
    [self initProgressTimer];
    [player setVolume:volume];
    _playerInitialized = YES;
    return player;
}


-(void)initProgressTimer
{
    if (timeObserver) {
        return;
    }
    timeObserver = [[NSTimer scheduledTimerWithTimeInterval: 10
                                                    target: self
                                                  selector:@selector(onTimerUpdate:)
                                                  userInfo: nil repeats:YES] retain];
}

/* Cancels the previously registered time observer. */
-(void)removePlayerTimeObserver
{
    if (timeObserver)
    {
        [timeObserver invalidate];
        RELEASE_TO_NIL(timeObserver)
    }
}

#pragma mark Public APIs

-(void)setPaused:(NSNumber *)paused
{
    if ([TiUtils boolValue:paused])
    {
        [self pause:nil];
    }
    else
    {
        [self play:nil];
    }
}

-(void)setMute:(NSNumber *)mute
{
    BOOL value = [TiUtils boolValue:mute];
    if (value) {
        [player mute];
    } else {
        [player unmute];
    }
}

#define PROP_BOOL(name,func) \
-(NSNumber*)name\
{\
return [self func:nil];\
}

-(id)progress
{
    //in our case it's the playing time
    if (player) {
        return @(_currentProgress * 1000);
    }
    return @(0);
}
-(id)state
{
    return @(_state);
}

-(id)stateDescription
{
    return [self stateToString:_state];
}

-(id)isPaused:(id)args
{
    return @(_state == STATE_PAUSED);
}

PROP_BOOL(paused,isPaused);

-(id)isPlaying:(id)args
{
    return @(_state == STATE_PLAYING);
}
PROP_BOOL(playing,isPlaying);

-(id)isStopped:(id)args
{
    return @(_state == STATE_STOPPED || _state == STATE_ERROR);
}
PROP_BOOL(stopped,isStopped);


-(id)isMute:(id)args
{
    return [self valueForKey:@"muted"];
}

-(id)currentItem
{
    return [self getCurrentQueueItem];
}

-(NSNumber *)duration
{
    return @(_duration);
}


-(id)index
{
    return @(self.indexOfNowPlayingItem);
}


-(NSNumber *)volume
{
    return @(volume);
}

-(void)setVolume:(id)newVolume
{
    volume = [TiUtils doubleValue:newVolume def:1.0f];
    if (player != nil) {
        [player setVolume:volume];
    }
}

-(void)setPlaylist:(id)args
{
    BOOL playing = _state == STATE_PLAYING;
    BOOL emptyPlaylist = args && !IS_OF_CLASS(args, NSNull);
    _ignoreStateChange = emptyPlaylist;
    [self stop:nil];
    _ignoreStateChange = NO;
    if (IS_OF_CLASS(args, NSArray)) {
        [self setOriginalQueue:[NSMutableArray arrayWithArray:args]];
    }
    else if (args) {
        [self setOriginalQueue:[NSMutableArray arrayWithObject:args]];
    } else {
        [self setOriginalQueue:nil];
    }
    if (!emptyPlaylist && playing) {
        [self play:nil];
    }
}

-(id)playlist {
    return self.originalQueue;
}

-(void)play:(id)args
{
    [self internalPlayOrResume];
}

-(id)getCurrentQueueItem {
    if (_queue) {
        NSInteger index = self.indexOfNowPlayingItem;
        if (index >=0 && index < [_queue count]) {
            return [_queue objectAtIndex:index];
        }
    }
    return nil;
}

-(void)internalPlayOrResume {
    // indicate we're going to start playing
    if (![[self stopped] boolValue])
    {
        if (![player isExecuting]) {
            [player play];
        }
    } else {
        if (![[TiAudioSession sharedSession] canPlayback]) {
            [self throwException:@"Improper audio session mode for playback"
                       subreason:[[TiAudioSession sharedSession] sessionMode]
                        location:CODELOCATION];
        }
        if ([_queue count]) {
            self.indexOfNowPlayingItem = 0;
        } else {
            self.indexOfNowPlayingItem = NSNotFound;
        }
    }
    
}

-(void)start:(id)args
{
    [self internalPlayOrResume];
}

-(void)stop:(id)args
{
    [self cleanPlayer];
    _needsPlayAfterInterruption = NO;
    _currentItem = nil;
    RELEASE_TO_NIL(_currentCuePoint)
    [self updateState:STATE_STOPPED];
}

-(void)pause:(id)args
{
    [self stop:args];
}


-(void)playPause:(id)args
{
    if ([[self playing] boolValue]) {
        [self pause:args];
    } else {
        [self play:args];
    }
}

-(void)next:(id)args
{
}

-(void)previous:(id)args
{
}

-(void)seek:(id)args {
    
}

NSDictionary* metadataKeys;

-(NSDictionary*)metadataKeys
{
    if (metadataKeys == nil) {
        metadataKeys = [@{
                          @"title":MPMediaItemPropertyTitle,
                          @"artist":MPMediaItemPropertyArtist,
                          @"album":MPMediaItemPropertyAlbumTitle,
                          @"duration":MPMediaItemPropertyPlaybackDuration,
                          @"tracknumber":MPMediaItemPropertyAlbumTrackNumber,
                          @"date":MPMediaItemPropertyReleaseDate,
                          @"year":MPMediaItemPropertyReleaseDate,
                          @"composer":MPMediaItemPropertyComposer,
                          @"comment":MPMediaItemPropertyComments,
                          @"genre":MPMediaItemPropertyGenre,
                          @"compilation":MPMediaItemPropertyIsCompilation
                          } retain];
    }
    return metadataKeys;
}


-(void)updateMetadataAlbumArt:(UIImage*)image {
    NSMutableDictionary* mediaInfo  = [[NSMutableDictionary alloc] initWithDictionary:[[MPNowPlayingInfoCenter defaultCenter] nowPlayingInfo]];
    MPMediaItemArtwork *albumArt = [[MPMediaItemArtwork alloc] initWithImage: image];
    [mediaInfo setObject:albumArt forKey:MPMediaItemPropertyArtwork];
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:mediaInfo];
    [mediaInfo release];
}

-(id)sanitizeURL:(id)value
{
    if (value == [NSNull null])
    {
        return nil;
    }
    
    if([value isKindOfClass:[NSString class]])
    {
        NSURL * result = [TiUtils toURL:value proxy:self];
        if (result != nil)
        {
            return result;
        }
    }
    
    return value;
}

-(void)startImageLoad:(NSURL *)url;
{
    [self cancelPendingImageLoads]; //Just in case we have a crusty old urlRequest.
    NSDictionary* info = nil;
    NSNumber* hires = [self valueForKey:@"hires"];
    if (hires) {
        info = [NSDictionary dictionaryWithObject:hires forKey:@"hires"];
    }
    urlRequest = [[[ImageLoader sharedLoader] loadImage:url delegate:self options:[self valueForUndefinedKey:@"httpOptions"] userInfo:info] retain];
}

-(void)cancelPendingImageLoads
{
    // cancel a pending request if we have one pending
    if (urlRequest!=nil)
    {
        [urlRequest cancel];
        RELEASE_TO_NIL(urlRequest);
    }
}

-(void)imageLoadSuccess:(ImageLoaderRequest*)request image:(id)image
{
    if (request != urlRequest || !image)
    {
        return;
    }
    [self updateMetadataAlbumArt:image];
    RELEASE_TO_NIL(urlRequest);
}

-(void)imageLoadFailed:(ImageLoaderRequest*)request error:(NSError*)error
{
    if (request == urlRequest)
    {
        RELEASE_TO_NIL(urlRequest);
    }
}

-(void)imageLoadCancelled:(ImageLoaderRequest *)request
{
}

-(UIImage*)downloadAlbumArt:(id)obj {
    if (!obj) return nil;
    if (IS_OF_CLASS(obj, UIImage)) {
        return obj;
    }
    if (IS_OF_CLASS(obj, TiBlob)) {
        return [(TiBlob*)obj image];
    }
    
    NSURL* imageURL = [self sanitizeURL:obj];
    if (![imageURL isKindOfClass:[NSURL class]]) {
        NSLog(@"[ERROR] invalid image type: \"%@\" is not a TiBlob, URL, TiFile",imageURL);
        return nil;
    }
    NSURL *url = [TiUtils toURL:[imageURL absoluteString] proxy:self];
    urlRequest = [[[ImageLoader sharedLoader] loadImage:url delegate:self options:[self valueForUndefinedKey:@"httpOptions"] userInfo:@{@"hires":@(YES)}] retain];
    return nil;
}

-(void)updateMetadata:(NSDictionary*)data {
    NSMutableDictionary* mediaInfo  = [[NSMutableDictionary alloc] init];
    NSDictionary* metadataKeys = [self metadataKeys];
    [data enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        NSString* realKey = [metadataKeys objectForKey:key];
        if (realKey) {
            [mediaInfo setObject:obj forKey:realKey];
        }
    }];
    if (_duration > 0) {
        [mediaInfo setObject:@(_duration) forKey:MPMediaItemPropertyPlaybackDuration];
    }
    UIImage* covertart = [self downloadAlbumArt:[data objectForKey:@"artwork"]];
    if (covertart) {
        MPMediaItemArtwork *albumArt = [[MPMediaItemArtwork alloc] initWithImage: covertart];
        [mediaInfo setObject:albumArt forKey:MPMediaItemPropertyArtwork];
    }
    [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:mediaInfo];
    [mediaInfo release];
}

-(void)updateMetadata {
    if (_currentCuePoint) {
        [self updateMetadata:[self convertCuePoint:_currentCuePoint]];
    }
    else if (IS_OF_CLASS(_currentItem, NSDictionary)) {
        [self updateMetadata:_currentItem];
    }
    else {
        [self updateMetadata:[self valueForKey:@"metadata"]];
    }
}


MAKE_SYSTEM_PROP(STATE_INITIALIZED,STATE_INITIALIZED);
MAKE_SYSTEM_PROP(STATE_ERROR,STATE_ERROR);
MAKE_SYSTEM_PROP(STATE_PLAYING,STATE_PLAYING);
MAKE_SYSTEM_PROP(STATE_BUFFERING,STATE_BUFFERING);
MAKE_SYSTEM_PROP(STATE_STOPPED,STATE_STOPPED);
MAKE_SYSTEM_PROP(STATE_PAUSED,STATE_PAUSED);

-(int)stateFromRate:(float)rate {
    if (rate == 0.0f) {
        return STATE_PAUSED;
    }
    return STATE_PLAYING;
}

-(void)updateState:(int)newState {
    if (_state != newState) {
        _state = newState;
        if (_state == STATE_PLAYING) {
            [[TiAudioSession sharedSession] startAudioSession];
        } else {
            if (_state == STATE_STOPPED || _state == STATE_ERROR) {
                _duration = 0;
                _currentProgress = 0;
                [self removePlayerTimeObserver];
                [[TiAudioSession sharedSession] stopAudioSession];
                
            }
        }
        if (!_ignoreStateChange && [self _hasListeners:@"state"])
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:@(_state),@"state",[self stateToString:_state],@"description",nil];
            [self fireEvent:@"state" withObject:event checkForListener:NO];
        }
    }
}

-(NSString*)stateToString:(NSInteger)state
{
    switch(state)
    {
        case STATE_INITIALIZED:
            return @"initialized";
        case STATE_ERROR:
            return @"error";
        case STATE_PLAYING:
            return @"playing";
        case STATE_BUFFERING:
            return @"buffering";
        case STATE_CONNECTING:
            return @"connecting";
        case STATE_PAUSED:
            return @"paused";
        case STATE_STOPPED:
            return @"stopped";
    }
}

-(NSString*)stateDescription:(id)arg
{
    ENSURE_SINGLE_ARG(arg,NSNumber);
    return [self stateToString:[TiUtils intValue:arg]];
}

- (void)handlePlayeEndedTrack {
    
    _duration = 0;
    _currentProgress = 0;
    if (!self.isLoadingAsset) {
        if (_repeatMode == MPMusicRepeatModeOne) {
            // Play the same track again
            self.indexOfNowPlayingItem = self.indexOfNowPlayingItem;
        } else {
            // Go to next track
            [self skipToNextItem];
        }
    }
}

#pragma mark Delegates

- (void)remoteControlEvent:(NSNotification*)note
{
    UIEvent *receivedEvent = [[note userInfo]objectForKey:@"event"];
    if (receivedEvent.type == UIEventTypeRemoteControl) {
        
        switch (receivedEvent.subtype) {
                
            case UIEventSubtypeRemoteControlTogglePlayPause:
                [self playPause: nil];
                break;
            case UIEventSubtypeRemoteControlPlay:
                [self play: nil];
                break;
            case UIEventSubtypeRemoteControlPause:
                [self pause: nil];
                break;
                
            default:
                break;
        }
    }
}

/// @name Required life-cycle methods

/**
 * Called when the player is successfully connected to the stream and is ready to start playing.
 *
 * @param player The TritonPlayer object that is connected.
 */

- (void)playerDidConnectToStream:(TritonPlayer *) player {
}

/**
 * Called when the player stopped properly after the stop method was called.
 *
 * @param player The TritonPlayer object whose stream was stopped.
 */

- (void)playerDidStopStream:(TritonPlayer *) player {
    [self updateState:STATE_STOPPED];
}

/// @name Handling connection errors

/**
 * Called when the network is unavailable, if the station is geoblocked or if the specified connection parameters for the station are invalid.
 *
 * @param player The TritonPlayer in which the connection failed.
 * @param error An NSError object describing the error that occurred.
 */

- (void)player:(TritonPlayer *) player didFailConnectingWithError:(NSError *) error {
    [self updateState:STATE_ERROR];
    if ([self _hasListeners:@"error"])
    {
        [self fireEvent:@"error" withObject:@{
                                              @"track":_currentItem?_currentItem:[NSNull null],
                                              @"index":@(self.indexOfNowPlayingItem)
                                              }errorCode:[error code] message:[TiUtils messageFromError:error]];
    }
    [self handlePlayeEndedTrack];
}


/// @name Handling cue point events

/**
 * Called when there's a Cue Point available to be processed. A NSDictionary is passed containing the Cue Point metadata. All the available keys are defined in CuePointEvent.h.
 * See STWCue_Metadata_Dictionary.pdf for more details on the available cue point information.
 *
 * @param player The player which is receiving cue point events
 * @param cuePointEvent A CuePointEvent object containing all cue point information.
 */
//static final HashMap<String, String> CUEPOINTS = new HashMap<String, String>() {
//    {
//        put(CuePoint.TRACK_ALBUM_NAME, "album");
//        put(CuePoint.TRACK_ARTIST_NAME, "artist");
//        put(CuePoint.CUE_TITLE, "title");
//        put(CuePoint.TRACK_GENRE, "genre");
//        put(CuePoint.CUE_TIME_DURATION, "duration");
//        put(CuePoint.TRACK_ALBUM_YEAR, "year");
//        put(CuePoint.TRACK_COVER_URL, "artwork");
//    }
//};

-(NSDictionary *)cuePointConvert
{
    static NSDictionary *cuePointConvert = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        cuePointConvert = [@{
                             TrackAlbumNameKey:@"album",
                             TrackArtistNameKey:@"artist",
                             CommonCueTitleKey:@"title",
                             TrackGenreKey:@"genre",
                             CommonCueTimeDurationKey:@"duration",
                             TrackAlbumYearKey:@"year",
                             TrackCoverURLKey:@"artwork",
                             }retain];
    });
    return cuePointConvert;
}
-(NSDictionary*)convertCuePoint:(CuePointEvent *)cuePointEvent {
    NSMutableDictionary* result = [NSMutableDictionary dictionaryWithDictionary:cuePointEvent.data];
    [[self cuePointConvert] enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        id value = [result objectForKey:key];
        if (value) {
            [result setObject:value forKey:obj];
            [result removeObjectForKey:key];
        }
    }];
    return result;
}

- (void)player:(TritonPlayer *) player didReceiveCuePointEvent:(CuePointEvent *)cuePointEvent {
    _currentCuePoint = [cuePointEvent retain];
    [self updateMetadata];
    if ([self _hasListeners:@"change"])
    {
        NSDictionary *event = @{
                                @"track":[self convertCuePoint:_currentCuePoint],
                                @"duration":@(_duration),
                                @"index":@(self.indexOfNowPlayingItem)
                                };
        [self fireEvent:@"change" withObject:event checkForListener:NO];
    }
}

/// @name Handling interruptions

/**
 * Notifies that an audio interruption is about to start (alarm, phone call, etc.). The application has the opportunity to take the proper actions: stop the player, lower the volume, etc.
 *
 * @param player The TritonPlayer object which is being interrupted.
 */

- (void)playerBeginInterruption:(TritonPlayer *) tPlayer {
    _needsPlayAfterInterruption = [tPlayer isExecuting];
    if (_needsPlayAfterInterruption) {
        [tPlayer stop];
    }
}

/**
 * Notifies about a finished interruption. It's the proper moment to resume the player, raise the volume, etc.
 *
 * @param player The TritonPlayer object whose interruption is ending.
 */

- (void)playerEndInterruption:(TritonPlayer *) tPlayer {
    if (_needsPlayAfterInterruption) {
        [tPlayer play];
    }
}

/// @name Optional life-cycle methods

/**
 * Called during the Player's attempt to connect to the stream, before it connects and starts playing. It can be used to give feedback of the operation's progress, show an activity indicator etc.
 *
 * @param player The TritonPlayer object whose stream is connecting.
 */

- (void)playerIsConnectingToStream:(TritonPlayer *) player {
    [self updateState:STATE_CONNECTING];
}

/**
 * Called when the player just started to play the stream.
 *
 * @param player The TritonPlayer object that is playing.
 */

- (void)playerDidStartPlaying:(TritonPlayer *) thePlayer {
    [self updateState:STATE_PLAYING];
}

- (void) player:(TritonPlayer *) thePlayer didChangeState:(TDPlayerState) state {
    switch(state) {
    case kTDPlayerStateCompleted:
            [self handlePlayeEndedTrack];
        break;
        
    case kTDPlayerStateConnecting:
            [self updateState:STATE_CONNECTING];
        break;
        
    case kTDPlayerStateError: {
        [self updateState:STATE_ERROR];
        if ([self _hasListeners:@"error"])
        {
            NSError *error = thePlayer.error;
            [self fireEvent:@"error" withObject:@{
                                                  @"track":_currentItem?_currentItem:[NSNull null],
                                                  @"index":@(self.indexOfNowPlayingItem)
                                                  } errorCode:[error code] message:[TiUtils messageFromError:error]];
        }
        break;
    }
    case kTDPlayerStatePlaying:
            [self updateState:STATE_PLAYING];
        break;
        
    case kTDPlayerStateStopped:
            [self updateState:STATE_STOPPED];
        break;
        
    case kTDPlayerStatePaused:
            [self updateState:STATE_PAUSED];
        break;
        
    default:
        break;
    }

}

-(void)player:(TritonPlayer *)thePlayer didReceiveInfo:(TDPlayerInfo)info andExtra:(NSDictionary *)extra {
    
    switch (info) {
        case kTDPlayerInfoConnectedToStream:
            break;
            
        case kTDPlayerInfoBuffering:
            if ([self _hasListeners:@"buffering"])
            {
                [self fireEvent:@"buffering" withObject:@{
                                                          @"progress":extra[InfoBufferingPercentageKey]
                                                          } checkForListener:NO];
            }
            break;
            
        case kTDPlayerInfoForwardedToAlternateMount:
            break;
    }
}

@end
