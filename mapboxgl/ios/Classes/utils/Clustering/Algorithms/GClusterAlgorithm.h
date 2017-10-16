#import <Foundation/Foundation.h>
#import "GClusterItem.h"
#import "GQTPointQuadTree.h"

@interface GClusterAlgorithm: NSObject
@property (nonatomic, strong) NSMutableArray *items;
@property (nonatomic, strong) GQTPointQuadTree *quadTree;
@property (nonatomic, readwrite, assign) CGFloat minZoom;
@property (nonatomic, readwrite, assign) CGFloat maxZoom;

- (void)addItem:(id <GClusterItem>) item inBounds:(MGLCoordinateBounds)bounds;
- (void)addItems:(NSArray *)items inBounds:(MGLCoordinateBounds)bounds;
- (void)removeItem:(id <GClusterItem>) item;
- (void)removeItems;
- (void)removeItemsFromMap:(MGLMapView*)mapView;
- (void)removeItemsNotInRectangle:(CGRect)rect;
- (void)hideItemsNotInBounds: (MGLCoordinateBounds)bounds forZoom:(CGFloat)zoom;

- (void)removeClusterItemsInSet:(NSSet *)set;
- (void)removeClusterItemsInSet:(NSSet *)set fromMap:(MGLMapView*)mapView;
- (BOOL)containsItem:(id <GClusterItem>) item;
- (NSSet*)getClusters:(float)zoom;
- (NSArray*)visibleItems;

@end
