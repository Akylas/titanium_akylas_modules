/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapViewProxy.h"
#import "AkylasMapView.h"
#import "AkylasMapModule.h"
#import "AkylasMapRouteProxy.h"

@implementation AkylasMapViewProxy
{
    AkylasMapAnnotationProxy* selectedAnnotation; // Annotation to select on initial display
	NSMutableArray* annotationsToAdd; // Annotations to add on initial display
	NSMutableArray* annotationsToRemove; // Annotations to remove on initial display
	NSMutableArray* routesToAdd;
	NSMutableArray* routesToRemove;
	int zoomCount; // Number of times to zoom in/out on initial display
}

#pragma mark Internal

-(NSArray *)keySequence
{
    return [NSArray arrayWithObjects:
            @"tileSource",
            @"region",
            @"centerCoordinate",
            @"minZoom",
            @"maxZoom",
            @"zoom",
            nil];
}

-(void)_destroy
{
	RELEASE_TO_NIL(selectedAnnotation);
	RELEASE_TO_NIL(annotationsToAdd);
	RELEASE_TO_NIL(annotationsToRemove);
	RELEASE_TO_NIL(routesToAdd);
	RELEASE_TO_NIL(routesToRemove);
	[super _destroy];
}

-(NSString*)apiName
{
    return @"Akylas.Map.View";
}

-(id) centerCoordinate
{
    id result =[(AkylasMapView*)[self getOrCreateView] centerCoordinate];
	return result;
}

-(void)viewDidInitialize
{
//	ENSURE_UI_THREAD_0_ARGS;
	AkylasMapView * ourView = (AkylasMapView *)[self view];

    if (annotationsToAdd )[ourView addAnnotations:annotationsToAdd];
    if (annotationsToRemove )[ourView removeAnnotations:annotationsToRemove];

    for (id arg in routesToAdd)
    {
        [ourView addRoute:arg];
    }
    
    for (id arg in routesToRemove)
    {
        [ourView removeRoute:arg];
    }
    
	[ourView selectAnnotation:selectedAnnotation];
	if (zoomCount > 0) {
		for (int i=0; i < zoomCount; i++) {
			[ourView zoom:[NSNumber numberWithDouble:1.0]];
		}
	}
	else {
		for (int i=zoomCount;i < 0;i++) {
			[ourView zoom:[NSNumber numberWithDouble:-1.0]];
		}
	}
	
	RELEASE_TO_NIL(selectedAnnotation);
	RELEASE_TO_NIL(annotationsToAdd);
	RELEASE_TO_NIL(annotationsToRemove);
	RELEASE_TO_NIL(routesToAdd);
	RELEASE_TO_NIL(routesToRemove);
    
	[super viewDidInitialize];
}

-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg
{
	AkylasMapAnnotationProxy *proxy = [self objectOfClass:[AkylasMapAnnotationProxy class] fromArg:arg];
    if (proxy) {
        [proxy setPlaced:NO];
		[proxy setDelegate:self];
    }
	return proxy;
}

-(AkylasMapRouteProxy*)routeFromArg:(id)arg
{
	AkylasMapRouteProxy *proxy = [self objectOfClass:[AkylasMapRouteProxy class] fromArg:arg];
    if (proxy) {
		[proxy setDelegate:self];
    }
	return proxy;
}

#pragma mark Public API

-(void)zoom:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewInitialized]) {
		TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] zoom:arg];}, YES);
	}
	else {
		double v = [TiUtils doubleValue:arg];
		// TODO: Find good delta tolerance value to deal with floating point goofs
		if (v == 0.0) {
			return;
		}
		if (v > 0) {
			zoomCount++;
		}
		else {
			zoomCount--;
		}
	}
}

-(void)selectAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewInitialized]) {
		 TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] selectAnnotation:arg];}, YES);
	}
	else {
		if (selectedAnnotation != arg) {
			RELEASE_TO_NIL(selectedAnnotation);
			selectedAnnotation = [arg retain];
		}
	}
}

