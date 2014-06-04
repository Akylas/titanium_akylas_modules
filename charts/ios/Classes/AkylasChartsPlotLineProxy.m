/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotLineProxy.h"
#import "AkylasChartsChart.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"

@implementation AkylasChartsPlotLineProxy

typedef enum AkylasChartsFillDirection {
    CPTFillDirectionBottom,
    CPTFillDirectionTop,
    CPTFillDirectionOrigin
} AkylasChartsFillDirection;

-(void)configurePlot
{
	[super configurePlot];
	
	CPTScatterPlot* plot = (CPTScatterPlot*)[self plot];
	
	// NOTE: We pass in the current plot values as the default so that any existing settings
	// from the theme are retained unless overridden.
	
	plot.dataLineStyle = [AkylasChartsParsers parseLine:self withPrefix:@"line" def:plot.dataLineStyle];
    
    AkylasChartsFillDirection direction = CPTFillDirectionBottom;
    NSString* fillDirectionOption = [TiUtils stringValue:[self valueForUndefinedKey:@"fillDirection"]];
    if (fillDirectionOption != nil) {
        if ([fillDirectionOption isEqualToString:@"top"]) {
            direction = CPTFillDirectionTop;
        }
        else if ([fillDirectionOption isEqualToString:@"origin"]) {
            direction = CPTFillDirectionOrigin;
        }
    }
    NSDecimal areaBaseValue;
    plot.areaFill = [AkylasChartsParsers parseFillColor:[self valueForUndefinedKey:@"fillColor"]
                                           withGradient:[self valueForUndefinedKey:@"fillGradient"]
                                             andOpacity:[self valueForUndefinedKey:@"fillOpacity"]
                                                    def:nil];
    	switch (direction) {
        case CPTFillDirectionBottom:
                areaBaseValue = [[NSDecimalNumber minimumDecimalNumber] decimalValue];
            break;
        case CPTFillDirectionTop:
            areaBaseValue = [[NSDecimalNumber maximumDecimalNumber] decimalValue];
            break;
        case CPTFillDirectionOrigin:
            {
            NSArray* axes = plot.plotArea.axisSet.axes;
            areaBaseValue= ([axes count] > 1)?((CPTXYAxis*)axes[1]).orthogonalCoordinateDecimal:([axes count] > 0)?((CPTXYAxis*)axes[0]).orthogonalCoordinateDecimal:CPTDecimalFromFloat(0);
            }
            break;
        default:
            break;
    }
    plot.areaBaseValue = [AkylasChartsParsers decimalFromFloat:[self valueForUndefinedKey:@"fillBase"] def:areaBaseValue];
    
	// Plot Symbols
	plot.plotSymbol = [AkylasChartsParsers parseSymbol:[self valueForUndefinedKey:@"symbol"] def:plot.plotSymbol];
    highlightSymbol = [[AkylasChartsParsers parseSymbol:[self valueForUndefinedKey:@"symbolHighlight"] def:nil] retain];
    
    // Symbol
    plot.plotSymbolMarginForHitDetection = [TiUtils floatValue:[self valueForUndefinedKey:@"dataClickMargin"] def:plot.plotSymbolMarginForHitDetection];
    
    // Scatter algorithm
    plot.interpolation = [TiUtils intValue:[self valueForUndefinedKey:@"scatterAlgorithm"] def:plot.interpolation];
}

-(id)init
{
	if (self = [super init]) {
        highlightSymbolIndex = NSUIntegerMax;
        
		// these properties should trigger a redisplay
		static NSSet * plotProperties = nil;
		if (plotProperties == nil)
		{
			plotProperties = [[NSSet alloc] initWithObjects:
							  @"lineColor", @"lineWidth", @"lineOpacity",
							  @"fillColor", @"fillGradient", @"fillOpacity", @"fillBase",
							  @"symbol", @"symbolHighlight", @"labels", @"dataClickMargin",
                              @"scatterAlgorithm", @"fillSpacePath", @"fillDirection",
							  nil];
		}
		
		self.propertyChangedProperties = plotProperties;
	}
	
	return self;
}
	
-(void)dealloc
{
    RELEASE_TO_NIL(highlightSymbol)
    [super dealloc];
}

-(void)scatterPlot:(CPTScatterPlot*)plot plotSymbolWasSelectedAtRecordIndex:(NSUInteger)index
{
    double pts[2];
    pts[CPTCoordinateX] = [[self numberForPlot:index forCoordinate:CPTCoordinateX] doubleValue];
    pts[CPTCoordinateY] = [[self numberForPlot:index forCoordinate:CPTCoordinateY] doubleValue];
    CGPoint plotPoint = [self.plot.plotSpace plotAreaViewPointForDoublePrecisionPlotPoint:pts numberOfCoordinates:2];

	[self notifyOfDataClickedEvent:index atPlotPoint:plotPoint];
}

-(NSArray *)numbersForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndexRange:(NSRange)indexRange;
{
    if (fieldEnum == CPTScatterPlotFieldX) {
        return [[self dataX] subarrayWithRange:indexRange];
    }
    else {
        return [[self dataY] subarrayWithRange:indexRange];
    }
}

-(NSNumber*)highlightIndex
{
    return NUMINT(highlightSymbolIndex);
}

-(void)setHighlightIndex:(id)value
{
    ENSURE_UI_THREAD_1_ARG(value);
    
    // Get new value for the highlighted symbol index. Any negative value will turn off the
    // symbol highlighting
    int val = [TiUtils intValue:value def:-1];
    NSUInteger newValue = (val >= 0) ? (NSUInteger)val : NSUIntegerMax;
    NSUInteger oldValue = highlightSymbolIndex;
    if (newValue != oldValue) {
        highlightSymbolIndex = newValue;
        if (oldValue != NSUIntegerMax) {
            [self.plot reloadDataInIndexRange:NSMakeRange(oldValue, 1)];
        }
        if (newValue != NSUIntegerMax) {
            [self.plot reloadDataInIndexRange:NSMakeRange(newValue, 1)];
        }
    }
    [self.plot setNeedsDisplay];
}

-(CPTPlotSymbol *)symbolForScatterPlot:(CPTScatterPlot *)plot recordIndex:(NSUInteger)index
{
	return (index == highlightSymbolIndex) ? highlightSymbol : plot.plotSymbol;
}

@end
