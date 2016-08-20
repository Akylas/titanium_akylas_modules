//
//  LimitLineProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "AkylasCharts2LimitLineProxy.h"
#import "TiUtils.h"

@implementation AkylasCharts2LimitLineProxy
{
    ChartLimitLine* _limitLine;
}

-(void)dealloc {
    RELEASE_TO_NIL(_limitLine)
    [super dealloc];
}

-(ChartLimitLine*)getOrCreateLimitLine {
    if (!_limitLine) {
        _limitLine = [[ChartLimitLine alloc] init];
    }
}
-(void)setEnabled:(id)value
{
    [[self getOrCreateLimitLine] setEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"enabled" notification:NO];
}
-(void)setXOffset:(id)value
{
    [[self getOrCreateLimitLine] setXOffset:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"xOffset" notification:NO];
}
-(void)setYOffset:(id)value
{
    [[self getOrCreateLimitLine] setYOffset:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"yOffset" notification:NO];
}
-(void)setLineWidth:(id)value
{
    [[self getOrCreateLimitLine] setLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"lineWidth" notification:NO];
}
-(void)setDrawLabel:(id)value
{
    [[self getOrCreateLimitLine] setDrawLabelEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawLabel" notification:NO];
}

-(void)setLineColor:(id)value
{
    [[self getOrCreateLimitLine] setLineColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"lineColor" notification:NO];
}

-(void)setLineDashPhase:(id)value
{
    [[self getOrCreateLimitLine] setLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"lineDashPhase" notification:NO];
}

-(void)setLineDashLengths:(id)value
{
    [[self getOrCreateLimitLine] setLineDashLengths:value];
    [self replaceValue:value forKey:@"lineDashLengths" notification:NO];
}

-(void)setLineDash:(id)value
{
    ENSURE_DICT(value)
    [[self getOrCreateLimitLine] setLineDashLengths:[value objectForKey:@"pattern"]];
    [[self getOrCreateLimitLine] setLineDashPhase:[TiUtils floatValue:@"phase" properties:value]];
    [self replaceValue:value forKey:@"lineDash" notification:NO];
}

-(void)setLimit:(id)value
{
    [[self getOrCreateLimitLine] setLimit:[TiUtils doubleValue:value]];
    [self replaceValue:value forKey:@"limit" notification:NO];
}

-(void)setLabel:(id)value
{
    [[self getOrCreateLimitLine] setLabel:[TiUtils stringValue:value]];
    [self replaceValue:value forKey:@"label" notification:NO];
}

-(void)setLabelPosition:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module labelPositionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:ChartLimitLabelPositionRightTop];
    }
    [[self getOrCreateLimitLine] setLabelPosition:(ChartLimitLabelPosition)result];
    [self replaceValue:value forKey:@"labelPosition" notification:NO];
}

-(void)setFont:(id)value
{
    [[self getOrCreateLimitLine] setValueFont:[[TiUtils fontValue:value] font]];
    [self replaceValue:value forKey:@"font" notification:NO];
}

-(void)setColor:(id)value
{
    [[self getOrCreateLimitLine] setValueTextColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"color" notification:NO];
}
@end
