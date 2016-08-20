//
//  HorizontalBarChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "HorizontalBarChart.h"

@implementation HorizontalBarChart


-(ChartViewBase*)newChartView {
    
    return [[HorizontalBarChartView alloc] initWithFrame:CGRectZero];
}

@end
