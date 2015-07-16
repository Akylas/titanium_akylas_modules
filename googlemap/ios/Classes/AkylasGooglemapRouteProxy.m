//
//  AkylasGoogleMapRouteProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapRouteProxy.h"

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
}

-(void)dealloc
{
    RELEASE_TO_NIL(_gPath);
    RELEASE_TO_NIL(_spans);
    [_gPoly setUserData:nil];
    RELEASE_TO_NIL(_gPoly);
    [super dealloc];
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
    if (_gPoly != nil && !_spans)  {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.spans = @[[GMSStyleSpan spanWithColor:_color]];
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


-(void)setWidth:(id)value
{
    [super setWidth:value];

    if (_gPoly != nil)  {
        _gPoly.strokeWidth =_lineWidth;
    }
}

-(void)setTouchable:(BOOL)touchable
{
    [super setTouchable:touchable];
    if (configurationSet && _gPoly) {
        TiThreadPerformBlockOnMainThread(^{
            _gPoly.tappable = self.touchable;
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
    return 300;
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
        _gPoly.strokeWidth = _lineWidth;
        _gPoly.tappable = self.touchable;
        _gPoly.userData = self;
    }
    return _gPoly;
}

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView
{
    [self getOverlay];
    _gPoly.map = mapView;
    return _gPoly;
}

-(GMSOverlay*)gOverlay
{
    return _gPoly;
}

@end
