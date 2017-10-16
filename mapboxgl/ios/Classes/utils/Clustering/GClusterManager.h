#import <Foundation/Foundation.h>
#import "GClusterAlgorithm.h"
#import "GClusterRenderer.h"
#import "GQTPointQuadTreeItem.h"

@interface MGLPointAnnotation(CanCluster)
- (void)setCanBeClustered:(BOOL)value;
- (BOOL)canBeClustered;
@end

@interface GClusterManager : NSObject <MGLMapViewDelegate>

@property(nonatomic, strong) MGLMapView *mapView;
@property(nonatomic, weak) id<MGLMapViewDelegate> delegate;
//@property(nonatomic, strong) id<GClusterAlgorithm> clusterAlgorithm;
@property(nonatomic, strong) id<GClusterRenderer> clusterRenderer;
@property(nonatomic, strong) NSMutableArray *items;

- (void)addItem:(id <GClusterItem>) item inAlgorithm:(GClusterAlgorithm*)algo;
- (void)addItems:(NSArray*) items inAlgorithm:(GClusterAlgorithm*)algo;
- (void)removeItems;
- (void)removeItemsNotInRectangle:(CGRect)rect;
- (void)hideItemsNotInVisibleBounds;

- (void)removeItem:(id <GClusterItem>) item fromAlgorithm:(GClusterAlgorithm*)algo;
-(BOOL)containsItem:(id<GClusterItem>)item inAlgorithm:(GClusterAlgorithm*)algo;
- (void)cluster;
-(void)clusterAlgo:(GClusterAlgorithm*)clusterAlgorithm;

- (void)addClusterAlgorithm:(GClusterAlgorithm*)clusterAlgorithm;
- (void)removeClusterAlgorithm:(GClusterAlgorithm*)clusterAlgorithm;

+ (instancetype)managerWithMapView:(MGLMapView*)googleMap
                          renderer:(id<GClusterRenderer>)renderer;

- (void)mapView:(MGLMapView *)mapView didChangeCameraPosition:(MGLMapCamera *)cameraPosition;
- (void)mapView:(MGLMapView *)mapView idleAtCameraPosition:(MGLMapCamera *)cameraPosition;
@end
