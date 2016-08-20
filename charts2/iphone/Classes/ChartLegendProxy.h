//
//  ChartLegendProxy.h
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "TiParentingProxy.h"

@class AkylasCharts2BaseChartViewProxy;
@interface ChartLegendProxy : TiParentingProxy
{
    ChartLegend* _legend;
}

@property(nonatomic, readwrite, retain) AkylasCharts2BaseChartViewProxy* _Nullable parentChartViewProxy;
-(id _Nonnull)_initWithPageContext:(id<TiEvaluator> _Nonnull)context args:(NSArray* _Nullable)args axis:(ChartLegend* _Nullable)legend;
-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
@end
