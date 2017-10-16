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
#import "AkylasGooglemapClusterProxy.h"
#import "AkylasGooglemapModule.h"
#import "TiApp.h"
#import "GStaticCluster.h"


@implementation AkylasGMSMapView
-(void)dealloc
{
    RELEASE_TO_NIL(_tileCache)
    [super dealloc];
}

- (id)init
{
    if ((self = [super init])) {
        _networkConnected = YES;
    }
    return self;
}

-(BOOL)networkConnected
{
    return _networkConnected;
}

//- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
//{
//    [super touchesBegan:touches withEvent:event];
//}
//
//- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
//{
//    [super touchesMoved:touches withEvent:event];
//}
//
//- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
//{
//    [super touchesEnded:touches withEvent:event];
//}
//
//- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
//{
//    [super touchesCancelled:touches withEvent:event];
//}
@end

@interface AkCalloutView : SMCalloutView
@property (nonatomic, readwrite, retain) GMSOverlay* overlay;

@end
@implementation AkCalloutView
-(void)dealloc {
    RELEASE_TO_NIL(_overlay)
    [super dealloc];
}
@end

@implementation AkylasGooglemapView
{
    AkylasGMSMapView *map;
    BOOL _forwarding;
    NSInteger _userTrackingMode;
    CGFloat _userLocationRequiredZoom;
//    BOOL _inUserAction;
    
    AkCalloutView* _calloutView;
    UIView* calloutTouchedView;
    UIView* _emptyCalloutView;
//    UIView* _gestureView;
    AkMapDragState _dragState;
    
    BOOL _firstLayout;
    BOOL _dragging;
    
    GClusterManager* _clusterManager;
    GMSOverlay* _selectedOverlay;
    
    BOOL needsRegionZoomFix;
    CLLocationCoordinate2D _touchCoords;
    
    BOOL _ignoreSelectChange;
    
    CGPoint panStartLocation;
}

- (id)init
{
    if ((self = [super init])) {
        _shouldFollowUserLocation = NO;
//        _inUserAction = NO;
        _firstLayout = YES;
        _forwarding = NO;
        _dragging = NO;
        _mpp = 0.0f;
        _ignoreSelectChange = NO;
        _touchCoords = kCLLocationCoordinate2DInvalid;
        _userTrackingMode = AkUserTrackingModeNone;
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
    RELEASE_TO_NIL(_clusterManager)
    RELEASE_TO_NIL(_selectedOverlay)
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

-(AkylasGMSMapView*)map
{
    if (map==nil)
    {
//        @try {
            map = [[AkylasGMSMapView alloc] initWithFrame:self.bounds];
            
//        }
//        @catch (NSException * e) {
//            NSLog(@"Exception: %@", e);
//        }
        [map addObserver:self forKeyPath:@"selectedMarker" options:(NSKeyValueObservingOptionNew |
                                                                    NSKeyValueObservingOptionOld) context:nil];
        [map setTileCache:[[[TiCache alloc] initWithConfig:@[@{@"type":@"db-cache", @"name":@"AkGMSCache", @"capacity":@(10000), @"useCachesDirectory":@(YES)}] expiryPeriod:0] autorelease]];
        [self addSubview:map];
//        NSArray* subs = [map subviews];
//        _gestureView = [[map subviews] objectAtIndex:0];
        map.delegate = self;
        map.networkConnected = [[TiApp app] networkConnected];
//        map.settings.allowScrollGesturesDuringRotateOrZoom = YES;
        //Initialize loaded state to YES. This will automatically go to NO if the map needs to download new data
        loaded = YES;
        [[map settings] setConsumesGesturesInView:NO];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(networkChanged:) name:kTiNetworkChangedNotification object:nil];
        
        UITapGestureRecognizer *tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        tapGestureRecognizer.delegate = self;
        [map addGestureRecognizer:tapGestureRecognizer];
        [tapGestureRecognizer release];
        
        UIPanGestureRecognizer *panGestureRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(didRecognizePan:)];
        panGestureRecognizer.delegate = self;
        [map addGestureRecognizer:panGestureRecognizer];
        [panGestureRecognizer release];
    }
    return map;
}

- (float) distanceBetween : (CGPoint) p1 and: (CGPoint)p2
{
    return sqrt(pow(p2.x-p1.x,2)+pow(p2.y-p1.y,2));
}
- (void)didRecognizePan:(UIPanGestureRecognizer*)sender {
    if (sender.state == UIGestureRecognizerStateBegan) {
        panStartLocation = [sender locationInView:self];
    } else if (sender.state == UIGestureRecognizerStateChanged) {
        CGPoint location = [sender locationInView:self];
        if ([self distanceBetween:panStartLocation and:location] > 20) {
            [self setUserTrackingMode_:@(0)];
            sender.enabled = NO;
            sender.enabled = YES;
        }
    }
}

-(GClusterManager*)clusterManager {
    if (_clusterManager==nil)
    {
        [self map];
        _clusterManager = [[GClusterManager managerWithMapView:map renderer:[[[AkylasGooglemapClusterRenderer alloc] initWithMapView:map] autorelease]] retain];
        _clusterManager.delegate = self;
    }
    return _clusterManager;
}

