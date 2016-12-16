/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasCharts2BaseChartViewProxy.h"
#import "ChartDataProxy.h"
#import "ChartLegendProxy.h"
#import "ChartXAxisProxy.h"
#import "BaseChart.h"

@implementation AkylasCharts2BaseChartViewProxy


-(id)_initFromCreateFunction:(id<TiEvaluator>)context_ args:(NSArray*)args
{
    [self setRootProxy:self];
    [super _initFromCreateFunction:context_ args:args];
}

-(void)dealloc {
    if (_dataProxy) {
        _dataProxy.parentChartViewProxy = nil;
        [_dataProxy cleanupBeforeRelease];
        RELEASE_TO_NIL(_dataProxy)
    }
    if (_xAxisProxy) {
        _xAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_xAxisProxy)
    }
    if (_legendProxy) {
        _legendProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_legendProxy)
    }
    RELEASE_TO_NIL(_rootProxy)
    [super dealloc];
}


-(TiUIView*)newView
{
    BaseChart *newView = [[BaseChart alloc] init];
    return newView;
}

-(NSArray *)keySequence
{
    static NSArray *keySequence = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        keySequence = [@[@"xAxis", @"legend"] retain];
    });
    return keySequence;
}

-(ChartViewBase*)chartView {
    if (view) {
        return [(BaseChart*)view getOrCreateChartView];
    }
    return nil;
}


-(Class)dataClass {
    return nil;
}


-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy
{
    //    [super unarchiveFromTemplate:viewTemplate_ withEvents:withEvents rootProxy:rootProxy];
    [_dataProxy unarchivedWithRootProxy:rootProxy];
    [_legendProxy unarchivedWithRootProxy:rootProxy];
    [_xAxisProxy unarchivedWithRootProxy:rootProxy];
}


-(void)setRootProxy:(TiProxy*)rootProxy {
    if (!rootProxy) {
        RELEASE_TO_NIL(_rootProxy)
        return;
    }
    //only accept one setRootProxy, the first one counts!
    if (!_rootProxy) {
        if (rootProxy != self) {
            _rootProxy = [rootProxy retain];
        } else {
            _rootProxy = self;
        }
        [self unarchivedWithRootProxy:_rootProxy];
    }
}


- (void)prepareForReuse
{
    [(BaseChart*)[self view] prepareForReuse];
}

- (void)unarchiveFromTemplate:(id)viewTemplate_ withEvents:(BOOL)withEvents rootProxy:(TiProxy*)rootProxy
{
    [self setRootProxy:rootProxy];
    [super unarchiveFromTemplate:viewTemplate_ withEvents:withEvents rootProxy:rootProxy];
}

- (void)unarchiveFromDictionary:(NSDictionary*)dictionary rootProxy:(TiParentingProxy*)rootProxy
{
    [self setRootProxy:rootProxy];
    [super unarchiveFromDictionary:dictionary rootProxy:rootProxy];
}

-(void)highlightValue:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    [[self chartView] highlightValueWithX:[TiUtils intValue:@"x" properties:args] dataSetIndex:[TiUtils intValue:@"dataSetIndex" properties:args] callDelegate:YES];
}

-(void) notifyDataSetChanged:(id)ununsed
{
    [[self chartView] notifyDataSetChanged];
}

-(void) redraw:(id)ununsed
{
    [[self chartView] setNeedsDisplay];

}

-(void)willFirePropertyChanges {
    if (_xAxisProxy) {
        [_xAxisProxy setAxis:[self chartXAxis]];
    }
    if (_legendProxy) {
        [_legendProxy setLegend:[self chartLegend]];
    }
    if (_dataProxy) {
        [[self chartView] setData:[_dataProxy data]];
    }
    [super willFirePropertyChanges];
}

-(void)windowDidOpen
{
    [super windowDidOpen];
    if (_rootProxy) {
        [self setRootProxy:_rootProxy];
    }
}

-(void)viewDidDetach
{
    [super viewDidDetach];
    if (_xAxisProxy) {
        [_xAxisProxy setAxis:nil];
    }
    if (_legendProxy) {
        [_legendProxy setLegend:nil];
    }
}

-(ChartXAxis*)chartXAxis {
    return [[self chartView] xAxis];
}

-(ChartXAxisProxy*)getOrCreateXAxis:(id)value {
    if (!_xAxisProxy) {
        _xAxisProxy =[[ChartXAxisProxy alloc] _initWithPageContext:[self getContext] args:@[value] axis:[self chartXAxis]];
        if (_xAxisProxy) {
            [_xAxisProxy unarchivedWithRootProxy:_rootProxy];
        }
        _xAxisProxy.parentChartViewProxy = self;
    } else if (value) {
        if (_xAxisProxy.axis) {
            //no need to apply properties in no axis actually set
            [self applyProperties:value onBindedProxy:_xAxisProxy];
        }
    }
    return _xAxisProxy;
}

-(ChartXAxisProxy*)xAxis {
    return [self getOrCreateXAxis:nil];;
}

-(ChartLegend*)chartLegend {
    return [[self chartView] legend];
}

-(ChartLegendProxy*)getOrCreateLegend:(id)value {
    if (!_legendProxy) {
        _legendProxy =[[ChartLegendProxy  alloc] _initWithPageContext:[self getContext] args:@[value] legend:[self chartLegend]];
        if (_rootProxy) {
            [_legendProxy unarchivedWithRootProxy:_rootProxy];
        }
        _legendProxy.parentChartViewProxy = self;
    } else if (value) {
        if (_legendProxy.legend) {
            //no need to apply properties in no axis actually set
            [self applyProperties:value onBindedProxy:_legendProxy];
        }
    }
    return _legendProxy;
}

-(ChartLegendProxy*)legend {
    return [self getOrCreateLegend:nil];
}

-(ChartDataProxy*)getOrCreateData:(id)value {
    if (!_dataProxy) {
        _dataProxy =[[[self dataClass] alloc] _initWithPageContext:[self getContext] args:@[value]];
        if (_rootProxy) {
            [_dataProxy unarchivedWithRootProxy:_rootProxy];
        }
        [[self chartView] setData:[_dataProxy data]];
        _dataProxy.parentChartViewProxy = self;
    } else if (value) {
        [self applyProperties:value onBindedProxy:_dataProxy];
    }

    return _dataProxy;
}

-(ChartDataProxy*)data {
    return [self getOrCreateData:nil];
}
-(void)setData:(id)value
{
    Class theClass = [self dataClass];
    
    if (IS_OF_CLASS(value, theClass)) {
        if (_dataProxy) {
            _dataProxy.parentChartViewProxy = nil;
            RELEASE_TO_NIL(_dataProxy)
        }
        _dataProxy = [value retain];
        _dataProxy.parentChartViewProxy = self;
    } else if (IS_OF_CLASS(value, NSDictionary)) {
        [self getOrCreateData:value];
    } else {
        [self throwException:@"Invalid argument passed to data property" subreason:@"You must pass a correct data property" location:CODELOCATION];
    }
    [self replaceValue:value forKey:@"data" notification:NO];
}



-(void)setXAxis:(id)value
{
    ENSURE_DICT(value)
    [self getOrCreateXAxis:value];
    [self replaceValue:value forKey:@"xAxis" notification:NO];
}

-(void)setLegend:(id)value
{
    ENSURE_DICT(value)
    [self getOrCreateLegend:value];
    [self replaceValue:value forKey:@"legend" notification:NO];
}


@end
