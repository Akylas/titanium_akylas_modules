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
}

#pragma mark Internal

- (id)init
{
    if ((self = [super init])) {
        _minZoom = 0;
        _maxZoom = 100;
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
    if (mapLine2View) {
        CFRelease(mapLine2View);
        mapLine2View = nil;
    }
	[super dealloc];
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
    [[self map] setFrame:bounds];
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

-(void)internalAddAnnotations:(id)annotations
{
    MKMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapAnnotationProxy* annotProxy in annotations) {
            [mapView addAnnotation:annotProxy];
        }
    }
    else {
        [mapView addAnnotation:(AkylasMapAnnotationProxy*)annotations];
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    MKMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapAnnotationProxy* annotProxy in annotations) {
            if ([annotProxy isKindOfClass:[AkylasMapAnnotationProxy class]]) {
                [mapView removeAnnotation:(AkylasMapAnnotationProxy*)annotProxy];
            }
        }
    }
    else if ([annotations isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        [mapView removeAnnotation:(AkylasMapAnnotationProxy*)annotations];
    }
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

-(void)zoom:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(zoom,args);

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

#pragma mark Public APIs

-(void)setMapType_:(id)value
{
	[[self map] setMapType:[TiUtils intValue:value]];
}


-(id)getRegion
{
    return [AkylasMapModule dictFromRegion:[self getCurrentRegion]];
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
	[self map].showsUserLocation = [TiUtils boolValue:value];
}


-(id)userLocationEnabled
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

-(id)userLocation
{
    return [self dictFromUserLocation:[self map].userLocation];
}

-(void)setUserTrackingMode_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
	[[self map] setUserTrackingMode:[TiUtils intValue:value def:MKUserTrackingModeNone] animated:animate];
}

-(id)userTrackingMode
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
    [self setZoomInternal:_zoom];
}

