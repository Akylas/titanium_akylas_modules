//
//  AkylasMapMapboxViewProxy.h
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapBaseViewProxy.h"

@class AkylasGMSMapView, GClusterManager, AkylasGooglemapClusterRenderer;
@interface AkylasGooglemapViewProxy : AkylasMapBaseViewProxy
-(AkylasGMSMapView*)map;
-(GClusterManager*)clusterManager;
-(AkylasGooglemapClusterRenderer*) clusterRenderer;
@end
