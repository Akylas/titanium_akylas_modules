/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapboxViewProxy.h"
#import "AkylasMapboxView.h"
#import "AkylasMapboxModule.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxTileSourceProxy.h"

@implementation AkylasMapboxViewProxy

#pragma mark Internal

-(NSArray *)keySequence
{
    return [NSArray arrayWithObjects:
            @"animate",
            @"location",
            @"regionFit",
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
    return @"Akylas.Mapbox.View";
}

//-(NSNumber*) longitudeDelta
//{
//	__block CLLocationDegrees delta = 0.0;
//	
//	if ([self viewAttached]) {
//		TiThreadPerformOnMainThread(^{
//			delta = [(AkylasMapboxView *)[self view] longitudeDelta];
//		},YES);
//		
//	}
//	return [NSNumber numberWithDouble:delta];
//
//}

//-(NSNumber*) latitudeDelta
//{
//	__block CLLocationDegrees delta = 0.0;
//	
//	if ([self viewAttached]) {
//		TiThreadPerformOnMainThread(^{
//			delta = [(AkylasMapboxView *)[self view] latitudeDelta];
//		},YES);
//		
//	}
//	return [NSNumber numberWithDouble:delta];
//}

-(id) centerCoordinate
{
//	if ([self viewAttached]) {
//		TiThreadPerformOnMainThread(^{
//			delta = [(AkylasMapboxView *)[self view] latitudeDelta];
//		},YES);
//		
//	}
    id result =[(AkylasMapboxView*)[self getOrCreateView] centerCoordinate];
	return result;
}

-(void)viewDidAttach
{
	ENSURE_UI_THREAD_0_ARGS;
	AkylasMapboxView * ourView = (AkylasMapboxView *)[self view];

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
	
	[super viewDidAttach];
}

-(AkylasMapboxTileSourceProxy*)tileSourceFromArg:(id)arg
{
	if ([arg isKindOfClass:[AkylasMapboxTileSourceProxy class]])
	{
		return arg;
	}
	ENSURE_TYPE(arg,NSDictionary);
	AkylasMapboxTileSourceProxy *proxy = [[[AkylasMapboxTileSourceProxy alloc] _initWithPageContext:[self pageContext] args:[NSArray arrayWithObject:arg]] autorelease];
    
	[proxy setDelegate:self];
	return proxy;
}

-(AkylasMapboxAnnotationProxy*)annotationFromArg:(id)arg
{
	if ([arg isKindOfClass:[AkylasMapboxAnnotationProxy class]])
	{
		[(AkylasMapboxAnnotationProxy*)arg setDelegate:self];
		[arg setPlaced:NO];
		return arg;
	}
	ENSURE_TYPE(arg,NSDictionary);
	AkylasMapboxAnnotationProxy *proxy = [[[AkylasMapboxAnnotationProxy alloc] _initWithPageContext:[self pageContext] args:[NSArray arrayWithObject:arg]] autorelease];
    
	[proxy setDelegate:self];
	return proxy;
}

-(AkylasMapboxRouteProxy*)routeFromArg:(id)arg
{
	if ([arg isKindOfClass:[AkylasMapboxRouteProxy class]])
	{
		[(AkylasMapboxRouteProxy*)arg setDelegate:self];
		[arg setPlaced:NO];
		return arg;
	}
	ENSURE_TYPE(arg,NSDictionary);
	AkylasMapboxRouteProxy *proxy = [[[AkylasMapboxRouteProxy alloc] _initWithPageContext:[self pageContext] args:[NSArray arrayWithObject:arg]] autorelease];
    
	[proxy setDelegate:self];
	return proxy;
}

#pragma mark Public API

-(void)zoom:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewAttached]) {
		TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] zoom:arg];}, NO);
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
	if ([self viewAttached]) {
		 TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] selectAnnotation:arg];}, NO);
	}
	else {
		if (selectedAnnotation != arg) {
			RELEASE_TO_NIL(selectedAnnotation);
			selectedAnnotation = [arg retain];
		}
	}
}

-(void)deselectAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewAttached]) {
		TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] deselectAnnotation:arg];}, NO);
	}
	else {
		RELEASE_TO_NIL(selectedAnnotation);
	}
}

