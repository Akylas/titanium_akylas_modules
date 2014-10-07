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
	NSMutableArray* routesToAdd;
	NSMutableArray* routesToRemove;
	int zoomCount; // Number of times to zoom in/out on initial display
	NSMutableArray* _annotations;
    long _maxAnnotations;
}

#pragma mark Internal

-(NSArray *)keySequence
{
    return [NSArray arrayWithObjects:
            @"tileSource",
            @"region",
            @"minZoom",
            @"maxZoom",
            @"zoom",
            @"centerCoordinate",
            nil];
}


- (id)init
{
    if ((self = [super init])) {
        _maxAnnotations = 0;        
    }
    return self;
}


-(void)_destroy
{
	RELEASE_TO_NIL(selectedAnnotation);
	RELEASE_TO_NIL(routesToAdd);
	RELEASE_TO_NIL(routesToRemove);
	[super _destroy];
}

-(NSString*)apiName
{
    return @"Akylas.Map.View";
}

-(void)viewDidInitialize
{
//	ENSURE_UI_THREAD_0_ARGS;
	AkylasMapView * ourView = (AkylasMapView *)[self view];

    if (_annotations) {
        [ourView addAnnotations:_annotations];
    }

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
	RELEASE_TO_NIL(routesToAdd);
	RELEASE_TO_NIL(routesToRemove);
    
	[super viewDidInitialize];
}

-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg
{
    if ([arg isKindOfClass:[RMAnnotation class]]) {
        
    }
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


-(void)setMaxAnnotations:(id)arg{
    ENSURE_SINGLE_ARG(arg, NSNumber)
    _maxAnnotations = [arg longValue];
    [self handleMaxAnnotationsForAboutToAdd:0];
}

#pragma mark Public API

-(void)zoomTo:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
	if ([self viewInitialized]) {
		TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] zoomTo:arg];}, YES);
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
    if ([arg isKindOfClass:[NSNumber class]])
	{
        int index = [arg intValue];
		if (index >= 0 && index < [_annotations count]) {
            arg = [_annotations objectAtIndex:index];
        }
        else {
            return;
        }
	}
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


-(void)handleMaxAnnotationsForAboutToAdd:(NSUInteger)newCountAddition
{
    if (_maxAnnotations <= 0) return;
    NSUInteger newCount = [_annotations count] + newCountAddition;
    if (newCount > _maxAnnotations) {
        unsigned long length = MIN(newCount - _maxAnnotations, [_annotations count]);
        NSRange range = NSMakeRange(0, length);
        NSArray* toRemove = [_annotations subarrayWithRange:range];
        [self removeAnnotations:@[toRemove]];
    }
    
}

-(void)addAnnotation:(id)arg
{
	ENSURE_SINGLE_ARG(arg,NSObject);
    AkylasMapAnnotationProxy* annProxy = [self annotationFromArg:arg];
    if (!annProxy || [_annotations containsObject:annProxy]) return;
    [self rememberProxy:annProxy];
    if (!_annotations) {
        _annotations = [NSMutableArray new];
    }
    [self handleMaxAnnotationsForAboutToAdd:1];
    [_annotations addObject:annProxy];
    
	if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addAnnotation:annProxy];}, YES);
	}
}

-(void)addAnnotations:(id)arg
{
	ENSURE_SINGLE_ARG_OR_NIL(arg,NSArray);
    if (arg == nil) return;
    NSMutableArray* newAnnotations = [NSMutableArray arrayWithCapacity:[arg count]];
    for (id ann in arg) {
        AkylasMapAnnotationProxy* annProxy = [self annotationFromArg:ann];
        if (!annProxy || [_annotations containsObject:annProxy]) {
            continue;
        }
        [newAnnotations addObject:annProxy];
        [self rememberProxy:annProxy];
    }
    if (!_annotations) {
        _annotations = [NSMutableArray new];
    }
    [self handleMaxAnnotationsForAboutToAdd:[newAnnotations count]];
    NSUInteger toAddCount = [newAnnotations count];
    if (toAddCount > _maxAnnotations) {
        newAnnotations = [newAnnotations subarrayWithRange:NSMakeRange(toAddCount - _maxAnnotations, _maxAnnotations)];
    }
    [_annotations addObjectsFromArray:newAnnotations];
    
	if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addAnnotations:newAnnotations];}, YES);
	}
}

-(void)setAnnotations:(id)arg{
    [self removeAllAnnotations:nil];
    [self addAnnotations:@[arg]];
}

-(NSArray*)annotations
{
    return _annotations;
}

-(void)removeAnnotation:(id)arg
{
    if (!arg) return;
	ENSURE_SINGLE_ARG(arg,NSObject);
    
    AkylasMapAnnotationProxy* annot = nil;
    if ([arg isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        annot = arg;
    }
    else if([arg isKindOfClass:[NSNumber class]]) {
        NSInteger index = [arg integerValue];
        if (index >= 0 && index < [_annotations count]) {
            annot = [_annotations objectAtIndex:index];
        }
    }
    if (annot) {
        [self forgetProxy:annot];
        [_annotations removeObject:annot];
        if ([self viewInitialized])
        {
            TiThreadPerformOnMainThread(^{
                [(AkylasMapView*)[self view] removeAnnotation:annot];
            }, YES);
        }
    }
}

-(void)removeAnnotations:(id)arg
{
    if (!arg) return;
    ENSURE_SINGLE_ARG(arg,NSArray);
    
    NSMutableArray* removeAnnotations = [NSMutableArray arrayWithCapacity:[arg count]];
    for (id ann in arg) {
        AkylasMapAnnotationProxy* annProxy = [self annotationFromArg:ann];
        if (!annProxy || ![_annotations containsObject:annProxy]) {
            continue;
        }
        [removeAnnotations addObject:annProxy];
        [self forgetProxy:annProxy];
    }
    [_annotations removeObjectsInArray:removeAnnotations];
    
	if ([self viewInitialized]) {
        [(AkylasMapView*)[self view] removeAnnotations:removeAnnotations];
	}
}

-(void)removeAllAnnotations:(id)unused
{
    RELEASE_TO_NIL(_annotations)
    if ([self viewInitialized]) {
        [(AkylasMapView*)[self view] removeAllAnnotations];
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
