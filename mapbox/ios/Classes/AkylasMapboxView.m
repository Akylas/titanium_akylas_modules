//
//  AkylasMapMapboxView.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapboxView.h"
#import "AkylasMapboxAnnotationProxy.h"
#import "AkylasMapboxTileSourceProxy.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxModule.h"
#import "SMCalloutView.h"
#import "TiApp.h"


@implementation AkylasMapboxView
{
    MGLMapView *map;
    //    NSMutableArray *_tileSources;
//    MGLT* _tileSourceContainer;
    BOOL _userStackTileSource;
    BOOL _needsRegionUpdate;
    AkRegion region;
}

- (id)init
{
    if ((self = [super init])) {
        _userStackTileSource = NO;
        _needsRegionUpdate = NO;
    }
    return self;
}

-(void)dealloc
{
	if (map!=nil)
	{
        map.delegate = nil;
        RELEASE_TO_NIL(map);
        [[NSNotificationCenter defaultCenter] removeObserver:self name:kTiNetworkChangedNotification object:nil];
	}
	[super dealloc];
}

-(MGLMapView*)map
{
    if (map==nil)
    {
        map = [[RMMapView alloc] initWithFrame:self.bounds];
        map.decelerationMode = RMMapDecelerationFast;
        //        map.adjustTilesForRetinaDisplay = [[UIScreen mainScreen] scale] > 1.0;
        [map setShowsUserLocation:YES];
        map.delegate = self;
        map.tileCache.backgroundCacheDelegate = self;
        map.showLogoBug = NO;
        map.missingTilesDepth = 4;
        map.hideAttribution = YES;
        _internalZoom = map.zoom;
        //        map.debugTiles = YES;
        map.userInteractionEnabled = YES;
        map.showsUserLocation = YES; // defaults
                                     //        map.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        map.clusteringEnabled = NO;
        //        [map removeAllCachedImages];
        [self addSubview:map];
        //Initialize loaded state to YES. This will automatically go to NO if the map needs to download new data
        loaded = YES;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(networkChanged:) name:kTiNetworkChangedNotification object:nil];
    }
    return map;
}

- (id)accessibilityElement
{
    return [self map];
}


-(void)networkChanged:(NSNotification*)note
{
    BOOL connected = [[note.userInfo objectForKey:@"online"] boolValue];
    if (map!=nil)
    {
        __block BOOL needsUpdate = NO;
        [map.tileSources enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:[RMAbstractWebMapSource class]]) {
                needsUpdate = needsUpdate || [((RMAbstractWebMapSource*)obj) onNetworkChange:connected];
            }
        }];
        if (needsUpdate) {
            //            [map setTileSources:map.tileSources];
            //            if ([self.proxy valueForUndefinedKey:@"region"]) {
            //                [self setRegion_:[self.proxy valueForUndefinedKey:@"region"]];
            //            }
            //            if ([self.proxy valueForUndefinedKey:@"zoom"]) {
            //                [self setZoom_:[self.proxy valueForUndefinedKey:@"zoom"]];
            //            }
            //            if ([self.proxy valueForUndefinedKey:@"centerCoordinate"]) {
            //                [self setCenterCoordinate_:[self.proxy valueForUndefinedKey:@"centerCoordinate"]];
            //            }
        }
    }
}

-(void)setBounds:(CGRect)bounds
{
    //TIMOB-13102.
    //When the bounds change the mapview fires the regionDidChangeAnimated delegate method
    //Here we update the region property which is not what we want.
    //Instead we set a forceRender flag and render in frameSizeChanged and capture updated
    //region there.
    ignoreRegionChanged = !_needsRegionUpdate;
    [super setBounds:bounds];
    ignoreRegionChanged = NO;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    BOOL animating = [self animating];
    
    
    //if we are animating it means we want to keep the zoom for sure...
    [[self map] setFrame:bounds animated:animating];
    [super frameSizeChanged:frame bounds:bounds];
    if (_needsRegionUpdate) {
        _needsRegionUpdate = NO;
        dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 0.0 * NSEC_PER_SEC);
        dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
            
            [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast regionFit:regionFits animated:NO roundOutZoom:YES];
        });
        
    }
}

