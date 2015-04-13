/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapView.h"
#import "AkylasMapViewProxy.h"
#import "TiUtils.h"
#import "AkylasMapModule.h"
#import "AkylasMapAnnotationProxy.h"
#import "AkylasMapRouteProxy.h"
#import "ImageLoader.h"


@implementation CalloutReusableView {
    NSDictionary *_dataItem;
}
@synthesize dataItem = _dataItem;

- (id)initWithFrame:(CGRect)frame reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithFrame:frame]) {
        _reuseIdentifier = [reuseIdentifier copy];
        self.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    }
    return self;
}

-(ReusableViewProxy*)reusableProxy
{
    return (ReusableViewProxy*)self.proxy;
}

- (void)dealloc
{
    [[self reusableProxy] detachView];
    [[self reusableProxy] deregisterProxy:[[self reusableProxy] pageContext]];
    [self reusableProxy].modelDelegate = nil;
    RELEASE_TO_NIL(_dataItem)
    [super dealloc];
}

- (BOOL)canApplyDataItem:(NSDictionary *)otherItem;
{
    id template = [_dataItem objectForKey:@"template"];
    id otherTemplate = [otherItem objectForKey:@"template"];
    BOOL same = (template == otherTemplate) || [template isEqual:otherTemplate];
    return same;
}

- (void)setDataItem:(NSDictionary *)dataItem
{
    if (dataItem == (_dataItem)) return;
    if (_dataItem) {
        RELEASE_TO_NIL(_dataItem)
        [(TiViewProxy*)self.proxy dirtyItAll];
    }
    _dataItem = [dataItem retain];
    [[self reusableProxy] setDataItem:_dataItem];
}

- (void)prepareForReuse
{
    [[self reusableProxy] prepareForReuse];
}

-(void)configurationStart
{
    [super configurationStart];
}

-(void)configurationSet
{
    [super configurationSet];
}
@end


@implementation CalloutViewProxy
{
    NSString *_reuseIdentifier;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_reuseIdentifier);
    [super dealloc];
}

- (id)initInContext:(id<TiEvaluator>)context reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initInContext:context]) {
        //        defaultReadyToCreateView = YES;
        self.canBeResizedByFrame  = YES;
        
        _reuseIdentifier = [reuseIdentifier copy];
        //        [self setView:[[CalloutReusableView alloc] initWithFrame:CGRectZero reuseIdentifier:reuseIdentifier]];
    }
    return self;
}


-(TiUIView*)newView {
    return [[CalloutReusableView alloc] initWithFrame:CGRectZero reuseIdentifier:_reuseIdentifier];
}

-(TiDimension)defaultAutoWidthBehavior:(id)unused
{
    return TiDimensionAutoSize;
}
-(TiDimension)defaultAutoHeightBehavior:(id)unused
{
    return TiDimensionAutoSize;
}

- (NSDictionary *)overrideEventObject:(NSDictionary *)eventObject forEvent:(NSString *)eventType fromViewProxy:(TiViewProxy *)viewProxy
{
    NSMutableDictionary *updatedEventObject = (NSMutableDictionary*)[super overrideEventObject:eventObject forEvent:eventType fromViewProxy:viewProxy];
    [updatedEventObject setObject:@YES forKey:@"inCallout"];
    if (_annotation) {
        [updatedEventObject setObject:_annotation forKey:@"annotation"];
    }
    return updatedEventObject;
}

@end

@interface AkylasMapViewProxy()

-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg;
-(AkylasMapRouteProxy*)routeFromArg:(id)arg;
-(AkylasMapTileSourceProxy*)tileSourceFromArg:(id)arg;

@end

@implementation AkylasMapView
{
    NSDictionary* _calloutTemplates;
    id _defaultCalloutTemplate;
    NSMutableDictionary *_reusableViews;
}
@synthesize defaultPinImage;
@synthesize defaultPinAnchor;
@synthesize defaultCalloutAnchor;

