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

@implementation AkylasMapAnnotationProxy
{
    MKPinAnnotationColor _pinColor;
}

@synthesize delegate;
@synthesize needsRefreshingWithSelection;
@synthesize placed;
@synthesize offset;
@synthesize draggable;
@synthesize rmannotation = _rmannotation;

#define LEFT_BUTTON  1
#define RIGHT_BUTTON 2

#pragma mark Internal

-(void)_configure
{
	static int mapTags = 0;
	tag = mapTags++;
	needsRefreshingWithSelection = NO;
	offset = CGPointZero;
    _anchorPoint = CGPointMake(0.5, 0.5);
    _size = RMMarkerMapboxImageSizeMedium;
    _tintColor = nil;
	[super _configure];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_rmannotation);
    RELEASE_TO_NIL(_marker);
    RELEASE_TO_NIL(_image);
    RELEASE_TO_NIL(_tintColor);
	
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
	return [self valueForUndefinedKey:@"subtitle"];
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

- (id)pinColor
{
	return [self valueForUndefinedKey:@"pincolor"];
}

-(void)setPincolor:(id)color
{
    RELEASE_TO_NIL(_tintColor);
	[self replaceValue:color forKey:@"pincolor" notification:NO];
    TiColor* tiColor= [TiUtils colorValue:color];
    if (tiColor) {
        _tintColor = [tiColor.color retain];
    }
    [self setNeedsRefreshingWithSelection:YES];
}

- (BOOL)animatesDrop
{
	return [TiUtils boolValue:[self valueForUndefinedKey:@"animate"]];
}

- (UIView*)leftViewAccessory
{
	TiViewProxy* viewProxy = [self valueForUndefinedKey:@"leftView"];
	if (viewProxy!=nil && [viewProxy isKindOfClass:[TiViewProxy class]])
	{
		return [viewProxy view];
	}
	else
	{
		id button = [self valueForUndefinedKey:@"leftButton"];
		if (button!=nil)
		{
			return [self makeButton:button tag:LEFT_BUTTON];
		}
	}
	return nil;
}

- (UIView*)rightViewAccessory
{
	TiViewProxy* viewProxy = [self valueForUndefinedKey:@"rightView"];
	if (viewProxy!=nil && [viewProxy isKindOfClass:[TiViewProxy class]])
	{
		return [viewProxy view];
	}
	else
	{
		id button = [self valueForUndefinedKey:@"rightButton"];
		if (button!=nil)
		{
			return [self makeButton:button tag:RIGHT_BUTTON];
		}
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
	id current = [self valueForUndefinedKey:@"rightView"];
	[self replaceValue:rightview forKey:@"rightView" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self rightViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

- (void)setLeftView:(id)leftview
{
	id current = [self valueForUndefinedKey:@"leftView"];
	[self replaceValue:leftview forKey:@"leftView" notification:NO];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self leftViewAccessory];
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setCustomView:(id)customView
{
	id current = [self valueForUndefinedKey:@"customView"];
	[self replaceValue:customView forKey:@"customView" notification:NO];
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
    }
    [_marker replaceUIImage:_image anchorPoint:_anchorPoint];
}

-(void)setImage:(id)image
{
    RELEASE_TO_NIL(_image)
    _image = [[[ImageLoader sharedLoader] loadImmediateImage:[TiUtils toURL:image proxy:self]] retain];
	[self replaceValue:image forKey:@"image" notification:NO];
    if (_marker) {
    }
    [_marker replaceUIImage:_image anchorPoint:_anchorPoint];
}

-(void)setMbImage:(id)image
{
    RELEASE_TO_NIL(_mbImage)
    _mbImage = [[TiUtils stringValue:image] retain];
	[self replaceValue:image forKey:@"mbImage" notification:NO];
    [self setNeedsRefreshingWithSelection:(_marker != nil)];
}

- (void)setCenterOffset:(id)centeroffset
{
    [self replaceValue:centeroffset forKey:@"centerOffset" notification:NO];
    CGPoint newVal = [TiUtils pointValue:centeroffset];
    if (!CGPointEqualToPoint(newVal,offset)) {
        offset = newVal;
        [self setNeedsRefreshingWithSelection:YES];
    }
}

#pragma mark Mapbox


-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView
{
    if (_rmannotation == nil) {
        _rmannotation = [[RMAnnotation alloc] initWithMapView:mapView coordinate:self.coordinate andTitle:[self title]];
        _rmannotation.userInfo = self;
        _rmannotation.subtitle = [self subtitle];
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
            _marker = [[RMMarker alloc] initWithMapboxMarkerImage:_mbImage tintColor:_tintColor size:_size];
        }
        _marker.userInfo = self;
        _marker.canShowCallout = YES;
        _marker.leftCalloutAccessoryView = [self leftViewAccessory];
        _marker.rightCalloutAccessoryView = [self rightViewAccessory];
    }
    return _marker;
}


#pragma mark Native Map


-(void)proxyDidRelayout:(id)sender
{
    id current = [self valueForUndefinedKey:@"customView"];
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
