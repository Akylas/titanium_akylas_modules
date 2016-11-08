//
//  AkylasCharts2ScatterChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2ScatterChartDataSetProxy.h"
#import "AkylasCharts2ScatterChartDataProxy.h"

@implementation AkylasCharts2ScatterChartDataSetProxy

-(ScatterChartDataSet*)set
{
    if (!_set) {
        _set = [[ScatterChartDataSet alloc] init];
    }
    return (ScatterChartDataSet*)_set;
}


-(void)setScatterShapeSize:(id)value
{
    [[self set] setScatterShapeSize:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"scatterShapeSize" notification:NO];
}
-(void)setScatterShape:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module scatterShapeFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:ScatterShapeSquare];
    }
    [[self set] setScatterShape:result];
    [self replaceValue:value forKey:@"scatterShape" notification:NO];
}
-(void)setScatterShapeHoleRadius:(id)value
{
    [[self set] setScatterShapeHoleRadius:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"scatterShapeHoleRadius" notification:NO];
}
-(void)setScatterShapeHoleColor:(id)value
{
    [[self set] setScatterShapeHoleColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"scatterShapeHoleColor" notification:NO];
}

//-(void)setCustomScatterShape:(id)value
//{
////    [[self set] setScatterShapeHoleColor:[[TiUtils colorValue:value] _color]];
//    [self replaceValue:value forKey:@"customScatterShape" notification:NO];
//}


-(Class)dataClass {
    return [AkylasCharts2ScatterChartDataProxy class];
}
@end
