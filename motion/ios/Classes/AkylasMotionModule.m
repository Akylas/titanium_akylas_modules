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

@implementation AkylasMotionModule

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
    updateInterval = 0.030; // time between 2 data in seconds
    computeRotationMatrix = TRUE;
    
    //        usingReference = false;
    
    motionManager = [[CMMotionManager alloc] init];
    
    motionManager.deviceMotionUpdateInterval = updateInterval;
    motionManager.gyroUpdateInterval = updateInterval;
    motionManager.accelerometerUpdateInterval = updateInterval;
    motionManager.magnetometerUpdateInterval = updateInterval;
    motionHandler = Block_copy(^(CMDeviceMotion *motion, NSError *error){
        [self processMotionData:motion withError:error];});
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
	
	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup 

-(void)dealloc
{
    RELEASE_TO_NIL(motionManager);
	RELEASE_TO_NIL(motionHandler);
	RELEASE_TO_NIL(motionQueue);
    
	// release any resources that have been retained by the module
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

-(void)_listenerAdded:(NSString *)type count:(int)count
{
	if (count == 1)
	{
		BOOL needsStart = FALSE;
		if ([type isEqualToString:@"motion"])
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
		if (needsStart && motionManager.deviceMotionAvailable
			&& !motionManager.deviceMotionActive)
		{
            TiThreadPerformOnMainThread(^{
                //                if (([CMMotionManager availableAttitudeReferenceFrames] & CMAttitudeReferenceFrameXTrueNorthZVertical) != 0)
                //                {
                //                    [motionManager startDeviceMotionUpdatesUsingReferenceFrame:CMAttitudeReferenceFrameXTrueNorthZVertical toQueue:[NSOperationQueue currentQueue]
                //                        withHandler:motionHandler];
                //                }
                //                else
                //                {
                [motionManager startDeviceMotionUpdatesToQueue:[NSOperationQueue currentQueue]
                                                   withHandler:motionHandler];
                //                }
                
            }, NO);
		}
	}
}

-(void)_listenerRemoved:(NSString *)type count:(int)count
{
	if (count == 0)
	{
		if ([type isEqualToString:@"motion"])
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
        NSMutableDictionary *event = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       [NSDictionary dictionaryWithObjectsAndKeys:
                                        NUMFLOAT(motion.gravity.x), @"x",
                                        NUMFLOAT(motion.gravity.y), @"y",
                                        NUMFLOAT(motion.gravity.z), @"z", nil], @"gravity",
                                       [NSDictionary dictionaryWithObjectsAndKeys:
                                        NUMFLOAT(motion.userAcceleration.x), @"x",
                                        NUMFLOAT(motion.userAcceleration.y), @"y",
                                        NUMFLOAT(motion.userAcceleration.z), @"z", nil], @"user",
//                                       [NSDictionary dictionaryWithObjectsAndKeys:
//                                        NUMFLOAT(accRef.x), @"x",
//                                        NUMFLOAT(accRef.y), @"y",
//                                        NUMFLOAT(accRef.z), @"z", nil], @"userref",
                                       NUMFLOAT(motion.gravity.x + motion.userAcceleration.x), @"x",
                                       NUMFLOAT(motion.gravity.y + motion.userAcceleration.y), @"y",
                                       NUMFLOAT(motion.gravity.z + motion.userAcceleration.z), @"z", nil], @"accelerometer",
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       NUMFLOAT(radiansToDegrees(currentAttitude.yaw)), @"yaw",
                                       NUMFLOAT(radiansToDegrees(currentAttitude.pitch)), @"pitch",
                                       NUMFLOAT(radiansToDegrees(currentAttitude.roll)), @"roll", nil], @"orientation",
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       NUMDOUBLE(motion.magneticField.accuracy), @"accuracy",
                                       NUMDOUBLE(motion.magneticField.field.x), @"x",
                                       NUMDOUBLE(motion.magneticField.field.y), @"y",
                                       NUMDOUBLE(motion.magneticField.field.z), @"z", nil], @"magnetometer",
                                      [NSDictionary dictionaryWithObjectsAndKeys:
                                       NUMDOUBLE(motion.rotationRate.x), @"x",
                                       NUMDOUBLE(motion.rotationRate.y), @"y",
                                       NUMDOUBLE(motion.rotationRate.z), @"z", nil], @"gyroscope",
                                      NUMLONGLONG(realTimestamp), @"timestamp",
                                      nil];
        if (computeRotationMatrix)
        {
            CMRotationMatrix rotation = currentAttitude.rotationMatrix;
            Ti3DMatrix *timatrix = [[Ti3DMatrix alloc] init];
            [timatrix setM11:[NSNumber numberWithFloat:rotation.m11]];
            [timatrix setM12:[NSNumber numberWithFloat:rotation.m21]];
            [timatrix setM13:[NSNumber numberWithFloat:rotation.m31]];
            
            [timatrix setM21:[NSNumber numberWithFloat:rotation.m12]];
            [timatrix setM22:[NSNumber numberWithFloat:rotation.m22]];
            [timatrix setM23:[NSNumber numberWithFloat:rotation.m32]];
            
            [timatrix setM31:[NSNumber numberWithFloat:rotation.m13]];
            [timatrix setM32:[NSNumber numberWithFloat:rotation.m23]];
            [timatrix setM33:[NSNumber numberWithFloat:rotation.m33]];
            [event setObject:[timatrix autorelease] forKey:@"invertedRotationMatrix"];
            timatrix = [[Ti3DMatrix alloc] init];
            [timatrix setM11:[NSNumber numberWithFloat:rotation.m11]];
            [timatrix setM12:[NSNumber numberWithFloat:rotation.m12]];
            [timatrix setM13:[NSNumber numberWithFloat:rotation.m13]];
            
            [timatrix setM21:[NSNumber numberWithFloat:rotation.m21]];
            [timatrix setM22:[NSNumber numberWithFloat:rotation.m22]];
            [timatrix setM23:[NSNumber numberWithFloat:rotation.m23]];
            
            [timatrix setM31:[NSNumber numberWithFloat:rotation.m31]];
            [timatrix setM32:[NSNumber numberWithFloat:rotation.m32]];
            [timatrix setM33:[NSNumber numberWithFloat:rotation.m33]];
            [event setObject:[timatrix autorelease] forKey:@"rotationMatrix"];
        }
 		[self fireEvent:@"motion" withObject:event];
	}
    else{
        if (accelerometerRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   [NSDictionary dictionaryWithObjectsAndKeys:
                                    NUMFLOAT(motion.gravity.x), @"x",
                                    NUMFLOAT(motion.gravity.y), @"y",
                                    NUMFLOAT(motion.gravity.z), @"z", nil], @"gravity",
                                   [NSDictionary dictionaryWithObjectsAndKeys:
                                    NUMFLOAT(motion.userAcceleration.x), @"x",
                                    NUMFLOAT(motion.userAcceleration.y), @"y",
                                    NUMFLOAT(motion.userAcceleration.z), @"z", nil], @"user",
                                   NUMFLOAT(motion.gravity.x + motion.userAcceleration.x), @"x",
                                   NUMFLOAT(motion.gravity.y + motion.userAcceleration.y), @"y",
                                   NUMFLOAT(motion.gravity.z + motion.userAcceleration.z), @"z",
                                   NUMLONGLONG(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"accelerometer" withObject:event];
        }
        if (orientationRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   NUMFLOAT(radiansToDegrees(currentAttitude.yaw)), @"yaw",
                                   NUMFLOAT(radiansToDegrees(currentAttitude.pitch)), @"pitch",
                                   NUMFLOAT(radiansToDegrees(currentAttitude.roll)), @"roll",
                                   NUMLONGLONG(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"orientation" withObject:event];
        }
        if (gyroscopeRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   NUMFLOAT(motion.rotationRate.x), @"x",
                                   NUMFLOAT(motion.rotationRate.y), @"y",
                                   NUMFLOAT(motion.rotationRate.z), @"z",
                                   NUMLONGLONG(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"gyroscope" withObject:event];
        }
        if (magnetometerRegistered)
        {
            NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                                   NUMFLOAT(motion.magneticField.field.x), @"x",
                                   NUMFLOAT(motion.magneticField.field.y), @"y",
                                   NUMFLOAT(motion.magneticField.field.z), @"z",
                                   NUMFLOAT(motion.magneticField.accuracy), @"accuracy",
                                   NUMLONGLONG(realTimestamp), @"timestamp",
                                   nil];
            [self fireEvent:@"magnetometer" withObject:event];
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
    motionManager.deviceMotionUpdateInterval = updateInterval;
    motionManager.accelerometerUpdateInterval = updateInterval;
    motionManager.gyroUpdateInterval = updateInterval;
    motionManager.magnetometerUpdateInterval = updateInterval;
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
	return NUMBOOL(motionManager.accelerometerAvailable);
}

-(NSNumber*)hasGyroscope
{
	return NUMBOOL(motionManager.gyroAvailable);
}

-(NSNumber*)hasMagnetometer
{
	return NUMBOOL(motionManager.magnetometerAvailable);
}

-(NSNumber*)hasOrientation
{
	return NUMBOOL(motionManager.deviceMotionAvailable);
}

@end
