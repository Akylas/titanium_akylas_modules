;//
//  AkylasGoogleMapClusterRenderer.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasGooglemapClusterRenderer.h"
#import "GQuadItem.h"
#import "GCluster.h"
#import "GClusterAlgorithm.h"
#import "GStaticCluster.h"
#import "AkylasGooglemapClusterProxy.h"

@implementation AkylasGooglemapClusterRenderer {
    GMSMapView *_map;
    NSMutableDictionary *_markerCache;
}

- (id)initWithMapView:(GMSMapView*)googleMap {
    if (self = [super init]) {
        _map = googleMap;
        _markerCache = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void)clustersChanged:(AkylasClusterAlgorithm*)algo forZoom:(CGFloat)zoom {
    NSUInteger theId = algo.uniqueId;
    [self clearCacheForId:theId];
    if (zoom == -1) {
        return;
    }
    NSSet* clusters = [algo getClusters:zoom];

    @synchronized(_markerCache) {
        for (id <GCluster> cluster in clusters) {
            GMSMarker *marker = cluster.marker;
            if (!marker && [cluster isKindOfClass:[GStaticCluster class]]) {
                marker = [algo.proxy createClusterMarker:cluster];
                NSMutableArray* cache = [_markerCache objectForKey:@(theId)];
                if (!cache) {
                    cache = [NSMutableArray array];
                    [_markerCache setObject:cache forKey:@(theId)];
                }
                [cache addObject:marker];
            } else {
                marker.appearAnimation =  kGMSMarkerAnimationNone;
            }
            
            marker.map = _map;
        }
    }
}

-(void)clearCacheForId:(NSUInteger)theId {
    @synchronized(_markerCache) {

        NSMutableArray* cache = [_markerCache objectForKey:@(theId)];
        if (cache) {
            for (GMSMarker *marker in cache) {
                if (_map.selectedMarker == marker) {
                    _map.selectedMarker = nil;
                }
                marker.map = nil;
            }
            [cache removeAllObjects];
        }
    }
}

-(void)clearCache {
    @synchronized(_markerCache) {
        for (NSMutableArray *cache in _markerCache) {
            for (GMSMarker *marker in cache) {
                if (_map.selectedMarker == marker) {
                    _map.selectedMarker = nil;
                }
                marker.map = nil;
            }
        }
        [_markerCache removeAllObjects];
    }
}
@end
