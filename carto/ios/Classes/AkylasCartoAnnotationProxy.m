//
//  AkylasCartoAnnotationProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasCartoAnnotationProxy.h"
#import "GClusterManager.h"
#import "AkylasCartoView.h"
#import "AkylasCartoClusterProxy.h"


@implementation AkylasGMSMarker
//-(void)setMap:(GMSMapView *)map
//{
//    if (self.map && !map && self.map.selectedMarker == self) {
//        self.map.selectedMarker = nil;
//    }
//    [super setMap:map];
//}
@end

@implementation AkylasCartoAnnotationProxy
{
    AkylasNTMarker* _marker;
    NTMarkerStyle* _style;
    NTMarkerStyleBuilder* _markerStyleBuilder;
    BOOL _selected;
}
@synthesize selected = _selected;

-(void)dealloc
{
//    [_marker setUserData:nil];
    RELEASE_TO_NIL(_marker);
    RELEASE_TO_NIL(_style);
    RELEASE_TO_NIL(_markerStyleBuilder);
    [super dealloc];
}


-(void)_configure
{
    [super _configure];
    _appearAnimation = YES;
    _tracksViewChanges = NO;
    _selected = NO;
    _mAnchorPoint = CGPointMake(0.5, 1.0);
    _calloutAnchorPoint = CGPointMake(0, 1.0f);
    self.zIndex = [[self class] gZIndexDelta];
}



-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Annotation";
}

-(void)updateMarker
{
//    _gmarker.flat = self.flat;
//    _gmarker.draggable = self.draggable;
    _marker.tappable = self.visible?self.touchable:NO;
    _marker.opacity = self.visible?self.opacity:0;
    _marker.rotation = self.heading;
    _marker.position = self.position;
    if (_marker.map && IS_OF_CLASS(_marker.map.delegate, AkylasCartoView)) {
       [ (AkylasCartoView*)(_gmarker.map.delegate) markerDidUpdate:_marker ];
    }
}

-(BOOL)shouldAnimate {
    return _marker && [super shouldAnimate];
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

-(NTMarker *)marker {
    return [self getMarker];
}

//-(void)setTitle:(id)value
//{
//    [super setTitle:value];
//    if (_gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            _marker.title = self.title;
//        }, NO);
//    }
////    [self setNeedsRefreshingWithSelection:NO];
//}
//
//-(void)setSubtitle:(id)value
//{
//    [super setSubtitle:value];
//    
//    if (_gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            _gmarker.snippet = self.subtitle;
//        }, NO);
//    }
////    [self setNeedsRefreshingWithSelection:NO];
//}

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

//-(void)setTouchable:(BOOL)touchable
//{
//    [super setTouchable:touchable];
//    if (configurationSet && _gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            [self updateMarker];
//        }, NO);
//    }
//}

//-(void)setTracksViewChanges:(BOOL)value
//{
//    _tracksViewChanges = value;
//    if (configurationSet && _gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            _gmarker.tracksViewChanges = _tracksViewChanges;
//        }, NO);
//    }
//}


//-(void)setAppearAnimation:(BOOL)value
//{
//    _appearAnimation = value;
//    if (configurationSet && _gmarker) {
//        TiThreadPerformBlockOnMainThread(^{
//            _gmarker.appearAnimation = _appearAnimation?kGMSMarkerAnimationPop:kGMSMarkerAnimationNone;
//        }, NO);
//    }
//}

-(void)setCanBeClustered:(BOOL)canBeClustered
{
    [super setCanBeClustered:canBeClustered];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.canBeClustered = self.canBeClustered;
            if (IS_OF_CLASS(self.delegate, AkylasCartoView)) {
                [[((AkylasCartoView*)self.delegate) clusterManager] cluster];
            } else if (IS_OF_CLASS(self.delegate, AkylasCartoClusterProxy)) {
                [((AkylasCartoView*)self.delegate) cluster];
            }
        }, NO);
        
    }
    
}

