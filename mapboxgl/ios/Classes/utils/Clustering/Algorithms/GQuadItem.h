#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "GQTPointQuadTreeItem.h"
#import "GClusterItem.h"
#import "GCluster.h"

@interface GQuadItem : NSObject <GCluster, GQTPointQuadTreeItem, NSCopying> 

- (id)initWithItem:(id <GClusterItem>)item;

@property(nonatomic, readonly) CLLocationCoordinate2D position;

@property (nonatomic,readonly) MGLPointAnnotation *marker;

/**
 * Controls whether this marker will be shown on map.
 */
@property(nonatomic, assign) BOOL hidden;

- (id <GClusterItem>)item;
-(void)updatePosition;
@end
