    /**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapMapView.h"
#import "TiUtils.h"
#import "AkylasMapModule.h"
#import "AkylasMapAnnotationProxy.h"
#import "AkylasMapPinAnnotationView.h"
#import "AkylasMapImageAnnotationView.h"
#import "AkylasMapCustomAnnotationView.h"
#import "AkylasMapRouteProxy.h"



@interface MKMapView (ZoomLevel)
- (void)setCenterCoordinate:(CLLocationCoordinate2D)centerCoordinate
                  zoomLevel:(NSUInteger)zoomLevel
                   animated:(BOOL)animated;

-(double) getZoomLevel;
- (void)setZoomLevel:(NSUInteger)zoomLevel animated:(BOOL)animated;
@end



@implementation MKMapView (ZoomLevel)

- (void)setCenterCoordinate:(CLLocationCoordinate2D)centerCoordinate
                  zoomLevel:(NSUInteger)zoomLevel animated:(BOOL)animated {
    if (self.frame.size.width != 0 && self.frame.size.height !=0) {
        MKCoordinateSpan span = MKCoordinateSpanMake(0, 360/pow(2, zoomLevel)*self.frame.size.width/256);
        [self setRegion:MKCoordinateRegionMake(centerCoordinate, span) animated:animated];
    }
}

- (void)setZoomLevel:(NSUInteger)zoomLevel animated:(BOOL)animated {
    if (self.frame.size.width != 0 && self.frame.size.height !=0) {
        [self setCenterCoordinate:[self region].center zoomLevel:zoomLevel animated:animated];
    }
}

-(double) getZoomLevel {
    return log2(360 * ((self.frame.size.width/256) / self.region.span.longitudeDelta));
}

@end

@implementation AkylasMapMapView
{
    CGFloat _minZoom;
    CGFloat _maxZoom;
    SMCalloutView* _calloutView;
    UIView* calloutTouchedView;
    BOOL _cameraAnimating;
    MKMapCamera* _pendingCamera;
    CGFloat _lastCameraHeading;
}

#pragma mark Internal

- (id)init
{
    if ((self = [super init])) {
        _minZoom = 0;
        _maxZoom = 100;
        _cameraAnimating = NO;
        _pendingCamera = nil;
        _lastCameraHeading = 0.0f;
    }
    return self;
}

-(void)dealloc
{
	if (map!=nil)
	{
		map.delegate = nil;
		RELEASE_TO_NIL(map);
	}
    if (_calloutView) {
        _calloutView.delegate = nil;
        RELEASE_TO_NIL(_calloutView)
    }
    if (mapLine2View) {
        CFRelease(mapLine2View);
        mapLine2View = nil;
    }
	[super dealloc];
}

/**
 * Given a MKMapRect, this returns the zoomLevel based on
 * the longitude width of the box.
 *
 * This is because the Mercator projection, when tiled,
 * normally operates with 2^zoomLevel tiles (1 big tile for
 * world at zoom 0, 2 tiles at 1, 4 tiles at 2, etc.)
 * and the ratio of the longitude width (out of 360º)
 * can be used to reverse this.
 *
 * This method factors in screen scaling for the iPhone 4:
 * the tile layer will use the *next* zoomLevel. (We are given
 * a screen that is twice as large and zoomed in once more
 * so that the "effective" region shown is the same, but
 * of higher resolution.)
 */
- (NSUInteger)zoomLevelForMapRect:(MKMapRect)mapRect {
    MKCoordinateRegion r = MKCoordinateRegionForMapRect(mapRect);
    CGFloat lon_ratio = r.span.longitudeDelta/360.0;
    NSUInteger z = (NSUInteger)(log(1/lon_ratio)/log(2.0)-1.0);
    
    z += ([[UIScreen mainScreen] scale] - 1.0);
    return z;
}
/**
 * Similar to above, but uses a MKZoomScale to determine the
 * Mercator zoomLevel. (MKZoomScale is a ratio of screen points to
 * map points.)
 */
- (CGFloat)zoomLevelForZoomScale:(MKZoomScale)zoomScale {
    CGFloat realScale = zoomScale / [[UIScreen mainScreen] scale];
    CGFloat z = (log(realScale)/log(2.0)+20.0);
    
    z += ([[UIScreen mainScreen] scale] - 1.0);
    return z;
}

/**
 * Similar to above, but uses a MKZoomScale to determine the
 * Mercator zoomLevel. (MKZoomScale is a ratio of screen points to
 * map points.)
 */
- (MKZoomScale)zoomScaleForZoomLevel:(CGFloat)zoomLevel {

    zoomLevel -= ([[UIScreen mainScreen] scale] - 1.0);
    CGFloat realScale = exp((zoomLevel-20.0)*log(2.0));
    CGFloat zoomScale = realScale * [[UIScreen mainScreen] scale];
    
    return zoomScale;
}
/**
 * Shortcut to determine the number of tiles wide *or tall* the
 * world is, at the given zoomLevel. (In the Spherical Mercator
 * projection, the poles are cut off so that the resulting 2D
 * map is "square".)
 */
- (NSUInteger)worldTileWidthForZoomLevel:(NSUInteger)zoomLevel {
    return (NSUInteger)(pow(2,zoomLevel));
}


MKCoordinateRegion mkregionFromRegion(RMSphericalTrapezium trapez)
{
    return (MKCoordinateRegion){
        .center = RMSphericalTrapeziumCenter(trapez),
        .span = {
            .latitudeDelta = trapez.northEast.latitude - trapez.southWest.latitude,
            .longitudeDelta = trapez.northEast.longitude - trapez.southWest.longitude
        }
    };
}

