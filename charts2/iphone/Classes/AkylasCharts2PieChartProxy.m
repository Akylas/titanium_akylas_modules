//
//  AkylasCharts2PieChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2PieChartProxy.h"
#import "PieChart.h"

@implementation AkylasCharts2PieChartProxy

-(TiUIView*)newView
{
    PieChart *newView = [[PieChart alloc] init];
    return newView;
}
@end