-(AkylasGooglemapClusterRenderer*) clusterRenderer{
    return [_clusterManager clusterRenderer];
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
//    BOOL test = [hitView isUserInteractionEnabled];
    
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
//    BOOL animating = [self animating];
    
    
    //if we are animating it means we want to keep the zoom for sure...
    [[self map] setFrame:bounds];
    [super frameSizeChanged:frame bounds:bounds];
    if (_firstLayout) {
        _firstLayout = NO;
        id prop = [[self proxy] valueForUndefinedKey:@"region"];
        if (prop) {
            [self setRegion_:prop];
        } else {
            prop = [[self proxy] valueForUndefinedKey:@"centerCoordinate"];
            if (prop) {
                [self setCenterCoordinate_:prop];
            }
        }
    }
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
    needsRegionZoomFix = NO;
    [CATransaction begin];
    if (!animate) {
        [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
    }
    [super configurationStart];
}

-(void)configurationSet
{
    ignoreRegionChanged = NO;
    if (needsRegionZoomFix) {
        needsRegionZoomFix = NO;
        if ([self.proxy valueForUndefinedKey:@"region"]) {
            [self setRegion_:[self.proxy valueForUndefinedKey:@"region"]];
        } else if ([self.proxy valueForUndefinedKey:@"zoom"]) {
            [self setZoom_:[self.proxy valueForUndefinedKey:@"zoom"]];
        }
    }
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
    __block CGFloat px = 1.0f;
    TiThreadPerformBlockOnMainThread(^{
        px = [[self map].projection pointsForMeters:1 atCoordinate:[self map].camera.target];
    }, YES);

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
	if (value==nil)
	{
        return;
	}
    ENSURE_UI_THREAD_1_ARG(value)
    
    
    AkRegion region = [AkylasGooglemapModule regionFromObject:value];
    if (!AkRegionIsValid(region)) {
        return;
    }
    needsRegionZoomFix = NO;
    if (self.regionFits) {
        GMSCameraUpdate* cameraUpdate = [GMSCameraUpdate fitBounds:boundsFromRegion(region)];
        //    GMSCameraPosition* position = [[self map] cameraForBounds:boundsFromRegion(region) insets:UIEdgeInsetsZero];
        if (animate || !configurationSet) {
            [[self map] animateWithCameraUpdate:cameraUpdate];
        } else {
            [[self map] moveCamera:cameraUpdate];
        }
    } else {
        GMSCameraPosition* position = [[self map] cameraForBounds:boundsFromRegion(region) insets:UIEdgeInsetsZero];
        if (animate || !configurationSet) {
            [[self map] animateToCameraPosition:position];
        } else {
            [self map].camera = position;
        }
    }
    
}


-(void)setConstraintRegionFit_:(id)value
{
//	[self map].constraintRegionFit = [TiUtils boolValue:value];
}

-(void)setUserLocationEnabled:(BOOL)userLocationEnabled
{
    [super setUserLocationEnabled:userLocationEnabled];
    TiThreadPerformBlockOnMainThread(^{
        [self map].myLocationEnabled = self.userLocationEnabled;
    }, NO);
}


-(BOOL)userLocationEnabled
{
    return @([self map].myLocationEnabled);
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
        [self setShouldFollowUserLocation:_userTrackingMode != AkUserTrackingModeNone];
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
    NSUInteger type = kAkMapTypeNormal;
    if (IS_OF_CLASS(value, NSString)) {
        id obj = [[AkylasGooglemapModule MapType] objectForKey:value];
        if (obj) {
            type = [obj integerValue];
        }
    } else {
        type = [TiUtils intValue:value def:type];
    }
    switch (type) {
        case kAkMapTypeHybrid:
            type = kGMSTypeHybrid;
            break;
        case kAkMapTypeSatellite:
            type = kGMSTypeSatellite;
            break;
        case kAkMapTypeNone:
            type = kGMSTypeNone;
            break;
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
    needsRegionZoomFix = NO;
    //    if (!configurationSet) {
    //        _needsCameraUpdate = YES;
    //        return;
    //    }
    if ([self shouldAnimate]) {
        [[self map] animateToZoom:newValue];
    } else {
        [[self map] moveCamera:[GMSCameraUpdate zoomTo:newValue]];
    }
}

-(void)setBearing_:(id)value
{
    CGFloat newValue = [TiUtils floatValue:value def:0];
    ENSURE_UI_THREAD_1_ARG(value)
    if ([self shouldAnimate]) {
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
    if ([self shouldAnimate]) {
        [[self map] animateToViewingAngle:newValue];
    } else {
        GMSCameraPosition* pos = [self map].camera;
        [[self map] setCamera:[GMSCameraPosition cameraWithLatitude:pos.target.latitude longitude:pos.target.longitude zoom:pos.zoom bearing:pos.bearing viewingAngle:newValue]];
    }
}

-(void)setCenterCoordinate_:(id)value
{
    ENSURE_UI_THREAD_1_ARG(value)
//    [self setShouldFollowUserLocation:!configurationSet];
    CLLocationCoordinate2D coord = [AkylasGooglemapModule locationFromObject:value];
    [self setCenterCoordinate:coord animated:[self shouldAnimate]];
}

-(id)centerCoordinate_
{
    return [AkylasGooglemapModule dictFromLocation2D:[self centerCoordinate]];
}

-(void)setMinZoom_:(id)zoom
{
    CGFloat min = [TiUtils floatValue:zoom def:0];
    CGFloat max = [self map].maxZoom;
    CGFloat oldValue = [self map].minZoom;
    [[self map] setMinZoom:min maxZoom:max];
    if (!configurationSet && min < oldValue) {
        needsRegionZoomFix = YES;
    }
}

-(void)setMaxZoom_:(id)zoom
{
    CGFloat min = [self map].minZoom;
    CGFloat max = [TiUtils floatValue:zoom def:32];
    CGFloat oldValue = [self map].maxZoom;
    [[self map] setMinZoom:min maxZoom:max];
    if (!configurationSet && max > oldValue) {
        needsRegionZoomFix = YES;
    }
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
    [self updateCalloutPosition];
}

-(void)setConsumesGesturesInView_:(id)value
{
    [self map].settings.consumesGesturesInView = [TiUtils boolValue:value];
}

-(void)setIndoorPicker_:(id)value
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

-(void)setPreferredFrameRate_:(id)value
{
    [self map].preferredFrameRate = (GMSFrameRate)[TiUtils intValue:value];
}

-(void)setMapStyle_:(id)value
{
    NSError* error = nil;
    if (IS_OF_CLASS(value, NSDictionary)) {
        [self map].mapStyle = [GMSMapStyle styleWithJSONString:[TiUtils jsonStringify:value] error:&error];
    } else if (IS_OF_CLASS(value, NSString)) {
        [self map].mapStyle = [GMSMapStyle styleWithJSONString:[TiUtils stringValue:value] error:&error];
    }
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

-(void)showInfoWindow
{
    GMSOverlay* overlay = map.selectedMarker;
    if (!overlay) {
        overlay = _selectedOverlay;
    }
    if (overlay) {
        [self showCalloutForOverlay:overlay];
    }
}

-(void)hideInfoWindow
{
    GMSOverlay* overlay = map.selectedMarker;
    if (!overlay) {
        overlay = _selectedOverlay;
    }
    if (overlay) {
        [self hideCalloutForOverlay:overlay];
    }
}

-(void)selectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
    ENSURE_UI_THREAD(selectAnnotation,args);
    GMSOverlay* overlay = nil;
    AkylasGMSMapView* mapView = [self map];
    if (IS_OF_CLASS(args, AkylasMapBaseAnnotationProxy)) {
        overlay = [args getGOverlayForMapView:mapView];
    }
    if (!overlay) {
        if (_selectedOverlay) {
            [self onDeselected:_selectedOverlay];
            [self fireSelectedEventFromOverlay:_selectedOverlay toOverlay:nil];
            RELEASE_TO_NIL(_selectedOverlay)
        } else {
            map.selectedMarker = nil;
        }
        
        return;
    }
    if (overlay == map.selectedMarker || overlay == _selectedOverlay) {
        return;
    }

    AkylasMapBaseAnnotationProxy *annotProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;
    if (annotProxy && !annotProxy.selectable) {
        return;
    }
    if (IS_OF_CLASS(overlay, GMSMarker)) {
        map.selectedMarker = (GMSMarker*)overlay;
    } else {
        [self selectOverlay:overlay];
    }

}

-(void)deselectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG(args,NSObject);
    ENSURE_UI_THREAD(deselectAnnotation,args);
    GMSOverlay* overlay = nil;
    AkylasGMSMapView* mapView = [self map];
    if (IS_OF_CLASS(args, AkylasMapBaseAnnotationProxy)) {
        overlay = [args getGOverlayForMapView:mapView];
    }
    if (overlay && overlay == map.selectedMarker) {
        map.selectedMarker = nil;
    } else if (_selectedOverlay && overlay == _selectedOverlay) {
        [self onDeselected:_selectedOverlay];
        [self fireSelectedEventFromOverlay:_selectedOverlay toOverlay:nil];
        RELEASE_TO_NIL(_selectedOverlay)
    }
}

-(id)selectedAnnotation_
{
    if ([map selectedMarker]) {
        return [map selectedMarker].userData;
    } else if (_selectedOverlay && [_selectedOverlay respondsToSelector:@selector(userData)]) {
        return [(id)_selectedOverlay userData];

    }
    return nil;
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
    if (_shouldFollowUserLocation) {
        [self selectUserAnnotation];
    }
}

- (void)setCenterCoordinate:(CLLocationCoordinate2D)centerCoordinate animated:(BOOL)animated
{
//    [self setShouldFollowUserLocation:NO];
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
    __block GMSCoordinateBounds* bounds;
    TiThreadPerformBlockOnMainThread(^{
        bounds = [[GMSCoordinateBounds alloc] initWithRegion: [self map].projection.visibleRegion];
    }, YES);
    return ((AkRegion){.northEast = bounds.northEast, .southWest = bounds.southWest});
}

-(BOOL)shouldAnimate
{
    return ([self viewInitialized] &&(animate || !configurationSet));
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
    TiThreadPerformBlockOnMainThread(^{
        __block NSInteger realIndex = index;
    //    GMSMapView* mapView = [self map];
        if (IS_OF_CLASS(annotations, NSArray)) {
            [annotations enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                [self internalAddAnnotations:obj atIndex:(realIndex >= 0)?realIndex++:realIndex];
            }];
        }
        else {
            AkylasGMSMapView* mapView = [self map];
            GMSOverlay* overlay = [annotations getGOverlayForMapView:mapView];
            if (overlay) {
                overlay.zIndex = (int)((AkylasMapBaseAnnotationProxy*)annotations).zIndex;
                [overlay setMap:mapView];
            }
            
    //        if (realIndex >= 0) {
    //            overlay.zIndex = (overlay.zIndex > 0) ? overlay.zIndex : (int)realIndex;
    //        }
        }
    }, NO);
}

-(void)internalRemoveAnnotations:(id)annotations
{
    TiThreadPerformBlockOnMainThread(^{
        SEL selector = @selector(removeFromMap);
        if ([annotations isKindOfClass:[NSArray class]]) {
            for (AkylasGooglemapAnnotationProxy* annotProxy in annotations) {
                GMSOverlay* overlay = [annotProxy gOverlay];
                if (overlay) {
                    [overlay setMap:nil];
                    if (overlay == _selectedOverlay) {
                        [self onDeselected:_selectedOverlay];
                        [self fireSelectedEventFromOverlay:_selectedOverlay toOverlay:nil];
                        RELEASE_TO_NIL(_selectedOverlay)
                    } else if (overlay == map.selectedMarker) {
                        map.selectedMarker = nil;
                    }
                }
                
            }
        }
        else if (IS_OF_CLASS(annotations, AkylasMapBaseAnnotationProxy)) {
            GMSOverlay* overlay = [annotations gOverlay];
            if (overlay) {
                [overlay setMap:nil];
                if (overlay == _selectedOverlay) {
                    [self onDeselected:_selectedOverlay];
                    [self fireSelectedEventFromOverlay:_selectedOverlay toOverlay:nil];
                    RELEASE_TO_NIL(_selectedOverlay)
                } else if (overlay == map.selectedMarker) {
                    map.selectedMarker = nil;
                }
            }
        }
    }, NO);
}

-(void)internalAddRoutes:(id)routes atIndex:(NSInteger)index
{
    [self internalAddAnnotations:routes atIndex:index];
}

-(void)internalRemoveRoutes:(id)routes
{
    [self internalRemoveAnnotations:routes];
}


-(void)internalAddGroundOverlays:(id)grounds atIndex:(NSInteger)index
{
    [self internalAddAnnotations:grounds atIndex:index];
}

-(void)internalRemoveGroundOverlays:(id)grounds
{
    [self internalRemoveAnnotations:grounds];
}



-(void)showCalloutForOverlay:(GMSOverlay*)overlay
{
    BOOL canShowCallout = self.canShowInfoWindow;
    AkylasMapBaseAnnotationProxy *annProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;

    if (annProxy) {
        canShowCallout &= annProxy.showInfoWindow;
    }
    if (canShowCallout) {
        if (!_calloutView) {
            _calloutView = [AkCalloutView new];
            _calloutView.delegate = self;
            if (PostVersion7) {
                _calloutView.tintColor = self.tintColor;
            }
        }
        BOOL changing = YES;
        if (_calloutView.overlay == overlay) {
            changing = NO;
//            return;
        }
        
        
        // Apply the desired calloutOffset (from the top-middle of the view)
        CGPoint anchorOffset = [annProxy nGetAnchorPoint];
        CGPoint calloutOffset = [annProxy nGetCalloutAnchorPoint];
        CLLocationCoordinate2D coords = [annProxy coordinate];
        CGSize size = CGSizeZero;
        if (IS_OF_CLASS(overlay, GMSMarker)) {
            size = ((GMSMarker*)overlay).layer.frame.size;
        }
        if (CGSizeEqualToSize(size, CGSizeZero)) {
            size = [annProxy getSize];
        }
        calloutOffset.x = (calloutOffset.x - anchorOffset.x) * size.width;
        calloutOffset.y = (calloutOffset.y - anchorOffset.y) * size.height;
        
        _calloutView.overlay = overlay;
        // apply the MKAnnotationView's basic properties
        _calloutView.title = annProxy.title;
        _calloutView.subtitle = annProxy.subtitle;
        
        _calloutView.calloutOffset = calloutOffset;
        
        SMCalloutMaskedBackgroundView* backView = (SMCalloutMaskedBackgroundView*)_calloutView.backgroundView;
        backView.alpha = [annProxy nGetCalloutAlpha];
        
        if (changing) {
            if (self.calloutUseTemplates) {
                _calloutView.leftAccessoryView = [self reusableViewForProxy:annProxy objectKey:@"leftView"];
                _calloutView.rightAccessoryView = [self reusableViewForProxy:annProxy objectKey:@"rightView"];
                _calloutView.contentView = [self reusableViewForProxy:annProxy objectKey:@"customView"];
            }
            else {
                _calloutView.leftAccessoryView = [annProxy nGetLeftViewAccessory];
                _calloutView.rightAccessoryView = [annProxy nGetRightViewAccessory];
                _calloutView.contentView = [annProxy nGetCustomViewAccessory];
            }
            
        }
        
        if (annProxy) {
            _calloutView.contentViewInset = [annProxy nGetCalloutPadding];
            backView.backgroundColor = [annProxy nGetCalloutBackgroundColor];
            backView.cornerRadius = [annProxy nGetCalloutBorderRadius];
            backView.arrowHeight = [annProxy nGetCalloutArrowHeight];
        }
        else {
            backView.backgroundColor = [UIColor whiteColor];
            backView.cornerRadius = DEFAULT_CALLOUT_CORNER_RADIUS;
            backView.arrowHeight = DEFAULT_CALLOUT_ARROW_HEIGHT;
            _calloutView.contentViewInset = DEFAULT_CALLOUT_PADDING;
        }
        
        CGRect calloutRect = CGRectZero;
        CGPoint point = [map.projection pointForCoordinate:coords];
        calloutRect.origin = point;
        
        _calloutView.constrainedInsets = map.padding;
        //        __block NSArray* subviews  = [map subviews];
        //        UIView* vectorMap  = [subviews objectAtIndex:0];
        // This does all the magic.
        //        [_calloutView presentCalloutFromRect:calloutRect inLayer:marker.layer constrainedToLayer:map.layer animated:YES];
        [_calloutView presentCalloutFromRect:calloutRect inView:map constrainedToView:map animated:changing];
        
    }
}
-(void)hideCalloutForOverlay:(GMSOverlay*)overlay
{
    if (_calloutView && _calloutView.overlay == overlay) {
        [self dismissCalloutView];
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if([keyPath isEqualToString:@"myLocation"]) {
        CLLocation *location = [object myLocation];
        CLLocationCoordinate2D target =
        CLLocationCoordinate2DMake(location.coordinate.latitude, location.coordinate.longitude);
        
        if (_shouldFollowUserLocation && _userTrackingMode != AkUserTrackingModeNone) {
            GMSMutableCameraPosition* position = [[GMSMutableCameraPosition alloc] init];
//            GMSMutablePosition* currentposition = [self map].camera;
            
            position.target = location.coordinate;
            if (_userTrackingMode == AkUserTrackingModeFollowWithHeading) {
//                CGFloat bearing  = location.course;
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
            
            if (animate && configurationSet) {
                [[self map] animateToCameraPosition:position];
            } else {
                [[self map] setCamera:position];
            }
            [position release];
        }
        if ([self.viewProxy _hasListeners:@"location" checkParent:NO])
        {
            [self.proxy fireEvent:@"location" withObject:[AkylasGooglemapModule dictFromLocation:location] propagate:NO checkForListener:NO];
        }
    } else if([keyPath isEqualToString:@"selectedMarker"]) {

        GMSOverlay *oldMarker = [change objectForKey:NSKeyValueChangeOldKey];
        GMSOverlay *newMarker = [change objectForKey:NSKeyValueChangeNewKey];
        if (newMarker == oldMarker) {
            return;
        }
        GMSOverlay* oldOverlay = nil;
        if (IS_OF_CLASS(oldMarker, GMSOverlay)) {
            oldOverlay = [oldMarker retain];
            [self onDeselected:oldMarker];

        }
        if (_selectedOverlay) {
            RELEASE_TO_NIL(oldOverlay)
            oldOverlay = [_selectedOverlay retain];
            [self onDeselected:_selectedOverlay];
            RELEASE_TO_NIL(_selectedOverlay)
        }
        if (IS_OF_CLASS(newMarker, GMSMarker)) {
            [self onSelected:newMarker];
        }
        
        [self fireSelectedEventFromOverlay:oldOverlay toOverlay:newMarker];
        RELEASE_TO_NIL(oldOverlay)
    }
}

- (BOOL)internalAddTileSources:(id)tileSource atIndex:(NSInteger)index
{
    TiThreadPerformBlockOnMainThread(^{
        __block NSInteger realIndex = index;
        if (IS_OF_CLASS(tileSource, NSArray)) {
            [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                [self internalAddTileSources:obj atIndex:(realIndex >= 0)?realIndex++:realIndex];
            }];
        } else {
            GMSMapView* mapView = [self map];
            GMSTileLayer* layer = [(AkylasGooglemapTileSourceProxy*)tileSource getGTileLayerForMapView:mapView];
            layer.zIndex = (int)((AkylasMapBaseTileSourceProxy*)tileSource).zIndex;

        }
    }, NO);
}

- (BOOL)internalRemoveTileSources:(id)tileSource
{
    TiThreadPerformBlockOnMainThread(^{
        if (IS_OF_CLASS(tileSource, NSArray)) {
            [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                [self internalRemoveTileSources:obj];
            }];
        } else {
            [[(AkylasGooglemapTileSourceProxy*)tileSource gTileLayer] setMap:nil];
        }
    }, NO);
}

- (BOOL)internalAddClusters:(id)cluster atIndex:(NSInteger)index
{
    TiThreadPerformBlockOnMainThread(^{
        __block NSInteger realIndex = index;
        if (IS_OF_CLASS(cluster, NSArray)) {
            [cluster enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                [self internalAddClusters:obj atIndex:(realIndex >= 0)?realIndex++:realIndex];
            }];
        } else {
            AkylasGooglemapClusterProxy* clusterProxy = (AkylasGooglemapClusterProxy*)cluster;
            [[self clusterManager] addClusterAlgorithm:[clusterProxy algorithm]];
    }
        [_clusterManager cluster];
    }, NO);
}

