/**
 * charts2
 *
 * Created by Your Name
 * Copyright (c) 2016 Your Company. All rights reserved.
 */

#import "AkylasCharts2Module.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "NSData+Additions.h"

@implementation BaseCallbackNumberFormatter
{
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

- (NSString * _Nonnull)stringForValue:(double)value entry:(ChartDataEntry * _Nonnull)entry dataSetIndex:(NSInteger)dataSetIndex viewPortHandler:(ChartViewPortHandler * _Nullable)viewPortHandler;
{
    if (_callback) {
        NSArray * invocationArray = [NSArray arrayWithObjects:@(value), @(dataSetIndex), nil];
        id result = [_callback call:invocationArray thisObject:nil];
        NSString* strresult = [TiUtils stringValue:result];
        return strresult;
    }
    
}
@end

@implementation CallbackNumberFormatter

- (NSString * _Nonnull)stringForValue:(double)value entry:(ChartDataEntry * _Nonnull)entry dataSetIndex:(NSInteger)dataSetIndex viewPortHandler:(ChartViewPortHandler * _Nullable)viewPortHandler;
{
    if (_callback) {
        NSArray * invocationArray = [NSArray arrayWithObjects:@(value), @(dataSetIndex), nil];
        id result = [_callback call:invocationArray thisObject:nil];
        NSString* strresult = [TiUtils stringValue:result];
        return strresult;
    }
    
}
@end

@implementation AxisCallbackNumberFormatter

- (NSString * _Nonnull)stringForValue:(double)value axis:(ChartAxisBase * _Nullable)axis;
{
    if (_callback) {
        NSArray * invocationArray = [NSArray arrayWithObjects:@(value), nil];
        id result = [_callback call:invocationArray thisObject:nil];
        NSString* strresult = [TiUtils stringValue:result];
        return strresult;
    }
    
}
@end

@implementation AkylasCharts2Module

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"16741bb0-b237-43fa-b5de-64b0b75c0097";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.charts2";
}

-(NSString*)getPasswordKey {
    return @"akylas.modules.key";
}
-(NSString*) getPassword {
    return stringWithHexString(@"7265745b496b2466553b486f736b7b4f");
}


#pragma mark Lifecycle

+(ChartDefaultValueFormatter*)numberFormatterValue:(id)value {
    NSNumberFormatter* formatter = nil;
    if (value) {
        formatter = [[NSNumberFormatter alloc] init];
        if (IS_OF_CLASS(value, NSString)) {
            formatter.positiveFormat = value;
            formatter.negativeFormat = value;
        } else if (IS_OF_CLASS(value, NSDictionary)) {
            formatter.positiveFormat = [TiUtils stringValue:@"positiveFormat" properties:value
                                                        def:[TiUtils stringValue:@"format" properties:value def:formatter.positiveFormat]];
            formatter.negativeFormat = [TiUtils stringValue:@"negativeFormat" properties:value
                                                        def:[TiUtils stringValue:@"format" properties:value def:formatter.negativeFormat]];
            formatter.positivePrefix = [TiUtils stringValue:@"positivePrefix" properties:value
                                                        def:[TiUtils stringValue:@"prefix" properties:value def:formatter.positivePrefix]];
            formatter.negativePrefix = [TiUtils stringValue:@"negativePrefix" properties:value
                                                        def:[TiUtils stringValue:@"prefix" properties:value def:formatter.negativePrefix]];
            formatter.positiveSuffix = [TiUtils stringValue:@"positiveSuffix" properties:value
                                                        def:[TiUtils stringValue:@"suffix" properties:value def:formatter.positiveSuffix]];
            formatter.negativeSuffix = [TiUtils stringValue:@"negativeSuffix" properties:value
                                                        def:[TiUtils stringValue:@"suffix" properties:value def:formatter.negativeSuffix]];
        }
        return [[[ChartDefaultValueFormatter alloc] initWithFormatter:[formatter autorelease]] autorelease];
    }
    return nil;
}

