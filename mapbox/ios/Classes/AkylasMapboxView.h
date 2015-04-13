
#import "AkylasMapBaseView.h"
#import "TiCache.h"

@protocol AkylasMapboxAnnotation
@required
-(NSString *)lastHitName;
@end

@interface AkylasMapboxView : AkylasMapBaseView<RMMapViewDelegate, RMTileCacheBackgroundDelegate>
{
}
-(RMMapView*)map;
@end
