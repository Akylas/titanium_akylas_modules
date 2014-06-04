//
//  AkylasMapMapboxView.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapMapboxView.h"
#import "AkylasTileSource.h"
#import "AkylasMapAnnotationProxy.h"
#import "AkylasMapModule.h"

@implementation AkylasMapMapboxView
{
    RMMapView *map;
    NSMutableArray *_tileSources;
    RMStackTileSource* _tileSourceContainer;
    BOOL _userStackTileSource;
    BOOL _needsRegionUpdate;
}

- (id)init
{
    if ((self = [super init])) {
        _userStackTileSource = NO;
        _needsRegionUpdate = NO;
    }
    return self;
}


-(void)clearTileSources {
    if (_tileSources) {
        for ( AkylasMapTileSourceProxy* tileSource in _tileSources) {
            [self.proxy forgetProxy:tileSource];
        }
        RELEASE_TO_NIL(_tileSources)
    }
}

-(void)dealloc
{
	if (map!=nil)
	{
		map.delegate = nil;
		RELEASE_TO_NIL(map);
	}
    [self clearTileSources];
	[super dealloc];
}

-(UIView*)viewForHitTest
{
    return map;
}

-(RMMapView*)map
{
    if (map==nil)
    {
        map = [[RMMapView alloc] initWithFrame:[TiUtils appFrame]];
        map.decelerationMode = RMMapDecelerationFast;
        CLLocationCoordinate2D coord = map.centerCoordinate;
        //        map.adjustTilesForRetinaDisplay = [[UIScreen mainScreen] scale] > 1.0;
        [map setShowsUserLocation:YES];
        map.delegate = self;
        map.tileCache.backgroundCacheDelegate = self;
        map.showLogoBug = NO;
        map.missingTilesDepth = 4;
        map.hideAttribution = YES;
        //        map.debugTiles = YES;
        map.userInteractionEnabled = YES;
        map.showsUserLocation = YES; // defaults
//        map.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
        map.clusteringEnabled = NO;
        //        [map removeAllCachedImages];
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


//-(void)setBounds:(CGRect)bounds
//{
//    BOOL animating = [self animating];
//    //if we are animating it means we want to keep the zoom for sure...
//    [[self map] setFrame:bounds keepZoom:animating animated:animating];
//	[super setBounds:bounds];
//}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    BOOL animating = [self animating];
    
    
    //if we are animating it means we want to keep the zoom for sure...
    [[self map] setFrame:bounds keepZoom:animating animated:animating];
    [super frameSizeChanged:frame bounds:bounds];
    if (_needsRegionUpdate) {
        _needsRegionUpdate = NO;
        [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast regionFit:regionFits animated:NO];
    }
}


-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn
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


-(void)internalAddAnnotations:(id)annotations
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapAnnotationProxy* annotProxy in annotations) {
            [mapView addAnnotation:[annotProxy getRMAnnotationForMapView:mapView]];
        }
    }
    else {
        [mapView addAnnotation:[(AkylasMapAnnotationProxy*)annotations getRMAnnotationForMapView:mapView]];
    }
}

-(void)internalRemoveAnnotations:(id)annotations
{
    RMMapView* mapView = [self map];
    if ([annotations isKindOfClass:[NSArray class]]) {
        for (AkylasMapAnnotationProxy* annotProxy in annotations) {
            if ([annotProxy isKindOfClass:[AkylasMapAnnotationProxy class]]) {
                RMAnnotation* annot = [annotProxy getRMAnnotation];
                if (annot)[mapView removeAnnotation:annot];
            }
        }
    }
    else if ([annotations isKindOfClass:[AkylasMapAnnotationProxy class]]) {
        RMAnnotation* annot = [(AkylasMapAnnotationProxy*)annotations getRMAnnotation];
        if (annot)[mapView removeAnnotation:annot];
    }
}


-(void)setSelectedAnnotation:(AkylasMapAnnotationProxy*)annotation
{
    RMAnnotation* an = [annotation getRMAnnotationForMapView:[self map]];
    [map selectAnnotation:[annotation getRMAnnotationForMapView:[self map]] animated:[self shouldAnimate]];
    map.centerCoordinate = an.coordinate;
}

