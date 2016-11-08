//
//  AkylasCharts2LineChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "AkylasCharts2LineChartProxy.h"
#import "LineChart.h"
#import "AkylasCharts2LineChartDataProxy.h"

@implementation AkylasCharts2LineChartProxy

-(TiUIView*)newView
{
    LineChart *newView = [[LineChart alloc] init];
    return newView;
}


-(Class)dataClass {
    return [AkylasCharts2LineChartDataProxy class];
}
@end
