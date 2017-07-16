//
//  AkylasCharts2RadarChartProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2RadarChartProxy.h"
#import "RadarChart.h"
#import "AkylasCharts2RadarChartDataProxy.h"
#import "ChartYAxisProxy.h"

@implementation AkylasCharts2RadarChartProxy
{
    ChartYAxisProxy* _yAxisProxy;
}
-(TiUIView*)newView
{
    RadarChart *newView = [[RadarChart alloc] init];
    return newView;
}

-(void)dealloc
{
    if (_yAxisProxy) {
        _yAxisProxy.parentChartViewProxy = nil;
        RELEASE_TO_NIL(_yAxisProxy)
    }
    [super dealloc];
}

-(NSArray *)keySequence
{
    static NSArray *labelKeySequence = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        labelKeySequence = [[[super keySequence] arrayByAddingObjectsFromArray:@[@"yAxis"]] retain];
    });
    return labelKeySequence;
}


-(Class)dataClass {
    return [AkylasCharts2RadarChartDataProxy class];
}

-(ChartYAxis*)chartYAxis {
    return [(RadarChartView*)[self chartView] yAxis];
}

-(void)willFirePropertyChanges {
    if (_yAxisProxy) {
        [_yAxisProxy setAxis:[self chartYAxis]];
    }
    [super willFirePropertyChanges];
}

-(void)viewDidDetach
{
    [super viewDidDetach];
    if (_yAxisProxy) {
        [_yAxisProxy setAxis:nil];
    }
}

-(ChartYAxisProxy*)yAxis {
    return [self getOrCreateYAxis:nil];
}

-(ChartYAxisProxy*)getOrCreateYAxis:(id)value {
    if (!_yAxisProxy) {
        _yAxisProxy =[[ChartYAxisProxy alloc] _initWithPageContext:[self getContext] args:value?@[value]:nil axis:[self chartYAxis]];
        if (_yAxisProxy) {
            [_yAxisProxy unarchivedWithRootProxy:_rootProxy];
        }
        _yAxisProxy.parentChartViewProxy = self;
    } else if (value) {
        [self applyProperties:value onBindedProxy:_yAxisProxy];
    }
    return _yAxisProxy;
}
-(void)setYAxis:(id)value
{
    ENSURE_DICT(value)
    [self getOrCreateYAxis:value];
    [self replaceValue:value forKey:@"yAxis" notification:NO];
}


@end
