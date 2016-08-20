//
//  AkylasCharts2HorizontalBarChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2HorizontalBarChartProxy.h"
#import "HorizontalBarChart.h"

@implementation AkylasCharts2HorizontalBarChartProxy

-(TiUIView*)newView
{
    HorizontalBarChart *newView = [[HorizontalBarChart alloc] init];
    return newView;
}
@end
