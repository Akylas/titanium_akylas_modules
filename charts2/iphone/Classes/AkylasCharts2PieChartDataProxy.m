//
//  AkylasCharts2PieChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2PieChartDataProxy.h"
#import "AkylasCharts2PieChartDataSetProxy.h"

@implementation AkylasCharts2PieChartDataProxy


-(PieChartData*)data {
    if (!_data) {
        _data = [[PieChartData alloc] init];
    }
    return (PieChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2PieChartDataSetProxy class];
}

@end