RMSphericalTrapezium regionFromMKRegion(MKCoordinateRegion mkregion)
{
    CLLocationCoordinate2D center = mkregion.center;
    CLLocationDegrees latitudeDelta_2 = mkregion.span.latitudeDelta/2.0;
    CLLocationDegrees longitudeDelta_2 = mkregion.span.longitudeDelta/2.0;
    return (RMSphericalTrapezium){
        .northEast = {
            .latitude =  center.latitude + latitudeDelta_2,
            .longitude =  center.longitude + longitudeDelta_2
        },
        .southWest = {
            .latitude =  center.latitude - latitudeDelta_2,
            .longitude =  center.longitude - longitudeDelta_2
        }
    };
}


BOOL MKCoordinateRegionIsValid(MKCoordinateRegion mkregion)
{

    return mkregion.span.latitudeDelta > 0 && mkregion.span.longitudeDelta >0 &&
    CLLocationCoordinate2DIsValid(mkregion.center);
}

-(void)render
{
    if (![NSThread isMainThread]) {
        TiThreadPerformOnMainThread(^{[self render];}, NO);
        return;
    }
    //TIMOB-10892 if any of below conditions is true , regionthatfits returns invalid.
    if (map == nil || map.bounds.size.width == 0 || map.bounds.size.height == 0) {
        return;
    }

    if (REGION_VALID(region))
    {
        if (regionFits) {
            [map setRegion:[map regionThatFits:mkregionFromRegion(region)] animated:animate];
        }
        else {
            [map setRegion:mkregionFromRegion(region) animated:animate];
        }
    }
}

-(MKMapView*)map
{
    if (map==nil)
    {
        map = [[MKMapView alloc] initWithFrame:CGRectZero];
        map.delegate = self;
        map.userInteractionEnabled = YES;
        map.showsUserLocation = YES; // defaults
        map.autoresizingMask = UIViewAutoresizingNone;
        [self addSubview:map];
        mapLine2View = CFDictionaryCreateMutable(NULL, 10, &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks);
        //Initialize loaded state to YES. This will automatically go to NO if the map needs to download new data
        loaded = YES;
    }
    return map;
}

- (id)accessibilityElement
{
	return [self map];
}

- (NSArray *)customAnnotations
{
    NSMutableArray *annotations = [NSMutableArray arrayWithArray:self.map.annotations];
    [annotations removeObject:self.map.userLocation];
    return annotations;
}

-(void)setBounds:(CGRect)bounds
{
    //TIMOB-13102.
    //When the bounds change the mapview fires the regionDidChangeAnimated delegate method
    //Here we update the region property which is not what we want.
    //Instead we set a forceRender flag and render in frameSizeChanged and capture updated
    //region there.
    CGRect oldBounds = (map != nil) ? [map bounds] : CGRectZero;
    forceRender = (oldBounds.size.width == 0 || oldBounds.size.height==0);
    ignoreRegionChanged = YES;
    [super setBounds:bounds];
    ignoreRegionChanged = NO;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    MKCoordinateRegion visibleRegion = [map region];
    [[self map] setFrame:bounds];
    if (MKCoordinateRegionIsValid(visibleRegion)) {
        [map setRegion:[map regionThatFits:visibleRegion]];
    }
    [super frameSizeChanged:frame bounds:bounds];
    if (forceRender) {
        //Set this to NO so that region gets captured.
        ignoreRegionChanged = NO;
        [self render];
        forceRender = NO;
    }
}

-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn
{
	NSArray *selected = map.selectedAnnotations;
	BOOL wasSelected = [selected containsObject:proxy]; //If selected == nil, this still returns FALSE.
    ignoreClicks = YES;
	if (yn==NO)
	{
		[map deselectAnnotation:proxy animated:NO];
	}
	else
	{
		[map removeAnnotation:proxy];
		[map addAnnotation:proxy];
		[map setNeedsLayout];
	}
	if (wasSelected)
	{
		[map selectAnnotation:proxy animated:NO];
	}
    ignoreClicks = NO;
}

-(void)internalAddAnnotations:(id)annotations atIndex:(NSInteger)index
{
    MKMapView* mapView = [self map];
    if (IS_OF_CLASS(annotations, NSArray)) {
        [mapView addAnnotations:[annotations filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(id object, NSDictionary *bindings) {
            return [object isKindOfClass:[AkylasMapAnnotationProxy class]];
        }]]];
    }
    else {
        [mapView addAnnotation:annotations];
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    MKMapView* mapView = [self map];
    if (IS_OF_CLASS(annotations, NSArray)) {
        [mapView removeAnnotations:[annotations filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(id object, NSDictionary *bindings) {
            return [object isKindOfClass:[AkylasMapAnnotationProxy class]];
        }]]];
    }
    else if ([annotations isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        [mapView removeAnnotation:(AkylasMapAnnotationProxy*)annotations];
    }
}


-(void)internalRemoveAllAnnotations
{
    MKMapView* mapView = [self map];
    [mapView removeAnnotations:mapView.annotations];
}


-(void)internalAddRoutes:(id)routes atIndex:(NSInteger)index
{
    MKMapView* mapView = [self map];
    __block NSInteger realIndex = index;
    if (realIndex == -1) {
        realIndex = INT_MAX;
    }
    if (IS_OF_CLASS(routes, NSArray)) {
        [routes enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            if (IS_OF_CLASS(obj, AkylasMapRouteProxy)) {
                MKPolyline *routeLine = [obj getPolyline];
                
                CFDictionaryAddValue(mapLine2View, routeLine, obj);
                [self addOverlay:routeLine index:realIndex level:[obj level]];
            }
        }];
    }
    else if(IS_OF_CLASS(routes, AkylasMapRouteProxy)){
        MKPolyline *routeLine = [routes getPolyline];
        
        CFDictionaryAddValue(mapLine2View, routeLine, routes);
        [self addOverlay:routeLine index:realIndex level:[routes level]];
    }
}

