//
//  AkylasMapMapboxView.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasGooglemapView.h"
#import "AkylasGooglemapAnnotationProxy.h"
#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasGooglemapRouteProxy.h"
#import "AkylasGooglemapModule.h"
#import "TiApp.h"



@implementation AkylasGMSMapView
-(void)dealloc
{
    RELEASE_TO_NIL(_tileCache)
    [super dealloc];
}
@end

GMSCoordinateBounds* boundsFromRegion(AkRegion trapez)
{
    return [[[GMSCoordinateBounds alloc] initWithCoordinate:trapez.northEast coordinate:trapez.southWest] autorelease];
}

@implementation AkylasGooglemapView
{
    AkylasGMSMapView *map;
    BOOL _shouldFollowUserLocation;
    NSInteger _userTrackingMode;
    CGFloat _userLocationRequiredZoom;
    BOOL _inUserAction;
    
    SMCalloutView* _calloutView;
    UIView* calloutTouchedView;
    UIView* _emptyCalloutView;
    
    AkMapDragState _dragState;
}

- (id)init
{
    if ((self = [super init])) {
        _shouldFollowUserLocation = YES;
        _inUserAction = NO;
        _emptyCalloutView = [[UIView alloc] initWithFrame:CGRectZero];
        _dragState = AkMapDragStateNone;
    }
    return self;
}

-(void)dealloc
{
	if (map!=nil)
	{
        [[NSNotificationCenter defaultCenter] removeObserver:self name:kTiNetworkChangedNotification object:nil];
        if (_userTrackingMode != AkUserTrackingModeNone) {
            [map removeObserver:self forKeyPath:@"myLocation"];
        }
        [map removeObserver:self forKeyPath:@"selectedMarker"];
        [map clear];
//        [map stopRendering] ;
		map.delegate = nil;
        _internalZoom = map.camera.zoom;
        RELEASE_TO_NIL(map);
	}
    if (_calloutView) {
        _calloutView.delegate = nil;
        RELEASE_TO_NIL(_calloutView)
    }
    RELEASE_TO_NIL(_emptyCalloutView)
	[super dealloc];
}

- (UIGestureRecognizer *)gestureRecognizerForEvent:(NSString *)event
{
    if ([event isEqualToString:@"longpress"] ||
        [event isEqualToString:@"click"] ||
        [event isEqualToString:@"singletap"]) {
        return nil;
    }

    return [super gestureRecognizerForEvent:event];
}

-(UIView*)viewForHitTest
{
    return map;
}

- (void)tintColorDidChange
{
    if (_calloutView)
        _calloutView.tintColor = self.tintColor;
}


- (void)didReceiveMemoryWarning
{
    [[map tileCache] didReceiveMemoryWarning];
}

-(GMSMapView*)map
{
    if (map==nil)
    {
        map = [[AkylasGMSMapView alloc] initWithFrame:self.bounds];

        [map addObserver:self forKeyPath:@"selectedMarker" options:0 context:nil];
        [map setTileCache:[[TiCache alloc] initWithConfig:@[@{@"type":@"db-cache", @"name":@"AkGMSCache"}] expiryPeriod:0]];
        [self addSubview:map];
        map.delegate = self;
        map.networkConnected = [[TiApp app] networkConnected];
        //Initialize loaded state to YES. This will automatically go to NO if the map needs to download new data
        loaded = YES;
        [[map settings] setConsumesGesturesInView:NO];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(networkChanged:) name:kTiNetworkChangedNotification object:nil];
    }
    return map;
}

-(UIView *)hitTest:(CGPoint) point withEvent:(UIEvent *)event
{
    UIView *hitView = [super hitTest:point withEvent:event];
    if (!([hitView isKindOfClass:[UIControl class]]) && [_calloutView pointInside:[_calloutView convertPoint:point fromView:self] withEvent:event]) {
        calloutTouchedView = hitView;
    }
    else {
        calloutTouchedView = nil;
    }
    BOOL test = [hitView isUserInteractionEnabled];
    
    return hitView;
}

- (id)accessibilityElement
{
	return [self map];
}

//
//-(void)networkChanged:(NSNotification*)note
//{
//    BOOL connected = [[note.userInfo objectForKey:@"online"] boolValue];
//    if (map!=nil)
//	{
//        __block BOOL needsUpdate = NO;
//        [map.tileSources enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
//            if ([obj isKindOfClass:[RMAbstractWebMapSource class]]) {
//                needsUpdate = needsUpdate || [((RMAbstractWebMapSource*)obj) onNetworkChange:connected];
//            }
//        }];
//        if (needsUpdate) {
////            [map setTileSources:map.tileSources];
////            if ([self.proxy valueForUndefinedKey:@"region"]) {
////                [self setRegion_:[self.proxy valueForUndefinedKey:@"region"]];
////            }
////            if ([self.proxy valueForUndefinedKey:@"zoom"]) {
////                [self setZoom_:[self.proxy valueForUndefinedKey:@"zoom"]];
////            }
////            if ([self.proxy valueForUndefinedKey:@"centerCoordinate"]) {
////                [self setCenterCoordinate_:[self.proxy valueForUndefinedKey:@"centerCoordinate"]];
////            }
//        }
//    }
//}

//-(void)setBounds:(CGRect)bounds
//{
//    //TIMOB-13102.
//    //When the bounds change the mapview fires the regionDidChangeAnimated delegate method
//    //Here we update the region property which is not what we want.
//    //Instead we set a forceRender flag and render in frameSizeChanged and capture updated
//    //region there.
////    ignoreRegionChanged = !_cameraUpdate;
//    [super setBounds:bounds];
////    ignoreRegionChanged = NO;
//}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    BOOL animating = [self animating];
    
    
    //if we are animating it means we want to keep the zoom for sure...
    [[self map] setFrame:bounds];
    [super frameSizeChanged:frame bounds:bounds];
