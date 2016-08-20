//
//  ChartDataProxy.h
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "TiParentingProxy.h"

@class AkylasCharts2BaseChartViewProxy;
@class ChartDataSetProxy;
@interface ChartDataProxy : TiParentingProxy
{
    ChartData* _data;
}
@property(nonatomic, readwrite, retain) AkylasCharts2BaseChartViewProxy* _Nullable parentChartViewProxy;
-(id _Nonnull)_initWithPageContext:(id<TiEvaluator> _Nonnull)context args:(NSArray* _Nullable)args data:(ChartData* _Nullable)data;
-(ChartData* _Nonnull)data;
-(Class _Nonnull)dataSetsClass;

-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
-(ChartDataSetProxy* _Nullable)dataSetAtIndex:(NSUInteger)index;

-(void) notifyDataChanged:(id _Nullable)ununsed;
-(void) redraw:(id _Nullable)ununsed;
-(void)cleanupBeforeRelease;
@end
