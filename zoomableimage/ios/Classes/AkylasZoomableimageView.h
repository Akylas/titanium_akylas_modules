/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiScrollingView.h"
#import "ImageLoader.h"

@interface AkylasZoomableimageView : TiScrollingView<TiScrolling>
-(id)getImage;
-(id)convertToUIImage:(id)arg;
-(void)setReusing:(BOOL)value;
-(void)imageLoadSuccess:(ImageLoaderRequest*)request image:(id)image;
-(id)convertToUIImage:(id)arg;
-(id)prepareImage:(id)image;
@end
