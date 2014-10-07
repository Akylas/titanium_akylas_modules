/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapView.h"
#import "TiUtils.h"
#import "AkylasMapModule.h"
#import "AkylasMapAnnotationProxy.h"
#import "AkylasMapRouteProxy.h"
#import "ImageLoader.h"

@implementation AkylasMapView
{
}
@synthesize defaultPinImage;
@synthesize defaultPinAnchor;
@synthesize defaultCalloutAnchor;

#pragma mark Internal

- (id)init
{
    if ((self = [super init])) {
        defaultPinAnchor = CGPointMake(0.5, 0.5);
        defaultCalloutAnchor = CGPointMake(0, -0.5f);
        _internalZoom = -1;
        animate = true;

    }
    return self;
}

-(void)dealloc
{
	[super dealloc];
}


-(BOOL)viewInitialized {
    return [(TiViewProxy*)(self.proxy) viewInitialized];
}

-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg
{
    return [(AkylasMapViewProxy*)[self proxy] annotationFromArg:arg];
}

-(NSArray*)annotationsFromArgs:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	NSMutableArray * result = [NSMutableArray arrayWithCapacity:[value count]];
	if (value!=nil)
	{
		for (id arg in value)
		{
			[result addObject:[self annotationFromArg:arg]];
		}
	}
	return result;
}

-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn
{
}


-(void)internalAddAnnotations:(id)annotations
{
}

-(void)internalRemoveAnnotations:(id)annotations
{
}

-(void)internalRemoveAllAnnotations
{
}

#pragma mark Public APIs

-(void)addAnnotation:(id)args
{
	[self internalAddAnnotations:[self annotationFromArg:args]];
}

-(void)addAnnotations:(id)args
{
    NSArray* toadd = [self annotationsFromArgs:args];
	[self internalAddAnnotations:toadd];
}

-(void)removeAnnotation:(id)args
{
	 [self internalRemoveAnnotations:args];
}

-(void)removeAnnotations:(id)args
{
    [self internalRemoveAnnotations:args];
}

-(void)removeAllAnnotations
{
	ENSURE_UI_THREAD_0_ARGS;
    [self internalRemoveAllAnnotations];
}

-(void)setAnnotations_:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	ENSURE_UI_THREAD(setAnnotations_,value)
	if (value != nil) {
		[self addAnnotations:value];
	}
}


-(void)setSelectedAnnotation:(AkylasMapAnnotationProxy*)annotation
{
}

-(void)selectAnnotation:(id)args
{
}

-(void)deselectAnnotation:(id)args
{
}

-(void)selectUserAnnotation
{
}

-(void)zoomTo:(id)args
{
}

-(RMSphericalTrapezium) getCurrentRegion
{
    return kMapboxDefaultLatLonBoundingBox;
}

#pragma mark Public APIs


-(id)metersPerPixel_
{
    return @(0);
}

-(id)region_
{
    return [AkylasMapModule dictFromRegion:[self getCurrentRegion]];
}

-(void)setScrollableAreaLimit_:(id)value
{
}

-(void)setRegion_:(id)value
{

}

-(void)setAnimateChanges_:(id)value
{
	animate = [TiUtils boolValue:value];
}

-(void)setRegionFit_:(id)value
{
    regionFits = [TiUtils boolValue:value];
}

-(void)setUserLocationEnabled_:(id)value
{
}

-(id)userLocationEnabled_
{
    return NO;
}

-(id)userLocation_
{
    return nil;
}

-(void)setUserTrackingMode_:(id)value
{
}

-(id)userTrackingMode_
{
    return NUMINT(0);
}

-(void)setZoom_:(id)zoom
{
}

-(void)setMinZoom_:(id)zoom
{
}

-(void)setMaxZoom_:(id)zoom
{
}

- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated
{
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
}

-(void)setCenterCoordinate_:(id)center
{
}

-(id)centerCoordinate_
{
    return nil;
}

-(void)addRoute:(AkylasMapRouteProxy*)route
{
	[self internalAddAnnotations:route];
}

-(void)removeRoute:(AkylasMapRouteProxy*)route
{
	[self internalRemoveAnnotations:route];
}

-(void)setTintColor_:(id)color
{
}

-(void)setDefaultPinImage_:(id)image
{
    self.defaultPinImage = [[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self.proxy]];
}
-(void)setDefaultPinAnchor_:(id)anchor
{
    self.defaultPinAnchor = [TiUtils pointValue:anchor];
}
-(void)setDefaultCalloutAnchor_:(id)anchor
{
    self.defaultCalloutAnchor = [TiUtils pointValue:anchor];
}
@end
