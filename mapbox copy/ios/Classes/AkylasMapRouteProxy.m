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

#import <GoogleMaps/GoogleMaps.h>


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
    RMShape * _shape;
    UIColor * _color;
    NSArray * _spans;
    NSString * _lineJoin;
    NSString * _lineCap;
    int _lineWidth;
    RMSphericalTrapezium _box;
	pthread_rwlock_t routeLineLock;
    
    //NativeMap
    MKPolyline *_routePolyline;
    
    //GoogleMap
    GMSPolyline* _gPoly;
    GMSMutablePath *_gPath;
}

@synthesize routeLine = _routeLine;
@synthesize box = _box;
@synthesize level, routeRenderer;

-(id)init
{
	if (self = [super init]) {
		pthread_rwlock_init(&routeLineLock, NULL);
        _lineWidth = 10;
        _color = [[UIColor blueColor] retain];
        _lineJoin = _lineCap = @"round";
        _box = ((RMSphericalTrapezium){.northEast = {.latitude = kRMMinLatitude, .longitude = kRMMinLongitude}, .southWest = {.latitude = kRMMaxLatitude, .longitude = kRMMaxLongitude}});
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
    
    RELEASE_TO_NIL(_gPath);
    RELEASE_TO_NIL(_gPoly);
    RELEASE_TO_NIL(_spans);

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
    _box = ((RMSphericalTrapezium){.northEast = {.latitude = kRMMinLatitude, .longitude = kRMMinLongitude}, .southWest = {.latitude = kRMMaxLatitude, .longitude = kRMMaxLongitude}});
    
    
    if (!points) {
        pthread_rwlock_unlock(&routeLineLock);
        RELEASE_TO_NIL(_shape)
        RELEASE_TO_NIL(routeRenderer)
        RELEASE_TO_NIL(_routePolyline)
        return;
    }
    _routeLine = [[NSMutableArray arrayWithCapacity:[points count]] retain];
    
    for (id point in points) {
        [self processPoint:point];
    }
    NSUInteger count = [_routeLine count];
    pthread_rwlock_unlock(&routeLineLock);
    RELEASE_TO_NIL(_shape)
    RELEASE_TO_NIL(routeRenderer)
    RELEASE_TO_NIL(_routePolyline)
    RELEASE_TO_NIL(_gPath)
    if (count > 1)[self setNeedsRefreshingWithSelection:YES];
}

-(RMSphericalTrapezium) box
{
    RMSphericalTrapezium result;
    pthread_rwlock_rdlock(&routeLineLock);
    result = _box;
    pthread_rwlock_unlock(&routeLineLock);
    return result;
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
    if (_gPath) {
        [_gPath addCoordinate:newPoint.coordinate];
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
    if (_gPoly != nil && !_spans)  {
        _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
    }
    if (routeRenderer != nil) {
        routeRenderer.fillColor = routeRenderer.strokeColor = _color;
    }
}

-(void)setSpans:(id)value
{
    [self replaceValue:value forKey:@"spans" notification:NO];
    ENSURE_TYPE(value, NSArray)
    if (!value) {
        RELEASE_TO_NIL(_spans)
        if (_gPoly != nil)  {
            _gPoly.spans = nil;
        }
    }
    NSMutableArray* spans = [NSMutableArray arrayWithCapacity:[value count]];
    [value enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if (IS_OF_CLASS(obj, NSArray)) {
            NSUInteger length = [obj count];
            if (length > 0) {
                UIColor* color1 = [[TiUtils colorValue:[obj objectAtIndex:0]] _color];
                UIColor* color2 = nil;
                NSUInteger segments = 0;
                if (length > 2) {
                    color2 = [[TiUtils colorValue:[obj objectAtIndex:1]] _color];
                    segments = [TiUtils intValue:[obj objectAtIndex:2]];
                } else if (length > 1) {
                    color2 = [[TiUtils colorValue:[obj objectAtIndex:1]] _color];
                    if (color2 == nil) {
                        segments = [TiUtils intValue:[obj objectAtIndex:1]];
                    }
                }
                GMSStrokeStyle *style;
                if (color2) {
                    style = [GMSStrokeStyle gradientFromColor:color1 toColor:color2];
                } else {
                    style = [GMSStrokeStyle solidColor:color1];
                }
                if (segments > 0) {
                    [spans addObject:[GMSStyleSpan spanWithStyle:style segments:segments]];
                } else {
                    [spans addObject:[GMSStyleSpan spanWithStyle:style]];
                }
            }
        }
    }];
    _spans = [spans retain];
    if (_gPoly != nil)  {
        _gPoly.spans = _spans;
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
    if (_gPoly != nil)  {
        _gPoly.strokeWidth =_lineWidth;
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

-(void)setShadow:(id)value
{
    [self replaceValue:value forKey:@"shadow" notification:NO];
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
    [self replaceValue:arg forKey:@"scaleLineDash" notification:NO];
    if (_shape != nil)  {
        _shape.scaleLineDash =[TiUtils boolValue:arg def:NO];
    }
}

-(void)setLineDash:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    
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


#pragma mark GoogleMap
+(int)gZIndexDelta {
    return 30;
}
-(GMSMutablePath *) getGPath {
    if (_gPath == nil) {
        _gPath =  [[GMSMutablePath alloc] init];
        pthread_rwlock_rdlock(&routeLineLock);
        NSUInteger count = [_routeLine count];
        for (int i = 0; i < count; ++i) {
            CLLocation* entry = [_routeLine objectAtIndex:i];
            [_gPath addCoordinate:entry.coordinate];
        }
        pthread_rwlock_unlock(&routeLineLock);
    }
    return _gPath;
}

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView
{
    if (_gPoly == nil) {
        _gPoly = [GMSPolyline polylineWithPath:[self getGPath]];
        if (_spans) {
            _gPoly.spans = _spans;
        } else {
            _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
        }
        _gPoly.strokeWidth = _lineWidth;
    }
    else if (_gPoly.map != mapView) {
        RELEASE_TO_NIL(_gPoly)
        return [self getGOverlayForMapView:mapView];
    }
    return _gPoly;
}


-(GMSOverlay*)gOverlay
{
    return _gPoly;
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
