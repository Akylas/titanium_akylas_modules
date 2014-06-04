//
//  RMMapView+BundlePath.m
//  MapBox
//
//  Created by Martin Guillon on 28/04/2014.
//
//

#import "RMMapView+BundlePath.h"

@implementation RMMapView (BundlePath)


+ (NSString *)pathForBundleResourceNamed:(NSString *)name ofType:(NSString *)extension
{
    NSString *bundlePath      = [[NSBundle mainBundle] pathForResource:@"modules/akylas.mapbox/Mapbox" ofType:@"bundle"];
//    NSAssert(bundlePath, @"Resource bundle not found in application.");
    
    NSBundle *resourcesBundle = [NSBundle bundleWithPath:bundlePath];
    
    return [resourcesBundle pathForResource:name ofType:extension];
}
@end
