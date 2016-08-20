//
//  CombinedChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "CombinedChart.h"
#import "AkylasCharts2CombinedChartDataProxy.h"

@implementation CombinedChart


-(ChartViewBase*)newChartView {
    
    return [[CombinedChartView alloc] initWithFrame:CGRectZero];
}

-(CombinedChartView*)getOrCreateCombinedChartView {
    return (CombinedChartView*)[self getOrCreateChartView];
}

-(CombinedChartView*)candleStickChartView {
    return (CombinedChartView*)_chartView;
}

-(Class)dataClass {
    return [AkylasCharts2CombinedChartDataProxy class];
}

-(void)setDrawHighlightArrow_:(id)value
{
    [[self getOrCreateCombinedChartView] setDrawHighlightArrowEnabled:[TiUtils boolValue:value]];
}
-(void)setDrawValueAboveBar_:(id)value
{
    [[self getOrCreateCombinedChartView] setDrawValueAboveBarEnabled:[TiUtils boolValue:value]];
}
-(void)setDrawBarShadow_:(id)value
{
    [[self getOrCreateCombinedChartView] setDrawBarShadowEnabled:[TiUtils boolValue:value]];
}
-(void)setDrawOrder_:(id)value
{
    [[self getOrCreateCombinedChartView] setDrawOrder:value];
}
@end
