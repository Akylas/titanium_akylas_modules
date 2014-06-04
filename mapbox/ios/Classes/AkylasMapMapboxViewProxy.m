//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapMapboxViewProxy.h"
#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapMapboxView.h"
#import "AkylasTileSource.h"

@implementation AkylasMapMapboxViewProxy

-(NSString*)apiName
{
    return @"AkylasMap.MapBoxView";
}


-(AkylasMapTileSourceProxy*)tileSourceFromArg:(id)arg
{
    AkylasMapTileSourceProxy *proxy = [self objectOfClass:[AkylasMapTileSourceProxy class] fromArg:arg];
	return proxy;
}

-(RMMapView*)getMap
{
    return [(AkylasMapMapboxView*)[self getOrCreateView] map];
}

-(RMTileCache*)getTileCache
{
    return [self getMap].tileCache;
}

-(void)cancelBackgroundCache:(id)arg
{
    [[self getTileCache] cancelBackgroundCache];
}

-(void)addTileSource:(id)args{
    AkylasMapMapboxView* mapView = (AkylasMapMapboxView*)[self getOrCreateView];
    if (mapView) {
		TiThreadPerformOnMainThread(^{
            id tileSourceProxy = nil;
            NSNumber* index = nil;
            ENSURE_ARG_AT_INDEX(tileSourceProxy, args, 0, NSObject);
            ENSURE_ARG_OR_NIL_AT_INDEX(index, args, 1, NSNumber);
            NSInteger realIndex = -1;
            if (index) {
                realIndex = [index integerValue];
            }
            [mapView addTileSource:[AkylasTileSource tileSourceProxyWithSource:tileSourceProxy proxyForSourceURL:self] atIndex:realIndex];
        }, YES);
        
    }
}

-(void)removeTileSource:(id)args{
    AkylasMapMapboxView* mapView = (AkylasMapMapboxView*)[self getOrCreateView];
    if (mapView) {
		TiThreadPerformOnMainThread(^{
            id tileSourceProxy = nil;
            NSNumber* index = nil;
            ENSURE_ARG_AT_INDEX(tileSourceProxy, args, 0, NSObject);
            [mapView removeTileSource:tileSourceProxy];
        }, YES);
        
    }
}

@end
