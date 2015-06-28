//
//  AkylasGoogleMapTileSourceProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapboxTileSourceProxy.h"
#import "AkylasMapboxModule.h"
#import "TiUtils.h"

#import <Mapbox/RMOpenStreetMapSource.h>
#import <Mapbox/RMOpenSeaMapSource.h>
#import <Mapbox/RMOpenCycleMapSource.h>
#import <Mapbox/RMTileMillSource.h>
#import <Mapbox/RMMapQuestOSMSource.h>
#import "RMIgnMapSource.h"
#import "AkylasWebSource.h"

AkRegion AkRegionFromMBBox(RMSphericalTrapezium box) {
    return (AkRegion){
        .northEast = box.northEast,
        .southWest = box.southWest
    };
}

RMSphericalTrapezium MBBoxFromAkRegion(AkRegion box) {
    return (RMSphericalTrapezium){
        .northEast = box.northEast,
        .southWest = box.southWest
    };
}

@implementation AkylasMapboxTileSourceProxy
{
    id<RMTileSource> _mpTileSource;
    
    RMTileCache* _tileCache;
    RMMapView* _mapView;
}
-(void)dealloc
{
    RELEASE_TO_NIL(_mpTileSource);
    RELEASE_TO_NIL(_tileCache);
    _mapView = nil;
    [super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Mapbox.TileSource";
}

//-(NSDictionary*)region
//{
//    return [AkylasMapBaseModule dictFromRegion:[[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox]];
//}


-(void)setCacheable:(BOOL)cacheable
{
    [super setCacheable:cacheable];
    if (_mpTileSource) {
        [_mpTileSource setCacheable:self.cacheable];
    }
}

-(void)setOpacity:(CGFloat)opacity
{
    [super setOpacity:opacity];
    
    if (_mpTileSource)  {
//        _mpTileSource.opacity = self.opacity;
        if (_mapView) {
            TiThreadPerformBlockOnMainThread(^{
                [_mapView setAlpha:self.opacity forTileSource:_mpTileSource];
            }, NO);
        }
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

-(NSNumber*)minZoom
{
    if (_mpTileSource) {
        return @([_mpTileSource minZoom]);
    }
    return [super minZoom];
}

-(NSNumber*)maxZoom
{
    if (_mpTileSource) {
        return @([_mpTileSource maxZoom]);
    }
    return [super maxZoom];
}

-(NSDictionary*)region
{
    return [AkylasMapboxModule dictFromRegion:AkRegionFromMBBox([self MBBox])];
}

-(RMSphericalTrapezium)MBBox {
    return [[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox];
}

-(void)beginBackgroundCache:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg,NSDictionary);
    id<RMTileSource> source = [self mpTileSource];
    RMSphericalTrapezium region = MBBoxFromAkRegion([AkylasMapboxModule regionFromObject:arg]);
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


-(NSDictionary*)centerCoordinate
{
    RMSphericalTrapezium box = [[self getMPTileSourceForMapView:nil] latitudeLongitudeBoundingBox];
    
    return [AkylasMapboxModule dictFromLocation2D:RMSphericalTrapeziumCenter(box)];
}

-(id<RMTileSource>)mpSource
{
    id source = [self valueForKey:@"source"];
    if (!IS_OF_CLASS(source, NSString)) {
        if (IS_OF_CLASS(source, AkylasMapboxTileSourceProxy) && source != self) {
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
    CGFloat minZoom = [TiUtils floatValue:[self valueForKey:@"minZoom"] def:1.0f];
    CGFloat maxZoom = [TiUtils floatValue:[self valueForKey:@"maxZoom"] def:22.0f];
    id<RMTileSource> result = nil;
    if ([typeLower isEqualToString:@"websource"])
    {
        result = [[AkylasWebSource alloc] initWithDictionary:[self allProperties]];
    }
    else if ([typeLower isEqualToString:@"openstreetmap"])
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
        result = [[RMTileMillSource alloc] initWithHost:[TiUtils stringValue:[self valueForKey:@"host"]] mapName:name tileCacheKey:cacheKey minZoom:minZoom maxZoom:maxZoom];
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
        result.minZoom = minZoom;
        result.maxZoom = maxZoom;
        result.cacheable = [TiUtils boolValue:[self valueForKey:@"cacheable"] def:YES];
        if (IS_OF_CLASS(result, RMAbstractWebMapSource)) {
            ((RMAbstractWebMapSource*)result).userAgent = [TiUtils stringValue:[self valueForKey:@"userAgent"]];
        }
    }
    return [result autorelease];
}


#pragma mark Mapbox

-(id<RMTileSource>)getMPTileSourceForMapView:(RMMapView*)mapView
{
    if (_mpTileSource == nil) {
        _mpTileSource = [[self mpSource] retain];
        if (!_mpTileSource) return nil;
        [_mpTileSource setCacheable:self.cacheable];
    }
    _mapView = mapView;
    return _mpTileSource;
}


-(id<RMTileSource>)mpTileSource
{
    return _mpTileSource;
}

@end
