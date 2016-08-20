//
//  AkylasCharts2ScatterChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2ScatterChartDataProxy.h"
#import "AkylasCharts2ScatterChartDataSetProxy.h"

@implementation AkylasCharts2ScatterChartDataProxy


-(ScatterChartData*)data {
    if (!_data) {
        _data = [[ScatterChartData alloc] init];
    }
    return (ScatterChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2ScatterChartDataSetProxy class];
}
@end
