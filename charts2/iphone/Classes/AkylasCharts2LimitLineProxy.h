//
//  LimitLineProxy.h
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "TiParentingProxy.h"

@interface AkylasCharts2LimitLineProxy : TiParentingProxy
-(ChartLimitLine*)getOrCreateLimitLine;
@end
