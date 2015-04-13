/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "TiUIView.h"
#import "ReusableViewProxy.h"
#import "ReusableViewProtocol.h"

#define REGION_VALID(region) (region.northEast.latitude > region.southWest.latitude && region.northEast.longitude > region.southWest.longitude && !isnan(region.northEast.latitude) && !isnan(region.northEast.longitude) && !isnan(region.southWest.latitude) && !isnan(region.southWest.longitude))
#define PostVersion7 (floor(NSFoundationVersionNumber) >  NSFoundationVersionNumber_iOS_6_1)

@class AkylasMapAnnotationProxy;
@class AkylasMapTileSourceProxy;

@interface CalloutReusableView : TiUIView <ReusableViewProtocol>
@property(nonatomic, readonly, copy) NSString *reuseIdentifier;
@property (nonatomic, readwrite, retain) NSDictionary *dataItem;
- (id)initWithFrame:(CGRect)frame reuseIdentifier:(NSString *)reuseIdentifier;
@end

@interface CalloutViewProxy : ReusableViewProxy
@property (nonatomic, retain) AkylasMapAnnotationProxy *annotation;

@end


@protocol AkylasMapboxAnnotation
@required
-(NSString *)lastHitName;
@end


@interface AkylasMapView : TiUIView {
    BOOL _calloutUseTemplates;
	BOOL regionFits;
	BOOL animate;
	BOOL loaded;
	BOOL ignoreClicks;
	BOOL ignoreRegionChanged;
	BOOL forceRender;
    CGFloat _internalZoom;
}

//@property (nonatomic, readonly) NSArray *customAnnotations;
@property (nonatomic, retain) UIImage *defaultPinImage;
@property (nonatomic, assign) CGPoint defaultPinAnchor;
@property (nonatomic, assign) CGPoint defaultCalloutAnchor;

#pragma mark Public APIs
-(void)updateCamera:(id)args;
-(void)zoomTo:(id)args;

-(void)addAnnotation:(id)args atIndex:(NSInteger)index;
-(void)removeAnnotation:(id)args;
-(void)removeAllAnnotations;
-(void)selectAnnotation:(id)args;
-(void)deselectAnnotation:(id)args;

-(void)addRoute:(id)args atIndex:(NSInteger)index;
-(void)removeRoute:(id)args;
-(void)removeAllRoutes;

-(void)addTileSource:(id)args atIndex:(NSInteger)index;
-(void)removeTileSource:(id)args;
-(void)removeAllTileSources;

#pragma mark Framework
-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn;
- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated;
- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated;
-(void)selectUserAnnotation;
-(BOOL)viewInitialized;
-(void) reuseIfNecessary:(id)object;
-(CalloutReusableView*) reusableViewForProxy:(AkylasMapAnnotationProxy*)proxy objectKey:(NSString*)key;

@end


