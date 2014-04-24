/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapboxView.h"
#import "TiUtils.h"
#import "AkylasMapboxModule.h"
#import "AkylasMapboxAnnotationProxy.h"
#import "AkylasMapboxPinAnnotationView.h"
#import "AkylasMapboxImageAnnotationView.h"
#import "AkylasMapboxCustomAnnotationView.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasTileSource.h"
#import "ImageLoader.h"

@implementation AkylasMapboxView
{
    NSMutableArray *tileSources;
}
@synthesize defaultPinImage;
@synthesize defaultPinAnchor;

#pragma mark Internal

-(void)initializeState
{
	// This method is called right after allocating the view and
	// is useful for initializing anything specific to the view
    
    [self addMap];
    
	[super initializeState];
    
	NSLog(@"[VIEW LIFECYCLE EVENT] initializeState");
}

- (id)init
{
    if ((self = [super init])) {
        defaultPinAnchor = CGPointMake(0.5, 0.5);
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
    if (tileSources) {
        for ( AkylasTileSource* tileSource in tileSources) {
            if (tileSource.proxy) {
                [self.proxy forgetProxy:tileSource.proxy];
            }
        }
        RELEASE_TO_NIL(tileSources)
    }
	[super dealloc];
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

//    if (region.center.latitude!=0 && region.center.longitude!=0 && !isnan(region.center.latitude) && !isnan(region.center.longitude))
//    {
//        if (regionFits) {
//            [map setRegion:[map regionThatFits:region] animated:animate];
//        }
//        else {
            [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast animated:animate];
//        }
//    }
}


-(UIView*)viewForHitTest
{
    return map;
}

-(void)addMap
{
}

-(RMMapView*)map
{
    if (map==nil)
    {
        RELEASE_TO_NIL(tileSources)
        map = [[RMMapView alloc] initWithFrame:[TiUtils appFrame]];
        
        [map setTileSources:[self setTileSourcesFromProp:[self.proxy valueForKey:@"tileSource"]]];
//        if(tileSources) {
//            id<RMTileSource> tileSource = ((AkylasTileSource*)[tileSources objectAtIndex:0]).tileSource;
//            if ([tileSource respondsToSelector:@selector(centerCoordinate)]) {
//                [map setCenterCoordinate:[(id)tileSource centerCoordinate]];
//            }
//            map.zoom = tileSource.minZoom;
//            map.minZoom = tileSource.minZoom;
//            map.maxZoom = tileSource.maxZoom;
//        }
        map.decelerationMode = RMMapDecelerationFast;
        CLLocationCoordinate2D coord = map.centerCoordinate;
//        map.adjustTilesForRetinaDisplay = [[UIScreen mainScreen] scale] > 1.0;
        [map setShowsUserLocation:YES];
        map.delegate = self;
        map.tileCache.backgroundCacheDelegate = self;
        map.showLogoBug = NO;
        map.hideAttribution = YES;
        map.userInteractionEnabled = YES;
        map.showsUserLocation = YES; // defaults
        map.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        map.clusteringEnabled = NO;
        [self addSubview:map];
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
    NSMutableArray *annotations = [NSMutableArray array];
    for (RMAnnotation* annotation in map.annotations) {
        if (!annotation.isUserLocationAnnotation && [annotation.userInfo isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
            [annotations addObject:annotation.userInfo];
        }
    }
    return annotations;
}

//-(void)willFirePropertyChanges
//{
//	regionFits = [TiUtils boolValue:[self.proxy valueForKey:@"regionFit"]];
//	animate = [TiUtils boolValue:[self.proxy valueForKey:@"animate"]];
//}

-(void)didFirePropertyChanges
{
	[self render];
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

-(AkylasMapboxAnnotationProxy*)annotationFromArg:(id)arg
{
    return [(AkylasMapboxViewProxy*)[self proxy] annotationFromArg:arg];
}

-(NSArray*)annotationsFromArgs:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	NSMutableArray * result = [NSMutableArray arrayWithCapacity:[value count]];
	if (value!=nil)
	{
		for (id arg in value)
		{
			[result addObject:[self annotationFromArg:arg]];
		}
	}
	return result;
}

-(void)refreshAnnotation:(AkylasMapboxAnnotationProxy*)proxy readd:(BOOL)yn
{
    RMAnnotation *newSelected = [proxy getAnnotationForMapView:[self map]];
	RMAnnotation *selected = map.selectedAnnotation;
	BOOL wasSelected = newSelected == selected; //If selected == nil, this still returns FALSE.
    ignoreClicks = YES;
    [map deselectAnnotation:newSelected animated:animate];
	if (yn)
	{
		[map removeAnnotation:newSelected];
		[map addAnnotation:newSelected];
		[newSelected.layer setNeedsLayout];
	}
	if (wasSelected)
	{
		[map selectAnnotation:newSelected animated:animate];
	}
    ignoreClicks = NO;
}

-(void)internalAddAnnotations:(id)annotations
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapboxAnnotationProxy* annotProxy in annotations) {
            [mapView addAnnotation:[annotProxy getAnnotationForMapView:mapView]];
        }
    }
    else {
        [mapView addAnnotation:[(AkylasMapboxAnnotationProxy*)annotations getAnnotationForMapView:mapView]];
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapboxAnnotationProxy* annotProxy in annotations) {
            if ([annotProxy isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
                [mapView removeAnnotation:[annotProxy getAnnotation]];
            }
        }
    }
    else if ([annotations isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
        [mapView removeAnnotation:[(AkylasMapboxAnnotationProxy*)annotations getAnnotation]];
    }
}

#pragma mark Public APIs

-(void)addAnnotation:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(addAnnotation,args);
	[self internalAddAnnotations:[self annotationFromArg:args]];
}

