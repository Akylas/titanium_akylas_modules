#import "GClusterManager.h"
#import "GCluster.h"
#import <objc/runtime.h>

NSString * const kGMSMarkerClusterKey = @"kCanCluster";
@implementation MGLPointAnnotation(CanCluster)
- (void)setCanBeClustered:(BOOL )value
{
    objc_setAssociatedObject(self, (__bridge const void *)(kGMSMarkerClusterKey), @(value), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (BOOL)canBeClustered
{
    id value = objc_getAssociatedObject(self, (__bridge const void *)(kGMSMarkerClusterKey));
    if (value) {
        return [value boolValue];
    }
    return true;
}

@end

@implementation GClusterManager {
    CGFloat _zoom;
    NSMutableSet* _clusterAlgorithmSet;
}

- (void)setMapView:(MGLMapView*)mapView {
    _zoom = -1;
    _mapView = mapView;
}


- (void)addClusterAlgorithm:(GClusterAlgorithm*)algo {
    if (_clusterAlgorithmSet == nil) {
        _clusterAlgorithmSet = [NSMutableSet set];
    }
    [_clusterAlgorithmSet addObject:algo];
    [self clusterAlgo:algo];
}

- (void)removeClusterAlgorithm:(GClusterAlgorithm*)clusterAlgorithm {
    if (_clusterAlgorithmSet != nil) {
        [_clusterAlgorithmSet removeObject:clusterAlgorithm];
    }
}


- (void)setClusterRenderer:(id <GClusterRenderer>)clusterRenderer {
    _zoom = -1;
    _clusterRenderer = clusterRenderer;
}

- (void)addItem:(id <GClusterItem>) item inAlgorithm:(GClusterAlgorithm*)algo {
    [algo addItem:item inBounds:[self visibleBounds]];
}

- (void)addItems:(NSArray*) items inAlgorithm:(GClusterAlgorithm*)algo {
    [algo addItems:items inBounds:[self visibleBounds]];
}

- (void)removeItem:(id <GClusterItem>) item fromAlgorithm:(GClusterAlgorithm*)algo {
    [algo removeItem:item];
}

-(BOOL)containsItem:(id<GClusterItem>)item inAlgorithm:(GClusterAlgorithm*)algo{
    return [algo containsItem:item];
}

- (void)removeItems {
    [_clusterAlgorithmSet enumerateObjectsUsingBlock:^(GClusterAlgorithm* set, BOOL *stop) {
        [set removeItems];
    }];
    [_clusterRenderer clearCache];
}

- (void)removeItemsNotInRectangle:(CGRect)rect {
    [_clusterAlgorithmSet enumerateObjectsUsingBlock:^(GClusterAlgorithm* set, BOOL *stop) {
        [set removeItemsNotInRectangle:rect];
    }];
}

-(void)clusterAlgo:(GClusterAlgorithm*)clusterAlgorithm
{
    [_clusterRenderer clustersChanged:clusterAlgorithm forZoom:_zoom];
}

-(MGLCoordinateBounds)visibleBounds
{
    MGLCoordinateBounds bounds = self.mapView.visibleCoordinateBounds;
    CLLocationCoordinate2D northEast = bounds.northEast;
    CLLocationCoordinate2D southWest = bounds.southWest;
    CGFloat width_2 = (northEast.longitude - southWest.longitude)/2;
    CGFloat height_2 = (northEast.latitude - southWest.latitude)/2;
//    CGFloat width_2 = 0;
//    CGFloat height_2 = 0;
//    bounds = [[GMSCoordinateBounds alloc]initWithCoordinate:
//              CLLocationCoordinate2DMake(northEast.latitude + height_2, northEast.longitude + width_2)
//                                                 coordinate:
//              CLLocationCoordinate2DMake(southWest.latitude - height_2, southWest.longitude - width_2)];
    return MGLCoordinateBounds {};
}

- (void)hideItemsNotInVisibleBounds{
   
    GMSCoordinateBounds* bounds = [self visibleBounds];
    [_clusterAlgorithmSet enumerateObjectsUsingBlock:^(GClusterAlgorithm* set, BOOL *stop) {
        [set hideItemsNotInBounds:bounds forZoom:_zoom];
    }];
    [self cluster];
}

- (void)cluster {
    _zoom = floorf(self.mapView.camera.zoom);
    [_clusterAlgorithmSet enumerateObjectsUsingBlock:^(GClusterAlgorithm* set, BOOL *stop) {
        [self clusterAlgo:set];
    }];
}


#pragma mark mapview delegate

- (void)mapView:(GMSMapView *)mapView didChangeCameraPosition:(GMSCameraPosition *)cameraPosition {
    

    CGFloat newZoom = floorf([cameraPosition zoom]);
    if (_zoom != newZoom) {
        _zoom = newZoom;
        [self hideItemsNotInVisibleBounds];
    } else {
        [NSObject cancelPreviousPerformRequestsWithTarget:self
                                                 selector:@selector(hideItemsNotInVisibleBounds) object:nil];
        [self performSelector:@selector(hideItemsNotInVisibleBounds) withObject:nil afterDelay:0.3];
    }
}

- (void)mapView:(GMSMapView *)mapView idleAtCameraPosition:(GMSCameraPosition *)cameraPosition {
    assert(mapView == _mapView);
    
    // Don't re-compute clusters if the map has just been panned/tilted/rotated.
    CGFloat newZoom = floorf([cameraPosition zoom]);
    if (_zoom != newZoom) {
        _zoom = newZoom;
        
        [self cluster];
    }
}

#pragma mark convenience

+ (instancetype)managerWithMapView:(GMSMapView*)googleMap
                          renderer:(id<GClusterRenderer>)renderer {
    GClusterManager *mgr = [[[self class] alloc] init];
    if(mgr) {
        mgr.mapView = googleMap;
        mgr.clusterRenderer = renderer;
    }
    return mgr;
}

@end
