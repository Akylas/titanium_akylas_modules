//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapGoogleMapViewProxy.h"
#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapGoogleMapView.h"

@implementation AkylasMapGoogleMapViewProxy

-(NSString*)apiName
{
    return @"AkylasMap.GoogleMapView";
}

-(AkylasMapGoogleMapView*)getMap
{
    return [(AkylasMapGoogleMapView*)[self getOrCreateView] map];
}

@end
