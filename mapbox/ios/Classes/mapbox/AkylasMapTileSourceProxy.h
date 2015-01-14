//
//  AkylasMapboxTileSourceProxy.h
//  MapBox
//
//  Created by Martin Guillon on 07/03/2014.
//
//

#import "TiProxy.h"

@class AkylasTileSource;
@interface AkylasMapTileSourceProxy : TiProxy<RMTileCacheBackgroundDelegate>
@property (nonatomic, readonly) BOOL caching;
@property (nonatomic, readonly) NSDictionary* region;
@property (nonatomic, readonly) NSNumber* minZoom;
@property (nonatomic, readonly) NSNumber* maxZoom;
-(AkylasTileSource*)tileSource;
-(id)initWithSource:(NSString*)source;

@end