- (BOOL)internalRemoveClusters:(id)cluster
{
    TiThreadPerformBlockOnMainThread(^{
       if (IS_OF_CLASS(cluster, NSArray)) {
            [cluster enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                [self internalRemoveClusters:obj];
            }];
        } else if (cluster) {
            AkylasGooglemapClusterProxy* clusterProxy = (AkylasGooglemapClusterProxy*)cluster;
            [((AkylasGooglemapClusterRenderer*)[_clusterManager clusterRenderer]) clearCacheForId:[clusterProxy uniqueId]];
            [[clusterProxy algorithm] removeItemsFromMap:map];
            [_clusterManager removeClusterAlgorithm:[clusterProxy algorithm]];
        }
    }, NO);
}


#pragma mark Event generation

-(BOOL)fireSelectedEventFromOverlay:(GMSOverlay*)oldOverlay toOverlay:(GMSOverlay*)newOverlay
{
    if (_ignoreSelectChange) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"selected" checkParent:NO])
    {
        CLLocationCoordinate2D coords;
        if (IS_OF_CLASS(newOverlay, GMSMarker)) {
            coords = ((GMSMarker*)newOverlay).position;
        } else if (IS_OF_CLASS(oldOverlay, GMSMarker)) {
            coords = ((GMSMarker*)oldOverlay).position;
        }
        AkylasMapBaseAnnotationProxy *annotProxy = ([newOverlay respondsToSelector:@selector(userData)])? [(id)newOverlay userData]: nil;

        
        NSMutableDictionary *event = [TiUtils dictionaryFromPoint:[[map projection] pointForCoordinate:coords] inView:map];
        [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:coords]];
        if (IS_OF_CLASS(newOverlay, AkylasClusterMarker)) {
            GMSCoordinateBounds* bounds = [(AkylasClusterMarker*)newOverlay cluster].bounds;
            [event setObject:annotProxy forKey:@"cluster"];
            NSSet* set = [[(AkylasClusterMarker*)newOverlay cluster] items];
            NSMutableArray* array = [NSMutableArray arrayWithCapacity:[set count]];
            [set enumerateObjectsUsingBlock:^(GQuadItem* obj, BOOL *stop) {
                [array addObject:obj.item];
            }];
            [event setObject:array forKey:@"annotations"];
            
            [event setObject:[AkylasGooglemapModule dictFromRegion:((AkRegion){.northEast = bounds.northEast, .southWest = bounds.southWest})] forKey:@"region"];
        } else if (annotProxy){
            if (IS_OF_CLASS(annotProxy, AkylasMapBaseRouteProxy)) {
                [event setObject:annotProxy forKey:@"route"];
            } else {
                [event setObject:annotProxy forKey:@"annotation"];
                [event setObject:NUMINTEGER([annotProxy tag]) forKey:@"index"];
            }
        }
        [self.proxy fireEvent:@"selected" withObject:event propagate:NO checkForListener:NO];
    }
}