+(ChartDefaultAxisValueFormatter*)axisNumberFormatterValue:(id)value {
    NSNumberFormatter* formatter = nil;
    if (value) {
        formatter = [[NSNumberFormatter alloc] init];
        if (IS_OF_CLASS(value, NSString)) {
            formatter.positiveFormat = value;
            formatter.negativeFormat = value;
        } else if (IS_OF_CLASS(value, NSDictionary)) {
            formatter.positiveFormat = [TiUtils stringValue:@"positiveFormat" properties:value
                                                        def:[TiUtils stringValue:@"format" properties:value def:formatter.positiveFormat]];
            formatter.negativeFormat = [TiUtils stringValue:@"negativeFormat" properties:value
                                                        def:[TiUtils stringValue:@"format" properties:value def:formatter.negativeFormat]];
            formatter.positivePrefix = [TiUtils stringValue:@"positivePrefix" properties:value
                                                        def:[TiUtils stringValue:@"prefix" properties:value def:formatter.positivePrefix]];
            formatter.negativePrefix = [TiUtils stringValue:@"negativePrefix" properties:value
                                                        def:[TiUtils stringValue:@"prefix" properties:value def:formatter.negativePrefix]];
            formatter.positiveSuffix = [TiUtils stringValue:@"positiveSuffix" properties:value
                                                        def:[TiUtils stringValue:@"suffix" properties:value def:formatter.positiveSuffix]];
            formatter.negativeSuffix = [TiUtils stringValue:@"negativeSuffix" properties:value
                                                        def:[TiUtils stringValue:@"suffix" properties:value def:formatter.negativeSuffix]];
        }
        return [[[ChartDefaultAxisValueFormatter alloc] initWithFormatter:[formatter autorelease]] autorelease];
    }
    return nil;
}

+(ChartDataSetRounding)entryRoundValue:(id)value
{
    ChartDataSetRounding result = ChartDataSetRoundingClosest;
    if (IS_OF_CLASS(value, NSString)) {
        if ([value isEqualToString:@"up"])
        {
            result = ChartDataSetRoundingUp;
        }
        else if ([value isEqualToString:@"down"])
        {
            result = ChartDataSetRoundingDown;
        }
    }
    else {
        result = [TiUtils intValue:value def:result];
    }
    return result;
}

+(AxisDependency)axisDependencyValue:(id)value
{
    AxisDependency result = AxisDependencyLeft;
    if (IS_OF_CLASS(value, NSString)) {
        if ([value isEqualToString:@"right"])
        {
            result = AxisDependencyRight;
        }
    }
    else {
        result = [TiUtils intValue:value def:result];
    }
    return result;
}

+(CGLineJoin)lineJoinFromString:(NSString*)value
{
    if ([value isEqualToString:@"miter"])
    {
        return kCGLineJoinMiter;
    }
    else if ([value isEqualToString:@"round"])
    {
        return kCGLineJoinRound;
    }
    return kCGLineJoinBevel;
}

+(CGLineCap)lineCapFromString:(NSString*)value
{
    if ([value isEqualToString:@"square"])
    {
        return kCGLineCapSquare;
    }
    else if ([value isEqualToString:@"round"])
    {
        return kCGLineCapRound;
    }
    return kCGLineCapButt;
}

+(ScatterShape)scatterShapeFromString:(NSString*)value
{
    if ([value isEqualToString:@"x"])
    {
        return ScatterShapeX;
    }
    else if ([value isEqualToString:@"cross"])
    {
        return ScatterShapeCross;
    }
    else if ([value isEqualToString:@"circle"])
    {
        return ScatterShapeCircle;
    }
    else if ([value isEqualToString:@"triangle"])
    {
        return ScatterShapeTriangle;
    }
    return ScatterShapeSquare;
}

+(XAxisLabelPosition)xAxisLabelPositionFromString:(NSString*)value
{
    if ([value isEqualToString:@"bottom"])
    {
        return XAxisLabelPositionBottom;
    }
    else if ([value isEqualToString:@"bottom.inside"])
    {
        return XAxisLabelPositionBottomInside;
    }
    else if ([value isEqualToString:@"top.inside"])
    {
        return XAxisLabelPositionTopInside;
    }
    else if ([value isEqualToString:@"both.sided"])
    {
        return XAxisLabelPositionBothSided;
    }
    return XAxisLabelPositionTop;
}

