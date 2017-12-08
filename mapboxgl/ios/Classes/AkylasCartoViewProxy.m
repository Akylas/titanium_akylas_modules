//
//  AkylasMapMapboxViewProxy.m
//  AkylasMap
//
//  Created by Martin Guillon on 14/05/2014.
//
//

#import "AkylasCartoViewProxy.h"
#import "AkylasCartoAnnotationProxy.h"
#import "AkylasCartoTileSourceProxy.h"
#import "AkylasCartoRouteProxy.h"
#import "AkylasCartoGroundOverlayProxy.h"
#import "AkylasCartoClusterProxy.h"
#import "AkylasCartoView.h"

@implementation AkylasCartoViewProxy

-(NSString*)apiName
{
    return @"AkylasCarto.View";
}

-(AkylasCartoView*)map
{
    return [(AkylasCartoView*)view map];
}
-(GClusterManager*)clusterManager
{
    return [(AkylasCartoView*)view clusterManager];
}
-(AkylasCartoClusterRenderer*) clusterRenderer
{
    return [(AkylasCartoView*)view clusterRenderer];
}

-(Class)annotationClass
{
    return [AkylasCartoAnnotationProxy class];
}

-(Class)routeClass
{
    return [AkylasCartoRouteProxy class];
}

-(Class)tileSourceClass
{
    return [AkylasCartoTileSourceProxy class];
}


-(Class)groundOverlayClass
{
    return [AkylasCartoGroundOverlayProxy class];
}

-(Class)clusterClass
{
    return [AkylasCartoClusterProxy class];
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
