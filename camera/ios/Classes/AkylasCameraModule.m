/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasCameraModule.h"
#import "TiBase.h"
#import "AkylasCameraViewProxy.h"
#import <AVFoundation/AVFoundation.h>

@implementation AkylasCameraModule

#pragma mark Internal
MAKE_SYSTEM_STR(QUALITY_BEST,AVCaptureSessionPresetPhoto);
MAKE_SYSTEM_STR(QUALITY_HIGH,AVCaptureSessionPresetHigh);
MAKE_SYSTEM_STR(QUALITY_MEDIUM,AVCaptureSessionPresetMedium);
MAKE_SYSTEM_STR(QUALITY_LOW,AVCaptureSessionPresetLow);

MAKE_SYSTEM_PROP(CAMERA_FRONT,AVCaptureDevicePositionFront);
MAKE_SYSTEM_PROP(CAMERA_BACK,AVCaptureDevicePositionBack);

MAKE_SYSTEM_PROP(FLASH_ON,AVCaptureFlashModeOn);
MAKE_SYSTEM_PROP(FLASH_OFF,AVCaptureFlashModeOff);
MAKE_SYSTEM_PROP(FLASH_AUTO,AVCaptureFlashModeAuto);

MAKE_SYSTEM_PROP(WHITE_BALANCE_LOCKED,AVCaptureWhiteBalanceModeLocked);
MAKE_SYSTEM_PROP(WHITE_BALANCE_AUTO,AVCaptureWhiteBalanceModeAutoWhiteBalance);
MAKE_SYSTEM_PROP(WHITE_BALANCE_CONTINUOUS,AVCaptureWhiteBalanceModeContinuousAutoWhiteBalance);

MAKE_SYSTEM_PROP(EXPOSURE_LOCKED,AVCaptureExposureModeLocked);
MAKE_SYSTEM_PROP(EXPOSURE_AUTO,AVCaptureExposureModeAutoExpose);
MAKE_SYSTEM_PROP(EXPOSURE_CONTINUOUS,AVCaptureExposureModeContinuousAutoExposure);
// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"eabe798b-9367-4102-af0e-2e5b9d3b2ffa";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.camera";
}

#pragma mark Lifecycle

-(void)startup
{
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Camera.View", [AkylasCameraViewProxy class]);
    [super startup];
}


@end