-(void)addAnnotations:(id)args
{
	ENSURE_TYPE(args,NSArray);
	ENSURE_UI_THREAD(addAnnotations,args);

	[self internalAddAnnotations:[self annotationsFromArgs:args]];
}

-(void)removeAnnotation:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);

	id<MKAnnotation> doomedAnnotation = nil;
	
	if ([args isKindOfClass:[NSString class]])
	{
		// for pre 0.9, we supported removing by passing the annotation title
		NSString *title = [TiUtils stringValue:args];
		for (id<MKAnnotation>an in self.customAnnotations)
		{
			if ([title isEqualToString:an.title])
			{
				doomedAnnotation = an;
				break;
			}
		}
	}
	else if ([args isKindOfClass:[AkylasMapboxAnnotationProxy class]])
	{
		doomedAnnotation = args;
	}
	
    TiThreadPerformOnMainThread(^{
        [self internalRemoveAnnotations:doomedAnnotation];
    }, NO);
}

-(void)removeAnnotations:(id)args
{
	ENSURE_TYPE_OR_NIL(args,NSArray); // assumes an array of AkylasMapboxAnnotationProxy, and NSString classes
    
    // Test for annotation title strings
    NSMutableArray *doomedAnnotations = [NSMutableArray arrayWithArray:args];
    NSUInteger count = [doomedAnnotations count];
    id doomedAn;
    for (int i = 0; i < count; i++)
    {
        doomedAn = [doomedAnnotations objectAtIndex:i];
        if ([doomedAn isKindOfClass:[NSString class]])
        {
            // for pre 0.9, we supported removing by passing the annotation title
            NSString *title = [TiUtils stringValue:doomedAn];
            for (AkylasMapboxAnnotationProxy* annotation in self.customAnnotations)
            {
                if ([[annotation getAnnotation].title isEqualToString:title])
                {
                    [doomedAnnotations replaceObjectAtIndex:i withObject:annotation];
                }
            }
        }
    }
    
    TiThreadPerformOnMainThread(^{
        [self internalRemoveAnnotations:doomedAnnotations];
    }, NO);
}

-(void)removeAllAnnotations:(id)args
{
    for (AkylasMapboxAnnotationProxy* annotation in self.customAnnotations)
    {
        if (![annotation isKindOfClass:[AkylasMapboxRouteProxy class]])
        {
            [self.map removeAnnotation:[annotation getAnnotation]];
        }
    }
}

