//
//  AkylasCharts2RadarChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2RadarChartDataSetProxy.h"

@implementation AkylasCharts2RadarChartDataSetProxy


-(RadarChartDataSet*)set
{
    if (!_set) {
        _set = [[RadarChartDataSet alloc] init];
    }
    return (RadarChartDataSet*)_set;
}

-(void)setDrawHighlightCircle:(id)value
{
    [[self set] setDrawHighlightCircleEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawHighlightCircle" notification:NO];
}

-(void)setHighlightCircleFillColor:(id)value
{
    [[self set] setHighlightCircleFillColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"highlightCircleFillColor" notification:NO];
}

-(void)setHighlightCircleStrokeColor:(id)value
{
    [[self set] setHighlightCircleStrokeColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"highlightCircleStrokeColor" notification:NO];
}

-(void)setHighlightCircleStrokeAlpha:(id)value
{
    [[self set] setHighlightCircleStrokeAlpha:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightCircleStrokeAlpha" notification:NO];
}
-(void)setHighlightCircleInnerRadius:(id)value
{
    [[self set] setHighlightCircleInnerRadius:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightCircleInnerRadius" notification:NO];
}
-(void)setHighlightCircleOuterRadius:(id)value
{
    [[self set] setHighlightCircleOuterRadius:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightCircleOuterRadius" notification:NO];
}
-(void)setHighlightCircleStrokeWidth:(id)value
{
    [[self set] setHighlightCircleStrokeWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightCircleStrokeWidth" notification:NO];
}
@end