-(void)configurationStart
{
    ignoreRegionChanged = YES;
    [super configurationStart];
}

-(void)configurationSet
{
    ignoreRegionChanged = NO;
    [super configurationSet];
}

//- (NSArray *)customAnnotations
//{
//    NSMutableArray* result = [NSMutableArray array];
//    for (RMAnnotation* annot in self.map.annotations) {
//        if (annot.userInfo) {
//            [result addObject:annot.userInfo];
//        }
//    }
//    return result;
//}


-(void)refreshAnnotation:(AkylasMapboxAnnotationProxy*)proxy readd:(BOOL)yn
{
    RMAnnotation *newSelected = [proxy getRMAnnotationForMapView:[self map]];
    if (!newSelected) return;
    RMAnnotation *selected = map.selectedAnnotation;
    BOOL wasSelected = newSelected == selected; //If selected == nil, this still returns FALSE.
    ignoreClicks = YES;
    [map deselectAnnotation:newSelected animated:[self shouldAnimate]];
    if (yn)
    {
        [map removeAnnotation:newSelected];
        [map addAnnotation:newSelected];
        [newSelected.layer setNeedsLayout];
    }
    if (wasSelected)
    {
        [map selectAnnotation:newSelected animated:[self shouldAnimate]];
    }
    ignoreClicks = NO;
}


-(void)internalAddAnnotations:(id)annotations atIndex:(NSInteger)index
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapboxAnnotationProxy* annotProxy in annotations) {
            [mapView addAnnotation:[(AkylasMapboxAnnotationProxy*)annotProxy getRMAnnotationForMapView:mapView]];
        }
    }
    else {
        [mapView addAnnotation:[(AkylasMapboxAnnotationProxy*)annotations getRMAnnotationForMapView:mapView]];
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapBaseAnnotationProxy* annotProxy in annotations) {
            if ([annotProxy isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
                RMAnnotation* annot = [(AkylasMapboxAnnotationProxy*)annotProxy getRMAnnotation];
                if (annot)[mapView removeAnnotation:annot];
            }
        }
    }
    else if ([annotations isKindOfClass:[AkylasMapboxAnnotationProxy class]]) {
        RMAnnotation* annot = [(AkylasMapboxAnnotationProxy*)annotations getRMAnnotation];
        if (annot)[mapView removeAnnotation:annot];
    }
}

-(void)removeAllAnnotations
{
    ENSURE_UI_THREAD_0_ARGS;
    RMMapView* mapView = [self map];
    [mapView removeAllAnnotations];
}

-(void)internalAddRoutes:(id)routes atIndex:(NSInteger)index
{
    [self internalAddAnnotations:routes atIndex:index];
}

-(void)internalRemoveRoutes:(id)routes
{
    [self internalRemoveAnnotations:routes];
}

-(void)removeAllRoutes
{
    ENSURE_UI_THREAD_0_ARGS;
    RMMapView* mapView = [self map];
    [mapView removeAllAnnotationsOfClass:[RMRouteAnnotation class]];
}

-(void)setSelectedAnnotation:(AkylasMapboxAnnotationProxy*)annotation
{
    RMAnnotation* an = [annotation getRMAnnotationForMapView:[self map]];
    [map selectAnnotation:an animated:[self shouldAnimate]];
    if (!annotation.marker.canShowCallout) {
        map.centerCoordinate = an.coordinate;
    }
}

-(void)selectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
    ENSURE_UI_THREAD(selectAnnotation,args);
    
    if (args == nil) {
        [[self map] deselectAnnotation:[self map].selectedAnnotation animated:[self shouldAnimate]];
    }
    
    if ([args isKindOfClass:[AkylasMapboxAnnotationProxy class]])
    {
        [self setSelectedAnnotation:args];
    }
}

