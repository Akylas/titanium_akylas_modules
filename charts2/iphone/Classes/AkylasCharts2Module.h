/**
 * charts2
 *
 * Created by Your Name
 * Copyright (c) 2016 Your Company. All rights reserved.
 */

#import "TiProtectedModule.h"
@interface CallbackNumberFormatter: NSNumberFormatter
-(id)initWithCallback:(KrollCallback*)callback;

@end
@interface AkylasCharts2Module : TiProtectedModule
{
}
+(NSNumberFormatter*)numberFormatterValue:(id)value;
+(CGLineJoin)lineJoinFromString:(NSString*)value;
+(CGLineCap)lineCapFromString:(NSString*)value;
+(ChartLimitLabelPosition)labelPositionFromString:(NSString*)value;
+(XAxisLabelPosition)xAxisLabelPositionFromString:(NSString*)value;
+(YAxisLabelPosition)yAxisLabelPositionFromString:(NSString*)value;
+(NSArray*) arrayColors:(id)value;
+(ChartDataSetRounding)entryRoundValue:(id)value;
+(AxisDependency)axisDependencyValue:(id)value;
+(LineChartMode)lineChartModeFromString:(NSString*)value;
+(PieChartValuePosition)pieValuePositionFromString:(NSString*)value;
+(ScatterShape)scatterShapeFromString:(NSString*)value;
+(ChartLegendDirection)legendDirectionFromString:(NSString*)value;
+(ChartLegendPosition)legendPositionFromString:(NSString*)value;
+(ChartLegendForm)legendFormFromString:(NSString*)value;
@end
