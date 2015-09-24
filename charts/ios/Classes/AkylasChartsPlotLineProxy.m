/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotLineProxy.h"
#import "AkylasChartsChart.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"


typedef enum AkylasChartsFillDirection {
    CPTFillDirectionBottom,
    CPTFillDirectionTop,
    CPTFillDirectionOrigin
} AkylasChartsFillDirection;

@implementation AkylasChartsPlotLineProxy
{
    BOOL _needsParseLine;
    BOOL _needsParseFill;
    BOOL _needsParseSymbols;
    BOOL _needsParseDirection;
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    _needsParseLine = YES;
    _needsParseFill = YES;
    _needsParseSymbols = YES;
    _needsParseDirection = YES;
	[super _initWithProperties:properties];
}


-(void)configurePlot:(NSDictionary*)props
{
	[super configurePlot:props];
	
	CPTScatterPlot* plot = (CPTScatterPlot*)[self plot];
    if (plot ==nil) return;
	
	// NOTE: We pass in the current plot values as the default so that any existing settings
	// from the theme are retained unless overridden.
	
    if (_needsParseLine) {
        _needsParseLine = NO;
        plot.dataLineStyle = [AkylasChartsParsers parseLine:props withPrefix:@"line" def:plot.dataLineStyle];
    }
    

    if (_needsParseFill) {
        _needsParseFill = NO;
        
        plot.areaFill = [AkylasChartsParsers parseFillColor:[props objectForKey:@"fillColor"]
                                               withGradient:[props objectForKey:@"fillGradient"]
                                                 andOpacity:[props objectForKey:@"fillOpacity"]
                                                        def:nil];
        AkylasChartsFillDirection direction = CPTFillDirectionBottom;
        NSString* fillDirectionOption = [TiUtils stringValue:[props objectForKey:@"fillDirection"]];
        if (fillDirectionOption != nil) {
            if ([fillDirectionOption isEqualToString:@"top"]) {
                direction = CPTFillDirectionTop;
            }
            else if ([fillDirectionOption isEqualToString:@"origin"]) {
                direction = CPTFillDirectionOrigin;
            }
        }
        
        NSNumber* areaBaseValue;
        switch (direction) {
            case CPTFillDirectionBottom:
                    areaBaseValue = [NSDecimalNumber minimumDecimalNumber];
                break;
            case CPTFillDirectionTop:
                areaBaseValue = [NSDecimalNumber maximumDecimalNumber];
                break;
            case CPTFillDirectionOrigin:
                {
                NSArray* axes = plot.plotArea.axisSet.axes;
                areaBaseValue= ([axes count] > 1)?((CPTXYAxis*)axes[1]).orthogonalPosition:([axes count] > 0)?((CPTXYAxis*)axes[0]).orthogonalPosition:@(0);
                }
                break;
            default:
                break;
        }
        plot.areaBaseValue = [AkylasChartsParsers decimalFromFloat:[props objectForKey:@"fillBase"] def:areaBaseValue];
    }
    
    if (_needsParseSymbols) {
        _needsParseSymbols = NO;
        // Plot Symbols
        plot.plotSymbol = [AkylasChartsParsers parseSymbol:[props objectForKey:@"symbol"] def:plot.plotSymbol];
        highlightSymbol = [[AkylasChartsParsers parseSymbol:[props objectForKey:@"symbolHighlight"] def:nil] retain];
        // Symbol
        plot.plotSymbolMarginForHitDetection = [TiUtils floatValue:[props objectForKey:@"dataClickMargin"] def:plot.plotSymbolMarginForHitDetection];
    }
    
   
    
    // Scatter algorithm
    plot.interpolation = [TiUtils intValue:[props objectForKey:@"scatterAlgorithm"] def:plot.interpolation];
}

-(id)init
{
	if (self = [super init]) {
        highlightSymbolIndex = NSUIntegerMax;
        
		// these properties should trigger a redisplay
//		static NSSet * plotProperties = nil;
//		if (plotProperties == nil)
//		{
//			plotProperties = [[NSSet alloc] initWithObjects:
//							  @"lineColor", @"lineWidth", @"lineOpacity",
//							  @"fillColor", @"fillGradient", @"fillOpacity", @"fillBase",
//							  @"symbol", @"symbolHighlight", @"labels", @"dataClickMargin",
//                              @"scatterAlgorithm", @"fillSpacePath", @"fillDirection",
//							  nil];
//		}
//		
//		self.propertyChangedProperties = plotProperties;
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
    return NUMUINTEGER(highlightSymbolIndex);
}

-(void)setHighlightIndex:(id)value
{
    ENSURE_UI_THREAD_1_ARG(value);
    
    // Get new value for the highlighted symbol index. Any negative value will turn off the
    // symbol highlighting
    NSInteger val = [TiUtils intValue:value def:-1];
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

-(void)setValue:(id)value forKey:(NSString *)key
{
    if ([key hasPrefix:@"line"])
    {
        _needsParseLine = YES;
    } else if ([key hasPrefix:@"fill"])
    {
        _needsParseFill = YES;
    }
    [super setValue:value forKey:key];
}

@end
