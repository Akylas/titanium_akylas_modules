//
//  AkylasGoogleMapRouteProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseRouteProxy.h"
@interface TINTLine : NTLine
@property(nonatomic, strong) id userData;
@end

@class AkylasGMSMapView;
@interface AkylasCartoRouteProxy : AkylasMapBaseRouteProxy
-(NTVectorElement*)getGOverlayForMapView:(AkylasGMSMapView*)mapView;
-(NTVectorElement*)gOverlay;
//-(GMSPath*)gPath;
@end
