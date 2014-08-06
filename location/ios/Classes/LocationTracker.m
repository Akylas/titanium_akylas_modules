//
//  LocationTracker.m
//  Location
//
//  Created by Rick
//  Copyright (c) 2014 Location All rights reserved.
//

#import "LocationTracker.h"

#define LATITUDE @"latitude"
#define LONGITUDE @"longitude"
#define ACCURACY @"theAccuracy"

@implementation LocationTracker
{
    BOOL _started;
    BOOL _shouldBeStarted;
}

+ (CLLocationManager *)sharedLocationManager {
	static CLLocationManager *_locationManager;
	
	@synchronized(self) {
		if (_locationManager == nil) {
			_locationManager = [[CLLocationManager alloc] init];
		}
	}
	return _locationManager;
}

- (id)init {
	if (self==[super init]) {
        _started = _shouldBeStarted = NO;
        _updateInterval = 60;
        _locationAge = 30;
        _minAge = 0;
        _accuracyInMeters = 2000;
        _desiredAccuracy = kCLLocationAccuracyBestForNavigation;
        _distanceFilter = 100;
        _useSignificantChanges = YES;
        _useSignificantChangesInBackground = YES;
        //Get the share model and also initialize myLocationArray
        self.shareModel = [LocationShareModel sharedModel];
        self.shareModel.myLocationArray = [[NSMutableArray alloc]init];
	}
	return self;
}

-(void)applicationEnterBackground{
    CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = _desiredAccuracy;
    locationManager.distanceFilter = _distanceFilter;
    
    if (_useSignificantChanges) {
        [locationManager stopMonitoringSignificantLocationChanges];
    }
    else {
        [locationManager stopUpdatingLocation];
    }

    
    
    if (_useSignificantChangesInBackground) {
        [locationManager startMonitoringSignificantLocationChanges];
    }
    else {
        //Use the BackgroundTaskManager to manage all the background Task
        self.shareModel.bgTask = [BackgroundTaskManager sharedBackgroundTaskManager];
        [locationManager startUpdatingLocation];
    }
}


-(void)applicationEnterForeground{
    if (_shouldBeStarted && !_started) {
        [self startLocationTracking];
        return;
    }
    CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = _desiredAccuracy;
    locationManager.distanceFilter = _distanceFilter;
    
    
    if (_useSignificantChangesInBackground) {
        [locationManager stopMonitoringSignificantLocationChanges];
    }
    else {
        [self.shareModel.bgTask endAllBackgroundTasks];
        if (self.shareModel.timer) {
            [self.shareModel.timer invalidate];
            self.shareModel.timer = nil;
        }
        
        [locationManager stopUpdatingLocation];
    }
    
    
    if (_useSignificantChanges) {
        [locationManager startMonitoringSignificantLocationChanges];
    }
    else {
        [locationManager startUpdatingLocation];
    }
}

- (void) restartLocationUpdates
{
//    NSLog(@"restartLocationUpdates");
    
    if (self.shareModel.timer) {
        [self.shareModel.timer invalidate];
        self.shareModel.timer = nil;
    }
    
    if (_shouldBeStarted && !_started) {
        [self startLocationTracking];
        return;
    }
    
    CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
    locationManager.delegate = self;
    locationManager.desiredAccuracy = _desiredAccuracy;
    locationManager.distanceFilter = _distanceFilter;
    [locationManager startUpdatingLocation];
}


- (void)startLocationTracking {
    if (_started) return;
    _shouldBeStarted = YES;
	if ([CLLocationManager locationServicesEnabled] == NO) {
	} else {
        
        CLAuthorizationStatus authorizationStatus= [CLLocationManager authorizationStatus];
        
        if(authorizationStatus == kCLAuthorizationStatusDenied || authorizationStatus == kCLAuthorizationStatusRestricted){
        } else {
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationEnterBackground) name:UIApplicationDidEnterBackgroundNotification object:nil];
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];
            _started = YES;
            
            CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
            locationManager.delegate = self;
            locationManager.desiredAccuracy = _desiredAccuracy;
            locationManager.distanceFilter = _distanceFilter;
            
            UIApplicationState state = [UIApplication sharedApplication].applicationState;
            if ((state == UIApplicationStateActive && _useSignificantChanges) ||
                (state == UIApplicationStateBackground && _useSignificantChangesInBackground)) {
                [locationManager startMonitoringSignificantLocationChanges];
            }
            else {
                [locationManager startUpdatingLocation];
            }
        }
	}
}


- (void)stopLocationTracking {
    if (_started == NO) return;
    _started = _shouldBeStarted = NO;
    if (self.shareModel.timer) {
        [self.shareModel.timer invalidate];
        self.shareModel.timer = nil;
    }
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
	CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
    
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if ((state == UIApplicationStateActive && _useSignificantChanges) ||
        (state == UIApplicationStateBackground && _useSignificantChangesInBackground)) {
        [locationManager stopMonitoringSignificantLocationChanges];
    }
    else {
        [locationManager stopUpdatingLocation];
    }
}

#pragma mark - CLLocationManagerDelegate Methods

