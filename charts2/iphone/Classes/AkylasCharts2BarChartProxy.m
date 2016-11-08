//
//  AkylasCharts2BarChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BarChartProxy.h"
#import "BarChart.h"
#import "AkylasCharts2BarChartDataProxy.h"

@implementation AkylasCharts2BarChartProxy

-(TiUIView*)newView
{
    BarChart *newView = [[BarChart alloc] init];
    return newView;
}


-(Class)dataClass {
    return [AkylasCharts2BarChartDataProxy class];
}

@end
