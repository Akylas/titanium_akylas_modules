//
//  AkylasGoogleMapRouteProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxView.h"

@interface AkylasMapBaseAnnotationProxy()
-(void)setShadow:(id)value;
-(void)setScaleLineDash:(id)arg;
-(void)setLineDash:(id)arg;
@end

@implementation RMRouteAnnotation : RMShapeAnnotation
@end

@implementation AkylasMapboxRouteProxy
{
    RMShape * _shape;
    RMAnnotation* _rmannotation;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_shape);
    RELEASE_TO_NIL(_rmannotation);
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Mapbox.Route";
}

-(void)setColor:(id)value
{
    [super setColor:value];
    if (_shape != nil)  {
        _shape.lineColor =_color;
    }
}


-(void)onPointProcessed
{
    RELEASE_TO_NIL(_shape)
}


-(void)onPointAdded:(CLLocation*)newPoint
{
    if (_shape) {
        [_shape addLineToCoordinate:newPoint.coordinate];
    }
}


-(void)setWidth:(id)value
{
    [super setWidth:value];

    if (_shape != nil)  {
        _shape.lineWidth =_lineWidth;
    }
}


-(void)setLineJoin:(id)value
{
    [super setLineJoin:value];

    if (_shape != nil)  {
        _shape.lineJoin =_lineJoin;
    }
}

-(void)setLineCap:(id)value
{
    [super setLineCap:value];

    if (_shape != nil)  {
        _shape.lineCap =_lineCap;
    }
}

-(void)setShadow:(id)value
{
    [super setShadow:value];
    NSShadow* shadow = [TiUtils shadowValue:value];
    if (_shape != nil)  {
        _shape.enableShadow = shadow != nil;
        if (shadow != nil) {
            _shape.shadowRadius = shadow.shadowBlurRadius;
            _shape.shadowOffset =shadow.shadowOffset;
            _shape.shadowColor = ((UIColor*)shadow.shadowColor).CGColor;
        }
    }
}

-(void)setScaleLineDash:(id)arg
{
    [super setScaleLineDash:arg];
    if (_shape != nil)  {
        _shape.scaleLineDash =[TiUtils boolValue:arg def:NO];
    }
}

-(void)setLineDash:(id)arg
{
    [super setLineDash:arg];
    
    if ([arg objectForKey:@"pattern"]) {
        NSArray* value  = [arg objectForKey:@"pattern"];
        if (_shape != nil)  {
            _shape.lineDashLengths = value;
        }
    }
    if ([arg objectForKey:@"phase"]) {
        if (_shape != nil)  {
            _shape.lineDashPhase = [TiUtils floatValue:[arg objectForKey:@"phase"]];
        }
    }
    [self replaceValue:arg forKey:@"lineDash" notification:NO];
}

-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView
{
    if (_rmannotation && _rmannotation.mapView != mapView) {
        RELEASE_TO_NIL(_rmannotation)
    }
    
    pthread_rwlock_rdlock(&routeLineLock);
    NSUInteger count = [_routeLine count];
    pthread_rwlock_unlock(&routeLineLock);
    
    if (count > 1 && _rmannotation == nil) {
        
        _rmannotation = [[RMRouteAnnotation alloc] initWithMapView:mapView points:_routeLine];
        _rmannotation.userInfo = self;
        _rmannotation.subtitle = [self subtitle];
        //        [_annotation setBoundingBoxFromLocations:_routeLine];
    }
    return _rmannotation;
}
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView
{
    if (_shape == nil) {
        _shape = [[RMShape alloc] initWithView:[mapView map]];
        NSArray*line = [self getRouteLine];
        [_shape performBatchOperations:^(RMShape *aShape) {
            _shape.lineCap = _lineCap;
            _shape.lineJoin = _lineJoin;
            _shape.lineColor = _color;
            _shape.lineWidth = _lineWidth;
            for (CLLocation *location in line) {
                [aShape addLineToCoordinate:location.coordinate];
            }
        }];
    }
    return _shape;
}


@end
