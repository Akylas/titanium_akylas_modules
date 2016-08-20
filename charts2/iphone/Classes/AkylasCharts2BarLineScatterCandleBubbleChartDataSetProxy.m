//
//  AkylasCharts2BarLineScatterCandleBubbleChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 30/07/16.
//
//

#import "AkylasCharts2BarLineScatterCandleBubbleChartDataSetProxy.h"

@implementation AkylasCharts2BarLineScatterCandleBubbleChartDataSetProxy

-(BarLineScatterCandleBubbleChartDataSet*)set
{
    if (!_set) {
        _set = [[BarLineScatterCandleBubbleChartDataSet alloc] init];
    }
    return (BarLineScatterCandleBubbleChartDataSet*)_set;
}

-(void)setHighlightColor:(id)value
{
    [[self set] setHighlightColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"highlightColor" notification:NO];
}


-(void)setHighlightLineWidth:(id)value
{
    [[self set] setHighlightLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightLineWidth" notification:NO];
}

-(void)setHighlightLineDashPhase:(id)value
{
    [[self set] setHighlightLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightLineDashPhase" notification:NO];
}

-(void)setHighlightLineDashLengths:(id)value
{
    [[self set] setHighlightLineDashLengths:value];
    [self replaceValue:value forKey:@"highlightLineDashLengths" notification:NO];
}


@end
