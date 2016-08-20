//
//  AkylasCharts2CombinedChartDataProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 31/07/16.
//
//

#import "AkylasCharts2CombinedChartDataProxy.h"
#import "AkylasCharts2LineChartDataProxy.h"
#import "AkylasCharts2CandleChartDataProxy.h"
#import "AkylasCharts2BubbleChartDataProxy.h"
#import "AkylasCharts2BarChartDataProxy.h"

@implementation AkylasCharts2CombinedChartDataProxy

{
    AkylasCharts2LineChartDataProxy* _lineDataProxy;
    AkylasCharts2CandleChartDataProxy* _candleDataProxy;
    AkylasCharts2BubbleChartDataProxy* _bubbleDataProxy;
    AkylasCharts2BarChartDataProxy* _barDataProxy;
}

-(void)dealloc {
    RELEASE_TO_NIL(_lineDataProxy)
    RELEASE_TO_NIL(_lineDataProxy)
    RELEASE_TO_NIL(_candleDataProxy)
    RELEASE_TO_NIL(_bubbleDataProxy)
    [super dealloc];
}

-(CombinedChartData*)data {
    if (!_data) {
        _data = [[CombinedChartData alloc] init];
    }
    return (CombinedChartData*)_data;
}


-(void)setLineData:(id)value {
    RELEASE_TO_NIL(_lineDataProxy)
    if (IS_OF_CLASS(value, AkylasCharts2LineChartDataProxy)) {
        _lineDataProxy = [value retain];
    } else if (IS_OF_CLASS(value, NSDictionary)) {
        _lineDataProxy = [[AkylasCharts2LineChartDataProxy alloc] _initWithPageContext:[self getContext] args:@[value] data:nil];
    }
    [self data].lineData = [_lineDataProxy data];
}

-(void)setBarData:(id)value {
    RELEASE_TO_NIL(_barDataProxy)
    if (IS_OF_CLASS(value, AkylasCharts2BarChartDataProxy)) {
        _barDataProxy = [value retain];
    } else if (IS_OF_CLASS(value, NSDictionary)) {
        _barDataProxy = [[AkylasCharts2BarChartDataProxy alloc] _initWithPageContext:[self getContext] args:@[value] data:nil];
    }
    [self data].barData = [_barDataProxy data];
}


-(void)setCandleData:(id)value {
    RELEASE_TO_NIL(_candleDataProxy)
    if (IS_OF_CLASS(value, AkylasCharts2CandleChartDataProxy)) {
        _candleDataProxy = [value retain];
    } else if (IS_OF_CLASS(value, NSDictionary)) {
        _candleDataProxy = [[AkylasCharts2CandleChartDataProxy alloc] _initWithPageContext:[self getContext] args:@[value] data:nil];
    }
    [self data].candleData = [_candleDataProxy data];
}


-(void)setBubbleData:(id)value {
    RELEASE_TO_NIL(_bubbleDataProxy)
    if (IS_OF_CLASS(value, AkylasCharts2BubbleChartDataProxy)) {
        _bubbleDataProxy = [value retain];
    } else if (IS_OF_CLASS(value, NSDictionary)) {
        _bubbleDataProxy = [[AkylasCharts2BubbleChartDataProxy alloc] _initWithPageContext:[self getContext] args:@[value] data:nil];
    }
    [self data].bubbleData = [_bubbleDataProxy data];
}

//-(Class)dataSetsClass {
//    return [AkylasCharts2CombinedChartDataSetProxy class];
//}
@end
