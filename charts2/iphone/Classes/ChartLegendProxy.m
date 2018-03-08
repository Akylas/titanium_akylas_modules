//
//  ChartLegendProxy.m
//  akylas.charts2
//
//  Created by Martin Guillon on 29/07/16.
//
//

#import "ChartLegendProxy.h"
#import "AkylasCharts2BaseChartViewProxy.h"
#import "TiUtils.h"

@implementation ChartLegendProxy

-(void)dealloc
{
    RELEASE_TO_NIL(_legend)
    RELEASE_TO_NIL(_parentChartViewProxy)
    [super dealloc];
}

-(id)_initWithPageContext:(id<TiEvaluator>)context_ args:(NSArray* _Nullable)args legend:(ChartLegend* _Nullable)legend
{
    [self setLegend:legend];
    return [self _initWithPageContext:context_ args:legend?args:nil];
}

-(void)setLegend:(ChartLegend* _Nullable)legend{
    RELEASE_TO_NIL(_legend)
   _legend = [legend retain];
}

-(ChartLegend* _Nullable)legend{
    return _legend;
}

-(void)unarchivedWithRootProxy:(TiProxy*)rootProxy {
    if (self.bindId) {
        [rootProxy addBinding:self forKey:self.bindId];
    }
}

- (void)replaceValue:(id)value forKey:(NSString*)key notification:(BOOL)notify
{
    [super replaceValue:value forKey:key notification:notify];
    [self.parentChartViewProxy redraw:nil];
}


-(void)setEnabled:(id)value
{
    [_legend setEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"enabled" notification:NO];
}

-(void)setColor:(id)value
{
    [_legend setTextColor:[[TiUtils colorValue:value] _color]];
    [self replaceValue:value forKey:@"color" notification:NO];
}

-(void)setFont:(id)value
{
    [_legend setFont:[[TiUtils fontValue:value] font]];
    [self replaceValue:value forKey:@"font" notification:NO];
}

-(void)setDirection:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module legendDirectionFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:ChartLegendDirectionLeftToRight];
    }
    [_legend setDirection:result];
    [self replaceValue:value forKey:@"direction" notification:NO];
}

-(void)setHorizontalAlignment:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module legendHorizontalAlignmentFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:ChartLegendHorizontalAlignmentLeft];
    }
    [_legend setHorizontalAlignment:result];
    [self replaceValue:value forKey:@"horizontalAlignment" notification:NO];
}

-(void)setVerticalAlignment:(id)value
{
  NSInteger result;
  if (IS_OF_CLASS(value, NSString)) {
    result = [AkylasCharts2Module legendVerticalAlignmentFromString:[TiUtils stringValue:value]];
  }
  else {
    result = [TiUtils intValue:value def:ChartLegendVerticalAlignmentBottom];
  }
  [_legend setVerticalAlignment:result];
  [self replaceValue:value forKey:@"verticalAlignment" notification:NO];
}

//-(void)setPosition:(id)value
//{
//  NSInteger result;
//  if (IS_OF_CLASS(value, NSString)) {
//    result = [AkylasCharts2Module legendPositionFromString:[TiUtils stringValue:value]];
//  }
//  else {
//    result = [TiUtils intValue:value def:ChartLegendPositionBelowChartLeft];
//  }
//  [_legend setHorizontalAlignment:<#(enum ChartLegendHorizontalAlignment)#>:result];
//  [self replaceValue:value forKey:@"position" notification:NO];
//}


-(void)setFormSize:(id)value
{
    [_legend setFormSize:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"formSize" notification:NO];
}

-(void)setForm:(id)value
{
    NSInteger result;
    if (IS_OF_CLASS(value, NSString)) {
        result = [AkylasCharts2Module legendFormFromString:[TiUtils stringValue:value]];
    }
    else {
        result = [TiUtils intValue:value def:ChartLegendFormSquare];
    }
        
    [_legend setForm:result];
    [self replaceValue:value forKey:@"form" notification:NO];
}

-(void)setFormLineWidth:(id)value
{
    [_legend setFormLineWidth:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"formLineWidth" notification:NO];
}

-(void)setFormToTextSpace:(id)value
{
    [_legend setFormToTextSpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"formToTextSpace" notification:NO];
}

-(void)setXEntrySpace:(id)value
{
    [_legend setXEntrySpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"xEntrySpace" notification:NO];
}

-(void)setYEntrySpace:(id)value
{
    [_legend setYEntrySpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"yEntrySpace" notification:NO];
}

-(void)setXOffset:(id)value
{
    [_legend setXOffset:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"xOffset" notification:NO];
}
-(void)setYOffset:(id)value
{
    [_legend setYOffset:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"yOffset" notification:NO];
}

-(void)setStackSpace:(id)value
{
    [_legend setStackSpace:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"stackSpace" notification:NO];
}


-(void)setWordWrap:(id)value
{
    [_legend setWordWrapEnabled:[TiUtils boolValue:value]];
    [self replaceValue:value forKey:@"wordWrap" notification:NO];
}

-(void)setMaxSizePercent:(id)value
{
    [_legend setMaxSizePercent:[TiUtils floatValue:value]];
    [self replaceValue:value forKey:@"maxSizePercent" notification:NO];
}

@end
