//
//  BubbleChart.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "BubbleChart.h"
#import "AkylasCharts2BubbleChartDataProxy.h"

@implementation BubbleChart


-(ChartViewBase*)newChartView {
    
    return [[BubbleChartView alloc] initWithFrame:CGRectZero];
}

-(BubbleChartView*)getOrCreateBubbleChartView {
    return (BubbleChartView*)[self getOrCreateChartView];
}

-(BubbleChartView*)bubbleChartView {
    return (BubbleChartView*)_chartView;
}

-(Class)dataClass {
    return [AkylasCharts2BubbleChartDataProxy class];
}


@end
