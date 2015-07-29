//
//  AkylasGoogleMapRouteProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseRouteProxy.h"
@interface TIGMSPolyline : GMSPolyline
@property(nonatomic, strong) id userData;
@end

@class AkylasGMSMapView;
@interface AkylasGooglemapRouteProxy : AkylasMapBaseRouteProxy
-(GMSOverlay*)getGOverlayForMapView:(AkylasGMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
