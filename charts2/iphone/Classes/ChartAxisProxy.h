/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiParentingProxy.h"


@interface XAxisValueCallbackFormatter: NSObject<ChartXAxisValueFormatter>
-(id _Nonnull)initWithCallback:(KrollCallback* _Nonnull)callback;
-(id _Nonnull)initWithNumberFormatter:(NSNumberFormatter* _Nonnull)formatter;
- (NSString * _Nonnull)stringForXValue:(NSInteger)index original:(NSString * _Nonnull)original viewPortHandler:(ChartViewPortHandler * _Nonnull)viewPortHandler;
@end

@class AkylasCharts2BaseChartViewProxy;
@interface ChartAxisProxy : TiParentingProxy {
    ChartAxisBase* _axis;
}
@property(nonatomic, readwrite, retain) AkylasCharts2BaseChartViewProxy* _Nullable parentChartViewProxy;

-(id _Nonnull)_initWithPageContext:(id<TiEvaluator> _Nonnull)context args:(NSArray* _Nullable)args axis:(ChartAxisBase* _Nonnull)axis;

-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
@end
