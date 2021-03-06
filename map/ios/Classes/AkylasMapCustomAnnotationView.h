/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import <MapKit/MapKit.h>
#import "AkylasMapView.h"
#import "TiViewProxy.h"

@interface AkylasMapCustomAnnotationView : MKAnnotationView<AkylasMapAnnotation> {
@private
    NSString * lastHitName;
    TiViewProxy* theProxy;
    UIView* wrapperView;
}
@property (nonatomic, assign) SMCalloutView* calloutView;

- (id)initWithAnnotation:(id <MKAnnotation>)annotation reuseIdentifier:(NSString *)reuseIdentifier map:(AkylasMapView*)map;
- (NSString *)lastHitName;
- (void)setProxy:(TiViewProxy*)pinView;

@end
