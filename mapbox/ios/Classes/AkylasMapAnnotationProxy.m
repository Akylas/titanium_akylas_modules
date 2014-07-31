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

@implementation AkylasMapAnnotationProxy
{
    MKPinAnnotationColor _pinColor;
	TiViewProxy* _leftViewProxy;
	TiViewProxy* _rightViewProxy;
	TiViewProxy* _customViewProxy;
    CGPoint _anchorPoint;
    CGPoint _calloutAnchorPoint;
    CGFloat _calloutAlpha;
}

@synthesize delegate;
@synthesize needsRefreshingWithSelection;
@synthesize placed;
@synthesize draggable;
@synthesize rmannotation = _rmannotation;

#define LEFT_BUTTON  1
#define RIGHT_BUTTON 2
#define DETACH_RELEASE_TO_NIL(x) { if (x!=nil) { [x detachView];  [x release]; x = nil; } }

#pragma mark Internal


-(void)_configure
{
	static int mapTags = 0;
	tag = mapTags++;
	needsRefreshingWithSelection = NO;
    _anchorPoint = CGPointMake(0.5, 0.5);
    _calloutAnchorPoint = CGPointMake(0, -0.5f);
    _size = RMMarkerMapboxImageSizeMedium;
    _calloutAlpha = 1;
    _tintColor = nil;
	[super _configure];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_rmannotation);
    RELEASE_TO_NIL(_marker);
    RELEASE_TO_NIL(_image);
    RELEASE_TO_NIL(_tintColor);
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

#pragma mark Public APIs

-(CLLocationCoordinate2D)coordinate
{
	CLLocationCoordinate2D result;
	result.latitude = [TiUtils doubleValue:[self valueForUndefinedKey:@"latitude"]];
	result.longitude = [TiUtils doubleValue:[self valueForUndefinedKey:@"longitude"]];
	return result;
}

-(void)setCoordinate:(CLLocationCoordinate2D)coordinate
{
	[self setValue:NUMDOUBLE(coordinate.latitude) forUndefinedKey:@"latitude"];
	[self setValue:NUMDOUBLE(coordinate.longitude) forUndefinedKey:@"longitude"];
}

-(void)setLatitude:(id)latitude
{
    double newValue = [TiUtils doubleValue:latitude];
    [self replaceValue:latitude forKey:@"latitude" notification:NO];
    if (_rmannotation != nil) {
        _rmannotation.coordinate = self.coordinate;
    }
    [self setNeedsRefreshingWithSelection:YES];
}

-(void)setLongitude:(id)longitude
{
    double newValue = [TiUtils doubleValue:longitude];
    [self replaceValue:longitude forKey:@"longitude" notification:NO];
    if (_rmannotation != nil) {
        _rmannotation.coordinate = self.coordinate;
    }
    [self setNeedsRefreshingWithSelection:YES];
}

- (NSString *)title
{
	return [self valueForUndefinedKey:@"title"];
}

-(void)setTitle:(id)title
{
	title = [TiUtils replaceString:[TiUtils stringValue:title]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_rmannotation) {
        _rmannotation.title = title;
    }
    [self replaceValue:title forKey:@"title" notification:NO];
    [self setNeedsRefreshingWithSelection:NO];
}

- (NSString *)subtitle
{
	return [self valueForUndefinedKey:@"subtitle"];
}

-(void)setSubtitle:(id)subtitle
{
	subtitle = [TiUtils replaceString:[TiUtils stringValue:subtitle]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_rmannotation) {
        _rmannotation.subtitle = subtitle;
    }
    [self replaceValue:subtitle forKey:@"subtitle" notification:NO];
    [self setNeedsRefreshingWithSelection:NO];
}

- (int)mapPincolor
{
	return _pinColor;
}

- (id)pincolor
{
	return [self valueForUndefinedKey:@"pincolor"];
}

-(void)setPincolor:(id)color
{
    RELEASE_TO_NIL(_tintColor);
	[self replaceValue:color forKey:@"pincolor" notification:NO];
    if ([color isKindOfClass:[NSNumber class]]) {
        _pinColor = [TiUtils intValue:color];
    }
    else {
        TiColor* tiColor= [TiUtils colorValue:color];
        if (tiColor) {
            _tintColor = [tiColor.color retain];
        }
    }
    [self setNeedsRefreshingWithSelection:YES];
}

- (BOOL)animatesDrop
{
	return [TiUtils boolValue:[self valueForUndefinedKey:@"animate"]];
}

-(UIColor*)calloutBackgroundColor {
    if ([self valueForUndefinedKey:@"calloutBackgroundColor"])
        return [[TiUtils colorValue:[self valueForUndefinedKey:@"calloutBackgroundColor"]] color];
    return [UIColor whiteColor];
}

-(CGFloat)calloutBorderRadius {
    return [TiUtils floatValue:[self valueForUndefinedKey:@"animate"] def:DEFAULT_CALLOUT_CORNER_RADIUS];
}

-(UIEdgeInsets)calloutPadding {
    if ([self valueForUndefinedKey:@"calloutPadding"])
        return [TiUtils insetValue:[self valueForUndefinedKey:@"calloutPadding"]];
    return DEFAULT_CALLOUT_PADDING;
}

