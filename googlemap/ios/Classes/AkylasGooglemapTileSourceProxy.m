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
        TiThreadPerformBlockOnMainThread(^{
            [_gTileLayer clearTileCache];
        }, NO);
    } else {
        _needsClearCache = YES;
    }
}


-(GMSTileLayer*)tileLayer
{
    id source = [self valueForKey:@"source"];
    if (!IS_OF_CLASS(source, NSString)) {
        if (IS_OF_CLASS(source, AkylasGooglemapTileSourceProxy) && source != self) {
            return [source tileLayer];
        }
        else if ([source respondsToSelector:@selector(nativePath)]) {
            
        }
        else  {
            return nil;
        }
    }
    NSString* typeLower = [source lowercaseString];
    GMSTileLayer* result = nil;
    GMSTileURLConstructor constructor = nil;
    
    //    if ([typeLower hasSuffix:@"mbtiles"])
    //    {
    //        result = [[RMMBTilesSource alloc] initWithTileSetURL:[TiUtils toURL:type proxy:proxy]];
    //    }
    if ([typeLower isEqualToString:@"websource"])
    {
        _url = [TiUtils stringValue:[self valueForKey:@"url"]];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString* sX = [NSString stringWithFormat:@"%lu", x];
            NSString* sY = [NSString stringWithFormat:@"%lu", y];
            NSString* sZ = [NSString stringWithFormat:@"%lu", zoom];
            return [NSURL URLWithString:[[[_url stringByReplacingOccurrencesOfString:@"{x}" withString:sX] stringByReplacingOccurrencesOfString:@"{y}" withString:sY] stringByReplacingOccurrencesOfString:@"{z}" withString:sZ]];
        };
    }
    else if ([typeLower isEqualToString:@"openstreetmap"])
    {
        _url = @"http://tile.openstreetmap.org/%lu/%lu/%lu.png";
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:_url, (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
        
    }
    else if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[AkylasGMSMBTilesLayer alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
    }
    else if ([typeLower isEqualToString:@"openseamap"])
    {
        _url = @"http://tiles.openseamap.org/seamark/%lu/%lu/%lu.png";
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:_url, (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest"])
    {
        _url = @"http://otile1.mqcdn.com/tiles/1.0.0/map/%lu/%lu/%lu.png";
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:_url, (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest-sat"])
    {
        _url = @"http://otile1.mqcdn.com/tiles/1.0.0/sat/%lu/%lu/%lu.png";
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:_url, (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"tilemill"] )
    {
        NSString* name = [TiUtils stringValue:[self valueForKey:@"mapName"]];
        NSString* url = [NSString stringWithFormat:@"%@:20008/tile/%@", [TiUtils stringValue:[self valueForKey:@"host"]], name];
        _url = [[url stringByAppendingString:@"/%lu/%lu/%lu.png?updated=%i"] retain];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:_url, (unsigned long)x, (unsigned long)y, (unsigned long)zoom, (int)[[NSDate date] timeIntervalSince1970]];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapbox"] )
    {
        NSString* mapID = [TiUtils stringValue:[self valueForKey:@"mapId"]];
        NSString* imageQuality = [TiUtils stringValue:[self valueForKey:@"imageQuality"] def:@"png"];
        NSString* accessToken = [TiUtils stringValue:[self valueForKey:@"accessToken"] def:[AkylasMapBaseModule valueForKey:@"accessToken"]];
        CGFloat contentScaleFactor = [UIScreen mainScreen].scale;
        NSString* url = [NSString stringWithFormat:@"https://a.tiles.mapbox.com/v4/%@/", mapID];
        _url = [[[url stringByAppendingString:@"%lu/%lu/%lu"] stringByAppendingString:[NSString stringWithFormat:@"%@.%@?access_token=%@", contentScaleFactor > 1.0 ? @"@2x" : @"",
                                                                                       imageQuality,
                                                                                       accessToken]] retain];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            return  [NSURL URLWithString:[NSString stringWithFormat:_url,
                                          (unsigned long)zoom,
                                          (unsigned long)x,
                                          (unsigned long)y
                                          ]];
        };
    }
    else if ([typeLower isEqualToString:@"ign"] )
    {
        NSString* key = [TiUtils stringValue:[self valueForKey:@"key"] def:@"xxx"];
        NSString* layer = [TiUtils stringValue:[self valueForKey:@"layer"] def:@"xxx"];
        NSString* format = [TiUtils stringValue:[self valueForKey:@"format"] def:@"image/jpeg"];
        
        NSString* realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS";
        if ([layer isEqualToString:@"express"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.CLASSIQUE";
        } else if ([layer isEqualToString:@"expressStandard"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD";
        } else if ([layer isEqualToString:@"plan"]) {
            realLayer = @"GEOGRAPHICALGRIDSYSTEMS.PLANIGN";
        } else if ([layer isEqualToString:@"buildings"]) {
            realLayer = @"BUILDINGS.BUILDINGS";
        } else if ([layer isEqualToString:@"parcels"]) {
            realLayer = @"CADASTRALPARCELS.PARCELS";
        } else if ([layer isEqualToString:@"slopes"]) {
            realLayer = @"ELEVATION.SLOPES.HIGHRES";
        }
        NSString* url = [NSString stringWithFormat:@"http://gpp3-wxs.ign.fr/%@/geoportail/wmts?LAYER=%@&EXCEPTIONS=text/xml&FORMAT=%@&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM", key,
                         realLayer,
                         format];
        _url = [[url stringByAppendingString:@"&TILEMATRIX=%lu&TILEROW=%lu&TILECOL=%lu"] retain];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString* result = [NSString stringWithFormat:_url,
                                (unsigned long)zoom,
                                (unsigned long)y,
                                (unsigned long)x
                                ];
            return  [NSURL URLWithString:result];
        };
    }
    
    if (constructor) {
        AkylasGMSURLTileLayer* theLayer = [[AkylasGMSURLTileLayer alloc] initWithConstructor:constructor];
        theLayer.cacheKey = [TiUtils stringValue:[self valueForKey:@"id"]];
        theLayer.userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        theLayer.cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
        theLayer.minZoom = [TiUtils floatValue:[self valueForKey:@"minZoom"] def:-1];
        theLayer.maxZoom = [TiUtils floatValue:[self valueForKey:@"maxZoom"] def:-1];
        result = theLayer;
    }
    return [result autorelease];
}


-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView
{
    if (_gTileLayer == nil) {
        _gTileLayer = [[self tileLayer] retain];
        if (!_gTileLayer) return nil;
        if (_needsClearCache) {
            [_gTileLayer clearTileCache];
            _needsClearCache = NO;
        }
        _gTileLayer.fadeIn = self.fadeIn;
        _gTileLayer.zIndex = (int)self.zIndex;
        _gTileLayer.opacity = self.visible?self.opacity:0;
        _gTileLayer.tileSize = self.tileSize;
        [_gTileLayer setMap:mapView];
    }
    else if (_gTileLayer.map && _gTileLayer.map != mapView) {
        RELEASE_TO_NIL(_gTileLayer)
        return [self getGTileLayerForMapView:mapView];
    }
    return _gTileLayer;
}


-(GMSTileLayer*)gTileLayer
{
    return _gTileLayer;
}

@end
