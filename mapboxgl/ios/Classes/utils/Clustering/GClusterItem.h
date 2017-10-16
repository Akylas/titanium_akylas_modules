#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@class  GQuadItem;
@protocol GClusterItem <NSObject>

@property (nonatomic, assign, readonly) CLLocationCoordinate2D position;

@property (nonatomic, readonly) MGLPointAnnotation *marker;

@end