//    if (_cameraUpdate) {
//        [self handleCameraUpdate];
//        _needsRegionUpdate = NO;
//        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.0 * NSEC_PER_SEC);
//        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
//            
//            [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast regionFit:regionFits animated:NO roundOutZoom:YES];
//        });
        
//    }
}

//-(void)handleCameraUpdate {
//    if (!_needsCameraUpdate) {
//        return;
//    }
//    
//    BOOL shouldAnimate = _cameraAnimate || animate;
//    
//    
//    
//    if (shouldAnimate) {
//        if (_cameraUpdate) {
//            [map animateWithCameraUpdate:_cameraUpdate];
//        }
//    } else {
//        if (_cameraUpdate) {
//            [map moveCamera:_cameraUpdate];
//        }
//    }
//    RELEASE_TO_NIL(_cameraUpdate)
////    _cameraRegionUpdate = NO;
//    _cameraAnimate = NO;
////    _cameraRegion = nil;
////    _cameraCenter = kCLLocationCoordinate2DInvalid;
//    _needsCameraUpdate = NO;
//}


-(void)configurationStart
{
    ignoreRegionChanged = YES;
    [CATransaction begin];
    if (!animate) {
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
    }
    [super configurationStart];
}

-(void)configurationSet
{
    ignoreRegionChanged = NO;
    [CATransaction commit];
	[super configurationSet];
}

-(void)networkChanged:(NSNotification*)note
{
    BOOL connected = [[note.userInfo objectForKey:@"online"] boolValue];
    if (map!=nil)
    {
        map.networkConnected = connected;
    }
}

#pragma mark Properties


-(void)setAnimateChanges_:(id)value
{
    animate = [TiUtils boolValue:value];
    if (!configurationSet) {
        if (animate) {
            [CATransaction setValue:(id)kCFBooleanFalse forKey:kCATransactionDisableActions];
        } else  {
            [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
        }
    }
}

-(CLLocationCoordinate2D)centerCoordinate {
    return [self map].camera.target;
}

-(id)metersPerPixel_
{
    CGFloat px = [[self map].projection pointsForMeters:1 atCoordinate:[self map].camera.target];
    return @(1.0f/px);
}


-(void)setScrollableAreaLimit_:(id)value
{
//    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
//    RMSphericalTrapezium bounds = kMapboxDefaultLatLonBoundingBox;
//    if (value!=nil)
//	{
//		bounds = [AkylasMapModule regionFromObject:value];
//	}
//    [[self map] setConstraintsSouthWest:bounds.southWest northEast:bounds.northEast];
}

-(void)setRegion_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    ENSURE_UI_THREAD_1_ARG(value)
    [self setShouldFollowUserLocation:NO];
	if (value==nil)
	{
        return;
	}
    
    GMSCameraPosition* position = [[self map] cameraForBounds:boundsFromRegion([AkylasGooglemapModule regionFromObject:value]) insets:UIEdgeInsetsZero];
//    if (!configurationSet) {
//        _needsCameraUpdate = YES;
//        _cameraUpdate = [[GMSCameraUpdate setCamera:position] retain];
//        return;
//    }
    if (animate || !configurationSet) {
        [[self map] animateToCameraPosition:position];
    } else {
        [self map].camera = position;
    }
}


-(void)setConstraintRegionFit_:(id)value
{
//	[self map].constraintRegionFit = [TiUtils boolValue:value];
}

-(void)setUserLocationEnabled_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSObject);
    ENSURE_UI_THREAD_1_ARG(value)
    [self map].myLocationEnabled = [TiUtils boolValue:value];
}


-(id)userLocationEnabled_
{
    return NUMBOOL([self map].myLocationEnabled);
}

-(id)userLocation_
{
    return [AkylasGooglemapModule dictFromLocation:[self map].myLocation];
}


-(void)setUserTrackingMode_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
    NSInteger old = _userTrackingMode;
    _userTrackingMode = [TiUtils intValue:value def:AkUserTrackingModeNone];
    if (old != _userTrackingMode) {
        if (_userTrackingMode == AkUserTrackingModeNone) {
            [[self map] removeObserver:self forKeyPath:@"myLocation"];
            [self setShouldFollowUserLocation:NO];
        } else {
            [[self map] addObserver:self forKeyPath:@"myLocation" options:0 context:nil];
            [self setShouldFollowUserLocation:YES];
        }

    } else {
        [self setShouldFollowUserLocation:YES];
    }
}

-(void)setUserLocationRequiredZoom_:(id)value
{
    ENSURE_SINGLE_ARG(value,NSNumber);
    _userLocationRequiredZoom = [TiUtils floatValue:value def:10];
}

-(id)userTrackingMode_
{
    return NUMINTEGER(_userTrackingMode);
}


-(void)setMapType_:(id)value
{
    NSUInteger type = [TiUtils intValue:value def:kAkMapTypeNormal];
    switch (type) {
        case kAkMapTypeHybrid:
            type = kGMSTypeHybrid;
            break;
        case kAkMapTypeSatellite:
            type = kGMSTypeSatellite;
            break;
        case kAkMapTypeNone:
            type = kGMSTypeNone;
        case kAkMapTypeTerrain:
            type = kGMSTypeTerrain;
            break;
        default:
            type = kGMSTypeNormal;
           break;
    }
    [self map].mapType = (GMSMapViewType)type;
}

-(void)setZoom_:(id)value
{
    CGFloat newValue = [TiUtils floatValue:value def:_internalZoom];
    ENSURE_UI_THREAD_1_ARG(value)
    if (newValue == _internalZoom) return;
    //    if (!configurationSet) {
    //        _needsCameraUpdate = YES;
    //        return;
    //    }
    if (animate || !configurationSet) {
        [[self map] animateToZoom:newValue];
    } else {
        [[self map] moveCamera:[GMSCameraUpdate zoomTo:newValue]];
    }
}

