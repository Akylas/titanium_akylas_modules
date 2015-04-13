//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasGoogleMapViewProxy.h"
#import "AkylasGoogleMapAnnotationProxy.h"
#import "AkylasGoogleMapTileSourceProxy.h"
#import "AkylasGoogleMapRouteProxy.h"
#import "AkylasGoogleMapView.h"

@implementation AkylasGoogleMapViewProxy

-(NSString*)apiName
{
    return @"AkylasGoogleMap.View";
}

-(GMSMapView*)getMap
{
    return [(AkylasGoogleMapView*)[self getOrCreateView] map];
}


-(Class)annotationClass
{
    return [AkylasGoogleMapAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasGoogleMapRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasGoogleMapTileSourceProxy class];
}

@end
