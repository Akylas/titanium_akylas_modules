/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasMotionModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "Ti3DMatrix.h"
#import <CoreMotion/CMMotionManager.h>
#import <CoreMotion/CMAltimeter.h>

@implementation AkylasMotionModule {
    NSRecursiveLock* lock;
    CMMotionManager *motionManager;
    CMAltimeter *altitudeManager;
    CMDeviceMotionHandler motionHandler;
    CMAltitudeHandler altitudeHandler;
    NSOperationQueue* _motionQueue;
    BOOL accelerometerRegistered;
    BOOL gyroscopeRegistered;
    BOOL rotationRegistered;
    BOOL magnetometerRegistered;
    BOOL barometerRegistered;
    BOOL orientationRegistered;
    BOOL motionRegistered;
    BOOL computeRotationMatrix;
    float updateInterval;
    NSTimeInterval bootTimestamp;
    //    BOOL usingReference;
    BOOL altitudeRegistered;
}

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"d01001a4-bf0a-4c9c-96ae-e2c2b3835830";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.motion";
}

#pragma mark Lifecycle

-(CMMotionManager*)motionManager
{
    [lock lock];
    if (motionManager==nil)
    {
        motionManager = [[CMMotionManager alloc] init];
        motionManager.deviceMotionUpdateInterval = updateInterval;
        motionManager.gyroUpdateInterval = updateInterval;
        motionManager.accelerometerUpdateInterval = updateInterval;
        motionManager.magnetometerUpdateInterval = updateInterval;
        motionHandler = Block_copy(^(CMDeviceMotion *motion, NSError *error){
            [self processMotionData:motion withError:error];});
        _motionQueue = [[NSOperationQueue alloc] init];
    }
    [lock unlock];
    return motionManager;
}
    
-(void)shutdownMotionManager
{
    [lock lock];
    if (motionManager == nil) {
        [lock unlock];
        return;
    }
    [motionManager stopDeviceMotionUpdates];

    RELEASE_TO_NIL_AUTORELEASE(motionManager);
    RELEASE_TO_NIL_AUTORELEASE(_motionQueue);
    RELEASE_TO_NIL(motionHandler);
    [lock unlock];
}

-(CMAltimeter*)altitudeManager
{
    if (altitudeManager==nil && [CMAltimeter isRelativeAltitudeAvailable])
    {
        altitudeManager = [[CMAltimeter alloc] init];
        altitudeHandler = Block_copy(^(CMAltitudeData *data, NSError *error){
            [self processAltitudeData:data withError:error];});

    }
    return altitudeManager;
}

-(void)shutdownAltitudeManager
{
    if (altitudeManager) {
        [altitudeManager stopRelativeAltitudeUpdates];
        RELEASE_TO_NIL_AUTORELEASE(motionManager);
    }
    RELEASE_TO_NIL(altitudeHandler);
}


-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
    
    // Get NSTimeInterval of uptime i.e. the delta: now - bootTime
    NSTimeInterval uptime = [NSProcessInfo processInfo].systemUptime;
    // Now since 1970
    NSTimeInterval nowTimeIntervalSince1970 = [[NSDate date] timeIntervalSince1970];    
    // Voila our offset
    bootTimestamp = nowTimeIntervalSince1970 - uptime ;
    
    accelerometerRegistered = FALSE;
    orientationRegistered = FALSE;
    gyroscopeRegistered = FALSE;
    magnetometerRegistered = FALSE;
    altitudeRegistered = FALSE;
    updateInterval = 0.030; // time between 2 data in seconds
    computeRotationMatrix = NO;
	
	NSLog(@"[INFO] %@ loaded",self);
}

//-(void)shutdown:(id)sender
//{
//	// this method is called when the module is being unloaded
//	// typically this is during shutdown. make sure you don't do too
//	// much processing here or the app will be quit forceably
//	
//	// you *must* call the superclass
//	[super shutdown:sender];
//}

#pragma mark Cleanup 

