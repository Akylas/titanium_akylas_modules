//
//  AkylasCharts2PieChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2PieChartProxy.h"
#import "PieChart.h"
#import "AkylasCharts2PieChartDataProxy.h"

@implementation AkylasCharts2PieChartProxy

-(TiUIView*)newView
{
    PieChart *newView = [[PieChart alloc] init];
    return newView;
}


-(Class)dataClass {
    return [AkylasCharts2PieChartDataProxy class];
}


-(void)setXAxis:(id)value
{
}

@end
