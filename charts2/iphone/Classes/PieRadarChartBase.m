//
//  PieRadarChartBase.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "PieRadarChartBase.h"

@implementation PieRadarChartBase


- (void)prepareForReuse
{
//    [[self pieRadarChartView] fitScreen];
    [super prepareForReuse];
    
}

-(PieRadarChartViewBase*)pieRadarChartView {
    return (PieRadarChartViewBase*)_chartView;
}

-(PieRadarChartViewBase*)getOrCreatePieRadarChartView {
    return (PieRadarChartViewBase*)[self getOrCreateChartView];
}

-(void)setRotation_:(id)value
{
    [[self getOrCreatePieRadarChartView] setRotationEnabled:[TiUtils boolValue:value]];
}

-(void)setMinOffset_:(id)value
{
    [[self getOrCreatePieRadarChartView] setMinOffset:[TiUtils floatValue:value]];
}
-(void)setRotationAngle_:(id)value
{
    [[self getOrCreatePieRadarChartView] setRotationAngle:[TiUtils floatValue:value]];
}
-(void)setRotationWithTwoFingers_:(id)value
{
    [[self getOrCreatePieRadarChartView] setRotationWithTwoFingers:[TiUtils boolValue:value]];
}
@end
