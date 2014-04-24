/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiBase.h"
#import <MapKit/MapKit.h>
#import "AkylasMapboxView.h"

@interface AkylasMapboxImageAnnotationView : MKAnnotationView<AkylasMapboxAnnotation> {
@private
	
	NSString * lastHitName;
}

- (id)initWithAnnotation:(id <MKAnnotation>)annotation reuseIdentifier:(NSString *)reuseIdentifier map:(AkylasMapboxView*)map image:(UIImage*)image;
-(NSString *)lastHitName;

@end
