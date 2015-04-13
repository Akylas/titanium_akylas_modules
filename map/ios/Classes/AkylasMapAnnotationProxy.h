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
@interface AkylasMapAnnotationProxy : AkylasMapBaseAnnotationProxy<MKAnnotation>
@property (nonatomic, readwrite, assign) MKPinAnnotationColor pinColor;
@property (nonatomic, readwrite, retain) MKAnnotationView *annView;

+(int)gZIndexDelta;

-(GMSOverlay*)getGOverlayForMapView:(GMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
@end
