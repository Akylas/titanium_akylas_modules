//
//  AkylasGooglemapAnnotationProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapAnnotationProxy.h"

@implementation AkylasGooglemapAnnotationProxy
{
    GMSMarker* _gmarker;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_gmarker);
    [super dealloc];
}


-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Annotation";
}

-(void)refreshCoords {
    if (_gmarker) {
        [CATransaction begin];
        _gmarker.position = self.coordinate;
        [CATransaction commit];
    }
    [super refreshCoords];
}

-(void)setTitle:(id)value
{
    [super setTitle:value];
    if (_gmarker) {
        _gmarker.title = self.title;
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSubtitle:(id)value
{
    [super setSubtitle:value];
    
    if (_gmarker) {
        _gmarker.snippet = self.subtitle;
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setFlat:(BOOL)flat
{
    [super setFlat:flat];
    if (_gmarker) {
        _gmarker.flat = self.flat;
    }
}

-(void)setShowInfoWindow:(BOOL)showInfoWindow
{
    [super setShowInfoWindow:showInfoWindow];
    if (_gmarker) {
        _gmarker.tappable = self.showInfoWindow;
    }
}


-(void)setHeading:(CGFloat)heading
{
    [super setHeading:heading];
    if (_gmarker) {
        _gmarker.rotation = self.heading;
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    if (_gmarker) {
        _gmarker.opacity = self.opacity;
    }
}

-(void)setDraggable:(BOOL)draggable
{
    [super setDraggable:draggable];
    if (_gmarker) {
        _gmarker.draggable = self.draggable;
    }
}

-(void)setColor:(id)color
{
    [super setColor:color];
//    [self replaceValue:color forKey:@"color" notification:NO];
//    if ([color isKindOfClass:[NSNumber class]]) {
//        self.pinColor = [TiUtils intValue:color];
//        self.tintColor = nil;
//    }
//    else {
//        self.tintColor = [[TiUtils colorValue:color] _color];
        if (_gmarker) {
            _gmarker.icon = [GMSMarker markerImageWithColor:self.tintColor];
        }
//    }
    [self setNeedsRefreshingWithSelection:YES];
}

-(void)setAnchorPoint:(id)value
{
    [super setAnchorPoint:value];
    if (_gmarker) {
        _gmarker.groundAnchor = [self nGetAnchorPoint];
    }
}

-(void)setCalloutAnchorPoint:(id)value
{
    [super setCalloutAnchorPoint:value];
    if (_gmarker) {
        _gmarker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
    }
}

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.icon = _internalImage;
        }, NO);
    }
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
        _gmarker.flat = self.flat;
        _gmarker.userData = self;
        if (_internalImage) {
            _gmarker.icon = _internalImage;
        } else if (self.tintColor){
            if (_gmarker) {
                _gmarker.icon = [GMSMarker markerImageWithColor:self.tintColor];
            }
        }
        _gmarker.draggable = self.draggable;
        _gmarker.tappable = self.showInfoWindow;
        _gmarker.opacity = self.opacity;
        _gmarker.rotation = self.heading;
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


@end
