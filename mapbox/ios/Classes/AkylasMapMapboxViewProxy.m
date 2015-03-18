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
#import "AkylasMapboxTileSource.h"

@implementation AkylasMapMapboxViewProxy

-(NSString*)apiName
{
    return @"AkylasMap.MapBoxView";
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

@end
