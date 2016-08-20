//
//  AkylasCharts2CandleChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CandleChartDataSetProxy.h"

@implementation AkylasCharts2CandleChartDataSetProxy


-(CandleChartDataSet*)set
{
    if (!_set) {
        _set = [[CandleChartDataSet alloc] init];
    }
    return (CandleChartDataSet*)_set;
}

-(Class)dataEntryClass {
    return [CandleChartDataEntry class];
}

-(ChartDataEntry*)dataEntryFromNumber:(NSNumber*)number index:(NSUInteger)idx {
    CandleChartDataEntry* result = [[[[self dataEntryClass] class] alloc] init];
    [result setValue:[number doubleValue]];
    [result setHigh:[number doubleValue]];
    [result setLow:[number doubleValue]];
    [result setClose:[number doubleValue]];
    [result setXIndex:idx];
    return [result autorelease];
}

-(NSMutableDictionary*)chartDataEntryDict:(CandleChartDataEntry *)entry {
    NSMutableDictionary* result = [super chartDataEntryDict:entry];
    if (result) {
        [result setObject:@(entry.high) forKey:@"high"];
        [result setObject:@(entry.low) forKey:@"low"];
        [result setObject:@(entry.close) forKey:@"close"];
    }
    return result;
}

//-(ChartDataEntry*)dictToChartDataEntry:(NSDictionary *)dict {
//    ChartDataEntry* result = [super dictToChartDataEntry:dict];
//    if (IS_OF_CLASS(result, CandleChartDataEntry)) {
//        [(CandleChartDataEntry*)result setHigh:[TiUtils doubleValue:@"high" properties:dict]];
//        [(CandleChartDataEntry*)result setLow:[TiUtils doubleValue:@"low" properties:dict]];
//        [(CandleChartDataEntry*)result setClose:[TiUtils doubleValue:@"close" properties:dict]];
//    }
//    return result;
//}


-(void)setBarSpace:(id)value
{
    [[self set] setBarSpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"barSpace" notification:NO];
}

-(void)setShowCandleBar:(id)value
{
    [[self set] setShowCandleBar:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"showCandleBar" notification:NO];
}
-(void)setShadowWidth:(id)value
{
    [[self set] setShadowWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"shadowWidth" notification:NO];
}
-(void)setShadowColor:(id)value
{
    [[self set] setShadowColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"shadowColor" notification:NO];
}

-(void)setShadowColorSameAsCandle:(id)value
{
    [[self set] setShadowColorSameAsCandle:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"shadowColorSameAsCandle" notification:NO];
}

-(void)setNeutralColor:(id)value
{
    [[self set] setNeutralColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"neutralColor" notification:NO];
}

-(void)setIncreasingColor:(id)value
{
    [[self set] setIncreasingColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"increasingColor" notification:NO];
}

-(void)setDecreasingColor:(id)value
{
    [[self set] setDecreasingColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"decreasingColor" notification:NO];
}

-(void)setIncreasingFilled:(id)value
{
    [[self set] setIncreasingFilled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"increasingFilled" notification:NO];
}

-(void)setDecreasingFilled:(id)value
{
    [[self set] setDecreasingFilled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"decreasingFilled" notification:NO];
}
@end
