/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "ChartAxisProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"
#import "AkylasCharts2LimitLineProxy.h"
#import "AkylasCharts2BaseChartViewProxy.h"

@implementation XAxisValueCallbackFormatter
{
    NSNumberFormatter* _formatter;
//    NSNumberFormatter* _numberformatter;
}

-(id)initWithNumberFormatter:(NSNumberFormatter*)formatter
{
    if ([super init]) {
        _formatter = [formatter retain];
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_callback)
    RELEASE_TO_NIL(_formatter)
//    RELEASE_TO_NIL(_numberformatter)
    [super dealloc];
}

- (NSString * _Nonnull)stringForValue:(double)value axis:(ChartAxisBase * _Nullable)axis
{
    if (_callback) {
        NSArray * invocationArray = [NSArray arrayWithObjects:@(value), nil];
        id result = [_callback call:invocationArray thisObject:nil];
        NSString* strresult = [TiUtils stringValue:result];
        return strresult;
    } else {
        return [_formatter stringFromNumber:@(value)];
    }
    return nil;
}
@end

@implementation ChartAxisProxy
{
    NSMutableArray* _limitLines;
}

-(id)_initWithPageContext:(id<TiEvaluator>)context_ args:(NSArray* _Nullable)args axis:(ChartAxisBase* _Nullable)axis
{
    [self setAxis:axis];
    return [self _initWithPageContext:context_ args:axis?args:nil];
}

-(void)dealloc
{
    _axis = nil;
    RELEASE_TO_NIL(_limitLines)
    [super dealloc];
}


-(void)setAxis:(ChartAxisBase* _Nullable)axis {
    _axis = axis;
}

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy {
    if (self.bindId) {
        [rootProxy addBinding:self forKey:self.bindId];
    }
}

- (void)replaceValue:(id)value forKey:(NSString*)key notification:(BOOL)notify
{
    [super replaceValue:value forKey:key notification:notify];
    [self.parentChartViewProxy redraw:nil];
}

-(void)setEnabled:(id)value
{
    [_axis setEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"enabled" notification:NO];
}

-(void)setLabelCount:(id)value
{
    NSInteger count = [TiUtils intValue:value];
    [_axis setLabelCount:count force:(count >= 0)];
    [self replaceValue:value forKey:@"labelCount" notification:NO];
}

-(void)setDrawAxisLine:(id)value
{
    [_axis setDrawAxisLineEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawAxisLine" notification:NO];
}

-(void)setDrawGridLines:(id)value
{
    [_axis setDrawGridLinesEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawGridLines" notification:NO];
}

-(void)setDrawLabels:(id)value
{
    [_axis setDrawLabelsEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawLabels" notification:NO];
}

-(void)setDrawLimitLinesBehindData:(id)value
{
    [_axis setDrawLimitLinesBehindDataEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawLimitLinesBehindData" notification:NO];
}

-(void)setAxisLineWidth:(id)value
{
    [_axis setAxisLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"axisLineWidth" notification:NO];
}

-(void)setAxisLineDashPhase:(id)value
{
    [_axis setAxisLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"axisLineDashPhase" notification:NO];
}

-(void)setAxisLineDashLengths:(id)value
{
    [_axis setAxisLineDashLengths:value];
    [self replaceValue:value forKey:@"axisLineDashLengths" notification:NO];
}

-(void)setAxisLineDash:(id)value
{
    ENSURE_DICT(value)
    [_axis setAxisLineDashLengths:[value objectForKey:@"pattern"]];
    [_axis setAxisLineDashPhase:[TiUtils floatValue:@"phase" properties:value]];
    [self replaceValue:value forKey:@"axisLineDash" notification:NO];
}

-(void)setGridLineWidth:(id)value
{
    [_axis setGridLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"gridLineWidth" notification:NO];
}

-(void)setAxisLineColor:(id)value
{
    [_axis setAxisLineColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"axisLineColor" notification:NO];
}

-(void)setGridColor:(id)value
{
    [_axis setGridColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"gridColor" notification:NO];
}

-(void)setLabelColor:(id)value
{
    [_axis setLabelTextColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"labelColor" notification:NO];
}

-(void)setLabelFont:(id)value
{
    [_axis setLabelFont:[[TiUtils fontValue:value] font]];
    [self replaceValue:value forKey:@"labelFont" notification:NO];
}
-(void)setGridLineDashPhase:(id)value
{
    [_axis setGridLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"gridLineDashPhase" notification:NO];
}

-(void)setGridLineDashLengths:(id)value
{
    [_axis setGridLineDashLengths:value];
    [self replaceValue:value forKey:@"gridLineDashLengths" notification:NO];
}

-(void)setGridLineDash:(id)value
{
    ENSURE_DICT(value)
    [_axis setGridLineDashLengths:[value objectForKey:@"pattern"]];
    [_axis setGridLineDashPhase:[TiUtils floatValue:@"phase" properties:value]];
    [self replaceValue:value forKey:@"gridLineDash" notification:NO];
}

-(void)setGridLineCap:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module lineCapFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:kCGLineCapButt];
    }
    [_axis setGridLineCap:(CGLineCap)result];
    [self replaceValue:value forKey:@"gridLineCap" notification:NO];
}

-(void)setMinValue:(id)value
{
    if (value) {
        [_axis setAxisMinValue:[TiUtils floatValue:value]];
    } else {
        [_axis resetCustomAxisMin];
    }
    [self replaceValue:value forKey:@"minValue" notification:NO];
}

-(void)setMaxValue:(id)value
{
    if (value) {
        [_axis setAxisMaxValue:[TiUtils floatValue:value]];
    } else {
        [_axis resetCustomAxisMax];
    }
    [self replaceValue:value forKey:@"maxValue" notification:NO];
}

-(void)addLimitLine:(id)args
{
    AkylasCharts2LimitLineProxy* proxy;
    if (IS_OF_CLASS(args, AkylasCharts2LimitLineProxy)) {
        proxy = args;
    } else if(IS_OF_CLASS(args, NSDictionary)) {
        proxy = [[AkylasCharts2LimitLineProxy alloc] _initWithPageContext:[self getContext] args:@[args]];
    }
    
    if (proxy) {
        if (!_limitLines) {
            _limitLines = [[NSMutableArray alloc] init];
        }
        [_limitLines addObject:proxy];
        [_axis addLimitLine:[proxy getOrCreateLimitLine]];
    }

}

-(void)removeLimitLine:(id)args
{
    ENSURE_SINGLE_ARG(args, AkylasCharts2LimitLineProxy)
    [_limitLines removeObject:args];
    [_axis removeLimitLine:[args getOrCreateLimitLine]];
}

-(void)removeAllLimitLines:(id)unused
{
    [_axis removeAllLimitLines];
    RELEASE_TO_NIL(_limitLines)
}
@end
