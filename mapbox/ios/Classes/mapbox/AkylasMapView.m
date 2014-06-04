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

#pragma mark Internal

- (id)init
{
    if ((self = [super init])) {
        defaultPinAnchor = CGPointMake(0.5, 0.5);
        _zoom = -1;
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

#pragma mark Public APIs

-(void)addAnnotation:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(addAnnotation,args);
	[self internalAddAnnotations:[self annotationFromArg:args]];
}

-(void)addAnnotations:(id)args
{
	ENSURE_TYPE(args,NSArray);
	ENSURE_UI_THREAD(addAnnotations,args);

	[self internalAddAnnotations:[self annotationsFromArgs:args]];
}

-(void)removeAnnotation:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(removeAnnotation,args);

	 [self internalRemoveAnnotations:args];
}

-(void)removeAnnotations:(id)args
{
	ENSURE_TYPE_OR_NIL(args,NSArray); // assumes an array of AkylasMapboxAnnotationProxy, and NSString classes
	ENSURE_UI_THREAD(removeAnnotation,args);
    [self internalRemoveAnnotations:args];
}

-(void)internalRemoveAllAnnotations
{
    [self internalRemoveAnnotations:self.customAnnotations];
}

-(void)removeAllAnnotations:(id)args
{
	ENSURE_UI_THREAD(removeAllAnnotations,args);
    [self internalRemoveAllAnnotations];
}

-(void)setAnnotations_:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	ENSURE_UI_THREAD(setAnnotations_,value)
    [self internalRemoveAllAnnotations];
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

-(void)zoom:(id)args
{
}

-(RMSphericalTrapezium) getCurrentRegion
{
    return kMapboxDefaultLatLonBoundingBox;
}

#pragma mark Public APIs

-(id)getRegion
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

-(id)userLocationEnabled
{
    return NO;
}

-(id)userLocation
{
    return nil;
}

-(void)setUserTrackingMode_:(id)value
{
}

-(id)userTrackingMode
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

-(id)centerCoordinate
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
    self.defaultPinImage = [[[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self.proxy]] retain];
}
-(void)setDefaultPinAnchor_:(id)anchor
{
    self.defaultPinAnchor = [TiUtils pointValue:anchor];
}
@end
