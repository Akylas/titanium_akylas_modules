//
//  AkylasCharts2BarChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BarChartDataProxy.h"
#import "AkylasCharts2BarChartDataSetProxy.h"

@implementation AkylasCharts2BarChartDataProxy

-(BarChartData*)data {
    if (!_data) {
        _data = [[BarChartData alloc] init];
    }
    return (BarChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2BarChartDataSetProxy class];
}

//-(void)setGroupSpace:(id)value
//{
//    [[self data] setGroupSpace:[TiUtils floatValue:value]];
//    [self replaceValue:value forKey:@"groupSpace" notification:NO];
//}
@end
