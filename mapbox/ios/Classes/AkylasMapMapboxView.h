
#import "AkylasMapView.h"

@interface AkylasMapMapboxView : AkylasMapView<RMMapViewDelegate, RMTileCacheBackgroundDelegate>
{
}

-(RMMapView*)map;

#pragma mark Public APIs
- (BOOL)addTileSource:(AkylasMapTileSourceProxy*)tileSource;
- (BOOL)addTileSource:(AkylasMapTileSourceProxy*)tileSource atIndex:(NSInteger)index;
- (BOOL)removeTileSource:(AkylasMapTileSourceProxy*)tileSource;

@end
