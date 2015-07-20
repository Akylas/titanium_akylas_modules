/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"

@interface AkylasCameraModule : TiModule 
{
}
@property(nonatomic,readonly) NSString *QUALITY_BEST;
@property(nonatomic,readonly) NSString *QUALITY_HIGH;
@property(nonatomic,readonly) NSString *QUALITY_MEDIUM;
@property(nonatomic,readonly) NSString *QUALITY_LOW;
@property(nonatomic,readonly) NSNumber *CAMERA_FRONT;
@property(nonatomic,readonly) NSNumber *CAMERA_BACK;

@property(nonatomic,readonly) NSNumber *FLASH_ON;
@property(nonatomic,readonly) NSNumber *FLASH_OFF;
@property(nonatomic,readonly) NSNumber *FLASH_AUTO;

@property(nonatomic,readonly) NSNumber *WHITE_BALANCE_LOCKED;
@property(nonatomic,readonly) NSNumber *WHITE_BALANCE_AUTO;
@property(nonatomic,readonly) NSNumber *WHITE_BALANCE_CONTINUOUS;

@property(nonatomic,readonly) NSNumber *EXPOSURE_LOCKED;
@property(nonatomic,readonly) NSNumber *EXPOSURE_AUTO;
@property(nonatomic,readonly) NSNumber *EXPOSURE_CONTINUOUS;
@end