-(void)selectUserAnnotation:(id)unused
{
	if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] selectUserAnnotation];}, YES);
	}
}

-(void)deselectAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewInitialized]) {
		TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] deselectAnnotation:arg];}, YES);
	}
	else {
		RELEASE_TO_NIL(selectedAnnotation);
	}
}

-(void)addAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
    AkylasMapAnnotationProxy* annProxy = [self annotationFromArg:arg];
    if (!annProxy) return;
    [self rememberProxy:annProxy];
    
	if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addAnnotation:arg];}, YES);
	}
	else 
	{
		if (annotationsToAdd==nil)
		{
			annotationsToAdd = [[NSMutableArray alloc] init];
		}
		if (annotationsToRemove!=nil && [annotationsToRemove containsObject:annProxy])
		{
			[annotationsToRemove removeObject:annProxy];
		}
		else 
		{
			[annotationsToAdd addObject:annProxy];
		}
	}
}

-(void)addAnnotations:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSArray);
    NSMutableArray* newAnnotations = [NSMutableArray arrayWithCapacity:[arg count]];
    for (id ann in arg) {
        AkylasMapAnnotationProxy* annotation = [self annotationFromArg:ann];
        [newAnnotations addObject:annotation];
        [self rememberProxy:annotation];
    }
    
	if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addAnnotations:newAnnotations];}, YES);
	}
	else {
		for (id annotation in newAnnotations) {
			[self addAnnotation:annotation];
		}
	}
}

-(void)setAnnotations:(id)arg{
    ENSURE_TYPE(arg,NSArray);
    
    NSMutableArray* newAnnotations = [NSMutableArray arrayWithCapacity:[arg count]];
    for (id ann in arg) {
        [newAnnotations addObject:[self annotationFromArg:ann]];
    }
    
    BOOL attached = [self viewInitialized];
    __block NSArray* currentAnnotations = nil;
    if (attached) {
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapView*)[self view] customAnnotations] retain];
        }, YES);
    }
    else {
        currentAnnotations = annotationsToAdd;
    }
 
    // Because the annotations may contain an annotation proxy and not just
    // descriptors for them, we have to check and make sure there is
    // no overlap and remember/forget appropriately.
    
    for(AkylasMapAnnotationProxy * annProxy in currentAnnotations) {
        if (![newAnnotations containsObject:annProxy]) {
            [self forgetProxy:annProxy];
        }
    }
    for(AkylasMapAnnotationProxy* annProxy in newAnnotations) {
        if (![currentAnnotations containsObject:annProxy]) {
            [self rememberProxy:annProxy];
        }
    }
    
    if(attached) {
        TiThreadPerformOnMainThread(^{
            [(AkylasMapView*)[self view] setAnnotations_:newAnnotations];
        }, YES);
        [currentAnnotations release];
    }
    else {
        RELEASE_TO_NIL(annotationsToAdd);
        RELEASE_TO_NIL(annotationsToRemove);
        
        annotationsToAdd = [[NSMutableArray alloc] initWithArray:newAnnotations];
    }
}

-(NSArray*)annotations
{
    if ([self viewInitialized]) {
        __block NSArray* currentAnnotations = nil;
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapView*)[self view] customAnnotations] retain];
        }, YES);
        return [currentAnnotations autorelease];
    }
    else {
        return annotationsToAdd;
    }
}

-(void)removeAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
    
    // For legacy reasons, we can apparently allow the arg here to be a string (0.8 compatibility?!?)
    // and so only need to convert/remember/forget if it is an annotation instead.
    if ([arg isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        [self forgetProxy:arg];
    }
    
	if ([self viewInitialized])
	{
        TiThreadPerformOnMainThread(^{
            [(AkylasMapView*)[self view] removeAnnotation:arg];
        }, NO);
	}
	else 
	{
		if (annotationsToRemove==nil)
		{
			annotationsToRemove = [[NSMutableArray alloc] init];
		}
		if (annotationsToAdd!=nil && [annotationsToAdd containsObject:arg]) 
		{
			[annotationsToAdd removeObject:arg];
		}
		else 
		{
			[annotationsToRemove addObject:arg];
		}
	}
}

