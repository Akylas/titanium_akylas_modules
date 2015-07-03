//
//  AkylasGooglemapAnnotationProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseAnnotationProxy.h"
#import "GClusterItem.h"

@class GMSOverlay;
@class GMSMapView;

@interface AkylasGooglemapAnnotationProxy : AkylasMapBaseAnnotationProxy<GClusterItem>

+(int)gZIndexDelta;

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