-(void)_destroy
{
    [self shutdownMotionManager];
    RELEASE_TO_NIL(lock);
    [self shutdownAltitudeManager];
//    RELEASE_TO_NIL(motionQueue);
    [super _destroy];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

-(void)_listenerAdded:(NSString *)type count:(NSInteger)count
{
	if (count == 1)
	{
		BOOL needsStart = FALSE;
        if (!altitudeRegistered && [type isEqualToString:@"altitude"] && [CMAltimeter isRelativeAltitudeAvailable])
        {
            altitudeRegistered = TRUE;
            if (!barometerRegistered) {
                TiThreadPerformBlockOnMainThread(^{
                    [[self altitudeManager] startRelativeAltitudeUpdatesToQueue:[NSOperationQueue currentQueue] withHandler:altitudeHandler];
                }, NO);
            }
            
        } else if (!barometerRegistered && [type isEqualToString:@"pressure"] && [CMAltimeter isRelativeAltitudeAvailable])
        {
            barometerRegistered = TRUE;
            if (!altitudeRegistered) {
                TiThreadPerformBlockOnMainThread(^{
                    [[self altitudeManager] startRelativeAltitudeUpdatesToQueue:[NSOperationQueue currentQueue] withHandler:altitudeHandler];
                }, NO);
            }
            
        } else if ([type isEqualToString:@"motion"])
		{
			needsStart = TRUE;
			motionRegistered = TRUE;
		}
		else if ([type isEqualToString:@"accelerometer"])
		{
			needsStart = TRUE;
			accelerometerRegistered = TRUE;
		}
		else if ([type isEqualToString:@"gyroscope"])
		{
			needsStart = TRUE;
			gyroscopeRegistered = TRUE;
		}
        else if ([type isEqualToString:@"rotation"])
        {
            needsStart = TRUE;
            rotationRegistered = TRUE;
        }
        else if ([type isEqualToString:@"orientation"])
		{
			needsStart = TRUE;
			orientationRegistered = TRUE;
		}
		else if ([type isEqualToString:@"magnetometer"])
		{
			needsStart = TRUE;
			magnetometerRegistered = TRUE;
		}
		if (needsStart && [self motionManager].deviceMotionAvailable
			&& ![self motionManager].deviceMotionActive)
		{
            TiThreadPerformBlockOnMainThread(^{
                //                if (([CMMotionManager availableAttitudeReferenceFrames] & CMAttitudeReferenceFrameXTrueNorthZVertical) != 0)
                //                {
                //                    [motionManager startDeviceMotionUpdatesUsingReferenceFrame:CMAttitudeReferenceFrameXTrueNorthZVertical toQueue:[NSOperationQueue currentQueue]
                //                        withHandler:motionHandler];
                //                }
                //                else
                //                {
                [motionManager startDeviceMotionUpdatesToQueue:_motionQueue
                                                   withHandler:motionHandler];
                //                }
                
            }, NO);
		}
	}
}

-(void)_listenerRemoved:(NSString *)type count:(NSInteger)count
{
	if (count == 0)
	{
        if (altitudeRegistered && [type isEqualToString:@"altitude"])
        {
            altitudeRegistered = FALSE;
            if (barometerRegistered) {
                [self shutdownAltitudeManager];
            }
        }
        else if (barometerRegistered && [type isEqualToString:@"pressure"])
        {
            barometerRegistered = FALSE;
            if (altitudeRegistered) {
                [self shutdownAltitudeManager];
            }
        }
        else if ([type isEqualToString:@"motion"])
        {
            motionRegistered = FALSE;
        }
        else if ([type isEqualToString:@"accelerometer"])
		{
			accelerometerRegistered = FALSE;
		}
        else if ([type isEqualToString:@"gyroscope"])
        {
            gyroscopeRegistered = FALSE;
        }
        else if ([type isEqualToString:@"rotation"])
        {
            rotationRegistered = FALSE;
        }
        else if ([type isEqualToString:@"orientation"])
		{
			orientationRegistered = FALSE;
		}
		else if ([type isEqualToString:@"magnetometer"])
		{
			magnetometerRegistered = FALSE;
		}
		if (!motionRegistered && !accelerometerRegistered && !orientationRegistered && !gyroscopeRegistered && !magnetometerRegistered)
			[motionManager stopDeviceMotionUpdates];
	}
}

-(CMAcceleration)userAccelerationInReferenceFrame:(CMAcceleration)acc withRot:(CMRotationMatrix)rot
{
//    CMAcceleration acc = [self userAcceleration];
//    CMRotationMatrix rot = [self attitude].rotationMatrix;
    
    CMAcceleration accRef;
    accRef.x = acc.x*rot.m11 + acc.y*rot.m12 + acc.z*rot.m13;
    accRef.y = acc.x*rot.m21 + acc.y*rot.m22 + acc.z*rot.m23;
    accRef.z = acc.x*rot.m31 + acc.y*rot.m32 + acc.z*rot.m33;
    
    return accRef;
}

-(void) processAltitudeData: (CMAltitudeData *) data withError:(NSError *) error
{
    if ([self _hasListeners:@"altitude"]) {
        [self fireEvent:@"altitude" withObject:@{
                                                 @"relativeAltitude":data.relativeAltitude,
                                                 @"pressure":data.pressure
                                                 }];
    }
    if ([self _hasListeners:@"pressure"]) {
        [self fireEvent:@"pressure" withObject:@{
                                                 @"relativeAltitude":data.relativeAltitude,
                                                 @"pressure":data.pressure
                                                 }];
    }
}

-(void) processMotionData: (CMDeviceMotion *) motion withError:(NSError *) error
{
    CMAttitude* currentAttitude = motion.attitude;
    
    NSTimeInterval realTimestamp = (bootTimestamp + motion.timestamp) * 1000; // MILLI SECONDS
//	NSLog(@"[INFO] %f realTimestamp",realTimestamp);
    //    if (!usingReference)
    //    {
    ////        [lastAttitude release];
    ////        lastAttitude = [currentAttitude retain];
    //    }
    //    else if (referenceAttitude)
    //    {
    //        [currentAttitude multiplyByInverseOfAttitude: referenceAttitude];
    //        NSLog(@"obtaining: %f, %f, %f",currentAttitude.yaw,currentAttitude.pitch,currentAttitude.roll);
    //    }
    if (motionRegistered)
	{
//        CMAcceleration accRef = [self userAccelerationInReferenceFrame:motion.userAcceleration withRot:currentAttitude.rotationMatrix];
        //        NSLog(@"yaw: %f, %f",currentAttitude.yaw,currentAttitude.yaw*currentAttitude.rotationMatrix.m11);
        //        NSLog(@"obtaining: %f, %f, %f",currentAttitude.yaw,currentAttitude.pitch,currentAttitude.roll);
        CMQuaternion quat = currentAttitude.quaternion;
        NSMutableDictionary *event = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       [NSDictionary dictionaryWithObjectsAndKeys:
                                        @(motion.gravity.x), @"x",
                                        @(motion.gravity.y), @"y",
                                        @(motion.gravity.z), @"z", nil], @"gravity",
                                       [NSDictionary dictionaryWithObjectsAndKeys:
                                        @(motion.userAcceleration.x), @"x",
                                        @(motion.userAcceleration.y), @"y",
                                        @(motion.userAcceleration.z), @"z", nil], @"user",
                                       @(motion.gravity.x + motion.userAcceleration.x), @"x",
                                       @(motion.gravity.y + motion.userAcceleration.y), @"y",
                                       @(motion.gravity.z + motion.userAcceleration.z), @"z", nil], @"accelerometer",
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       @(radiansToDegrees(currentAttitude.yaw)), @"yaw",
                                       @(radiansToDegrees(currentAttitude.pitch)), @"pitch",
                                       @(radiansToDegrees(currentAttitude.roll)), @"roll", nil], @"orientation",
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       @(motion.magneticField.accuracy), @"accuracy",
                                       @(motion.magneticField.field.x), @"x",
                                       @(motion.magneticField.field.y), @"y",
                                       @(motion.magneticField.field.z), @"z", nil], @"magnetometer",
                                       @[@(motion.rotationRate.x),
                                       @(motion.rotationRate.y),
                                       @(motion.rotationRate.z)], @"gyroscope",
                                      @[@(quat.x), @(quat.y), @(quat.z), @(quat.w) ], @"quaternion",
                                      @(realTimestamp), @"timestamp",
                                      nil];
        if (computeRotationMatrix)
        {
            CMRotationMatrix rotation = currentAttitude.rotationMatrix;
             [event setObject:@[@(rotation.m11),@(rotation.m12),@(rotation.m13),
                                @(rotation.m21),
                                @(rotation.m22),
                                @(rotation.m23),
                                @(rotation.m31),
                                @(rotation.m32),
                                @(rotation.m33)] forKey:@"rotationMatrix"];
        }
 		[self fireEvent:@"motion" withObject:event propagate:NO checkForListener:NO];
	}
    else{
        if (accelerometerRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   [NSDictionary dictionaryWithObjectsAndKeys:
                                    @(motion.gravity.x), @"x",
                                    @(motion.gravity.y), @"y",
                                    @(motion.gravity.z), @"z", nil], @"gravity",
                                   [NSDictionary dictionaryWithObjectsAndKeys:
                                    @(motion.userAcceleration.x), @"x",
                                    @(motion.userAcceleration.y), @"y",
                                    @(motion.userAcceleration.z), @"z", nil], @"user",
                                   @(motion.gravity.x + motion.userAcceleration.x), @"x",
                                   @(motion.gravity.y + motion.userAcceleration.y), @"y",
                                   @(motion.gravity.z + motion.userAcceleration.z), @"z",
                                   @(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"accelerometer" withObject:event propagate:NO checkForListener:NO];
        }
        if (orientationRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   @(radiansToDegrees(currentAttitude.yaw)), @"yaw",
                                   @(radiansToDegrees(currentAttitude.pitch)), @"pitch",
                                   @(radiansToDegrees(currentAttitude.roll)), @"roll",
                                   @(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"orientation" withObject:event propagate:NO checkForListener:NO];
        }
        if (gyroscopeRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   @(motion.rotationRate.x), @"x",
                                   @(motion.rotationRate.y), @"y",
                                   @(motion.rotationRate.z), @"z",
                                   @(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"gyroscope" withObject:event propagate:NO checkForListener:NO];
        }
        if (magnetometerRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   @(motion.magneticField.field.x), @"x",
                                   @(motion.magneticField.field.y), @"y",
                                   @(motion.magneticField.field.z), @"z",
                                   @(motion.magneticField.accuracy), @"accuracy",
                                   @(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"magnetometer" withObject:event propagate:NO checkForListener:NO];
        }
        
        if (rotationRegistered)
        {
            CMQuaternion quat = currentAttitude.quaternion;
            CMRotationMatrix rotation = currentAttitude.rotationMatrix;
            CMAttitude* currentAttitude = motion.attitude;

            [self fireEvent:@"rotation" withObject:@{@"quaternion":@[@(quat.x), @(quat.y), @(quat.z), @(quat.w) ],
                                                     @"timestamp": @(realTimestamp),
                                                     @"rotationMatrix":@[
                                                         @(rotation.m11),
                                                         @(rotation.m12),
                                                         @(rotation.m13),
                                                        @(rotation.m21),
                                                        @(rotation.m22),
                                                                         @(rotation.m23),
                                                                         @(rotation.m31),
                                                                         @(rotation.m32),
                                                                         @(rotation.m33)],
                                                     @"rotation":@[@(currentAttitude.yaw),
                                                                   @(currentAttitude.pitch),
                                                                   @(currentAttitude.roll)]
                                                     } propagate:NO checkForListener:NO];
        }
    }
}


