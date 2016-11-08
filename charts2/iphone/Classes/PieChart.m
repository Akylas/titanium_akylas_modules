//
//  PieChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "PieChart.h"
#import "TiUtils.h"

@implementation PieChart


-(ChartViewBase*)newChartView {
    
    return [[PieChartView alloc] initWithFrame:CGRectZero];
}

-(PieChartView*)getOrCreatePieChartView {
    return (PieChartView*)[self getOrCreateChartView];
}

-(PieChartView*)pieChartView {
    return (PieChartView*)_chartView;
}


-(void)setHoleColor_:(id)value
{
    [[self getOrCreatePieChartView] setHoleColor:[[TiUtils colorValue:value] _color]];
}

-(void)setDrawSlicesUnderHole_:(id)value
{
    [[self getOrCreatePieChartView] setDrawSlicesUnderHoleEnabled:[TiUtils boolValue:value]];
}

-(void)setDrawHole_:(id)value
{
    [[self getOrCreatePieChartView] setDrawHoleEnabled:[TiUtils boolValue:value]];
}

-(void)setCenterText_:(id)value
{
    [[self getOrCreatePieChartView] setCenterText:[TiUtils stringValue:value]];
}

-(void)setDrawCenterText_:(id)value
{
    [[self getOrCreatePieChartView] setDrawCenterTextEnabled:[TiUtils boolValue:value]];
}

-(void)setHoleRadiusPercent_:(id)value
{
    [[self getOrCreatePieChartView] setHoleRadiusPercent:[TiUtils floatValue:value]];
}



-(void)setTransparentCircleColor_:(id)value
{
    [[self getOrCreatePieChartView] setTransparentCircleColor:[[TiUtils colorValue:value] _color]];
}

-(void)setTransparentCircleRadiusPercent_:(id)value
{
    [[self getOrCreatePieChartView] setTransparentCircleRadiusPercent:[TiUtils floatValue:value]];
}

-(void)setDrawSliceText_:(id)value
{
    [[self getOrCreatePieChartView] setDrawSliceTextEnabled:[TiUtils boolValue:value]];
}

-(void)setDrawEntryLabels_:(id)value
{
    [[self getOrCreatePieChartView] setDrawSliceTextEnabled:[TiUtils boolValue:value]];
}

-(void)setUsePercentValues_:(id)value
{
    [[self getOrCreatePieChartView] setUsePercentValuesEnabled:[TiUtils boolValue:value]];
}
-(void)setCenterTextRadiusPercent_:(id)value
{
    [[self getOrCreatePieChartView] setCenterTextRadiusPercent:[TiUtils floatValue:value]];
}
-(void)setMaxAngle_:(id)value
{
    [[self getOrCreatePieChartView] setMaxAngle:[TiUtils floatValue:value]];
}
@end
