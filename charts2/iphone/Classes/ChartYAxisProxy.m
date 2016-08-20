//
//  ChartYAxis.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "ChartYAxisProxy.h"
#import "TiUtils.h"
#import "AkylasCharts2Module.h"

@implementation ChartYAxisProxy

-(ChartYAxis*)axis
{
    return (ChartYAxis*)_axis;
}

-(void)setDrawTopYLabelEntry:(id)value
{
    [[self axis] setDrawTopYLabelEntryEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawTopYLabelEntry" notification:NO];
}

-(void)setShowOnlyMinMax:(id)value
{
    [[self axis] setShowOnlyMinMaxEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"showOnlyMinMax" notification:NO];
}

-(void)setInverted:(id)value
{
    [[self axis] setInverted:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"inverted" notification:NO];
}
-(void)setStartAtZero:(id)value
{
    [[self axis] setStartAtZeroEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"startAtZero" notification:NO];
}

-(void)setLabelCount:(id)value
{
    NSInteger count = [TiUtils intValue:value];
    [[self axis] setLabelCount:count force:(count >= 0)];
    [self replaceValue:value forKey:@"labelCount" notification:NO];
}

-(void)setDrawZeroLine:(id)value
{
    [[self axis] setDrawZeroLineEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"drawZeroLine" notification:NO];
}
-(void)setZeroLineColor:(id)value
{
    [[self axis] setZeroLineColor:[[TiUtils colorValue:value] color]];
    [self replaceValue:value forKey:@"zeroLineColor" notification:NO];
}

-(void)setZeroLineWidth:(id)value
{
    [[self axis] setZeroLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"zeroLineWidth" notification:NO];
}

-(void)setZeroLineDashPhase:(id)value
{
    [[self axis] setZeroLineDashPhase:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"zeroLineDashPhase" notification:NO];
}

-(void)setZeroLineDashLengths:(id)value
{
    [[self axis] setZeroLineDashLengths:value];
    [self replaceValue:value forKey:@"zeroLineDashLengths" notification:NO];
}

-(void)setZeroLineDash:(id)value
{
    ENSURE_DICT(value)
    [[self axis] setZeroLineDashLengths:[value objectForKey:@"pattern"]];
    [[self axis] setZeroLineDashPhase:[TiUtils floatValue:@"phase" properties:value]];
    [self replaceValue:value forKey:@"zeroLineDash" notification:NO];
}

-(void)setSpaceTop:(id)value
{
    [[self axis] setSpaceTop:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"spaceTop" notification:NO];
}
-(void)setSpaceBottom:(id)value
{
    [[self axis] setSpaceBottom:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"spaceBottom" notification:NO];
}
-(void)setLabelPosition:(id)value
{
    [[self axis] setLabelPosition:[TiUtils intValue:value]];
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module yAxisLabelPositionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:YAxisLabelPositionOutsideChart];
    }
    [[self axis] setLabelPosition:result];
    [self replaceValue:value forKey:@"labelPosition" notification:NO];
}

-(void)setMinWidth:(id)value
{
    [[self axis] setMinWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"minWidth" notification:NO];
}

-(void)setMaxWidth:(id)value
{
    [[self axis] setMaxWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"maxWidth" notification:NO];
}


-(void)setGranularityEnabled:(id)value
{
    [[self axis] setGranularityEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"granuralityEnabled" notification:NO];
}

-(void)setGranularity:(id)value
{
    [[self axis] setGranularity:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"granurality" notification:NO];
}
-(void)setValueFormatter:(id)value
{
    if (IS_OF_CLASS(value, KrollCallback)) {
        [[self axis] setValueFormatter:[[[CallbackNumberFormatter alloc] initWithCallback:value] autorelease]];
    } else {
        [[self axis] setValueFormatter:[AkylasCharts2Module numberFormatterValue:value]];
    }
    [self replaceValue:value forKey:@"valueFormatter" notification:NO];
}
@end
