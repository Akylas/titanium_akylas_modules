/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import "AkylasMapMapView.h"

@interface AkylasMapImageAnnotationView : MKAnnotationView<AkylasMapAnnotation> {
@private
	
	NSString * lastHitName;
}
@property (nonatomic, assign) SMCalloutView* calloutView;

- (id)initWithAnnotation:(id <MKAnnotation>)annotation reuseIdentifier:(NSString *)reuseIdentifier map:(AkylasMapMapView*)map image:(UIImage*)image;
-(NSString *)lastHitName;

@end
