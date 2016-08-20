//
//  AkylasCharts2LineChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "AkylasCharts2LineChartDataProxy.h"
#import "AkylasCharts2LineChartDataSetProxy.h"

@implementation AkylasCharts2LineChartDataProxy

-(LineChartData*)data {
    if (!_data) {
        _data = [[LineChartData alloc] init];
    }
    return (LineChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2LineChartDataSetProxy class];
}
@end
