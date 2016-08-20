//
//  CandleStickChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "CandleStickChart.h"
#import "AkylasCharts2CandleChartDataProxy.h"

@implementation CandleStickChart


-(ChartViewBase*)newChartView {
    
    return [[CandleStickChartView alloc] initWithFrame:CGRectZero];
}

-(CandleStickChartView*)getOrCreateCandleStickChartView {
    return (CandleStickChartView*)[self getOrCreateChartView];
}

-(CandleStickChartView*)candleStickChartView {
    return (CandleStickChartView*)_chartView;
}

-(Class)dataClass {
    return [AkylasCharts2CandleChartDataProxy class];
}
@end