-(void)deselectAnnotation:(id)args
{
    ENSURE_SINGLE_ARG(args,NSObject);
    ENSURE_UI_THREAD(deselectAnnotation,args);
    
    if ([args isKindOfClass:[AkylasMapboxAnnotationProxy class]])
    {
        [[self map] deselectAnnotation:[(AkylasMapboxAnnotationProxy*)args getRMAnnotation] animated:[self shouldAnimate]];
    }
}

-(void)selectUserAnnotation
{
    if ([self map].showsUserLocation) {
        CLLocation* location = [self map].userLocation.location;
        if (location) {
            [[self map] setCenterCoordinate:location.coordinate animated:[self shouldAnimate]];
        }
    }
}

//-(NSMutableArray*) setTileSourcesFromProp:(id)arg
//{
//    [self clearTileSources];
//
//
//    NSMutableArray* result = nil;
//    if ([arg isKindOfClass:[NSArray class]]) {
//        _tileSources =  [[NSMutableArray arrayWithCapacity:[arg count]] retain];
//        result =  [NSMutableArray arrayWithCapacity:[arg count]];
//        for (id source in arg) {
//            AkylasMapTileSourceProxy* tileSource = [AkylasTileSource tileSourceProxyWithSource:source proxyForSourceURL:self.proxy];
//            if (tileSource) {
//                [self.proxy rememberProxy:tileSource];
//                [_tileSources addObject:tileSource];
//                [result addObject:[tileSource.tileSource tileSource]];
//            }
//        }
//    }
//    else {
//        AkylasMapTileSourceProxy* tileSource = [AkylasTileSource tileSourceProxyWithSource:arg proxyForSourceURL:self.proxy];
//        if (tileSource)  {
//            [self.proxy rememberProxy:tileSource];
//            _tileSources =  [[NSMutableArray arrayWithObject:tileSource] retain];
//            result = [NSMutableArray arrayWithObject:[tileSource.tileSource tileSource]];
//        }
//    }
//    return result;
//}


-(void)zoomTo:(id)args
{
    ENSURE_SINGLE_ARG(args,NSObject);
    ENSURE_UI_THREAD(zoomTo,args);
    
    double v = [TiUtils doubleValue:args];
    [map setZoom:[TiUtils doubleValue:args] animated:YES];
}


-(RMSphericalTrapezium) getCurrentRegion
{
    return [map latitudeLongitudeBoundingBox];
}

-(id)metersPerPixel_
{
    return @([[self map] metersPerPixel]);
}


-(void)setScrollableAreaLimit_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    AkRegion bounds = kAkDefaultLatLonBoundingBox;
    if (value!=nil)
    {
        bounds = [AkylasMapboxModule regionFromObject:value];
    }
    [[self map] setConstraintsSouthWest:bounds.southWest northEast:bounds.northEast];
}

-(void)setRegion_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    if (value==nil)
    {
    }
    else
    {
        region = [AkylasMapboxModule regionFromObject:value];
        map.userTrackingMode = RMUserTrackingModeNone;
        if ([(TiViewProxy*)[self proxy] viewLayedOut]) {
            [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast regionFit:regionFits animated:[self shouldAnimate] roundOutZoom:YES];
        }
        else {
            _needsRegionUpdate = YES;
        }
    }
}


-(void)setConstraintRegionFit_:(id)value
{
    [self map].constraintRegionFit = [TiUtils boolValue:value];
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
    return NUMBOOL([self map].showsUserLocation);
}

-(id)userLocation_
{
    return [self dictFromUserLocation:[self map].userLocation];
}


