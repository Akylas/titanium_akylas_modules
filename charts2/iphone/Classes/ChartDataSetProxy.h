//
//  ChartDataSetProxy.h
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "TiParentingProxy.h"



@interface FillCallbackFormatter: NSObject<ChartFillFormatter>
-(id _Nonnull)initWithCallback:(KrollCallback* _Nonnull)callback;
- (CGFloat)getFillLinePositionWithDataSet:(id <ILineChartDataSet> _Nonnull)dataSet dataProvider:(id <LineChartDataProvider> _Nonnull)dataProvider;
@end

@class ChartDataProxy;

@interface ChartDataSetProxy : TiParentingProxy
{
    ChartDataSet* _set;
}
@property(nonatomic, readwrite, retain) ChartDataProxy* _Nullable parentDataProxy;
-(ChartDataSet* _Nonnull)set;
-(NSMutableDictionary* _Nullable)chartDataEntryDict:(ChartDataEntry * _Nullable)entry;
-(ChartDataEntry* _Nullable)dictToChartDataEntry:(NSDictionary* _Nullable)dict;
-(void)unarchivedWithRootProxy:(TiProxy* _Nullable)rootProxy;
-(void)cleanupBeforeRelease;
@end
