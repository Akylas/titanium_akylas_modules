//
//  AkylasGoogleMapTileSourceProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseTileSourceProxy.h"

@interface AkylasMapboxTileSourceProxy : AkylasMapBaseTileSourceProxy<RMTileCacheBackgroundDelegate>

-(id<RMTileSource>)getMPTileSourceForMapView:(RMMapView*)mapView;
-(id<RMTileSource>)mpTileSource;

@end