-(void)setAnnotations_:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	ENSURE_UI_THREAD(setAnnotations_,value)
	[self removeAllAnnotations:nil];
	if (value != nil) {
		[self addAnnotations:value];
	}
}


-(void)setSelectedAnnotation:(AkylasMapboxAnnotationProxy*)annotation
{
    RMAnnotation* an = [annotation getAnnotationForMapView:[self map]];
    [map selectAnnotation:[annotation getAnnotationForMapView:[self map]] animated:animate];
    map.centerCoordinate = an.coordinate;
}

-(void)selectAnnotation:(id)args
{
	ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
	ENSURE_UI_THREAD(selectAnnotation,args);
	
	if (args == nil) {
        [[self map] deselectAnnotation:[self map].selectedAnnotation animated:animate];
//		for (id<MKAnnotation> annotation in [[self map] selectedAnnotations]) {
//			[[self map] deselectAnnotation:annotation animated:animate];
//		}
//		return;
	}
	
	if ([args isKindOfClass:[NSString class]])
	{
		// for pre 0.9, we supported selecting by passing the annotation title
		NSString *title = [TiUtils stringValue:args];
		for (AkylasMapboxAnnotationProxy* annotation in self.customAnnotations)
        {
            if ([[annotation getAnnotation].title isEqualToString:title])
            {
				// TODO: Slide the view over to the selected annotation, and/or zoom so it's with all other selected.
				[self setSelectedAnnotation:annotation];
				break;
			}
		}
	}
	else if ([args isKindOfClass:[AkylasMapboxAnnotationProxy class]])
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
		for (AkylasMapboxAnnotationProxy* annotation in self.customAnnotations)
        {
            if ([[annotation getAnnotation].title isEqualToString:title])
            {
				[[self map] deselectAnnotation:[annotation getAnnotation] animated:animate];
				break;
			}
		}
	}
	else if ([args isKindOfClass:[AkylasMapboxAnnotationProxy class]])
	{
		[[self map] deselectAnnotation:[(AkylasMapboxAnnotationProxy*)args getAnnotation] animated:animate];
	}
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
//	RMSphericalTrapezium _region = [self getCurrentRegion];
//	// TODO: Adjust zoom factor based on v
//	if (v > 0)
//	{
//		_region.span.latitudeDelta = _region.span.latitudeDelta / 2.0002;
//		_region.span.longitudeDelta = _region.span.longitudeDelta / 2.0002;
//	}
//	else
//	{
//		_region.span.latitudeDelta = _region.span.latitudeDelta * 2.0002;
//		_region.span.longitudeDelta = _region.span.longitudeDelta * 2.0002;
//	}
	[map setZoom:[TiUtils doubleValue:args] animated:YES];
//	[self render];
}




//
//-(NSDictionary*)dictionaryFromRegion
//{
//    NSMutableDictionary* theDict = [NSMutableDictionary dictionary];
//    [theDict setObject:NUMFLOAT(region.center.latitude) forKey:@"latitude"];
//    [theDict setObject:NUMFLOAT(region.center.longitude) forKey:@"longitude"];
//    [theDict setObject:NUMFLOAT(region.span.latitudeDelta) forKey:@"latitudeDelta"];
//    [theDict setObject:NUMFLOAT(region.span.longitudeDelta) forKey:@"longitudeDelta"];
//    
//    return theDict;
//}

-(RMSphericalTrapezium) getCurrentRegion
{
//    MKCoordinateRegion result;
//    if (loaded) {
//        result.center = [[self map] centerCoordinate];
//        MKCoordinateSpan span;
//        RMSphericalTrapezium boundsCoords = [[self map] latitudeLongitudeBoundingBox];
//        CLLocationDegrees latitudeDelta = boundsCoords.northEast.latitude - boundsCoords.southWest.latitude;
//        CLLocationDegrees longitudeDelta = boundsCoords.northEast.longitude - boundsCoords.southWest.longitude;
//        result.span.latitudeDelta = latitudeDelta;
//        result.span.longitudeDelta = longitudeDelta;
//        result.span = span;
//    }
    return [map latitudeLongitudeBoundingBox];
}

