//
//  AkylasCharts2BarLineScatterCandleBubbleChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BarLineScatterCandleBubbleChartDataProxy.h"
#import "AkylasCharts2BarLineScatterCandleBubbleChartDataSetProxy.h"

@implementation AkylasCharts2BarLineScatterCandleBubbleChartDataProxy

-(BarLineScatterCandleBubbleChartData*)data {
    if (!_data) {
        _data = [[BarLineScatterCandleBubbleChartData alloc] init];
    }
    return (BarLineScatterCandleBubbleChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2BarLineScatterCandleBubbleChartDataSetProxy class];
}
@end
