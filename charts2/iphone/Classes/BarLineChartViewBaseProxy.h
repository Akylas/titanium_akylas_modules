//
//  BarLineChartViewBaseProxy.h
//  akylas.charts2
//
//  Created by Martin Guillon on 08/10/2016.
//
//

#import "AkylasCharts2BaseChartViewProxy.h"
#import "ChartYAxisProxy.h"

@interface BarLineChartViewBaseProxy : AkylasCharts2BaseChartViewProxy
{
    ChartYAxisProxy* _leftAxisProxy;
    ChartYAxisProxy* _rightAxisProxy;
}

-(void)setLeftAxis:(id)value;
-(void)setRightAxis:(id)value;
@end
