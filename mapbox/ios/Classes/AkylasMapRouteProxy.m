/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiUtils.h"
#import "AkylasMapRouteProxy.h"
#import "AkylasMapModule.h"
#import "AkylasMapView.h"
#import "AkylasMapMapboxView.h"
#import "AkylasMapMapView.h"



int lineCapFromString(NSString* value)
{
    if ([value isEqual:@"round" ])
        return kCGLineCapRound;
    else if ([value isEqual:@"square" ])
        return kCGLineCapSquare;
    else  return kCGLineCapButt;
}

int lineJoinFromString(NSString* value)
{
    if ([value isEqual:@"round" ])
        return kCGLineJoinRound;
    else if ([value isEqual:@"bevel" ])
        return kCGLineJoinBevel;
    else  return kCGLineJoinMiter;
}

@implementation RMRouteAnnotation : RMShapeAnnotation
@end

@implementation AkylasMapRouteProxy
{
    NSMutableArray *_routeLine;
    MKPolyline *_routePolyline;
    RMShape * _shape;
    UIColor * _color;
    NSString * _lineJoin;
    NSString * _lineCap;
    int _lineWidth;
    RMSphericalTrapezium _box;
	pthread_rwlock_t routeLineLock;
}

@synthesize routeLine = _routeLine;
@synthesize level, routeRenderer;

-(id)init
{
	if (self = [super init]) {
		pthread_rwlock_init(&routeLineLock, NULL);
        _lineWidth = 10;
        _color = [[UIColor blueColor] retain];
        _lineJoin = _lineCap = @"round";
    }
    return self;
}

-(void)dealloc
{
    pthread_rwlock_rdlock(&routeLineLock);
    RELEASE_TO_NIL(_routeLine);
    pthread_rwlock_unlock(&routeLineLock);
	pthread_rwlock_destroy(&routeLineLock);
    RELEASE_TO_NIL(_shape);
    RELEASE_TO_NIL(_color);
    RELEASE_TO_NIL(_lineJoin);
    RELEASE_TO_NIL(_lineCap);
    RELEASE_TO_NIL(_routePolyline);
    RELEASE_TO_NIL(routeRenderer);
	[super dealloc];
}

-(void)_initWithProperties:(NSDictionary*)properties
{
	[super _initWithProperties:properties];
}

-(NSString*)apiName
{
    return @"Akylas.Map.Route";
}

-(void)refreshIfNeeded
{
	@synchronized(self)
	{
		if (!needsRefreshing)
		{
			return; //Already done.
		}
		if (delegate!=nil && [delegate viewAttached])
		{
			[(AkylasMapView*)[delegate view] refreshAnnotation:self readd:needsRefreshingWithSelection];
		}
		needsRefreshing = NO;
		needsRefreshingWithSelection = NO;
	}
}



-(CLLocation*)processPoint:(id)point
{
    CLLocation* newPoint;
    if ([point isKindOfClass:[CLLocation class]])
    {
        newPoint = point;
    } else if ([point isKindOfClass:[NSDictionary class]]) {
        newPoint = [AkylasMapModule cllocationFromDict:point];
    } else if ([point isKindOfClass:[NSArray class]]) {
        newPoint = [AkylasMapModule cllocationFromArray:point];
    }
    if (newPoint) {
        pthread_rwlock_rdlock(&routeLineLock);
        [_routeLine addObject:newPoint];
        pthread_rwlock_unlock(&routeLineLock);
        [self updateBoundingBoxWithPoint:newPoint];
    }
    return newPoint;
}

-(void)processPoints:(NSArray*)points
{
    pthread_rwlock_rdlock(&routeLineLock);
    RELEASE_TO_NIL(_routeLine)
    _routeLine = [[NSMutableArray arrayWithCapacity:[points count]] retain];
    _box = ((RMSphericalTrapezium){.northEast = {.latitude = kRMMinLatitude, .longitude = kRMMinLongitude}, .southWest = {.latitude = kRMMaxLatitude, .longitude = kRMMaxLongitude}});
    for (id point in points) {
        [self processPoint:point];
    }
    NSUInteger count = [_routeLine count];
    pthread_rwlock_unlock(&routeLineLock);
//    if (_shape) {
//        [_shape setCoordinates:_routeLine];
//    }
//    else {
    RELEASE_TO_NIL(_shape)
    RELEASE_TO_NIL(routeRenderer)
    RELEASE_TO_NIL(_routePolyline)
    if (count > 1)[self setNeedsRefreshingWithSelection:YES];
//    }
    
}

