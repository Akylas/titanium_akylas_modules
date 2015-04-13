/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapAnnotationProxy.h"
#import "TiUtils.h"
#import "TiViewProxy.h"
#import "ImageLoader.h"
#import "TiButtonUtil.h"
#import "AkylasMapViewProxy.h"
#import "AkylasMapView.h"
#import "AkylasMapMapboxView.h"
#import "ImageLoader.h"

#import <GoogleMaps/GoogleMaps.h>

@implementation AkylasMapAnnotationProxy
{
	TiViewProxy* _leftViewProxy;
	TiViewProxy* _rightViewProxy;
	TiViewProxy* _customViewProxy;
    CGPoint _mAnchorPoint;
    CGPoint _calloutAnchorPoint;
    CGFloat _calloutAlpha;
    id _sortKey;
    BOOL configurationSet;
    BOOL needsLocationRefresh;
    ImageLoaderRequest *urlRequest;
    
    BOOL _flat;
    BOOL _showInfoWindow;
    CGFloat _heading;
    CGFloat _opacity;
    
    
    //MKMapView
    MKAnnotationView* _annView;
    
    //GoogleMap
    GMSMarker* _gmarker;
}

@synthesize delegate;
@synthesize needsRefreshingWithSelection;
@synthesize placed, showInfoWindow = _showInfoWindow, draggable = _draggable, flat = _flat, title, subtitle, pinColor,tintColor;
@synthesize heading = _heading, opacity = _opacity;
@synthesize rmannotation = _rmannotation;
@synthesize annView = _annView;

#define LEFT_BUTTON  1
#define RIGHT_BUTTON 2
#define DETACH_RELEASE_TO_NIL(x) { if (x!=nil) { [x detachView];  [x release]; x = nil; } }

#pragma mark Internal


-(void)_configure
{
	static int mapTags = 0;
	tag = mapTags++;
	needsRefreshingWithSelection = NO;
    _mAnchorPoint = CGPointMake(0.5, 0.5);
    _calloutAnchorPoint = CGPointMake(0, -0.5f);
    _size = RMMarkerMapboxImageSizeMedium;
    _calloutAlpha = 1;
    _sortKey = nil;
    _minZoom = 0.0f;
    _maxZoom = 22.0f;
    configurationSet  = NO;
    needsLocationRefresh = NO;
    
    pinColor = MKPinAnnotationColorRed;
    _flat = NO;
    _showInfoWindow = YES;
    _draggable = NO;
    placed = NO;
    _opacity = 1.0f;
    _heading = 0.0f;
    _zIndex = -1;
	[super _configure];
}


-(TiProxy *)parentForBubbling
{
	return delegate;
}


-(void)dealloc
{
    RELEASE_TO_NIL(_rmannotation);
    RELEASE_TO_NIL(_marker);
    RELEASE_TO_NIL(_gmarker);
    RELEASE_TO_NIL(_internalImage);
    RELEASE_TO_NIL(_sortKey);
    RELEASE_TO_NIL(title);
    RELEASE_TO_NIL(subtitle);
    RELEASE_TO_NIL(tintColor);
    DETACH_RELEASE_TO_NIL(_leftViewProxy)
    DETACH_RELEASE_TO_NIL(_rightViewProxy)
    DETACH_RELEASE_TO_NIL(_customViewProxy)
	
	[super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Map.Annotation";
}

-(NSMutableDictionary*)langConversionTable
{
    return [NSMutableDictionary dictionaryWithObjectsAndKeys:@"title",@"titleid",@"subtitle",@"subtitleid",nil];
}

-(void)refreshAfterDelay
{
	[self performSelector:@selector(refreshIfNeeded) withObject:nil afterDelay:0.1];
}

-(void)setNeedsRefreshingWithSelection: (BOOL)shouldReselect
{
	if (delegate == nil)
	{
		return; //Nobody to refresh!
	}
	@synchronized(self)
	{
		BOOL invokeMethod = !needsRefreshing;
		needsRefreshing = YES;
		needsRefreshingWithSelection |= shouldReselect;

		if (invokeMethod)
		{
			TiThreadPerformOnMainThread(^{[self refreshAfterDelay];}, NO);
		}
	}
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

-(void)applyProperties:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args)
    configurationSet = NO;
    [super applyProperties:args];
    configurationSet = YES;
    if (needsLocationRefresh) {
        [self refreshCoords];
    }

}

#pragma mark Public APIs