- (BOOL)fireEvent:(NSString*)type onOverlay:(GMSOverlay *) overlay source:(NSString *)source
{
    if (ignoreClicks)
    {
        return;
    }
    
    AkylasMapBaseAnnotationProxy *annotProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;
    if (annotProxy == nil)
    {
        return NO;
    }
    
    TiViewProxy * ourProxy = [self viewProxy];
    BOOL propagate = [type isEqualToString:@"click"];
    BOOL viewWants = [annotProxy _hasListeners:type checkParent:propagate];
    if(!viewWants)
    {
        return NO;
    }
    
    
    NSMutableDictionary *event;
    if (IS_OF_CLASS(annotProxy, AkylasMapBaseRouteProxy)) {
        event = [NSMutableDictionary dictionary];
        [event setObject:annotProxy forKey:@"route"];
        CGFloat tolerance = [((AkylasGooglemapRouteProxy*)annotProxy) lineWidth]*_mpp/2;
        NSDictionary* result = [((AkylasGooglemapRouteProxy*)annotProxy) pointOnPathFrom:_touchCoords tolerance:tolerance];
        if (result) {
            [event setObject:result forKey:@"onpath"];
        }
    } else {
        CGPoint point = [[map projection] pointForCoordinate:((GMSMarker*)overlay).position];
        event = [TiUtils dictionaryFromPoint:point inView:map];
        [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:[[map projection] coordinateForPoint:point]]];
        
        if (IS_OF_CLASS(overlay, AkylasClusterMarker)) {
            GMSCoordinateBounds* bounds = [(AkylasClusterMarker*)overlay cluster].bounds;
            [event setObject:annotProxy forKey:@"cluster"];
            NSSet* set = [[(AkylasClusterMarker*)overlay cluster] items];
            NSMutableArray* array = [NSMutableArray arrayWithCapacity:[set count]];
            [set enumerateObjectsUsingBlock:^(GQuadItem* obj, BOOL *stop) {
                [array addObject:obj.item];
            }];
            [event setObject:array forKey:@"annotations"];
            
            [event setObject:[AkylasGooglemapModule dictFromRegion:((AkRegion){.northEast = bounds.northEast, .southWest = bounds.southWest})] forKey:@"region"];
        } else {
            [event setObject:annotProxy forKey:@"annotation"];
            [event setObject:NUMINTEGER([annotProxy tag]) forKey:@"index"];
            if ([annotProxy title] != nil)
            {
                [event setObject:[annotProxy title] forKey:@"title"];
            }
        }
    }
    if (source) {
        [event setObject:source forKey:@"clicksource"];
    }
    [event setObject:ourProxy forKey:@"map"];
    [event setObject:@(_mpp) forKey:@"mpp"];
    [event setObject:@(_internalZoom) forKey:@"zoom"];

    [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:_touchCoords]];
    [annotProxy fireEvent:type withObject:event propagate:propagate checkForListener:NO];
    return YES;
}

