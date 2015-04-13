//
//  TritonPlayer.h
//  TritonPlayer
//
//  Copyright 2014 Triton Digital. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <AudioToolbox/AudioQueue.h>

// Settings dictionary keys

extern NSString *const SettingsEnableLocationTrackingKey;
extern NSString *const SettingsStationNameKey;
extern NSString *const SettingsMountKey;
extern NSString *const SettingsAppNameKey;
extern NSString *const SettingsBroadcasterKey;
extern NSString *const SettingsStreamParamsExtraKey;

/// Errors generated by TritonPlayer
typedef NS_ENUM(NSInteger, TDPlayerError) {
    
    /// The specified mount doesn’t exist
    TDPlayerMountNotFoundError = 3000,
    
    /// The mount is geoblocked
    TDPlayerMountGeoblockedError = 3001,
    
    /// A required parameter is missing or an invalid parameter was sent
    TDPlayerMountBadRequestError = 3002,
    
    /// The version of the provisioning doesn't exist
    TDPlayerMountNotImplemntedError = 3003,
    
    /// The host doesn't exist
    TDPlayerHostNotFoundError = 3004
};

@class CuePointEvent;
@class TritonPlayer;

/**
 * TritonPlayerDelegate defines methods you can implement to handle streaming state notifications and to receive cue point events.
 */
@protocol TritonPlayerDelegate

@required

/// @name Required life-cycle methods

/**
 * Called when the player is successfully connected to the stream and is ready to start playing.
 *
 * @param player The TritonPlayer object that is connected.
 */

- (void)playerDidConnectToStream:(TritonPlayer *) player;

/**
 * Called when the player stopped properly after the stop method was called.
 *
 * @param player The TritonPlayer object whose stream was stopped.
 */

- (void)playerDidStopStream:(TritonPlayer *) player;

/// @name Handling connection errors

/**
 * Called when the network is unavailable, if the station is geoblocked or if the specified connection parameters for the station are invalid.
 *
 * @param player The TritonPlayer in which the connection failed.
 * @param error An NSError object describing the error that occurred.
 */

- (void)player:(TritonPlayer *) player didFailConnectingWithError:(NSError *) error;


/// @name Handling cue point events

/**
 * Called when there's a Cue Point available to be processed. A NSDictionary is passed containing the Cue Point metadata. All the available keys are defined in CuePointEvent.h.
 * See STWCue_Metadata_Dictionary.pdf for more details on the available cue point information.
 *
 * @param player The player which is receiving cue point events
 * @param cuePointEvent A CuePointEvent object containing all cue point information.
 */

- (void)player:(TritonPlayer *) player didReceiveCuePointEvent:(CuePointEvent *)cuePointEvent;

@optional

/// @name Deprecated

/**
 * Called when the played station is blocked for the player's location/region.
 *
 * @param player The TritonPlayer object whose station is geo-blocked.
 * @deprecated Use player:didFailConnectingWithError: instead
 */

- (void)playerWasGeoBlocked:(TritonPlayer *) player;

/// @name Handling interruptions

/**
 * Notifies that an audio interruption is about to start (alarm, phone call, etc.). The application has the opportunity to take the proper actions: stop the player, lower the volume, etc.
 *
 * @param player The TritonPlayer object which is being interrupted.
 */

- (void)playerBeginInterruption:(TritonPlayer *) player;

/**
 * Notifies about a finished interruption. It's the proper moment to resume the player, raise the volume, etc.
 *
 * @param player The TritonPlayer object whose interruption is ending.
 */

- (void)playerEndInterruption:(TritonPlayer *) player;

/// @name Optional life-cycle methods

/**
 * Called during the Player's attempt to connect to the stream, before it connects and starts playing. It can be used to give feedback of the operation's progress, show an activity indicator etc.
 *
 * @param player The TritonPlayer object whose stream is connecting.
 */

- (void)playerIsConnectingToStream:(TritonPlayer *) player;

/**
 * Called when the player just started to play the stream.
 *
 * @param player The TritonPlayer object that is playing.
 */

- (void)playerDidStartPlaying:(TritonPlayer *) player;

@end

/** 
 * TritonPlayer handles the playback of stations provided by Triton Digital. It also supports receiving CuePoint events with metadata for track information, ads etc.
 */

@interface TritonPlayer : NSObject

/// @name Querying player information

/**
 * Tells whether the player is attempting to connect to the network.
 */

@property (readonly) BOOL operationInProgress;

/**
 * Tells whether the player is streaming audio.
 */

@property (readonly) BOOL isExecuting;

/**
 * The underlying audio queue which playbacks the audio. Use this when you need to process or analyze the audio data. Ex. When building a spectrum analyzer.
 *
 * @returns The underlying AudioQueue
 */

- (AudioQueueRef)getAudioQueue;

/**
 * Returns the current library version
 *
 * @return A string containing the current library version.
 */

- (NSString *)getLibVersion;

/**
 * Informs if the network is available.
 *
 * @return Whether the network is available.
 */

- (BOOL)isNetworkReachable;

/// @name Location targeting

/**
 * The most recent user location available for audience targeting.
 */

@property (readonly) CLLocation *targetingLocation;

/// @name Handling interruptions

/**
 * When an interruption ends (phone call, alarm, siri etc.) this flags will be true when it is appropriate to resume playback without waiting for user input.
 *
 * If the user ignored a call, it means that he/she wants to continue listening to the app and the flag will be YES. On the other side, if the interruption was caused by the Music app or other audio app being executed,
 * it means that he/should explicitly play again to continue listening to the stream and the flag will retur NO.
 */

@property (readonly) BOOL shouldResumePlaybackAfterInterruption;

/// @name Creating a TritonPlayer

/** 
 * Instantiate a new player using the specified settings
 *
 * @param inDelegate The delegate for handling stream callbacks and CuePoint events
 * @param settings A NSDictionary containing station parameters.
 */

- (id)initWithDelegate:(id)inDelegate andSettings:(NSDictionary *) settings;

/// @name Updating player settings

/** 
 * Update player settings. All the information passed overrides the current settings and will take effect the next time the play method is called.
 *
 * When changing to a new station, this method must be called before calling play on the new station.
 *
 * @param settings A NSDictionary containing station parameters.
 */

- (BOOL) updateSettings:(NSDictionary *) settings;

/// @name Play/Stop

/** 
 * Plays the current stream with the configuration from the settings dictionary.
 *
 * @see updateSettings:
 */

- (void)play;

/** 
 * Stops the current stream
 */

- (void)stop;

/// @name Controlling the volume

/**
 * Mute current playing audio
 */

- (void)mute;

/**
 * Unumte current playing audio
 */

- (void)unmute;

/**
 * Set volume of current playing audio
 *
 * @param volume a float between 0-1.
 */

- (void)setVolume:(float)volume;

@end