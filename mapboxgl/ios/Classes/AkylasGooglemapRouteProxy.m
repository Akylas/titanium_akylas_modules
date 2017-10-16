//
//  AkylasGoogleMapRouteProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapRouteProxy.h"
#import "AkylasGooglemapView.h"

@implementation TIGMSPolyline

-(void)dealloc
{
    RELEASE_TO_NIL(_userData);
    [super dealloc];
}
@end

@implementation AkylasGooglemapRouteProxy
{
    TIGMSPolyline* _gPoly;
    GMSMutablePath *_gPath;
    NSArray * _spans;
    GMSMapView* _mapView;
    BOOL _selected;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_mapView);
    RELEASE_TO_NIL(_gPath);
    RELEASE_TO_NIL(_spans);
    [_gPoly setUserData:nil];
    RELEASE_TO_NIL(_gPoly);
    [super dealloc];
}
-(void)_configure
{
    _selected = NO;
    [super _configure];
    self.zIndex = [[self class] gZIndexDelta];
}
-(NSString*)apiName
{
    return @"Akylas.GoogleMap.Route";
}

//-(void)updateMarker
//{
//    //    _gmarker.flat = self.flat;
//    //    _gmarker.draggable = self.draggable;
//    _gPoly.tappable = self.touchable:NO;
////    _gPoly.opacity = self.visible?self.opacity:0;
//}


-(void)setColor:(id)value
{
    [super setColor:value];
    if (_gPoly != nil && (!_selected || !_selectedColor) && !_spans)  {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
        }, NO);
    }
}

-(void)setSelectedColor:(id)value
{
    [super setSelectedColor:value];
    if (_gPoly != nil && _selected && !_spans)  {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.spans = @[[GMSStyleSpan spanWithColor:_selectedColor]];
        }, NO);
    }
}


-(void)onPointProcessed
{
    RELEASE_TO_NIL(_gPath)
    if (_gPoly) {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.path = [self getGPath];
        }, NO);
    }
}

-(void)onPointAdded:(CLLocation*)newPoint
{

    TiThreadPerformBlockOnMainThread(^{
        if (_gPath) {
            [_gPath addCoordinate:newPoint.coordinate];
        }
    }, NO);
    //    if (_shape) {
    //        [_shape addLineToCoordinate:newPoint.coordinate];
    //    }
    //    if (_gPath) {
    //        [_gPath addCoordinate:newPoint.coordinate];
    //    }
}


-(void)setLineWidth:(CGFloat)lineWidth
{
    [super setLineWidth:lineWidth];

    if (_gPoly != nil)  {
        _gPoly.strokeWidth =self.lineWidth;
    }
}

-(void)setTouchable:(BOOL)touchable
{
    [super setTouchable:touchable];
    if (_gPoly) {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.tappable = self.touchable;
        }, NO);
    }
}

-(void)setVisible:(BOOL)visible
{
    [super setVisible:visible];
    if (_gPoly && _mapView) {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.map = self.visible?_mapView:nil;
        }, NO);
    }
}

-(void)setSpans:(id)value
{
    [self replaceValue:value forKey:@"spans" notification:NO];
    ENSURE_TYPE(value, NSArray)
    if (!value) {
        RELEASE_TO_NIL(_spans)
        if (_gPoly != nil)  {
            _gPoly.spans = nil;
        }
    }
    NSMutableArray* spans = [NSMutableArray arrayWithCapacity:[value count]];
    [value enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        if (IS_OF_CLASS(obj, NSArray)) {
            NSUInteger length = [obj count];
            if (length > 0) {
                UIColor* color1 = [[TiUtils colorValue:[obj objectAtIndex:0]] _color];
                UIColor* color2 = nil;
                NSUInteger segments = 0;
                if (length > 2) {
                    color2 = [[TiUtils colorValue:[obj objectAtIndex:1]] _color];
                    segments = [TiUtils intValue:[obj objectAtIndex:2]];
                } else if (length > 1) {
                    color2 = [[TiUtils colorValue:[obj objectAtIndex:1]] _color];
                    if (color2 == nil) {
                        segments = [TiUtils intValue:[obj objectAtIndex:1]];
                    }
                }
                GMSStrokeStyle *style;
                if (color2) {
                    style = [GMSStrokeStyle gradientFromColor:color1 toColor:color2];
                } else {
                    style = [GMSStrokeStyle solidColor:color1];
                }
                if (segments > 0) {
                    [spans addObject:[GMSStyleSpan spanWithStyle:style segments:segments]];
                } else {
                    [spans addObject:[GMSStyleSpan spanWithStyle:style]];
                }
            }
        }
    }];
    _spans = [spans retain];
    if (_gPoly != nil)  {
        _gPoly.spans = _spans;
    }
}