- (void)fireEventOnMap:(NSString*)type atCoordinate:(CLLocationCoordinate2D)coordinate
{
    if (ignoreClicks)
    {
        return;
    }
    
    BOOL propagate = [type isEqualToString:@"click"];
    if ([self.viewProxy _hasListeners:type checkParent:propagate]) {
        NSMutableDictionary *event = [TiUtils dictionaryFromPoint:[[map projection] pointForCoordinate:coordinate] inView:map];
        [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:coordinate]];
        [event setObject:@(_mpp) forKey:@"mpp"];
        [event setObject:@(_internalZoom) forKey:@"zoom"];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:propagate];
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
//- (void)mapView:(GMSMapView *)mapView willMove:(BOOL)gesture {
////    NSString* type = @"willMove";
////    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
////        
////        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
////                               NUMBOOL(gesture),@"userAction",
////                               nil
////                               ];
////        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
////    }
////
//    if (configurationSet && gesture) {
//        _inUserAction = configurationSet && gesture;
//    }
//}

-(void)dismissCalloutView {
    [_calloutView dismissCalloutAnimated:YES];
    if (self.calloutUseTemplates) {
        [self reuseIfNecessary:_calloutView.leftAccessoryView];
        [self reuseIfNecessary:_calloutView.rightAccessoryView];
        [self reuseIfNecessary:_calloutView.contentView];
        _calloutView.leftAccessoryView = nil;
        _calloutView.rightAccessoryView = nil;
        _calloutView.contentView = nil;
    }
    _calloutView.overlay = nil;
//    _calloutView.delegate = nil;
//    RELEASE_TO_NIL(_calloutView)
}

