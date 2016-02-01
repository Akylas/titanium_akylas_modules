/**
 * Akylas
 * Copyright (c) 2014-2015 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "AkylasZoomableimageModule.h"
#import "AkylasZoomableimageViewProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation AkylasZoomableimageModule

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"0d005e93-9980-4739-9e41-fd1129c8ff3a";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.zoomableimage";
}

#pragma mark Lifecycle

-(void)startup
{
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.ZoomableImageView", [AkylasZoomableimageViewProxy class]);
	[super startup];
}

@end