//-(CLLocationDegrees) longitudeDelta
//{
//    if (loaded) {
//        MKCoordinateRegion _region = [self getCurrentRegion];
//        return _region.span.longitudeDelta;
//    }
//    return 0.0;
//}
//
//-(CLLocationDegrees) latitudeDelta
//{
//    if (loaded) {
//        MKCoordinateRegion _region = [self getCurrentRegion];
//        return _region.span.latitudeDelta;
//    }
//    return 0.0;
//}



-(NSMutableArray*) setTileSourcesFromProp:(id)arg
{
    if (tileSources) {
        for ( AkylasTileSource* tileSource in tileSources) {
            if (tileSource.proxy) {
                [self.proxy forgetProxy:tileSource.proxy];
            }
        }
        RELEASE_TO_NIL(tileSources)
    }
    
    NSMutableArray* result = nil;
    if ([arg isKindOfClass:[NSArray class]]) {
        tileSources =  [[NSMutableArray arrayWithCapacity:[arg count]] retain];
        result =  [NSMutableArray arrayWithCapacity:[arg count]];
        for (id source in arg) {
            AkylasTileSource* tileSource = [AkylasTileSource tileSourceWithSource:source proxyForSourceURL:self.proxy];
            if (tileSource) {
                if (tileSource.proxy) {
                    [self.proxy rememberProxy:tileSource.proxy];
                }
                [tileSources addObject:tileSource];
                [result addObject:tileSource.tileSource];
            }
        }
    }
    else {
        AkylasTileSource* tileSource = [AkylasTileSource tileSourceWithSource:arg proxyForSourceURL:self.proxy];
        if (tileSource)  {
            if (tileSource.proxy) {
                [self.proxy rememberProxy:tileSource.proxy];
            }
            tileSources =  [[NSMutableArray arrayWithObject:tileSource] retain];
            result = [NSMutableArray arrayWithObject:tileSource.tileSource];
        }
    }
    return result;
}

#pragma mark Public APIs

-(id)getRegion
{
    return [AkylasMapboxModule dictFromRegion:[self getCurrentRegion]];
}

-(void)setTileSource_:(id)value
{
    if (loaded) {
        
        [map setTileSources:[self setTileSourcesFromProp:value]];
//        if(tileSources) {
//            id<RMTileSource> tileSource = ((AkylasTileSource*)[tileSources objectAtIndex:0]).tileSource;
//            if ([tileSource respondsToSelector:@selector(centerCoordinate)]) {
//                [map setCenterCoordinate:[(id)tileSource centerCoordinate]];
//            }
//            map.zoom = tileSource.minZoom;
//            map.minZoom = tileSource.minZoom;
//            map.maxZoom = tileSource.maxZoom;
//        }
    }
    else {
        [self map];
    }
}

-(void)setScrollableAreaLimit_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    RMSphericalTrapezium bounds = kMapboxDefaultLatLonBoundingBox;
    if (value!=nil)
	{
		bounds = [AkylasMapboxModule regionFromDict:value];
	}
    [[self map] setConstraintsSouthWest:bounds.southWest northEast:bounds.northEast];
}

-(void)setRegion_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    
//	id rf = [value objectForKey:@"regionFit"];
//	if (rf)
//	{
//		regionFits = [rf boolValue];
//	}
	if (value==nil)
	{
		// unset the region and set it back to the user's location of the map
		// what else to do??
		RMUserLocation* user = [self map].userLocation;
		if (user!=nil)
		{
//			region.center = user.location.coordinate;
			[self render];
		}
		else 
		{
			// if we unset but we're not allowed to get the users location, what to do?
		}
	}
	else 
	{
        id an = [value objectForKey:@"animate"];
        if (an)
        {
            animate = [an boolValue];
        }
		region = [AkylasMapboxModule regionFromDict:value];
		[self render];
	}
}

-(void)setAnimate_:(id)value
{
	animate = [TiUtils boolValue:value];
}

-(void)setRegionFit_:(id)value
{
    regionFits = [TiUtils boolValue:value];
    [self render];
}

-(void)setUserLocation_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSObject);
	[self map].showsUserLocation = [TiUtils boolValue:value];
}

