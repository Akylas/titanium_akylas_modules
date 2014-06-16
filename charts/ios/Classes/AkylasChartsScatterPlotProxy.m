/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsScatterPlotProxy.h"
#import "AkylasChartsParsers.h"
#import "AkylasChartsChartProxy.h"
#import "AkylasChartsScatterPlot.h"
#import "AkylasChartsChart.h"
#import "TiUtils.h"

// For now, this is just a base class for line and bar plots, since they both
// are really types of scatter plots. There are subtle differences between
// line and bar plots so they exist in different classes for now.

@implementation AkylasChartsScatterPlotProxy
{
    BOOL _needsParseLabels;
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    _needsParseLabels = YES;
	[super _initWithProperties:properties];
}

-(CPTPlot*)allocPlot
{
	return [[AkylasChartsScatterPlot alloc] init];
}

-(void)configurePlot:(NSDictionary*)props
{
    [super configurePlot:props];
    
	CPTPlot* plot = (CPTPlot*)[self plot];
    if (plot ==nil) return;
 
    // identifier is copied, so no need to retain
	plot.identifier = [TiUtils stringValue:[props objectForKey:@"name"]];
    plot.shadow = [AkylasChartsParsers parseShadow:@"shadow" inProperties:self def:nil];

	// Parse the labels
    if (_needsParseLabels) {
        _needsParseLabels = NO;
        [AkylasChartsParsers parseLabelStyle:[props objectForKey:@"labels"] forPlot:plot def:nil];
    }
}

-(NSDictionary*)plotSpace
{
    CPTXYPlotSpace* ps = (CPTXYPlotSpace*)self.plot.plotSpace;
    
    NSDictionary* xRange = [NSDictionary dictionaryWithObjectsAndKeys:
                            NUMDOUBLE(ps.xRange.locationDouble), @"min",
                            NUMDOUBLE(ps.xRange.locationDouble +ps.xRange.lengthDouble), @"max",
                            NUMDOUBLE(ps.xRange.minLimitDouble), @"minLimit",
                            NUMDOUBLE(ps.xRange.maxLimitDouble), @"maxLimit", 
                            nil];
    NSDictionary* yRange = [NSDictionary dictionaryWithObjectsAndKeys:
                            NUMDOUBLE(ps.yRange.locationDouble), @"min",
                            NUMDOUBLE(ps.yRange.locationDouble + ps.yRange.lengthDouble), @"max",
                            NUMDOUBLE(ps.yRange.minLimitDouble), @"minLimit",
                            NUMDOUBLE(ps.yRange.maxLimitDouble), @"maxLimit", 
                            nil];
    
    NSDictionary* result = [NSDictionary dictionaryWithObjectsAndKeys:
                            xRange, @"xRange",
                            yRange, @"yRange",
                            nil];
    
    return result;
}

-(NSNumber*)indexFromViewPoint:(id)args
{
	ENSURE_SINGLE_ARG(args,NSDictionary);
	CGPoint point;
	point.x = (float)[TiUtils floatValue:[args objectForKey:@"x"] def:0];
	point.y = (float)[TiUtils floatValue:[args objectForKey:@"y"] def:0];

    // The point passed in is in view coordinates. We need to convert to graph coordinates
    // and specifically to the plot area. Remember that graph cooridates are inverted from
    // view coordinates.
    CPTGraphHostingView *hostingView = [(AkylasChartsChart*)self.chartProxy.view hostingView];
    CGPoint pointInPlotArea = [hostingView.layer convertPoint:point toLayer:self.plot.plotArea];

    NSDecimal newPoint[2];
    [self.plot.graph.defaultPlotSpace plotPoint:newPoint numberOfCoordinates:2 forPlotAreaViewPoint:pointInPlotArea];
    NSDecimalRound(&newPoint[0], &newPoint[0], 0, NSRoundPlain);

    int x = [[NSDecimalNumber decimalNumberWithDecimal:newPoint[0]] intValue];
    int count = (int)[self numberOfRecordsForPlot:[self plot]];
    if (x >= count) {
        x = count - 1;
    }
    if (x < 0) {
        x = 0;
    }

    return NUMINT(x);
}

-(NSDictionary*)dataPointFromIndex:(id)args
{
    ENSURE_ARG_COUNT(args,1)
    int index = [TiUtils intValue:[args objectAtIndex:0] def:0];

    double pts[2];
    pts[CPTCoordinateX] = [[self numberForPlot:index forCoordinate:CPTCoordinateX] doubleValue];
    pts[CPTCoordinateY] = [[self numberForPlot:index forCoordinate:CPTCoordinateY] doubleValue];
    CGPoint plotPoint = [self.plot.plotSpace plotAreaViewPointForDoublePrecisionPlotPoint:pts numberOfCoordinates:2];
    CGPoint graphPoint = [self.plot.plotArea convertPoint:plotPoint toLayer:self.plot.graph];
    CGPoint viewPoint = [self viewPointFromGraphPoint:graphPoint];

    NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys:
                              NUMINT(index),@"index",
                              [self numberForPlot:index],@"value",
                              self.plot.identifier,@"name",
                              NUMINT(viewPoint.x),@"x",
                              NUMINT(viewPoint.y),@"y",
                              nil
                           ];

    return result;
}

@end
