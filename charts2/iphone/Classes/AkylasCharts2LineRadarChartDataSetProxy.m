//
//  AkylasCharts2LineRadarChartDataSetSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "AkylasCharts2LineRadarChartDataSetProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"
#import "TiGradient.h"

@implementation AkylasCharts2LineRadarChartDataSetProxy

-(LineRadarChartDataSet*)set
{
    if (!_set) {
        _set = [[LineRadarChartDataSet alloc] init];
    }
    return (LineRadarChartDataSet*)_set;
}

-(void)setLineWidth:(id)value
{
    [[self set] setLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"lineWidth" notification:NO];
}

-(void)setDrawFilled:(id)value
{
    [[self set] setDrawFilledEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawFilled" notification:NO];
}

-(void)setFillAlpha:(id)value
{
    [[self set] setFillAlpha:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"fillAlpha" notification:NO];
}
-(void)setFillColor:(id)value
{
    [[self set] setFillColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"fillColor" notification:NO];
}

-(void)setFillImage:(id)value
{
    [[self set] setFill:[ChartFill fillWithImage:[TiUtils image:[TiUtils stringValue:value] proxy:self]]];
    [self replaceValue:value forKey:@"fillImage" notification:NO];
}


-(void)setFillImageTile:(id)value
{
    [[self set] setFill:[ChartFill fillWithImage:[TiUtils image:[TiUtils stringValue:value] proxy:self] tiled:true]];
    [self replaceValue:value forKey:@"fillImageTiled" notification:NO];
}

-(void)setFillGradient:(id)value
{
    TiGradient* gradient = [TiGradient gradientFromObject:value proxy:self];
    if ([gradient typeValue] == TiGradientTypeLinear) {
        [[self set] setFill:[ChartFill fillWithLinearGradient:[gradient cachedGradient] angle:[gradient angle]]];
    }
    [self replaceValue:value forKey:@"fillGradient" notification:NO];
}
@end
