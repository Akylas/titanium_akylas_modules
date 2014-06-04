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
	RELEASE_TO_NIL(plots);
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

-(NSMutableArray*)plots
{
	if (plots == nil) {
		plots = [[NSMutableArray alloc] init];
	}
	
	return plots;
}

-(NSMutableArray*)markers
{
	if (markers == nil) {
		markers = [[NSMutableArray alloc] init];
	}
	
	return markers;
}

-(void)setPlots:(id)args
{
	// If a view is currently attached to this proxy then tell it to remove all plots
	// currently shown in the graph
	if ([self view]) {
		[(AkylasChartsChart*)[self view] removeAllPlots];
	}
	
	// Clear the current list of plots
	RELEASE_TO_NIL(plots);
	
	// Now set the current list to this new list
	[self add:args];
}

-(void)add:(id)arg
{
	if (!IS_NULL_OR_NIL(arg)) {
		// If we get an array of plot proxy objects we can just iterate through it
		// and add each one individually. This is just a helper for adding a set of
		// plot proxies in one call.
		if ([arg isKindOfClass:[NSArray class]])
		{
			for (id a in arg) {
				[self add:a];
			}
			return;
		}
		
		// Make sure that we are getting a plot proxy object
		if (![arg isKindOfClass:[AkylasChartsPlotProxy class]] && ![arg isKindOfClass:[AkylasChartsPieSegmentProxy class]]) {
			[self throwException:@"Plot type is invalid" subreason:nil location:CODELOCATION];
		}
		
		// Only add if not already it the list
//		AkylasChartsPlotProxy *plot = (AkylasChartsPlotProxy*)arg;
		if ([[self plots] indexOfObject:arg] == NSNotFound) {
			[[self plots] addObject:arg];
            
            if ([arg isKindOfClass:[AkylasChartsPlotProxy class]])
                ((AkylasChartsPlotProxy*)arg).chartProxy = self;
            else if ([arg isKindOfClass:[AkylasChartsPieSegmentProxy class]])
                ((AkylasChartsPieSegmentProxy*)arg).chartProxy = self;
		
			// If a view is currently attached to this proxy then tell it to add this new plot
			// to the graph
			if ([self view]) {
				[(AkylasChartsChart*)[self view] addPlot:arg];
			}
            
            // Remember the proxy or else it will get GC'd if created by a logic variable
            [self rememberProxy:arg];
		}
		else {
			NSLog(@"[DEBUG] Attempted to add plot that is already in the plot array");
		}
	}
}

-(void)remove:(id)arg
{
	ENSURE_SINGLE_ARG(arg, TiProxy);
	
	// Make sure that we are getting a plot proxy object
    if (![arg isKindOfClass:[AkylasChartsPlotProxy class]] && ![arg isKindOfClass:[AkylasChartsPieSegmentProxy class]]) {
		[self throwException:@"Plot type is invalid" subreason:nil location:CODELOCATION];
	}
	
//	AkylasChartsPlotProxy *plot = (AkylasChartsPlotProxy*)arg;
    
    // Remove the plot from our list of plot proxy objects
	[plots removeObject:arg];
    
    // Forget the previously remembered proxy
    [self forgetProxy:arg];
	
	// If a view is currently attached to this proxy then tell it to remove the plot
	// from the graph
	if ([self view]) {
		[(AkylasChartsChart*)[self view] removePlot:arg];
	}
}

-(void)viewDidInitialize
{
	[super viewDidInitialize];
    for (id plot in plots) {
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

@end
