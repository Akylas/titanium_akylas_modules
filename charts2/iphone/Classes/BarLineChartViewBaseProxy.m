//
//  BarLineChartViewBaseProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 08/10/2016.
//
//

#import "BarLineChartViewBaseProxy.h"

@implementation BarLineChartViewBaseProxy


-(void)dealloc
{
    
    if (_leftAxisProxy) {
        _leftAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_leftAxisProxy)
    }
    if (_rightAxisProxy) {
        _rightAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_rightAxisProxy)
    }
    [super dealloc];
}


-(void)willFirePropertyChanges {
    if (_leftAxisProxy) {
        [_leftAxisProxy setAxis:[self chartLeftAxis]];
    }
    if (_rightAxisProxy) {
        [_rightAxisProxy setAxis:[self chartRightAxis]];
    }
    [super willFirePropertyChanges];
}

-(void)viewDidDetach
{
    [super viewDidDetach];
    if (_leftAxisProxy) {
        [_leftAxisProxy setAxis:nil];
    }
    if (_rightAxisProxy) {
        [_rightAxisProxy setAxis:nil];
    }
}

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy
{
    [super unarchivedWithRootProxy:rootProxy];
    [_leftAxisProxy unarchivedWithRootProxy:rootProxy];
    [_rightAxisProxy unarchivedWithRootProxy:rootProxy];
}


-(ChartYAxis*)chartLeftAxis {
    return [(BarLineChartViewBase*)[self chartView] leftAxis];
}


-(ChartYAxis*)chartRightAxis {
    return [(BarLineChartViewBase*)[self chartView] rightAxis];
}

-(ChartYAxisProxy*)getOrCreateLeftAxis:(id)value {
    if (!_leftAxisProxy) {
        _leftAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[self getContext] args:value?@[value]:nil axis:[self chartLeftAxis]];
        if (_leftAxisProxy) {
            [_leftAxisProxy unarchivedWithRootProxy:_rootProxy];
        }
        _leftAxisProxy.parentChartViewProxy = self;
    } else if (value) {
        [self applyProperties:value onBindedProxy:_leftAxisProxy];
    }
    return _leftAxisProxy;
}
-(ChartYAxisProxy*)getOrCreateRightAxis:(id)value {
    if (!_rightAxisProxy) {
        _rightAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[self getContext] args:value?@[value]:nil axis:[self chartRightAxis]];
        if (_rightAxisProxy) {
            [_rightAxisProxy unarchivedWithRootProxy:_rootProxy];
        }
        _rightAxisProxy.parentChartViewProxy = self;
    } else if (value) {
        [self applyProperties:value onBindedProxy:_rightAxisProxy];
    }
    return _rightAxisProxy;
}

-(ChartYAxisProxy*)leftAxis {
    return [self getOrCreateLeftAxis:nil];
}

-(ChartYAxisProxy*)rightAxis {
    return [self getOrCreateRightAxis:nil];

}

-(void)setLeftAxis:(id)value
{
    ENSURE_DICT(value)
    [self getOrCreateLeftAxis:value];
    [self replaceValue:value forKey:@"leftAxis" notification:NO];
}


-(void)setRightAxis:(id)value
{
    ENSURE_DICT(value)
    [self getOrCreateRightAxis:value];
    [self replaceValue:value forKey:@"rightAxis" notification:NO];
}


@end
