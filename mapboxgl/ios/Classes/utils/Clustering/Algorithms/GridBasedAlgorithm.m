#import <GoogleMaps/GoogleMaps.h>
#import "GridBasedAlgorithm.h"
#import "GQTBounds.h"
#import "GQTPoint.h"
#import "GStaticCluster.h"
#import "GClusterManager.h"
#import "GQuadItem.h"
#import "SphericalMercatorProjection.h"

@implementation GridBasedAlgorithm {
    NSInteger _gridSize;
}
@synthesize gridSize = _gridSize;

- (id)initWithGridSize:(NSInteger)aGridSize {
    if (self = [super init]) {
        _gridSize = aGridSize;
    }
    return self;
}
static NSUInteger getCoord(NSUInteger numCells, GQTPoint pos) {
    return (NSUInteger) (numCells * floor(pos.x) + floor(pos.y));
}

- (NSSet*)getClusters:(float)zoom {
    if ([self.items count] == 0) {
        return nil;
    }
    int discreteZoom = (int) zoom;
    
    long numCells = (long) ceil(256 * pow(2, zoom) / _gridSize);
    SphericalMercatorProjection* projection = [[SphericalMercatorProjection alloc] initWithWorldWidth:numCells];
    
    NSMutableSet *results = [[NSMutableSet alloc] init];
    NSMutableDictionary *clusters = [[NSMutableDictionary alloc] init];
    
    for (GQuadItem* candidate in self.items) {
        if (candidate.hidden || candidate.marker.opacity == 0) {
            candidate.marker.map =nil;
            continue;
        }
        
        if (![candidate.marker canBeClustered]) {
            [results addObject:candidate];
            continue;
        }
        
        NSUInteger coord = getCoord(numCells, [projection coordinateToPoint:candidate.position]);
        
        GStaticCluster* cluster = [clusters objectForKey:@(coord)];
        if (!cluster) {
            cluster = [[GStaticCluster alloc] init];
            [clusters setObject:cluster forKey:@(coord)];
        }
        [cluster add:candidate];
        candidate.marker.map =nil;
    }
    [clusters enumerateKeysAndObjectsUsingBlock:^(id key, GStaticCluster* cluster, BOOL *stop) {
        if (cluster.items.count > 1) {
            [results addObject:cluster];
            [cluster update];
        } else {
            [results addObject:[cluster.items anyObject]];
        }
    }];
    
    return results;
}

@end