-(void)refreshCoords {
    if (_gmarker) {
        [CATransaction begin];
        _gmarker.position = self.coordinate;
        [CATransaction commit];
//        return;
    }
    if (_rmannotation != nil) {
        _rmannotation.coordinate = self.coordinate;
    }

//    self.coordinate = [self coordinate];
//    if (_annView) {
//        _annView
//        _annView setCenter:]
//    }
    [self setNeedsRefreshingWithSelection:YES];
}

-(CLLocationCoordinate2D)coordinate
{
	CLLocationCoordinate2D result;
	result.latitude = [TiUtils doubleValue:[self valueForUndefinedKey:@"latitude"]];
	result.longitude = [TiUtils doubleValue:[self valueForUndefinedKey:@"longitude"]];
	return result;
}

-(void)setCoordinate:(CLLocationCoordinate2D)coordinate
{
    [self willChangeValueForKey:@"coordinate"];
	[self setValue:NUMDOUBLE(coordinate.latitude) forUndefinedKey:@"latitude"];
	[self setValue:NUMDOUBLE(coordinate.longitude) forUndefinedKey:@"longitude"];
    [self didChangeValueForKey:@"coordinate"];
}

-(void)setLatitude:(id)latitude
{
    double newValue = [TiUtils doubleValue:latitude];
    [self replaceValue:latitude forKey:@"latitude" notification:NO];
    if (!configurationSet) {
        needsLocationRefresh = YES;
        return;
    }
    [self refreshCoords];
}



-(void)setLongitude:(id)longitude
{
    double newValue = [TiUtils doubleValue:longitude];
    [self replaceValue:longitude forKey:@"longitude" notification:NO];
    if (!configurationSet) {
        needsLocationRefresh = YES;
        return;
    }
    [self refreshCoords];
}


