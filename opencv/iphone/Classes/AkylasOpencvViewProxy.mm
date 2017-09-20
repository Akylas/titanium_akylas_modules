/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013年 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasOpencvViewProxy.hpp"
#import "AkylasOpencvView.hpp"
#import "TiUtils.h"

@implementation AkylasOpencvViewProxy


#ifndef USE_VIEW_FOR_UI_METHOD
    #define USE_VIEW_FOR_UI_METHOD(methodname)\
    -(void)methodname:(id)args\
    {\
    [self makeViewPerformSelector:@selector(methodname:) withObject:args createIfNeeded:YES waitUntilDone:NO];\
    }
#endif

USE_VIEW_FOR_UI_METHOD(pause);
USE_VIEW_FOR_UI_METHOD(resume);


@end