-(void)internalRemoveRoutes:(id)routes
{
    MKMapView* mapView = [self map];
    if (IS_OF_CLASS(routes, NSArray)) {
        [routes enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            if (IS_OF_CLASS(obj, AkylasMapRouteProxy)) {
                MKPolyline *routeLine = [obj getPolyline];
                CFDictionaryRemoveValue(mapLine2View, routeLine);
                [map removeOverlay:routeLine];
            }
        }];
    }
    else if(IS_OF_CLASS(routes, AkylasMapRouteProxy)){
        MKPolyline *routeLine = [routes getPolyline];
        CFDictionaryRemoveValue(mapLine2View, routeLine);
        [map removeOverlay:routeLine];
    }
}


-(void)internalRemoveAllRoutes
{
    MKMapView* mapView = [self map];
    [mapView removeOverlays:[((__bridge NSDictionary*)mapLine2View) allValues]];
    CFDictionaryRemoveAllValues(mapLine2View);
}


-(void)addRoute:(AkylasMapRouteProxy*)route
{
    MKPolyline *routeLine = [route getPolyline];
    
    CFDictionaryAddValue(mapLine2View, routeLine, route);
    [self addOverlay:routeLine level:[route level]];
}

-(void)removeRoute:(AkylasMapRouteProxy*)route
{
    MKPolyline *routeLine = [route getPolyline];
    CFDictionaryRemoveValue(mapLine2View, routeLine);
    [map removeOverlay:routeLine];
}

#pragma mark Public APIs


-(void)setSelectedAnnotation:(AkylasMapAnnotationProxy*)annotation
{
    [[self map] selectAnnotation:annotation animated:animate];
}

-(void)selectAnnotation:(id)args
{
	ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
	ENSURE_UI_THREAD(selectAnnotation,args);
	
	if (args == nil) {
		for (id<MKAnnotation> annotation in [[self map] selectedAnnotations]) {
			[[self map] deselectAnnotation:annotation animated:animate];
		}
		return;
	}
	
	if ([args isKindOfClass:[AkylasMapAnnotationProxy class]])
	{
		[self setSelectedAnnotation:args];
	}
}

-(void)deselectAnnotation:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(deselectAnnotation,args);

	if ([args isKindOfClass:[NSString class]])
	{
		// for pre 0.9, we supporting selecting by passing the annotation title
		NSString *title = [TiUtils stringValue:args];
		for (id<MKAnnotation>an in [NSArray arrayWithArray:[self map].annotations])
		{
			if ([title isEqualToString:an.title])
			{
				[[self map] deselectAnnotation:an animated:animate];
				break;
			}
		}
	}
	else if ([args isKindOfClass:[AkylasMapAnnotationProxy class]])
	{
		[[self map] deselectAnnotation:args animated:animate];
	}
}

-(void)selectUserAnnotation
{
    [[self map] selectAnnotation:[self map].userLocation animated:animate];
}

-(RMSphericalTrapezium) getCurrentRegion
{
    return regionFromMKRegion([[self map] region]);
}

-(void)zoomTo:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(zoomTo,args);

	double v = [TiUtils doubleValue:args];
	// TODO: Find a good delta tolerance value to deal with floating point goofs
	if (v == 0.0) {
		return;
	}
	MKCoordinateRegion _region = [[self map] region];
	// TODO: Adjust zoom factor based on v
	if (v > 0)
	{
		_region.span.latitudeDelta = _region.span.latitudeDelta / 2.0002;
		_region.span.longitudeDelta = _region.span.longitudeDelta / 2.0002;
	}
	else
	{
		_region.span.latitudeDelta = _region.span.latitudeDelta * 2.0002;
		_region.span.longitudeDelta = _region.span.longitudeDelta * 2.0002;
	}
	region = regionFromMKRegion(_region);
	[self render];
}

-(void)setCamera:(MKMapCamera*)camera animated:(BOOL)animated {
    if (_cameraAnimating) {
        RELEASE_TO_NIL(_pendingCamera)
        _pendingCamera = [camera retain];
        return;
    }
    
    [CATransaction begin];
    if (animated) {
        CGFloat animationDuration = 0.3;
        [CATransaction setAnimationDuration:animationDuration];
        [UIView beginAnimations:nil context:nil];
        [UIView setAnimationCurve:UIViewAnimationCurveLinear];
        [UIView setAnimationDuration:animationDuration];
        [UIView setAnimationBeginsFromCurrentState:YES];
        _cameraAnimating = YES;
    } else {
        [CATransaction setDisableActions:YES];
    }

    [map setCamera:camera animated:animated];

    if (animated) {
        [UIView commitAnimations];
    }
    [CATransaction commit];
}