-(void)setUserTrackingMode_:(id)value
{
    ENSURE_SINGLE_ARG(value,NSNumber);
    TiThreadPerformBlockOnMainThread(^{
        [self map].userTrackingMode = [TiUtils intValue:value def:RMUserTrackingModeNone];
    }, NO);
}

-(void)setUserLocationRequiredZoom_:(id)value
{
    ENSURE_SINGLE_ARG(value,NSNumber);
    [self map].userLocationRequiredZoom = [TiUtils floatValue:value def:10];
}

-(id)userTrackingMode_
{
    return NUMINT([self map].userTrackingMode);
}

-(void)setAdjustTilesForRetinaDisplay_:(id)value
{
    [self map].adjustTilesForRetinaDisplay = [TiUtils boolValue:value def:[self map].adjustTilesForRetinaDisplay];
}

-(void)setDebugTiles_:(id)debug
{
    [self map].debugTiles = [TiUtils boolValue:debug];
}

-(BOOL)shouldAnimate
{
    return [self viewInitialized] && animate;
}

-(void)setZoom_:(id)zoom
{
    CGFloat newValue = [TiUtils floatValue:zoom def:_internalZoom];
    if (newValue == _internalZoom) return;
    float screenScale = [UIScreen mainScreen].scale;
    if (![self map].adjustTilesForRetinaDisplay && screenScale > 1.0)
        newValue -= 1.0;
    [[self map] setZoom:newValue animated:[self shouldAnimate]];
}


-(void)setCenterCoordinate_:(id)center
{
    CLLocationCoordinate2D coord = center?[AkylasMapboxModule locationFromObject:center]:[self map].userLocation.location.coordinate;

    [[self map] setCenterCoordinate:coord animated:[self shouldAnimate]];
}

-(id)centerCoordinate_
{
    CLLocationCoordinate2D coord = [self map].centerCoordinate;
    return [AkylasMapboxModule dictFromLocation2D:coord];
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
    [[self map] zoomInToNextNativeZoomAt:pivot animated:[self shouldAnimate] && animated];
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
    [[self map] zoomOutToNextNativeZoomAt:pivot animated:[self shouldAnimate] && animated];
}

-(void)setTintColor_:(id)color
{
    [self map].tintColor = [TiUtils colorValue:color].color;
}

-(NSDictionary*)dictFromUserLocation:(RMUserLocation*)userLocation
{
    if (!userLocation) return @{};
    NSDictionary* result = [AkylasMapboxModule dictFromLocation:userLocation.location] ;
    if (userLocation.heading) {
        NSMutableDictionary* mutDict = [NSMutableDictionary dictionaryWithDictionary:result];
        [mutDict setObject:[AkylasMapboxModule dictFromHeading:userLocation.heading] forKey:@"heading"];
        result = mutDict;
    }
    return result;
}

-(RMStackTileSource*)getTileSourceContainer {
    if (!_tileSourceContainer) {
        _tileSourceContainer =[[RMStackTileSource alloc] init];
    }
    return _tileSourceContainer;
}

//-(void)setTileSource_:(id)value
//{
//    if (_userStackTileSource) {
//        [[self getTileSourceContainer] setTileSources:[self setTileSourcesFromProp:value]];
//        if ([[[self map] tileSources] count] == 0) {
//            [[self map] setTileSource:_tileSourceContainer];
//        }
//    }
//    else {
//        [[self map] setTileSources:[[[self setTileSourcesFromProp:value] reverseObjectEnumerator] allObjects]];
//    }
//}

//- (BOOL)addTileSource:(AkylasMapTileSourceProxy*)tileSource
//{
//    [self addTileSource:tileSource atIndex:-1];
//}

