//
//  AkylasChartsMarkerAnnotation.m
//  Titanium
//
//  Created by Martin Guillon on 11/07/13.
//
//

#import "AkylasChartsMarkerAnnotation.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"
#import "TiBase.h"
#import "AkylasChartsLineAndTextLayer.h"

@implementation AkylasChartsMarkerAnnotation

-(id)initForPlotSpace:(CPTPlotSpace *)plotSpace withProperties:(NSDictionary*)properties
{
	if (self = [super initWithPlotSpace:plotSpace anchorPlotPoint:nil]) {
        CPTTextStyle* textStyle = nil;
        NSDictionary* labelProps = [properties valueForKey:@"label"];
        if (labelProps) {
            textStyle = [AkylasChartsParsers parseTextStyle:labelProps def:nil];
            if ([labelProps objectForKey:@"offset"]) {
            }
        }
        AkylasChartsLineAndTextLayer *newLayer = [[AkylasChartsLineAndTextLayer alloc]
                                              initWithDirection:[TiUtils intValue:@"type" properties:properties def:0]
                                              style:[AkylasChartsParsers parseLine:properties withPrefix:@"line" def:nil]
                                              text:[TiUtils stringValue:@"title" properties:properties def:nil]
                                              textStyle:textStyle];
        if (newLayer.direction == CPTLineDirectionHorizontal)
        {
            self.anchorPlotPoint = [NSArray arrayWithObjects:[NSDecimalNumber notANumber], [properties objectForKey:@"value"], nil];
            self.contentAnchorPoint= CGPointMake(0.0, 0.5);
            self.displacement = CGPointMake(0.5f, 0);
        }
        else {
            self.anchorPlotPoint = [NSArray arrayWithObjects:[properties objectForKey:@"value"], [NSDecimalNumber notANumber], nil];
            self.contentAnchorPoint= CGPointMake(0.5, 0.0);
            self.displacement = CGPointMake(0.0f, 0.5f); //not sure why :s axis size?
        }
        if (labelProps) {
            [newLayer setTextShadow:[AkylasChartsParsers parseShadow:@"shadow" inProperties:labelProps def:nil]];
            [newLayer setTextDisplacement:[TiUtils pointValue:@"offset" properties:labelProps def:CGPointZero]];
        }
        if ([properties objectForKey:@"padding"]) {
            UIEdgeInsets insets = [TiUtils contentInsets:[properties objectForKey:@"padding"]];
            newLayer.insets = insets;
        }
        newLayer.shadow = [AkylasChartsParsers parseShadow:@"shadow" inProperties:properties def:nil];
        self.contentLayer = [newLayer autorelease];

	}
	
	return self;
}

-(void)setAnnotationHostLayer:(CPTAnnotationHostLayer *)newLayer
{
    [[NSNotificationCenter defaultCenter] removeObserver:self.contentLayer];
    [super setAnnotationHostLayer:newLayer];
    [((AkylasChartsLineAndTextLayer*)self.contentLayer) setParentLayer:self.annotationHostLayer];
//    [[NSNotificationCenter defaultCenter] addObserver:self.contentLayer
//                                             selector:@selector(sizeToFit)
//                                                 name:CPTLayerBoundsDidChangeNotification
//                                               object:self.annotationHostLayer];
}
@end
