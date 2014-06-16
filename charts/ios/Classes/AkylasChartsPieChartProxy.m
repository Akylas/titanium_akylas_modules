/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPieChartProxy.h"
#import "AkylasChartsPieSegmentProxy.h"

@implementation AkylasChartsPieChartProxy

-(NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot
{
    return [[self plots] count];
}

-(NSNumber *)numberForPlot:(CPTPlot *)plot field:(NSUInteger)fieldEnum recordIndex:(NSUInteger)idx
{
    if (fieldEnum == CPTPieChartFieldSliceWidth) {
        AkylasChartsPieSegmentProxy* segment = [[self plots] objectAtIndex:idx];
        if (segment)
            return [NSDecimalNumber decimalNumberWithDecimal:[segment.value decimalValue]];
    }
    return [NSDecimalNumber zero];
}

-(CPTFill *)sliceFillForPieChart:(CPTPieChart *)pieChart recordIndex:(NSUInteger)idx
{
    AkylasChartsPieSegmentProxy* segment = [[self plots] objectAtIndex:idx];
    if (segment)
        return segment.fill;
    return nil;
}


-(CPTLineStyle *)sliceBorderForPieChart:(CPTPieChart *)pieChart recordIndex:(NSUInteger)idx
{
    AkylasChartsPieSegmentProxy* segment = [[self plots] objectAtIndex:idx];
    if (segment)
        return segment.border;
    return nil;
}

-(CPTLayer *)dataLabelForPlot:(CPTPlot *)plot recordIndex:(NSUInteger)idx
{
    AkylasChartsPieSegmentProxy* segment = [[self plots] objectAtIndex:idx];
    if (segment)
        return [[CPTTextLayer alloc] initWithText:segment.title style:segment.labelStyle];
    return nil;
}

-(CGFloat)radialOffsetForPieChart:(CPTPieChart *)pieChart recordIndex:(NSUInteger)index
{

    AkylasChartsPieSegmentProxy* segment = [[self plots] objectAtIndex:index];
    if (segment)
        return segment.explodeOffset;
	
	return 0.0f;
}

//
//-(void)pieChart:(CPTPieChart *)plot sliceWasSelectedAtRecordIndex:(NSUInteger)idx
//{
//    
//}
//
//-(void)pieChart:(CPTPieChart *)plot sliceWasSelectedAtRecordIndex:(NSUInteger)idx withEvent:(CPTNativeEvent *)event
//{
//    
//}
@end
