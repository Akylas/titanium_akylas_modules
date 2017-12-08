//
//  AkylasMapMapboxViewProxy.h
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapBaseViewProxy.h"

@class AkylasGMSMapView, GClusterManager, AkylasCartoClusterRenderer, AkylasCartoView;
@interface AkylasCartoViewProxy : AkylasMapBaseViewProxy
-(AkylasCartoView*)map;
-(GClusterManager*)clusterManager;
-(AkylasCartoClusterRenderer*) clusterRenderer;
@end