+(YAxisLabelPosition)yAxisLabelPositionFromString:(NSString*)value
{
    if ([value isEqualToString:@"inside"])
    {
        return YAxisLabelPositionInsideChart;
    }
    return YAxisLabelPositionOutsideChart;
}

+(PieChartValuePosition)pieValuePositionFromString:(NSString*)value
{
    if ([value isEqualToString:@"outside"])
    {
        return PieChartValuePositionOutsideSlice;
    }
    return PieChartValuePositionInsideSlice;
}


+(ChartLimitLabelPosition)labelPositionFromString:(NSString*)value
{
    if ([value isEqualToString:@"left.top"])
    {
        return ChartLimitLabelPositionLeftTop;
    }
    else if ([value isEqualToString:@"left.bottom"])
    {
        return ChartLimitLabelPositionLeftBottom;
    }
    else if ([value isEqualToString:@"right.bottom"])
    {
        return ChartLimitLabelPositionRightBottom;
    }
    return ChartLimitLabelPositionRightTop;
}

+(LineChartMode)lineChartModeFromString:(NSString*)value
{
    if ([value isEqualToString:@"stepped"])
    {
        return LineChartModeStepped;
    }
    else if ([value isEqualToString:@"cubic"])
    {
        return LineChartModeCubicBezier;
    }
    else if ([value isEqualToString:@"horizontal"])
    {
        return LineChartModeHorizontalBezier;
    }
    return LineChartModeLinear;
}

+(ChartLegendDirection)legendDirectionFromString:(NSString*)value
{
    if ([value isEqualToString:@"right.to.left"])
    {
        return ChartLegendDirectionRightToLeft;
    }
    
    return ChartLegendDirectionLeftToRight;
}

+(ChartLegendHorizontalAlignment)legendHorizontalAlignmentFromString:(NSString*)value
{
  if ([value isEqualToString:@"left"])
  {
    return ChartLegendHorizontalAlignmentLeft;
  } else if ([value isEqualToString:@"right"])
  {
    return ChartLegendHorizontalAlignmentRight;
  } else if ([value isEqualToString:@"center"])
  {
    return ChartLegendHorizontalAlignmentCenter;
  }
  
  return ChartLegendHorizontalAlignmentLeft;
}


+(ChartLegendVerticalAlignment)legendVerticalAlignmentFromString:(NSString*)value
{
  if ([value isEqualToString:@"top"])
  {
    return ChartLegendVerticalAlignmentTop;
  } else if ([value isEqualToString:@"bottom"])
  {
    return ChartLegendVerticalAlignmentBottom;
  } else if ([value isEqualToString:@"center"])
  {
    return ChartLegendVerticalAlignmentCenter;
  }
  
  return ChartLegendVerticalAlignmentBottom;
}

+(ChartLegendForm)legendFormFromString:(NSString*)value
{
    if ([value isEqualToString:@"line"])
    {
        return ChartLegendFormLine;
    }
    else if ([value isEqualToString:@"circle"])
    {
        return ChartLegendFormCircle;
    }
    return ChartLegendFormSquare;
}
//-(id)getTemplateColors:(id)value {
//    if (IS_OF_CLASS(value, NSString)) {
//        SEL selector = NSSelectorFromString(value);
//        if ([[ChartColorTemplates class] respondsToSelector:selector]) {
//            NSArray* template  = [[ChartColorTemplates class] performSelector:selector withObject:nil];
//            
//            return template;
//        }
//    }
//    nil;
//}

+(NSArray*) arrayColors:(id)value {
    if (IS_OF_CLASS(value, NSString)) {
        SEL selector = NSSelectorFromString(value);
        if ([[ChartColorTemplates class] respondsToSelector:selector]) {
            NSArray* template  = [[ChartColorTemplates class] performSelector:selector withObject:nil];
            
            return template;
        }
    } else if (IS_OF_CLASS(value, NSArray)) {
//        ENSURE_SINGLE_ARG(value, NSArray)
        NSMutableArray* result= [NSMutableArray array];
        [value enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            UIColor* color = [[TiUtils colorValue:obj] _color];
            if (color) {
                [result addObject:color];
            }
        }];
        return result;
    }
    
    return nil;
}

@end