-(void)setBearing_:(id)value
{
    CGFloat newValue = [TiUtils floatValue:value def:0];
    ENSURE_UI_THREAD_1_ARG(value)
    if (animate || !configurationSet) {
        [[self map] animateToBearing:newValue];
    } else {
        GMSCameraPosition* pos = [self map].camera;
        [[self map] setCamera:[GMSCameraPosition cameraWithLatitude:pos.target.latitude longitude:pos.target.longitude zoom:pos.zoom bearing:newValue viewingAngle:pos.viewingAngle]];
    }
}

-(void)setTilt_:(id)value
{
    CGFloat newValue = [TiUtils floatValue:value def:0];
    ENSURE_UI_THREAD_1_ARG(value)
    if (animate || !configurationSet) {
        [[self map] animateToViewingAngle:newValue];
    } else {
        GMSCameraPosition* pos = [self map].camera;
        [[self map] setCamera:[GMSCameraPosition cameraWithLatitude:pos.target.latitude longitude:pos.target.longitude zoom:pos.zoom bearing:pos.bearing viewingAngle:newValue]];
    }
}

-(void)setCenterCoordinate_:(id)value
{
    ENSURE_UI_THREAD_1_ARG(value)
    [self setShouldFollowUserLocation:NO];
    CLLocationCoordinate2D coord = [AkylasGooglemapModule locationFromObject:value];
    if (animate || !configurationSet) {
        [[self map] animateToLocation:coord];
    } else {
        [[self map] moveCamera:[GMSCameraUpdate setTarget:coord]];
    }
}

-(id)centerCoordinate_
{
    return [AkylasGooglemapModule dictFromLocation2D:[self centerCoordinate]];
}

-(void)setMinZoom_:(id)zoom
{
    
    [[self map] setMinZoom:[TiUtils floatValue:zoom] maxZoom:[self map].maxZoom];
}

-(void)setMaxZoom_:(id)zoom
{
    [[self map] setMinZoom:[self map].minZoom maxZoom:[TiUtils floatValue:zoom]];
}

-(void)setTintColor_:(id)color
{
    [self map].tintColor = [TiUtils colorValue:color].color;
}

//-(void)setUserLocationButton_:(id)value
//{
//    [self map].settings.myLocationButton = [TiUtils boolValue:value];
//}

-(void)setCompass_:(id)value
{
    [self map].settings.compassButton = [TiUtils boolValue:value];
}

-(void)setPadding_:(id)value
{
    [self map].padding = [TiUtils insetValue:value];
}

-(void)setConsumesGesturesInView_:(id)value
{
    [self map].settings.consumesGesturesInView = [TiUtils boolValue:value];
}

-(void)setBuildingsControls_:(id)value
{
    [self map].settings.indoorPicker = [TiUtils boolValue:value];
}

-(void)setScrollEnabled_:(id)value
{
    [self map].settings.scrollGestures = [TiUtils boolValue:value];
}

-(void)setZoomEnabled_:(id)value
{
    [self map].settings.zoomGestures = [TiUtils boolValue:value];
}

-(void)setRotateEnabled_:(id)value
{
    [self map].settings.rotateGestures = [TiUtils boolValue:value];
}

-(void)setTiltEnabled_:(id)value
{
    [self map].settings.tiltGestures = [TiUtils boolValue:value];
}

-(void)setBuildings_:(id)value
{
    [self map].buildingsEnabled = [TiUtils boolValue:value];
}

-(void)setIndoor_:(id)value
{
    [self map].indoorEnabled = [TiUtils boolValue:value];
}

-(void)setTraffic_:(id)value
{
    [self map].trafficEnabled = [TiUtils boolValue:value];
}

#pragma mark Methods

- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated
{
    [CATransaction begin];
    if (!animated) {
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
    }
    [[self map] animateWithCameraUpdate:[GMSCameraUpdate zoomIn]];
    [[self map] animateToLocation:[[self map].projection coordinateForPoint:pivot]];
    [CATransaction commit];
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
    [CATransaction begin];
    if (!animated) {
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
    }
    [[self map] animateWithCameraUpdate:[GMSCameraUpdate zoomOut]];
    [[self map] animateToLocation:[[self map].projection coordinateForPoint:pivot]];
    [CATransaction commit];
}

-(void)selectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
    ENSURE_UI_THREAD(selectAnnotation,args);
    
    if (args == nil) {
        [self map].selectedMarker = nil;
    }
    
    if ([args isKindOfClass:[AkylasMapBaseAnnotationProxy class]])
    {
        [self setSelectedAnnotation:args];
    }
}

-(void)deselectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG(args,NSObject);
    ENSURE_UI_THREAD(deselectAnnotation,args);
    
    [self map].selectedMarker = nil;
    
}

-(void)zoomTo:(id)args
{
    ENSURE_SINGLE_ARG(args,NSObject);
    ENSURE_UI_THREAD(zoomTo,args);
    
    double zoom = [TiUtils doubleValue:args];
    if (animate) {
        [[self map] animateToZoom:zoom];
    } else {
        [[self map] moveCamera:[GMSCameraUpdate zoomTo:zoom]];
    }
}

#pragma mark Internal


-(void)setShouldFollowUserLocation:(BOOL)value
{
    if (_shouldFollowUserLocation != value) {
        _shouldFollowUserLocation = value;
        
        if ([self.viewProxy _hasListeners:@"usertracking" checkParent:NO])
        {
            [self.proxy fireEvent:@"usertracking" withObject:@{@"mode":NUMINTEGER(_shouldFollowUserLocation?_userTrackingMode : AkUserTrackingModeNone), @"animated":NUMBOOL(animate)} propagate:NO checkForListener:NO];
        }
    }
}

