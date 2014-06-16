/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "TiUIView.h"

#define REGION_VALID(region) (region.northEast.latitude > region.southWest.latitude && region.northEast.longitude > region.southWest.longitude && !isnan(region.northEast.latitude) && !isnan(region.northEast.longitude) && !isnan(region.southWest.latitude) && !isnan(region.southWest.longitude))

@class AkylasMapAnnotationProxy;
@class AkylasMapTileSourceProxy;
@protocol AkylasMapboxAnnotation
@required
-(NSString *)lastHitName;
@end


@interface AkylasMapView : TiUIView {
	BOOL regionFits;
	BOOL animate;
	BOOL loaded;
	BOOL ignoreClicks;
	BOOL ignoreRegionChanged;
	BOOL forceRender;
	RMSphericalTrapezium region;
    CGFloat _zoom;
}

@property (nonatomic, readonly) NSArray *customAnnotations;
@property (nonatomic, retain) UIImage *defaultPinImage;
@property (nonatomic, assign) CGPoint defaultPinAnchor;

#pragma mark Private APIs
-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg;
-(NSArray*)annotationsFromArgs:(id)value;


#pragma mark Public APIs
-(void)addAnnotation:(id)args;
-(void)addAnnotations:(id)args;
-(void)setAnnotations_:(id)value;
-(void)removeAnnotation:(id)args;
-(void)removeAnnotations:(id)args;
-(void)removeAllAnnotations:(id)args;
-(void)selectAnnotation:(id)args;
-(void)deselectAnnotation:(id)args;
-(void)zoom:(id)args;
-(void)addRoute:(id)args;
-(void)removeRoute:(id)args;

#pragma mark Framework
-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn;
-(id)centerCoordinate;
- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated;
- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated;
-(id)getRegion;
-(void)selectUserAnnotation;
-(BOOL)viewInitialized;
@end

