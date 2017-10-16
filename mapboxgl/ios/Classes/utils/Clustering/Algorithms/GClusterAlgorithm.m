#import <GoogleMaps/GoogleMaps.h>
#import "GQTBounds.h"
#import "GQTPoint.h"
#import "GStaticCluster.h"
#import "GClusterManager.h"
#import "GQuadItem.h"
#import "SphericalMercatorProjection.h"

@implementation GClusterAlgorithm {
}

- (id)init {
    if (self = [super init]) {
        _items = [[NSMutableArray alloc] init];
        _quadTree = [[GQTPointQuadTree alloc] initWithBounds:(GQTBounds){0,0,1,1}];
        self.minZoom = -1;
        self.maxZoom = -1;
    }
    return self;
}

- (void)addItem:(id <GClusterItem>) item inBounds:(GMSCoordinateBounds *)bounds {
    GQuadItem *quadItem = [[GQuadItem alloc] initWithItem:item];
    if (bounds && ![bounds containsCoordinate:quadItem.position ]) {
        quadItem.hidden = YES;
    }else{
        quadItem.hidden = NO;
    }
    [_items addObject:quadItem];
    [_quadTree add:quadItem];
}

- (void)addItems:(NSArray *)items inBounds:(GMSCoordinateBounds *)bounds {
    [items enumerateObjectsUsingBlock:^(id <GClusterItem> item, NSUInteger idx, BOOL *stop) {
        GQuadItem *quadItem = [[GQuadItem alloc] initWithItem:item];
        if (bounds && ![bounds containsCoordinate:quadItem.position ]) {
            quadItem.hidden = YES;
        }else{
            quadItem.hidden = NO;
        }
        [_items addObject:quadItem];
        [_quadTree add:quadItem];

    }];
}

- (void)removeItem:(id <GClusterItem>) item{
    NSSet *set = [NSSet setWithObject:item];
    [self removeClusterItemsInSet:set];
}
- (void)removeClusterItemsInSet:(NSSet *)set {
    [self removeClusterItemsInSet:set fromMap:nil];
}
-(BOOL)containsItem:(id <GClusterItem>) item{
    __block BOOL bFound = NO;
    [_items enumerateObjectsUsingBlock:^(GQuadItem *quadItem, NSUInteger idx, BOOL *stop) {
        if(quadItem.item == item){
            bFound =  YES;
            *stop = YES;
        }
    }];
    return bFound;
}
- (void)removeItems
{
    [self removeItemsFromMap:nil];
}

- (void)removeClusterItemsInSet:(NSSet *)set fromMap:(GMSMapView*)mapView {
    __block GMSMarker* selectedMarker = mapView.selectedMarker;
    NSMutableArray *toRemove = [NSMutableArray array];
    [_items enumerateObjectsUsingBlock:^(GQuadItem *quadItem, NSUInteger idx, BOOL *stop) {
        if ([set containsObject:quadItem.item]) {
            [toRemove addObject:quadItem];
        }
    }];
    [_items removeObjectsInArray:toRemove];
    [toRemove enumerateObjectsUsingBlock:^(GQuadItem *quadItem, NSUInteger idx, BOOL *stop) {
        [_quadTree remove:quadItem];
        if (selectedMarker && selectedMarker == quadItem.marker) {
            mapView.selectedMarker = nil;
            selectedMarker = nil;
        }
        quadItem.marker.map = nil;
    }];
}

- (void)removeItemsFromMap:(GMSMapView*)mapView
{
    GMSMarker* selectedMarker = mapView.selectedMarker;
    for (GQuadItem *item in _items) {
        if (selectedMarker && selectedMarker == item.marker) {
            mapView.selectedMarker = nil;
            selectedMarker = nil;
        }
        item.marker.map = nil;
    }
    [_items removeAllObjects];
    [_quadTree clear];
}

- (void)removeItemsNotInRectangle:(CGRect)rect
{
    NSMutableArray *newItems = [[NSMutableArray alloc] init];
    [_quadTree clear];
    
    for (GQuadItem *item in _items) {
        if (CGRectContainsPoint(rect, CGPointMake(item.position.latitude, item.position.longitude)))
        {
            [newItems addObject:item];
            [_quadTree add:item];
        }
    }
    
    _items = newItems;
}

- (void)hideItemsNotInBounds: (GMSCoordinateBounds *)bounds forZoom:(CGFloat)zoom
{
    BOOL zoomHidden =(self.maxZoom > 0 && zoom > self.maxZoom) || (self.minZoom > 0 && zoom < self.minZoom);
    for (GQuadItem *item in _items){
        if ((!item.marker.map || item.marker != item.marker.map.selectedMarker) && (zoomHidden || ![bounds containsCoordinate:item.position])) {
                item.hidden = YES;
                item.marker.map = nil;
        } else{
            item.hidden = NO;
        }
    }
}

- (NSArray*)visibleItems
{
//    NSMutableArray *items = [[NSMutableArray alloc] init];
//    
//    for (GQuadItem *item in _items) {
//        if (!item.hidden) {
//            [items addObject:item];
//        }
//    }
//    return items;
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"hidden == false"];
    return [_items filteredArrayUsingPredicate:predicate];
}

- (NSSet*)getClusters:(float)zoom {
    return [NSSet setWithArray:_items];
}

@end
