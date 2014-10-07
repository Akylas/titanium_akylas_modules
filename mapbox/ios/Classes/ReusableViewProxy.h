//
//  ReusableViewProxy.h
//  akylas.map
//
//  Created by Martin Guillon on 05/09/2014.
//
//

#import "TiViewProxy.h"

@interface ReusableViewProxy : TiViewProxy<TiViewEventOverrideDelegate>
- (id)initInContext:(id<TiEvaluator>)context;
- (void)setDataItem:(NSDictionary *)dataItem;
-(void)deregisterProxy:(id<TiEvaluator>)context;
@end
