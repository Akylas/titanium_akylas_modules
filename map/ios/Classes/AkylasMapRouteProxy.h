//
//  AkylasGoogleMapRouteProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseRouteProxy.h"
#import "AkylasMKOverlayPathUniversal.h"

@class AkylasMapView;
@interface AkylasMapRouteProxy : AkylasMapBaseRouteProxy
@property (nonatomic, readonly) NSUInteger level;
@property (nonatomic, readonly) id <AkylasMKOverlayPathUniversal> routeRenderer;


-(id <AkylasMKOverlayPathUniversal>)rendererForMapView:(AkylasMapView*)mapView;
-(MKPolyline*) getPolyline;
@end