-(void)setTitle:(id)value
{
    RELEASE_TO_NIL(title)
	title = [[TiUtils replaceString:[TiUtils stringValue:value]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "] copy];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_rmannotation) {
        _rmannotation.title = title;
    }
    if (_gmarker) {
        _gmarker.title = title;
    }
    [self replaceValue:title forKey:@"title" notification:NO];
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSubtitle:(id)value
{
    RELEASE_TO_NIL(subtitle)
	subtitle = [[TiUtils replaceString:[TiUtils stringValue:value]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "] copy];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_rmannotation) {
        _rmannotation.subtitle = subtitle;
    }
    
    if (_gmarker) {
        _gmarker.snippet = subtitle;
    }
    [self replaceValue:subtitle forKey:@"subtitle" notification:NO];
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setFlat:(BOOL)flat
{
    _flat = flat;
    if (_gmarker) {
        _gmarker.flat = _flat;
    }
}

-(void)setShowInfoWindow:(BOOL)showInfoWindow
{
    _showInfoWindow = showInfoWindow;
    if (_gmarker) {
        _gmarker.tappable = _showInfoWindow;
    }
}


-(void)setHeading:(CGFloat)heading
{
    _heading = heading;
    if (_gmarker) {
        _gmarker.rotation = _heading;
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    _opacity = opacity;
    if (_gmarker) {
        _gmarker.opacity = _opacity;
    }
}

-(void)setDraggable:(BOOL)draggable
{
    _draggable = draggable;
    if (_gmarker) {
        _gmarker.draggable = _draggable;
    }
    
    if (_annView) {
        _annView.draggable = _draggable;
    }
}

-(void)setColor:(id)color
{
	[self replaceValue:color forKey:@"color" notification:NO];
    if ([color isKindOfClass:[NSNumber class]]) {
        self.pinColor = [TiUtils intValue:color];
        self.tintColor = nil;
    }
    else {
        self.tintColor = [[TiUtils colorValue:color] _color];
        if (_gmarker) {
            _gmarker.icon = [GMSMarker markerImageWithColor:self.tintColor];
        }
    }
    [self setNeedsRefreshingWithSelection:YES];
}

- (id)sortKey
{
	return _sortKey;
}

-(void)setSortKey:(id)sortKey
{
	RELEASE_TO_NIL(_sortKey);
	[self replaceValue:sortKey forKey:@"sortKey" notification:NO];
    _sortKey = [sortKey retain];
    if (_rmannotation) {
        _rmannotation.sortKey = _sortKey;
    }
}

-(UIColor*)nGetCalloutBackgroundColor {
    if ([self valueForUndefinedKey:@"calloutBackgroundColor"])
        return [[TiUtils colorValue:[self valueForUndefinedKey:@"calloutBackgroundColor"]] color];
    return [UIColor whiteColor];
}

-(CGFloat)nGetCalloutBorderRadius {
    return [TiUtils floatValue:[self valueForUndefinedKey:@"calloutBorderRadius"] def:DEFAULT_CALLOUT_CORNER_RADIUS];
}

-(CGFloat)nGetCalloutArrowHeight {
    return [TiUtils floatValue:[self valueForUndefinedKey:@"calloutArrowHeight"] def:DEFAULT_CALLOUT_CORNER_RADIUS];
}

-(UIEdgeInsets)nGetCalloutPadding {
    if ([self valueForUndefinedKey:@"calloutPadding"])
        return [TiUtils insetValue:[self valueForUndefinedKey:@"calloutPadding"]];
    return DEFAULT_CALLOUT_PADDING;
}

- (UIView*)nGetLeftViewAccessory
{
	if (_leftViewProxy == nil) {
        _leftViewProxy = (TiViewProxy*)[[self createChildFromObject:[self valueForUndefinedKey:@"leftView"]] retain];
    }
    if (_leftViewProxy) {
        [_leftViewProxy setCanBeResizedByFrame:YES];
		return [_leftViewProxy getAndPrepareViewForOpening:[TiUtils appFrame]];
    }
    id button = [self valueForUndefinedKey:@"leftButton"];
    if (button!=nil)
    {
        return [self makeButton:button tag:LEFT_BUTTON];
    }
    return nil;
}

- (UIView*)nGetRightViewAccessory
{
    if (_rightViewProxy == nil) {
        id value = [self valueForUndefinedKey:@"rightView"];
        _rightViewProxy = (TiViewProxy*)[[self createChildFromObject:[self valueForUndefinedKey:@"rightView"]] retain];
    }
    if (_rightViewProxy) {
        [_rightViewProxy setCanBeResizedByFrame:YES];
		return [_rightViewProxy getAndPrepareViewForOpening:[TiUtils appFrame]];
    }
    id button = [self valueForUndefinedKey:@"rightButton"];
    if (button!=nil)
    {
        return [self makeButton:button tag:RIGHT_BUTTON];
    }
	return nil;
}

- (UIView*)nGetCustomViewAccessory
{
    if (_customViewProxy == nil) {
        id value = [self valueForUndefinedKey:@"customView"];
        _customViewProxy = (TiViewProxy*)[[self createChildFromObject:[self valueForUndefinedKey:@"customView"]] retain];
    }
    if (_customViewProxy) {
        [_customViewProxy setCanBeResizedByFrame:YES];
		return [_customViewProxy getAndPrepareViewForOpening:[TiUtils appFrame]];
    }
	return nil;
}

- (void)setLeftButton:(id)button
{
	id current = [self valueForUndefinedKey:@"leftButton"];
	[self replaceValue:button forKey:@"leftButton" notification:NO];
	if (_marker) {
        _marker.leftCalloutAccessoryView = [self nGetLeftViewAccessory];
        [self setNeedsRefreshingWithSelection:NO];
    }
}

- (void)setRightButton:(id)button
{
	id current = [self valueForUndefinedKey:@"rightButton"];
	[self replaceValue:button forKey:@"rightButton" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self nGetRightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setRightView:(id)rightview
{
    DETACH_RELEASE_TO_NIL(_rightViewProxy)
	[self replaceValue:rightview forKey:@"rightView" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self nGetRightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setLeftView:(id)leftview
{
    DETACH_RELEASE_TO_NIL(_leftViewProxy)
	[self replaceValue:leftview forKey:@"leftView" notification:NO];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self nGetLeftViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setCustomView:(id)leftview
{
    DETACH_RELEASE_TO_NIL(_customViewProxy)
	[self replaceValue:leftview forKey:@"customView" notification:NO];
    if (_marker) {
        _marker.customCalloutAccessoryView = [self nGetCustomViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}


-(void)setPinView:(id)customView
{
	id current = [self valueForUndefinedKey:@"pinView"];
	[self replaceValue:customView forKey:@"pinView" notification:NO];
	if ([current isEqual: customView] == NO)
	{
        [current setProxyObserver:nil];
        [self forgetProxy:current];
        [self rememberProxy:customView];
        [customView setProxyObserver:self];
        [self setNeedsRefreshingWithSelection:YES];
	}
}

-(void)setAnchorPoint:(id)value
{
    _mAnchorPoint = [TiUtils pointValue:value def:_mAnchorPoint];
	[self replaceValue:value forKey:@"anchorPoint" notification:NO];
    if (_marker) {
        [_marker replaceUIImage:_internalImage anchorPoint:_mAnchorPoint];
    }
    if (_gmarker) {
        _gmarker.groundAnchor = _mAnchorPoint;
    }
}

-(CGPoint)nGetAnchorPoint
{
    return _mAnchorPoint;
}

-(CGSize)getSize {
    if (_internalImage) {
        return _internalImage.size;
    }
    if (_gmarker) {
        return CGSizeMake(20, 40);
    }
    return CGSizeZero;
}

-(void)setCalloutAnchorPoint:(id)value
{
    _calloutAnchorPoint = [TiUtils pointValue:value def:_calloutAnchorPoint];
    if (_gmarker) {
        _gmarker.infoWindowAnchor = _calloutAnchorPoint;
    }
	[self replaceValue:value forKey:@"calloutAnchorPoint" notification:NO];
}

-(void)setCalloutAlpha:(id)value
{
    _calloutAlpha = [TiUtils floatValue:value def:1.0f];
	[self replaceValue:value forKey:@"calloutAlpha" notification:NO];
}

-(CGPoint)nGetCalloutAnchorPoint
{
    return _calloutAnchorPoint;
}

-(CGFloat)nGetCalloutAlpha
{
    return _calloutAlpha;
}

-(UIImage*)nGetInternalImage
{
    return _internalImage;
}

-(BOOL)nHasInternalImage
{
    return [self valueForKey:@"image"] != nil;
}

-(void)startImageLoad:(NSURL *)url;
{
    [self cancelPendingImageLoads]; //Just in case we have a crusty old urlRequest.
    NSDictionary* info = nil;
    NSNumber* hires = [self valueForKey:@"hires"];
    if (hires) {
        info = [NSDictionary dictionaryWithObject:hires forKey:@"hires"];
    }
    urlRequest = [[[ImageLoader sharedLoader] loadImage:url delegate:self options:[self valueForUndefinedKey:@"httpOptions"] userInfo:info] retain];
}

-(void)setInternalImage:(UIImage*)image {
    RELEASE_TO_NIL(_internalImage)
    _internalImage = [image retain];
    if (_marker) {
        [_marker replaceUIImage:_internalImage anchorPoint:_mAnchorPoint];
    }
    if (_gmarker) {
        _gmarker.icon = _internalImage;
    }
    if (_annView) {
        _annView.image = _internalImage;
    }
}

-(void)cancelPendingImageLoads
{
    // cancel a pending request if we have one pending
    if (urlRequest!=nil)
    {
        [urlRequest cancel];
        RELEASE_TO_NIL(urlRequest);
    }
}

-(void)imageLoadSuccess:(ImageLoaderRequest*)request image:(id)image
{
    if (request != urlRequest || !image)
    {
        return;
    }
    [self setInternalImage:image];
//    [self setNeedsRefreshingWithSelection:YES];
    RELEASE_TO_NIL(urlRequest);
}

-(void)imageLoadFailed:(ImageLoaderRequest*)request error:(NSError*)error
{
    if (request == urlRequest)
    {
        RELEASE_TO_NIL(urlRequest);
    }
}

-(void)imageLoadCancelled:(ImageLoaderRequest *)request
{
}

-(void)setImage:(id)image
{
    NSURL* url = [TiUtils toURL:image proxy:self];
    [self setInternalImage: [[ImageLoader sharedLoader] loadImmediateImage:url]];
    if (_internalImage==nil)
    {
        [self startImageLoad:url];
    }
	[self replaceValue:image forKey:@"image" notification:NO];

}

-(void)setMbImage:(id)image
{
    RELEASE_TO_NIL(_mbImage)
    _mbImage = [[TiUtils stringValue:image] retain];
	[self replaceValue:image forKey:@"mbImage" notification:NO];
    [self setNeedsRefreshingWithSelection:(_marker != nil)];
}

#pragma mark Mapbox


-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView
{
    if (_rmannotation == nil) {
        _rmannotation = [[RMAnnotation alloc] initWithMapView:mapView coordinate:self.coordinate andTitle:nil];
        _rmannotation.userInfo = self;
        _rmannotation.sortKey = _sortKey;
        _rmannotation.minZoom = self.minZoom;
        _rmannotation.maxZoom = self.maxZoom;
    }
    else if (_rmannotation.mapView != mapView) {
        RELEASE_TO_NIL(_rmannotation)
        return [self getRMAnnotationForMapView:mapView];
    }
    return _rmannotation;
}

-(RMAnnotation*)getRMAnnotation
{
    return _rmannotation;
}

-(RMMarker*)marker
{
    return _marker;
}

-(UIImage*)coloredImage:(UIImage*)image withColor:(UIColor*)color
{
    if (image == nil || color == nil) return image;
    // begin a new image context, to draw our colored image onto
    UIGraphicsBeginImageContext(image.size);
    
    // get a reference to that context we created
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    // set the fill color
    [color setFill];
    
    // translate/flip the graphics context (for transforming from CG* coords to UI* coords
    CGContextTranslateCTM(context, 0, image.size.height);
    CGContextScaleCTM(context, 1.0, -1.0);
    
    // set the blend mode to color burn, and the original image
    CGContextSetBlendMode(context, kCGBlendModeColorBurn);
    CGRect rect = CGRectMake(0, 0, image.size.width, image.size.height);
    CGContextDrawImage(context, rect, image.CGImage);
    
    // set a mask that matches the shape of the image, then draw (color burn) a colored rectangle
    CGContextClipToMask(context, rect, image.CGImage);
    CGContextAddRect(context, rect);
    CGContextDrawPath(context,kCGPathFill);
    
    // generate a new UIImage from the graphics context we drew onto
    UIImage *coloredImg = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    //return the color-burned image
    return coloredImg;
}

-(UIImage*)getMapBoxImage:(NSString *)symbolName color:(UIColor*)color size:(RMMarkerMapboxImageSize)size
{
    NSString *sizeString = nil;
    
    switch (size)
    {
        case RMMarkerMapboxImageSizeSmall:
            sizeString = @"small";
            break;
            
        case RMMarkerMapboxImageSizeMedium:
        default:
            sizeString = @"medium";
            break;
            
        case RMMarkerMapboxImageSizeLarge:
            sizeString = @"large";
            break;
    }
    
    BOOL useRetina = ([[UIScreen mainScreen] scale] > 1.0);
    
    NSString *colorHex = nil;

    if (color)
    {
        CGFloat white, red, green, blue, alpha;

        if ([color getRed:&red green:&green blue:&blue alpha:&alpha])
        {
            colorHex = [NSString stringWithFormat:@"%02lx%02lx%02lx", (unsigned long)(red * 255), (unsigned long)(green * 255), (unsigned long)(blue * 255)];
        }
        else if ([color getWhite:&white alpha:&alpha])
        {
            colorHex = [NSString stringWithFormat:@"%02lx%02lx%02lx", (unsigned long)(white * 255), (unsigned long)(white * 255), (unsigned long)(white * 255)];
        }
    }
    NSURL *imageURLForCache = [NSURL URLWithString:[NSString stringWithFormat:@"http://api.tiles.mapbox.com/v3/marker/pin-%@%@%@%@.png",
                                            (sizeString ? [sizeString substringToIndex:1] : @"m"),
                                            (symbolName ? [@"-" stringByAppendingString:symbolName] : @""),
                                            (colorHex?colorHex:@"+000000"),
                                            (useRetina  ? @"@2x" : @"")]];
     NSURL *imageURLForDownload = [NSURL URLWithString:[NSString stringWithFormat:@"http://api.tiles.mapbox.com/v3/marker/pin-%@%@%@%@.png",
                                                     (sizeString ? [sizeString substringToIndex:1] : @"m"),
                                                     (symbolName ? [@"-" stringByAppendingString:symbolName] : @""),
                                                     (colorHex?@"+cccccc":@"+000000"),
                                                     (useRetina  ? @"@2x" : @"")]];
    UIImage* image = [[ImageLoader sharedLoader] loadImmediateImage:imageURLForCache];
    if (image == nil)  {
        image = [[ImageLoader sharedLoader] loadImmediateImage:imageURLForDownload];
        if (image == nil) {
            NSData* data = [NSData brandedDataWithContentsOfURL:imageURLForDownload];
            image = [UIImage imageWithData:data scale:(useRetina ? 2.0 : 1.0)];
            [[ImageLoader sharedLoader] cache:image forURL:imageURLForDownload];
        }
        if (color) {
            image = [self coloredImage:image withColor:color];
        }
        [[ImageLoader sharedLoader] cache:image forURL:imageURLForCache];
    }
    
    return image;
}

-(RMMapLayer*)shapeLayerForMapView:(AkylasMapMapboxView*)mapView
{
    if (_marker == nil) {
        if (_internalImage) {
            _marker = [[RMMarker alloc] initWithUIImage:_internalImage anchorPoint:_mAnchorPoint];
        }
        else if (mapView.defaultPinImage) {
            _marker = [[RMMarker alloc] initWithUIImage:mapView.defaultPinImage anchorPoint:mapView.defaultPinAnchor];
        }
        else {
            
            _marker = [[RMMarker alloc] initWithUIImage:[self getMapBoxImage:_mbImage color:tintColor size:_size] anchorPoint:mapView.defaultPinAnchor];
        }
        if ([self valueForUndefinedKey:@"anchorPoint"]) {
            _marker.anchorPoint = _mAnchorPoint;
        }
        else {
            _marker.anchorPoint = mapView.defaultPinAnchor;
        }
        
        if (![self valueForUndefinedKey:@"calloutAnchorPoint"]) {
            _calloutAnchorPoint = mapView.defaultCalloutAnchor;
        }
        _marker.canShowCallout = [TiUtils boolValue:[self valueForUndefinedKey:@"canShowCallout"] def:YES];
    }
    return _marker;
}


#pragma mark GoogleMap
+(int)gZIndexDelta {
    return 1000;
}


-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView
{
    if (_gmarker == nil) {
        
        _gmarker = [GMSMarker markerWithPosition:self.coordinate];
        _gmarker.appearAnimation = kGMSMarkerAnimationPop;
        _gmarker.map = mapView;
        _gmarker.title = [self title];
        _gmarker.snippet = [self subtitle];
        _gmarker.flat = _flat;
        _gmarker.userData = self;
        if (_internalImage) {
            _gmarker.icon = _internalImage;
        } else if (tintColor){
            if (_gmarker) {
                _gmarker.icon = [GMSMarker markerImageWithColor:tintColor];
            }
        }
        _gmarker.draggable = _draggable;
        _gmarker.tappable = _showInfoWindow;
        _gmarker.opacity = _opacity;
        _gmarker.rotation = _heading;
        _gmarker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
        _gmarker.groundAnchor = [self nGetAnchorPoint];
    }
    else if (_gmarker.map != mapView) {
        RELEASE_TO_NIL(_gmarker)
        return [self getGOverlayForMapView:mapView];
    }
    return _gmarker;
}


-(GMSOverlay*)gOverlay
{
    return _gmarker;
}

#pragma mark Native Map


-(void)proxyDidRelayout:(id)sender
{
    id current = [self valueForUndefinedKey:@"pinView"];
    if ( ([current isEqual:sender] == YES) && (self.placed) ) {
        [self setNeedsRefreshingWithSelection:YES];
    }
}

-(int)tag
{
	return tag;
}

-(UIView*)makeButton:(id)button tag:(int)buttonTag
{
	UIView *button_view = nil;
	if ([button isKindOfClass:[NSNumber class]])
	{
		// this is button type constant
		NSInteger type = [TiUtils intValue:button];
		button_view = [TiButtonUtil buttonWithType:type];
	}
	else
	{
		UIImage *image = [[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:button proxy:self]];
		if (image!=nil)
		{
			CGSize size = [image size];
			UIButton *bview = [UIButton buttonWithType:UIButtonTypeCustom];
			[TiUtils setView:bview positionRect:CGRectMake(0,0,size.width,size.height)];
			bview.backgroundColor = [UIColor clearColor];
			[bview setImage:image forState:UIControlStateNormal];
			button_view = bview;
		}
	}
	if (button_view!=nil)
	{
		button_view.tag = buttonTag;
	}
	return button_view;
}

-(void)setMinZoom:(CGFloat)value
{
    _minZoom = value;
    [self replaceValue:@(value) forKey:@"minZoom" notification:NO];
    if (_rmannotation) {
        _rmannotation.minZoom = _minZoom;
    }
}

-(void)setMaxZoom:(CGFloat)value
{
    _maxZoom = value;
    [self replaceValue:@(value) forKey:@"maxZoom" notification:NO];
    if (_rmannotation) {
        _rmannotation.maxZoom = _maxZoom;
    }
}

@end