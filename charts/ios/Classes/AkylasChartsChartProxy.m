/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsChartProxy.h"
#import "AkylasChartsChart.h"
#import "AkylasChartsPlotProxy.h"

#import "AkylasChartsPieSegmentProxy.h"
#import "AkylasChartsMarkerAnnotation.h"
#import "AkylasChartsMarkerProxy.h"

#import "TiUtils.h"

#define PREPARE_ARRAY_ARGS(args) \
ENSURE_TYPE(args, NSArray) \
NSNumber* num = nil; \
NSInteger index = -1; \
NSObject* value = nil; \
ENSURE_ARG_OR_NIL_AT_INDEX(value, args, 0, NSObject); \
ENSURE_ARG_OR_NIL_AT_INDEX(num, args, 1, NSNumber); \
if (num) { \
index = [num integerValue]; \
} \


@implementation AkylasChartsChartProxy
{
    NSMutableArray	*_markers;
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    if ([properties isEqual:[NSNull null]]) {
        [self initializeProperty:@"fillColor" defaultValue:[properties objectForKey:@"backgroundColor"]];
        [self initializeProperty:@"fillGradient" defaultValue:[properties objectForKey:@"backgroundGradient"]];
        [self initializeProperty:@"fillOpacity" defaultValue:[properties objectForKey:@"backgroundOpacity"]];
    }
	[super _initWithProperties:properties];
}

-(void)dealloc
{
    if ((_markers)) {
        for (id proxy in (_markers)) {
//            [proxy setDelegate:nil];
            [self forgetProxy:proxy];
        }
        RELEASE_TO_NIL((_markers));
    }
	[super dealloc];
}

-(void)refreshPlotSpaces
{
	if ([self view]) {
		[(AkylasChartsChart*)[self view] refreshPlotSpaces];
	}
}

-(void)relayout:(id)args
{
    [(AkylasChartsChart*)[self view] refreshPlotSpaces];
}

-(NSArray*)plots
{
    if (childrenCount == 0) return nil;
    pthread_rwlock_rdlock(&childrenLock);
    NSArray* copy = [[children filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(id object, NSDictionary *bindings) {
        return [object isKindOfClass:[AkylasChartsPlotProxy class]] || [object isKindOfClass:[AkylasChartsPieSegmentProxy class]];
    }]] retain];
    pthread_rwlock_unlock(&childrenLock);
    
	return [copy autorelease];
}

-(void)childAdded:(TiProxy*)child atIndex:(NSInteger)position shouldRelayout:(BOOL)shouldRelayout
{
    [super childAdded:child atIndex:position shouldRelayout:shouldRelayout];
   // Make sure that we are getting a plot proxy object
    if (![child isKindOfClass:[AkylasChartsPlotProxy class]] && ![child isKindOfClass:[AkylasChartsPieSegmentProxy class]]) {
        return;
    }
    
//    if ([child isKindOfClass:[AkylasChartsPlotProxy class]])
//        ((AkylasChartsPlotProxy*)child).chartProxy = self;
//    else if ([child isKindOfClass:[AkylasChartsPieSegmentProxy class]])
//        ((AkylasChartsPieSegmentProxy*)child).chartProxy = self;
    
    // If a view is currently attached to this proxy then tell it to add this new plot
    // to the graph
    if ([self view]) {
        [(AkylasChartsChart*)[self view] addPlot:child];
    }
}
-(void)childRemoved:(TiProxy*)child
{
    // Make sure that we are getting a plot proxy object
    if (![child isKindOfClass:[AkylasChartsPlotProxy class]] && ![child isKindOfClass:[AkylasChartsPieSegmentProxy class]]) {
        return;
	}
		
	// If a view is currently attached to this proxy then tell it to remove the plot
	// from the graph
	if ([self view]) {
		[(AkylasChartsChart*)[self view] removePlot:child];
	}
}


-(void)viewDidInitialize
{
	[super viewDidInitialize];
    for (id plot in [self plots]) {
        [(AkylasChartsChart*)[self view] addPlot:plot];
    }
    for (AkylasChartsMarkerProxy* marker in _markers) {
        [(AkylasChartsChart*)[self view] addMarker:marker];
    }
}

