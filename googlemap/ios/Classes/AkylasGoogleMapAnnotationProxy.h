//
//  AkylasGooglemapAnnotationProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseAnnotationProxy.h"
@class GMSOverlay;
@class GMSMapView;
@interface AkylasGoogleMapAnnotationProxy : AkylasMapBaseAnnotationProxy

+(int)gZIndexDelta;

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