- (BOOL)internalAddTileSources:(id)tileSource atIndex:(NSInteger)index
{
    __block NSInteger realIndex = index;
    if (realIndex == -1) {
        realIndex = INT_MAX;
    }
    if (IS_OF_CLASS(tileSource, NSArray)) {
        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self internalAddTileSources:obj atIndex:realIndex++];
        }];
    } else {
        RMMapView* mapView = [self map];
        id<RMTileSource> source = [(AkylasMapboxTileSourceProxy*)tileSource getMPTileSourceForMapView:mapView];
        if (_userStackTileSource) {
            [[self getTileSourceContainer] addTileSource:source atIndex:realIndex];
            if ([[mapView tileSources] count] == 0) {
                [mapView setTileSource:_tileSourceContainer];
            }
        } else {
            [mapView addTileSource:source atIndex:realIndex];
        }
        [mapView setAlpha:((AkylasMapboxTileSourceProxy*)tileSource).opacity forTileSource:source];
    }
}

- (BOOL)internalRemoveTileSources:(id)tileSource
{
    if (IS_OF_CLASS(tileSource, NSArray)) {
        [tileSource enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self internalRemoveTileSources:obj];
        }];
    } else {
        if (_userStackTileSource) {
            [_tileSourceContainer removeTileSource:[(AkylasMapboxTileSourceProxy*)tileSource mpTileSource]];
        } else {
            [[self map] removeTileSource:[(AkylasMapboxTileSourceProxy*)tileSource mpTileSource]];
        }
    }
}

-(void)removeAllTileSources
{
    ENSURE_UI_THREAD_0_ARGS;
    if (_userStackTileSource) {
        if (_tileSourceContainer) {
            [[self map] removeTileSource:_tileSourceContainer];
            RELEASE_TO_NIL(_tileSourceContainer)
        }
    } else {
        [[self map] setTileSources:nil];
    }
}

#pragma mark Delegates

- (RMMapLayer *)mapView:(RMMapView *)theMap layerForAnnotation:(RMAnnotation *)annotation
{
    if (annotation.isUserLocationAnnotation)
        return nil;
    
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    return [proxy shapeLayerForMapView:self];
}

- (BOOL)mapView:(RMMapView *)theMap shouldDragAnnotation:(RMAnnotation *)annotation;
{
    if (annotation.isUserLocationAnnotation)
        return false;
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    if (proxy) return [proxy draggable];
    return false;
}

- (void)mapView:(RMMapView *)theMap didUpdateUserLocation:(RMUserLocation *)userLocation
{
    if ([self.viewProxy _hasListeners:@"location" checkParent:NO])
    {
        [self.proxy fireEvent:@"location" withObject:[self dictFromUserLocation:userLocation] propagate:NO checkForListener:NO];
    }
}

- (void)mapView:(RMMapView *)theMap didFailToLocateUserWithError:(NSError *)error
{
    if ([self.viewProxy _hasListeners:@"location" checkParent:NO])
    {
        [self.proxy fireEvent:@"location" withObject:@{@"error":[error description]} propagate:NO checkForListener:NO];
    }
}

- (void)mapView:(RMMapView *)theMap didChangeUserTrackingMode:(RMUserTrackingMode)mode animated:(BOOL)animated
{
    if ([self.viewProxy _hasListeners:@"usertracking" checkParent:NO])
    {
        [self.proxy fireEvent:@"usertracking" withObject:@{@"mode":NUMINT(mode), @"animated":NUMBOOL(animated)} propagate:NO checkForListener:NO];
    }
}

- (void)mapViewRegionDidChange:(RMMapView *)theMap
{
    if (ignoreRegionChanged) {
        return;
    }
    if ([self.viewProxy _hasListeners:@"regionchanged" checkParent:NO])
    {
        [self.proxy fireEvent:@"regionchanged" withObject:@{
                                                            @"region":[self.proxy valueForKey:@"region"],
                                                            @"zoom":@(theMap.adjustedZoomForRetinaDisplay)
                                                            } propagate:NO checkForListener:NO];
    }
}


- (void)singleTapOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"click" withRecognizer:recognizer];
    [self fireEventOnMap:@"singletap" withRecognizer:recognizer];
}
- (void)doubleTapOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"doubleclick" withRecognizer:recognizer];
}

