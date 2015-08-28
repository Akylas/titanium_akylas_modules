/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotBarProxy.h"
#import "AkylasChartsChart.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"

@implementation AkylasChartsPlotBarProxy

-(CPTPlot*)allocPlot
{
	return [[CPTBarPlot alloc] init];
}

-(void)configurePlot:(NSDictionary*)props
{
	[super configurePlot:props];
	
	CPTBarPlot* plot = (CPTBarPlot*)[self plot];
	
	// NOTE: We pass in the current plot values as the default so that any existing settings
	// from the theme are retained unless overridden.
	
	plot.lineStyle = [AkylasChartsParsers parseLine:props withPrefix:@"line" def:plot.lineStyle];
	
	plot.fill = [AkylasChartsParsers parseFillColor:[props objectForKey:@"fillColor"]
								   withGradient:[props objectForKey:@"fillGradient"]
									 andOpacity:[props objectForKey:@"fillOpacity"]
											def:plot.fill];
	
	plot.barsAreHorizontal = [TiUtils boolValue:[props objectForKey:@"barDirection"] def:[TiUtils boolValue:[self.parent valueForUndefinedKey:@"barDirection"] def:plot.barsAreHorizontal]];
	plot.barWidth = [AkylasChartsParsers decimalFromFloat:[props objectForKey:@"barWidth"] def:[AkylasChartsParsers decimalFromFloat:[self.parent valueForUndefinedKey:@"barWidth"] def:plot.barWidth]];
	plot.barOffset = [AkylasChartsParsers decimalFromFloat:[props objectForKey:@"barOffset"] def:[AkylasChartsParsers decimalFromFloat:[self.parent valueForUndefinedKey:@"barOffset"] def:plot.barOffset]];
	plot.barCornerRadius = [TiUtils floatValue:[props objectForKey:@"barRadius"] def:[TiUtils floatValue:[self.parent valueForUndefinedKey:@"barRadius"] def:plot.cornerRadius]];

	// These fields allow for changing the base of the bar -- could be used for stacked bar charts
	// TODO
	//plot.baseValue = NSDecimalFromFloat(0.0f);
	//plot.barBasesVary = NO;
}

-(id)init
{
	if (self = [super init]) {
		// these properties should trigger a redisplay
//		static NSSet * plotProperties = nil;
//		if (plotProperties==nil)
//		{
//			plotProperties = [[NSSet alloc] initWithObjects:
//							  @"lineColor", @"lineWidth", @"lineOpacity",
//							  @"fillColor", @"fillGradient", @"fillOpacity",
//							  @"barDirection", @"barWidth", @"barOffset", @"barCornerRadius",
//							  @"labels",
//							  nil];
//		}
//		
//		self.propertyChangedProperties = plotProperties;
	}
	
	return self;
}

-(void)dealloc
{
	[super dealloc];
}

-(void)barPlot:(CPTBarPlot*)plot barWasSelectedAtRecordIndex:(NSUInteger)index
{
    double pts[2];
    pts[CPTCoordinateX] = [[self numberForPlot:index forCoordinate:CPTCoordinateX] doubleValue];
    pts[CPTCoordinateY] = [[self numberForPlot:index forCoordinate:CPTCoordinateY] doubleValue];
    CGPoint plotPoint = [self.plot.plotSpace plotAreaViewPointForDoublePrecisionPlotPoint:pts numberOfCoordinates:2];
    
	[self notifyOfDataClickedEvent:index atPlotPoint:plotPoint];
}

-(NSArray *)numbersForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndexRange:(NSRange)indexRange
{
    NSArray *num = nil;
    
	if (fieldEnum == CPTBarPlotFieldBarLocation) {
		// This field identifies the column (for horizontal bar charts) for the value that
		// is being indexed by 'index'. By returning the actual data value you will generate
		// a frequency chart (or something like that). For now, we are just generating a normal
		// bar chart, so return the same index that is passed in.
		num = [self numbersForPlotRange:indexRange forCoordinate:CPTCoordinateX];
	} else if (fieldEnum == CPTBarPlotFieldBarTip) {
		num = [self numbersForPlotRange:indexRange forCoordinate:CPTCoordinateY];
	} else if (fieldEnum == CPTBarPlotFieldBarBase) {
		// TODO -- This is where we could do a stacked bar ???
		num = [NSArray arrayWithObject:[NSNumber numberWithFloat:0.0f]];
	}
	
	return num;
}

-(NSNumber*)numberForPlot:(CPTPlot*)plot field:(NSUInteger)fieldEnum recordIndex:(NSUInteger)index
{
	NSNumber *num = nil;

	if (fieldEnum == CPTBarPlotFieldBarLocation) {
		// This field identifies the column (for horizontal bar charts) for the value that
		// is being indexed by 'index'. By returning the actual data value you will generate
		// a frequency chart (or something like that). For now, we are just generating a normal
		// bar chart, so return the same index that is passed in.
		num = [self numberForPlot:index forCoordinate:CPTCoordinateX];
	} else if (fieldEnum == CPTBarPlotFieldBarTip) {
		num = [self numberForPlot:index forCoordinate:CPTCoordinateY];
	} else if (fieldEnum == CPTBarPlotFieldBarBase) {
		// TODO -- This is where we could do a stacked bar ???
		num = [NSNumber numberWithFloat:0.0];
	}
	
	return num;
}

//-(NSArray *)numbersForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndexRange:(NSRange)indexRange;
//{
//    if (fieldEnum == CPTBarPlotFieldBarLocation) {
//		// This field identifies the column (for horizontal bar charts) for the value that
//		// is being indexed by 'index'. By returning the actual data value you will generate
//		// a frequency chart (or something like that). For now, we are just generating a normal
//		// bar chart, so return the same index that is passed in.
//		num = [self numberForPlot:index forCoordinate:CPTCoordinateX];
//	} else if (fieldEnum == CPTBarPlotFieldBarTip) {
//		num = [self numberForPlot:index forCoordinate:CPTCoordinateY];
//	} else if (fieldEnum == CPTBarPlotFieldBarBase) {
//		// TODO -- This is where we could do a stacked bar ???
//		num = [NSNumber numberWithFloat:0.0];
//	}
//    if (fieldEnum == CPTScatterPlotFieldX) {
//        return [[self dataX] subarrayWithRange:indexRange];
//    }
//    else {
//        return [[self dataY] subarrayWithRange:indexRange];
//    }
//}

@end
