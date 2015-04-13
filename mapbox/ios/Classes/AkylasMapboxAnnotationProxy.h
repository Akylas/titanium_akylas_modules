//
//  AkylasGooglemapAnnotationProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseAnnotationProxy.h"

@class AkylasMapboxView;
@interface AkylasMapboxAnnotationProxy : AkylasMapBaseAnnotationProxy

@property (nonatomic, readonly) RMAnnotation *rmannotation;
-(RMAnnotation*)getRMAnnotationForMapView:(RMMapView*)mapView;
-(RMMapLayer*)shapeLayerForMapView:(AkylasMapboxView*)mapView;
-(RMMarker*)marker;
-(RMAnnotation*)getRMAnnotation;
@end
