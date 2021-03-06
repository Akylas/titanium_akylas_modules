/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsParsers.h"
#import "TiPoint.h"
#import "TiUtils.h"
#import "AkylasChartsLabelFormatter.h"
#import "AkylasChartsScatterPlot.h"

#define kDefaultAxisTitleOffset 15

@implementation AkylasChartsParsers

// parseColor
//
// Parameters:
//
// color - color value
// def - default color object
//
// Returns:
//   CPTColor* object or nil
//
+(CPTColor*)parseColor:(id)color def:(CPTColor*)def
{
	TiColor *newColor = [TiUtils colorValue:color];
	if (newColor != nil) {
			return [CPTColor colorWithCGColor:[newColor _color].CGColor];
	}
	
	return def;
}

+(CPTColor*)parseColor:(id)color andOpacity:(id)opacity def:(CPTColor*)def
{
	UIColor *newColor = [[TiUtils colorValue:color] color];
	if (newColor != nil) {
        float alpha = [TiUtils floatValue:opacity def:1.0];
        UIColor* uicolor = [AkylasChartsParsers multiplyColor:newColor byAlpha:alpha];
//        NSLog(@"test %@", uicolor.CIColor.stringRepresentation);
        return [CPTColor colorWithCGColor:uicolor.CGColor];
	}
	
	return def;
}

+(UIColor*) multiplyColor:(UIColor*)color byAlpha:(float)alpha {
    if (alpha < 1.0f) {
        float currentalpha = CGColorGetAlpha(color.CGColor);
        return [color colorWithAlphaComponent:currentalpha*alpha];
    }
    return color;
}

+(CGPoint)anchorValue:(NSString*)name properties:(NSDictionary*)properties def:(CGPoint)def
{
    
	if ([properties isKindOfClass:[NSDictionary class]])
	{
        TiPoint* point = [[[TiPoint alloc] initWithObject:[properties objectForKey:name]] autorelease];
		return [point pointWithinSize:CGSizeMake(1.0f, 1.0f)];
	}
    
	return def;
}

+(CGPoint)anchorValue:(NSString*)name properties:(NSDictionary*)properties
{
	return [AkylasChartsParsers anchorValue:name properties:properties def:CGPointZero];
}

+ (CGFloat)angleFromPoints:(CGPoint)pos1 otherPoint:(CGPoint)pos2
{
    CGPoint vector = CGPointMake(pos1.x-pos2.x, pos1.y - pos2.y);
    double angleCalc;
    if (vector.y < 0)
    {
        // upper Half
        angleCalc = atan2(-vector.y,vector.x);
    }
    else
    {
        angleCalc = atan2(vector.y,-vector.x)+M_PI;
    }
    
    return angleCalc;
}

+(CPTGradient*)parseGradient:(NSDictionary*)properties andOpacity:(id)opacity def:(CPTGradient*)def
{
    CPTGradient* gradient = [[[CPTGradient alloc] init] autorelease];
    gradient.gradientType = CPTGradientTypeAxial;
    float alpha = [TiUtils floatValue:opacity def:1.0];
    
    CGRect bounds = CGRectMake(0.0f, 0.0f, 1.0f, 1.0f);
    CGPoint defaultOffset = CGPointMake(0.5f, 0.5f);
    CGPoint startPoint = [TiUtils pointValue:[properties objectForKey:@"startPoint"] bounds:bounds defaultOffset:defaultOffset];
    CGPoint endPoint = [TiUtils pointValue:[properties objectForKey:@"endPoint"] bounds:bounds defaultOffset:defaultOffset];
    
    if ([[properties valueForKey:@"type"] isEqualToString:@"radial"]) {
        gradient.gradientType = CPTGradientTypeRadial;
        gradient.startAnchor = startPoint;
        gradient.endAnchor = endPoint;
    }
    else {
        gradient.angle = [AkylasChartsParsers angleFromPoints:startPoint otherPoint:endPoint]/M_PI * 180.0 - 180.0;
    }
    
    NSArray* colors = [properties objectForKey:@"colors"];
    int offsetCount = 0;
    for(id color in colors) {
        if ([color isKindOfClass:[NSDictionary class]]) {
            float offset =[TiUtils floatValue:@"offset" properties:color];
            if (offset <= 1 && offset >= 0)
                offsetCount++;
        }
    }
    
    float offsetDelta = -1;
    if (offsetCount != [colors count]) {
        if ([colors count] > 1)
            offsetDelta = 1.0f / ([colors count] - 1);
        else
            offsetDelta = 1.0f;
    }
    
    for (int i = 0; i < [colors count]; i++) {
        id color = [colors objectAtIndex:i];
        UIColor *newColor;
        float offset = (offsetDelta == -1)?-1:(i*offsetDelta);
        if ([color isKindOfClass:[NSDictionary class]]) {
            newColor = [[TiUtils colorValue:@"color" properties:color] color];
            if (offset == -1)
                offset =[TiUtils floatValue:@"offset" properties:color];
        }
        else {
            newColor = [[TiUtils colorValue:color] color];
        }
        newColor = [AkylasChartsParsers multiplyColor:newColor byAlpha:alpha];
        gradient = [gradient addColorStop:[CPTColor colorWithCGColor:newColor.CGColor] atPosition:offset];
    }
	return gradient;
}

