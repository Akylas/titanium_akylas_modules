//
//  AkylasChartsMarkerAnnotation.h
//  Titanium
//
//  Created by Martin Guillon on 11/07/13.
//
//

#import "CorePlot-CocoaTouch.h"
#import "AkylasChartsLineLayer.h"

@interface AkylasChartsMarkerAnnotation : CPTPlotSpaceAnnotation
{
@private
//    CPTPlotSpaceAnnotation* layer;

}


-(id)initWithProperties:(NSDictionary*)props;
-(void) setGraph:(CPTGraph*)graph;
@end
