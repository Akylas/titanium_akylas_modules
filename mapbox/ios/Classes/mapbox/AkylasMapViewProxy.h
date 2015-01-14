/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
 
#import "TiViewProxy.h"
#import "AkylasMapAnnotationProxy.h"

@interface AkylasMapViewProxy : TiViewProxy {
	
}


-(AkylasMapAnnotationProxy*)annotationFromArg:(id)arg;

-(void)addAnnotation:(id)args;
-(void)addAnnotations:(id)args;
-(void)removeAnnotation:(id)args;
-(void)removeAnnotations:(id)args;
-(void)removeAllAnnotations:(id)args;
-(void)selectAnnotation:(id)args;
-(void)deselectAnnotation:(id)args;
-(void)addRoute:(id)args;
-(void)removeRoute:(id)args;

@end
