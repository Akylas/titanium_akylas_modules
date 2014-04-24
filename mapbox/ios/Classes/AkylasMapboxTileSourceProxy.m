#import "AkylasMapboxTileSourceProxy.h"
#import "AkylasMapboxModule.h"
#import "AkylasTileSource.h"
#import "TiUtils.h"

@implementation AkylasMapboxTileSourceProxy
{
    AkylasTileSource* _tileSource;
    RMTileCache* _tileCache;
}
@synthesize caching;
#pragma mark Internal

-(void)_configure
{
	[super _configure];
}

-(void)dealloc
{
    if (_tileSource != nil) {
        _tileSource.proxy = nil;
        RELEASE_TO_NIL(_tileSource);
    }
    RELEASE_TO_NIL(_tileCache);
	[super dealloc];
}

-(NSString*)apiName
{
    return @"Akylas.Mapbox.TileSource";
}

//-(NSMutableDictionary*)langConversionTable
//{
//    return [NSMutableDictionary dictionaryWithObjectsAndKeys:@"title",@"titleid",@"subtitle",@"subtitleid",nil];
//}


-(BOOL)caching
{
    return (_tileCache && [_tileCache isBackgroundCaching]);
}

-(AkylasTileSource*)tileSource
{
    if (_tileSource == nil) {
        _tileSource = [[AkylasTileSource tileSourceWithSource:[self valueForKey:@"source"] proxyForSourceURL:self] retain];
        _tileSource.proxy = self;
    }
    return _tileSource;
}

-(id<RMTileSource>)RMTileSource
{
    return [[self tileSource] tileSource];
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
    return [AkylasMapboxModule dictFromRegion:[[self RMTileSource] latitudeLongitudeBoundingBox]];
}

-(NSNumber*)minZoom
{
    return NUMFLOAT([[self RMTileSource] minZoom]);
}

-(NSNumber*)maxZoom
{
    return NUMFLOAT([[self RMTileSource] maxZoom]);
}


//-(NSDictionary*)centerCoordinate
//{
//    return [AkylasMapboxModule dictFromRegion:[[self tileSource] latitudeLongitudeBoundingBox]];
//}

-(void)beginBackgroundCache:(id)arg
{
	ENSURE_SINGLE_ARG_OR_NIL(arg,NSDictionary);
//    if (!_tileSource || !_tileSource.mapView) return;
    id<RMTileSource> source = [[self tileSource] tileSource];
    RMSphericalTrapezium region = [AkylasMapboxModule regionFromDict:arg];
    float minZoom = [TiUtils floatValue:@"minZoom" properties:arg def:source.minZoom];
    float maxZoom = [TiUtils floatValue:@"maxZoom" properties:arg def:source.maxZoom];
    if (!CLLocationCoordinate2DIsValid(region.southWest)  || !CLLocationCoordinate2DIsValid(region.northEast))
    {
        region = source.latitudeLongitudeBoundingBox;
    }
    [[self tileCache] beginBackgroundCacheForTileSource:source southWest:region.southWest northEast:region.northEast minZoom:minZoom maxZoom:maxZoom];

//    NSString* source
//    RMSphericalTrapezium region = [AkylasMapboxModule regionFromDict:arg];
    
}

- (void)tileCache:(RMTileCache *)tileCache didBeginBackgroundCacheWithCount:(int)tileCount forTileSource:(id <RMTileSource>)tileSource
{
    if ([self _hasListeners:@"cachebegin"])
	{
		[self fireEvent:@"cachebegin" withObject:@{@"tileCount":NUMINT(tileCount)} propagate:NO checkForListener:NO];
	}
}

- (void)tileCache:(RMTileCache *)tileCache didBackgroundCacheTile:(RMTile)tile withIndex:(int)tileIndex ofTotalTileCount:(int)totalTileCount;
{
    if ([self _hasListeners:@"cachedtile"])
	{
		[self fireEvent:@"cachedtile" withObject:@{@"tileIndex":NUMINT(tileIndex), @"totalTileCount":NUMINT(totalTileCount)} propagate:NO checkForListener:NO];
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


@end
