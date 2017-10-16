#import "GClusterAlgorithm.h"

@interface NonHierarchicalDistanceBasedAlgorithm : GClusterAlgorithm

@property (nonatomic, readwrite, assign) NSInteger maxDistanceAtZoom;
@property (nonatomic, readwrite, assign) NSInteger minDistance;

- (id)initWithMaxDistanceAtZoom:(NSInteger)maxDistanceAtZoom;

@end
