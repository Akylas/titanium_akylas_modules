/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiViewProxy.h"

#define DEFAULT_CALLOUT_PADDING UIEdgeInsetsMake(13,13,13,13)
#define DEFAULT_CALLOUT_CORNER_RADIUS 8
#define DEFAULT_CALLOUT_ARROW_HEIGHT 13

@class AkylasMapViewProxy;
@class AkylasMapView;
@class AkylasMapMapboxView;
@interface AkylasMapAnnotationProxy : TiParentingProxy<MKAnnotation, TiProxyObserver> {
@protected
    
    UIImage* _internalImage;
    NSString* _mbImage;
    UIColor* _tintColor;
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
@property (nonatomic, readwrite, assign) BOOL placed;
@property (nonatomic, readwrite, assign) BOOL draggable;
@property (nonatomic, readwrite, assign) CGFloat minZoom;
@property (nonatomic, readwrite, assign) CGFloat maxZoom;

// Title and subtitle for use by selection UI.
- (NSString *)title;
- (NSString *)subtitle;

-(void)setNeedsRefreshingWithSelection: (BOOL)shouldReselect;

//Mapbox
@property (nonatomic, readonly) RMAnnotation *rmannotation;
-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView;
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapMapboxView*)mapView;
-(RMMarker*)marker;
-(RMAnnotation*)getRMAnnotation;

//native
- (int)tag;
- (int)mapPincolor;
- (BOOL)animatesDrop;
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

@end