// parseTextStyle
//
// Parameters:
//
// properties - dictionary of key-value pairs containing font definitions
// def - default text style object
//
// Returns:
//  CPTMutableTextStyle* object or nil
//
+(CPTMutableTextStyle*)parseTextStyle:(NSDictionary*)properties def:(CPTTextStyle*)def
{
	CPTMutableTextStyle *textStyle = [CPTMutableTextStyle textStyle];
	if (properties != nil) {			
		// Configure the font name and size
		NSDictionary* font = [properties objectForKey:@"font"];
		if (font) {
			WebFont *f = [TiUtils fontValue:font def:[WebFont defaultFont]];
			textStyle.fontName = f.font.fontName;
			textStyle.fontSize = f.size;
		} else {
			textStyle.fontName = def.fontName;
			textStyle.fontSize = def.fontSize;
		}
		
		// Configure the font color
		textStyle.color = [AkylasChartsParsers parseColor:[properties objectForKey:@"color"] andOpacity:[properties objectForKey:@"opacity"] def:def.color];
	} else {
		textStyle.fontName = def.fontName;
		textStyle.fontSize = def.fontSize;
		textStyle.color = def.color;
	}
		
	return textStyle;
}

// parseLineColor
//
// Parameters:
//
// color - color value
// width - width value
// opacity - opacity value
// def - default line style object
//
// Returns:
//   CPTLineStyle* or nil
//
+(CPTLineStyle*)parseLineColor:(id)color withWidth:(id)width withGradient:(id)gradient andOpacity:(id)opacity def:(CPTLineStyle*)def
{
	CPTMutableLineStyle* lineStyle = [CPTMutableLineStyle lineStyle];
	
	lineStyle.lineColor = [AkylasChartsParsers parseColor:color andOpacity:opacity def:[def lineColor]];
    
    if (gradient != nil) {
        CPTGradient* cptgradient = [AkylasChartsParsers parseGradient:gradient andOpacity:opacity def:nil];
        if (cptgradient) {
            lineStyle.lineFill = [CPTFill fillWithGradient:cptgradient];
        }
    }
		
	lineStyle.lineWidth = [TiUtils floatValue:width def:[def lineWidth]];
		
	return lineStyle;
}

+ (NSString *)camelize:(NSString *)string {
	NSMutableString *ret = [NSMutableString string];
	NSString *str = [string lowercaseString];
	NSArray *strPath = [str componentsSeparatedByString:@"/"];
    
	for (int i = 0; i < [strPath count]; i++) {
		NSString *s1 = [strPath objectAtIndex:i];
		NSArray *strArr = [s1 componentsSeparatedByString:@"_"];
		for (int x = 0; x < [strArr count]; x++) {
			NSString *s2 = [strArr objectAtIndex:x];
			unichar l = [s2 characterAtIndex:0];
			NSString *letter = [[NSString stringWithCharacters:&l length:1] uppercaseString];
			NSString *rest = [s2 substringFromIndex:1];
			[ret appendFormat:@"%@%@", letter, rest];
		}
		if (i < [strPath count] - 1) [ret appendString:@"::"];
	}
    
	return ret;
}

/**
 * Converts an underscored separated string into a camelCasedString with the
 * first letter lower case.
 *
 * Changes '/' to '::' to convert paths to namespaces.
 */