- (void)setCenterCoordinate:(CLLocationCoordinate2D)centerCoordinate animated:(BOOL)animated
{
    [self setShouldFollowUserLocation:NO];
    //    if (!configurationSet) {
    //        _needsCameraUpdate = YES;
    //        _cameraUpdate = [[GMSCameraUpdate setTarget:centerCoordinate] retain];
    //        return;
    //    }
    if (animated) {
        [[self map] animateToLocation:centerCoordinate];
    } else {
        [[self map] moveCamera:[GMSCameraUpdate setTarget:centerCoordinate]];
    }
}

-(AkRegion) getCurrentRegion
{
    GMSVisibleRegion visible = [self map].projection.visibleRegion;
    return ((AkRegion){.northEast = {.latitude = visible.farRight.latitude, .longitude = visible.farRight.longitude}, .southWest = {.latitude = visible.nearLeft.latitude, .longitude = visible.nearLeft.longitude}});
}

-(BOOL)shouldAnimate
{
    return [self viewInitialized] && animate;
}

-(void)selectUserAnnotation
{
    if ([self map].myLocationEnabled) {
        CLLocation* location = map.myLocation;
        if (location) {
            [self setCenterCoordinate:location.coordinate animated:[self shouldAnimate]];
        }
    }
}

-(void)refreshAnnotation:(AkylasGooglemapAnnotationProxy*)proxy readd:(BOOL)yn
{
    GMSOverlay *newSelected = [proxy getGOverlayForMapView:[self map]];
    if (!IS_OF_CLASS(newSelected, GMSMarker)) return;
//    GMSMarker *selected = map.selectedMarker;
//    BOOL wasSelected = newSelected == selected; //If selected == nil, this still returns FALSE.
//    ignoreClicks = YES;
//    
//    if (yn)
//    {
//        if (wasSelected) {
//            map.selectedMarker = nil;
//        }
//        newSelected.map = nil;
//        newSelected.map = [self map];
//        if (wasSelected)
//        {
//            map.selectedMarker = (GMSMarker*)newSelected;
//        }
//    }
    [((GMSMarker*)newSelected).layer setNeedsLayout];
    
//    ignoreClicks = NO;
}


-(void)internalAddAnnotations:(id)annotations atIndex:(NSInteger)index
{
    __block NSInteger realIndex = index;
    GMSMapView* mapView = [self map];
    if (IS_OF_CLASS(annotations, NSArray)) {
        [annotations enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self internalAddAnnotations:obj atIndex:(realIndex >= 0)?realIndex++:realIndex];
        }];
    }
    else {
        GMSMapView* mapView = [self map];
        GMSOverlay* overlay = [(AkylasGooglemapAnnotationProxy*)annotations getGOverlayForMapView:mapView];
        if (((AkylasMapBaseAnnotationProxy*)annotations).zIndex == -1) {
            overlay.zIndex = (int)(realIndex + [[annotations class] gZIndexDelta]);
        } else {
            overlay.zIndex = (int)((AkylasGooglemapAnnotationProxy*)annotations).zIndex;
        }
        [overlay setMap:mapView];
//        if (realIndex >= 0) {
//            overlay.zIndex = (overlay.zIndex > 0) ? overlay.zIndex : (int)realIndex;
//        }
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapBaseAnnotationProxy* annotProxy in annotations) {
            if ([annotProxy isKindOfClass:[AkylasGooglemapAnnotationProxy class]]) {
                [[(AkylasGooglemapAnnotationProxy*)annotProxy gOverlay] setMap:nil];
            }
        }
    }
    else if ([annotations isKindOfClass:[AkylasGooglemapAnnotationProxy class]]) {
        [[(AkylasGooglemapAnnotationProxy*)annotations gOverlay] setMap:nil];
    }
}

-(void)internalRemoveAllAnnotations
{
    GMSMapView* mapView = [self map];
    [mapView clear];
}

-(void)internalAddRoutes:(id)routes atIndex:(NSInteger)index
{
    [self internalAddAnnotations:routes atIndex:index];
}

-(void)internalRemoveRoutes:(id)routes
{
    [self internalRemoveAnnotations:routes];
}

-(void)internalRemoveAllRoutes
{
    //    RMMapView* mapView = [self map];
    //    [mapView removeAllAnnotationsOfClass:[RMRouteAnnotation class]];
}


