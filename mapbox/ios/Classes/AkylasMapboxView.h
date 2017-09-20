
#import "AkylasMapBaseView.h"
#import "TiCache.h"

@protocol AkylasMapboxAnnotation
@required
-(NSString *)lastHitName;
@end

@interface AkylasMapboxView : AkylasMapBaseView<MGLMapViewDelegate, MGLOfflineStorageDelegate>
{
}
-(MGLMapView*)map;
@end
