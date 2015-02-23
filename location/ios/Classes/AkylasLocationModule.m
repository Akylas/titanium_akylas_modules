
#import "AkylasLocationModule.h"
#import "NetworkModule.h"
#import "TiProperties.h"
#import "KrollBridge.h"
#import "TiApp.h"
#import "TiFileSystemHelper.h"

@implementation AkylasLocationModule
{
    LocationTracker* _locationTracker;
    NSURL* _scriptUrl;
    CLLocation* _lastLocation;
    NSTimeInterval _lastTimeForLocation;
    BOOL hasCallback;
    CLLocationDistance _distanceFilter;
    double _desiredAccuracy;
    BOOL _useSignificantChanges;
    BOOL _useSignificantChangesInBackground;
    KrollBridge *bridge;
}
@synthesize distanceFilter = _distanceFilter, desiredAccuracy = _desiredAccuracy, useSignificantChanges = _useSignificantChanges, useSignificantChangesInBackground = _useSignificantChangesInBackground;


MAKE_SYSTEM_PROP_DBL(ACCURACY_BEST,kCLLocationAccuracyBest);
MAKE_SYSTEM_PROP_DBL(ACCURACY_HIGH,kCLLocationAccuracyBest);
MAKE_SYSTEM_PROP_DBL(ACCURACY_NEAREST_TEN_METERS,kCLLocationAccuracyNearestTenMeters);
MAKE_SYSTEM_PROP_DBL(ACCURACY_HUNDRED_METERS,kCLLocationAccuracyHundredMeters);
MAKE_SYSTEM_PROP_DBL(ACCURACY_KILOMETER,kCLLocationAccuracyKilometer);
MAKE_SYSTEM_PROP_DBL(ACCURACY_THREE_KILOMETERS,kCLLocationAccuracyThreeKilometers);
MAKE_SYSTEM_PROP_DBL(ACCURACY_LOW, kCLLocationAccuracyThreeKilometers);
MAKE_SYSTEM_PROP(ACCURACY_BEST_FOR_NAVIGATION, kCLLocationAccuracyBestForNavigation);//Since 2.1.3
MAKE_SYSTEM_STR(APP_PROPERTY_SCRIPT, @"akylas.location.script");
MAKE_SYSTEM_STR(APP_PROPERTY_LAST_LOCATION, @"akylas.location.lastLocation");


- (id)init
{
    if ((self = [super init])) {
        _lastTimeForLocation = 0;
        hasCallback = NO;
        _lastLocation = nil;
        _minAge = _backgroundMinAge = 60;
        _distanceFilter = 100;
        _desiredAccuracy = kCLLocationAccuracyThreeKilometers;
    }
    return self;
}

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.location";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
    
	[super startup];
    NSString* scriptUrl = [TiProperties getString:[self APP_PROPERTY_SCRIPT] defaultValue:nil];
    if (scriptUrl) {
        bridge = [[KrollBridge alloc] initWithHost:[TiApp app]];
        _scriptUrl = [[NSURL fileURLWithPath:[TiFileSystemHelper pathFromComponents:@[scriptUrl]]] retain];
    }
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
    [self stop:nil];
    if (bridge) {
		[bridge performSelector:@selector(shutdown:) withObject:nil];
    }
	// you *must* call the superclass
	[super shutdown:sender];
}


#pragma mark Cleanup 

-(void)dealloc
{
	// release any resources that have been retained by the module
    if (_locationTracker) {
        _locationTracker.delegate = nil;
        RELEASE_TO_NIL(_locationTracker)
    }
    RELEASE_TO_NIL(bridge);
    RELEASE_TO_NIL(_lastLocation)
    RELEASE_TO_NIL(_scriptUrl)
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications
//
//-(void)_listenerAdded:(NSString *)type count:(int)count
//{
//	if (count == 1 && [type isEqualToString:@"my_event"])
//	{
//		// the first (of potentially many) listener is being added 
//		// for event named 'my_event'
//	}
//}
//
//-(void)_listenerRemoved:(NSString *)type count:(int)count
//{
//	if (count == 0 && [type isEqualToString:@"my_event"])
//	{
//		// the last listener called for event named 'my_event' has
//		// been removed, we can optionally clean up any resources
//		// since no body is listening at this point for that event
//	}
//}


-(BOOL)locationFarEnough:(CLLocation*) loc1 fromLocation:(CLLocation*) loc2{
    if (!loc2) return true;
    float dist = [loc2 distanceFromLocation:loc1];
    return dist > _distanceFilter;
}



-(LocationTracker*) getLocationTracker {
    if (_locationTracker == nil) {
        _locationTracker = [[LocationTracker alloc]init];
        _locationTracker.distanceFilter = self.distanceFilter;
        _locationTracker.desiredAccuracy = self.desiredAccuracy;
        _locationTracker.delegate = self;
        _locationTracker.minAge = _minAge / 1000; //_locationTracker works in sec
    }
    return _locationTracker;
}

-(void)start:(id)unused
{
    ENSURE_UI_THREAD_1_ARG(unused)
    _lastTimeForLocation = 0;
    [[self getLocationTracker] startLocationTracking];
}

-(void)stop:(id)unused
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [_locationTracker stopLocationTracking];
}

