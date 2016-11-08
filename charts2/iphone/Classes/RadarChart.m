//
//  RadarChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "RadarChart.h"
#import "AkylasCharts2RadarChartProxy.h"

@implementation RadarChart




-(ChartViewBase*)newChartView {
    
    return [[RadarChartView alloc] initWithFrame:CGRectZero];
}

-(RadarChartView*)getOrCreateRadarChartView {
    return (RadarChartView*)[self getOrCreateChartView];
}

-(RadarChartView*)radarChartView {
    return (RadarChartView*)_chartView;
}

-(void)setYAxis_:(id)value
{
    [(AkylasCharts2RadarChartProxy*)[self proxy] setYAxis:value];
}

-(void)setWebLineWidth_:(id)value
{
    [[self getOrCreateRadarChartView] setWebLineWidth:[TiUtils floatValue:value]];
}

-(void)setInnerWebLineWidth_:(id)value
{
    [[self getOrCreateRadarChartView] setInnerWebLineWidth:[TiUtils floatValue:value]];
}

-(void)setWebColor_:(id)value
{
    [[self getOrCreateRadarChartView] setWebColor:[[TiUtils colorValue:value] _color]];
}

-(void)setInnerWebColor_:(id)value
{
    [[self getOrCreateRadarChartView] setInnerWebColor:[[TiUtils colorValue:value] _color]];
}

-(void)setWebAlpha_:(id)value
{
    [[self getOrCreateRadarChartView] setWebAlpha:[TiUtils floatValue:value]];
}

-(void)setDrawWeb_:(id)value
{
    [[self getOrCreateRadarChartView] setDrawWeb:[TiUtils boolValue:value]];
}

-(void)setSkipWebLineCount_:(id)value
{
    [[self getOrCreateRadarChartView] setSkipWebLineCount:[TiUtils floatValue:value]];
}
@end
