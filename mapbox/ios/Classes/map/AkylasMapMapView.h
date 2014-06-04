/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapView.h"
#import "AkylasMKOverlayPathUniversal.h"

@class AkylasMapAnnotationProxy;

@protocol AkylasMapAnnotation
@required
-(NSString *)lastHitName;
@end


@interface AkylasMapMapView : AkylasMapView<MKMapViewDelegate> {
	MKMapView *map;
    CFMutableDictionaryRef mapLine2View;   // MKPolyline(route line) -> MKPolylineView(route view)
}

@property (nonatomic, readonly) CLLocationDegrees longitudeDelta;
@property (nonatomic, readonly) CLLocationDegrees latitudeDelta;
@property (nonatomic, readonly) NSArray *customAnnotations;

#pragma mark Private APIs
-(MKMapView*)map;

#pragma mark Public APIs
-(void)firePinChangeDragState:(MKAnnotationView *) pinview newState:(MKAnnotationViewDragState)newState fromOldState:(MKAnnotationViewDragState)oldState;

#pragma mark Utils
-(void)addOverlay:(MKPolyline*)polyline level:(MKOverlayLevel)level;
@end