-(void)addAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
    AkylasMapboxAnnotationProxy* annProxy = [self annotationFromArg:arg];
    [self rememberProxy:annProxy];
    
	if ([self viewAttached]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] addAnnotation:arg];}, NO);
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
        AkylasMapboxAnnotationProxy* annotation = [self annotationFromArg:ann];
        [newAnnotations addObject:annotation];
        [self rememberProxy:annotation];
    }
    
	if ([self viewAttached]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] addAnnotations:newAnnotations];}, NO);
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
    
    BOOL attached = [self viewAttached];
    __block NSArray* currentAnnotations = nil;
    if (attached) {
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapboxView*)[self view] customAnnotations] retain];
        }, YES);
    }
    else {
        currentAnnotations = annotationsToAdd;
    }
 
    // Because the annotations may contain an annotation proxy and not just
    // descriptors for them, we have to check and make sure there is
    // no overlap and remember/forget appropriately.
    
    for(AkylasMapboxAnnotationProxy * annProxy in currentAnnotations) {
        if (![newAnnotations containsObject:annProxy]) {
            [self forgetProxy:annProxy];
        }
    }
    for(AkylasMapboxAnnotationProxy* annProxy in newAnnotations) {
        if (![currentAnnotations containsObject:annProxy]) {
            [self rememberProxy:annProxy];
        }
    }
    
    if(attached) {
        TiThreadPerformOnMainThread(^{
            [(AkylasMapboxView*)[self view] setAnnotations_:newAnnotations];
        }, NO);
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
    if ([self viewAttached]) {
        __block NSArray* currentAnnotations = nil;
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapboxView*)[self view] customAnnotations] retain];
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
    if ([arg isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
        [self forgetProxy:arg];
    }
    
	if ([self viewAttached]) 
	{
        TiThreadPerformOnMainThread(^{
            [(AkylasMapboxView*)[self view] removeAnnotation:arg];
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
        if ([ann isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
            [self forgetProxy:ann];
        }
    }
    
	if ([self viewAttached]) {
        [(AkylasMapboxView*)[self view] removeAnnotations:arg];
	}
	else {
		for (id annotation in arg) {
			[self removeAnnotation:annotation];
		}
	}
}

-(void)removeAllAnnotations:(id)unused
{
	if ([self viewAttached]) {
        __block NSArray* currentAnnotations = nil;
        TiThreadPerformOnMainThread(^{
            currentAnnotations = [[(AkylasMapboxView*)[self view] customAnnotations] retain];
        }, YES);
        
        for(id object in currentAnnotations)
        {
            AkylasMapboxAnnotationProxy * annProxy = [self annotationFromArg:object];
            [self forgetProxy:annProxy];
        }
        [currentAnnotations release];
        TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] removeAllAnnotations:unused];}, NO);
	}
	else 
	{
        for (AkylasMapboxAnnotationProxy* annotation in annotationsToAdd) {
            [self forgetProxy:annotation];
        }
        
        RELEASE_TO_NIL(annotationsToAdd);
        RELEASE_TO_NIL(annotationsToRemove);
	}
}

-(void)addRoute:(id)arg
{
	ENSURE_SINGLE_ARG(arg,AkylasMapboxRouteProxy);
    AkylasMapboxRouteProxy* routeProxy = [self routeFromArg:arg];
   
	if ([self viewAttached]) 
	{
		TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] addRoute:routeProxy];}, NO);
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
	ENSURE_SINGLE_ARG(arg,AkylasMapboxRouteProxy);
    
	if ([self viewAttached])
	{
		TiThreadPerformOnMainThread(^{[(AkylasMapboxView*)[self view] removeRoute:arg];}, NO);
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
-(RMMapView*)getMap
{
    return [(AkylasMapboxView*)[self getOrCreateView] map];
}

-(RMTileCache*)getTileCache
{
    return [self getMap].tileCache;
}

-(NSDictionary*)region
{
    return [(AkylasMapboxView*)[self getOrCreateView] getRegion];
}

-(void)cancelBackgroundCache:(id)arg
{
    [[self getTileCache] cancelBackgroundCache];
}

-(void)zoomIn:(id)arg{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary)
    AkylasMapboxView* mapView = (AkylasMapboxView*)[self view];
    if (mapView) {
        CGPoint centerPoint = [TiUtils pointValue:arg def:CGPointMake(CGRectGetMidX(mapView.bounds), CGRectGetMidY(mapView.bounds))];
        BOOL animated = [TiUtils boolValue:@"animated" properties:arg def:YES];
		TiThreadPerformOnMainThread(^{[mapView zoomInAt:centerPoint animated:animated];}, NO);
    }
}

-(void)zoomOut:(id)arg{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary)
    AkylasMapboxView* mapView = (AkylasMapboxView*)[self view];
    if (mapView) {
        CGPoint centerPoint = [TiUtils pointValue:arg def:CGPointMake(CGRectGetMidX(mapView.bounds), CGRectGetMidY(mapView.bounds))];
        BOOL animated = [TiUtils boolValue:@"animated" properties:arg def:YES];
		TiThreadPerformOnMainThread(^{[mapView zoomOutAt:centerPoint animated:animated];}, NO);
        
    }
}


@end
