//
//  AkylasGooglemapClusterSetProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasMapBaseClusterProxy.h"
#import "GClusterAlgorithm.h"
#import "NonHierarchicalDistanceBasedAlgorithm.h"
#import "GCluster.h"


@class AkylasGooglemapClusterProxy;
@interface AkylasClusterAlgorithm : NonHierarchicalDistanceBasedAlgorithm

@property (nonatomic, readonly) NSUInteger uniqueId;
@property (nonatomic, readwrite, assign) AkylasGooglemapClusterProxy* proxy;
@end


@interface AkylasGooglemapClusterProxy : AkylasMapBaseClusterProxy
@property (nonatomic, readwrite, assign) CGFloat maxDistance;
@property (nonatomic, readwrite, assign) BOOL showText;
@property (nonatomic, readwrite, retain) UIColor *color;

-(id<GClusterAlgorithm>)algorithm;
-(NSUInteger) uniqueId;
-(GMSMarker*)createClusterMarker:(id <GCluster>)cluster;
-(void)cluster;
@end
