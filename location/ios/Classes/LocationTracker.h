//
//  LocationTracker.h
//  Location
//
//  Created by Rick
//  Copyright (c) 2014 Location. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "LocationShareModel.h"

@protocol LocationTrackerDelegate<NSObject>
@optional
-(void)didUpdateLocation:(CLLocation*)location;
@end

@interface LocationTracker : NSObject <CLLocationManagerDelegate>

@property (nonatomic, retain) CLLocation* myLastLocation;

@property (nonatomic) BOOL useSignificantChanges;
@property (nonatomic) BOOL useSignificantChangesInBackground;
@property (nonatomic) NSTimeInterval updateInterval;
@property (nonatomic) NSTimeInterval locationAge;
@property (nonatomic) NSTimeInterval minAge;
@property (nonatomic) float accuracyInMeters;
@property (nonatomic) CLLocationDistance distanceFilter;
@property (nonatomic) CLLocationAccuracy desiredAccuracy;

@property (strong,nonatomic) LocationShareModel * shareModel;

@property (nonatomic, retain) CLLocation* myLocation;


@property (nonatomic, readwrite, assign) id<LocationTrackerDelegate> delegate;

+ (CLLocationManager *)sharedLocationManager;

- (void)startLocationTracking;
- (void)stopLocationTracking;
- (CLLocation*)getLocation;
-(BOOL)started;

@end
