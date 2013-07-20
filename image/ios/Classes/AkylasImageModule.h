/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"

@class GPUImageFilter;
@interface AkylasImageModule : TiModule {
    GPUImageFilter* currentFilter;
}
@property(nonatomic,readonly) NSNumber* FILTER_GAUSSIAN_BLUR;
@end
