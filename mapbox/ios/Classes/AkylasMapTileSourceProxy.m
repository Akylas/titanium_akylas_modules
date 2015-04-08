#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapModule.h"
#import "TiUtils.h"
#import <Mapbox/Mapbox.h>
#import <GoogleMaps/GoogleMaps.h>
#import <Mapbox/RMOpenStreetMapSource.h>
#import <Mapbox/RMOpenSeaMapSource.h>
#import <Mapbox/RMOpenCycleMapSource.h>
#import <Mapbox/RMTileMillSource.h>
#import <Mapbox/RMMapQuestOSMSource.h>
#import "RMIgnMapSource.h"

#import "AkylasGMSURLTileLayer.h"
#import "AkylasGMSMBTilesLayer.h"

@implementation AkylasMapTileSourceProxy
{
    id<RMTileSource> _mpTileSource;
    
    RMTileCache* _tileCache;
    GMSTileLayer*  _gTileLayer;
    
    BOOL _fadeIn;
    BOOL _cacheable;
    NSInteger _zIndex;
    CGFloat _opacity;
    NSInteger _tileSize;
}
@synthesize caching;
@synthesize fadeIn = _fadeIn, zIndex =_zIndex, opacity = _opacity, tileSize = _tileSize, cacheable = _cacheable;
#pragma mark Internal

-(void)_configure
{
    _fadeIn = YES;
    _zIndex = -1;
    _opacity = 1.0f;
    _tileSize = 256;
    _cacheable = YES;
	[super _configure];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_mpTileSource);
    RELEASE_TO_NIL(_tileCache);
    RELEASE_TO_NIL(_gTileLayer);
	[super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Map.TileSource";
}

-(BOOL)caching
{
    return (_tileCache && [_tileCache isBackgroundCaching]);
}

-(void)setFadeIn:(BOOL)fadeIn
{
    _fadeIn = fadeIn;
    if (_gTileLayer) {
        _gTileLayer.fadeIn = _fadeIn;
    }
}

