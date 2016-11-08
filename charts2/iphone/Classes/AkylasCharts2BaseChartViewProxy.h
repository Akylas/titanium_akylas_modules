/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiViewProxy.h"

@class ChartXAxisProxy;
@class ChartDataProxy;
@class ChartLegendProxy;
@class ChartViewBase;
@interface AkylasCharts2BaseChartViewProxy : TiViewProxy
{
    ChartXAxisProxy* _xAxisProxy;
    ChartLegendProxy* _legendProxy;
    ChartDataProxy* _dataProxy;
    TiProxy* _rootProxy;
}

-(ChartViewBase*)chartView;
-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy;
//-(ChartDataProxy*)dataProxy;
-(Class)dataClass;
-(void) notifyDataSetChanged:(id)ununsed;
-(void) redraw:(id)ununsed;

-(void)setXAxis:(id)value;
-(void)setData:(id)value;
-(void)setLegend:(id)value;
@end
