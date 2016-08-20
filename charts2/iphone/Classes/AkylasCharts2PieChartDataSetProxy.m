//
//  AkylasCharts2PieChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2PieChartDataSetProxy.h"

@implementation AkylasCharts2PieChartDataSetProxy


-(PieChartDataSet*)set
{
    if (!_set) {
        _set = [[PieChartDataSet alloc] init];
    }
    return (PieChartDataSet*)_set;
}


-(void)setSelectionShift:(id)value
{
    [[self set] setSelectionShift:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"selectionShift" notification:NO];
}
-(void)setSliceSpace:(id)value
{
    [[self set] setSliceSpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"sliceSpace" notification:NO];
}

-(void)setXValuePosition:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module pieValuePositionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:LineChartModeLinear];
    }
    [[self set] setXValuePosition:result];
    [self replaceValue:value forKey:@"xValuePosition" notification:NO];
}

-(void)setYValuePosition:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module pieValuePositionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:LineChartModeLinear];
    }
    [[self set] setYValuePosition:result];
    [self replaceValue:value forKey:@"yValuePosition" notification:NO];
}

-(void)setValueLineColor:(id)value
{
    [[self set] setValueLineColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"valueLineColor" notification:NO];
}

-(void)setValueLineWidth:(id)value
{
    [[self set] setValueLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"valueLineWidth" notification:NO];
}

-(void)setValueLinePart1OffsetPercentage:(id)value
{
    [[self set] setValueLinePart1OffsetPercentage:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"valueLinePart1OffsetPercentage" notification:NO];
}

-(void)setValueLinePart1Length:(id)value
{
    [[self set] setValueLinePart1Length:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"valueLinePart1Length" notification:NO];
}

-(void)setValueLinePart2Length:(id)value
{
    [[self set] setValueLinePart2Length:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"valueLinePart2Length" notification:NO];
}

-(void)setValueLineVariableLength:(id)value
{
    [[self set] setValueLineVariableLength:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"valueLineVariableLength" notification:NO];
}
@end
