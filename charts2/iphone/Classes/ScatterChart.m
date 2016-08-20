//
//  ScatterChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "ScatterChart.h"
#import "AkylasCharts2ScatterChartDataProxy.h"

@implementation ScatterChart

-(ChartViewBase*)newChartView {
    
    return [[ScatterChartView alloc] initWithFrame:CGRectZero];
}

-(ScatterChartView*)getOrCreateScatterChartView {
    return (ScatterChartView*)[self getOrCreateChartView];
}

-(ScatterChartView*)scatterChartView {
    return (ScatterChartView*)_chartView;
}

-(Class)dataClass {
    return [AkylasCharts2ScatterChartDataProxy class];
}

@end
