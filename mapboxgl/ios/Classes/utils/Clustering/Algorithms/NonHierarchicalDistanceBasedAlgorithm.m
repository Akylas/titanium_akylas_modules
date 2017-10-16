#import <GoogleMaps/GoogleMaps.h>
#import "NonHierarchicalDistanceBasedAlgorithm.h"
#import "GQTBounds.h"
#import "GQTPoint.h"
#import "GStaticCluster.h"
#import "GClusterManager.h"
#import "GQuadItem.h"

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 * <p/>
 * High level algorithm:<br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br>
 * 2. Create a cluster with the center of the item. <br>
 * 3. Add all items that are within a certain distance to the cluster. <br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br>
 * 5. Remove those items from the list of candidate clusters.
 * <p/>
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
@implementation NonHierarchicalDistanceBasedAlgorithm {
    NSInteger _maxDistanceAtZoom;
    NSInteger _minDistance;
}
@synthesize maxDistanceAtZoom = _maxDistanceAtZoom;
@synthesize minDistance = _minDistance;

- (id)initWithMaxDistanceAtZoom:(NSInteger)aMaxDistanceAtZoom {
    if (self = [self init]) {
        _maxDistanceAtZoom = aMaxDistanceAtZoom;
    }
    return self;
}

- (id)init {
    if (self = [super init]) {
        _minDistance = -1;
    }
    return self;
}

- (NSSet*)getClusters:(float)zoom {
    if ([self.items count] == 0) {
        return nil;
    }
    int discreteZoom = (int) zoom;
    
    double zoomSpecificSpan = _maxDistanceAtZoom / pow(2, discreteZoom) / 256;
    double zoomMinSpan = _minDistance / pow(2, discreteZoom) / 256;
    
    NSMutableSet *visitedCandidates = [[NSMutableSet alloc] init];
    NSMutableSet *results = [[NSMutableSet alloc] init];
    NSMutableDictionary *distanceToCluster = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *itemToCluster = [[NSMutableDictionary alloc] init];
    
    for (GQuadItem* candidate in self.items) {
        if (candidate.hidden || candidate.marker.opacity == 0) {
            candidate.marker.map =nil;
            continue;
        }
        
        if ([visitedCandidates containsObject:candidate]) {
            // Candidate is already part of another cluster.
            continue;
        }
        
        if (![candidate.marker canBeClustered]) {
            [results addObject:candidate];
            [visitedCandidates addObject:candidate];
            [distanceToCluster setObject:[NSNumber numberWithDouble:0] forKey:candidate];
            continue;
        }
        
        GQTBounds bounds = [self createBoundsFromSpan:candidate.point span:zoomSpecificSpan];
        NSArray *clusterItems  = [self.quadTree searchWithBounds:bounds];
        if ([clusterItems count] <= 1) {
            // Only the current marker is in range. Just add the single item to the results.
            [results addObject:candidate];
            [visitedCandidates addObject:candidate];
            [distanceToCluster setObject:[NSNumber numberWithDouble:0] forKey:candidate];
            continue;
        }
        
        GStaticCluster *cluster = [[GStaticCluster alloc] init];
        
        for (GQuadItem* clusterItem in clusterItems) {
            if (clusterItem.hidden || candidate.marker.opacity == 0) {
                clusterItem.marker.map =nil;
                continue;
            }
            if (![clusterItem.marker canBeClustered]) {
                continue;
            }
            NSNumber *existingDistance = [distanceToCluster objectForKey:clusterItem];
            double distance = [self distanceSquared:clusterItem.point :candidate.point];
            if (distance <= zoomMinSpan) {
                continue;
            }
            if (existingDistance != nil) {
                // Item already belongs to another cluster. Check if it's closer to this cluster.
                if ([existingDistance doubleValue] < distance) {
                    continue;
                }
                
                // Move item to the closer cluster.
                GStaticCluster *oldCluster = [itemToCluster objectForKey:clusterItem];
                [oldCluster remove:clusterItem];
                if (oldCluster.items.count <= 1) {
                    [results removeObject:oldCluster];
                    [results addObject:[oldCluster.items anyObject]];
                    [oldCluster removeAllItems];
                }
            }
            [distanceToCluster setObject:[NSNumber numberWithDouble:distance] forKey:clusterItem];
            [cluster add:clusterItem];
            clusterItem.marker.map =nil;
            [itemToCluster setObject:cluster forKey:clusterItem];
        }
        if (cluster.items.count <= 1) {
            [results addObject:candidate];
            [visitedCandidates addObject:candidate];
            [distanceToCluster setObject:[NSNumber numberWithDouble:0] forKey:candidate];
        } else {
            [results addObject:cluster];
            [cluster update];
            [visitedCandidates addObjectsFromArray:clusterItems];
        }
        
    }
    
    return results;
}

- (double)distanceSquared:(GQTPoint) a :(GQTPoint) b {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
}

- (GQTBounds) createBoundsFromSpan:(GQTPoint) point span:(double) span {
    double halfSpan = span / 2;
    GQTBounds bounds;
    bounds.minX = point.x - halfSpan;
    bounds.maxX = point.x + halfSpan;
    bounds.minY = point.y - halfSpan;
    bounds.maxY = point.y + halfSpan;

    return bounds;
}

@end
