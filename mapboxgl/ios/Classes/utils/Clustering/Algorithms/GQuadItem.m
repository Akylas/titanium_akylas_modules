#import "GQuadItem.h"
#import "SphericalMercatorProjection.h"

SphericalMercatorProjection* sProjection;
@implementation GQuadItem{
    id <GClusterItem> _item;
    GQTPoint _point;
}


+(SphericalMercatorProjection*)projection {
    if (!sProjection) {
        sProjection = [[SphericalMercatorProjection alloc] initWithWorldWidth:1];
    }
    return sProjection;
}

- (id)initWithItem:(id <GClusterItem>)clusterItem {
    if (self = [super init]) {

        _item = clusterItem;
        [self updatePosition];
    }
    return self;
}

- (GQTPoint)point {
    return _point;
}

- (id <GClusterItem>)item {
    return _item;
}

- (GMSMarker*)marker {
    return _item.marker;
}

-(void)updatePosition {
    _point = [[GQuadItem projection] coordinateToPoint:_item.position];
}


- (id)copyWithZone:(NSZone *)zone {
    GQuadItem *newGQuadItem = [[self class] allocWithZone:zone];
    newGQuadItem->_point = _point;
    newGQuadItem->_item = _item;
    return newGQuadItem;
}

- (CLLocationCoordinate2D)position {
    return _item.position;
}

- (NSSet*)items {
    return [NSSet setWithObject:_item];
}

- (BOOL)isEqualToQuadItem:(GQuadItem *)other {
    return [_item isEqual:other->_item]
            && _point.x == other->_point.x
            && _point.y == other->_point.y;
}

#pragma mark - NSObject

- (BOOL)isEqual:(id)other {
    if (other == self)
        return YES;
    if (!other || ![[other class] isEqual:[self class]])
        return NO;

    return [self isEqualToQuadItem:other];
}

- (NSUInteger)hash {
    return [_item hash];
}

@end
