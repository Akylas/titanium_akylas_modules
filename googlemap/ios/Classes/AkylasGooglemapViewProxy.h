//
//  AkylasMapMapboxViewProxy.h
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapBaseViewProxy.h"

@class GMSMapView, GClusterManager, AkylasGooglemapClusterRenderer;
@interface AkylasGooglemapViewProxy : AkylasMapBaseViewProxy
-(GMSMapView*)map;
-(GClusterManager*)clusterManager;
-(AkylasGooglemapClusterRenderer*) clusterRenderer;
@end