-(void)updateCalloutPosition {
    if (_calloutView && ![_calloutView isDismissing]) {
        GMSOverlay* overlay = _calloutView.overlay;
        if (overlay != nil) {
            AkylasMapBaseAnnotationProxy *annProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;

            CGPoint anchorOffset = [annProxy nGetAnchorPoint];
            CGPoint calloutOffset = [annProxy nGetCalloutAnchorPoint];
            CLLocationCoordinate2D coords = [annProxy coordinate];
            CGSize size = CGSizeZero;
            if (IS_OF_CLASS(overlay, GMSMarker)) {
                size = ((GMSMarker*)overlay).layer.frame.size;
            }
            if (CGSizeEqualToSize(size, CGSizeZero)) {
                size = [annProxy getSize];
            }
            calloutOffset.x = (calloutOffset.x - anchorOffset.x) * size.width;
            calloutOffset.y = (calloutOffset.y - anchorOffset.y) * size.height;
            
            //        _calloutView.calloutOffset = calloutOffset;
            
            CGPoint arrowPt = _calloutView.backgroundView.arrowPoint;
            CGPoint pt = [map.projection pointForCoordinate:coords];
            
            pt.x -= arrowPt.x - calloutOffset.x;
            pt.y -= arrowPt.y - calloutOffset.y;
            
            
            _calloutView.frame = (CGRect) {.origin = pt, .size = _calloutView.frame.size };
        } else {
            [self dismissCalloutView];
        }
    }
}