+(NSDictionary*)dictFromLocation:(CLLocation*)location
{
    if (!location) return @{};
    return @{
             @"latitude":NUMDOUBLE(location.coordinate.latitude),
             @"longitude":NUMDOUBLE(location.coordinate.longitude),
             @"altitude":NUMDOUBLE(location.altitude),
             @"horizontalAccuracy":NUMDOUBLE(location.horizontalAccuracy),
             @"verticalAccuracy":NUMDOUBLE(location.verticalAccuracy),
             @"heading":NUMDOUBLE(location.course),
             @"speed":NUMDOUBLE(location.speed),
             @"timestamp":NUMDOUBLE([location.timestamp timeIntervalSince1970]*1000),
             };
}

-(void)didUpdateLocation:(CLLocation*)location {
    if (!hasCallback && !_scriptUrl) return;
    NSTimeInterval time = [location.timestamp timeIntervalSince1970];
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if ((state == UIApplicationStateBackground && time - _lastTimeForLocation < self.backgroundMinAge) ||
        (state == UIApplicationStateActive && time - _lastTimeForLocation < self.minAge)){
        return;
    }
    
    if ([self locationFarEnough:location fromLocation:_lastLocation]) {
        _lastTimeForLocation = time;
        _lastLocation = [location retain];
        NSDictionary * eventDict = [AkylasLocationModule dictFromLocation:location];
        [TiProperties setString:[TiUtils jsonStringify:eventDict] forKey:[self APP_PROPERTY_LAST_LOCATION]];
        if (hasCallback) {
            [self fireCallback:@"callback" withArg:eventDict withSource:self];
        }
        if (_scriptUrl) {
            if ([[bridge krollContext] running]) {
                //if already running no need to boot. Will be much faster!
                [bridge evalFile:[_scriptUrl path]];
            }
            else {
                //the preload is important, without it it is seen as the default bridge
                // and tries to load app.js
                [bridge boot:nil url:_scriptUrl preload:@{}];
            }
        }
    }
}

-(void)setDistanceFilter:(CLLocationDistance)distanceFilter
{
    _distanceFilter = distanceFilter;
    if (_locationTracker) {
        _locationTracker.distanceFilter = _distanceFilter;
    }
}

-(void)setDesiredAccuracy:(double)desiredAccuracy
{
    _desiredAccuracy = desiredAccuracy;
    if (_locationTracker) {
        _locationTracker.desiredAccuracy = _desiredAccuracy;
    }
}


-(void)setMinAge:(double)minAge
{
    _minAge = _backgroundMinAge = minAge;
    if (_locationTracker) {
        _locationTracker.minAge = _minAge / 1000; //_locationTracker works in sec
    }
}


-(void)setUseSignificantChanges:(BOOL)useSignificantChanges
{
    _useSignificantChanges = useSignificantChanges;
    if (_locationTracker) {
        _locationTracker.useSignificantChanges = _useSignificantChanges;
    }
}

-(void)setUseSignificantChangesInBackground:(BOOL)useSignificantChangesInBackground
{
    _useSignificantChangesInBackground = useSignificantChangesInBackground;
    if (_locationTracker) {
        _locationTracker.useSignificantChangesInBackground = _useSignificantChangesInBackground;
    }
}


-(void)setCallback:(id)callback
{
    ENSURE_SINGLE_ARG(callback, KrollCallback)
    [self replaceValue:callback forKey:@"callback" notification:NO];
    hasCallback = callback != nil;
}

@end