#pragma mark Internal

- (id)init
{
    if ((self = [super init])) {
        defaultPinAnchor = CGPointMake(0.5, 0.5);
        defaultCalloutAnchor = CGPointMake(0, -0.5f);
        _internalZoom = -1;
        animate = true;
        regionFits = NO;
        _calloutUseTemplates = NO;
        _reusableViews = [[NSMutableDictionary alloc] init];
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_calloutTemplates)
    RELEASE_TO_NIL(_defaultCalloutTemplate)
    RELEASE_TO_NIL(_reusableViews)
	[super dealloc];
}

-(void)zoom_
{
    return @(_internalZoom);
}


-(BOOL)viewInitialized {
    return [(TiViewProxy*)(self.proxy) viewInitialized];
}

-(id)annotationsFromArgs:(id)value
{
    if (IS_OF_CLASS(value, NSArray)) {
        NSMutableArray * result = [NSMutableArray arrayWithCapacity:[value count]];
        if (value!=nil)
        {
            for (id arg in value)
            {
                [result addObject:[(AkylasMapViewProxy*)[self proxy] annotationFromArg:arg]];
            }
        }
        return result;
    } else {
        return [(AkylasMapViewProxy*)[self proxy] annotationFromArg:value];
    }
}

-(id)routesFromArgs:(id)value
{
    if (IS_OF_CLASS(value, NSArray)) {
        NSMutableArray * result = [NSMutableArray arrayWithCapacity:[value count]];
        if (value!=nil)
        {
            for (id arg in value)
            {
                [result addObject:[(AkylasMapViewProxy*)[self proxy] routeFromArg:arg]];
            }
        }
        return result;
    } else {
        return [(AkylasMapViewProxy*)[self proxy] routeFromArg:value];
    }
}

-(id)tileSourcesFromArgs:(id)value
{
    if (IS_OF_CLASS(value, NSArray)) {
        NSMutableArray * result = [NSMutableArray arrayWithCapacity:[value count]];
        if (value!=nil)
        {
            for (id arg in value)
            {
                [result addObject:[(AkylasMapViewProxy*)[self proxy] tileSourceFromArg:arg]];
            }
        }
        return result;
    } else {
        return [(AkylasMapViewProxy*)[self proxy] tileSourceFromArg:value];
    };
}

-(void)refreshAnnotation:(AkylasMapAnnotationProxy*)proxy readd:(BOOL)yn
{
}


-(BOOL)internalAddAnnotations:(id)value atIndex:(NSInteger)index{}
-(BOOL)internalRemoveAnnotations:(id)value{}
-(BOOL)internalRemoveAllAnnotations{}
-(BOOL)internalAddRoutes:(id)value atIndex:(NSInteger)index{}
-(BOOL)internalRemoveRoutes:(id)value{}
-(BOOL)internalRemoveAllRoutes{}
-(BOOL)internalAddTileSources:(id)value atIndex:(NSInteger)index{}
-(BOOL)internalRemoveTileSources:(id)value{}
-(BOOL)internalRemoveAllTileSources{}

#pragma mark Public APIs

-(void)addAnnotation:(id)args atIndex:(NSInteger)index
{
	[self internalAddAnnotations:args atIndex:index];
}

-(void)removeAnnotation:(id)args
{
	 [self internalRemoveAnnotations:args];
}

-(void)removeAllAnnotations
{
	ENSURE_UI_THREAD_0_ARGS;
    [self internalRemoveAllAnnotations];
}

-(void)setAnnotations_:(id)value
{
	ENSURE_TYPE_OR_NIL(value,NSArray);
	ENSURE_UI_THREAD(setAnnotations_,value)
	if (value != nil) {
		[self addAnnotation:value atIndex:-1];
	}
}


-(void)setSelectedAnnotation:(AkylasMapAnnotationProxy*)annotation
{
}

-(void)selectAnnotation:(id)args
{
}

