//
//  ChartDataSetProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "ChartDataSetProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"
#import "ChartDataProxy.h"


@implementation FillCallbackFormatter
{
    KrollCallback* _callback;
}

-(id)initWithCallback:(KrollCallback*)callback
{
    if (self = [super init]) {
        _callback = [callback retain];
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_callback)
    [super dealloc];
}

- (CGFloat)getFillLinePositionWithDataSet:(id <ILineChartDataSet> _Nonnull)dataSet dataProvider:(id <LineChartDataProvider> _Nonnull)dataProvider
{
//    NSArray * invocationArray = [NSArray arrayWithObjects:number, nil];
//    id result = [_callback call:invocationArray thisObject:nil];
//    NSString* strresult = [TiUtils stringValue:result];
//    return strresult;
}
@end

@implementation ChartDataSetProxy

-(void)dealloc
{
    RELEASE_TO_NIL(_set)
    RELEASE_TO_NIL(_parentDataProxy)
    [super dealloc];
}

-(void)cleanupBeforeRelease {
    self.parentDataProxy = nil;
    RELEASE_TO_NIL(_set)
}



-(ChartDataSet*)set {
    return _set;
}

- (void)replaceValue:(id)value forKey:(NSString*)key notification:(BOOL)notify
{
    [super replaceValue:value forKey:key notification:notify];
    [self redraw:nil];
}

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy {
    if (self.bindId) {
        [rootProxy addBinding:self forKey:self.bindId];
    }
}

-(void)setLabel:(id)value
{
    [[self set] setLabel:[TiUtils stringValue:value]];
    [self replaceValue:value forKey:@"label" notification:NO];
}

-(void)setAxisDependency:(id)value
{
    [[self set] setAxisDependency:[AkylasCharts2Module axisDependencyValue:value]];
    [self replaceValue:value forKey:@"axisDependency" notification:NO];
}


-(void)setHighlight:(id)value
{
    [[self set] setHighlightEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"highlight" notification:NO];
}

-(void)setVisible:(id)value
{
    [[self set] setVisible:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"visible" notification:NO];
}
-(void)setDrawValues:(id)value
{
    [[self set] setDrawValuesEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawValues" notification:NO];
}

-(void)setValueTextColor:(id)value
{
    [[self set] setValueTextColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"valueTextColor" notification:NO];
}

-(void)setValueFont:(id)value
{
    [[self set] setValueFont:[[TiUtils fontValue:value] font]];
    [self replaceValue:value forKey:@"valueFont" notification:NO];
}
-(void)setColors:(id)value
{

    NSArray* result= [AkylasCharts2Module arrayColors:value];
    if ([result count] > 0) {
        [[self set] setColors:result];
    } else {
        [[self set] resetColors];
    }
   
    [self replaceValue:value forKey:@"colors" notification:NO];
}

-(void)setColor:(id)value
{
    [[self set] setColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"color" notification:NO];
}

-(void)setValueTextColors:(id)value
{
    ENSURE_SINGLE_ARG(value, NSArray)
    NSArray* result= [AkylasCharts2Module arrayColors:value];
//    if ([result count] > 0) {
        [[self set] setValueColors:result];
//    } else {
//        [_set resetColors];
//    }
    [self replaceValue:value forKey:@"valueColors" notification:NO];
}

-(Class)dataEntryClass {
    return [ChartDataEntry class];
}

-(ChartDataEntry*)dataEntryFromNumber:(NSNumber*)number index:(NSUInteger)idx {
    ChartDataEntry* result = [[[[self dataEntryClass] class] alloc] init];
    [result setY:[number doubleValue]];
    [result setX:idx];
    return [result autorelease];
}

-(void) redraw:(id)ununsed
{
    if (self.parentDataProxy) {
        [self.parentDataProxy redraw:nil];
    }
}

-(void) notifyDataSetChanged:(id)ununsed
{
    [[self set] notifyDataSetChanged];
    if (self.parentDataProxy) {
        [self.parentDataProxy notifyDataChanged:nil];
    }
}

-(void)setValues:(id)value
{
    NSMutableArray* result = [NSMutableArray array];
//    Class dataEntryClass = [self dataEntryClass];
    [value enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            ChartDataEntry* entry = [self objectToChartDatEntry:obj index:idx];
        if (entry) {
            [result addObject:entry];
        }
    }];
    [[self set] setValues:result];
    
    //TODO: optimize and do not do it here but when batch props are applied 
    [self notifyDataSetChanged:nil];
    [self replaceValue:value forKey:@"values" notification:NO];
}


