//
//  ChartXAxisProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "ChartXAxisProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"

@implementation ChartXAxisProxy

-(ChartXAxis*)axis
{
    return (ChartXAxis*)_axis;
}

-(void)setLabelPosition:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module xAxisLabelPositionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:XAxisLabelPositionTop];
    }
    [[self axis] setLabelPosition:result];
    [self replaceValue:value forKey:@"labelPosition" notification:NO];
}

//-(void)setSpaceBetweenLabels:(id)value
//{
//    [[self axis] setSpaceBetweenLabels:[TiUtils intValue:value]];
//    [self replaceValue:value forKey:@"spaceBetweenLabels" notification:NO];
//}

-(void)setLabelWidth:(id)value
{
    [[self axis] setLabelWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"labelWidth" notification:NO];
}

-(void)setLabelHeight:(id)value
{
    [[self axis] setLabelHeight:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"labelHeight" notification:NO];
}

-(void)setWordWrapWidthPercent:(id)value
{
    [[self axis] setWordWrapWidthPercent:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"wordWrapWidthPercent" notification:NO];
}

-(void)setValueFormatter:(id)value
{
    if (IS_OF_CLASS(value, KrollCallback)) {
        [[self axis] setValueFormatter:[[[XAxisValueCallbackFormatter alloc] initWithCallback:value] autorelease]];
    } else  {
        ChartDefaultAxisValueFormatter* formatter = [AkylasCharts2Module axisNumberFormatterValue:value];
        if (formatter) {
            [[self axis] setValueFormatter:[formatter autorelease]];
        } else {
            [[self axis] setValueFormatter:nil];
        }
    }
    [self replaceValue:value forKey:@"valueFormatter" notification:NO];
}

-(void)setWordWrap:(id)value
{
    [[self axis] setWordWrapEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"wordWrap" notification:NO];
}


//-(void)setLabelsToSkip:(id)value
//{
//    NSInteger result = [TiUtils intValue:value];
//    if (result >= 0) {
//        [[self axis] setLabelsToSkip:result];
//    } else {
//        [[self axis] resetLabelsToSkip];
//    }
//    [self replaceValue:value forKey:@"labelsToSkip" notification:NO];
//}


@end
