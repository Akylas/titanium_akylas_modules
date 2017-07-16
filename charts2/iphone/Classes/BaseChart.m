/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "BaseChart.h"
#import "BalloonMarker.h"
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

-(void)setTouchEnabled_:(id)arg
{
    [super setTouchEnabled_:arg];
    _chartView.userInteractionEnabled = self.userInteractionEnabled;
}

-(BOOL)touchedContentViewWithEvent:(UIEvent *)event
{
    // The view hierarchy of the movie player controller's view is subject to change,
    // and traversing it is dangerous. If we received a touch which isn't on a TiUIView,
    // assume it falls into the movie player view hiearchy; this matches previous
    // behavior as well.
    
    UITouch* touch = [[event allTouches] anyObject];
    UIView* view = [touch view];
    return (view = self || view  == _chartView);
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
        [_chartView.gestureRecognizers enumerateObjectsUsingBlock:^(__kindof UIGestureRecognizer * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            obj.cancelsTouchesInView = NO;
        }];
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



//-(void)setData_:(id)value
//{
//    [(AkylasCharts2BaseChartViewProxy*)[self proxy] setData:value];
//}

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


-(void)setMarker_ :(id)value
{
    ENSURE_DICT(value)
//    NSString* type = [TiUtils stringValue:@"type" properties:value def:@"balloon"];
    BalloonMarker *marker = [[BalloonMarker alloc]
                             initWithColor: [UIColor colorWithWhite:180/255. alpha:1.0]
                             font: [UIFont systemFontOfSize:12.0]
                             textColor: UIColor.whiteColor
                             insets: UIEdgeInsetsMake(8.0, 8.0, 20.0, 8.0)];
    [[self getOrCreateChartView] setMarker:[marker autorelease]];
}

//-(void)highlightValues:(id)args
//{
//    
//}

//-(void)highlightValue:(id)args
//{
//    ENSURE_ARG_COUNT(args, 2);
//    NSUInteger dataSetIndex = [TiUtils intValue:[args objectAtIndex:0]];
//    NSUInteger itemIndex = [TiUtils intValue:[args objectAtIndex:1]];
//}

-(NSDictionary*)dictionaryFromTouch:(UITouch*)touch
{
    NSMutableDictionary* event = [super dictionaryFromTouch:touch];
    CGPoint pointOfTouch = [touch locationInView:self];
   ChartHighlight* highlight = [[self chartView] getHighlightByTouchPoint:pointOfTouch];
    if (highlight) {
//        ChartDataSetProxy* dataSetProxy = [[self dataProxy] dataSetAtIndex:highlight.dataSetIndex];
        [event setObject:@{
                          @"dataIndex":@(highlight.dataIndex),
                          @"dataSetIndex":@(highlight.dataSetIndex),
                          @"x":@(highlight.x),
                          @"xPx":@(highlight.xPx),
                          @"y":@(highlight.y),
                          @"yPx":@(highlight.yPx),
                          @"isStacked":@(highlight.isStacked),
                          @"stackIndex":@(highlight.stackIndex),
                          } forKey:@"data"] ;
    }
    return event;
}

- (void)chartValueSelected:(ChartViewBase * _Nonnull)chartView entry:(ChartDataEntry * _Nonnull)entry highlight:(ChartHighlight * _Nonnull)highlight {
    BOOL hasHighlight = [proxy _hasListeners:@"highlight"];
    BOOL hasClick = [proxy _hasListeners:@"click"];
//    BOOL hasClick = NO;
    NSLog(@"highlight %@", [highlight description])
    if (hasHighlight || hasClick)
    {
        ChartDataSetProxy* dataSetProxy = [[self dataProxy] dataSetAtIndex:highlight.dataSetIndex];
        NSDictionary* event = @{
                                @"data":@{
                                        @"dataIndex":@(highlight.dataIndex),
                                        @"dataSetIndex":@(highlight.dataSetIndex),
                                        @"x":@(highlight.x),
                                        @"xPx":@(highlight.xPx),
                                        @"y":@(highlight.y),
                                        @"yPx":@(highlight.yPx),
                                        @"isStacked":@(highlight.isStacked),
                                        @"stackIndex":@(highlight.stackIndex),
                                        }
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