+ (NSString *)camelizeWithLowerFirstLetter:(NSString *)string {
	NSString *ret = [AkylasChartsParsers camelize:string];
    
	unichar l = [ret characterAtIndex:0];
	NSString *letter = [[NSString stringWithCharacters:&l length:1] lowercaseString];
	NSString *rest = [ret substringFromIndex:1];
    
	return [NSString stringWithFormat:@"%@%@", letter, rest];
}

+ (NSString *)lowerCamelize:(NSString *)string WithPrefix:(NSString *)prefix {
    if (!prefix) {
        return string;
    }
	if (prefix != nil) return [AkylasChartsParsers camelizeWithLowerFirstLetter:[NSString stringWithFormat:@"%@_%@", prefix, string]];
    return string;
}

+(CPTLineStyle*)parseLine:(id)object withPrefix:(id)prefix def:(CPTLineStyle*)def
{
    if ([object isKindOfClass:[TiProxy class]]) {
        object = [((TiProxy*)object) allProperties];
    }
	CPTMutableLineStyle* lineStyle = [CPTMutableLineStyle lineStyle];
    
    NSString* property = [AkylasChartsParsers lowerCamelize:@"opacity" WithPrefix:prefix];
    id opacity = [object objectForKey:property];
    
    property = [AkylasChartsParsers lowerCamelize:@"color" WithPrefix:prefix];
    id props = [object objectForKey:property];
    if (props) {
        lineStyle.lineColor = [AkylasChartsParsers parseColor:props andOpacity:opacity def:def.lineColor];
    }
    
    property = [AkylasChartsParsers lowerCamelize:@"gradient" WithPrefix:prefix];
    props = [object objectForKey:property];
    if (props)
        lineStyle.lineFill = [CPTFill fillWithGradient:[AkylasChartsParsers parseGradient:props andOpacity:opacity def:nil]];
    
    property = [AkylasChartsParsers lowerCamelize:@"width" WithPrefix:prefix];
    props = [object objectForKey:property];
    if (props)
        lineStyle.lineWidth = [TiUtils floatValue:props def:def.lineWidth];
    
    property = [AkylasChartsParsers lowerCamelize:@"cap" WithPrefix:prefix];
    props = [object objectForKey:property];
    if (props)
        lineStyle.lineCap = (int)[TiUtils intValue:props def:kCGLineCapSquare];

    property = [AkylasChartsParsers lowerCamelize:@"join" WithPrefix:prefix];
    props = [object objectForKey:property];
    if (props)
        lineStyle.lineJoin = (int)[TiUtils intValue:props def:kCGLineJoinMiter];
    
    property = [AkylasChartsParsers lowerCamelize:@"dash" WithPrefix:prefix];
    NSDictionary* dict = [object objectForKey:property];
    if (dict) {
        lineStyle.dashPattern = [dict objectForKey:@"pattern"];
        lineStyle.patternPhase = [TiUtils floatValue:@"phase" properties:dict def:0.0f];
    }
    lineStyle.lineCap = (int)[TiUtils intValue:props def:kCGLineCapSquare];
    
	return lineStyle;
}

// parseFillColor
//
// Parameters:
//
// color - color value
// gradientProps - gradient dictionary
// opacity - opacity value
// def - default fill object
//
// Returns:
//   CPTFill* or nil
//
// NOTE: Gradient definition takes precedence over color if both are specified
//
+(CPTFill*)parseFillColor:(id)color withGradient:(id)gradientProps andOpacity:(id)opacity def:(CPTFill*)def
{
	// Check for gradient
	if (gradientProps) {
        return [CPTFill fillWithGradient:[AkylasChartsParsers parseGradient:gradientProps andOpacity:opacity def:nil]];
	}
	
	if (color) {
		CPTColor* fillColor = [AkylasChartsParsers parseColor:color andOpacity:opacity def:nil];
        return [CPTFill fillWithColor:fillColor];
    }
	
	return def;
}

// parsePlotRange
//
// Parameters:
//
// properties - dictionary of key-value pairs
// def - default plot range
//
// Returns:
//   CPTPlotRange*
//
+(CPTPlotRange*)parsePlotRange:(NSDictionary*)properties def:(CPTPlotRange*)def
{
    if (properties != nil) {
        float min = [TiUtils floatValue:@"min" properties:properties def:0.0];
        float max = [TiUtils floatValue:@"max" properties:properties def:0.0];
        return [CPTPlotRange plotRangeWithLocation:@(min)
                                            length:@(max - min)];
    }
    
    return def;
}

