//
//  LineChartView.m
//  akylas.charts2
//
//  Created by Martin Guillon on 27/07/16.
//
//

#import "BarLineChartBase.h"
#import "BarLineChartViewBaseProxy.h"
@implementation BarLineChartBase


- (void)prepareForReuse
{
    [[self baseLineChartView] fitScreen];
    [super prepareForReuse];
    
}

-(BarLineChartViewBase*)baseLineChartView {
    return (BarLineChartViewBase*)_chartView;
}

-(BarLineChartViewBase*)getOrCreateLineChartView {
    return (BarLineChartViewBase*)[self getOrCreateChartView];
}


//-(void) notifyDataSetChanged
//{
//    [_chartView notifyDataSetChanged];
//}


-(void)setLeftAxis_:(id)value
{
    [(BarLineChartViewBaseProxy*)[self proxy] setLeftAxis:value];
}

-(void)setRightAxis_:(id)value
{
    [(BarLineChartViewBaseProxy*)[self proxy] setRightAxis:value];
}

-(void)setDrag_:(id)value
{
    [[self getOrCreateLineChartView] setDragEnabled:[TiUtils boolValue:value]];
}

-(void)setScale_:(id)value
{
    [[self getOrCreateLineChartView] setScaleEnabled:[TiUtils boolValue:value]];
}


-(void)setScaleX_:(id)value
{
    [[self getOrCreateLineChartView] setScaleXEnabled:[TiUtils boolValue:value]];
}

-(void)setScaleY_:(id)value
{
    [[self getOrCreateLineChartView] setScaleYEnabled:[TiUtils boolValue:value]];
}

-(void)setDoubleTapToZoom_:(id)value
{
    [[self getOrCreateLineChartView] setDoubleTapToZoomEnabled:[TiUtils boolValue:value]];
}

-(void)setPinchZoom_:(id)value
{
    [[self getOrCreateLineChartView] setPinchZoomEnabled:[TiUtils boolValue:value]];
}

-(void)setAutoScaleMinMax_:(id)value
{
    [[self getOrCreateLineChartView] setAutoScaleMinMaxEnabled:[TiUtils boolValue:value]];
}

-(void)setHighlightPerDrag_:(id)value
{
    [[self getOrCreateLineChartView] setHighlightPerDragEnabled:[TiUtils boolValue:value]];
}

//-(void)setHighlightFullBar_:(id)value
//{
//    [[self getOrCreateLineChartView] setHighlightFullBarEnabled:[TiUtils boolValue:value]];
//}

-(void)setDrawGridBackground_:(id)value
{
    [[self getOrCreateLineChartView] setDrawGridBackgroundEnabled:[TiUtils boolValue:value]];
}

-(void)setDrawBorders_:(id)value
{
    [[self getOrCreateLineChartView] setDrawBordersEnabled:[TiUtils boolValue:value]];
}

-(void)setMinOffset_:(id)value
{
    [[self getOrCreateLineChartView] setMinOffset:[TiUtils floatValue:value]];
}

-(void)setKeepPositionOnRotation_:(id)value
{
    [[self getOrCreateLineChartView] setKeepPositionOnRotation:[TiUtils boolValue:value]];
}

-(void)setGridBackgroundColor_:(id)value
{
    [[self getOrCreateLineChartView] setGridBackgroundColor:[[TiUtils colorValue:value] _color]];
}

-(void)setBorderColor_:(id)value
{
    [[self getOrCreateLineChartView] setBorderColor:[[TiUtils colorValue:value] _color]];
}

-(void)setBorderWidth_:(id)value
{
    [[self getOrCreateLineChartView] setBorderLineWidth:[TiUtils floatValue:value]];
}

-(void)setMaxVisibleValueCount_:(id)value
{
    [[self getOrCreateLineChartView] setMaxVisibleCount:[TiUtils intValue:value]];
}

@end
