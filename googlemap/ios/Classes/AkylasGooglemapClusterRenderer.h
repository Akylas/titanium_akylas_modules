//
//  AkylasGoogleMapClusterRenderer.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "GClusterRenderer.h"
@interface AkylasGooglemapClusterRenderer : NSObject <GClusterRenderer> 
- (id)initWithMapView:(GMSMapView*)googleMap;
-(void)clearCache;
-(void)clearCacheForId:(NSUInteger)theId;
@end