-(void)viewWillDetach
{
    [(AkylasChartsChart*)[self view] removeAllPlots];
    [(AkylasChartsChart*)[self view] removeAllMarkers];
	[super viewWillDetach];
}

#ifndef USE_VIEW_FOR_UI_METHOD
	#define USE_VIEW_FOR_UI_METHOD(methodname)\
	-(void)methodname:(id)args\
	{\
		[self makeViewPerformSelector:@selector(methodname:) withObject:args createIfNeeded:YES waitUntilDone:NO];\
	}
#endif
USE_VIEW_FOR_UI_METHOD(refresh);

-(AkylasChartsMarkerProxy*)markerFromArg:(id)arg
{
    AkylasChartsMarkerProxy *proxy = [self objectOfClass:[AkylasChartsMarkerProxy class] fromArg:arg];
	return proxy;
}


-(id)addMarker:(id)args
{
    PREPARE_ARRAY_ARGS(args)
    id newMarkers = nil;
    if (IS_OF_CLASS(value, NSArray)) {
        NSArray* array = (NSArray*)value;
        newMarkers = [NSMutableArray arrayWithCapacity:[array count]];
        for (id marker in array) {
            AkylasChartsMarkerProxy* markerProxy = [self markerFromArg:marker];
            if (!markerProxy || [_markers containsObject:markerProxy]) {
                continue;
            }
            [(NSMutableArray*)newMarkers addObject:markerProxy];
//            markerProxy.delegate = self;
            [self rememberProxy:markerProxy];
        }
        if (!_markers) {
            _markers = [NSMutableArray new];
        }
        
        [_markers addObjectsFromArray:newMarkers];
        
    } else {
        newMarkers = [self markerFromArg:value];
        if (!newMarkers || [_markers containsObject:newMarkers]) return;
        [self rememberProxy:newMarkers];
//        ((AkylasMapBaseRouteProxy*)newMarkers).delegate = self;
        if (!_markers) {
            _markers = [NSMutableArray new];
        }
        [_markers addObject:newMarkers];
        newMarkers = @[newMarkers];
    }
    
    if (newMarkers && [self viewInitialized]) {
        for (AkylasChartsMarkerProxy* marker in newMarkers) {
            [(AkylasChartsChart*)[self view] addMarker:marker];
        }
    }
    return newMarkers;
}

-(void)setMarkers:(id)arg{
    [self removeAllMarkers:nil];
    [self addMarker:@[arg]];
}

-(NSArray*)markers
{
    return _markers;
}

-(void)removeMarker:(id)args
{
    PREPARE_ARRAY_ARGS(args)
    id oldMarkers = nil;
    if (IS_OF_CLASS(value, NSArray)) {
        NSArray* array = (NSArray*)value;
        oldMarkers = [NSMutableArray arrayWithCapacity:[array count]];
        for (id markerProxy in array) {
            if (IS_OF_CLASS(markerProxy, AkylasChartsMarkerProxy)) {
//                [ann setDelegate:nil];
                [self forgetProxy:markerProxy];
                if ([markerProxy valueForUndefinedKey:@"bindId"]) {
                    [self removeBindingsForProxy:markerProxy];
                }
                [(NSMutableArray*)oldMarkers addObject:markerProxy];
            }
        }
        [_markers removeObjectsInArray:array];
    } else if (IS_OF_CLASS(value, AkylasChartsMarkerProxy)) {
//        [(AkylasChartsMarkerProxy*)value setDelegate:nil];
        [self forgetProxy:(AkylasChartsMarkerProxy*)value];
        [_markers removeObject:value];
        oldMarkers = @[value];
    }
    if ([self viewInitialized]) {
        
        for (AkylasChartsMarkerProxy* marker in oldMarkers) {
            [(AkylasChartsChart*)[self view] removeMarker:marker.marker];
        }
    }
}

-(void)removeAllMarkers:(id)unused
{
    if ([_markers count] > 0) {
        for (id markerProxy in _markers) {
//            [ann setDelegate:nil];
            [self forgetProxy:markerProxy];
        }
        if ([self viewInitialized]) {
            [(AkylasChartsChart*)[self view] removeAllMarkers];

        }
        RELEASE_TO_NIL(_markers)
    }
}

/////


@end