- (void)singleTapTwoFingersOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"singletap"  withRecognizer:recognizer];
}
- (void)longPressOnMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEventOnMap:@"longpress" withRecognizer:recognizer];
}
- (void)tapOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:(UIGestureRecognizer *)recognizer
{
    [self fireEvent:@"click" onAnnotation:annotation source:@"pin" withRecognizer:recognizer];
}

- (void)doubleTapOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
{
    if (![self fireEvent:@"doubleclick" onAnnotation:annotation source:@"pin" withRecognizer:recognizer]) {
        [theMap doubleTapWithGesture:recognizer];
    }
}

- (void)longPressOnAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
{
    [self fireEvent:@"longpress" onAnnotation:annotation source:@"pin" withRecognizer:recognizer];
}

- (void)tapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
{
    [self fireEvent:@"click" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
}

- (void)doubleTapOnLabelForAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
{
    [self fireEvent:@"doubleclick" onAnnotation:annotation source:@"label" withRecognizer:recognizer];
}

- (void)tapOnCalloutAccessoryControl:(UIControl *)control forAnnotation:(RMAnnotation *)annotation onMap:(RMMapView *)theMap recognizer:recognizer
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

- (void)mapView:(RMMapView *)theMap willShowCallout:(SMCalloutView*)callout forAnnotation:(RMAnnotation *)annotation {
    
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    if (PostVersion7) {
        callout.tintColor = self.tintColor;
    }
    
    // Apply the desired calloutOffset (from the top-middle of the view)
    CGPoint calloutOffset = [proxy nGetCalloutAnchorPoint];
    calloutOffset.y +=0.5f;
    calloutOffset.x *= annotation.layer.frame.size.width;
    calloutOffset.y *= annotation.layer.frame.size.height;
    
    callout.calloutOffset = calloutOffset;
    
    if (proxy == nil) {
        callout.title    = annotation.title;
        callout.subtitle = annotation.subtitle;
        return;
    }
    callout.title    = [proxy title];
    callout.subtitle = [proxy subtitle];
    SMCalloutMaskedBackgroundView* backView = (SMCalloutMaskedBackgroundView*)callout.backgroundView;
    backView.alpha = [proxy nGetCalloutAlpha];
    
    if (_calloutUseTemplates) {
        callout.leftAccessoryView = [self reusableViewForProxy:proxy objectKey:@"leftView"];
        callout.rightAccessoryView = [self reusableViewForProxy:proxy objectKey:@"rightView"];
        callout.contentView = [self reusableViewForProxy:proxy objectKey:@"customView"];
    }
    else {
        callout.leftAccessoryView = [proxy nGetLeftViewAccessory];
        callout.rightAccessoryView = [proxy nGetRightViewAccessory];
        callout.contentView = [proxy nGetCustomViewAccessory];
    }
    
    callout.arrowHeight = [proxy nGetCalloutArrowHeight];
    callout.contentViewInset = [proxy nGetCalloutPadding];
    if (backView && [backView isKindOfClass:[SMCalloutMaskedBackgroundView class]]) {
        backView.backgroundColor = [proxy nGetCalloutBackgroundColor];
        backView.cornerRadius = [proxy nGetCalloutBorderRadius];
    }
    callout.permittedArrowDirection = SMCalloutArrowDirectionDown;
}



- (void)mapView:(RMMapView *)theMap didHideCallout:(SMCalloutView*)callout forAnnotation:(RMAnnotation *)annotation {
    if (_calloutUseTemplates) {
        [self reuseIfNecessary:callout.leftAccessoryView];
        [self reuseIfNecessary:callout.rightAccessoryView];
        [self reuseIfNecessary:callout.contentView];
    }
}

- (void)mapView:(RMMapView *)theMap didSelectAnnotation:(RMAnnotation *)annotation {
    [self fireEvent:@"focus" onAnnotation:annotation source:@"pin" withRecognizer:nil];
}

- (void)mapView:(RMMapView *)theMap didDeselectAnnotation:(RMAnnotation *)annotation{
    [self fireEvent:@"blur" onAnnotation:annotation source:@"pin" withRecognizer:nil];
}

- (void)beforeMapZoom:(RMMapView *)theMap byUser:(BOOL)wasUserAction
{
    NSString* type = @"willZoom";
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        
        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}
- (void)afterMapZoom:(RMMapView *)theMap byUser:(BOOL)wasUserAction
{
    NSString* type = @"zoom";
    _internalZoom = theMap.adjustedZoomForRetinaDisplay;
    [self.proxy replaceValue:NUMFLOAT(_internalZoom) forKey:type notification:NO];
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        
        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               NUMFLOAT(_internalZoom), @"zoom",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}

- (void)beforeMapMove:(RMMapView *)theMap byUser:(BOOL)wasUserAction;
{
    NSString* type = @"willMove";
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        
        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}
- (void)afterMapMove:(RMMapView *)theMap byUser:(BOOL)wasUserAction
{
    NSString* type = @"move";
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        
        NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               nil
                               ];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}

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

