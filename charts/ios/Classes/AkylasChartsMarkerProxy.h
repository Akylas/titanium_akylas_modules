//
//  AkyasChartsMarkerProxy.h
//  AkylasCharts
//
//  Created by Martin Guillon on 16/05/2014.
//
//

#import "TiProxy.h"

@class AkylasChartsMarkerAnnotation;
@class CPTPlotSpace;
@interface AkylasChartsMarkerProxy : TiProxy
-(AkylasChartsMarkerAnnotation*)getMarkerAnnotationForGraph:(CPTPlotSpace *)plotSpace;
-(AkylasChartsMarkerAnnotation*)marker;
@property (readwrite, assign, nonatomic) BOOL visible;

@end
