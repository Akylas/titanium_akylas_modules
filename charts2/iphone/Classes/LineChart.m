//
//  LineChartView.m
//  akylas.charts2
//
//  Created by Martin Guillon on 27/07/16.
//
//

#import "LineChart.h"
#import "ChartYAxisProxy.h"

@implementation LineChart {
}


-(ChartViewBase*)newChartView {
    
    BarLineChartViewBase* view = [[LineChartView alloc] initWithFrame:CGRectZero];
    return view;
}


@end