- (void)mapView:(RMMapView *)mapView annotation:(RMAnnotation *)annotation didChangeDragState:(RMMapLayerDragState)newState fromOldState:(RMMapLayerDragState)oldState
{
    [self firePinChangeDragState:annotation newState:newState fromOldState:oldState];
}

- (void)firePinChangeDragState:(RMAnnotation *) annotation newState:(RMMapLayerDragState)newState fromOldState:(RMMapLayerDragState)oldState
{
    AkylasMapboxAnnotationProxy* proxy = annotation.userInfo;
    
    if (proxy == nil)
        return;
    
    
    if (newState == RMMapLayerDragStateEnding) {
        CLLocationCoordinate2D coord = annotation.coordinate;
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
                            NUMINT(oldState),@"oldState",
                            nil];
    
    if (parentWants)
        [ourProxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
    
    if (viewWants)
        [proxy fireEvent:@"pinchangedragstate" withObject:event propagate:NO checkForListener:NO];
}

#pragma mark Event generation

- (BOOL)fireEvent:(NSString*)type onAnnotation:(RMAnnotation *) pinview source:(NSString *)source withRecognizer:(UIGestureRecognizer*)recognizer
{
    if (ignoreClicks)
    {
        return;
    }
    
    AkylasMapboxAnnotationProxy *annotProxy = pinview.userInfo;
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
    
    id title = [annotProxy title];
    if (title == nil)
    {
        title = [NSNull null];
    }
    
    id clicksource = source ? source : (id)[NSNull null];
    
    CGPoint point = [recognizer locationInView:self];
    NSMutableDictionary *event = [TiUtils dictionaryFromGesture:recognizer inView:self];
    [event addEntriesFromDictionary:[AkylasMapboxModule dictFromLocation2D:[map pixelToCoordinate:point]]];
    [event setObject:clicksource forKey:@"clicksource"];
    [event setObject:annotProxy forKey:@"annotation"];
    [event setObject:ourProxy forKey:@"map"];
    [event setObject:title forKey:@"title"];
    [event setObject:NUMINT([annotProxy tag]) forKey:@"index"];
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

- (void)fireEventOnMap:(NSString*)type withRecognizer:(UIGestureRecognizer*)recognizer
{
    if (ignoreClicks)
    {
        return;
    }
    
    if ([self.viewProxy _hasListeners:type checkParent:NO]) {
        CGPoint point = [recognizer locationInView:self];
        NSMutableDictionary *event = [TiUtils dictionaryFromGesture:recognizer inView:self];
        [event addEntriesFromDictionary:[AkylasMapboxModule dictFromLocation2D:[map pixelToCoordinate:point]]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
    }
}
@end
