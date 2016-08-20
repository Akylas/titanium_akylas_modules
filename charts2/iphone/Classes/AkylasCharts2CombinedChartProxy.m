//
//  AkylasCharts2CombinedChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CombinedChartProxy.h"
#import "CombinedChart.h"
@implementation AkylasCharts2CombinedChartProxy


-(TiUIView*)newView
{
    CombinedChart *newView = [[CombinedChart alloc] init];
    return newView;
}
@end
