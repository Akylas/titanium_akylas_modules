//
//  AkylasGooglemapGroundOverlayProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 09/05/2015.
//
//

#import "AkylasMapBaseGroundOverlayProxy.h"

@interface AkylasGooglemapGroundOverlayProxy : AkylasMapBaseGroundOverlayProxy
-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
