//
//  AkyasChartsMarkerProxy.m
//  AkylasCharts
//
//  Created by Martin Guillon on 16/05/2014.
//
//

#import "AkylasChartsMarkerProxy.h"
#import "AkylasChartsMarkerAnnotation.h"
#import "AkylasChartsLineAndTextLayer.h"

@implementation AkylasChartsMarkerProxy
{
    AkylasChartsMarkerAnnotation* _marker;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_marker)

    [super dealloc];
}

-(AkylasChartsMarkerAnnotation*)marker
{
    return _marker;
}

-(AkylasChartsMarkerAnnotation*)getMarkerAnnotationForGraph:(CPTPlotSpace *)plotSpace {
    if (!_marker || _marker.plotSpace != plotSpace) {
        if (_marker) {
            [_marker.plotSpace.graph removeAnnotation:_marker];
            RELEASE_TO_NIL(_marker)
        }
        _marker = [[AkylasChartsMarkerAnnotation alloc] initForPlotSpace:plotSpace withProperties:[self allProperties]];
        
    }
    return _marker;
}

-(NSNumber*)value
{
    return [self valueForUndefinedKey:@"value"];
}

-(void)setValue:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    ENSURE_UI_THREAD_1_ARG(value);
    if (_marker) {
        BOOL horizontal = ((AkylasChartsLineAndTextLayer*)_marker.contentLayer).direction == CPTLineDirectionHorizontal;
        if (horizontal)
        {
            _marker.anchorPlotPoint = [NSArray arrayWithObjects:[NSDecimalNumber notANumber], value, nil];
        }
        else {
            _marker.anchorPlotPoint = [NSArray arrayWithObjects:value, [NSDecimalNumber notANumber], nil];
        }
    }
    [self replaceValue:value forKey:@"value" notification:NO];
}

@end
