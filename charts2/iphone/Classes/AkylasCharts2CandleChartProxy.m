//
//  AkylasCharts2CandleChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CandleChartProxy.h"
#import "CandleStickChart.h"

@implementation AkylasCharts2CandleChartProxy

-(TiUIView*)newView
{
    CandleStickChart *newView = [[CandleStickChart alloc] init];
    return newView;
}
@end