-(void)deselectAnnotation:(id)args
{
}

-(void)selectUserAnnotation
{
}

-(void)zoomTo:(id)args
{
}

-(void)updateCamera:(id)args
{
    NSMutableDictionary* dict = [NSMutableDictionary dictionaryWithDictionary:args];
    [dict removeObjectForKey:@"animate"];
    [self.proxy applyProperties:dict];
}

-(RMSphericalTrapezium) getCurrentRegion
{
    return kMapboxDefaultLatLonBoundingBox;
}


#pragma mark Public APIs


-(id)metersPerPixel_
{
    return @(0);
}

-(id)region_
{
    return [AkylasMapModule dictFromRegion:[self getCurrentRegion]];
}

-(void)setScrollableAreaLimit_:(id)value
{
}

-(void)setRegion_:(id)value
{

}

-(void)setAnimateChanges_:(id)value
{
	animate = [TiUtils boolValue:value];
}

-(void)setRegionFit_:(id)value
{
    regionFits = [TiUtils boolValue:value];
}

-(void)setUserLocationEnabled_:(id)value
{
}

-(id)userLocationEnabled_
{
    return NO;
}

-(id)userLocation_
{
    return nil;
}

-(void)setUserTrackingMode_:(id)value
{
}

-(id)userTrackingMode_
{
    return NUMINTEGER(0);
}

-(void)setZoom_:(id)zoom
{
}

-(void)setMinZoom_:(id)zoom
{
}

-(void)setMaxZoom_:(id)zoom
{
}

- (void)zoomInAt:(CGPoint)pivot animated:(BOOL)animated
{
}

- (void)zoomOutAt:(CGPoint)pivot animated:(BOOL)animated
{
}

-(void)setCenterCoordinate_:(id)center
{
}

-(id)centerCoordinate_
{
    return nil;
}

-(void)addRoute:(id)args atIndex:(NSInteger)index
{
    [self internalAddRoutes:args atIndex:index];
}

-(void)removeRoute:(id)args
{
    [self internalRemoveRoutes:args];
}

-(void)removeAllRoutes
{
    ENSURE_UI_THREAD_0_ARGS;
    [self internalRemoveAllRoutes];
}

-(void)setRoutes_:(id)value
{
    ENSURE_TYPE_OR_NIL(value,NSArray);
    ENSURE_UI_THREAD(setRoutes_,value)
    if (value != nil) {
        [self addRoute:value atIndex:-1];
    }
}

-(void)addTileSource:(id)args atIndex:(NSInteger)index
{
    [self internalAddTileSources:args atIndex:index];
}

-(void)removeTileSource:(id)args
{
    [self internalRemoveTileSources:args];
}

-(void)removeTileSources:(id)args
{
    [self internalRemoveTileSources:args];
}

-(void)removeAllTileSources
{
    ENSURE_UI_THREAD_0_ARGS;
    [self internalRemoveAllTileSources];
}

-(void)setTileSource_:(id)value
{
    ENSURE_TYPE_OR_NIL(value,NSArray);
    ENSURE_UI_THREAD(setTileSource_,value)
    if (value != nil) {
        [self addTileSource:value atIndex:-1];
    }
}


-(void)setTintColor_:(id)color
{
}

-(void)setDefaultPinImage_:(id)image
{
    self.defaultPinImage = [[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self.proxy]];
}
-(void)setDefaultPinAnchor_:(id)anchor
{
    self.defaultPinAnchor = [TiUtils pointValue:anchor];
}
-(void)setDefaultCalloutAnchor_:(id)anchor
{
    self.defaultCalloutAnchor = [TiUtils pointValue:anchor];
}


#pragma mark Templates

- (void)setDefaultCalloutTemplate_:(id)args
{
    ENSURE_TYPE_OR_NIL(args,NSString);
    [_defaultCalloutTemplate release];
    _defaultCalloutTemplate = [args copy];
}

