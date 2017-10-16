#import "GClusterAlgorithm.h"

@interface GridBasedAlgorithm : GClusterAlgorithm


@property (nonatomic, readwrite, assign) NSInteger gridSize;

- (id)initWithGridSize:(NSInteger)gridSize;

@end
