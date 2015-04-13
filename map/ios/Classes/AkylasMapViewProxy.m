//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasMapViewProxy.h"
#import "AkylasMapAnnotationProxy.h"
#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapRouteProxy.h"
#import "AkylasMapCameraProxy.h"
#import "AkylasMapView.h"

@implementation AkylasMapViewProxy

-(NSString*)apiName
{
    return @"AkylasMap.View";
}


-(Class)annotationClass
{
    return [AkylasMapAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasMapRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasMapTileSourceProxy class];
}

-(AkylasMapCameraProxy*)camera
{
    return [(AkylasMapView *)[self view] camera];
}

-(void)animateCamera:(id)args
{
    [(AkylasMapView *)[self view] animateCamera:args];
}

@end
