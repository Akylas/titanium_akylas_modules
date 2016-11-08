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
-(id _Nullable)_initWithPageContext:(id<TiEvaluator> _Nullable)context_ args:(NSArray* _Nullable)args legend:(ChartLegend* _Nullable)legend;
-(void)setLegend:(ChartLegend* _Nullable)legend;
-(ChartLegend* _Nullable)legend;
-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
@end
