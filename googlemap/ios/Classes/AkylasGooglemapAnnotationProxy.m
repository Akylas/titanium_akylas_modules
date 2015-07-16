//
//  AkylasGooglemapAnnotationProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapAnnotationProxy.h"
#import "GClusterManager.h"
#import "AkylasGooglemapView.h"
#import "AkylasGooglemapClusterProxy.h"

@implementation AkylasGooglemapAnnotationProxy
{
    GMSMarker* _gmarker;
}

-(void)dealloc
{
    [_gmarker setUserData:nil];
    RELEASE_TO_NIL(_gmarker);
    [super dealloc];
}


-(void)_configure
{
    [super _configure];
    _mAnchorPoint = CGPointMake(0.5, 1.0);
    _calloutAnchorPoint = CGPointMake(0, 1.0f);
}



-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Annotation";
}

-(void)updateMarker
{
//    _gmarker.flat = self.flat;
//    _gmarker.draggable = self.draggable;
    _gmarker.tappable = self.visible?self.touchable:NO;
    _gmarker.opacity = self.visible?self.opacity:0;
    _gmarker.rotation = self.heading;
    _gmarker.position = self.position;
}

-(void)setConfigurationSet:(BOOL)value
{
    [super setConfigurationSet:value];
    if (configurationSet) {
        if (_gmarker) {
            [CATransaction begin];
            if (![self shouldAnimate]) {
                [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
            }
            [self updateMarker];
            [CATransaction commit];
        }
    }
}

-(void)refreshCoords {
//    if (_gmarker) {
//        [CATransaction begin];
//        if (![self shouldAnimate]) {
//            [CATransaction setValue:(id)kCFBooleanTrue forKey:kCATransactionDisableActions];
//        }
//        _gmarker.position = self.coordinate;
//        _gmarker.opacity = self.visible?self.opacity:0;
//        [CATransaction commit];
//    }
//    [super refreshCoords];
}

-(CLLocationCoordinate2D)position {
    return self.coordinate;
}

-(GMSMarker *)marker {
    return [self getMarker];
}

-(void)setTitle:(id)value
{
    [super setTitle:value];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.title = self.title;
        }, NO);
    }
//    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setSubtitle:(id)value
{
    [super setSubtitle:value];
    
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.snippet = self.subtitle;
        }, NO);
    }
//    [self setNeedsRefreshingWithSelection:NO];
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
//    if (_gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            _gmarker.tappable = self.showInfoWindow;
//        }, NO);
//    }
}


-(void)setHeading:(CGFloat)heading
{
    [super setHeading:heading];
    if (configurationSet && _gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.rotation = self.heading;
        }, NO);
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    if (configurationSet && _gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            [self updateMarker];
        }, NO);
    }
}


-(void)setVisible:(BOOL)visible
{
    [super setVisible:visible];
    if (configurationSet && _gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            [self updateMarker];
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

-(void)setTouchable:(BOOL)touchable
{
    [super setTouchable:touchable];
    if (configurationSet && _gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            [self updateMarker];
        }, NO);
    }
}
-(void)setCanBeClustered:(BOOL)canBeClustered
{
    [super setCanBeClustered:canBeClustered];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.canBeClustered = self.canBeClustered;
            if (IS_OF_CLASS(self.delegate, AkylasGooglemapView)) {
                [[((AkylasGooglemapView*)self.delegate) clusterManager] cluster];
            } else if (IS_OF_CLASS(self.delegate, AkylasGooglemapClusterProxy)) {
                [((AkylasGooglemapView*)self.delegate) cluster];
            }
        }, NO);
        
    }
    
}

-(void)setTintColor:(id)color
{
    [super setTintColor:color];
//    [self replaceValue:color forKey:@"color" notification:NO];
//    if ([color isKindOfClass:[NSNumber class]]) {
//        self.pinColor = [TiUtils intValue:color];
//        self.tintColor = nil;
//    }
//    else {
//        self.tintColor = [[TiUtils colorValue:color] _color];
        if (_gmarker && !_internalImage) {
            TiThreadPerformBlockOnMainThread(^{
                _gmarker.icon = [GMSMarker markerImageWithColor:[self nGetTintColor]];
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

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.icon = _internalImage;
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


-(void)removeFromMap {
     if (_gmarker != nil && _gmarker.map) {
         if (_gmarker.map.selectedMarker = _gmarker) {
             _gmarker.map.selectedMarker = nil;
         }
         _gmarker.map = nil;
     }
}


-(GMSMarker*)getMarker {
    if (_gmarker == nil) {
        
        _gmarker = [[GMSMarker markerWithPosition:self.coordinate] retain];
        _gmarker.appearAnimation = kGMSMarkerAnimationPop;
        _gmarker.title = [self title];
        _gmarker.snippet = [self subtitle];
        _gmarker.userData = self;
        if (_internalImage) {
            _gmarker.icon = _internalImage;
        } else if ([self nGetTintColor]){
                _gmarker.icon = [GMSMarker markerImageWithColor:[self nGetTintColor]];
        }
        _gmarker.draggable = self.draggable;
        _gmarker.flat = self.flat;
       
        [self updateMarker];

        _gmarker.canBeClustered = self.canBeClustered;
        _gmarker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
        _gmarker.groundAnchor = [self nGetAnchorPoint];
    }
    return _gmarker;
}

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView
{
    [self getMarker];
    _gmarker.map = mapView;
    return _gmarker;
}


-(GMSOverlay*)gOverlay
{
    return _gmarker;
}


@end