-(void)setSelectedAnnotation:(AkylasGooglemapAnnotationProxy*)annotation
{
    GMSMapView* mapView = [self map];
    GMSOverlay *marker = [annotation getGOverlayForMapView:mapView];
    if (!IS_OF_CLASS(marker, GMSMarker)) return;
    mapView.selectedMarker = (GMSMarker*)marker;
    //    if (!annotation.marker.canShowCallout) {
    //        map.centerCoordinate = an.coordinate;
    //    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if([keyPath isEqualToString:@"myLocation"]) {
        CLLocation *location = [object myLocation];
        //...
        NSLog(@"Location, %@,", location);
        
        CLLocationCoordinate2D target =
        CLLocationCoordinate2DMake(location.coordinate.latitude, location.coordinate.longitude);
        
        if (_shouldFollowUserLocation && _userTrackingMode != AkUserTrackingModeNone) {
            GMSMutableCameraPosition* position = [[GMSMutableCameraPosition alloc] init];
//            GMSMutablePosition* currentposition = [self map].camera;
            
            position.target = location.coordinate;
            if (_userTrackingMode == AkUserTrackingModeFollowWithHeading) {
                position.bearing = location.course;
            }
            
            float currentZoom = (_internalZoom != -1) ? _internalZoom : 0;
            if (currentZoom < _userLocationRequiredZoom) {
                if (location.horizontalAccuracy > 0) {
                    // approx meterPerDegree latitude, plus some margin
                    double delta = (location.horizontalAccuracy / 110000) * 1.2;
                    GMSVisibleRegion currentBox = [[[self map] projection] visibleRegion];
                    CLLocationCoordinate2D desiredSouthWest = CLLocationCoordinate2DMake(location.coordinate.latitude
                                                         - delta, location.coordinate.longitude - delta);
                    
                    CLLocationCoordinate2D desiredNorthEast = CLLocationCoordinate2DMake(location.coordinate.latitude
                                                         + delta, location.coordinate.longitude + delta);
                    
                    if (desiredNorthEast.latitude != currentBox.farRight.latitude
                        || desiredNorthEast.longitude != currentBox.farRight.longitude
                        || desiredSouthWest.latitude != currentBox.nearLeft.latitude
                        || desiredSouthWest.longitude != currentBox.nearLeft.longitude) {
                        position.zoom = _userLocationRequiredZoom;
                    }
                    
                } else {
                    position.zoom = _userLocationRequiredZoom;
                }
                
            } else {
                position.zoom = [self map].camera.zoom;
            }
            if ([self map].camera) {
                position.viewingAngle = [self map].camera.viewingAngle;
            }
            
            if (animate || !configurationSet) {
                [[self map] animateToCameraPosition:position];
            } else {
                [[self map] setCamera:position];
            }
        }
        if ([self.viewProxy _hasListeners:@"location" checkParent:NO])
        {
            [self.proxy fireEvent:@"location" withObject:[AkylasGooglemapModule dictFromLocation:location] propagate:NO checkForListener:NO];
        }
    } else if([keyPath isEqualToString:@"selectedMarker"]) {

        GMSMarker *test = [object selectedMarker];
        GMSMarker *oldMarker = [change objectForKey:NSKeyValueChangeOldKey];
        GMSMarker *newMarker = [change objectForKey:NSKeyValueChangeNewKey];
        if (oldMarker) {
            [self fireEvent:@"blur" onOverlay:oldMarker source:@"pin"];
        }
        if (newMarker)
        {
            [self fireEvent:@"focus" onOverlay:oldMarker source:@"pin"];
        }
    }
}

- (BOOL)internalAddTileSources:(id)tileSource atIndex:(NSInteger)index
{
    __block NSInteger realIndex = index;
    if (IS_OF_CLASS(tileSource, NSArray)) {
        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self internalAddTileSources:obj atIndex:(realIndex >= 0)?realIndex++:realIndex];
        }];
    } else {
        GMSMapView* mapView = [self map];
        GMSTileLayer* layer = [(AkylasGooglemapTileSourceProxy*)tileSource getGTileLayerForMapView:mapView];
        [layer setMap:mapView];
        if (((AkylasMapBaseTileSourceProxy*)tileSource).zIndex == -1) {
            layer.zIndex = (int)realIndex;
        } else {
            layer.zIndex = (int)((AkylasMapBaseTileSourceProxy*)tileSource).zIndex;
        }

    }
}

- (BOOL)internalRemoveTileSources:(id)tileSource
{
    if (IS_OF_CLASS(tileSource, NSArray)) {
        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self internalRemoveTileSources:obj];
        }];
    } else {
        [[(AkylasGooglemapTileSourceProxy*)tileSource gTileLayer] setMap:nil];
    }
}

- (BOOL)internalRemoveAllTileSources
{
//    if (_userStackTileSource) {
//        if (_tileSourceContainer) {
//            [[self map] removeTileSource:_tileSourceContainer];
//            RELEASE_TO_NIL(_tileSourceContainer)
//        }
//    } else {
//        [[self map] setTileSources:nil];
//    }
}

#pragma mark Event generation

