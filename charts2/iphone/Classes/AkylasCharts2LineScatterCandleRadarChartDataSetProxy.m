//
//  AkylasCharts2LineScatterCandleRadarChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 30/07/16.
//
//

#import "AkylasCharts2LineScatterCandleRadarChartDataSetProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"

@implementation AkylasCharts2LineScatterCandleRadarChartDataSetProxy

-(LineScatterCandleRadarChartDataSet*)set
{
    if (!_set) {
        _set = [[LineScatterCandleRadarChartDataSet alloc] init];
    }
    return (LineScatterCandleRadarChartDataSet*)_set;
}


-(void)setDrawHorizontalHighlightIndicator:(id)value
{
    [[self set] setDrawHorizontalHighlightIndicatorEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawHorizontalHighlightIndicator" notification:NO];
}
-(void)setDrawVerticalHighlightIndicator:(id)value
{
    [[self set] setDrawVerticalHighlightIndicatorEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawVerticalHighlightIndicator" notification:NO];
}
-(void)setDrawHighlightIndicators:(id)value
{
    [[self set] setDrawHighlightIndicators:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawHighlightIndicators" notification:NO];
}
@end