-(void)markerDidUpdate:(GMSMarker*)marker
{
    if (map.selectedMarker == marker) {
        [self showCalloutForOverlay:marker];
    }
}

/**
 * Called when tiles have just been requested or labels have just started rendering.
 */
- (void)mapViewDidStartTileRendering:(GMSMapView *)mapView{
    [self.proxy fireEvent:@"startTileRendering"];

}

/**
 * Called when all tiles have been loaded (or failed permanently) and labels have been rendered.
 */
- (void)mapViewDidFinishTileRendering:(GMSMapView *)mapView {
    [self.proxy fireEvent:@"finishTileRendering"];

}

/**
 * Called when map is stable (tiles loaded, labels rendered, camera idle) and overlay objects have
 * been rendered.
 */
- (void)mapViewSnapshotReady:(GMSMapView *)mapView {
    [self.proxy fireEvent:@"ready"];
}

/**
 * Called repeatedly during any animations or gestures on the map (or once, if
 * the camera is explicitly set). This may not be called for all intermediate
 * camera positions. It is always called for the final position of an animation
 * or gesture.
 */
- (void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)position {
    if (_clusterManager) {
        [_clusterManager mapView:mapView didChangeCameraPosition:position];
    }
//    region = regionFromMKRegion([mapView region]);
    
    [self updateCalloutPosition];
   
    _internalZoom = position.zoom;
//    CLLocationCoordinate2D topLeft = mapView.projection.visibleRegion.farLeft;
//    CLLocationCoordinate2D bottomLeft = mapView.projection.visibleRegion.nearLeft;
//    CGFloat lat = fabs(topLeft.latitude - bottomLeft.latitude);
//    _mpp = cos(lat * M_PI / 180) * 2 * M_PI * 6378137 / (256 * pow(2, _internalZoom));
    _mpp = 156543.03392 * cos(position.target.latitude * M_PI / 180) / pow(2, _internalZoom);
    if (ignoreRegionChanged) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
    {
        
        CGFloat distance = _mpp * mapView.frame.size.width;
      
        [self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"mpp":@(_mpp),
                                                            @"mapdistance":@(distance),
                                                            @"region":[self.proxy valueForKey:@"region"],
                                                            @"zoom":@(_internalZoom),
                                                            @"bearing":@(position.bearing),
                                                            @"tilt":@(position.viewingAngle),
                                                            } propagate:NO checkForListener:NO];
    }
}

/**
 * Called when the map becomes idle, after any outstanding gestures or
 * animations have completed (or after the camera has been explicitly set).
 */
- (void)mapView:(GMSMapView *)mapView idleAtCameraPosition:(GMSCameraPosition *)position {
    if (_clusterManager) {
        [_clusterManager mapView:mapView idleAtCameraPosition:position];
    }
    if (ignoreRegionChanged) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
    {
        CGFloat distance = _mpp * mapView.frame.size.width;
        [self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"mpp":@(_mpp),
                                                            @"idle":@(YES),
//                                                            @"mapdistance":@(distance),
                                                            @"region":[self.proxy valueForKey:@"region"],
                                                            @"zoom":@(_internalZoom),
                                                            @"bearing":@(position.bearing),
                                                            @"tilt":@(position.viewingAngle),
                                                            } propagate:NO checkForListener:NO];
    }
}


/**
 * Called after a tap gesture at a particular coordinate, but only if a marker
 * was not tapped.  This is called before deselecting any currently selected
 * marker (the implicit action for tapping on the map).
 */
- (void)mapView:(GMSMapView *)mapView didTapAtCoordinate:(CLLocationCoordinate2D)coordinate {
    
    [self fireEventOnMap:@"click" atCoordinate:coordinate];
    [self fireEventOnMap:@"singletap" atCoordinate:coordinate];
    if (self.selectOnTap) {
        if (_selectedOverlay) {
            [self onDeselected:_selectedOverlay];
            [self fireSelectedEventFromOverlay:_selectedOverlay toOverlay:nil];
            RELEASE_TO_NIL(_selectedOverlay)
        } else if (map.selectedMarker) {
            map.selectedMarker = nil;
        }
    }

}

/**
 * Called after a long-press gesture at a particular coordinate.
 *
 * @param mapView The map view that was pressed.
 * @param coordinate The location that was pressed.
 */
- (void)mapView:(GMSMapView *)mapView didLongPressAtCoordinate:(CLLocationCoordinate2D)coordinate {
    if (!_dragging) {
        [self fireEventOnMap:@"longpress" atCoordinate:coordinate];
    }
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
    AkylasMapBaseAnnotationProxy *annotProxy = ([marker respondsToSelector:@selector(userData)])? [(id)marker userData]: nil;
    BOOL selectable = !annotProxy || annotProxy.selectable;
    if (selectable) {
        if (self.unselectOnTap && map.selectedMarker == marker) {
            map.selectedMarker = nil;
        } else if (self.selectOnTap && map.selectedMarker != marker) {
            mapView.selectedMarker = marker;
        }
    }
    
    
    
    if (calloutTouchedView) {
        calloutTouchedView = nil;
    } else {
        [self fireEvent:@"click" onOverlay:marker source:@"pin"];
    }
    
    return YES;
}

