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

@implementation AkylasChartsChartProxy
{
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
	// release any resources that have been retained by the module
	RELEASE_TO_NIL(markers);
	
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

-(NSMutableArray*)markers
{
	if (markers == nil) {
		markers = [[NSMutableArray alloc] init];
	}
	
	return markers;
}

-(void)childAdded:(TiProxy*)child atIndex:(NSInteger)position shouldRelayout:(BOOL)shouldRelayout
{
    // Make sure that we are getting a plot proxy object
    if (![child isKindOfClass:[AkylasChartsPlotProxy class]] && ![child isKindOfClass:[AkylasChartsPieSegmentProxy class]]) {
        return;
    }
    
    if ([child isKindOfClass:[AkylasChartsPlotProxy class]])
        ((AkylasChartsPlotProxy*)child).chartProxy = self;
    else if ([child isKindOfClass:[AkylasChartsPieSegmentProxy class]])
        ((AkylasChartsPieSegmentProxy*)child).chartProxy = self;
    
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
    for (AkylasChartsMarkerProxy* marker in markers) {
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

-(id)addMarker:(id)arg
{
    ENSURE_SINGLE_ARG(arg, NSObject)
    AkylasChartsMarkerProxy* marker = [self markerFromArg:arg];
    if(!marker || [markers containsObject:marker]) return;
    [self rememberProxy:marker];
    [[self markers] addObject:marker];
    if ([self view]) {
        [(AkylasChartsChart*)[self view] addMarker:marker];
    }
    return marker;
}

-(void)removeMarker:(id)arg
{
    ENSURE_SINGLE_ARG(arg, AkylasChartsMarkerProxy)
    if (arg != nil) {
        [self forgetProxy:arg];
        if(![markers containsObject:arg]) return;
        if ([self view]) {
            [(AkylasChartsChart*)[self view] removeMarker:arg];
        }
        [markers removeObject:arg];
    }
}

+(Class)proxyClassFromString:(NSString*)qualifiedName
{
    Class proxyClass = (Class)CFDictionaryGetValue([TiProxy classNameLookup], qualifiedName);
	if (proxyClass == nil) {
		NSString *prefix = [NSString stringWithFormat:@"%@%s",@"Ak","ylasCharts."];
		if ([qualifiedName hasPrefix:prefix]) {
			qualifiedName = [qualifiedName stringByReplacingOccurrencesOfString:prefix withString:@"AkylasCharts"];
		}
        else {
            return [[TiViewProxy class] proxyClassFromString:qualifiedName];
        }
		NSString *className = [[qualifiedName stringByReplacingOccurrencesOfString:@"." withString:@""] stringByAppendingString:@"Proxy"];
		proxyClass = NSClassFromString(className);
		if (proxyClass==nil) {
			DebugLog(@"[WARN] Attempted to load %@: Could not find class definition.", className);
			@throw [NSException exceptionWithName:@"org.appcelerator.module"
                                           reason:[NSString stringWithFormat:@"Class not found: %@", qualifiedName]
                                         userInfo:nil];
		}
		CFDictionarySetValue([TiProxy classNameLookup], qualifiedName, proxyClass);
	}
    return proxyClass;
}

@end
