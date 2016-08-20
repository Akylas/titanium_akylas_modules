//
//  AkylasCharts2CandleStickChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CandleChartDataProxy.h"
#import "AkylasCharts2CandleChartDataSetProxy.h"

@implementation AkylasCharts2CandleChartDataProxy


-(CandleChartData*)data {
    if (!_data) {
        _data = [[CandleChartData alloc] init];
    }
    return (CandleChartData*)_data;
}

-(Class)dataSetsClass {
    return [AkylasCharts2CandleChartDataSetProxy class];
}
@end