-(void)setUserTrackingMode_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
	[self map].userTrackingMode = [TiUtils intValue:value def:RMUserTrackingModeNone];
}


-(void)setAdjustTilesForRetinaDisplay_:(id)value
{
	[self map].adjustTilesForRetinaDisplay = [TiUtils boolValue:value def:[self map].adjustTilesForRetinaDisplay];
}

-(void)setDebugTiles_:(id)debug
{
	[self map].debugTiles = [TiUtils boolValue:debug];
}

-(void)setZoom_:(id)zoom
{
    [[self map] setZoom:[TiUtils floatValue:zoom] animated:true];
}

-(void)setMinZoom_:(id)zoom
{
    [[self map] setMinZoom:[TiUtils floatValue:zoom]];
}

-(void)setMaxZoom_:(id)zoom
{
    [[self map] setMaxZoom:[TiUtils floatValue:zoom]];
}

- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated
{
    [[self map] zoomInToNextNativeZoomAt:pivot animated:animated];
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
    [[self map] zoomOutToNextNativeZoomAt:pivot animated:animated];
}


-(void)setCenterCoordinate_:(id)center
{
    if ([center isKindOfClass:[NSArray class]] && [center count] == 2) {
        [self map].centerCoordinate = CLLocationCoordinate2DMake([TiUtils floatValue:[center objectAtIndex:0]],[TiUtils floatValue:[center objectAtIndex:1]]);
    }
    else {
        [self map].centerCoordinate = [self map].userLocation.location.coordinate;
    }
    
}

-(id)centerCoordinate
{
    CLLocationCoordinate2D coord = [self map].centerCoordinate;
    return [AkylasMapboxModule dictFromLocation2D:coord];
}

-(void)addRoute:(AkylasMapboxRouteProxy*)route
{
	[self internalAddAnnotations:route];
}

-(void)removeRoute:(AkylasMapboxRouteProxy*)route
{
	[self internalRemoveAnnotations:route];
}

-(void)setTintColor_:(id)color
{
    [self map].tintColor = [TiUtils colorValue:color].color;
}

-(void)setDefaultPinImage_:(id)image
{
    self.defaultPinImage = [[[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self.proxy]] retain];
}
-(void)setDefaultPinAnchor_:(id)anchor
{
    self.defaultPinAnchor = [TiUtils pointValue:anchor];
}

#pragma mark Utils
// Using these utility functions allows us to override them for different versions of iOS

//-(void)addOverlay:(MKPolyline*)polyline level:(MKOverlayLevel)level
//{
//    [map addOverlay:polyline];
//}

-(NSDictionary*)dictFromLocation:(CLLocation*)location
{
    if (!location) return @{};
    return @{
         @"latitude":NUMDOUBLE(location.coordinate.latitude),
         @"longitude":NUMDOUBLE(location.coordinate.longitude),
         @"altitude":NUMDOUBLE(location.altitude),
         @"horizontalAccuracy":NUMDOUBLE(location.horizontalAccuracy),
         @"verticalAccuracy":NUMDOUBLE(location.verticalAccuracy),
         @"course":NUMDOUBLE(location.course),
         @"speed":NUMDOUBLE(location.speed),
         @"timestamp":NUMDOUBLE([location.timestamp timeIntervalSince1970]*1000),
         };
}

-(NSDictionary*)dictFromHeading:(CLHeading*)heading
{
    if (!heading) return @{};
    return @{
             @"magneticHeading":NUMDOUBLE(heading.magneticHeading),
             @"trueHeading":NUMDOUBLE(heading.trueHeading),
             @"headingAccuracy":NUMDOUBLE(heading.headingAccuracy),
             @"x":NUMDOUBLE(heading.x),
             @"y":NUMDOUBLE(heading.y),
             @"z":NUMDOUBLE(heading.z),
             @"timestamp":NUMDOUBLE([heading.timestamp timeIntervalSince1970]*1000),
             };
}

