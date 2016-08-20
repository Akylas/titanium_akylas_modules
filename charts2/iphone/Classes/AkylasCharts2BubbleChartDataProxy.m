//
//  AkylasCharts2BubbleChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BubbleChartDataProxy.h"
#import "AkylasCharts2BubbleChartDataSetProxy.h"

@implementation AkylasCharts2BubbleChartDataProxy

-(BubbleChartData*)data {
    if (!_data) {
        _data = [[BubbleChartData alloc] init];
    }
    return (BubbleChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2BubbleChartDataSetProxy class];
}


@end