#pragma Public APIs


//- (void)markZeroReference
//{
//    [referenceAttitude release];
//    CMDeviceMotion* deviceMotion = motionManager.deviceMotion;
//    referenceAttitude = [deviceMotion.attitude retain];
//}


MAKE_SYSTEM_PROP_DBL(ACCURACY_HIGH,CMMagneticFieldCalibrationAccuracyHigh);
MAKE_SYSTEM_PROP_DBL(ACCURACY_MEDIUM,CMMagneticFieldCalibrationAccuracyMedium);
MAKE_SYSTEM_PROP_DBL(ACCURACY_LOW,CMMagneticFieldCalibrationAccuracyLow);
MAKE_SYSTEM_PROP_DBL(ACCURACY_UNCALIBRATED,CMMagneticFieldCalibrationAccuracyUncalibrated);
MAKE_SYSTEM_PROP(STANDARD_GRAVITY,9.80665);

-(NSNumber*)updateInterval
{
	return NUMINT(updateInterval*1000);
}

-(void)setUpdateInterval:(NSNumber *)interval //in ms
{
    updateInterval = [interval intValue] / 1000.0; // in seconds
    if (motionManager) {
        motionManager.deviceMotionUpdateInterval = updateInterval;
        motionManager.accelerometerUpdateInterval = updateInterval;
        motionManager.gyroUpdateInterval = updateInterval;
        motionManager.magnetometerUpdateInterval = updateInterval;
    }
    
}