#pragma mark GoogleMap
+(int)gZIndexDelta {
    static int lastIndex = 300;
    return lastIndex;
}
-(GMSMutablePath *) getGPath {
    if (_gPath == nil) {
        _gPath =  [[GMSMutablePath alloc] init];
        pthread_rwlock_rdlock(&routeLineLock);
        NSUInteger count = [_routeLine count];
        for (int i = 0; i < count; ++i) {
            CLLocation* entry = [_routeLine objectAtIndex:i];
            [_gPath addCoordinate:entry.coordinate];
        }
        pthread_rwlock_unlock(&routeLineLock);
    }
    return _gPath;
}

-(void)removeFromMap {
    RELEASE_TO_NIL(_mapView)
    if (_gPoly != nil && _gPoly.map) {
        _gPoly.map = nil;
    }
}

-(GMSOverlay*)getOverlay {
    if (_gPoly == nil) {
        _gPoly = [[TIGMSPolyline polylineWithPath:[self getGPath]] retain];
        if (_spans) {
            _gPoly.spans = _spans;
        } else {
            _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
        }
        _gPoly.strokeWidth = self.lineWidth;
        _gPoly.tappable = self.touchable;
        _gPoly.userData = self;
    }
    return _gPoly;
}

-(GMSPath*)gPath {
    return _gPath;
}

-(GMSOverlay*)getGOverlayForMapView:(AkylasGMSMapView*)mapView
{
    [self getOverlay];
    RELEASE_TO_NIL(_mapView)
    _mapView = [mapView retain];
    _gPoly.map = _mapView;
    return _gPoly;
}

-(GMSOverlay*)gOverlay
{
    return _gPoly;
}


-(void)showInfo:(id)args
{
    if (self.showInfoWindow) {
        if (_gPoly.map && IS_OF_CLASS(_gPoly.map.delegate, AkylasGooglemapView)) {
            [ (AkylasGooglemapView*)(_gPoly.map.delegate) showCalloutForOverlay:_gPoly];
        }
    }
    
}

-(void)hideInfo:(id)args
{
    if (_gPoly.map && IS_OF_CLASS(_gPoly.map.delegate, AkylasGooglemapView)) {
        [ (AkylasGooglemapView*)(_gPoly.map.delegate) hideCalloutForOverlay:_gPoly];
    }
}

-(void)onSelected:(GMSOverlay*)overlay {
    if (overlay != _gPoly) {
        return;
    }
    _selected = YES;
    _gPoly.zIndex = 10000;
    if (_selectedColor ) {
        _gPoly.spans = @[[GMSStyleSpan spanWithColor:_selectedColor]];
    }
    if (self.selectedLineWidth >= 0 && self.selectedLineWidth != self.lineWidth ) {
        _gPoly.strokeWidth =self.selectedLineWidth;
    }
    [self showInfo:nil];
}
-(void)onDeselected:(GMSOverlay*)overlay {
    if (overlay != _gPoly) {
        return;
    }
    _selected = NO;
    if (_selectedColor ) {
        _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
    }
    if (self.selectedLineWidth != self.lineWidth ) {
        _gPoly.strokeWidth =self.lineWidth;
    }
    
    _gPoly.zIndex = (int)self.zIndex;
    [self hideInfo:nil];
    
}

@end