-(void)setZIndex:(NSInteger)zIndex
{
    _zIndex = zIndex;
    if (_gTileLayer) {
        _gTileLayer.zIndex = (int)_zIndex;
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    _opacity = opacity;
    if (_gTileLayer) {
        _gTileLayer.opacity = _opacity;
    }
}

-(void)setTileSize:(NSInteger)tileSize
{
    _tileSize = tileSize;
    if (_gTileLayer) {
        _gTileLayer.tileSize = _tileSize;
    }
}

-(void)setCacheable:(BOOL)cacheable
{
    _cacheable = cacheable;
    if (_mpTileSource) {
        [_mpTileSource setCacheable:_cacheable];
    }
}

-(RMTileCache*)tileCache
{
    if (_tileCache == nil) {
        _tileCache = [RMTileCache new];
        _tileCache.backgroundCacheDelegate = self;
    }
    return _tileCache;
}
-(void)cancelBackgroundCache:(id)arg
{
    if (_tileCache) {
        [_tileCache cancelBackgroundCache];
    }
}

-(NSDictionary*)region
{
    return [AkylasMapModule dictFromRegion:[[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox]];
}

-(NSNumber*)minZoom
{
    if (_mpTileSource) {
        return @([_mpTileSource minZoom]);
    }
    return [self valueForKey:@"minZoom"];
}

-(NSNumber*)maxZoom
{
    if (_mpTileSource) {
        return @([_mpTileSource maxZoom]);
    }
    return [self valueForKey:@"maxZoom"];
}

-(NSDictionary*)centerCoordinate
{
    RMSphericalTrapezium box = [[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox];
    
    return [AkylasMapModule dictFromLocation2D:RMSphericalTrapeziumCenter(box)];
}

-(void)beginBackgroundCache:(id)arg
{
	ENSURE_SINGLE_ARG_OR_NIL(arg,NSDictionary);
    id<RMTileSource> source = [self mpTileSource];
    RMSphericalTrapezium region = [AkylasMapModule regionFromObject:arg];
    float minZoom = [TiUtils floatValue:@"minZoom" properties:arg def:source.minZoom];
    float maxZoom = [TiUtils floatValue:@"maxZoom" properties:arg def:source.maxZoom];
    if (!CLLocationCoordinate2DIsValid(region.southWest)  || !CLLocationCoordinate2DIsValid(region.northEast))
    {
        region = source.latitudeLongitudeBoundingBox;
    }
    [[self tileCache] beginBackgroundCacheForTileSource:source southWest:region.southWest northEast:region.northEast minZoom:minZoom maxZoom:maxZoom];
}

- (void)tileCache:(RMTileCache *)tileCache didBeginBackgroundCacheWithCount:(NSUInteger)tileCount forTileSource:(id <RMTileSource>)tileSource
{
    if ([self _hasListeners:@"cachebegin"])
	{
		[self fireEvent:@"cachebegin" withObject:@{@"tileCount":@(tileCount)} propagate:NO checkForListener:NO];
	}
}

- (void)tileCache:(RMTileCache *)tileCache didBackgroundCacheTile:(RMTile)tile withIndex:(NSUInteger)tileIndex ofTotalTileCount:(NSUInteger)totalTileCount;
{
    if ([self _hasListeners:@"cachedtile"])
	{
		[self fireEvent:@"cachedtile" withObject:@{@"tileIndex":@(tileIndex), @"totalTileCount":@(totalTileCount)} propagate:NO checkForListener:NO];
	}
}

- (void)tileCacheDidFinishBackgroundCache:(RMTileCache *)tileCache;
{
    if ([self _hasListeners:@"cachefinished"])
	{
		[self fireEvent:@"cachefinished" withObject:@{} propagate:NO checkForListener:NO];
	}
}

- (void)tileCacheDidCancelBackgroundCache:(RMTileCache *)tileCache
{
    if ([self _hasListeners:@"cachecancel"])
	{
		[self fireEvent:@"cachecancel" withObject:@{} propagate:NO checkForListener:NO];
	}
}

-(void)setSource:(id)value
{
    [self replaceValue:value forKey:@"source" notification:YES];
}

-(void)setUserAgent:(id)value {
    [self replaceValue:value forKey:@"userAgent" notification:YES];
}

-(GMSTileLayer*)tileLayer
{
    id source = [self valueForKey:@"source"];
    if (!IS_OF_CLASS(source, NSString)) {
        if (IS_OF_CLASS(source, AkylasMapTileSourceProxy) && source != self) {
            return [source mpSource];
        }
        else if ([source respondsToSelector:@selector(nativePath)]) {
            source = [source nativePath];
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
    
    if ([typeLower isEqualToString:@"openstreetmap"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://tile.openstreetmap.org/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };

    }
    else if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[AkylasGMSMBTilesLayer alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
    }
    else if ([typeLower isEqualToString:@"openseamap"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://tiles.openseamap.org/seamark/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://otile1.mqcdn.com/tiles/1.0.0/map/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapquest-sat"])
    {
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"http://otile1.mqcdn.com/tiles/1.0.0/sat/%lu/%lu/%lu.png", (unsigned long)zoom, (unsigned long)x, (unsigned long)y];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"tilemill"] )
    {
        NSString* name = [TiUtils stringValue:[self valueForKey:@"mapName"]];
        NSString* url = [NSString stringWithFormat:@"%@:20008/tile/%@", [TiUtils stringValue:[self valueForKey:@"host"]], name];
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString *URLStr =
            [NSString stringWithFormat:@"%@/%lu/%lu/%lu.png?updated=%i", url, (unsigned long)x, (unsigned long)y, (unsigned long)zoom, (int)[[NSDate date] timeIntervalSince1970]];
            return [NSURL URLWithString:URLStr];
        };
    }
    else if ([typeLower isEqualToString:@"mapbox"] )
    {
         NSString* mapID = [TiUtils stringValue:[self valueForKey:@"mapId"]];
         NSString* imageQuality = [TiUtils stringValue:[self valueForKey:@"imageQuality"] def:@"png"];
        NSString* accessToken = [TiUtils stringValue:[self valueForKey:@"accessToken"] def:[RMConfiguration configuration].accessToken];
         CGFloat contentScaleFactor = [UIScreen mainScreen].scale;
         constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            return  [NSURL URLWithString:[NSString stringWithFormat:@"https://a.tiles.mapbox.com/v4/%@/%lu/%lu/%lu%@.%@?access_token=%@",
                                          mapID,
                                          (unsigned long)zoom,
                                          (unsigned long)x,
                                          (unsigned long)y,
                                          contentScaleFactor > 1.0 ? @"@2x" : @"",
                                          imageQuality,
                                          accessToken
                                          ]];
        };
    }
    else if ([typeLower isEqualToString:@"ign"] )
    {
        NSString* key = [TiUtils stringValue:[self valueForKey:@"key"]];
        NSString* layer = [TiUtils stringValue:[self valueForKey:@"layer"]];
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
        constructor = ^(NSUInteger x, NSUInteger y, NSUInteger zoom) {
            NSString* result = [NSString stringWithFormat:@"http://gpp3-wxs.ign.fr/%@/geoportail/wmts?LAYER=%@&EXCEPTIONS=text/xml&FORMAT=%@&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%lu&TILEROW=%lu&TILECOL=%lu",
                                key,
                                realLayer,
                                format,
                                (unsigned long)zoom,
                                (unsigned long)y,
                                (unsigned long)x
                                ];
            return  [NSURL URLWithString:result];
        };
    }

    if (constructor) {
        result = [[AkylasGMSURLTileLayer alloc] initWithConstructor:constructor];
        ((AkylasGMSURLTileLayer*)result).userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        ((AkylasGMSURLTileLayer*)result).cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
    }
    return [result autorelease];
}

-(id<RMTileSource>)mpSource
{
    id source = [self valueForKey:@"source"];
    if (!IS_OF_CLASS(source, NSString)) {
        if (IS_OF_CLASS(source, AkylasMapTileSourceProxy) && source != self) {
            return [source mpSource];
        }
        else if ([source respondsToSelector:@selector(nativePath)]) {
            source = [source nativePath];
        }
        else  {
            return nil;
        }
    }
    NSString* typeLower = [source lowercaseString];
    id<RMTileSource> result = nil;
    if ([typeLower isEqualToString:@"openstreetmap"])
    {
        result = [[RMOpenStreetMapSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"openseamap"])
    {
        result = [[RMOpenSeaMapSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"mapquest"])
    {
        result = [[RMMapQuestOSMSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"tilemill"])
    {
        NSString* name = [TiUtils stringValue:[self valueForKey:@"mapId"]];
        NSString* cacheKey = [TiUtils stringValue:[self valueForKey:@"cacheKey"]];
        result = [[RMTileMillSource alloc] initWithHost:[TiUtils stringValue:[self valueForKey:@"host"]] mapName:name tileCacheKey:cacheKey minZoom:[TiUtils floatValue:[self valueForKey:@"minZoom"]] maxZoom:[TiUtils floatValue:[self valueForKey:@"maxZoom"]]];
    }
    else if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[RMMBTilesSource alloc] initWithTileSetURL:[TiUtils toURL:source proxy:self]];
    }
    else if ([typeLower hasSuffix:@"ign"])
    {
        NSString* key = [TiUtils stringValue:[self valueForKey:@"key"]];
        NSString* layer = [TiUtils stringValue:[self valueForKey:@"layer"]];
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

        result = [[RMIgnMapSource alloc] init];
        ((RMIgnMapSource*)result).key = [TiUtils stringValue:[self valueForKey:@"key"]];
        ((RMIgnMapSource*)result).layer = realLayer;
        ((RMIgnMapSource*)result).format = [TiUtils stringValue:[self valueForKey:@"format"] def:@"image/jpeg"];
    }
    else {
        result = [[RMMapboxSource alloc] initWithMapID:typeLower];
    }
    
    if (result) {
        result.cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:((RMIgnMapSource*)result).cacheable];
    }
    return [result autorelease];
}


#pragma mark Mapbox

-(id<RMTileSource>)getMPTileSourceForMapView:(RMMapView*)mapView
{
    if (_mpTileSource == nil) {
        _mpTileSource = [[self mpSource] retain];
        if (!_mpTileSource) return nil;
        [_mpTileSource setCacheable:_cacheable];
    }
    return _mpTileSource;
}


-(id<RMTileSource>)mpTileSource
{
    return _mpTileSource;
}

#pragma mark GoogleMap


-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView
{
    if (_gTileLayer == nil) {
        _gTileLayer = [[self tileLayer] retain];
        if (!_gTileLayer) return nil;
        _gTileLayer.fadeIn = _fadeIn;
        _gTileLayer.zIndex = (int)_zIndex;
        _gTileLayer.opacity = _opacity;
        _gTileLayer.tileSize = _tileSize;
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
