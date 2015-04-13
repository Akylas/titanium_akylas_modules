//
//  AkylasGoogleMapRouteProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapRouteProxy.h"

@implementation AkylasMapRouteProxy
{
    MKPolyline *_routePolyline;
}
@synthesize level, routeRenderer;

-(void)dealloc
{
    RELEASE_TO_NIL(_routePolyline);
    RELEASE_TO_NIL(routeRenderer);
    
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Route";
}

-(void)setColor:(id)value
{
    [super setColor:value];
    if (routeRenderer != nil) {
        routeRenderer.fillColor = routeRenderer.strokeColor = _color;
    }
}


-(void)onPointProcessed
{
    RELEASE_TO_NIL(routeRenderer)
    RELEASE_TO_NIL(_routePolyline)
}


-(void)onPointAdded:(CLLocation*)newPoint
{
    if (newPoint){
        RELEASE_TO_NIL(routeRenderer)
        RELEASE_TO_NIL(_routePolyline)
        if (routeRenderer) [self setNeedsRefreshingWithSelection:YES];
    }
}


-(void)setWidth:(id)value
{
    [super setWidth:value];

    if (routeRenderer != nil) {
        routeRenderer.lineWidth = _lineWidth;
    }
}

-(void)setLineJoin:(id)value
{
    [super setLineJoin:value];
    if (routeRenderer != nil) {
        routeRenderer.lineJoin = lineJoinFromString(_lineJoin);
    }
}

-(void)setLineCap:(id)value
{
    [super setLineCap:value];
    if (routeRenderer != nil) {
        routeRenderer.lineCap = lineCapFromString(_lineCap);
    }
}


-(void)setLevel:(id)value
{
    [self replaceValue:value forKey:@"level" notification:NO];
    // level is not supported before iOS 7 but it doesn't hurt to capture it.
    level = [[TiUtils numberFromObject:value] unsignedIntegerValue];
}

-(MKPolyline*) getPolyline {
    if (_routePolyline == nil) {
        NSArray*line = [self getRouteLine];
        NSUInteger count = [line count];
        MKMapPoint* pointArray = malloc(sizeof(CLLocationCoordinate2D) * count);
        for (int i = 0; i < count; ++i) {
            CLLocation* entry = [line objectAtIndex:i];
            MKMapPoint pt = MKMapPointForCoordinate(entry.coordinate);
            pointArray[i] = pt;
        }
        _routePolyline =  [[MKPolyline polylineWithPoints:pointArray count:count] retain];
        free(pointArray);
    }
    return _routePolyline;
}

-(id <AkylasMKOverlayPathUniversal>)rendererForMapView:(AkylasMapView*)mapView
{
    if (routeRenderer == nil) {
        Class rendererClass = [MKPolylineRenderer class];
        routeRenderer = [(id <AkylasMKOverlayPathUniversal>)[[rendererClass alloc] initWithPolyline:[self getPolyline]] retain];
        routeRenderer.lineCap = lineCapFromString(_lineCap);
        routeRenderer.lineJoin = lineJoinFromString(_lineJoin);
        routeRenderer.strokeColor = routeRenderer.fillColor = _color;
        routeRenderer.fillColor = nil;
        routeRenderer.lineWidth = _lineWidth;
    }
    return routeRenderer;
}


@end