-(void)selectAnnotation:(id)args
{
	ENSURE_SINGLE_ARG_OR_NIL(args,NSObject);
	ENSURE_UI_THREAD(selectAnnotation,args);
	
	if (args == nil) {
        [[self map] deselectAnnotation:[self map].selectedAnnotation animated:[self shouldAnimate]];
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
    
	if ([args isKindOfClass:[AkylasMapAnnotationProxy class]])
	{
		[[self map] deselectAnnotation:[(AkylasMapAnnotationProxy*)args getRMAnnotation] animated:[self shouldAnimate]];
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

-(NSMutableArray*) setTileSourcesFromProp:(id)arg
{
    [self clearTileSources];
    
    
    NSMutableArray* result = nil;
    if ([arg isKindOfClass:[NSArray class]]) {
        _tileSources =  [[NSMutableArray arrayWithCapacity:[arg count]] retain];
        result =  [NSMutableArray arrayWithCapacity:[arg count]];
        for (id source in arg) {
            AkylasMapTileSourceProxy* tileSource = [AkylasTileSource tileSourceProxyWithSource:source proxyForSourceURL:self.proxy];
            if (tileSource) {
                [self.proxy rememberProxy:tileSource];
                [_tileSources addObject:tileSource];
                [result addObject:[tileSource.tileSource tileSource]];
            }
        }
    }
    else {
        AkylasMapTileSourceProxy* tileSource = [AkylasTileSource tileSourceProxyWithSource:arg proxyForSourceURL:self.proxy];
        if (tileSource)  {
            [self.proxy rememberProxy:tileSource];
            _tileSources =  [[NSMutableArray arrayWithObject:tileSource] retain];
            result = [NSMutableArray arrayWithObject:[tileSource.tileSource tileSource]];
        }
    }
    return result;
}


-(void)zoom:(id)args
{
	ENSURE_SINGLE_ARG(args,NSObject);
	ENSURE_UI_THREAD(zoom,args);
    
	double v = [TiUtils doubleValue:args];
	[map setZoom:[TiUtils doubleValue:args] animated:YES];
}


-(RMSphericalTrapezium) getCurrentRegion
{
    return [map latitudeLongitudeBoundingBox];
}


-(void)setScrollableAreaLimit_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    RMSphericalTrapezium bounds = kMapboxDefaultLatLonBoundingBox;
    if (value!=nil)
	{
		bounds = [AkylasMapModule regionFromDict:value];
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
		region = [AkylasMapModule regionFromDict:value];
        map.userTrackingMode = RMUserTrackingModeNone;
        if ([(TiViewProxy*)[self proxy] viewLayedOut]) {
            [map zoomWithLatitudeLongitudeBoundsSouthWest:region.southWest northEast:region.northEast regionFit:regionFits animated:[self shouldAnimate]];
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
	[self map].showsUserLocation = [TiUtils boolValue:value];
}


-(id)userLocationEnabled
{
    return NUMBOOL([self map].showsUserLocation);
}

-(id)userLocation
{
    return [self dictFromUserLocation:[self map].userLocation];
}


-(void)setUserTrackingMode_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
	[self map].userTrackingMode = [TiUtils intValue:value def:RMUserTrackingModeNone];
}

-(void)setUserLocationRequiredZoom_:(id)value
{
	ENSURE_SINGLE_ARG(value,NSNumber);
	[self map].userLocationRequiredZoom = [TiUtils floatValue:value def:10];
}

-(id)userTrackingMode
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
    CGFloat newValue = [TiUtils floatValue:zoom def:_zoom];
    if (newValue == _zoom) return;
    [[self map] setZoom:newValue animated:[self shouldAnimate]];
}


-(void)setCenterCoordinate_:(id)center
{
    CLLocationCoordinate2D coord;
    if ([center isKindOfClass:[NSArray class]]) {
         coord = CLLocationCoordinate2DMake([TiUtils floatValue:[center objectAtIndex:0]],[TiUtils floatValue:[center objectAtIndex:1]]);
    }
    else {
        ENSURE_SINGLE_ARG(center,NSDictionary);
        if (center) {
            coord = [AkylasMapModule locationFromDict:center];
        }
        else {
            coord = [self map].userLocation.location.coordinate;
        }

    }
    [[self map] setCenterCoordinate:coord animated:[self shouldAnimate]];
}

-(id)centerCoordinate
{
    CLLocationCoordinate2D coord = [self map].centerCoordinate;
    return [AkylasMapModule dictFromLocation2D:coord];
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
    NSDictionary* result = [AkylasMapModule dictFromLocation:userLocation.location] ;
    if (userLocation.heading) {
        NSMutableDictionary* mutDict = [NSMutableDictionary dictionaryWithDictionary:result];
        [mutDict setObject:[AkylasMapModule dictFromHeading:userLocation.heading] forKey:@"heading"];
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

-(void)setTileSource_:(id)value
{
    if (_userStackTileSource) {
        [[self getTileSourceContainer] setTileSources:[self setTileSourcesFromProp:value]];
        if ([[[self map] tileSources] count] == 0) {
            [[self map] setTileSource:_tileSourceContainer];
        }
    }
    else {
        [[self map] setTileSources:[[[self setTileSourcesFromProp:value] reverseObjectEnumerator] allObjects]];
    }
}

- (BOOL)addTileSource:(AkylasMapTileSourceProxy*)tileSource
{
    [self addTileSource:tileSource atIndex:-1];
}

- (BOOL)addTileSource:(AkylasMapTileSourceProxy*)tileSource atIndex:(NSInteger)index
{
    if (_tileSources) {
        if (index < 0 || index >= [_tileSources count]) {
            [_tileSources addObject:tileSource];
        }
        else {
            [_tileSources insertObject:tileSource atIndex:index];
        }
    }
    else {
        _tileSources = [[NSMutableArray arrayWithObject:tileSource] retain];
    }
    if (_userStackTileSource) {
        [[self getTileSourceContainer] addTileSource:[[tileSource tileSource] tileSource] atIndex:index];
        if ([[[self map] tileSources] count] == 0) {
            [[self map] setTileSource:_tileSourceContainer];
        }
    }
    else {
        if (index == -1) {
            index = 0;
        }
        else {
            index = [_tileSources count] - index - 1;
        }
        [[self map] addTileSource:[[tileSource tileSource] tileSource] atIndex:index];
    }
    
}

- (BOOL)removeTileSource:(AkylasMapTileSourceProxy*)tileSource
{
    if (_tileSources) {
        [_tileSources removeObject:tileSource];
    }
    if (_userStackTileSource) {
        [_tileSourceContainer removeTileSource:[[tileSource tileSource] tileSource]];
    } else {
        [[self map] removeTileSource:[[tileSource tileSource] tileSource]];
    }
}



#pragma mark Delegates

- (RMMapLayer *)mapView:(RMMapView *)mapView layerForAnnotation:(RMAnnotation *)annotation
{
    if (annotation.isUserLocationAnnotation)
        return nil;
    
    AkylasMapAnnotationProxy* proxy = annotation.userInfo;
    return [proxy shapeLayerForMapView:self];
}

- (BOOL)mapView:(RMMapView *)mapView shouldDragAnnotation:(RMAnnotation *)annotation;
{
    if (annotation.isUserLocationAnnotation)
        return false;
    AkylasMapAnnotationProxy* proxy = annotation.userInfo;
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
    if ([annotation.userInfo isKindOfClass:[AkylasMapAnnotationProxy class]])
	{
        AkylasMapAnnotationProxy* proxy = annotation.userInfo;
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

- (void)afterMapZoom:(RMMapView *)mapView byUser:(BOOL)wasUserAction
{
    NSString* type = @"zoom";
    _zoom = mapView.zoom;
    [self.proxy replaceValue:NUMFLOAT(_zoom) forKey:type notification:NO];
    if ([self.proxy _hasListeners:type]) {
        
		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
                               NUMBOOL(wasUserAction),@"userAction",
                               NUMFLOAT(_zoom), @"zoom",
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
    AkylasMapAnnotationProxy* proxy = annotation.userInfo;
    
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

#pragma mark Event generation

- (void)fireEvent:(NSString*)type onAnnotation:(RMAnnotation *) pinview source:(NSString *)source withRecognizer:(UIGestureRecognizer*)recognizer
{
	if (ignoreClicks)
	{
		return;
	}
    
	AkylasMapAnnotationProxy *viewProxy = pinview.userInfo;
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
    [event addEntriesFromDictionary:[AkylasMapModule dictFromLocation2D:[map pixelToCoordinate:point]]];
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

- (void)fireEventOnMap:(NSString*)type withRecognizer:(UIGestureRecognizer*)recognizer
{
	if (ignoreClicks)
	{
		return;
	}
    
	if ([self.proxy _hasListeners:type]) {
        CGPoint point = [recognizer locationInView:self];
        NSMutableDictionary *event = [[TiUtils dictionaryFromGesture:recognizer inView:self] mutableCopy];
        [event addEntriesFromDictionary:[AkylasMapModule dictFromLocation2D:[map pixelToCoordinate:point]]];
        [self.proxy fireEvent:type withObject:event propagate:NO checkForListener:NO];
        [event release];
	}
}


@end
