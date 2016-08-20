/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiUIView.h"

@class ChartXAxisProxy;
@class ChartDataProxy;
@class ChartLegendProxy;
@interface BaseChart : TiUIView<ChartViewDelegate> {
    ChartViewBase* _chartView;
    ChartXAxisProxy* _xAxisProxy;
    ChartLegendProxy* _legendProxy;
    ChartDataProxy* _dataProxy;
}

-(ChartViewBase*)newChartView;
-(ChartViewBase*)getOrCreateChartView;
-(void)setData_:(id)value;
-(ChartDataProxy*)dataProxy;
-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy;
- (void)prepareForReuse;
-(Class)dataClass;
-(void) notifyDataSetChanged;
@end
