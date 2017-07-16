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
    @protected
    GMSTileLayer*  _gTileLayer;
    BOOL _needsClearCache;
}
-(void)dealloc
{
    RELEASE_TO_NIL(_gTileLayer);
//    RELEASE_TO_NIL(_url);
    [super dealloc];
}
-(void)_configure
{
    _needsClearCache = NO;
    [super _configure];
    self.zIndex = [[self class] gZIndexDelta];
    self.canChangeTileSize = YES;
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
            _gTileLayer.tileSize = [self getRealTileSize];
        }, NO);
    }
}

-(void)setAutoHd:(id)value
{
    [self replaceValue:value forKey:@"autoHd" notification:NO];
    if (_gTileLayer) {
        TiThreadPerformBlockOnMainThread(^{
            _gTileLayer.tileSize = [self getRealTileSize];
        }, NO);
    }
}

-(NSInteger)getRealTileSize {
    if (!self.canChangeTileSize) {
        return self.tileSize;
    }
    NSInteger tileSize = self.tileSize;
    BOOL autoHd = [TiUtils boolValue:[self valueForKey:@"autoHd"] def:NO];
    BOOL shouldBootUpHD = tileSize / [TiUtils screenScale] < 256;
    if (shouldBootUpHD && autoHd) {
        return tileSize;
    } else {
        return tileSize * 2;
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
    return lastIndex;
}

-(void)initializeLayer
{
    if (IS_OF_CLASS(_gTileLayer, AkylasGMSURLTileLayer)) {
        AkylasGMSURLTileLayer* theLayer = (AkylasGMSURLTileLayer*)_gTileLayer;
//        theLayer.url = _url;
        if ([theLayer.url containsString:@"{bbox}"]) {
            theLayer.wmsFormat = YES;
        }
        theLayer.subdomains = [TiUtils stringValue:[self valueForKey:@"subdomains"] def:@"abc"];
        theLayer.cacheKey = [TiUtils stringValue:[self valueForKey:@"id"]];
        theLayer.userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        theLayer.autoHd = [TiUtils boolValue:[self valueForKey:@"autoHd"] def:NO];
        theLayer.cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
        if (theLayer.minZoom == -1) {
            theLayer.minZoom = [TiUtils floatValue:[self valueForKey:@"minZoom"] def:-1];
        }
        if (theLayer.maxZoom == -1) {
            theLayer.maxZoom = [TiUtils floatValue:[self valueForKey:@"maxZoom"] def:-1];
        }
    }
}
-(GMSTileLayer*)tileLayer
{
    id source = [self valueForKey:@"source"];
    if (IS_OF_CLASS(source, AkylasGooglemapTileSourceProxy) && source != self) {
        return [source tileLayer];
    }
    NSString* typeLower = [source lowercaseString];
    GMSTileLayer* result = nil;
//    GMSTileURLConstructor constructor = nil;
    NSString* url  = [TiUtils stringValue:[self valueForKey:@"url"]];
    if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[AkylasGMSMBTilesLayer alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
//    } else {
//        _url = [TiUtils stringValue:[self valueForKey:@"url"]];
    }
    
    if (!result && url) {

        result = [[AkylasGMSURLTileLayer alloc] initWithConstructor:nil];
        ((AkylasGMSURLTileLayer*)result).url = url;
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
        [self initializeLayer];
        _gTileLayer.fadeIn = self.fadeIn;
        _gTileLayer.zIndex = (int)self.zIndex;
        _gTileLayer.opacity = self.visible?self.opacity:0;
        _gTileLayer.tileSize = [self getRealTileSize];
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

-(id)getCachedImage:(id)args
{
    ENSURE_TYPE(args, NSArray)
    NSNumber *x = nil;
    NSNumber *y = nil;
    NSNumber *z = nil;
    ENSURE_ARG_AT_INDEX(x, args, 0, NSNumber);
    ENSURE_ARG_AT_INDEX(y, args, 1, NSNumber);
    ENSURE_ARG_AT_INDEX(z, args, 1, NSNumber);
    if (IS_OF_CLASS(_gTileLayer, AkylasGMSURLTileLayer)) {
        UIImage* image = [(AkylasGMSURLTileLayer*)_gTileLayer getCachedImageForX:[x integerValue] y:[y integerValue] zoom:[z integerValue]];
    } else {
        return nil;
    }
}

@end
