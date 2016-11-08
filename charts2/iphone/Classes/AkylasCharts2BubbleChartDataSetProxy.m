//
//  AkylasCharts2BubbleChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BubbleChartDataSetProxy.h"

@implementation AkylasCharts2BubbleChartDataSetProxy

-(BubbleChartDataSet*)set
{
    if (!_set) {
        _set = [[BubbleChartDataSet alloc] init];
    }
    return (BubbleChartDataSet*)_set;
}

-(Class)dataEntryClass {
    return [BubbleChartDataEntry class];
}

-(ChartDataEntry*)dataEntryFromNumber:(NSNumber*)number index:(NSUInteger)idx {
    BubbleChartDataEntry* result = [[[[self dataEntryClass] class] alloc] init];
    [result setY:[number doubleValue]];
    [result setSize:[number doubleValue]];
    [result setX:idx];
    return [result autorelease];
}

-(NSMutableDictionary*)chartDataEntryDict:(BubbleChartDataEntry *)entry {
    NSMutableDictionary* result = [super chartDataEntryDict:entry];
    if (result) {
        [result setObject:@(entry.size) forKey:@"size"];
    }
    return result;
}

//-(ChartDataEntry*)dictToChartDataEntry:(NSDictionary *)dict {
//    ChartDataEntry* result = [super dictToChartDataEntry:dict];
//    if (IS_OF_CLASS(result, BubbleChartDataEntry)) {
//        [(BubbleChartDataEntry*)result setSize:[TiUtils doubleValue:@"size" properties:dict]];
//    }
//    return result;
//}
@end
