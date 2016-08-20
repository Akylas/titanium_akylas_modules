//
//  RadarChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "RadarChart.h"
#import "AkylasCharts2RadarChartDataProxy.h"
#import "ChartYAxisProxy.h"

@implementation RadarChart
{
    ChartYAxisProxy* _yAxisProxy;
}

-(void)dealloc
{
    if (_yAxisProxy) {
        _yAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_yAxisProxy)
    }
    [super dealloc];
}

-(ChartViewBase*)newChartView {
    
    return [[RadarChartView alloc] initWithFrame:CGRectZero];
}

-(RadarChartView*)getOrCreateRadarChartView {
    return (RadarChartView*)[self getOrCreateChartView];
}

-(RadarChartView*)radarChartView {
    return (RadarChartView*)_chartView;
}

-(Class)dataClass {
    return [AkylasCharts2RadarChartDataProxy class];
}

-(ChartYAxis*)yAxis {
    return [[self getOrCreateRadarChartView] yAxis];
}

-(ChartYAxisProxy*)yAxisProxy {
    if (!_yAxisProxy) {
        _yAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[[self proxy] getContext] args:nil axis:[self yAxis]];
        _yAxisProxy.parentChartViewProxy = (AkylasCharts2BaseChartViewProxy*)proxy;
    }
    return _yAxisProxy;
}

-(void)setYAxis_:(id)value
{
    ENSURE_DICT(value)
    [[self proxy] applyProperties:value onBindedProxy:[self yAxisProxy]];
}

-(id)yAxis_
{
    return [self yAxisProxy];
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
