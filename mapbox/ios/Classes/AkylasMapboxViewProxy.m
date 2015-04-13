//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapboxViewProxy.h"
#import "AkylasMapboxAnnotationProxy.h"
#import "AkylasMapboxTileSourceProxy.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxView.h"

@implementation AkylasMapboxViewProxy

-(NSString*)apiName
{
    return @"Akylas.Mapbox.View";
}

-(RMMapView*)getMap
{
    return [(AkylasMapboxView*)[self getOrCreateView] map];
}


-(Class)annotationClass
{
    return [AkylasMapboxAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasMapboxRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasMapboxTileSourceProxy class];
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