-(NTMarkerStyleBuilder*) getMarkerStyleBuilder
{
    if (!_markerStyleBuilder) {
        _markerStyleBuilder = [[NTMarkerStyleBuilder alloc] init];
        
        [markerStyleBuilder setColor:[self nGetTintColor]];
        if (_internalImage) {
            NTBitmap* markerBitmap = [NTBitmapUtils createBitmapFromUIImage:_internalImage];
            [markerStyleBuilder setBitmap:markerBitmap];
        } else if ([self nGetTintColor]){
            [markerStyleBuilder setColor:[self nGetTintColor]];
        }
        [markerStyleBuilder setSize:30];
        [markerStyleBuilder setHideIfOverlapped:NO];
        [markerStyleBuilder setPlacementPriority:(int)self.zIndex];
    }
    return _markerStyleBuilder;
}
-(NTMarkerStyle*) getMarkerStyle
{
    if (!_style) {
        _style = [[self getMarkerStyleBuilder] buildStyle];
    }
    return _style;
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
        if (_marker && !_internalImage) {
            TiThreadPerformBlockOnMainThread(^{
                [_marker getStyle].colo
                _marker.icon = [NTMarker markerImageWithColor:[self nGetTintColor]];
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
    if (_gmarker && !_selected) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.icon = _internalImage;
        }, NO);
    }
}
-(void)setInternalSelectedImage:(UIImage*)image {
    [super setInternalSelectedImage:image];
    if (_gmarker && _selected) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.icon = _internalSelectedImage;
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

- (void)setPinView:(id)value
{
    [self replaceValue:value forKey:@"pinView" notification:NO];
    if (_gmarker) {
        TiThreadPerformBlockOnMainThread(^{
            _gmarker.iconView = [self nGetPinViewAccessory];
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
    static int lastIndex = 800;
    return lastIndex;
}

-(void)removeFromMap {
     if (_gmarker != nil && _gmarker.map) {
         if (_gmarker.map.selectedMarker = _gmarker) {
             _gmarker.map.selectedMarker = nil;
         }
         _gmarker.map = nil;
     }
}


-(NTMarker*)getMarker {
    if (_gmarker == nil) {
        
        _gmarker = [[AkylasGMSMarker markerWithPosition:self.coordinate] retain];
        _gmarker.title = [self title];
        _gmarker.snippet = [self subtitle];
        _gmarker.userData = self;
        _gmarker.appearAnimation = _appearAnimation?kGMSMarkerAnimationPop:kGMSMarkerAnimationNone;
        _gmarker.tracksViewChanges = _tracksViewChanges;
        if (_internalImage) {
            _gmarker.icon = _internalImage;
        } else if ([self nGetTintColor]){
                _gmarker.icon = [NTMarker markerImageWithColor:[self nGetTintColor]];
        }
        _gmarker.draggable = self.draggable;
        _gmarker.flat = self.flat;
        _gmarker.zIndex = (int)self.zIndex;
       
        [self updateMarker];

        _gmarker.canBeClustered = self.canBeClustered;
        _gmarker.infoWindowAnchor = [self nGetCalloutAnchorPoint];
        _gmarker.groundAnchor = [self nGetAnchorPoint];
        _gmarker.iconView = [self nGetPinViewAccessory];
    }
    return _gmarker;
}

-(GMSOverlay*)getGOverlayForMapView:(AkylasGMSMapView*)mapView
{
    [self getMarker];
    _gmarker.map = mapView;
    return _gmarker;
}


-(GMSOverlay*)gOverlay
{
    return _gmarker;
}

-(void)showInfo:(id)args
{
    if (self.showInfoWindow) {
        GMSMapView* map = _gmarker.map;
        id delegate = [map delegate];
        if (map && IS_OF_CLASS(delegate, AkylasCartoView)) {
            TiThreadPerformBlockOnMainThread(^{
                [delegate showCalloutForOverlay:_gmarker];
            }, NO);
        }
    } else {
        [self hideInfo:nil];
    }
    
}

-(void)hideInfo:(id)args
{
    GMSMapView* map = _gmarker.map;
    id delegate = [map delegate];
    if (map && IS_OF_CLASS(delegate, AkylasCartoView)) {
        TiThreadPerformBlockOnMainThread(^{
        [delegate hideCalloutForOverlay:_gmarker];
        }, NO);
    }
}

-(void)onSelected:(GMSOverlay*)overlay {
    if (overlay != _gmarker) {
        return;
    }
    _selected = YES;
    _gmarker.zIndex = 10000;
    _gmarker.canBeClustered = NO;
    if (_internalSelectedImage) {
        _gmarker.icon = _internalSelectedImage;
    }
    [self showInfo:nil];
}
-(void)onDeselected:(GMSOverlay*)overlay {
    if (overlay != _gmarker) {
        return;
    }
    _selected = NO;
    if (_internalSelectedImage) {
        if (_internalImage) {
            _gmarker.icon = _internalImage;
        } else if ([self nGetTintColor]){
            _gmarker.icon = [NTMarker markerImageWithColor:[self nGetTintColor]];
        }
    }
    
    _gmarker.canBeClustered = self.canBeClustered;
    _gmarker.zIndex = (int)self.zIndex;
    [self hideInfo:nil];

}
@end
