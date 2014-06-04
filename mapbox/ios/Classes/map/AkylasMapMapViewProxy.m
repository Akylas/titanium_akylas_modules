/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasMapMapViewProxy.h"
#import "AkylasMapMapView.h"
#import "AkylasMapModule.h"
#import "AkylasMapRouteProxy.h"

@implementation AkylasMapMapViewProxy

#pragma mark Internal

-(NSString*)apiName
{
    return @"AkylasMap.MapView";
}

#pragma mark Public APIs iOS 7

-(id)camera
{
    [AkylasMapModule logAddedIniOS7Warning:@"camera"];
    return nil;
}

-(void)animateCamera:(id)args
{
    [AkylasMapModule logAddedIniOS7Warning:@"animateCamera()"];
}

-(void)showAnnotations:(id)args
{
    [AkylasMapModule logAddedIniOS7Warning:@"showAnnotations()"];
}

-(void)addTileSource:(id)args{

}

-(void)removeTileSource:(id)args{

}

@end
