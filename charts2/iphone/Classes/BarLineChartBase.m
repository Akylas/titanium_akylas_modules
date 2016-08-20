//
//  LineChartView.m
//  akylas.charts2
//
//  Created by Martin Guillon on 27/07/16.
//
//

#import "BarLineChartBase.h"
#import "ChartYAxisProxy.h"

@implementation BarLineChartBase {
    ChartYAxisProxy* _leftAxisProxy;
    ChartYAxisProxy* _rightAxisProxy;
}

-(void)dealloc
{
    
    if (_leftAxisProxy) {
        _leftAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_leftAxisProxy)
    }
    if (_rightAxisProxy) {
        _rightAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_rightAxisProxy)
    }
    [super dealloc];
}

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

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy
{
    [super unarchivedWithRootProxy:rootProxy];
    [_leftAxisProxy unarchivedWithRootProxy:rootProxy];
    [_rightAxisProxy unarchivedWithRootProxy:rootProxy];
    //    [_xAxisProxy unarchivedWithRootProxy:rootProxy];
}

-(void) notifyDataSetChanged
{
    [_chartView notifyDataSetChanged];
}

-(ChartYAxis*)rightAxis {
    return [[self getOrCreateLineChartView] rightAxis];
}


-(ChartYAxis*)leftAxis {
    return [[self getOrCreateLineChartView] leftAxis];
}


-(ChartYAxisProxy*)leftAxisProxy {
    if (!_leftAxisProxy) {
        _leftAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[[self proxy] getContext] args:nil axis:[self  leftAxis]];
        _leftAxisProxy.parentChartViewProxy = (AkylasCharts2BaseChartViewProxy*)proxy;
    }
    return _leftAxisProxy;
}

-(ChartYAxisProxy*)rightAxisProxy {
    if (!_rightAxisProxy) {
        _rightAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[[self proxy] getContext] args:nil axis:[self rightAxis]];
        _rightAxisProxy.parentChartViewProxy = (AkylasCharts2BaseChartViewProxy*)proxy;
    }
    return _rightAxisProxy;
}

-(void)setLeftAxis_:(id)value
{
    ENSURE_DICT(value)
    [[self proxy] applyProperties:value onBindedProxy:[self leftAxisProxy]];
}

-(id)leftAxis_
{
    return [self leftAxisProxy];
}

-(void)setRightAxis_:(id)value
{
    ENSURE_DICT(value)
    [[self proxy] applyProperties:value onBindedProxy:[self rightAxisProxy]];
}

-(id)rightAxis_
{
    return [self rightAxisProxy];
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

-(void)setHighlightFullBar_:(id)value
{
    [[self getOrCreateLineChartView] setHighlightFullBarEnabled:[TiUtils boolValue:value]];
}

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
    [[self getOrCreateLineChartView] setMaxVisibleValueCount:[TiUtils intValue:value]];
}

@end
