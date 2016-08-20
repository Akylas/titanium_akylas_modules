//
//  AkylasCharts2RadarChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2RadarChartDataProxy.h"
#import "AkylasCharts2RadarChartDataSetProxy.h"

@implementation AkylasCharts2RadarChartDataProxy

-(RadarChartData*)data {
    if (!_data) {
        _data = [[RadarChartData alloc] init];
    }
    return (RadarChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2RadarChartDataSetProxy class];
}

-(void)setHighlightLineWidth:(id)value
{
    [[self data] setHighlightLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightLineWidth" notification:NO];
}

-(void)setHighlightLineDashPhase:(id)value
{
    [[self data] setHighlightLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightLineDashPhase" notification:NO];
}

-(void)setHighlightLineDashLengths:(id)value
{
    [[self data] setHighlightLineDashLengths:value];
    [self replaceValue:value forKey:@"highlightLineDashLengths" notification:NO];
}

@end
