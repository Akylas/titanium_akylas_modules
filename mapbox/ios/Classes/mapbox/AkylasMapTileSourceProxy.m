#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapModule.h"
#import "AkylasTileSource.h"
#import "TiUtils.h"
#import <Mapbox/Mapbox.h>

@implementation AkylasMapTileSourceProxy
{
    AkylasTileSource* _tileSource;
    RMTileCache* _tileCache;
}
@synthesize caching;
#pragma mark Internal

-(id)initWithSource:(NSString*)source {
    if (self = [super init]) {
        [self setValue:source forKey:@"source"];
    }
    return self;
}

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
    return @"Akylas.Map.TileSource";
}

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
    return [AkylasMapModule dictFromRegion:[[self RMTileSource] latitudeLongitudeBoundingBox]];
}

-(NSNumber*)minZoom
{
    return @([[self RMTileSource] minZoom]);
}

-(NSNumber*)maxZoom
{
    return @([[self RMTileSource] maxZoom]);
}

-(NSDictionary*)centerCoordinate
{
    RMSphericalTrapezium box = [[self RMTileSource] latitudeLongitudeBoundingBox];
    
    return [AkylasMapModule dictFromLocation2D:RMSphericalTrapeziumCenter(box)];
}

-(void)beginBackgroundCache:(id)arg
{
	ENSURE_SINGLE_ARG_OR_NIL(arg,NSDictionary);
    id<RMTileSource> source = [[self tileSource] tileSource];
    RMSphericalTrapezium region = [AkylasMapModule regionFromDict:arg];
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


@end
