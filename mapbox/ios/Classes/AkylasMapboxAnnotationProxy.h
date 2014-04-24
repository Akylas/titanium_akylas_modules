/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiViewProxy.h"
#import <MapKit/MapKit.h>

@class AkylasMapboxViewProxy;
@class AkylasMapboxView;
@interface AkylasMapboxAnnotationProxy : TiViewProxy<MKAnnotation, TiProxyObserver> {
@protected
    RMAnnotation* _annotation;
    RMMarker* _marker;
    UIImage* _image;
    NSString* _mbImage;
    UIColor* _tintColor;
    RMMarkerMapboxImageSize _size;
    CGPoint _anchorPoint;
	int tag;
	AkylasMapboxViewProxy *delegate;
	BOOL needsRefreshing;
	BOOL needsRefreshingWithSelection;
	BOOL placed;
	CGPoint offset;
}

// Center latitude and longitude of the annotion view.
@property (nonatomic, assign) CLLocationCoordinate2D coordinate;
@property (nonatomic, readwrite, assign) AkylasMapboxViewProxy *delegate;
@property (nonatomic,readonly)	BOOL needsRefreshingWithSelection;
@property (nonatomic, readwrite, assign) BOOL placed;
@property (nonatomic, readwrite, assign) BOOL draggable;
@property (nonatomic, readonly) CGPoint offset;
@property (nonatomic, readonly) RMAnnotation *annotation;

- (int)tag;
-(RMAnnotation*)getAnnotationForMapView:(RMMapView*)mapView;
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView;
-(RMMarker*)marker;
-(RMAnnotation*)getAnnotation;
-(void)setNeedsRefreshingWithSelection: (BOOL)shouldReselect;

@end