-(NSDictionary*)dictFromUserLocation:(RMUserLocation*)userLocation
{
    if (!userLocation) return @{};
    NSDictionary* result = [self dictFromLocation:userLocation.location] ;
    if (userLocation.heading) {
        NSMutableDictionary* mutDict = [NSMutableDictionary dictionaryWithDictionary:result];
        [mutDict setObject:[self dictFromHeading:userLocation.heading] forKey:@"heading"];
        result = mutDict;
    }
    return result;
}

#pragma mark Delegates

- (RMMapLayer *)mapView:(RMMapView *)mapView layerForAnnotation:(RMAnnotation *)annotation
{
    if (annotation.isUserLocationAnnotation)
        return nil;
    
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    return [proxy shapeLayerForMapView:self];
}

- (BOOL)mapView:(RMMapView *)mapView shouldDragAnnotation:(RMAnnotation *)annotation;
{
    if (annotation.isUserLocationAnnotation)
        return false;
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    if (proxy) return [proxy draggable];
    return false;
}

- (void)mapView:(RMMapView *)mapView didUpdateUserLocation:(RMUserLocation *)userLocation
{
    if ([self.proxy _hasListeners:@"userlocation"])
	{
		[self.proxy fireEvent:@"userlocation" withObject:[self dictFromUserLocation:userLocation] propagate:NO checkForListener:NO];
	}
}

- (void)mapView:(RMMapView *)mapView didFailToLocateUserWithError:(NSError *)error
{
    if ([self.proxy _hasListeners:@"userlocation"])
	{
		[self.proxy fireEvent:@"userlocation" withObject:@{@"error":[error description]} propagate:NO checkForListener:NO];
	}
}

- (void)mapView:(RMMapView *)mapView didChangeUserTrackingMode:(RMUserTrackingMode)mode animated:(BOOL)animated
{
    if ([self.proxy _hasListeners:@"usertracking"])
	{
		[self.proxy fireEvent:@"usertracking" withObject:@{@"mode":NUMINT(mode), @"animated":NUMBOOL(animated)} propagate:NO checkForListener:NO];
	}
}

- (void)mapViewRegionDidChange:(RMMapView *)map
{
	if (ignoreRegionChanged) {
        return;
    }
	if ([self.proxy _hasListeners:@"regionchanged"])
	{
		[self.proxy fireEvent:@"regionchanged" withObject:[self getRegion] propagate:NO checkForListener:NO];
	}
}


- (void)singleTapOnMap:(RMMapView *)mapView recognizer:(UIGestureRecognizer *)recognizer
{
    BOOL hasClick  = [self.proxy _hasListeners:@"click"];
    BOOL hasTap  = [self.proxy _hasListeners:@"singletap"];
	if (hasClick) [self fireEventOnMap:@"click" withRecognizer:recognizer];
	if (hasTap) [self fireEventOnMap:@"singletap" withRecognizer:recognizer];
}
- (void)doubleTapOnMap:(RMMapView *)map recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"doubleclick" withRecognizer:recognizer];
}

- (void)singleTapTwoFingersOnMap:(RMMapView *)map recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"singletap"  withRecognizer:recognizer];
}
- (void)longPressOnMap:(RMMapView *)map recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"longpress" withRecognizer:recognizer];
}
- (void)tapOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEvent:@"click" onAnnotation:annotation source:@"pin" withRecognizer:recognizer];
}

- (void)doubleTapOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:recognizer
{
    [self fireEvent:@"doubleclick" onAnnotation:annotation source:@"pin" withRecognizer:recognizer];
}

- (void)longPressOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:recognizer
{
    [self fireEvent:@"longpress" onAnnotation:annotation source:@"pin" withRecognizer:recognizer];
}

