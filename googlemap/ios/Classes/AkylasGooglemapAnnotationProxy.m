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
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.title = self.title;
        }, NO);
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSubtitle:(id)value
{
    [super setSubtitle:value];
    
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.snippet = self.subtitle;
        }, NO);
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setFlat:(BOOL)flat
{
    [super setFlat:flat];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.flat = self.flat;
        }, NO);
    }
}

-(void)setShowInfoWindow:(BOOL)showInfoWindow
{
    [super setShowInfoWindow:showInfoWindow];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.tappable = self.showInfoWindow;
        }, NO);
    }
}


-(void)setHeading:(CGFloat)heading
{
    [super setHeading:heading];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.rotation = self.heading;
        }, NO);
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    if (_gmarker) {
        
    }
}

-(void)setVisible:(BOOL)visible
{
    [super setVisible:visible];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.opacity = self.visible?self.opacity:0;
        }, NO);
    }
}

-(void)setDraggable:(BOOL)draggable
{
    [super setDraggable:draggable];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.draggable = self.draggable;
        }, NO);
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
            TiThreadPerformBlockOnMainThread(^{
                _gmarker.icon = [GMSMarker markerImageWithColor:self.tintColor];
            }, NO);
        }
//    }
    [self setNeedsRefreshingWithSelection:YES];
}

-(void)setAnchorPoint:(id)value
{
    [super setAnchorPoint:value];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.groundAnchor = [self nGetAnchorPoint];
        }, NO);
    }
}

-(void)setCalloutAnchorPoint:(id)value
{
    [super setCalloutAnchorPoint:value];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
        }, NO);
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
        _gmarker.opacity = self.visible?self.opacity:0;
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