-(void)setValueFormatter:(id)value
{
    if (IS_OF_CLASS(value, KrollCallback)) {
        [[self set] setValueFormatter:[[[CallbackNumberFormatter alloc] initWithCallback:value] autorelease]];
    } else {
        [[self set] setValueFormatter:[AkylasCharts2Module numberFormatterValue:value]];
    }
    [self replaceValue:value forKey:@"valueFormatter" notification:NO];
}

-(id)yMin {
    return @([_set yMin]);
}
-(id)yMax {
    return @([_set yMax]);
}
-(id)entryCount {
    return @([_set entryCount]);
}

- (id)yValForX:(id)x {
    ChartDataEntry* entry = [self entryForX:x];
    if (entry) {
        return @(entry.y);
    }
}
- (id)yValsForX:(id)x {
    NSArray<ChartDataEntry *> * entries = [[self set] entriesForXValue:[TiUtils intValue:x]];
    NSMutableArray* result = [NSMutableArray array];
    [entries enumerateObjectsUsingBlock:^(ChartDataEntry * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [result addObject:@(obj.y)];
    }];
    return result;
}

-(NSMutableDictionary*)chartDataEntryDict:(ChartDataEntry *)entry {
    if (entry) {
        NSMutableDictionary* result = [NSMutableDictionary dictionary];
        [result setObject:@(entry.y) forKey:@"y"];
        [result setObject:@(entry.x) forKey:@"x"];
        if (entry.data)
        {
            [result setObject:entry.data forKey:@"data"];
        }
//        if (entry.icon)
//        {
//            [result setObject:entry.data forKey:@"data"];
//        }
        return result;
    }
    return nil;
}

-(ChartDataEntry*)objectToChartDatEntry:(id)obj index:(NSUInteger)idx {
    if (IS_OF_CLASS(obj, NSNumber)) {
        return [self dataEntryFromNumber:obj index:idx];
    } else if (IS_OF_CLASS(obj, NSDictionary)) {
        ChartDataEntry* entry = [self dictToChartDataEntry:obj];
        entry.x = idx;
        return entry;
    }
}

-(ChartDataEntry*)dictToChartDataEntry:(NSDictionary *)dict {
    if (dict) {
        ChartDataEntry* entry = [[[[self dataEntryClass] class] alloc] init];
        [entry setX:[TiUtils doubleValue:@"x" properties:dict]];
        [entry setY:[TiUtils doubleValue:@"y" properties:dict]];
        [entry setData:[dict objectForKey:@"data"]];
        return [entry autorelease];
    }
    return nil;
}


- (id)entryForIndex:(id)i {
    return [self chartDataEntryDict:[[self set] entryForIndex:[TiUtils intValue:i]]];
}
- (id)entryForX:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    ChartDataEntry * entry = [[self set] entryForXValue:[TiUtils doubleValue:@"x" properties:args] closestToY:[TiUtils doubleValue:@"y" properties:args] rounding:[AkylasCharts2Module entryRoundValue:[args objectForKey:@"round"]]];
    return [self chartDataEntryDict:entry];
}
- (id)entriesForX:(id)x {
    NSArray<ChartDataEntry *> * entries = [[self set] entriesForXValue:[TiUtils intValue:x]];
    NSMutableArray* result = [NSMutableArray array];
    [entries enumerateObjectsUsingBlock:^(ChartDataEntry * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        [result addObject:[self chartDataEntryDict:obj]];
    }];
    return result;
}
- (id)entryIndexWithX:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    NSInteger result = [[self set] entryIndexWithX:[TiUtils doubleValue:@"x" properties:args] closestToY:[TiUtils doubleValue:@"y" properties:args] rounding:[AkylasCharts2Module entryRoundValue:[args objectForKey:@"round"]]];
    return @(result);
}

- (id)entryIndexWithEntry:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    return @([_set entryIndexWithEntry:[self dictToChartDataEntry:args]]);
}

- (id)addEntry:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    return @([_set addEntry:[self dictToChartDataEntry:args]]);
}
- (id)addEntryOrdered:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    return @([_set addEntryOrdered:[self dictToChartDataEntry:args]]);
}
- (id)removeEntry:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    return @([_set removeEntry:[self dictToChartDataEntry:args]]);
}

- (id)removeEntryWithXIndex:(id)x {
    ChartDataEntry* entry = [self entryForX:x];
    if (entry) {
        return [self removeEntry:entry];
    }
}

- (id)removeFirst:(id)x {
    return @([[self set] removeFirst]);
}

- (id)removeLast:(id)x {
    return @([[self set] removeFirst]);
}

- (id)contains:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    return @([[self set] contains:[self dictToChartDataEntry:args]]);
}

- (void)clear:(id)x {
    [[self set] clear];
}

@end