-(void)updateBoundingBoxWithPoint:(CLLocation*) point {
    CLLocationDegrees currentLatitude = point.coordinate.latitude;
    CLLocationDegrees currentLongitude = point.coordinate.longitude;
    
    // POIs outside of the world...
    if (currentLatitude < kRMMinLatitude || currentLatitude > kRMMaxLatitude || currentLongitude < kRMMinLongitude || currentLongitude > kRMMaxLongitude)
        return;
    
    _box.northEast.latitude = fmax(currentLatitude, _box.northEast.latitude);
    _box.northEast.longitude = fmax(currentLongitude, _box.northEast.longitude);
    _box.southWest.latitude  = fmin(currentLatitude, _box.southWest.latitude);
    _box.southWest.longitude = fmin(currentLongitude, _box.southWest.longitude);
}


#pragma mark Public APIs

-(void)setPoints:(id)value
{
    ENSURE_TYPE(value, NSArray);
    [self processPoints:value];
}

-(void)addPoint:(id)value
{
    //it is always an array
    if (_routeLine == nil) {
        [self processPoints:value];
        return;
    }
    CLLocation* newPoint = [self processPoint:[value objectAtIndex:0]];
    if (newPoint == nil) return;
    pthread_rwlock_rdlock(&routeLineLock);
    NSUInteger count = [_routeLine count];
    pthread_rwlock_unlock(&routeLineLock);
    if (_shape) {
        [_shape addLineToCoordinate:newPoint.coordinate];
    }
    if (newPoint && count > 1){
        RELEASE_TO_NIL(routeRenderer)
        RELEASE_TO_NIL(_routePolyline)
        if (routeRenderer) [self setNeedsRefreshingWithSelection:YES];
    }
}

-(void)setColor:(id)value
{
    RELEASE_TO_NIL(_color);
    _color = [[TiUtils colorValue:value].color retain];
	[self replaceValue:value forKey:@"color" notification:NO];
    if (_shape != nil)  {
        _shape.lineColor =_color;
    }
    if (routeRenderer != nil) {
        routeRenderer.fillColor = routeRenderer.strokeColor = _color;
    }
}

- (id)color
{
    return [self valueForUndefinedKey:@"color"];
}

-(void)setWidth:(id)value
{
	[self replaceValue:value forKey:@"width" notification:NO];
    _lineWidth = [TiUtils floatValue:value def:3.0];
    if (_shape != nil)  {
        _shape.lineWidth =_lineWidth;
    }
    if (routeRenderer != nil) {
        routeRenderer.lineWidth = _lineWidth;
    }
}

-(void)setLineJoin:(id)value
{
	[self replaceValue:value forKey:@"lineJoin" notification:NO];
    _lineJoin = [[TiUtils stringValue:value] retain];
    if (_shape != nil)  {
        _shape.lineJoin =_lineJoin;
    }
    if (routeRenderer != nil) {
        routeRenderer.lineJoin = lineJoinFromString(_lineJoin);
    }
}

-(void)setLineCap:(id)value
{
	[self replaceValue:value forKey:@"lineCap" notification:NO];
    _lineCap = [[TiUtils stringValue:value] retain];
    if (_shape != nil)  {
        _shape.lineCap =_lineCap;
    }
    if (routeRenderer != nil) {
        routeRenderer.lineCap = lineCapFromString(_lineCap);
    }
}


-(CLLocationCoordinate2D)coordinate
{
    pthread_rwlock_rdlock(&routeLineLock);
    CLLocationCoordinate2D coord = ((CLLocation *)[_routeLine objectAtIndex:0]).coordinate;
    pthread_rwlock_unlock(&routeLineLock);
	return coord;
}


-(id)region
{
    if (_box.southWest.latitude - _box.northEast.latitude != 0 &&
        _box.southWest.longitude - _box.northEast.longitude != 0) {
        return [AkylasMapModule dictFromRegion:_box];
    }
    return nil;
}

-(NSArray*)getRouteLine
{
    pthread_rwlock_rdlock(&routeLineLock);
    NSArray* copy = [_routeLine mutableCopy];
	pthread_rwlock_unlock(&routeLineLock);
	return ((copy != nil) ? [copy autorelease] : [NSMutableArray array]);
}


#pragma mark MapBox

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
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapMapboxView*)mapView
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

#pragma mark Native Map

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

-(id <AkylasMKOverlayPathUniversal>)rendererForMapView:(AkylasMapMapView*)mapView
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
