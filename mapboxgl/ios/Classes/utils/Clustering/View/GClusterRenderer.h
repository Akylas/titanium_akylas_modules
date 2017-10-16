//
//  ClusterRenderer.h
//  Parkingmobility
//
//  Created by Colin Edwards on 1/18/14.
//  Copyright (c) 2014 Colin Edwards. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GClusterAlgorithm.h"

@protocol GClusterRenderer <NSObject>

- (void)clustersChanged:(GClusterAlgorithm*)algo forZoom:(CGFloat)zoom;
-(void)clearCache;

@end