-(void)updateCamera:(id)args
{
    if (map == nil || map.bounds.size.width == 0 || map.bounds.size.height == 0) {
        [self.proxy applyProperties:args];
        return;
    }
    if (_cameraAnimating) return;
    ENSURE_UI_THREAD(updateCamera,args);
    
    MKMapCamera *newCamera = [[map camera] copy];
//    MKCoordinateRegion currentRegion = newCamera.centerCoordinate;
    
    CGFloat altitude = newCamera.altitude;
    if ([args objectForKey:@"centerCoordinate"]) {
//        currentRegion.center = ;
        [newCamera setCenterCoordinate:[AkylasMapModule locationFromObject:[args objectForKey:@"centerCoordinate"]]];
    }
    if ([args objectForKey:@"altitude"]) {
        [newCamera setAltitude:[TiUtils floatValue:[args objectForKey:@"altitude"] def:0.0f]];
    } else if ([args objectForKey:@"zoom"]) {
        CLLocationDistance altitude = newCamera.altitude;
        MKZoomScale newZoomScale = [self zoomScaleForZoomLevel:[TiUtils floatValue:[args objectForKey:@"zoom"] def:1.0f]];
        MKZoomScale currentZoomScale = map.bounds.size.width / map.visibleMapRect.size.width;
        altitude = altitude* currentZoomScale / newZoomScale;
        [newCamera setAltitude:altitude];
    }
    if ([args objectForKey:@"tilt"]) {
        CGFloat pitch = MIN([TiUtils floatValue:[args objectForKey:@"tilt"] def:0.0f], 80);
        if (pitch != newCamera.pitch) {
            [newCamera setPitch:pitch];
        }
    }
    if ([args objectForKey:@"bearing"]) {
        CGFloat heading = [TiUtils floatValue:[args objectForKey:@"bearing"] def:0.0f];
        CGFloat shortestDelta = MIN(heading - _lastCameraHeading, (360 - heading) - _lastCameraHeading);
        heading = _lastCameraHeading + shortestDelta;
        [newCamera setHeading:heading];
        _lastCameraHeading = heading;
    }
    if (map == nil || map.bounds.size.width == 0 || map.bounds.size.height == 0) {
        return;
    }
    BOOL animating  = animate;
    if ([args objectForKey:@"animate"]) {
        animating = [TiUtils boolValue:[args objectForKey:@"bearing"] def:animate];
    }
    
    [self setCamera:newCamera animated:animating];
    
    if ([args objectForKey:@"region"]) {
        region = [AkylasMapModule regionFromDict:[args objectForKey:@"region"]];
        BOOL oldAnimate= animate;
        animate = animating;
        [self render];
        animate = oldAnimate;
    }
    [newCamera release];
}

#pragma mark Public APIs

-(void)setMapType_:(id)value
{
	[[self map] setMapType:[TiUtils intValue:value]];
}


//-(id)getRegion
//{
//    return [AkylasMapModule dictFromRegion:[self getCurrentRegion]];
//}

-(id)zoom_
{
    return @([map getZoomLevel]);
}


-(void)setRegion_:(id)value
{
	if (value==nil)
	{
	}
	else 
	{
		region = [AkylasMapModule regionFromDict:value];
		[self render];
	}
}

-(void)setAnimate_:(id)value
{
	animate = [TiUtils boolValue:value];
}

-(void)setUserLocationEnabled_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSObject);
    TiThreadPerformOnMainThread(^{
        [self map].showsUserLocation = [TiUtils boolValue:value];
    }, NO);
}


-(id)userLocationEnabled_
{
    return NUMINT([self map].showsUserLocation);
}

-(NSDictionary*)dictFromUserLocation:(MKUserLocation*)userLocation
{
    if (!userLocation) return @{};
    NSDictionary* result = [AkylasMapModule dictFromLocation:userLocation.location] ;
    if (userLocation.heading) {
        NSMutableDictionary* mutDict = [NSMutableDictionary dictionaryWithDictionary:result];
        [mutDict setObject:[AkylasMapModule dictFromHeading:userLocation.heading] forKey:@"heading"];
        result = mutDict;
    }
    return result;
}

-(id)userLocation_
{
    return [self dictFromUserLocation:[self map].userLocation];
}

-(void)setUserTrackingMode_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
    TiThreadPerformOnMainThread(^{
        [[self map] setUserTrackingMode:[TiUtils intValue:value def:MKUserTrackingModeNone] animated:animate];
    }, NO);
}

-(id)userTrackingMode_
{
    return NUMINT([self map].userTrackingMode);
}

-(void)setZoomInternal:(CGFloat)zoom {
    zoom = MAX(MIN(zoom, _maxZoom), _minZoom);
    [[self map] setZoomLevel:zoom animated:animate];
}

-(void)setZoom_:(id)zoom
{
	ENSURE_SINGLE_ARG(zoom,NSNumber);
    [self setZoomInternal:[TiUtils floatValue:zoom]];
}

-(void)setMinZoom_:(id)zoom
{
	ENSURE_SINGLE_ARG(zoom,NSNumber);
    _minZoom = [TiUtils floatValue:zoom];
    [self setZoomInternal:_internalZoom];
}

-(void)setMaxZoom_:(id)zoom
{
	ENSURE_SINGLE_ARG(zoom,NSNumber);
    _minZoom = [TiUtils floatValue:zoom];
    [self setZoomInternal:_internalZoom];
}

- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated
{
    MKMapView* mapView = [self map];
    CLLocationCoordinate2D coord = [mapView convertPoint:pivot toCoordinateFromView:mapView];
    CGFloat zoom = [mapView getZoomLevel];
    CGFloat currentZoom = [mapView getZoomLevel];
    float targetZoom = ceilf(currentZoom) + 1;
    float factor = powf(2, targetZoom - currentZoom);
    
    if (factor > 2.25) {
        targetZoom = ceilf(currentZoom);
    }
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
    MKMapView* mapView = [self map];
    CLLocationCoordinate2D coord = [mapView convertPoint:pivot toCoordinateFromView:mapView];
    CGFloat zoom = [mapView getZoomLevel];
    CGFloat currentZoom = [mapView getZoomLevel];
    float targetZoom = ceilf(currentZoom) + 1;
    float factor = powf(2, targetZoom - currentZoom);
    if (factor > 0.75) {
        targetZoom = floorf(currentZoom) - 1;
    }
}

