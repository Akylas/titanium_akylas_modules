//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasGooglemapViewProxy.h"
#import "AkylasGooglemapAnnotationProxy.h"
#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasGooglemapRouteProxy.h"
#import "AkylasGooglemapView.h"

@implementation AkylasGooglemapViewProxy

-(NSString*)apiName
{
    return @"AkylasGoogleMap.View";
}

-(GMSMapView*)getMap
{
    return [(AkylasGooglemapView*)[self getOrCreateView] map];
}


-(Class)annotationClass
{
    return [AkylasGooglemapAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasGooglemapRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasGooglemapTileSourceProxy class];
}

@end