- (BOOL)fireEvent:(NSString*)type onOverlay:(GMSOverlay *) overlay source:(NSString *)source
{
    if (ignoreClicks)
    {
        return;
    }
    
    AkylasMapBaseAnnotationProxy *annotProxy = IS_OF_CLASS(overlay, GMSMarker)? ((GMSMarker*)overlay).userData: nil;
    if (annotProxy == nil)
    {
        return NO;
    }
    
    TiViewProxy * ourProxy = [self viewProxy];
    BOOL parentWants = [ourProxy _hasListeners:type checkParent:NO];
    BOOL viewWants = [annotProxy _hasListeners:type checkParent:NO];
    if(!parentWants && !viewWants)
    {
        return NO;
    }
    
    CGPoint point = [[map projection] pointForCoordinate:((GMSMarker*)overlay).position];
    NSMutableDictionary *event = [TiUtils dictionaryFromPoint:point inView:map];
    [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:[[map projection] coordinateForPoint:point]]];
    if (IS_OF_CLASS(annotProxy, AkylasMapBaseRouteProxy)) {
        [event setObject:annotProxy forKey:@"route"];
    } else {
        [event setObject:annotProxy forKey:@"annotation"];
        [event setObject:NUMINTEGER([annotProxy tag]) forKey:@"index"];
        if ([annotProxy title] == nil)
        {
            [event setObject:[annotProxy title] forKey:@"title"];
        }
    }
    if (source) {
        [event setObject:source forKey:@"clicksource"];
    }
    [event setObject:ourProxy forKey:@"map"];
    if (parentWants)
    {
        [ourProxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
    if (viewWants)
    {
        [annotProxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
    return YES;
}

- (void)fireEventOnMap:(NSString*)type atCoordinate:(CLLocationCoordinate2D)coordinate
{
    if (ignoreClicks)
    {
        return;
    }
    
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        NSMutableDictionary *event = [TiUtils dictionaryFromPoint:[[map projection] pointForCoordinate:coordinate] inView:map];
        [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:coordinate]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}


#pragma mark Delegates


//- (void)doubleTapOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
//{
//    [self fireEventOnMap:@"doubleclick" withRecognizer:recognizer];
//}
//
//- (void)singleTapTwoFingersOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
//{
//    [self fireEventOnMap:@"singletap"  withRecognizer:recognizer];
//}
//
//- (void)doubleTapOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
//{
//    if (![self fireEvent:@"doubleclick" onAnnotation:annotation source:@"pin" withRecognizer:recognizer]) {
//        [theMap doubleTapWithGesture:recognizer];
//    }
//}

//- (void)tapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
//{
//    [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
//}
//
//- (void)doubleTapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
//{
//    [self fireEvent:@"doubleclick" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
//}

//- (void)tapOnCalloutAccessoryControl:(UIControl *)control forAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
//{
//    if ([annotation.userInfo isKindOfClass:[AkylasMapAnnotationProxy class]])
//	{
//        AkylasMapAnnotationProxy* proxy = annotation.userInfo;
//        RMMarker* marker = [proxy marker];
//        if (marker) {
//            NSString * clickSource = @"unknown";
//            if (marker.leftCalloutAccessoryView == control)
//            {
//                clickSource = @"leftButton";
//            }
//            else if (marker.rightCalloutAccessoryView == control)
//            {
//                clickSource = @"rightButton";
//            }
//            [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
//        }
//	}
//}

//- (void)mapView:(RMMapView *)theMap didSelectAnnotation:(RMAnnotation *)annotation {
//    [self fireEvent:@"focus" onAnnotation:annotation source:@"pin"];
//}
//
//- (void)mapView:(RMMapView *)theMap didDeselectAnnotation:(RMAnnotation *)annotation{
//    [self fireEvent:@"blur" onAnnotation:annotation source:@"pin"];
//}

//- (void)beforeMapZoom:(RMMapView *)theMap byUser:(BOOL)wasUserAction
//{
//    NSString* type = @"willZoom";
//    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
//        
//		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//                               NUMBOOL(wasUserAction),@"userAction",
//                               nil
//                               ];
//        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
//	}
//}
//- (void)afterMapZoom:(RMMapView *)theMap byUser:(BOOL)wasUserAction
//{
//    NSString* type = @"zoom";
//    _internalZoom = theMap.adjustedZoomForRetinaDisplay;
//    [self.proxy replaceValue:NUMFLOAT(_internalZoom) forKey:type notification:NO];
//    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
//        
//		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//                               NUMBOOL(wasUserAction),@"userAction",
//                               NUMFLOAT(_internalZoom), @"zoom",
//                               nil
//                               ];
//        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
//	}
//}
//
//- (void)beforeMapMove:(RMMapView *)theMap byUser:(BOOL)wasUserAction;
//{
//    NSString* type = @"willMove";
//    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
//        
//		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//                               NUMBOOL(wasUserAction),@"userAction",
//                               nil
//                               ];
//        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
//	}
//}
//- (void)afterMapMove:(RMMapView *)theMap byUser:(BOOL)wasUserAction
//{
//    NSString* type = @"move";
//    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
//        
//		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//                               NUMBOOL(wasUserAction),@"userAction",
//                               nil
//                               ];
//        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
//	}
//}

//- (void)mapViewWillStartLoadingMap:(MKMapView *)mapView
//{
//	loaded = NO;
//	if ([self.proxy _hasListeners:@"loading"])
//	{
//		[self.proxy fireEvent:@"loading" propagate:NO checkForListener:NO];
//	}
//}
//
//- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView
//{
//	ignoreClicks = YES;
//	loaded = YES;
//	if ([self.proxy _hasListeners:@"complete"])
//	{
//		[self.proxy fireEvent:@"complete" propagate:NO checkForListener:NO];
//	}
//	ignoreClicks = NO;
//}

//- (void)mapViewDidFailLoadingMap:(MKMapView *)mapView withError:(NSError *)error
//{
//	if ([self.proxy _hasListeners:@"error"])
//	{
//		NSString * message = [TiUtils messageFromError:error];
//		NSDictionary *event = [NSDictionary dictionaryWithObject:message forKey:@"message"];
//		[self.proxy fireEvent:@"error" withObject:event errorCode:[error code] message:message];
//	}
//}

/**
 * Called before the camera on the map changes, either due to a gesture,
 * animation (e.g., by a user tapping on the "My Location" button) or by being
 * updated explicitly via the camera or a zero-length animation on layer.
 *
 * @param gesture If YES, this is occuring due to a user gesture.
 */
- (void)mapView:(GMSMapView *)mapView willMove:(BOOL)gesture {
//    NSString* type = @"willMove";
//    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
//        
//        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//                               NUMBOOL(gesture),@"userAction",
//                               nil
//                               ];
//        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
//    }
//    
    _inUserAction = gesture;
}

/**
 * Called repeatedly during any animations or gestures on the map (or once, if
 * the camera is explicitly set). This may not be called for all intermediate
 * camera positions. It is always called for the final position of an animation
 * or gesture.
 */
- (void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)position {
//    region = regionFromMKRegion([mapView region]);
    if (mapView.selectedMarker != nil && _calloutView && !_calloutView.hidden) {
        CLLocationCoordinate2D anchor = [mapView.selectedMarker position];
        GMSMarker* marker = mapView.selectedMarker;
        AkylasMapBaseAnnotationProxy *annProxy = marker.userData;
       
        // Apply the desired calloutOffset (from the top-middle of the view)
        CGPoint calloutOffset = marker.infoWindowAnchor;
        CGSize size = marker.layer.frame.size;
        if (CGSizeEqualToSize(size, CGSizeZero)) {
            size = [annProxy getSize];
        }
        calloutOffset.x *= size.width;
        calloutOffset.y *= -size.height;
        
//        _calloutView.calloutOffset = calloutOffset;
        
        CGPoint arrowPt = _calloutView.backgroundView.arrowPoint;
        CGPoint pt = [mapView.projection pointForCoordinate:anchor];
        
        pt.x -= arrowPt.x - calloutOffset.x;
        pt.y -= arrowPt.y - calloutOffset.y;
        

        _calloutView.frame = (CGRect) {.origin = pt, .size = _calloutView.frame.size };
    } else {
        [_calloutView dismissCalloutAnimated:YES];
        if (_calloutUseTemplates) {
            [self reuseIfNecessary:_calloutView.leftAccessoryView];
            [self reuseIfNecessary:_calloutView.rightAccessoryView];
            [self reuseIfNecessary:_calloutView.contentView];
            _calloutView.leftAccessoryView = nil;
            _calloutView.rightAccessoryView = nil;
            _calloutView.contentView = nil;
        }
    }
    _internalZoom = position.zoom;
    if (ignoreRegionChanged) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
    {
        [self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"region":[self.proxy valueForKey:@"region"],
                                                            @"zoom":@(_internalZoom)
                                                            } propagate:NO checkForListener:NO];
    }
}

/**
 * Called when the map becomes idle, after any outstanding gestures or
 * animations have completed (or after the camera has been explicitly set).
 */
- (void)mapView:(GMSMapView *)mapView idleAtCameraPosition:(GMSCameraPosition *)position {
    if (ignoreRegionChanged) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
    {
        [self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"region":[self.proxy valueForKey:@"region"],
                                                            @"zoom":@(_internalZoom)
                                                            } propagate:NO checkForListener:NO];
    }
}


/**
 * Called after a tap gesture at a particular coordinate, but only if a marker
 * was not tapped.  This is called before deselecting any currently selected
 * marker (the implicit action for tapping on the map).
 */
- (void)mapView:(GMSMapView *)mapView didTapAtCoordinate:(CLLocationCoordinate2D)coordinate {
    [_calloutView dismissCalloutAnimated:YES];
    [self fireEventOnMap:@"click" atCoordinate:coordinate];
    [self fireEventOnMap:@"singletap" atCoordinate:coordinate];
}

/**
 * Called after a long-press gesture at a particular coordinate.
 *
 * @param mapView The map view that was pressed.
 * @param coordinate The location that was pressed.
 */
- (void)mapView:(GMSMapView *)mapView didLongPressAtCoordinate:(CLLocationCoordinate2D)coordinate {
    [self fireEventOnMap:@"longpress" atCoordinate:coordinate];
}


/**
 * Called after a marker has been tapped.
 *
 * @param mapView The map view that was pressed.
 * @param marker The marker that was pressed.
 * @return YES if this delegate handled the tap event, which prevents the map
 *         from performing its default selection behavior, and NO if the map
 *         should continue with its default selection behavior.
 */
- (BOOL)mapView:(GMSMapView *)mapView didTapMarker:(GMSMarker *)marker {
    /* don't move map camera to center marker on tap */
    if (map.selectedMarker == marker) {
        map.selectedMarker = nil;
        [_calloutView dismissCalloutAnimated:YES];
        return;
    }
    mapView.selectedMarker = marker;
    if (calloutTouchedView) {
        calloutTouchedView = nil;
        return YES;
    }
    BOOL canShowCallout = YES;
    AkylasMapBaseAnnotationProxy *annProxy = marker.userData;
    if (annProxy) {
        canShowCallout = annProxy.showInfoWindow;
    }
    if (canShowCallout) {
        if (!_calloutView) {
            _calloutView = [[SMCalloutView platformCalloutView] retain];
            _calloutView.delegate = self;
            if (PostVersion7) {
                _calloutView.tintColor = self.tintColor;
            }
        }
        // apply the MKAnnotationView's basic properties
        _calloutView.title = annProxy.title;
        _calloutView.subtitle = annProxy.subtitle;
        
        // Apply the desired calloutOffset (from the top-middle of the view)
        CGPoint calloutOffset = marker.infoWindowAnchor;
        CGSize size = marker.layer.frame.size;
        if (CGSizeEqualToSize(size, CGSizeZero)) {
            size = [annProxy getSize];
        }
        calloutOffset.x *= size.width;
        calloutOffset.y *= -size.height;
        
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
            _calloutView.constrainedInsets = [annProxy nGetCalloutPadding];
            backView.backgroundColor = [annProxy nGetCalloutBackgroundColor];
            backView.cornerRadius = [annProxy nGetCalloutBorderRadius];
            backView.arrowHeight = [annProxy nGetCalloutArrowHeight];
        }
        else {
            backView.backgroundColor = [UIColor whiteColor];
            backView.cornerRadius = DEFAULT_CALLOUT_CORNER_RADIUS;
            backView.arrowHeight = DEFAULT_CALLOUT_ARROW_HEIGHT;
            _calloutView.constrainedInsets = DEFAULT_CALLOUT_PADDING;
        }
        
        
        CGRect calloutRect = CGRectZero;
        CGPoint point = [map.projection pointForCoordinate:marker.position];
        calloutRect.origin = point;
//        __block NSArray* subviews  = [map subviews];
//        UIView* vectorMap  = [subviews objectAtIndex:0];
        // This does all the magic.
//        [_calloutView presentCalloutFromRect:calloutRect inLayer:marker.layer constrainedToLayer:map.layer animated:YES];
        [_calloutView presentCalloutFromRect:calloutRect inView:map constrainedToView:map animated:YES];

    }
    [self fireEvent:@"click" onOverlay:marker source:@"pin"];
    return YES;
}