-(void)setCenterCoordinate_:(id)center
{
    MKCoordinateRegion mkregion = mkregionFromRegion(region);
    CLLocationCoordinate2D coord;
    if ([center isKindOfClass:[NSArray class]]) {
        mkregion.center = CLLocationCoordinate2DMake([TiUtils floatValue:[center objectAtIndex:0]],[TiUtils floatValue:[center objectAtIndex:1]]);
    }
    else {
        ENSURE_SINGLE_ARG(center,NSDictionary);
        if (center) {
            mkregion.center = [AkylasMapModule locationFromDict:center];
            id latdelta = [center objectForKey:@"latitudeDelta"];
            id londelta = [center objectForKey:@"longitudeDelta"];
            if (latdelta)
            {
                mkregion.span.latitudeDelta = [latdelta doubleValue];
            }
            if (londelta)
            {
                mkregion.span.longitudeDelta = [londelta doubleValue];
            }
            id an = [center objectForKey:@"animate"];
            if (an)
            {
                animate = [an boolValue];
            }
            id rf = [center objectForKey:@"regionFit"];
            if (rf)
            {
                regionFits = [rf boolValue];
            }
        }
        else {
            mkregion.center = [self map].userLocation.location.coordinate;
        }
        
    }
    region = regionFromMKRegion(mkregion);
	[self render];
}

-(id)centerCoordinate_
{
    return [AkylasMapModule dictFromLocation2D:RMSphericalTrapeziumCenter(region)];
}

#pragma mark Public APIs iOS 7

-(void)setTintColor_:(id)color
{
    [AkylasMapModule logAddedIniOS7Warning:@"tintColor"];
}

-(void)setCamera_:(id)value
{
    [AkylasMapModule logAddedIniOS7Warning:@"camera"];
}

-(void)setPitchEnabled_:(id)value
{
    [AkylasMapModule logAddedIniOS7Warning:@"pitchEnabled"];
}

-(void)setRotateEnabled_:(id)value
{
    [AkylasMapModule logAddedIniOS7Warning:@"rotateEnabled"];
}

-(void)setShowsBuildings_:(id)value
{
    [AkylasMapModule logAddedIniOS7Warning:@"showsBuildings"];
}

-(void)setShowsPointsOfInterest_:(id)value
{
    [AkylasMapModule logAddedIniOS7Warning:@"showsPointsOfInterest"];
}

#pragma mark Utils
// Using these utility functions allows us to override them for different versions of iOS

-(void)addOverlay:(MKPolyline*)polyline level:(MKOverlayLevel)level
{
    [map addOverlay:polyline];
}

// These methods override the default implementation in TiMapView
-(void)addOverlay:(MKPolyline*)polyline index:(NSInteger)index level:(MKOverlayLevel)level
{
    [map insertOverlay:polyline atIndex:index];
}


- (void)tintColorDidChange
{
    if (_calloutView)
        _calloutView.tintColor = self.tintColor;
}


#pragma mark Delegates

// Delegate for >= iOS 7
- (MKOverlayRenderer *)mapView:(MKMapView *)mapView rendererForOverlay:(id < MKOverlay >)overlay
{
    AkylasMapRouteProxy* route = (AkylasMapRouteProxy *)CFDictionaryGetValue(mapLine2View, overlay);
    return [route rendererForMapView:self];
}

// Delegate for < iOS 7
// MKPolylineView is deprecated in iOS 7, still here for backward compatibility.
// Can be removed when support is dropped for iOS 6 and below.
- (MKOverlayView *)mapView:(MKMapView *)mapView viewForOverlay:(id <MKOverlay>)overlay
{
    AkylasMapRouteProxy* route = (AkylasMapRouteProxy *)CFDictionaryGetValue(mapLine2View, overlay);
    return (MKOverlayView*)[route rendererForMapView:self];
}

- (void)mapView:(MKMapView *)mapView regionDidChangeAnimated:(BOOL)animated
{
    _cameraAnimating = NO;
    if (_pendingCamera) {
        [self setCamera:_pendingCamera animated:animated];
        RELEASE_TO_NIL(_pendingCamera)
    }
    if (ignoreRegionChanged) {
        return;
    }
    region = regionFromMKRegion([mapView region]);
    _internalZoom = [mapView getZoomLevel];
    CGFloat zoomlevel = _internalZoom;
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
	{
		[self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"region":[AkylasMapModule dictFromRegion:region],
                                                            @"zoom":@(_internalZoom)
                                                            } propagate:NO checkForListener:NO];
	}
}

- (void)mapViewWillStartLoadingMap:(MKMapView *)mapView
{
	loaded = NO;
	if ([self.viewProxy _hasListeners:@"loading" checkParent:NO])
	{
		[self.proxy fireEvent:@"loading" propagate:NO checkForListener:NO];
	}
}

- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView
{
	ignoreClicks = YES;
	loaded = YES;
	if ([self.viewProxy _hasListeners:@"complete" checkParent:NO])
	{
		[self.proxy fireEvent:@"complete" propagate:NO checkForListener:NO];
	}
	ignoreClicks = NO;
}

- (void)mapViewDidFailLoadingMap:(MKMapView *)mapView withError:(NSError *)error
{
	if ([self.viewProxy _hasListeners:@"error" checkParent:NO])
	{
		NSString * message = [TiUtils messageFromError:error];
		NSDictionary *event = [NSDictionary dictionaryWithObject:message forKey:@"message"];
		[self.proxy fireEvent:@"error" withObject:event propagate:NO reportSuccess:NO errorCode:[error code] message:message checkForListener:NO];
	}
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)annotationView didChangeDragState:(MKAnnotationViewDragState)newState fromOldState:(MKAnnotationViewDragState)oldState
{
	[self firePinChangeDragState:annotationView newState:newState fromOldState:oldState];
}

