//
//  BarChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "BarChart.h"

@implementation BarChart


-(ChartViewBase*)newChartView {
    
    return [[BarChartView alloc] initWithFrame:CGRectZero];
}

-(BarChartView*)getOrCreateBarChartView {
    return (BarChartView*)[self getOrCreateChartView];
}

-(BarChartView*)barChartView {
    return (BarChartView*)_chartView;
}


//-(void)setDrawHighlightArrow_:(id)value
//{
//    [[self getOrCreateBarChartView] setDrawHighlightArrowEnabled:[TiUtils boolValue:value]];
//}
-(void)setDrawValueAboveBar_:(id)value
{
    [[self getOrCreateBarChartView] setDrawValueAboveBarEnabled:[TiUtils boolValue:value]];
}
-(void)setDrawBarShadow_:(id)value
{
    [[self getOrCreateBarChartView] setDrawBarShadowEnabled:[TiUtils boolValue:value]];
}
@end
