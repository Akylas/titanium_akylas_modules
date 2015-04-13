/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiViewProxy.h"
#import "ImageLoader.h"

#define DEFAULT_CALLOUT_PADDING UIEdgeInsetsMake(13,13,13,13)
#define DEFAULT_CALLOUT_CORNER_RADIUS 8
#define DEFAULT_CALLOUT_ARROW_HEIGHT 13

@class AkylasMapViewProxy;
@class AkylasMapView;
@class AkylasMapMapboxView;
@class GMSOverlay;
@class GMSMapView;
@interface AkylasMapAnnotationProxy : TiParentingProxy<MKAnnotation, TiProxyObserver, ImageLoaderDelegate> {
@protected
    
    UIImage* _internalImage;
    NSString* _mbImage;
	int tag;
	AkylasMapViewProxy *delegate;
	BOOL needsRefreshing;
	BOOL needsRefreshingWithSelection;
	BOOL placed;
	CGPoint offset;
    
    
    //Mapbox
    RMAnnotation* _rmannotation;
    RMMarker* _marker;
    RMMarkerMapboxImageSize _size;

}

// Center latitude and longitude of the annotion view.
@property (nonatomic, assign) CLLocationCoordinate2D coordinate;
@property (nonatomic, readwrite, assign) AkylasMapViewProxy *delegate;
@property (nonatomic,readonly)	BOOL needsRefreshingWithSelection;

@property (nonatomic, readwrite, assign) BOOL animate;
@property (nonatomic, readwrite, assign) BOOL placed;
@property (nonatomic, readwrite, assign) BOOL draggable;
@property (nonatomic, readwrite, assign) BOOL flat;
@property (nonatomic, readwrite, assign) BOOL showInfoWindow;
@property (nonatomic, readwrite, copy) NSString *title;
@property (nonatomic, readwrite, copy) NSString *subtitle;
@property (nonatomic, readwrite, assign) MKPinAnnotationColor pinColor;
@property (nonatomic, readwrite, retain) UIColor *tintColor;

@property (nonatomic, readwrite, assign) CGFloat minZoom;
@property (nonatomic, readwrite, assign) CGFloat maxZoom;

@property (nonatomic, readwrite, assign) CGFloat heading;
@property (nonatomic, readwrite, assign) CGFloat opacity;
@property (nonatomic, assign) NSInteger zIndex;

-(void)setNeedsRefreshingWithSelection: (BOOL)shouldReselect;

//Mapbox
@property (nonatomic, readonly) RMAnnotation *rmannotation;
-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView;
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapMapboxView*)mapView;
-(RMMarker*)marker;
-(RMAnnotation*)getRMAnnotation;
-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;

//native
@property (nonatomic, readwrite, retain) MKAnnotationView *annView;
- (int)tag;
- (UIView*)nGetLeftViewAccessory;
- (UIView*)nGetRightViewAccessory;
- (UIView*)nGetCustomViewAccessory;
-(UIColor*)nGetCalloutBackgroundColor;
-(CGFloat)nGetCalloutBorderRadius;
-(CGFloat)nGetCalloutArrowHeight;
-(UIEdgeInsets)nGetCalloutPadding;
-(CGPoint)nGetAnchorPoint;
-(CGPoint)nGetCalloutAnchorPoint;
-(CGFloat)nGetCalloutAlpha;
-(UIImage*)nGetInternalImage;
-(BOOL)nHasInternalImage;
-(CGSize)getSize;


+(int)gZIndexDelta;

@end
