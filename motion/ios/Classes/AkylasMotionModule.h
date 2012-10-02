/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"
#import <CoreMotion/CMMotionManager.h>

@interface AkylasMotionModule : TiModule 
{
    CMMotionManager *motionManager;
	CMDeviceMotionHandler motionHandler;
	NSOperationQueue* motionQueue;
	BOOL accelerometerRegistered;
	BOOL gyroscopeRegistered;
	BOOL magnetometerRegistered;
	BOOL orientationRegistered;
	BOOL motionRegistered;
    BOOL computeRotationMatrix;
    float updateInterval;
    NSTimeInterval bootTimestamp;
    //    CMAttitude* referenceAttitude;
    //    BOOL usingReference;
}
@property(nonatomic,readonly) NSNumber *STANDARD_GRAVITY;


@end
