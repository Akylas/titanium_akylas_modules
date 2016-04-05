/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiScrollingViewProxy.h"
#import "ImageLoader.h"

@interface AkylasZoomableimageViewProxy : TiScrollingViewProxy<ImageLoaderDelegate> {
    ImageLoaderRequest *urlRequest;
    NSURL* imageURL;
}
@property (nonatomic,retain) NSURL* imageURL;

-(void)cancelPendingImageLoads;
-(void)startImageLoad:(NSURL *)url;

@end