- (void)tapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:recognizer
{
    [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
}

- (void)doubleTapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:recognizer
{
    [self fireEvent:@"doubleclick" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
}

- (void)tapOnCalloutAccessoryControl:(UIControl *)control forAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)map recognizer:recognizer
{
    if ([annotation.userInfo isKindOfClass:[AkylasMapboxAnnotationProxy class]])
	{
        AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
        RMMarker* marker = [proxy marker];
        if (marker) {
            NSString * clickSource = @"unknown";
            if (marker.leftCalloutAccessoryView == control)
            {
                clickSource = @"leftButton";
            }
            else if (marker.rightCalloutAccessoryView == control)
            {
                clickSource = @"rightButton";
            }
            [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
        }
	}
}


- (void)mapView:(RMMapView *)mapView didSelectAnnotation:(RMAnnotation *)annotation {
    [self fireEvent:@"focus" onAnnotation:annotation source:@"pin" withRecognizer:nil];
}

- (void)mapView:(RMMapView *)mapView didDeselectAnnotation:(RMAnnotation *)annotation{
    [self fireEvent:@"blur" onAnnotation:annotation source:@"pin" withRecognizer:nil];
}

- (void)beforeMapZoom:(RMMapView *)map byUser:(BOOL)wasUserAction
{
    NSString* type = @"willZoom";
    if ([self.proxy _hasListeners:type]) {
        
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
}

- (void)afterMapZoom:(RMMapView *)map byUser:(BOOL)wasUserAction
{
    NSString* type = @"zoom";
    if ([self.proxy _hasListeners:type]) {
        
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
}

- (void)beforeMapMove:(RMMapView *)map byUser:(BOOL)wasUserAction;
{
    NSString* type = @"willMove";
    if ([self.proxy _hasListeners:type]) {
        
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
}
- (void)afterMapMove:(RMMapView *)map byUser:(BOOL)wasUserAction
{
    NSString* type = @"move";
    if ([self.proxy _hasListeners:type]) {
        
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
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
		[self.proxy fireEvent:@"error" withObject:event errorCode:[error code] message:message];
	}
}

- (void)mapView:(RMMapView *)mapView annotation:(RMAnnotation *)annotation didChangeDragState:(RMMapLayerDragState)newState fromOldState:(RMMapLayerDragState)oldState
{
	[self firePinChangeDragState:annotation newState:newState fromOldState:oldState];
}

- (void)firePinChangeDragState:(RMAnnotation *) annotation newState:(RMMapLayerDragState)newState fromOldState:(RMMapLayerDragState)oldState
{
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;

	if (proxy == nil)
		return;

	TiProxy * ourProxy = [self proxy];
	BOOL parentWants = [ourProxy _hasListeners:@"pinchangedragstate"];
	BOOL viewWants = [proxy _hasListeners:@"pinchangedragstate"];
	
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

// mapView:viewForAnnotation: provides the view for each annotation.
// This method may be called for all or some of the added annotations.
// For MapKit provided annotations (eg. MKUserLocation) return nil to use the MapKit provided annotation view.
//- (MKAnnotationView *)mapView:(MKMapView *)mapView viewForAnnotation:(id <MKAnnotation>)annotation
//{
//    if ([annotation isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
//        AkylasMapboxAnnotationProxy *ann = (AkylasMapboxAnnotationProxy*)annotation;
//        id customView = [ann valueForUndefinedKey:@"customView"];
//        if ( (customView == nil) || (customView == [NSNull null]) || (![customView isKindOfClass:[TiViewProxy class]]) ){
//            customView = nil;
//        }
//        NSString *identifier = nil;
//        UIImage* image = nil;
//        if (customView == nil) {
//            id imagePath = [ann valueForUndefinedKey:@"image"];
//            image = [TiUtils image:imagePath proxy:ann];
//            identifier = (image!=nil) ? @"AkylasMapbox-image":@"AkylasMapbox-pin";
//        }
//        else {
//            identifier = @"AkylasMapbox-customView";
//        }
//        MKAnnotationView *annView = nil;
//		
//        annView = (MKAnnotationView*) [mapView dequeueReusableAnnotationViewWithIdentifier:identifier];
//		
//        if (annView==nil) {
//            if ([identifier isEqualToString:@"AkylasMapbox-customView"]) {
//                annView = [[[AkylasMapboxCustomAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self] autorelease];
//            }
//            else if ([identifier isEqualToString:@"AkylasMapbox-image"]) {
//                annView=[[[AkylasMapboxImageAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self image:image] autorelease];
//            }
//            else {
//                annView=[[[AkylasMapboxPinAnnotationView alloc] initWithAnnotation:ann reuseIdentifier:identifier map:self] autorelease];
//            }
//        }
//        if ([identifier isEqualToString:@"AkylasMapbox-customView"]) {
//            [((AkylasMapboxCustomAnnotationView*)annView) setProxy:customView];
//        }
//        else if ([identifier isEqualToString:@"AkylasMapbox-image"]) {
//            annView.image = image;
//        }
//        else {
//            MKPinAnnotationView *pinview = (MKPinAnnotationView*)annView;
//            pinview.pinColor = [ann pinColor];
//            pinview.animatesDrop = [ann animatesDrop] && ![(AkylasMapboxAnnotationProxy *)annotation placed];
//            annView.calloutOffset = CGPointMake(-8, 0);
//        }
//        annView.canShowCallout = [TiUtils boolValue:[ann valueForUndefinedKey:@"canShowCallout"] def:YES];;
//        annView.enabled = YES;
//        annView.centerOffset = ann.offset;
//        UIView *left = [ann leftViewAccessory];
//        UIView *right = [ann rightViewAccessory];
//        if (left!=nil) {
//            annView.leftCalloutAccessoryView = left;
//        }
//        if (right!=nil) {
//            annView.rightCalloutAccessoryView = right;
//        }
//
//        BOOL draggable = [TiUtils boolValue: [ann valueForUndefinedKey:@"draggable"]];
//        if (draggable && [[MKAnnotationView class] instancesRespondToSelector:NSSelectorFromString(@"isDraggable")])
//            [annView performSelector:NSSelectorFromString(@"setDraggable:") withObject:[NSNumber numberWithBool:YES]];
//
//        annView.userInteractionEnabled = YES;
//        annView.tag = [ann tag];
//        return annView;
//    }
//    return nil;
//}


// mapView:didAddAnnotationViews: is called after the annotation views have been added and positioned in the map.
// The delegate can implement this method to animate the adding of the annotations views.
// Use the current positions of the annotation views as the destinations of the animation.
- (void)mapView:(MKMapView *)mapView didAddAnnotationViews:(NSArray *)views
{
	for (MKAnnotationView<AkylasMapboxAnnotation> *thisView in views)
	{
		if(![thisView conformsToProtocol:@protocol(AkylasMapboxAnnotation)])
		{
			return;
		}
        /*Image Annotation don't have any animation of its own. 
         *So in this case we do a custom animation, to place the 
         *image annotation on top of the mapview.*/
//        if([thisView isKindOfClass:[AkylasMapboxImageAnnotationView class]] || [thisView isKindOfClass:[AkylasMapboxCustomAnnotationView class]])
//        {
//            AkylasMapboxAnnotationProxy *anntProxy = [self proxyForAnnotation:thisView];
//            if([anntProxy animatesDrop] && ![anntProxy placed])
//            {
//                CGRect viewFrame = thisView.frame;
//                thisView.frame = CGRectMake(viewFrame.origin.x, viewFrame.origin.y - self.frame.size.height, viewFrame.size.width, viewFrame.size.height);
//                [UIView animateWithDuration:0.4 
//                                      delay:0.0 
//                                    options:UIViewAnimationCurveEaseOut 
//                                 animations:^{thisView.frame = viewFrame;}
//                                 completion:nil];
//            }
//        }
		AkylasMapboxAnnotationProxy * thisProxy = [self proxyForAnnotation:thisView];
		[thisProxy setPlaced:YES];
	}
}

#pragma mark Event generation

- (void)fireEvent:(NSString*)type onAnnotation:(RMAnnotation *) pinview source:(NSString *)source withRecognizer:(UIGestureRecognizer*)recognizer
{
	if (ignoreClicks)
	{
		return;
	}

	AkylasMapboxAnnotationProxy *viewProxy = pinview.userInfo;
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
	
    CGPoint point = [recognizer locationInView:self];
    NSMutableDictionary *event = [[TiUtils dictionaryFromGesture:recognizer inView:self] mutableCopy];
    [event addEntriesFromDictionary:[AkylasMapboxModule dictFromLocation2D:[map pixelToCoordinate:point]]];
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
        [event addEntriesFromDictionary:[AkylasMapboxModule dictFromLocation2D:[map pixelToCoordinate:point]]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
	}
}

@end