-(void)setMaxZoom_:(id)zoom
{
	ENSURE_SINGLE_ARG(zoom,NSNumber);
    _minZoom = [TiUtils floatValue:zoom];
    [self setZoomInternal:_zoom];
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

-(id)centerCoordinate
{
    return [AkylasMapModule dictFromLocation2D:RMSphericalTrapeziumCenter(region)];
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
    if (ignoreRegionChanged) {
        return;
    }
    region = regionFromMKRegion([mapView region]);
    _zoom = [mapView getZoomLevel];
    if ([self.proxy _hasListeners:@"regionchanged"])
	{
		[self.proxy fireEvent:@"regionchanged" withObject:[AkylasMapModule dictFromRegion:region] propagate:NO checkForListener:NO];
	}
}

- (void)mapViewWillStartLoadingMap:(MKMapView *)mapView
{
	loaded = NO;
	if ([self.proxy _hasListeners:@"loading"])
	{
		[self.proxy fireEvent:@"loading" propagate:NO checkForListener:NO];
	}
}

- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView
{
	ignoreClicks = YES;
	loaded = YES;
	if ([self.proxy _hasListeners:@"complete"])
	{
		[self.proxy fireEvent:@"complete" propagate:NO checkForListener:NO];
	}
	ignoreClicks = NO;
}

- (void)mapViewDidFailLoadingMap:(MKMapView *)mapView withError:(NSError *)error
{
	if ([self.proxy _hasListeners:@"error"])
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
	AkylasMapAnnotationProxy *viewProxy = [self proxyForAnnotation:pinview];

	if (viewProxy == nil)
		return;

	TiProxy * ourProxy = [self proxy];
	BOOL parentWants = [ourProxy _hasListeners:@"pinchangedragstate"];
	BOOL viewWants = [viewProxy _hasListeners:@"pinchangedragstate"];
	
	if(!parentWants && !viewWants)
		return;

	id title = [viewProxy title];
	if (title == nil)
		title = [NSNull null];

	NSNumber * indexNumber = NUMINT([viewProxy tag]);

	NSDictionary * event = [NSDictionary dictionaryWithObjectsAndKeys:
								viewProxy,@"annotation",
								ourProxy,@"map",
								title,@"title",
								indexNumber,@"index",
								NUMINT(newState),@"newState",
								NUMINT(oldState),@"oldState",
								nil];

	if (parentWants)
		[ourProxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];

	if (viewWants)
		[viewProxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
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

- (void)mapView:(MKMapView *)mapView didSelectAnnotationView:(MKAnnotationView *)view{
	if ([view conformsToProtocol:@protocol(AkylasMapAnnotation)])
	{
		BOOL isSelected = [view isSelected];
		MKAnnotationView<AkylasMapAnnotation> *ann = (MKAnnotationView<AkylasMapAnnotation> *)view;
		[self fireClickEvent:view source:isSelected?@"pin":[ann lastHitName]];
		return;
	}
}
- (void)mapView:(MKMapView *)mapView didDeselectAnnotationView:(MKAnnotationView *)view{
	if ([view conformsToProtocol:@protocol(AkylasMapAnnotation)])
	{
		BOOL isSelected = [view isSelected];
		MKAnnotationView<AkylasMapAnnotation> *ann = (MKAnnotationView<AkylasMapAnnotation> *)view;
		[self fireClickEvent:view source:isSelected?@"pin":[ann lastHitName]];
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
        id customView = [ann valueForUndefinedKey:@"customView"];
        if ( (customView == nil) || (customView == [NSNull null]) || (![customView isKindOfClass:[TiViewProxy class]]) ){
            customView = nil;
        }
        NSString *identifier = nil;
        UIImage* image = nil;
        if (customView == nil) {
            id imagePath = [ann valueForUndefinedKey:@"image"];
            image = [TiUtils image:imagePath proxy:ann];
            identifier = (image!=nil) ? @"timap-image":@"timap-pin";
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
            [((AkylasMapCustomAnnotationView*)annView) setProxy:customView];
        }
        else if ([identifier isEqualToString:@"timap-image"]) {
            annView.image = image;
        }
        else {
            MKPinAnnotationView *pinview = (MKPinAnnotationView*)annView;
            pinview.pinColor = [ann pinColor];
            pinview.animatesDrop = [ann animatesDrop] && ![(AkylasMapAnnotationProxy *)annotation placed];
            annView.calloutOffset = CGPointMake(-8, 0);
        }
        annView.canShowCallout = [TiUtils boolValue:[ann valueForUndefinedKey:@"canShowCallout"] def:YES];;
        annView.enabled = YES;
        annView.centerOffset = ann.offset;
        UIView *left = [ann leftViewAccessory];
        UIView *right = [ann rightViewAccessory];
        if (left!=nil) {
            annView.leftCalloutAccessoryView = left;
        }
        if (right!=nil) {
            annView.rightCalloutAccessoryView = right;
        }

        BOOL draggable = [TiUtils boolValue: [ann valueForUndefinedKey:@"draggable"]];
        if (draggable && [[MKAnnotationView class] instancesRespondToSelector:NSSelectorFromString(@"isDraggable")])
            [annView performSelector:NSSelectorFromString(@"setDraggable:") withObject:[NSNumber numberWithBool:YES]];

        annView.userInteractionEnabled = YES;
        annView.tag = [ann tag];
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
    
	AkylasMapAnnotationProxy *viewProxy = [self proxyForAnnotation:pinview];
	if (viewProxy == nil)
	{
		return;
	}
    
	TiProxy * ourProxy = [self proxy];
	BOOL parentWants = [ourProxy _hasListeners:type];
	BOOL viewWants = [viewProxy _hasListeners:type];
	if(!parentWants && !viewWants)
	{
		return;
	}
	
	id title = [viewProxy title];
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
    [event setObject:viewProxy forKey:@"annotation"];
    [event setObject:ourProxy forKey:@"map"];
    [event setObject:title forKey:@"title"];
    [event setObject:NUMINT([viewProxy tag]) forKey:@"index"];
	if (parentWants)
	{
		[ourProxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
	if (viewWants)
	{
		[viewProxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
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
    
	if ([self.proxy _hasListeners:type]) {
        CGPoint point = [recognizer locationInView:self];
        NSMutableDictionary *event = [[TiUtils dictionaryFromGesture:recognizer inView:self] mutableCopy];
        [event addEntriesFromDictionary:[AkylasMapModule dictFromLocation2D:[map convertPoint:point toCoordinateFromView:map]]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
        [event release];
	}
}



@end
