//
//  AkylasCharts2LineChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "AkylasCharts2LineChartDataSetProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"
#import "ChartDataProxy.h"

@implementation AkylasCharts2LineChartDataSetProxy

-(LineChartDataSet*)set
{
    if (!_set) {
        _set = [[LineChartDataSet alloc] init];
    }
    return (LineChartDataSet*)_set;
}

-(void)setMode:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module lineChartModeFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:LineChartModeLinear];
    }
    [[self set] setMode:result];
    [self replaceValue:value forKey:@"mode" notification:NO];
}


-(void)setCubicIntensity:(id)value
{
    [[self set] setCubicIntensity:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"cubicIntensity" notification:NO];
}


-(void)setDrawCubic:(id)value
{
    [[self set] setDrawCubicEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawCubic" notification:NO];
}

-(void)setDrawStepped:(id)value
{
    [[self set] setDrawSteppedEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawStepped" notification:NO];
}

-(void)setCircleRadius:(id)value
{
    [[self set] setCircleRadius:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"circleRadius" notification:NO];
}


-(void)setDrawCircles:(id)value
{
    [[self set] setDrawCirclesEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawCircles" notification:NO];
}

-(void)setCircleHoleRadius:(id)value
{
    [[self set] setCircleHoleRadius:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"circleHoleRadius" notification:NO];
}

-(void)setCircleColors:(id)value
{
    NSArray* result= [AkylasCharts2Module arrayColors:value];
    [[self set] setCircleColors:result];
    [self replaceValue:value forKey:@"circleColors" notification:NO];
}

-(void)setCircleColor:(id)value
{
    [[self set] setCircleColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"circleColor" notification:NO];
}
-(void)setCircleHoleColor:(id)value
{
    [[self set] setCircleHoleColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"circleHoleColor" notification:NO];
}

-(void)setDrawCircleHole:(id)value
{
    [[self set] setDrawCircleHoleEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawCircleHole" notification:NO];
}

-(void)setLineDashPhase:(id)value
{
    [[self set] setLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"lineDashPhase" notification:NO];
}

-(void)setLineDashLengths:(id)value
{
    [[self set] setLineDashLengths:value];
    [self replaceValue:value forKey:@"lineDashLengths" notification:NO];
}

-(void)setLineDash:(id)value
{
    ENSURE_DICT(value)
    [[self set] setLineDashLengths:[value objectForKey:@"pattern"]];
    [[self set] setLineDashPhase:[TiUtils floatValue:@"phase" properties:value]];
    [self replaceValue:value forKey:@"lineDash" notification:NO];
}

-(void)setLineCap:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module lineCapFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:kCGLineCapButt];
    }
    [[self set] setLineCapType:(CGLineCap)result];
    [self replaceValue:value forKey:@"lineCap" notification:NO];
}


-(void)setFillFormatter:(id)value
{
    if (IS_OF_CLASS(value, KrollCallback)) {
        [[self set] setFillFormatter:[[[FillCallbackFormatter alloc] initWithCallback:value] autorelease]];
    } else {
//        [[self set] setFillFormatter:[AkylasCharts2Module formatterValue:value]];
    }
    [self replaceValue:value forKey:@"fillFormatter" notification:NO];
}
@end
