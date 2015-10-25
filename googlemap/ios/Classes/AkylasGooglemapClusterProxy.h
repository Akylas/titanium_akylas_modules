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
#import "GridBasedAlgorithm.h"
#import "GStaticCluster.h"

@class AkylasGooglemapClusterProxy;

@interface AkylasClusterMarker:GMSMarker
@property (nonatomic, readwrite, retain) GStaticCluster* cluster;
@property (nonatomic, assign) BOOL selected;
@end

@interface AkylasClusterAlgorithm : GridBasedAlgorithm

@property (nonatomic, readonly) NSUInteger uniqueId;
@property (nonatomic, assign) BOOL visible;
@property (nonatomic, readwrite, assign) AkylasGooglemapClusterProxy* proxy;
@end


@interface AkylasGooglemapClusterProxy : AkylasMapBaseClusterProxy
@property (nonatomic, readwrite, assign) CGFloat maxDistance;
@property (nonatomic, readwrite, assign) BOOL showText;
@property (nonatomic, readwrite, assign) BOOL selectedShowText;

-(AkylasClusterAlgorithm*)algorithm;
-(NSUInteger) uniqueId;
-(GMSMarker*)createClusterMarker:(id <GCluster>)cluster;
-(void)cluster;
-(id)visibleAnnotations;
@end
