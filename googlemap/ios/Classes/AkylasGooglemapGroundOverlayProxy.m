//
//  AkylasGooglemapGroundOverlayProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 09/05/2015.
//
//

#import "AkylasGooglemapGroundOverlayProxy.h"
#import "AkylasGooglemapModule.h"

@implementation UIImage (GoogleMaps)
- (UIImage *)imageByApplyingAlpha:(CGFloat) alpha {
    UIGraphicsBeginImageContextWithOptions(self.size, NO, 0.0f);
    
    CGContextRef ctx = UIGraphicsGetCurrentContext();
    CGRect area = CGRectMake(0, 0, self.size.width, self.size.height);
    
    CGContextScaleCTM(ctx, 1, -1);
    CGContextTranslateCTM(ctx, 0, -area.size.height);
    
    CGContextSetBlendMode(ctx, kCGBlendModeMultiply);
    
    CGContextSetAlpha(ctx, alpha);
    
    CGContextDrawImage(ctx, area, self.CGImage);
    
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return newImage;
}
@end

@implementation AkylasGooglemapGroundOverlayProxy
{
    GMSGroundOverlay* _gOverlay;
    GMSCoordinateBounds *_gBounds;
}

-(void)dealloc
{
    RELEASE_TO_NIL(_gBounds);
    RELEASE_TO_NIL(_gOverlay);
    
    [super dealloc];
}

-(void)setTitle:(id)value
{
    [super setTitle:value];
    if (_gOverlay) {
        _gOverlay.title = self.title;
    }
    [self setNeedsRefreshingWithSelection:NO];
}

-(void)setHeading:(CGFloat)heading
{
    [super setHeading:heading];
    if (_gOverlay) {
        _gOverlay.bearing = self.heading;
    }
}

-(void)setRegion:(id)obj
{
    RELEASE_TO_NIL(_gBounds)
    AkRegion box = [AkylasMapBaseModule regionFromObject:obj];
    if (AkRegionIsValid(box)) {
        _gBounds = [boundsFromRegion(box) retain];
    }
    if (_gOverlay) {
        _gOverlay.bounds = _gBounds;
    }
    [self replaceValue:obj forKey:@"region" notification:NO];
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    if (_gOverlay && _internalImage) {
        if (opacity == 1.0f) {
            _gOverlay.icon = _internalImage;
        } else {
            _gOverlay.icon = [_internalImage imageByApplyingAlpha:opacity];
        }
    }
}

-(void)setInternalImage:(UIImage*)image {
    [super setInternalImage:image];
    if (_gOverlay) {
        TiThreadPerformBlockOnMainThread(^{
            _gOverlay.icon = _internalImage;
        }, NO);
    }
}


-(CGSize)getSize {
    if (_internalImage) {
        return _internalImage.size;
    }
    return CGSizeZero;
}

+(int)gZIndexDelta {
    return 30;
}

-(void)onPointProcessed
{
    RELEASE_TO_NIL(_gBounds)
    GMSMutablePath* path =  [[GMSMutablePath alloc] init];
    pthread_rwlock_rdlock(&routeLineLock);
    NSUInteger count = [_routeLine count];
    for (int i = 0; i < count; ++i) {
        CLLocation* entry = [_routeLine objectAtIndex:i];
        [path addCoordinate:entry.coordinate];
    }
    pthread_rwlock_unlock(&routeLineLock);
    _gBounds = [[GMSCoordinateBounds alloc] initWithPath:path];
    [path release];
}

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView
{
    if (_gOverlay == nil) {
        _gOverlay = [GMSGroundOverlay groundOverlayWithBounds:_gBounds icon:nil];
        _gOverlay.map = mapView;
        _gOverlay.title = [self title];
        if (_internalImage) {
            if (self.opacity == 1.0f) {
                _gOverlay.icon = _internalImage;
            } else {
                _gOverlay.icon = [_internalImage imageByApplyingAlpha:self.opacity];
            }
        }
        _gOverlay.tappable = self.showInfoWindow;
        _gOverlay.bearing = self.heading;
    }
    else if (_gOverlay.map != mapView) {
        RELEASE_TO_NIL(_gOverlay)
        return [self getGOverlayForMapView:mapView];
    }
    return _gOverlay;
}


-(GMSOverlay*)gOverlay
{
    return _gOverlay;
}

@end
