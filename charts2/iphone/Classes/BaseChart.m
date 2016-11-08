/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "BaseChart.h"
#import "ChartXAxisProxy.h"
#import "ChartLegendProxy.h"
#import "ChartDataProxy.h"
#import "ChartDataSetProxy.h"
#import "TiViewProxy.h"
#import "AkylasCharts2BaseChartViewProxy.h"

//#import "akylas_charts2-Swift.h"

@implementation BaseChart
{

    TiPoint* _descriptionPosition;
}

-(UIView*)viewForHitTest
{
    return _chartView;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    if (_descriptionPosition) {
        CGPoint size = [_descriptionPosition pointWithinSize:bounds.size];
//        [[self getOrCreateChartView] setDescriptionTextPositionWithX:size.x y:size.y];
    }
    if (_chartView != nil) {
        [TiUtils setView:_chartView positionRect:bounds];
    }
    [super frameSizeChanged:frame bounds:bounds];
}

-(void)dealloc {
    RELEASE_WITH_DELEGATE(_chartView)
    RELEASE_TO_NIL(_descriptionPosition)
    [super dealloc];
}

-(ChartViewBase*)newChartView {
    
    return nil;
}

-(ChartViewBase*)getOrCreateChartView {
    if (!_chartView) {
        _chartView = [self newChartView];
        _chartView.delegate = self;
        [self addSubview:_chartView];
    }
    return _chartView;
}

-(ChartViewBase*)chartView {
    return _chartView;
}


-(void)configurationSet
{
    [super configurationSet];
    [_chartView.data notifyDataChanged];
    [_chartView notifyDataSetChanged];
}

- (void)prepareForReuse
{
    [_chartView highlightValues:nil];
}

-(void) notifyDataSetChanged
{
}

//-(ChartLegend*)legend {
//    return [[self getOrCreateChartView] legend];
//}
//
//
//-(ChartData*)data {
//    return [[self getOrCreateChartView] data];
//}


-(ChartDataProxy*) dataProxy {
    return [(AkylasCharts2BaseChartViewProxy*)[self proxy] data];
}



-(void)setData_:(id)value
{
    [(AkylasCharts2BaseChartViewProxy*)[self proxy] setData:value];
}

-(void)setLegend_:(id)value
{
    [(AkylasCharts2BaseChartViewProxy*)[self proxy] setLegend:value];
}

-(void)setXAxis_:(id)value
{
    [(AkylasCharts2BaseChartViewProxy*)[self proxy] setXAxis:value];
}


//-(id)data_
//{
//    return [self dataProxy];
//}
-(void)setDrawMarkers_:(id)value
{
    [[self getOrCreateChartView] setDrawMarkers:[TiUtils boolValue:value]];
}
-(void)setHighlightPerTap_:(id)value
{
    [[self getOrCreateChartView] setHighlightPerTapEnabled:[TiUtils boolValue:value]];
}

-(void)setDragDeceleration_:(id)value
{
    [[self getOrCreateChartView] setDragDecelerationEnabled:[TiUtils boolValue:value]];
}

-(void)setDescriptionFont_:(id)value
{
    [[self getOrCreateChartView] setDescriptionFont:[[TiUtils fontValue:value] font]];
}

-(void)setDescriptionColor_:(id)value
{
    [[self getOrCreateChartView] setDescriptionTextColor:[[TiUtils colorValue:value] _color]];
}

-(void)setDescriptionTextAlign_:(id)value
{
    [[self getOrCreateChartView] setDescriptionTextAlign:[TiUtils textAlignmentValue:value]];
}

-(void)setDescriptionPosition_:(id)value
{
    RELEASE_TO_NIL(_descriptionPosition)
    _descriptionPosition = [[TiUtils tiPointValue:value] retain];
}

-(void)setDescription_:(id)value
{
    [[self getOrCreateChartView] setDescriptionText:[TiUtils stringValue:value]];
}

-(void)setNoDataFont_:(id)value
{
    [[self getOrCreateChartView] setNoDataFont:[[TiUtils fontValue:value] font]];
}

-(void)setNoDataColor_:(id)value
{
    [[self getOrCreateChartView] setNoDataTextColor:[[TiUtils colorValue:value] _color]];
}

-(void)setNoData_:(id)value
{
    [[self getOrCreateChartView] setNoDataText:[TiUtils stringValue:value]];
}

//-(void)setNoDataTextDescription_:(id)value
//{
//    [[self getOrCreateChartView] setNoDataTextDescription:[TiUtils stringValue:value]];
//}

-(void)setExtraOffset_:(id)value
{
    UIEdgeInsets inset = [TiUtils insetValue:value];
    [[self getOrCreateChartView] setExtraOffsetsWithLeft:inset.left top:inset.top right:inset.right bottom:inset.bottom];
}


-(void)setMarker:(id)value
{
    ENSURE_DICT(value)
    NSString* type = [TiUtils stringValue:@"type" properties:value def:@"balloon"];
    //ChartMarker* marker = BalloonMarker
    UIEdgeInsets inset = [TiUtils insetValue:value];
    [[self getOrCreateChartView] setExtraOffsetsWithLeft:inset.left top:inset.top right:inset.right bottom:inset.bottom];
}

- (void)chartValueSelected:(ChartViewBase * _Nonnull)chartView entry:(ChartDataEntry * _Nonnull)entry dataSetIndex:(NSInteger)dataSetIndex highlight:(ChartHighlight * _Nonnull)highlight {
    BOOL hasHighlight = [proxy _hasListeners:@"highlight"];
    BOOL hasClick = [proxy _hasListeners:@"click"];
    if (hasHighlight || hasClick)
    {
        ChartDataSetProxy* dataSetProxy = [[self dataProxy] dataSetAtIndex:dataSetIndex];
        NSDictionary* event = @{
                                @"data":[dataSetProxy chartDataEntryDict:entry],
                               @"dataSetIndex":@(dataSetIndex)
                               };
        if (hasHighlight) {
            [[self viewProxy] fireEvent:@"highlight" withObject:event propagate:NO checkForListener:NO];
        }
        if (hasClick) {
            [[self viewProxy] fireEvent:@"click" withObject:event propagate:YES checkForListener:NO];
        }
    }
}
- (void)chartValueNothingSelected:(ChartViewBase * _Nonnull)chartView {
    [[self viewProxy] fireEvent:@"click" withObject:nil propagate:YES checkForListener:YES];
}
- (void)chartScaled:(ChartViewBase * _Nonnull)chartView scaleX:(CGFloat)scaleX scaleY:(CGFloat)scaleY {
    if ([[self viewProxy] _hasListeners:@"scale"]) {
        [[self viewProxy] fireEvent:@"scale" withObject:@{
                                                         @"scaleX":@(scaleX),
                                                         @"scaleY":@(scaleY),
                                                         } propagate:NO checkForListener:NO];
    }
    
}
- (void)chartTranslated:(ChartViewBase * _Nonnull)chartView dX:(CGFloat)dX dY:(CGFloat)dY {
    if ([[self viewProxy] _hasListeners:@"translate"]) {
        [[self viewProxy] fireEvent:@"translate" withObject:@{
                                                         @"dX":@(dX),
                                                         @"dY":@(dY),
                                                         } propagate:NO checkForListener:NO];
    }
}
@end
