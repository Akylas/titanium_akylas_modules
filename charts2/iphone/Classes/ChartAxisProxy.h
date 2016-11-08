/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiParentingProxy.h"


@interface XAxisValueCallbackFormatter: BaseCallbackNumberFormatter<IChartAxisValueFormatter>
-(id _Nonnull)initWithNumberFormatter:(NSNumberFormatter* _Nonnull)formatter;
@end

@class AkylasCharts2BaseChartViewProxy;
@interface ChartAxisProxy : TiParentingProxy {
    ChartAxisBase* _axis;
}
@property(nonatomic, readwrite, retain) AkylasCharts2BaseChartViewProxy* _Nullable parentChartViewProxy;
-(id _Nullable)_initWithPageContext:(id<TiEvaluator> _Nullable)context_ args:(NSArray* _Nullable)args axis:(ChartAxisBase* _Nullable)axis;
-(void)setAxis:(ChartAxisBase* _Nullable)axis;
-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
@end