/**
 * Called after a marker's info window has been tapped.
 */
- (void)mapView:(GMSMapView *)mapView didTapInfoWindowOfMarker:(GMSMarker *)marker {
    if ([marker.userData isKindOfClass:[AkylasMapBaseAnnotationProxy class]])
    {
//        AkylasMapBaseAnnotationProxy* proxy = marker.userData;
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

- (void) mapView:(GMSMapView *) mapView didTapPOIWithPlaceID:(NSString *) placeID
            name:(NSString *) name
        location:(CLLocationCoordinate2D) location {
    if ([self.viewProxy _hasListeners:@"poi" checkParent:NO])
    {
        NSMutableDictionary *event = [TiUtils dictionaryFromPoint:[[map projection] pointForCoordinate:location] inView:map];
        [event addEntriesFromDictionary:[AkylasGooglemapModule dictFromLocation2D:location]];
        [event setObject:name forKey:@"name"];
        [event setObject:placeID forKey:@"placeId"];
        [self.proxy fireEvent:@"poi" withObject:event propagate:NO checkForListener:NO];
    }
}

-(void)onSelected:(GMSOverlay *)overlay
{
    AkylasMapBaseAnnotationProxy *annotProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;
    
    if (annotProxy) {
        if ([annotProxy respondsToSelector:@selector(onSelected:)]) {
            [(id)annotProxy onSelected:overlay];
        }
        [self fireEvent:@"focus" onOverlay:overlay source:@"pin"];
    }
}

-(void)onDeselected:(GMSOverlay *)overlay
{
    AkylasMapBaseAnnotationProxy *annotProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;
    if (annotProxy)
    {
        if ([annotProxy respondsToSelector:@selector(onDeselected:)]) {
            [(id)annotProxy onDeselected:overlay];
        }
        [self dismissCalloutView];
        [self fireEvent:@"blur" onOverlay:overlay source:@"pin"];
    }
}


-(void)setCanSelectRoute_:(id)value
{
    [super setCanSelectRoute_:value];
    if (!self.canSelectRoute && _selectedOverlay) {
        [self selectAnnotation:nil];
    }
}

-(void)selectOverlay:(GMSOverlay *)overlay {
    if (self.canSelectRoute) {
        GMSOverlay* oldOverlay = nil;
        if (map.selectedMarker) {
            oldOverlay = [map.selectedMarker retain];
            _ignoreSelectChange = YES;
            map.selectedMarker = nil;
            _ignoreSelectChange = NO;
        }
        if (_selectedOverlay && ((self.selectOnTap && overlay != _selectedOverlay) || self.unselectOnTap)) {
            RELEASE_TO_NIL(oldOverlay)
            oldOverlay = [_selectedOverlay retain];
            [self onDeselected:_selectedOverlay];
            RELEASE_TO_NIL(_selectedOverlay)
        }
        if (self.selectOnTap) {
            _selectedOverlay = [overlay retain];
            [self onSelected:_selectedOverlay];
        }
        
        [self fireSelectedEventFromOverlay:oldOverlay toOverlay:_selectedOverlay];
        RELEASE_TO_NIL(oldOverlay)
    } else {
        [self selectAnnotation:nil];
    }
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    CGPoint point = [gestureRecognizer locationInView:map];
    _touchCoords = [map.projection coordinateForPoint:point];
    return IS_OF_CLASS(gestureRecognizer, UIPanGestureRecognizer);
}
/**
 * Called after an overlay has been tapped.
 * This method is not called for taps on markers.
 *
 * @param mapView The map view that was pressed.
 * @param overlay The overlay that was pressed.
 */
- (void)mapView:(GMSMapView *)mapView didTapOverlay:(GMSOverlay *)overlay {
    AkylasMapBaseAnnotationProxy *annotProxy = ([overlay respondsToSelector:@selector(userData)])? [(id)overlay userData]: nil;
    BOOL selectable = !annotProxy || annotProxy.selectable;
    if (selectable) {
        [self selectOverlay:overlay];
    }
    [self fireEvent:@"click" onOverlay:overlay source:nil];
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

    return _emptyCalloutView;
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
    AkylasMapBaseAnnotationProxy* annProxy = marker.userData;
    
    if (annProxy == nil)
        return;
    
    
    if (newState == AkMapDragStateEnding) {
        CLLocationCoordinate2D coord = marker.position;
        [annProxy replaceValue:@(coord.latitude) forKey:@"latitude" notification:YES];
        [annProxy replaceValue:@(coord.longitude) forKey:@"longitude" notification:YES];
    }
    
    TiViewProxy * ourProxy = [self viewProxy];
    BOOL parentWants = [ourProxy _hasListeners:@"pinchangedragstate" checkParent:NO];
    BOOL viewWants = [annProxy _hasListeners:@"pinchangedragstate" checkParent:NO];
    
    if(!parentWants && !viewWants)
        return;
    
    id title = [annProxy title];
    if (title == nil)
        title = [NSNull null];
    
    NSNumber * indexNumber = NUMINT([annProxy tag]);
    
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
    _dragging = YES;
    [self didDragMarker:marker withState:AkMapDragStateStarting];
}

/**
 * Called after dragging of a marker ended.
 */
- (void)mapView:(GMSMapView *)mapView didEndDraggingMarker:(GMSMarker *)marker {
    _dragging = NO;
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
    if (self.calloutUseTemplates) {
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