-(void)removeAnnotations:(id)arg
{
    ENSURE_SINGLE_ARG(arg,NSArray);
    for (id ann in arg) {
        if ([ann isKindOfClass:[AkylasMapAnnotationProxy class]]) {
            [self forgetProxy:ann];
        }
    }
    
	if ([self viewInitialized]) {
        [(AkylasMapView*)[self view] removeAnnotations:arg];
	}
	else {
		for (id annotation in arg) {
			[self removeAnnotation:annotation];
		}
	}
}

-(void)removeAllAnnotations:(id)unused
{
	if ([self viewInitialized]) {
        __block NSArray* currentAnnotations = nil;
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapView*)[self view] customAnnotations] retain];
        }, YES);
        
        for(id object in currentAnnotations)
        {
            AkylasMapAnnotationProxy * annProxy = [self annotationFromArg:object];
            [self forgetProxy:annProxy];
        }
        [currentAnnotations release];
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeAllAnnotations:unused];}, YES);
	}
	else 
	{
        for (AkylasMapAnnotationProxy* annotation in annotationsToAdd) {
            [self forgetProxy:annotation];
        }
        
        RELEASE_TO_NIL(annotationsToAdd);
        RELEASE_TO_NIL(annotationsToRemove);
	}
}

-(void)addRoute:(id)arg
{
    AkylasMapRouteProxy* routeProxy = [self routeFromArg:arg];
    if (!routeProxy) return;
    [self rememberProxy:routeProxy];
   
	if ([self viewInitialized])
	{
		TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addRoute:routeProxy];}, YES);
	}
	else 
	{
		if (routesToAdd==nil)
		{
			routesToAdd = [[NSMutableArray alloc] init];
		}
		if (routesToRemove!=nil && [routesToRemove containsObject:routeProxy])
		{
			[routesToRemove removeObject:routeProxy];
		}
		else 
		{
			[routesToAdd addObject:routeProxy];
		}
	}
}

-(void)removeRoute:(id)arg
{
	ENSURE_SINGLE_ARG(arg,AkylasMapRouteProxy);
    [self forgetProxy:arg];
    
	if ([self viewInitialized])
	{
		TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeRoute:arg];}, YES);
	}
	else 
	{
		if (routesToRemove==nil)
		{
			routesToRemove = [[NSMutableArray alloc] init];
		}
		if (routesToAdd!=nil && [routesToAdd containsObject:arg])
		{
			[routesToAdd removeObject:arg];
		}
		else 
		{
			[routesToRemove addObject:arg];
		}
	}
}


-(NSDictionary*)region
{
    return [(AkylasMapView*)[self getOrCreateView] getRegion];
}

-(void)zoomIn:(id)arg{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary)
    AkylasMapView* mapView = (AkylasMapView*)[self view];
    if (mapView) {
        CGPoint centerPoint = [TiUtils pointValue:arg def:CGPointMake(CGRectGetMidX(mapView.bounds), CGRectGetMidY(mapView.bounds))];
        BOOL animated = [TiUtils boolValue:@"animated" properties:arg def:YES];
		TiThreadPerformOnMainThread(^{[mapView zoomInAt:centerPoint animated:animated];}, YES);
    }
}

-(void)zoomOut:(id)arg{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary)
    AkylasMapView* mapView = (AkylasMapView*)[self view];
    if (mapView) {
        CGPoint centerPoint = [TiUtils pointValue:arg def:CGPointMake(CGRectGetMidX(mapView.bounds), CGRectGetMidY(mapView.bounds))];
        BOOL animated = [TiUtils boolValue:@"animated" properties:arg def:YES];
		TiThreadPerformOnMainThread(^{[mapView zoomOutAt:centerPoint animated:animated];}, YES);
        
    }
}

@end
