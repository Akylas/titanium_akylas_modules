//
//  LineChartView.m
//  akylas.charts2
//
//  Created by Martin Guillon on 27/07/16.
//
//

#import "LineChart.h"
#import "ChartYAxisProxy.h"
#import "AkylasCharts2LineChartDataProxy.h"
#import "AkylasCharts2LineChartDataProxy.h"

@implementation LineChart {
}


-(ChartViewBase*)newChartView {
    
    return [[LineChartView alloc] initWithFrame:CGRectZero];
}

-(Class)dataClass {
    return [AkylasCharts2LineChartDataProxy class];
}

@end