- (UIView*)leftViewAccessory
{
	if (_leftViewProxy == nil) {
        id value = [self valueForUndefinedKey:@"leftView"];
        if ([value isKindOfClass:[TiViewProxy class]])
        {
            _leftViewProxy = (TiViewProxy*)value;
        } else if ([value isKindOfClass:[NSDictionary class]]) {
            id<TiEvaluator> context = self.executionContext;
            if (context == nil) {
                context = self.pageContext;
            }
            _leftViewProxy = (TiViewProxy*)[[TiViewProxy class] createFromDictionary:value rootProxy:self inContext:context];
        }
        [self rememberProxy:_leftViewProxy];
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

- (UIView*)rightViewAccessory
{
    if (_rightViewProxy == nil) {
        id value = [self valueForUndefinedKey:@"rightView"];
        if ([value isKindOfClass:[TiViewProxy class]])
        {
            _rightViewProxy = (TiViewProxy*)value;
        } else if ([value isKindOfClass:[NSDictionary class]]) {
            id<TiEvaluator> context = self.executionContext;
            if (context == nil) {
                context = self.pageContext;
            }
            _rightViewProxy = (TiViewProxy*)[[TiViewProxy class] createFromDictionary:value rootProxy:self inContext:context];
        }
        [self rememberProxy:_rightViewProxy];
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

- (UIView*)customViewAccessory
{
    if (_customViewProxy == nil) {
        id value = [self valueForUndefinedKey:@"customView"];
        if ([value isKindOfClass:[TiViewProxy class]])
        {
            _customViewProxy = (TiViewProxy*)value;
        } else if ([value isKindOfClass:[NSDictionary class]]) {
            id<TiEvaluator> context = self.executionContext;
            if (context == nil) {
                context = self.pageContext;
            }
            _customViewProxy = (TiViewProxy*)[[TiViewProxy class] createFromDictionary:value rootProxy:self inContext:context];
        }
        [self rememberProxy:_customViewProxy];
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
        _marker.leftCalloutAccessoryView = [self leftViewAccessory];
        [self setNeedsRefreshingWithSelection:NO];
    }
}

- (void)setRightButton:(id)button
{
	id current = [self valueForUndefinedKey:@"rightButton"];
	[self replaceValue:button forKey:@"rightButton" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self rightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setRightView:(id)rightview
{
    DETACH_RELEASE_TO_NIL(_rightViewProxy)
	[self replaceValue:rightview forKey:@"rightView" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self rightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setLeftView:(id)leftview
{
    DETACH_RELEASE_TO_NIL(_leftViewProxy)
	[self replaceValue:leftview forKey:@"leftView" notification:NO];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self leftViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setCustomView:(id)leftview
{
    DETACH_RELEASE_TO_NIL(_customViewProxy)
	[self replaceValue:leftview forKey:@"customView" notification:NO];
    if (_marker) {
        _marker.customCalloutAccessoryView = [self customViewAccessory];
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
    _anchorPoint = [TiUtils pointValue:value def:_anchorPoint];
	[self replaceValue:value forKey:@"anchorPoint" notification:NO];
    if (_marker) {
        [_marker replaceUIImage:_image anchorPoint:_anchorPoint];
    }
}

-(CGPoint)anchorPoint
{
    return _anchorPoint;
}

-(void)setCalloutAnchorPoint:(id)value
{
    _calloutAnchorPoint = [TiUtils pointValue:value def:_calloutAnchorPoint];
	[self replaceValue:value forKey:@"calloutAnchorPoint" notification:NO];
}

-(void)setCalloutAlpha:(id)value
{
    _calloutAlpha = [TiUtils floatValue:value def:1.0f];
	[self replaceValue:value forKey:@"calloutAlpha" notification:NO];
}

-(CGPoint)calloutAnchorPoint
{
    return _calloutAnchorPoint;
}

-(CGFloat)calloutAlpha
{
    return _calloutAlpha;
}

-(void)setImage:(id)image
{
    RELEASE_TO_NIL(_image)
    _image = [[[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self]] retain];
	[self replaceValue:image forKey:@"image" notification:NO];
    if (_marker) {
        [_marker replaceUIImage:_image anchorPoint:_anchorPoint];
    }
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
//        _rmannotation.subtitle = [self subtitle];
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
        if (_image) {
            _marker = [[RMMarker alloc] initWithUIImage:_image anchorPoint:_anchorPoint];
        }
        else if (mapView.defaultPinImage) {
            _marker = [[RMMarker alloc] initWithUIImage:mapView.defaultPinImage anchorPoint:mapView.defaultPinAnchor];
        }
        else {
            
            _marker = [[RMMarker alloc] initWithUIImage:[self getMapBoxImage:_mbImage color:_tintColor size:_size] anchorPoint:mapView.defaultPinAnchor];
        }
        if ([self valueForUndefinedKey:@"anchorPoint"]) {
            _marker.anchorPoint = _anchorPoint;
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
		int type = [TiUtils intValue:button];
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

@end
