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
#import "AkylasMapTileSourceProxy.h"

@implementation AkylasMapViewProxy
{
    AkylasMapAnnotationProxy* selectedAnnotation; // Annotation to select on initial display
    NSMutableArray* _annotations;
    NSMutableArray* _routes;
    NSMutableArray* _tileSources;
    long _maxAnnotations;
}

#pragma mark Internal

-(NSArray *)keySequence
{
    return [NSArray arrayWithObjects:
            @"minZoom",
            @"maxZoom",
            @"zoom",
            @"tileSource",
            @"region",
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
    if (_annotations) {
        for (id proxy in _annotations) {
            [self forgetProxy:proxy];
        }
        RELEASE_TO_NIL(_annotations);
    }
    if (_routes) {
        for (id proxy in _routes) {
            [self forgetProxy:proxy];
        }
        RELEASE_TO_NIL(_routes);
    }
    if (_tileSources) {
        for (id proxy in _tileSources) {
            [self forgetProxy:proxy];
        }
        RELEASE_TO_NIL(_tileSources);
    }
	[super _destroy];
}

-(NSString*)apiName
{
    return @"Akylas.Map.View";
}

-(void)configurationStart
{
    [super configurationStart];
	AkylasMapView * ourView = (AkylasMapView *)[self view];
    if (_tileSources) {
        [ourView addTileSource:_tileSources atIndex:-1];
    }
    if (_annotations) {
        [ourView addAnnotation:_annotations atIndex:-1];
    }
    
    if (_routes) {
        [ourView addRoute:_routes atIndex:-1];
    }
    
	[ourView selectAnnotation:selectedAnnotation];
//	if (zoomCount > 0) {
//		for (int i=0; i < zoomCount; i++) {
//			[ourView zoom:[NSNumber numberWithDouble:1.0]];
//		}
//	}
//	else {
//		for (int i=zoomCount;i < 0;i++) {
//			[ourView zoom:[NSNumber numberWithDouble:-1.0]];
//		}
//	}
	
	RELEASE_TO_NIL(selectedAnnotation);
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


-(AkylasMapTileSourceProxy*)tileSourceFromArg:(id)arg
{
    if (IS_OF_CLASS(arg, NSString)) {
        return [self objectOfClass:[AkylasMapTileSourceProxy class] fromArg:@{@"source":arg}];
    }
    return [self objectOfClass:[AkylasMapTileSourceProxy class] fromArg:arg];
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
//	else {
//		double v = [TiUtils doubleValue:arg];
//		// TODO: Find good delta tolerance value to deal with floating point goofs
//		if (v == 0.0) {
//			return;
//		}
//		if (v > 0) {
//			zoomCount++;
//		}
//		else {
//			zoomCount--;
//		}
//	}
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
        [self removeAnnotation:@[toRemove]];
    }
    
}

-(void)addAnnotation:(id)arg
{
    if (arg == nil) return;
    id newAnnotations = nil;
    NSInteger index = -1;
    if (IS_OF_CLASS(arg, NSArray) && [arg count] == 2 && IS_OF_CLASS([arg objectAtIndex:1], NSNumber)) {
        index = [[arg objectAtIndex:1] integerValue];
        arg = [arg objectAtIndex:0];
    }
    if (IS_OF_CLASS(arg, NSArray)) {
        newAnnotations = [NSMutableArray arrayWithCapacity:[arg count]];
        for (id ann in arg) {
            AkylasMapAnnotationProxy* annProxy = [self annotationFromArg:ann];
            if (!annProxy || [_annotations containsObject:annProxy]) {
                continue;
            }
            [(NSMutableArray*)newAnnotations addObject:annProxy];
            [self rememberProxy:annProxy];
        }
        if (!_annotations) {
            _annotations = [NSMutableArray new];
        }
        NSUInteger toAddCount = [newAnnotations count];
        [self handleMaxAnnotationsForAboutToAdd:toAddCount];
        if (_maxAnnotations > 0 && toAddCount > _maxAnnotations) {
            //handle the case where we already add more than we can chew
            newAnnotations = [newAnnotations subarrayWithRange:NSMakeRange(toAddCount - _maxAnnotations, _maxAnnotations)];
        }
        [_annotations addObjectsFromArray:newAnnotations];
        
    } else {
        newAnnotations = [self annotationFromArg:arg];
        if (!newAnnotations || [_annotations containsObject:newAnnotations]) return;
        [self rememberProxy:newAnnotations];
        if (!_annotations) {
            _annotations = [NSMutableArray new];
        }
        [self handleMaxAnnotationsForAboutToAdd:1];
        [_annotations addObject:newAnnotations];
    }

	if (newAnnotations && [self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addAnnotation:newAnnotations atIndex:index];}, YES);
	}
}

-(void)setAnnotations:(id)arg{
    [self removeAllAnnotations:nil];
    [self addAnnotation:arg];
}

-(NSArray*)annotations
{
    return _annotations;
}

-(void)removeAnnotation:(id)arg
{
    if (!arg) return;
    if (IS_OF_CLASS(arg, NSArray)) {
        for (id ann in arg) {
            if (IS_OF_CLASS(ann, AkylasMapAnnotationProxy))
            [self forgetProxy:ann];
        }
        [_annotations removeObjectsInArray:arg];
    } else if (IS_OF_CLASS(arg, AkylasMapAnnotationProxy)) {
        [self forgetProxy:arg];
        [_annotations removeObject:arg];
    }
    if ([self viewInitialized]) {
        [(AkylasMapView*)[self view] removeAnnotation:arg];
    }
}

