//
//  AkylasGoogleMapTileSourceProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasMapBaseModule.h"
#import "TiUtils.h"

#import "AkylasGMSURLTileLayer.h"
#import "AkylasGMSMBTilesLayer.h"
#import "AkylasGooglemapView.h"
#import "TiCache.h"

@implementation AkylasGooglemapTileSourceProxy
{
    GMSTileLayer*  _gTileLayer;
    BOOL _needsClearCache;
    NSString* _url;
}
-(void)dealloc
{
    RELEASE_TO_NIL(_gTileLayer);
    RELEASE_TO_NIL(_url);
    [super dealloc];
}
-(void)_configure
{
    _needsClearCache = NO;
    [super _configure];
    self.zIndex = [[self class] gZIndexDelta];
}

-(NSString*)apiName
{
    return @"Akylas.GoogleMap.TileSource";
}

//-(NSDictionary*)region
//{
//    return [AkylasMapBaseModule dictFromRegion:[[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox]];
//}


-(void)setFadeIn:(BOOL)fadeIn
{
    [super setFadeIn:fadeIn];
    if (_gTileLayer) {
        TiThreadPerformBlockOnMainThread(^{
            _gTileLayer.fadeIn = self.fadeIn;
        }, NO);
    }
}

-(void)setZIndex:(NSInteger)zIndex
{
    [super setZIndex:zIndex];
    if (_gTileLayer) {
        TiThreadPerformBlockOnMainThread(^{
            _gTileLayer.zIndex = (int)self.zIndex;
        }, NO);
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    if (self.opacity == opacity) {
        return;
    }
    [super setOpacity:opacity];
    if (_gTileLayer &&  self.visible) {
        TiThreadPerformBlockOnMainThread(^{
            CGFloat oldOpacity = _gTileLayer.opacity;
            _gTileLayer.opacity = self.opacity;
            if (oldOpacity == 0) {
                [self invalidate];
            }
        }, NO);
    }
}

-(void)invalidate {
    if (_gTileLayer) {
        GMSMapView* map = _gTileLayer.map;
        int zindex = _gTileLayer.zIndex;
        _gTileLayer.zIndex = -1;
        _gTileLayer.map = nil;
        _gTileLayer.map = map;
        _gTileLayer.zIndex = zindex;
    }
}

-(void)setVisible:(BOOL)visible
{
    if (self.visible == visible) {
        return;
    }
    [super setVisible:visible];
    if (_gTileLayer) {
        TiThreadPerformBlockOnMainThread(^{
            
            _gTileLayer.opacity = visible?self.opacity:0;
            if (visible) {
                [self invalidate];
            }
        }, NO);
    }
}

-(void)setTileSize:(NSInteger)tileSize
{
    [super setTileSize:tileSize];
    if (_gTileLayer) {
        TiThreadPerformBlockOnMainThread(^{
            _gTileLayer.tileSize = self.tileSize;
        }, NO);
    }
}

-(void)clearCache:(id)unused
{
    if (_gTileLayer) {
        TiCache* cache = ((AkylasGMSMapView*)_gTileLayer.map).tileCache;
        [cache removeAllCachedImagesForCacheKey:[TiUtils stringValue:[self valueForKey:@"id"]]];
        TiThreadPerformBlockOnMainThread(^{
            [_gTileLayer clearTileCache];
        }, NO);
    } else {
        _needsClearCache = YES;
    }
}

-(void)setErrorImage:(id)arg
{
    [super setErrorImage:arg];
    if ((IS_OF_CLASS(_gTileLayer, AkylasGMSURLTileLayer))) {
        ((AkylasGMSURLTileLayer*)_gTileLayer).errorImage = _errorImage;
        TiThreadPerformBlockOnMainThread(^{
            [_gTileLayer clearTileCache];
        }, NO);
    }
}

-(void)setShowTileAfterMaxZoom:(BOOL)showTileAfterMaxZoom
{
    [super setShowTileAfterMaxZoom:showTileAfterMaxZoom];
    if ((IS_OF_CLASS(_gTileLayer, AkylasGMSURLTileLayer))) {
        ((AkylasGMSURLTileLayer*)_gTileLayer).showTileAfterMaxZoom = self.showTileAfterMaxZoom;
    }
}

+(int)gZIndexDelta {
    static int lastIndex = 0;
    return lastIndex++;
}
-(GMSTileLayer*)tileLayer
{
    id source = [self valueForKey:@"source"];
    if (IS_OF_CLASS(source, AkylasGooglemapTileSourceProxy) && source != self) {
        return [source tileLayer];
    }
    NSString* typeLower = [source lowercaseString];
    GMSTileLayer* result = nil;
    GMSTileURLConstructor constructor = nil;
    
    if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[AkylasGMSMBTilesLayer alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
    } else {
        _url = [TiUtils stringValue:[self valueForKey:@"url"]];
    }
    
    if (!result) {
        AkylasGMSURLTileLayer* theLayer = [[AkylasGMSURLTileLayer alloc] initWithConstructor:constructor];
        theLayer.url = _url;
        theLayer.subdomains = [TiUtils stringValue:[self valueForKey:@"subdomains"] def:@"abc"];
        theLayer.cacheKey = [TiUtils stringValue:[self valueForKey:@"id"]];
        theLayer.userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        theLayer.cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
        theLayer.minZoom = [TiUtils floatValue:[self valueForKey:@"minZoom"] def:-1];
        theLayer.maxZoom = [TiUtils floatValue:[self valueForKey:@"maxZoom"] def:-1];
        result = theLayer;
    }
    return [result autorelease];
}

-(void)removeFromMap {
    if (_gTileLayer != nil && _gTileLayer.map) {
        _gTileLayer.map = nil;
    }
}

-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView
{
    if (_gTileLayer == nil) {
        _gTileLayer = [[self tileLayer] retain];
        if (!_gTileLayer) return nil;

        _gTileLayer.fadeIn = self.fadeIn;
        _gTileLayer.zIndex = (int)self.zIndex;
        _gTileLayer.opacity = self.visible?self.opacity:0;
        _gTileLayer.tileSize = self.tileSize;
        [_gTileLayer setMap:mapView];
        if (_needsClearCache) {
            [self clearCache:nil];
            _needsClearCache = NO;
        }
    }
    else  {
        _gTileLayer.map = mapView;
//        RELEASE_TO_NIL(_gTileLayer)
//        return [self getGTileLayerForMapView:mapView];
    }
    return _gTileLayer;
}


-(GMSTileLayer*)gTileLayer
{
    return _gTileLayer;
}

@end
