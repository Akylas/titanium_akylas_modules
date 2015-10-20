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
@class AkylasGMSMapView;

@interface AkylasGMSMarker : GMSMarker{
    
}
@end

@interface AkylasGooglemapAnnotationProxy : AkylasMapBaseAnnotationProxy<GClusterItem>

+(int)gZIndexDelta;
@property (nonatomic, readwrite, assign) BOOL appearAnimation;
@property (nonatomic, readonly, assign, getter=isSelected) BOOL selected;

-(GMSOverlay*)getGOverlayForMapView:(AkylasGMSMapView*)mapView;
-(GMSOverlay*)gOverlay;
-(void)removeFromMap;
@end
