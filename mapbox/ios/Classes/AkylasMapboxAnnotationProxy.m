/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapboxAnnotationProxy.h"
#import "TiUtils.h"
#import "TiViewProxy.h"
#import "ImageLoader.h"
#import "TiButtonUtil.h"
#import "AkylasMapboxViewProxy.h"
#import "AkylasMapboxView.h"

@implementation AkylasMapboxAnnotationProxy
{
}

@synthesize delegate;
@synthesize needsRefreshingWithSelection;
@synthesize placed;
@synthesize offset;
@synthesize draggable;
@synthesize annotation = _annotation;

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
    RELEASE_TO_NIL(_annotation);
    RELEASE_TO_NIL(_marker);
    RELEASE_TO_NIL(_image);
    RELEASE_TO_NIL(_tintColor);
	
	[super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Mapbox.Annotation";
}

-(NSMutableDictionary*)langConversionTable
{
    return [NSMutableDictionary dictionaryWithObjectsAndKeys:@"title",@"titleid",@"subtitle",@"subtitleid",nil];
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
			[(AkylasMapboxView*)[delegate view] refreshAnnotation:self readd:needsRefreshingWithSelection];
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
    if (_annotation != nil) {
        _annotation.coordinate = self.coordinate;
        [self setNeedsRefreshingWithSelection:YES];
    }
}

-(void)setLongitude:(id)longitude
{
    double newValue = [TiUtils doubleValue:longitude];
    [self replaceValue:longitude forKey:@"longitude" notification:NO];
    if (_annotation != nil) {
        _annotation.coordinate = self.coordinate;
        [self setNeedsRefreshingWithSelection:YES];
    }
}

// Title and subtitle for use by selection UI.
- (NSString *)title
{
    if (_annotation) {
        return _annotation.title;
    }
	return [self valueForUndefinedKey:@"title"];
}

-(void)setTitle:(id)title
{
	title = [TiUtils replaceString:[TiUtils stringValue:title]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_annotation) {
        _annotation.title = title;
        [self setNeedsRefreshingWithSelection:NO];
    }
    else {
        id current = [self valueForUndefinedKey:@"title"];
        [self replaceValue:title forKey:@"title" notification:NO];
    }
	
}

- (NSString *)subtitle
{
    if (_annotation) {
        return _annotation.subtitle;
    }
	return [self valueForUndefinedKey:@"subtitle"];
}

-(void)setSubtitle:(id)subtitle
{
	subtitle = [TiUtils replaceString:[TiUtils stringValue:subtitle]
			characters:[NSCharacterSet newlineCharacterSet] withString:@" "];
	//The label will strip out these newlines anyways (Technically, replace them with spaces)
    if (_annotation) {
        _annotation.subtitle = subtitle;
        [self setNeedsRefreshingWithSelection:NO];
    }
    else {
        [self replaceValue:subtitle forKey:@"subtitle" notification:NO];
    }
}

-(void)setPincolor:(id)color
{
    RELEASE_TO_NIL(_tintColor);
    _tintColor = [[TiUtils colorValue:color].color retain];
	[self replaceValue:color forKey:@"pincolor" notification:NO];
    if (_marker != nil) [self setNeedsRefreshingWithSelection:YES];
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
        [self setNeedsRefreshingWithSelection:NO];
    }
}

- (void)setRightView:(id)rightview
{
	id current = [self valueForUndefinedKey:@"rightView"];
	[self replaceValue:rightview forKey:@"rightView" notification:NO];
	if (_marker) {
        _marker.rightCalloutAccessoryView = [self rightViewAccessory];
        [self setNeedsRefreshingWithSelection:NO];
    }
}

- (void)setLeftView:(id)leftview
{
	id current = [self valueForUndefinedKey:@"leftView"];
	[self replaceValue:leftview forKey:@"leftView" notification:NO];
    if (_marker) {
        _marker.leftCalloutAccessoryView = [self leftViewAccessory];
        [self setNeedsRefreshingWithSelection:NO];
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

//-(void)setCustomView:(id)customView
//{
//	id current = [self valueForUndefinedKey:@"customView"];
//	[self replaceValue:customView forKey:@"customView" notification:NO];
//	if ([current isEqual: customView] == NO)
//	{
//        [current setProxyObserver:nil];
//        [self forgetProxy:current];
//        [self rememberProxy:customView];
//        [customView setProxyObserver:self];
//        [self setNeedsRefreshingWithSelection:YES];
//	}
//}

-(void)proxyDidRelayout:(id)sender
{
    id current = [self valueForUndefinedKey:@"customView"];
    if ( ([current isEqual:sender] == YES) && (self.placed) ) {
        [self setNeedsRefreshingWithSelection:YES];
    }
}

//- (void)setCenterOffset:(id)centeroffset
//{
//    [self replaceValue:centeroffset forKey:@"centerOffset" notification:NO];
//    CGPoint newVal = [TiUtils pointValue:centeroffset];
//    if (!CGPointEqualToPoint(newVal,offset)) {
//        offset = newVal;
//        [self setNeedsRefreshingWithSelection:YES];
//    }
//}

-(int)tag
{
	return tag;
}

-(RMAnnotation*)getAnnotationForMapView:(RMMapView*)mapView
{
    if (_annotation == nil) {
        _annotation = [[RMAnnotation alloc] initWithMapView:mapView coordinate:self.coordinate andTitle:[self title]];
        _annotation.userInfo = self;
        _annotation.subtitle = [self subtitle];
    }
    else if (_annotation.mapView != mapView) {
        RELEASE_TO_NIL(_annotation)
        return [self getAnnotationForMapView:mapView];
    }
    return _annotation;
}

-(RMAnnotation*)getAnnotation
{
    return _annotation;
}

-(RMMarker*)marker
{
    return _marker;
}

-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView
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

@end
