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
@interface AkylasGoogleMapTileSourceProxy : AkylasMapBaseTileSourceProxy

-(GMSTileLayer*)getGTileLayerForMapView:(GMSMapView*)mapView;
-(GMSTileLayer*)gTileLayer;

@end