// parseAxis
//
// Parameters:
//
// coordinate - CPTCoordinateX or CPTCoordinateY
// properties - dictionary of key-value pairs
// plotSpace - plotSpace object for the axis
// def - default Axis object
//
// Returns
//   CPTXYAxis* or nil
//
+(CPTXYAxis*)parseAxis:(CPTCoordinate)coordinate properties:(NSDictionary*)properties usingPlotSpace:(CPTPlotSpace*)plotSpace def:(CPTXYAxis*)def forProxy:(TiProxy*)proxy
{
	if (properties != nil) {
		// Get a copy of the xAxis or yAxis from the AxisSet so that we can use the
		// current settings as the default
		
		CPTXYAxis *axis = [[(CPTXYAxis*)[CPTXYAxis alloc] initWithFrame:CGRectZero] autorelease];
		axis.plotSpace = plotSpace;
		
		axis.coordinate = coordinate;
        
        // Parse the alignment or origin -- Alignment supercedes origin.
        BOOL exists;
        int axisAlignment = [TiUtils intValue:@"align" properties:properties def:CPTAlignmentLeft exists:&exists];
        if (exists) {
            int offset = [TiUtils floatValue:@"alignOffset" properties:properties def:0.0];
            if ((axisAlignment == CPTAlignmentRight) || (axisAlignment == CPTAlignmentTop)) {
                axis.axisConstraints = [CPTConstraints constraintWithUpperOffset:offset];
            } else {
                axis.axisConstraints = [CPTConstraints constraintWithLowerOffset:offset];
            }
        } else {     
            axis.orthogonalPosition = @([TiUtils floatValue:@"origin" properties:properties def:0.0]);
        }
		
		// Parse the title
		NSDictionary* titleProps = [properties valueForKey:@"title"];
		if (titleProps) {
			axis.title = [TiUtils stringValue:@"text" properties:titleProps def:nil];
			// Configure the font name and size and color
			axis.titleTextStyle  = [AkylasChartsParsers parseTextStyle:titleProps def:axis.titleTextStyle];
			// The displacement defines the offset from the specified edge
			axis.titleOffset = kDefaultAxisTitleOffset + [TiUtils floatValue:@"offset" properties:titleProps def:0];
		}
		
		// Parse the tick direction
		axis.tickDirection = [TiUtils intValue:@"tickDirection" properties:properties def:CPTSignNegative];
        // Parse the visible range
        axis.visibleRange = [AkylasChartsParsers parsePlotRange:[properties objectForKey:@"visibleRange"] def:axis.visibleRange];
        
		// Parse the line style
		axis.axisLineStyle = [AkylasChartsParsers parseLine:properties withPrefix:@"line" def:nil];
		
        // Set the default labeling policy
		axis.labelingPolicy = CPTAxisLabelingPolicyAutomatic;

		// Parse the major ticks
		axis.majorTickLineStyle = nil;
		axis.majorGridLineStyle = nil;
        axis.majorTickLocations = nil;
        axis.labelTextStyle = nil;
        
        NSDictionary* props = [properties valueForKey:@"majorTicks"];
        if (props != nil) {
            axis.majorTickLineStyle = [AkylasChartsParsers parseLine:props withPrefix:nil def:axis.majorGridLineStyle];
            axis.majorTickLength = [TiUtils floatValue:@"length" properties:props def:2.0];
            
            
            int majorTickInterval = [TiUtils intValue:@"interval" properties:props def:0];
            if (majorTickInterval > 0) {
                axis.labelingPolicy = CPTAxisLabelingPolicyFixedInterval;         
                axis.majorIntervalLength = @(majorTickInterval);
            }
            
            id gridlines = [props objectForKey:@"gridLines"];
            if (gridlines != nil) {
                
                axis.majorGridLineStyle = [AkylasChartsParsers parseLine:gridlines withPrefix:nil def:axis.majorGridLineStyle];
                axis.majorTickLineStyle = axis.majorGridLineStyle;
                axis.gridLinesRange = [AkylasChartsParsers parsePlotRange:[gridlines objectForKey:@"range"] def:axis.gridLinesRange];
            }
            else {
                axis.majorTickLineStyle = nil;
            }
            
                
            // Parse the label format
            NSDictionary* labelProps = [props valueForKey:@"labels"];
            if (labelProps) {
                axis.labelTextStyle = [AkylasChartsParsers parseTextStyle:labelProps def:axis.labelTextStyle];
                axis.labelOffset = [TiUtils floatValue:@"offset" properties:labelProps def:axis.labelOffset];
                axis.labelRotation = degreesToRadians([TiUtils floatValue:@"angle" properties:labelProps def:axis.labelRotation]);
                axis.labelAlignment = [TiUtils intValue:@"textAlign" properties:labelProps def:axis.labelAlignment];
                if (axis.labelAlignment == CPTAlignmentRight) {
                    axis.tickLabelDirection = CPTSignNegative;
                }
                else if (axis.labelAlignment == CPTAlignmentLeft) {
                    axis.tickLabelDirection = CPTSignPositive;
                }
                
                id formatCallback = [labelProps objectForKey:@"formatCallback"];
                if (formatCallback != nil && [formatCallback isKindOfClass:[KrollCallback class]]) {
//                    [proxy replaceValue:formatCallback forKey:(coordinate == CPTCoordinateX)?@"axisXFormatter":@"axisYFormatter" notification:NO];
                    // Label locations can be explicitly specified. It is important to know that the value for the label
                    // must be included in the set of values for the major ticks in order for them to be displayed. Core-plot
                    // will not display them if there isn't a major tick for the value.
                    AkylasChartsLabelFormatter* labelFormatter = [[AkylasChartsLabelFormatter alloc] initWithCallback:formatCallback];
                    axis.labelFormatter = [labelFormatter autorelease];
//                    axis.labelingPolicy = CPTAxisLabelingPolicyLocationsProvided;
                }
                else {

                    // NOTE: Don't set the 'axisLabels' property and use CPTAxisLabelingPolicyNone as this
                    // will cause it to ignore any and all formatting control (e.g. rotation, alignment, etc.).
                    // We can still get the formatting logic to execute by storing axis labels ourself and using
                    // a custom label formatter.
                    
                    id locations = [labelProps objectForKey:@"locations"];
                    if (locations != nil) {
                        // Label locations can be explicitly specified. It is important to know that the value for the label
                        // must be included in the set of values for the major ticks in order for them to be displayed. Core-plot
                        // will not display them if there isn't a major tick for the value.
                        AkylasChartsLabelFormatter* labelFormatter = [[[AkylasChartsLabelFormatter alloc] initWithArray:locations] autorelease];
                        axis.majorTickLocations = labelFormatter.tickLocations;
                        axis.labelFormatter = labelFormatter;
                        axis.labelingPolicy = CPTAxisLabelingPolicyLocationsProvided;
                    }
                    
                    
                    // Number format can be specified and the values will be formatted according to the specified format. Typically
                    // this is done with the '#' and '0' characters (e.g. "###0.00"). Optionally, prefix and suffix strings can
                    // be specified. See http://unicode.org/reports/tr35/tr35-6.html#Number_Format_Patterns for details.
                    if (axis.labelFormatter) {
                        NSNumberFormatter* formatter = [[NSNumberFormatter alloc] init];
                        formatter.positiveFormat = [TiUtils stringValue:@"numberFormatPositive" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberFormat" properties:labelProps def:formatter.positiveFormat]];
                        formatter.negativeFormat = [TiUtils stringValue:@"numberFormatNegative" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberFormat" properties:labelProps def:formatter.negativeFormat]];
                        formatter.positivePrefix = [TiUtils stringValue:@"numberPrefixPositive" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberPrefix" properties:labelProps def:formatter.positivePrefix]];
                        formatter.negativePrefix = [TiUtils stringValue:@"numberPrefixNegative" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberPrefix" properties:labelProps def:formatter.negativePrefix]];
                        formatter.positiveSuffix = [TiUtils stringValue:@"numberSuffixPositive" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberSuffix" properties:labelProps def:formatter.positiveSuffix]];
                        formatter.negativeSuffix = [TiUtils stringValue:@"numberSuffixNegative" properties:labelProps
                                                                    def:[TiUtils stringValue:@"numberSuffix" properties:labelProps def:formatter.negativeSuffix]];
                        axis.labelFormatter = formatter;
                        [formatter release];
                    }
                }


            }
        }		
        
        // Parse the minor ticks
		axis.minorTickLineStyle = nil;
		axis.minorGridLineStyle = nil;
        
        props = [properties valueForKey:@"minorTicks"];
        if (props != nil) {
			axis.minorTickLineStyle = [AkylasChartsParsers parseLineColor:[props objectForKey:@"color"]
															withWidth:[props objectForKey:@"width"]
                                                         withGradient:[props objectForKey:@"gradient"]
														   andOpacity:[props objectForKey:@"opacity"]
																  def:axis.minorTickLineStyle];
			axis.minorTickLength = [TiUtils floatValue:@"length" properties:props def:0.0];

            axis.minorTicksPerInterval = [TiUtils intValue:@"count" properties:props def:0];
            
            id gridlines = [props objectForKey:@"gridLines"];
            if (gridlines != nil) {
                axis.minorGridLineStyle = [AkylasChartsParsers parseLineColor:[gridlines objectForKey:@"color"]
                                                                withWidth:[gridlines objectForKey:@"width"]
                                                             withGradient:[gridlines objectForKey:@"gradient"]
                                                              andOpacity:[gridlines objectForKey:@"opacity"]
                                                                      def:axis.minorGridLineStyle];
            }
        } 
 
		return axis;
	}
	
	return def;
}

// parseSymbol
//
// Parameters:
//
// properties - dictionary of key-value pairs
//
// Returns:
//   CPTPlotSymbol* or nil
//
+(CPTPlotSymbol*)parseSymbol:(NSDictionary*)properties def:(CPTPlotSymbol*)def
{
    // *** WARNING ***
    // It is very important that the plot symbol be a valid object. Setting the plot symbol for
    // a plot to nil will break things in core plot library as it doesn't do a very good job of
    // checking for nil before trying to get the plot symbol's size. If you do set it to nil then
    // you will not get plot symbol clicked events on iOS5.
    
    if (def == nil) {
        def = [CPTPlotSymbol plotSymbol];
    }
    
	if (properties != nil) {
		CPTPlotSymbol *symbol = [[[CPTPlotSymbol alloc] init] autorelease];
		CPTPlotSymbolType type = (CPTPlotSymbolType)[TiUtils intValue:@"type" properties:properties def:def.symbolType];
		symbol.symbolType = type;
		
		symbol.lineStyle = [AkylasChartsParsers parseLine:properties withPrefix:@"line" def:def.lineStyle];
		
		symbol.fill = [AkylasChartsParsers parseFillColor:[properties objectForKey:@"fillColor"]
										 withGradient:[properties objectForKey:@"fillGradient"]
										 andOpacity:[properties objectForKey:@"fillOpacity"]
												def:def.fill];
		
		symbol.size = CGSizeMake([TiUtils floatValue:@"width" properties:properties def:def.size.width], 
								 [TiUtils floatValue:@"height" properties:properties def:def.size.height]);
		return symbol;
	}
	
	return def;
}

+(CPTShadow*)parseShadow:(NSDictionary*)properties def:(CPTShadow*)def
{
    if (properties != nil) {
        CPTMutableShadow *shadow = [CPTMutableShadow shadow];
        if (def) {
            shadow.shadowBlurRadius = def.shadowBlurRadius;
            shadow.shadowColor = def.shadowColor;
            shadow.shadowOffset = def.shadowOffset;
        }
		shadow.shadowBlurRadius = [TiUtils floatValue:@"radius" properties:properties def:shadow.shadowBlurRadius];
        
        shadow.shadowColor = [AkylasChartsParsers parseColor:[properties objectForKey:@"color"] andOpacity:[properties objectForKey:@"opacity"] def:shadow.shadowColor];
        
        CGPoint point = [TiUtils pointValue:[properties objectForKey:@"offset"]];
        shadow.shadowOffset = CGSizeMake(point.x, -point.y);
        return shadow ;
    }
	return def;
}

+(CPTShadow*)parseShadow:(NSString*) property inProperties:(id)properties def:(CPTShadow*)def
{
    if ([properties isKindOfClass:[TiProxy class]]) {
        properties = [((TiProxy*)properties) allProperties];
    }
    id object = [properties objectForKey:property];
    if (object != nil) {
        return [AkylasChartsParsers parseShadow:object def:def];
    }
    return def;
}

// parseLabelStyle
//
// Parameters:
//
// properties - dictionary of key-value pairs
// plot - plot object to update
//
+(void)parseLabelStyle:(NSDictionary*)properties forPlot:(CPTPlot*)plot def:(CPTTextStyle*) defaultStyle
{
    if(properties == nil) {
        plot.labelTextStyle = defaultStyle;
        return;
    }
	plot.labelTextStyle = [AkylasChartsParsers parseTextStyle:properties def:defaultStyle];
    if ([plot isKindOfClass:[AkylasChartsScatterPlot class]])
    {
        ((AkylasChartsScatterPlot*)plot).labelDisplacement = [TiUtils pointValue:@"offset" properties:properties def:CGPointZero];
    }
	plot.labelRotation = degreesToRadians([TiUtils floatValue:@"angle" properties:properties def:plot.labelRotation]);
    id formatCallback = [properties objectForKey:@"formatCallback"];
    if (formatCallback != nil && [formatCallback isKindOfClass:[KrollCallback class]]) {
        // Label locations can be explicitly specified. It is important to know that the value for the label
        // must be included in the set of values for the major ticks in order for them to be displayed. Core-plot
        // will not display them if there isn't a major tick for the value.
        AkylasChartsLabelFormatter* labelFormatter = [[[AkylasChartsLabelFormatter alloc] initWithCallback:formatCallback] autorelease];
        plot.labelFormatter = labelFormatter;
        //                    axis.labelingPolicy = CPTAxisLabelingPolicyLocationsProvided;
    }
    else {
        // NOTE: Don't set the 'axisLabels' property and use CPTAxisLabelingPolicyNone as this
        // will cause it to ignore any and all formatting control (e.g. rotation, alignment, etc.).
        // We can still get the formatting logic to execute by storing axis labels ourself and using
        // a custom label formatter.
        
        id locations = [properties objectForKey:@"locations"];
        if (locations != nil) {
            // Label locations can be explicitly specified. It is important to know that the value for the label
            // must be included in the set of values for the major ticks in order for them to be displayed. Core-plot
            // will not display them if there isn't a major tick for the value.
            AkylasChartsLabelFormatter* labelFormatter = [[[AkylasChartsLabelFormatter alloc] initWithArray:locations] autorelease];
            plot.labelFormatter = labelFormatter;
        }
        
        
        // Number format can be specified and the values will be formatted according to the specified format. Typically
        // this is done with the '#' and '0' characters (e.g. "###0.00"). Optionally, prefix and suffix strings can
        // be specified. See http://unicode.org/reports/tr35/tr35-6.html#Number_Format_Patterns for details.
        if (plot.labelFormatter) {
            AkylasChartsLabelFormatter* formatter = (AkylasChartsLabelFormatter*)plot.labelFormatter;
            formatter.positiveFormat = [TiUtils stringValue:@"numberFormatPositive" properties:properties
                                                        def:[TiUtils stringValue:@"numberFormat" properties:properties def:formatter.positiveFormat]];
            formatter.negativeFormat = [TiUtils stringValue:@"numberFormatNegative" properties:properties
                                                        def:[TiUtils stringValue:@"numberFormat" properties:properties def:formatter.negativeFormat]];
            formatter.positivePrefix = [TiUtils stringValue:@"numberPrefixPositive" properties:properties
                                                        def:[TiUtils stringValue:@"numberPrefix" properties:properties def:formatter.positivePrefix]];
            formatter.negativePrefix = [TiUtils stringValue:@"numberPrefixNegative" properties:properties
                                                        def:[TiUtils stringValue:@"numberPrefix" properties:properties def:formatter.negativePrefix]];
            formatter.positiveSuffix = [TiUtils stringValue:@"numberSuffixPositive" properties:properties
                                                        def:[TiUtils stringValue:@"numberSuffix" properties:properties def:formatter.positiveSuffix]];
            formatter.negativeSuffix = [TiUtils stringValue:@"numberSuffixNegative" properties:properties
                                                        def:[TiUtils stringValue:@"numberSuffix" properties:properties def:formatter.negativeSuffix]];
        }
    }
}

// decimalFromFloat
//
// Parameters:
//
// value - object to convert
// def - default value
//
// Returns:
//   NSDecimal
//
+(NSNumber*)decimalFromFloat:(id)value def:(NSNumber*)def
{
	if (value) {
		return @([TiUtils floatValue:value def:0.0]);
	}
	
	return def;
}

@end
