//
//  AkylasCharts2RadarChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2RadarChartProxy.h"
#import "RadarChart.h"

@implementation AkylasCharts2RadarChartProxy
-(TiUIView*)newView
{
    RadarChart *newView = [[RadarChart alloc] init];
    return newView;
}
@end
