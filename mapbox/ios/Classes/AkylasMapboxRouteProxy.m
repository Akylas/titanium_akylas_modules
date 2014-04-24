/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiUtils.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxModule.h"
#import "AkylasMapboxView.h"

@implementation AkylasMapboxRouteProxy
{
    NSArray *_routeLine;
    RMShape * _shape;
    UIColor * _color;
    int _lineWidth;
}

@synthesize routeLine = _routeLine;

-(id)init
{
	if (self = [super init]) {
        _lineWidth = 3;
        _color = [[UIColor blueColor] retain];
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_routeLine);
    RELEASE_TO_NIL(_shape);
	[super dealloc];
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    if ([properties objectForKey:@"points"] == nil) {
        [self throwException:@"missing required points property" subreason:nil location:CODELOCATION];
    }
    
	[super _initWithProperties:properties];
}

-(NSString*)apiName
{
    return @"Akylas.Mapbox.Route";
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
			[(AkylasMapboxView*)[delegate view] refreshAnnotation:self readd:needsRefreshingWithSelection];
		}
		needsRefreshing = NO;
		needsRefreshingWithSelection = NO;
	}
}

-(void)processPoints:(NSArray*)points
{
//    if (routeLine != nil) {
//        NSLog(@"[WARN] Route points can not be changed after route creation.");
//        return;
//    }
//    
    // Construct the MKPolyline
    NSMutableArray* result = [NSMutableArray arrayWithCapacity:[points count]];
    for (id point in points) {
        CLLocation* newPoint;
        if ([point isKindOfClass:[CLLocation class]])
        {
            newPoint = point;
        } else if ([point isKindOfClass:[NSDictionary class]]) {
            newPoint = [AkylasMapboxModule cllocationFromDict:point];
        }
        if (newPoint) {
            [result addObject:newPoint];
        }
    }
    RELEASE_TO_NIL(_routeLine)
    _routeLine = [[NSArray arrayWithArray:result] retain];
    
    if (_annotation) {
        [_annotation setBoundingBoxFromLocations:_routeLine];
        _annotation.layer = nil;
        RELEASE_TO_NIL(_shape)
        [self setNeedsRefreshingWithSelection:YES];
    }
}

#pragma mark Public APIs

-(void)setPoints:(id)value
{
    ENSURE_TYPE(value, NSArray);
    if (![value count]) {
        [self throwException:@"missing required points data" subreason:nil location:CODELOCATION];
    }
    
    [self processPoints:value];
}

-(void)setColor:(id)value
{
    RELEASE_TO_NIL(_color);
    _color = [[TiUtils colorValue:value].color retain];
	[self replaceValue:value forKey:@"pincolor" notification:NO];
    if (_shape != nil)  {
        _shape.lineColor =_color;
        [_shape setNeedsDisplay];
    }
}

-(void)setWidth:(id)value
{
//    width = [TiUtils floatValue:value];
}

-(void)setLevel:(id)value
{
    // level is not supported before iOS 7 but it doesn't hurt to capture it.
    if (![TiUtils isIOS7OrGreater]) {
        [AkylasMapboxModule logAddedIniOS7Warning:@"level"];
    }
//    level = [[TiUtils numberFromObject:value] unsignedIntegerValue];
}

-(CLLocationCoordinate2D)coordinate
{
    CLLocationCoordinate2D coord = ((CLLocation *)[_routeLine objectAtIndex:0]).coordinate;
	return coord;
}

-(RMAnnotation*)getAnnotationForMapView:(RMMapView*)mapView
{
    if (_annotation == nil) {
        
        _annotation = [[RMAnnotation alloc] initWithMapView:mapView coordinate:self.coordinate andTitle:[self title]];
        _annotation.userInfo = self;
        _annotation.subtitle = [self subtitle];
        [_annotation setBoundingBoxFromLocations:_routeLine];
    }
    else if (_annotation.mapView != mapView) {
        RELEASE_TO_NIL(_annotation)
        return [self getAnnotationForMapView:mapView];
    }
    return _annotation;
}


-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView
{
    if (_shape == nil) {
        _shape = [[RMShape alloc] initWithView:_annotation.mapView];
        _shape.lineColor = _color;
        _shape.lineWidth = _lineWidth;
       
        for (CLLocation *location in _routeLine)
            [_shape addLineToCoordinate:location.coordinate];
    }
    return _shape;
}

@end