- (void)firePinChangeDragState:(MKAnnotationView *) pinview newState:(MKAnnotationViewDragState)newState fromOldState:(MKAnnotationViewDragState)oldState 
{
	AkylasMapAnnotationProxy *proxy = [self proxyForAnnotation:pinview];

	if (proxy == nil)
		return;

	TiViewProxy * ourProxy = [self viewProxy];
	BOOL parentWants = [ourProxy _hasListeners:@"pinchangedragstate" checkParent:NO];
	BOOL viewWants = [proxy _hasListeners:@"pinchangedragstate" checkParent:NO];
	
	if(!parentWants && !viewWants)
		return;

	id title = [proxy title];
	if (title == nil)
		title = [NSNull null];

	NSNumber * indexNumber = NUMINT([proxy tag]);

	NSDictionary * event = [NSDictionary dictionaryWithObjectsAndKeys:
								proxy,@"annotation",
								ourProxy,@"map",
								title,@"title",
								indexNumber,@"index",
								NUMINT(newState),@"newState",
								NUMINT(oldState),@"oldState",
								nil];

	if (parentWants)
		[ourProxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];

	if (viewWants)
		[proxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
}

- (AkylasMapAnnotationProxy*)proxyForAnnotation:(MKAnnotationView*)pinview
{
	for (id annotation in [map annotations])
	{
		if ([annotation isKindOfClass:[AkylasMapAnnotationProxy class]])
		{
			if ([(AkylasMapAnnotationProxy*)annotation tag] == pinview.tag)
			{
				return annotation;
			}
		}
	}
	return nil;
}


-(UIView *)hitTest:(CGPoint) point withEvent:(UIEvent *)event
{
	BOOL hasTouchListeners = [self hasTouchableListener];
	UIView *hitView = [super hitTest:point withEvent:event];
    if (!([hitView isKindOfClass:[UIControl class]]) && [_calloutView pointInside:[_calloutView convertPoint:point fromView:self] withEvent:event]) {
        calloutTouchedView = hitView;
    }
    else {
        calloutTouchedView = nil;
    }

	return hitView;
}

- (void)mapView:(MKMapView *)mapView didSelectAnnotationView:(MKAnnotationView *)annotationView {
    if (calloutTouchedView) {
        calloutTouchedView = nil;
        return;
    }
    BOOL canShowCallout = YES;
    AkylasMapAnnotationProxy *annProxy = nil;
    if ([[annotationView annotation] isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        annProxy = (AkylasMapAnnotationProxy*)[annotationView annotation];
        canShowCallout = [TiUtils boolValue:[annProxy valueForUndefinedKey:@"showInfoWindow"] def:YES];
    }
    if (canShowCallout) {
        if (!_calloutView) {
            _calloutView = [[SMCalloutView platformCalloutView] retain];
            _calloutView.delegate = self;
            if (PostVersion7) {
                _calloutView.tintColor = self.tintColor;
            }
        }
        if ([annotationView respondsToSelector:@selector(setCalloutView:)]) {
            [annotationView setCalloutView:_calloutView];
        }
        // apply the MKAnnotationView's basic properties
        _calloutView.title = annotationView.annotation.title;
        _calloutView.subtitle = annotationView.annotation.subtitle;
        
        // Apply the desired calloutOffset (from the top-middle of the view)
        CGPoint calloutOffset = [annProxy nGetCalloutAnchorPoint];
        calloutOffset.y +=0.5f;
        calloutOffset.x *= annotationView.frame.size.width;
        calloutOffset.y *= annotationView.frame.size.height;
        
        if ([annotationView isKindOfClass:[MKPinAnnotationView class]]) {
            calloutOffset.x -=8;
        }
        
        _calloutView.calloutOffset = calloutOffset;
        
        SMCalloutMaskedBackgroundView* backView = (SMCalloutMaskedBackgroundView*)_calloutView.backgroundView;
        backView.alpha = [annProxy nGetCalloutAlpha];
        if (_calloutUseTemplates) {
            _calloutView.leftAccessoryView = [self reusableViewForProxy:annProxy objectKey:@"leftView"];
            _calloutView.rightAccessoryView = [self reusableViewForProxy:annProxy objectKey:@"rightView"];
            _calloutView.contentView = [self reusableViewForProxy:annProxy objectKey:@"customView"];
        }
        else {
            _calloutView.leftAccessoryView = [annProxy nGetLeftViewAccessory];
            _calloutView.rightAccessoryView = [annProxy nGetRightViewAccessory];
            _calloutView.contentView = [annProxy nGetCustomViewAccessory];
        }
        if (annProxy) {
            _calloutView.padding = [annProxy nGetCalloutPadding];
            backView.backgroundColor = [annProxy nGetCalloutBackgroundColor];
            backView.cornerRadius = [annProxy nGetCalloutBorderRadius];
            backView.arrowHeight = [annProxy nGetCalloutArrowHeight];
        }
        else {
            backView.backgroundColor = [UIColor whiteColor];
            backView.cornerRadius = DEFAULT_CALLOUT_CORNER_RADIUS;
            backView.arrowHeight = DEFAULT_CALLOUT_ARROW_HEIGHT;
            _calloutView.padding = DEFAULT_CALLOUT_PADDING;
        }

        // This does all the magic.
        [_calloutView presentCalloutFromRect:annotationView.bounds inView:annotationView constrainedToView:self animated:YES];
    }
    
    if ([annotationView conformsToProtocol:@protocol(AkylasMapAnnotation)])
	{
		BOOL isSelected = [annotationView isSelected];
		MKAnnotationView<AkylasMapAnnotation> *ann = (MKAnnotationView<AkylasMapAnnotation> *)annotationView;
		[self fireClickEvent:annotationView source:isSelected?@"pin":[ann lastHitName]];
		return;
	}
}

- (void)mapView:(MKMapView *)mapView didDeselectAnnotationView:(MKAnnotationView *)annotationView {
    if (calloutTouchedView) {
        [mapView selectAnnotation:annotationView.annotation animated:NO];
        return;
    }
    [_calloutView dismissCalloutAnimated:YES];
    if ([annotationView conformsToProtocol:@protocol(AkylasMapAnnotation)])
	{
		BOOL isSelected = [annotationView isSelected];
		MKAnnotationView<AkylasMapAnnotation> *ann = (MKAnnotationView<AkylasMapAnnotation> *)annotationView;
		[self fireClickEvent:annotationView source:isSelected?@"pin":[ann lastHitName]];
		return;
	}
}

- (void)mapView:(MKMapView *)mapView annotationView:(MKAnnotationView *)aview calloutAccessoryControlTapped:(UIControl *)control
{
	if ([aview conformsToProtocol:@protocol(AkylasMapAnnotation)])
	{
		MKPinAnnotationView *pinview = (MKPinAnnotationView*)aview;
		NSString * clickSource = @"unknown";
		if (aview.leftCalloutAccessoryView == control)
		{
			clickSource = @"leftButton";
		}
		else if (aview.rightCalloutAccessoryView == control)
		{
			clickSource = @"rightButton";
		}
		[self fireClickEvent:aview source:clickSource];
	}
}


// mapView:viewForAnnotation: provides the view for each annotation.
// This method may be called for all or some of the added annotations.
// For MapKit provided annotations (eg. MKUserLocation) return nil to use the MapKit provided annotation view.
- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    if ([annotation isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        AkylasMapAnnotationProxy *ann = (AkylasMapAnnotationProxy*)annotation;
        id pinView = [ann valueForUndefinedKey:@"pinView"];
        if ( (pinView == nil) || (pinView == [NSNull null]) || (![pinView isKindOfClass:[TiViewProxy class]]) ){
            pinView = nil;
        }
        NSString *identifier = nil;
        UIImage* image = nil;
        
        if (pinView == nil) {
            identifier = [ann nHasInternalImage] ? @"timap-image":@"timap-pin";
            image = [ann nGetInternalImage];
        }
        else {
            identifier = @"timap-customView";
        }
        MKAnnotationView *annView = nil;
		
        annView = (MKAnnotationView*) [mapView dequeueReusableAnnotationViewWithIdentifier:identifier];
        

		
        if (annView==nil) {
            if ([identifier isEqualToString:@"timap-customView"]) {
                annView = [[[AkylasMapCustomAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self] autorelease];
            }
            else if ([identifier isEqualToString:@"timap-image"]) {
                annView=[[[AkylasMapImageAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self image:image] autorelease];
            }
            else {
                annView=[[[AkylasMapPinAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self] autorelease];
            }
        }
        if ([identifier isEqualToString:@"timap-customView"]) {
            [((AkylasMapCustomAnnotationView*)annView) setProxy:pinView];
        }
        else if ([identifier isEqualToString:@"timap-image"]) {
            annView.image = image;
        }
        else {
            MKPinAnnotationView *pinview = (MKPinAnnotationView*)annView;
            pinview.pinColor = [ann mapPincolor];
            pinview.animatesDrop = [ann animatesDrop] && ![(AkylasMapAnnotationProxy *)annotation placed];
            annView.centerOffset = CGPointMake(8, -15); //reinit centerOffset for default Pin
        }
        CGPoint currentOffset = annView.centerOffset;
        // Apply the desired calloutOffset (from the top-middle of the view)
        CGPoint centerOffset = [ann nGetAnchorPoint];
        centerOffset.x = 0.5f - centerOffset.x;
        centerOffset.y = 0.5f - centerOffset.y;
        centerOffset.x *= annView.frame.size.width;
        centerOffset.y *= annView.frame.size.height;
        centerOffset.x += currentOffset.x;
        centerOffset.y += currentOffset.y;
        annView.centerOffset = centerOffset;
        
        annView.canShowCallout = NO; //SMCalloutView
        annView.enabled = YES;
//        annView.backgroundColor = [UIColor greenColor];
//        annView.centerOffset = CGPointMake(-8, 0);

        BOOL draggable = [TiUtils boolValue: [ann valueForUndefinedKey:@"draggable"]];
        if (draggable && [[MKAnnotationView class] instancesRespondToSelector:@selector(setDraggable:)])
            [annView performSelector:@selector(setDraggable:) withObject:[NSNumber numberWithBool:YES]];

        annView.userInteractionEnabled = YES;
        annView.tag = [ann tag];
        
        ann.annView = annView;
        return annView;
    }
    return nil;
}


// mapView:didAddAnnotationViews: is called after the annotation views have been added and positioned in the map.
// The delegate can implement this method to animate the adding of the annotations views.
// Use the current positions of the annotation views as the destinations of the animation.
- (void)mapView:(MKMapView *)mapView didAddAnnotationViews:(NSArray *)views
{
	for (MKAnnotationView<AkylasMapAnnotation> *thisView in views)
	{
		if(![thisView conformsToProtocol:@protocol(AkylasMapAnnotation)])
		{
			return;
		}
		AkylasMapAnnotationProxy * thisProxy = [self proxyForAnnotation:thisView];
        /*Image Annotation don't have any animation of its own.
         *So in this case we do a custom animation, to place the 
         *image annotation on top of the mapview.*/
        if([thisView isKindOfClass:[AkylasMapImageAnnotationView class]] || [thisView isKindOfClass:[AkylasMapCustomAnnotationView class]])
        {
            if([thisProxy animatesDrop] && ![thisProxy placed])
            {
                CGRect viewFrame = thisView.frame;
                thisView.frame = CGRectMake(viewFrame.origin.x, viewFrame.origin.y - self.frame.size.height, viewFrame.size.width, viewFrame.size.height);
                [UIView animateWithDuration:0.4 
                                      delay:0.0 
                                    options:UIViewAnimationCurveEaseOut 
                                 animations:^{thisView.frame = viewFrame;}
                                 completion:nil];
            }
        }
		[thisProxy setPlaced:YES];
	}
}

#pragma mark SMCalloutViewDelegate
//
// SMCalloutView delegate methods
//

- (void)calloutViewClicked:(SMCalloutView *)calloutView {
    
}

- (NSTimeInterval)calloutView:(SMCalloutView *)calloutView delayForRepositionWithSize:(CGSize)offset {
    
    // When the callout is being asked to present in a way where it or its target will be partially offscreen, it asks us
    // if we'd like to reposition our surface first so the callout is completely visible. Here we scroll the map into view,
    // but it takes some math because we have to deal in lon/lat instead of the given offset in pixels.
    
    CLLocationCoordinate2D coordinate = self.map.centerCoordinate;
    
    // where's the center coordinate in terms of our view?
    CGPoint center = [self.map convertCoordinate:coordinate toPointToView:self];
    
    // move it by the requested offset
    center.x -= offset.width;
    center.y -= offset.height;
    
    // and translate it back into map coordinates
    coordinate = [self.map convertPoint:center toCoordinateFromView:self];
    
    // move the map!
    [self.map setCenterCoordinate:coordinate animated:YES];
    
    // tell the callout to wait for a while while we scroll (we assume the scroll delay for MKMapView matches UIScrollView)
    return kSMCalloutViewRepositionDelayForUIScrollView;
}

- (void)disclosureTapped {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Tap!" message:@"You tapped the disclosure button."
                                                   delegate:nil cancelButtonTitle:nil otherButtonTitles:@"OK",nil];
    [alert show];
}

#pragma mark Click detection

-(id<MKAnnotation>)wasHitOnAnnotation:(CGPoint)point inView:(UIView*)view
{
	id<MKAnnotation> result = nil;
	for (UIView* subview in [view subviews]) {
		if (![subview pointInside:[self convertPoint:point toView:subview] withEvent:nil]) {
			continue;
		}
		
		if ([subview isKindOfClass:[MKAnnotationView class]]) {
			result = [(MKAnnotationView*)subview annotation];
		}
		else {
			result = [self wasHitOnAnnotation:point inView:subview];
		}
		
		if (result != nil) {
			break;
		}
	}
	return result;
}

#pragma mark Event generation

- (void)fireEvent:(NSString*)type onAnnotation:(MKAnnotationView *)pinview source:(NSString *)source withRecognizer:(UIGestureRecognizer*)recognizer
{
	if (ignoreClicks)
	{
		return;
	}
    
	AkylasMapAnnotationProxy *proxy = [self proxyForAnnotation:pinview];
	if (proxy == nil)
	{
		return;
	}
    
	TiViewProxy * ourProxy = [self viewProxy];
	BOOL parentWants = [ourProxy _hasListeners:type checkParent:NO];
	BOOL viewWants = [proxy _hasListeners:type checkParent:NO];
	if(!parentWants && !viewWants)
	{
		return;
	}
	
	id title = [proxy title];
	if (title == nil)
	{
		title = [NSNull null];
	}
    
	id clicksource = source ? source : (id)[NSNull null];
	
    NSMutableDictionary *event = [[TiUtils dictionaryFromGesture:recognizer inView:self] mutableCopy];
    if (recognizer) {
        CGPoint point = [recognizer locationInView:self];
        [event addEntriesFromDictionary:[AkylasMapModule dictFromLocation2D:[map convertPoint:point toCoordinateFromView:map]]];
    }
    
    [event setObject:clicksource forKey:@"clicksource"];
    [event setObject:proxy forKey:@"annotation"];
    [event setObject:ourProxy forKey:@"map"];
    [event setObject:title forKey:@"title"];
    [event setObject:NUMINT([proxy tag]) forKey:@"index"];
	if (parentWants)
	{
		[ourProxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
	if (viewWants)
	{
		[proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
    [event release];
}


- (void)fireClickEvent:(MKAnnotationView *) pinview source:(NSString *)source
{
	if (ignoreClicks)
	{
		return;
	}
    
	[self fireEvent:@"click" onAnnotation:pinview source:source withRecognizer:nil];
}



- (void)fireEventOnMap:(NSString*)type withRecognizer:(UIGestureRecognizer*)recognizer
{
	if (ignoreClicks)
	{
		return;
	}
    
	if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        CGPoint point = [recognizer locationInView:self];
        NSMutableDictionary *event = [[TiUtils dictionaryFromGesture:recognizer inView:self] mutableCopy];
        [event addEntriesFromDictionary:[AkylasMapModule dictFromLocation2D:[map convertPoint:point toCoordinateFromView:map]]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
        [event release];
	}
}

- (BOOL)internalAddTileSources:(id)tileSource atIndex:(NSInteger)index
{
//    __block NSInteger realIndex = index;
//    if (realIndex == -1) {
//        realIndex = INT_MAX;
//    }
//    if (IS_OF_CLASS(tileSource, NSArray)) {
//        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
//            [self internalAddTileSources:obj atIndex:realIndex++];
//        }];
//    } else {
//    }
}

- (BOOL)internalRemoveTileSources:(id)tileSource
{
//    if (IS_OF_CLASS(tileSource, NSArray)) {
//        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
//            [self internalRemoveTileSources:obj];
//        }];
//    } else {
//    }
}

- (BOOL)internalRemoveAllTileSources
{
//    [[self map] removeOverlays:[[self map] overlays]];
}


@end
