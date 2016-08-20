//
//  AkylasCharts2BubbleChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2BubbleChartProxy.h"
#import "BubbleChart.h"

@implementation AkylasCharts2BubbleChartProxy

-(TiUIView*)newView
{
    BubbleChart *newView = [[BubbleChart alloc] init];
    return newView;
}
@end
