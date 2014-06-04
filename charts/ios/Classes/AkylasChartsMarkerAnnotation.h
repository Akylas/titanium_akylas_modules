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
}


-(id)initForPlotSpace:(CPTPlotSpace *)plotSpace withProperties:(NSDictionary*)properties;
@end