-(void)removeAllAnnotations:(id)unused
{
    if (_annotations) {
        for (id ann in _annotations) {
            [self forgetProxy:ann];
        }
        RELEASE_TO_NIL(_annotations)
        if ([self viewInitialized]) {
            TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeAllAnnotations];}, YES);
        }
    }
}

/////

-(void)addRoute:(id)arg
{
    if (arg == nil) return;
    id newRoutes = nil;
    NSInteger index = -1;
    if (IS_OF_CLASS(arg, NSArray) && [arg count] == 2 && IS_OF_CLASS([arg objectAtIndex:1], NSNumber)) {
        index = [[arg objectAtIndex:1] integerValue];
        arg = [arg objectAtIndex:0];
    }
    if (IS_OF_CLASS(arg, NSArray)) {
        newRoutes = [NSMutableArray arrayWithCapacity:[arg count]];
        for (id ann in arg) {
            AkylasMapRouteProxy* routeProxy = [self routeFromArg:ann];
            if (!routeProxy || [_routes containsObject:routeProxy]) {
                continue;
            }
            [(NSMutableArray*)newRoutes addObject:routeProxy];
            [self rememberProxy:routeProxy];
        }
        if (!_routes) {
            _routes = [NSMutableArray new];
        }
        
        [_routes addObjectsFromArray:newRoutes];
        
    } else {
        newRoutes = [self routeFromArg:arg];
        if (!newRoutes || [_routes containsObject:newRoutes]) return;
        [self rememberProxy:newRoutes];
        if (!_routes) {
            _routes = [NSMutableArray new];
        }
        [_routes addObject:newRoutes];
    }
    
    if (newRoutes && [self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addRoute:newRoutes atIndex:index];}, YES);
    }
}

-(void)setRoutes:(id)arg{
    [self removeAllRoutes:nil];
    [self addRoute:arg];
}

-(NSArray*)routes
{
    return _routes;
}

-(void)removeRoute:(id)arg
{
    if (!arg) return;
    if (IS_OF_CLASS(arg, NSArray)) {
        for (id ann in arg) {
            if (IS_OF_CLASS(ann, AkylasMapRouteProxy))
                [self forgetProxy:ann];
        }
        [_routes removeObjectsInArray:arg];
    } else if (IS_OF_CLASS(arg, AkylasMapRouteProxy)) {
        [self forgetProxy:arg];
        [_routes removeObject:arg];
    }
    if ([self viewInitialized]) {
        [(AkylasMapView*)[self view] removeRoute:arg];
    }
}

-(void)removeAllRoutes:(id)unused
{
    if (_routes) {
        for (id ann in _routes) {
            [self forgetProxy:ann];
        }
        RELEASE_TO_NIL(_routes)
        if ([self viewInitialized]) {
            TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeAllRoutes];}, YES);
        }
    }
}

/////

-(void)addTileSource:(id)arg
{
    if (arg == nil) return;
    id newTileSources = nil;
    NSInteger index = -1;
    if (IS_OF_CLASS(arg, NSArray) && [arg count] == 2 && IS_OF_CLASS([arg objectAtIndex:1], NSNumber)) {
        index = [[arg objectAtIndex:1] integerValue];
        arg = [arg objectAtIndex:0];
    }
    if (IS_OF_CLASS(arg, NSArray)) {
        newTileSources = [NSMutableArray arrayWithCapacity:[arg count]];
        for (id ann in arg) {
            AkylasMapTileSourceProxy* tileSourceProxy = [self tileSourceFromArg:ann];
            if (!tileSourceProxy || [_tileSources containsObject:tileSourceProxy]) {
                continue;
            }
            [(NSMutableArray*)newTileSources addObject:tileSourceProxy];
            [self rememberProxy:tileSourceProxy];
        }
        if (!_tileSources) {
            _tileSources = [NSMutableArray new];
        }
        
        [_tileSources addObjectsFromArray:newTileSources];
        
    } else {
        newTileSources = [self tileSourceFromArg:arg];
        if (!newTileSources || [_tileSources containsObject:newTileSources]) return;
        [self rememberProxy:newTileSources];
        if (!_tileSources) {
            _tileSources = [NSMutableArray new];
        }
        [_tileSources addObject:newTileSources];
    }
    
    if (newTileSources && [self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] addTileSource:newTileSources atIndex:index];}, YES);
    }
}

-(void)setTileSource:(id)arg{
    [self removeAllTileSources:nil];
    [self addTileSource:arg];
}

-(id)tileSource
{
    return _tileSources;
}

-(void)removeTileSource:(id)arg
{
    if (!arg) return;
    if (IS_OF_CLASS(arg, NSArray)) {
        for (id ann in arg) {
            if (IS_OF_CLASS(ann, AkylasMapTileSourceProxy))
                [self forgetProxy:ann];
        }
        [_tileSources removeObjectsInArray:arg];
    } else if (IS_OF_CLASS(arg, AkylasMapTileSourceProxy)) {
        [self forgetProxy:arg];
        [_tileSources removeObject:arg];
    }
    if ([self viewInitialized]) {
        TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeTileSource:arg];}, YES);
    }
}

-(void)removeAllTileSources:(id)unused
{
    if (_tileSources) {
        for (id ann in _tileSources) {
            [self forgetProxy:ann];
        }
        RELEASE_TO_NIL(_tileSources)
        if ([self viewInitialized]) {
            TiThreadPerformOnMainThread(^{[(AkylasMapView*)[self view] removeAllTileSources];}, YES);
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

-(void)updateCamera:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSDictionary)
    AkylasMapView* mapView = (AkylasMapView*)[self view];
    if (mapView) {
        TiThreadPerformOnMainThread(^{[mapView updateCamera:args];}, YES);
        
    } else {
        [self applyProperties:args];
    }
}

@end
