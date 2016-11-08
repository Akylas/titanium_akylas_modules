//
//  ChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "ChartDataProxy.h"
#import "ChartDataSetProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2BaseChartViewProxy.h"

@implementation ChartDataProxy
{
    NSMutableArray* _dataSets;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_data)
    RELEASE_TO_NIL(_dataSets)
    [super dealloc];
}

-(void)cleanupBeforeRelease {
    if ([_dataSets count] > 0) {
        [_dataSets enumerateObjectsUsingBlock:^(ChartDataSetProxy*  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [obj cleanupBeforeRelease];
        }];
    }
}
-(id)init
{
    if (self = [super init])
    {
        _dataSets = [[NSMutableArray alloc] init];
    }
    return self;
}
-(id)_initWithPageContext:(id<TiEvaluator>)context args:(NSArray*)args data:(ChartData*)data
{
    _data = [data retain];
    return [super _initWithPageContext:context args:args];
}

- (void)replaceValue:(id)value forKey:(NSString*)key notification:(BOOL)notify
{
    [super replaceValue:value forKey:key notification:notify];
    [self redraw:nil];
}

-(ChartData*)data {
    return _data;
}

-(void)setXVals:(id)args {
//    [[self data] setXValsObjc:args];
    //TODO: optimize and do not do it here but when batch props are applied
    [self notifyDataChanged:nil];
    [self replaceValue:args forKey:@"xVals" notification:NO];
}

-(Class)dataSetsClass {
    return [ChartDataSetProxy class];
}

-(ChartDataSetProxy*)dataSetAtIndex:(NSUInteger)index {
    if (index < [_dataSets count]) {
        return [_dataSets objectAtIndex:index];
    }
    return nil;
}

-(void) notifyDataChanged:(id)ununsed
{
    [[self data] notifyDataChanged];
    if (self.parentChartViewProxy) {
        [self.parentChartViewProxy notifyDataSetChanged:nil];
    }
}
-(void) redraw:(id)ununsed
{
    if (self.parentChartViewProxy) {
        [self.parentChartViewProxy redraw:nil];
    }
}

-(id)dataSets {
    return _dataSets;
}

-(void)setDataSets:(id)args {
    NSMutableArray* result = [NSMutableArray array];
    ENSURE_ARRAY(args)
    
    if ([_dataSets count] > 0) {
        [_dataSets enumerateObjectsUsingBlock:^(ChartDataSetProxy*  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [obj cleanupBeforeRelease];
        }];
        [_dataSets removeAllObjects];
    }
    __block ChartDataSetProxy* setProxy;
    Class theClass = [self dataSetsClass];
    [args enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        if (IS_OF_CLASS(obj, theClass)) {
            setProxy = obj;
        } else if (IS_OF_CLASS(obj, NSDictionary)){
            setProxy = [[[theClass alloc] _initWithPageContext:[self getContext] args:@[obj]] autorelease];
        } else {
            setProxy = nil;
        }
        if (setProxy) {
            setProxy.parentDataProxy = self;
            [_dataSets addObject:setProxy];
            [result addObject:[(ChartDataSetProxy*)setProxy set]];
        }
    }];
    [[self data] setDataSets:result];
    [self replaceValue:args forKey:@"sets" notification:NO];
}


-(void)addDataSet:(id)args
{
    ChartDataSetProxy* setProxy;
    Class theClass = [self dataSetsClass];
    if (IS_OF_CLASS(args, theClass)) {
        setProxy = args;
    } else if (IS_OF_CLASS(args, NSDictionary)){
        setProxy = [[[theClass alloc] _initWithPageContext:[self getContext] args:@[args]] autorelease];
    } else {
        setProxy = nil;
    }
    if (setProxy) {
        setProxy.parentDataProxy = self;
        [_dataSets addObject:setProxy];
        [[self data] addDataSet:[(ChartDataSetProxy*)setProxy set]];
    }
}

-(void)removeDataSet:(id)args
{
    Class theClass = [self dataSetsClass];
    if (IS_OF_CLASS(args, theClass)) {
        ((ChartDataSetProxy*)args).parentDataProxy = nil;
        [_dataSets removeObject:args];
        [[self data] removeDataSet:[(ChartDataSetProxy*)args set]];
    }

}
-(void)setDrawValues:(id)value
{
    [[self data] setDrawValues:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawValues" notification:NO];
}

-(void)setHighlight:(id)value
{
    [[self data] setHighlightEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"highlight" notification:NO];
}

-(void)setValueFont:(id)value
{
    [[self data] setValueFont:[[TiUtils fontValue:value] font]];
    [self replaceValue:value forKey:@"valueFont" notification:NO];
}


-(void)setValueTextColor:(id)value
{
    [[self data] setValueTextColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"valueTextColor" notification:NO];
}

-(void)setValueFormatter:(id)value
{
    if (IS_OF_CLASS(value, KrollCallback)) {
        [[self data] setValueFormatter:[[[CallbackNumberFormatter alloc] initWithCallback:value] autorelease]];
    } else {
        [[self data] setValueFormatter:[AkylasCharts2Module numberFormatterValue:value]];
    }
    [self replaceValue:value forKey:@"valueFormatter" notification:NO];
}

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy {
    if (self.bindId) {
        [rootProxy addBinding:self forKey:self.bindId];
    }
    [_dataSets enumerateObjectsUsingBlock:^(ChartDataSetProxy*  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [obj unarchivedWithRootProxy:rootProxy];
    }];
}

@end