/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013年 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasOpencvView.hpp"
#import "TiApp.h"
#import "TiUtils.h"

@implementation AkylasOpencvView

#pragma mark -
#pragma mark Ad Lifecycle

-(id)init {
    [super init];

    m_cameraView = nil;
    m_threshold = 10;
    return self;
}

-(void)initCamera:(CGRect)bounds
{
    NSNumber* threshold = [self.proxy valueForKey:@"threshold"];
    m_threshold = [threshold intValue];
    NSString* bitmapName = [self.proxy valueForKey:@"bitmapName"];
    
    if (m_cameraView != nil) {
        [m_cameraView stopDetect];
        [m_cameraView removeFromSuperview];
        RELEASE_TO_NIL(m_cameraView);
    }
    m_cameraView = [[CameraView alloc] initWithFrame:bounds];
    [self addSubview:m_cameraView];
    [self initBmp:bitmapName];
    [m_cameraView startDetect:self selector:@selector(detectCallback:)];
}

- (void)initBmp:(NSString*)bitmapName
{
    NSString *imagePath = [[NSBundle mainBundle] pathForResource:bitmapName ofType:@"png"];
    UIImage *img = [UIImage imageWithContentsOfFile:imagePath];
    
    cv::Mat mat;
    UIImageToMat(img, mat);
    [m_cameraView setTrainingImages:mat];
}

-(void)detectCallback:(NSNumber*)similarity
{
    if ( [similarity intValue] > m_threshold ){
        [m_cameraView stopDetect];

        NSDictionary *e = [NSDictionary dictionaryWithObject:similarity forKey:@"similarity"];
        [self.proxy fireEvent:@"imageDetected" withObject:e];
    }
}


-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [self initCamera:bounds];
}

-(void)dealloc
{
    if ( m_cameraView != nil ) {
        [m_cameraView stopDetect];
        [m_cameraView removeFromSuperview];
        RELEASE_TO_NIL(m_cameraView);
    }
    [super dealloc];
}

- (void)viewWillDisappear:(BOOL)animated {
}

- (void)viewWillAppear:(BOOL)animated {
}

- (void)pause:(id)args
{
}

- (void)resume:(id)args
{
}

@end