/**
 * Called after a marker's info window has been tapped.
 */
- (void)mapView:(GMSMapView *)mapView didTapInfoWindowOfMarker:(GMSMarker *)marker {
    if ([marker.userData isKindOfClass:[AkylasMapBaseAnnotationProxy class]])
    {
        AkylasMapBaseAnnotationProxy* proxy = marker.userData;
//        NSString * clickSource = @"unknown";
//        if (marker.leftCalloutAccessoryView == control)
//        {
//            clickSource = @"leftButton";
//        }
//        else if (marker.rightCalloutAccessoryView == control)
//        {
//            clickSource = @"rightButton";
//        }
//        [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
    }
}

/**
 * Called after an overlay has been tapped.
 * This method is not called for taps on markers.
 *
 * @param mapView The map view that was pressed.
 * @param overlay The overlay that was pressed.
 */
- (void)mapView:(GMSMapView *)mapView didTapOverlay:(GMSOverlay *)overlay {
    
}

/**
 * Called when a marker is about to become selected, and provides an optional
 * custom info window to use for that marker if this method returns a UIView.
 * If you change this view after this method is called, those changes will not
 * necessarily be reflected in the rendered version.
 *
 * The returned UIView must not have bounds greater than 500 points on either
 * dimension.  As there is only one info window shown at any time, the returned
 * view may be reused between other info windows.
 *
 * Removing the marker from the map or changing the map's selected marker during
 * this call results in undefined behavior.
 *
 * @return The custom info window for the specified marker, or nil for default
 */
