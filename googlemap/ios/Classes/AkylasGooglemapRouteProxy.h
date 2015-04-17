//
//  AkylasGoogleMapRouteProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseRouteProxy.h"

@interface AkylasGooglemapRouteProxy : AkylasMapBaseRouteProxy
-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
