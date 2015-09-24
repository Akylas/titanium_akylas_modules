//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasGooglemapViewProxy.h"
#import "AkylasGooglemapAnnotationProxy.h"
#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasGooglemapRouteProxy.h"
#import "AkylasGooglemapGroundOverlayProxy.h"
#import "AkylasGooglemapClusterProxy.h"
#import "AkylasGooglemapView.h"

@implementation AkylasGooglemapViewProxy

-(NSString*)apiName
{
    return @"AkylasGoogleMap.View";
}

-(AkylasGMSMapView*)map
{
    return [(AkylasGooglemapView*)view map];
}
-(GClusterManager*)clusterManager
{
    return [(AkylasGooglemapView*)view clusterManager];
}
-(AkylasGooglemapClusterRenderer*) clusterRenderer
{
    return [(AkylasGooglemapView*)view clusterRenderer];
}

-(Class)annotationClass
{
    return [AkylasGooglemapAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasGooglemapRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasGooglemapTileSourceProxy class];
}


-(Class)groundOverlayClass
{
    return [AkylasGooglemapGroundOverlayProxy class];
}

-(Class)clusterClass
{
    return [AkylasGooglemapClusterProxy class];
}

-(id)coordinateForPoints:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSArray)
//    ENSURE_UI_THREAD_WAIT_1_ARG(args)
    AkylasGMSMapView* mapView = [self map];
    __block NSMutableArray* result;
    if (mapView) {
        result = [NSMutableArray array];
        TiThreadPerformBlockOnMainThread(^{
            GMSProjection* proj = [mapView projection];
            [args enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                CGPoint point = [TiUtils pointValue:obj];
                CLLocationCoordinate2D coords = [proj coordinateForPoint:point];
                [result addObject:@[@(coords.latitude), @(coords.longitude)]];
            }];
        }, YES);
    }
    return result;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

@end
