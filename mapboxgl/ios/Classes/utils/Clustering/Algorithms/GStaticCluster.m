#import "GStaticCluster.h"

@implementation GStaticCluster {
    CLLocationCoordinate2D _position;
    NSMutableSet *_items;
    MGLCoordinateBounds _bounds;
}

- (id)init{
    if (self = [super init]) {
        _items = [[NSMutableSet alloc] init];
    }
    return self;
}

- (void)add:(GQuadItem*)item {
    [_items addObject:item];
}

-(GMSMarker*)marker
{
    return nil;
}

- (void)remove:(GQuadItem*)item {
    [_items removeObject:item];
}
- (NSSet*)items {
    return _items;
}
- (NSInteger)count {
    return _items.count;
}

- (GMSCoordinateBounds*)bounds {
    return _bounds;
}


- (void)removeAllItems {
    [_items removeAllObjects];
}
- (CLLocationCoordinate2D)position {
    return _position;
}



-(void)update {
    
    _bounds = nil;
    GMSCoordinateBounds* bounds = nil;
    _position = kCLLocationCoordinate2DInvalid;
    for (GQuadItem* item in _items) {
        if (!bounds) {
            bounds = [[GMSCoordinateBounds alloc] initWithCoordinate:item.position coordinate:item.position];
        } else {
            bounds = [bounds includingCoordinate:item.position];
        }
    }
    if ([bounds isValid]) {
        _bounds = bounds;
        _position = CLLocationCoordinate2DMake(
                                               (bounds.southWest.latitude + bounds.northEast.latitude) / 2,
                                               (bounds.southWest.longitude + bounds.northEast.longitude) / 2);
    }
    
}
@end