- (UIView *)mapView:(GMSMapView *)mapView markerInfoWindow:(GMSMarker *)marker {

    return nil;
}

/**
 * Called when mapView:markerInfoWindow: returns nil. If this method returns a
 * view, it will be placed within the default info window frame. If this method
 * returns nil, then the default rendering will be used instead.
 *
 * @param mapView The map view that was pressed.
 * @param marker The marker that was pressed.
 * @return The custom view to disaply as contents in the info window, or null to
 * use the default content rendering instead
 */

- (UIView *)mapView:(GMSMapView *)mapView markerInfoContents:(GMSMarker *)marker {
    return nil;
}

-(void)didDragMarker:(GMSMarker *)marker withState:(AkMapDragState)newState {
    AkylasMapBaseAnnotationProxy* proxy = marker.userData;
    
    if (proxy == nil)
        return;
    
    
    if (newState == AkMapDragStateEnding) {
        CLLocationCoordinate2D coord = marker.position;
        [proxy replaceValue:@(coord.latitude) forKey:@"latitude" notification:YES];
        [proxy replaceValue:@(coord.longitude) forKey:@"longitude" notification:YES];
    }
    
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
                            NUMINT(_dragState),@"oldState",
                            nil];
    _dragState = newState;
    if (parentWants)
        [ourProxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
    
    if (viewWants)
        [proxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
}

/**
 * Called when dragging has been initiated on a marker.
 */
- (void)mapView:(GMSMapView *)mapView didBeginDraggingMarker:(GMSMarker *)marker {
    [self didDragMarker:marker withState:AkMapDragStateStarting];
}

/**
 * Called after dragging of a marker ended.
 */
- (void)mapView:(GMSMapView *)mapView didEndDraggingMarker:(GMSMarker *)marker {
    [self didDragMarker:marker withState:AkMapDragStateEnding];
}

/**
 * Called while a marker is dragged.
 */
- (void)mapView:(GMSMapView *)mapView didDragMarker:(GMSMarker *)marker {
    [self didDragMarker:marker withState:AkMapDragStateDragging];
}

/**
 * Called when the My Location button is tapped.
 *
 * @return YES if the listener has consumed the event (i.e., the default behavior should not occur),
 *         NO otherwise (i.e., the default behavior should occur). The default behavior is for the
 *         camera to move such that it is centered on the user location.
 */
- (BOOL)didTapMyLocationButtonForMapView:(GMSMapView *)mapView {
    
}

- (void)calloutViewClicked:(SMCalloutView *)calloutView {
    
}

// Called before the callout view appears on screen, or before the appearance animation will start.
- (void)calloutViewWillAppear:(SMCalloutView*)calloutView {
    
}

// Called after the callout view appears on screen, or after the appearance animation is complete.
- (void)calloutViewDidAppear:(SMCalloutView *)calloutView {
    
}

// Called before the callout view is removed from the screen, or before the disappearance animation is complete.
- (void)calloutViewWillDisappear:(SMCalloutView*)calloutView {
    
}

// Called after the callout view is removed from the screen, or after the disappearance animation is complete.
- (void)calloutViewDidDisappear:(SMCalloutView *)calloutView {
    if (_calloutUseTemplates) {
        [self reuseIfNecessary:_calloutView.leftAccessoryView];
        [self reuseIfNecessary:_calloutView.rightAccessoryView];
        [self reuseIfNecessary:_calloutView.contentView];
        _calloutView.leftAccessoryView = nil;
        _calloutView.rightAccessoryView = nil;
        _calloutView.contentView = nil;
    }
}

- (NSTimeInterval)calloutView:(SMCalloutView *)calloutView delayForRepositionWithSize:(CGSize)offset {
    CGPoint center = map.center;
    
    // move it by the requested offset
    center.x -= offset.width;
    center.y -= offset.height;
    
    // and translate it back into map coordinates
    CLLocationCoordinate2D coordinate = [self.map.projection coordinateForPoint:center];
    
    // move the map!
    [self setCenterCoordinate:coordinate animated:YES];
    
    // tell the callout to wait for a while while we scroll (we assume the scroll delay for MKMapView matches UIScrollView)
    return kSMCalloutViewRepositionDelayForUIScrollView;
}
@end
