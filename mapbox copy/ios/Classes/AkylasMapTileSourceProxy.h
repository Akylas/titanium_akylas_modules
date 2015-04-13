//
//  AkylasMapboxTileSourceProxy.h
//  MapBox
//
//  Created by Martin Guillon on 07/03/2014.
//
//

#import "TiProxy.h"

@class GMSTileLayer;
@class GMSMapView;
@interface AkylasMapTileSourceProxy : TiProxy<RMTileCacheBackgroundDelegate>
@property (nonatomic, readonly) BOOL caching;
@property (nonatomic, readonly) NSDictionary* region;
@property (nonatomic, readonly) NSNumber* minZoom;
@property (nonatomic, readonly) NSNumber* maxZoom;

@property (nonatomic, assign) BOOL cacheable;
@property (nonatomic, assign) BOOL fadeIn;
@property (nonatomic, assign) NSInteger zIndex;
@property (nonatomic, assign) CGFloat opacity;
@property (nonatomic, assign) NSInteger tileSize;

-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView;
-(GMSTileLayer*)gTileLayer;

-(id<RMTileSource>)getMPTileSourceForMapView:(RMMapView*)mapView;
-(id<RMTileSource>)mpTileSource;

@end
