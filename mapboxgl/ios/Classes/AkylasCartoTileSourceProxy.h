//
//  AkylasGoogleMapTileSourceProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseTileSourceProxy.h"

@class GMSTileLayer;
@class GMSMapView;
@interface AkylasCartoTileSourceProxy : AkylasMapBaseTileSourceProxy
@property (nonatomic, readwrite, assign) BOOL canChangeTileSize;
//@property (nonatomic, readwrite, copy) NSString* url;

-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView;
-(GMSTileLayer*)gTileLayer;

@end