//-(NSNumber*)useReference
//{
//	return NUMBOOL(usingReference);
//}
//
//
////the previous event will be used as a reference
//-(void)setUseReference:(NSNumber *)value
//{
//    NSLog(@"setUseReference: %d", [value boolValue]);
//    [self markZeroReference];
//    usingReference = [value boolValue];
//}

-(NSNumber*)computeRotationMatrix
{
	return NUMBOOL(computeRotationMatrix);
}


-(void)setComputeRotationMatrix:(NSNumber *)value
{
    computeRotationMatrix = [value boolValue];
}

-(NSNumber*)hasAccelerometer
{
	return NUMBOOL([self motionManager].accelerometerAvailable);
}

-(NSNumber*)hasGyroscope
{
	return NUMBOOL([self motionManager].gyroAvailable);
}

-(NSNumber*)hasMagnetometer
{
	return NUMBOOL([self motionManager].magnetometerAvailable);
}

-(NSNumber*)hasOrientation
{
    return NUMBOOL([self motionManager].deviceMotionAvailable);
}

-(NSNumber*)hasRotation
{
    return NUMBOOL([self motionManager].deviceMotionAvailable);
}


-(NSNumber*)hasBarometer
{
    return NUMBOOL([CMAltimeter isRelativeAltitudeAvailable]);
}


@end
