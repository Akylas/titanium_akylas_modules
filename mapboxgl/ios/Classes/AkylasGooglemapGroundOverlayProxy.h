//
//  AkylasGooglemapGroundOverlayProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 09/05/2015.
//
//

#import "AkylasMapBaseGroundOverlayProxy.h"

@class AkylasGMSMapView;
@interface AkylasGooglemapGroundOverlayProxy : AkylasMapBaseGroundOverlayProxy
-(GMSOverlay*)getGOverlayForMapView:(AkylasGMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
