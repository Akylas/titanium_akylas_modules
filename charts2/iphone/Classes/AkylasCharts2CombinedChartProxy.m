//
//  AkylasCharts2CombinedChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CombinedChartProxy.h"
#import "CombinedChart.h"
#import "AkylasCharts2CombinedChartDataProxy.h"
@implementation AkylasCharts2CombinedChartProxy


-(TiUIView*)newView
{
    CombinedChart *newView = [[CombinedChart alloc] init];
    return newView;
}


-(Class)dataClass {
    return [AkylasCharts2CombinedChartDataProxy class];
}

@end
