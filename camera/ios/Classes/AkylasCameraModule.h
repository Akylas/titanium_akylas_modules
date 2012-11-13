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
@property(nonatomic,readonly) NSString *QUALITY_HIGH;
@property(nonatomic,readonly) NSString *QUALITY_MEDIUM;
@property(nonatomic,readonly) NSString *QUALITY_LOW;
@property(nonatomic,readonly) NSNumber *CAMERA_FRONT;
@property(nonatomic,readonly) NSNumber *CAMERA_BACK;

@end