-(void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations{
    
//    NSLog(@"locationManager didUpdateLocations");
    
    for(int i=0;i<locations.count;i++){
        CLLocation * newLocation = [locations objectAtIndex:i];
        
        NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];
        
        if (locationAge > _locationAge) //sec
        {
            continue;
        }
        CLLocationCoordinate2D theLocation = newLocation.coordinate;
        CLLocationAccuracy theAccuracy = newLocation.horizontalAccuracy;
        
        //Select only valid location and also location with good accuracy
        if(newLocation != nil && theAccuracy > 0
           && theAccuracy < _accuracyInMeters
           && (self.myLastLocation == nil || [newLocation.timestamp timeIntervalSinceDate:self.myLastLocation.timestamp] >= _minAge)
           && ( !( theLocation.latitude == 0.0 && theLocation.longitude == 0.0 ))){
            
            self.myLastLocation = newLocation;
            
//            NSMutableDictionary * dict = [[NSMutableDictionary alloc]init];
//            [dict setObject:[NSNumber numberWithFloat:theLocation.latitude] forKey:@"latitude"];
//            [dict setObject:[NSNumber numberWithFloat:theLocation.longitude] forKey:@"longitude"];
//            [dict setObject:[NSNumber numberWithFloat:theAccuracy] forKey:@"theAccuracy"];
            
            //Add the vallid location with good accuracy into an array
            //Every 1 minute, I will select the best location based on accuracy and send to server
            [self.shareModel.myLocationArray addObject:newLocation];
            
            if (_delegate && [_delegate respondsToSelector:@selector(didUpdateLocation:)]) {
                [_delegate didUpdateLocation:newLocation];
            }
        }
    }
    
    [self handleRestart];

}

-(void)handleRestart {
    
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if ((state == UIApplicationStateActive && _useSignificantChanges) ||
        (state == UIApplicationStateBackground && _useSignificantChangesInBackground)) {
        return;
    }
    
    //If the timer still valid, return it (Will not run the code below)
    if (self.shareModel.timer) {
        return;
    }
    
    self.shareModel.bgTask = [BackgroundTaskManager sharedBackgroundTaskManager];
    [self.shareModel.bgTask beginNewBackgroundTask];
    
    //Restart the locationMaanger after 1 minute
    self.shareModel.timer = [NSTimer scheduledTimerWithTimeInterval:self.updateInterval target:self
                                                           selector:@selector(restartLocationUpdates)
                                                           userInfo:nil
                                                            repeats:NO];
    
    //Will only stop the locationManager after 10 seconds, so that we can get some accurate locations
    //The location manager will only operate for 10 seconds to save battery
    NSTimer * delay10Seconds;
    delay10Seconds = [NSTimer scheduledTimerWithTimeInterval:10 target:self
                                                    selector:@selector(stopLocationDelayBy10Seconds)
                                                    userInfo:nil
                                                     repeats:NO];
}


//Stop the locationManager
-(void)stopLocationDelayBy10Seconds{
    UIApplicationState state = [UIApplication sharedApplication].applicationState;
    if (state == UIApplicationStateActive) {
        return;
    }
    CLLocationManager *locationManager = [LocationTracker sharedLocationManager];
    [locationManager stopUpdatingLocation];
}


- (void)locationManager: (CLLocationManager *)manager didFailWithError: (NSError *)error
{
    switch([error code])
    {
        case kCLErrorDenied:{
            _started = NO;
            return;
        }
        default:
        {
            if (self.shareModel.timer) {
                [self.shareModel.timer invalidate];
                self.shareModel.timer = nil;
            }
            [self handleRestart];
        }
        break;
    }
}


//Send the location to Server
- (CLLocation*)getLocation {
    
    
    // Find the best location from the array based on accuracy
    CLLocation * myBestLocation = nil;
    
    for(int i=0;i<self.shareModel.myLocationArray.count;i++){
        CLLocation * currentLocation = [self.shareModel.myLocationArray objectAtIndex:i];
        
        if(i==0)
            myBestLocation = currentLocation;
        else{
            if(currentLocation.horizontalAccuracy <= myBestLocation.horizontalAccuracy){
                myBestLocation = currentLocation;
            }
        }
    }
//    NSLog(@"My Best location:%@",myBestLocation);
    
    //If the array is 0, get the last location
    //Sometimes due to network issue or unknown reason, you could not get the location during that  period, the best you can do is sending the last known location to the server
    if(self.shareModel.myLocationArray.count==0)
    {
//        NSLog(@"Unable to get location, use the last known location");

        self.myLocation=self.myLastLocation;
        
    }else{
        self.myLocation = myBestLocation;
    }
    
//    NSLog(@"Send to Server: Latitude(%f) Longitude(%f) Accuracy(%f)",self.myLocation.latitude, self.myLocation.longitude,self.myLocationAccuracy);
    
    //TODO: Your code to send the self.myLocation and self.myLocationAccuracy to your server
    
    //After sending the location to the server successful, remember to clear the current array with the following code. It is to make sure that you clear up old location in the array and add the new locations from locationManager
    [self.shareModel.myLocationArray removeAllObjects];
    self.shareModel.myLocationArray = nil;
    self.shareModel.myLocationArray = [[NSMutableArray alloc]init];
    return self.myLastLocation;
}

-(BOOL)started {
    return _started;
}


@end
