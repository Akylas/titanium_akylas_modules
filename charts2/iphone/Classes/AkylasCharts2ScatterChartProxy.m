//
//  AkylasCharts2ScatterChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2ScatterChartProxy.h"
#import "ScatterChart.h"

@implementation AkylasCharts2ScatterChartProxy

-(TiUIView*)newView
{
    ScatterChart *newView = [[ScatterChart alloc] init];
    return newView;
}
@end
