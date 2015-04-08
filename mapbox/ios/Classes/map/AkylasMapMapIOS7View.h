/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapMapView.h"
#import "AkylasMapCameraProxy.h"

@interface AkylasMapMapIOS7View : AkylasMapMapView {
@private
    KrollCallback *cameraAnimationCallback;
}

-(AkylasMapCameraProxy*)camera;
-(void)animateCamera:(id)args;
//-(void)showAnnotations:(id)args;

@end