-(void)setCalloutUseTemplates_:(id)value
{
    _calloutUseTemplates = [TiUtils boolValue:value];
}


- (void)setCalloutTemplates_:(id)args
{
    ENSURE_TYPE_OR_NIL(args,NSDictionary);
    NSMutableDictionary *templates = [[NSMutableDictionary alloc] initWithCapacity:[args count]];
    [(NSDictionary *)args enumerateKeysAndObjectsUsingBlock:^(NSString *key, id obj, BOOL *stop) {
        TiProxyTemplate *template = [TiProxyTemplate templateFromViewTemplate:obj];
        if (template != nil) {
            [templates setObject:template forKey:key];
        }
    }];
    
    [_calloutTemplates release];
    _calloutTemplates = [templates copy];
    [templates release];
}

- (NSMutableSet *)reusableViewsWithIdentifier:(NSString *)identifier
{
    if (identifier == nil) {
        return nil;
    }
    
    NSMutableSet *set = [_reusableViews objectForKey:identifier];
    if (set == nil) {
        set = [NSMutableSet set];
        [_reusableViews setObject:set forKey:identifier];
    }
    
    return set;
}

- (void)queueViewForReuse:(id<ReusableViewProtocol>)view
{
    if (view.reuseIdentifier == nil) {
        return;
    }
    
    [[self reusableViewsWithIdentifier:view.reuseIdentifier] addObject:view];
}

- (id<ReusableViewProtocol>)dequeueReusableViewWithIdentifer:(NSString *)identifier
{
    NSParameterAssert(identifier != nil);
    
    NSMutableSet *set = [self reusableViewsWithIdentifier:identifier];
    id<ReusableViewProtocol> view = [set anyObject];
    
    if (view != nil) {
        [view prepareForReuse];
        [set removeObject:view];
        
        return view;
    }
    return nil;
}

-(CalloutReusableView*) reusableViewForProxy:(AkylasMapAnnotationProxy*)proxy objectKey:(NSString*)key {
    id item = [proxy valueForUndefinedKey:key];
    CalloutReusableView* reuseCallout = nil;
    if ([item isKindOfClass:[NSDictionary class]]) {
        id templateId = [item objectForKey:@"template"];
        if (templateId == nil) {
            templateId = _defaultCalloutTemplate;
        }
        
        reuseCallout = (CalloutReusableView*)[self dequeueReusableViewWithIdentifer:templateId];
        
        if (reuseCallout == nil) {
            id<TiEvaluator> context = self.proxy.executionContext;
            if (context == nil) {
                context = self.proxy.pageContext;
            }
            CalloutViewProxy *calloutProxy = [[CalloutViewProxy alloc] initInContext:context reuseIdentifier:templateId];
            reuseCallout = (CalloutReusableView*)[calloutProxy getAndPrepareViewForOpening:[TiUtils appFrame]];
            [calloutProxy dirtyItAll];
            [reuseCallout configurationStart];
            id template = [_calloutTemplates objectForKey:templateId];
            if (template != nil) {
                [calloutProxy unarchiveFromTemplate:template withEvents:YES];
            }
            [reuseCallout configurationSet];
            [calloutProxy release];
        }
        CalloutViewProxy *calloutProxy = ((CalloutViewProxy *)reuseCallout.proxy);
        calloutProxy.annotation = proxy;
        [calloutProxy setParentForBubbling:proxy];
        reuseCallout.dataItem = item;
        [calloutProxy refreshViewIfNeeded:YES];
    }
    return reuseCallout;
}

-(void) reuseIfNecessary:(id)object {
    if ([object isKindOfClass:[CalloutReusableView class]]) {
        CalloutViewProxy *calloutProxy = ((CalloutViewProxy *)((CalloutReusableView*)object).proxy);
        [calloutProxy setParentForBubbling:nil];
        calloutProxy.annotation = nil;
        [self queueViewForReuse:(CalloutReusableView*)object];
    }
}
@end
