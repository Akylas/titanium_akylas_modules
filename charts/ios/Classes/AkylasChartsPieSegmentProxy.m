/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPieSegmentProxy.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"

@implementation AkylasChartsPieSegmentProxy
@synthesize fill, title, value, border, labelStyle, explodeOffset;
-(void)_initWithProperties:(NSDictionary*)properties
{
	[super _initWithProperties:properties];
    self.fill = [AkylasChartsParsers parseFillColor:[properties objectForKey:@"fillColor"]
                              withGradient:[properties objectForKey:@"fillGradient"]
                                andOpacity:[properties objectForKey:@"fillOpacity"]
                                       def:nil];
    self.title = [TiUtils stringValue:@"title" properties:properties def:nil];
    NSDictionary* labelProps = [properties valueForKey:@"label"];
    if (labelProps) {
        self.labelStyle = [AkylasChartsParsers parseTextStyle:labelProps def:nil];
    }
    self.border = [AkylasChartsParsers parseLine:properties withPrefix:@"line" def:nil];
    self.value = [properties objectForKey:@"value"];
    self.explodeOffset = [TiUtils floatValue:@"explodeOffset" properties:properties def:0.0f];
}

-(void)dealloc
{
	RELEASE_TO_NIL(fill);
	RELEASE_TO_NIL(title);
	RELEASE_TO_NIL(value);
	[super dealloc];
}

-(void)setValue:(NSNumber *)newValue
{
    RELEASE_TO_NIL(value);
    value = [newValue retain];
    [self.chartProxy refreshData];
}

-(void)removeFromChart:(CPTGraph*)fromGraph
{
}

-(void)renderInChart:(CPTGraph*)toGraph
{
//	[self configurePlot];
}

@end
