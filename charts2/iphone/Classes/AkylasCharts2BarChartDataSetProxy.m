//
//  AkylasCharts2BarChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BarChartDataSetProxy.h"

@implementation AkylasCharts2BarChartDataSetProxy

-(BarChartDataSet*)set
{
    if (!_set) {
        _set = [[BarChartDataSet alloc] init];
    }
    return (BarChartDataSet*)_set;
}

-(Class)dataEntryClass {
    return [BarChartDataEntry class];
}

-(void)setStackLabels:(id)value
{
    [[self set] setStackLabels:value];
    [self replaceValue:value forKey:@"stackLabels" notification:NO];
}


//-(void)setBarSpace:(id)value
//{
//    [[self set] setBarSpace:[TiUtils floatValue:value]];
//    [self replaceValue:value forKey:@"barSpace" notification:NO];
//}

-(void)setBarShadowColor:(id)value
{
    [[self set] setBarShadowColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"barShadowColor" notification:NO];
}

-(void)setBarBorderColor:(id)value
{
    [[self set] setBarBorderColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"barBorderColor" notification:NO];
}
-(void)setBarBorderWidth:(id)value
{
    [[self set] setBarBorderWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"barBorderWidth" notification:NO];
}
-(void)setHighlightAlpha:(id)value
{
    [[self set] setHighlightAlpha:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"highlightAlpha" notification:NO];
}
@end
