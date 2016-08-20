//
//  AkylasCharts2BarChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BarChartProxy.h"
#import "BarChart.h"

@implementation AkylasCharts2BarChartProxy

-(TiUIView*)newView
{
    BarChart *newView = [[BarChart alloc] init];
    return newView;
}

@end
