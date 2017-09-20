/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013年 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiUIView.h"
#import "CameraView.hpp"

@interface AkylasOpencvView : TiUIView {

@private
    CameraView* m_cameraView;
    int m_threshold;
}

-(void)initCamera:(CGRect)bounds;
-(void)detectCallback:(NSNumber*)similarity;
-(void)pause:(id)args;
-(void)resume:(id)args;

@end
