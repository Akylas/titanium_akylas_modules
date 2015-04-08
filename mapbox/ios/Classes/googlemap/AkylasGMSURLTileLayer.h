//
//  AkylasGMSURLTileLayer.h
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//

#import <GoogleMaps/GoogleMaps.h>

@interface AkylasGMSURLTileLayer : GMSTileLayer

/** The network timeout for each attempt to download a tile image. */
@property (nonatomic, assign) NSTimeInterval requestTimeoutSeconds;

@property (nonatomic, readwrite, copy) NSString* userAgent;

@property (nonatomic, assign) BOOL cacheable;
- (id)initWithConstructor:(GMSTileURLConstructor)constructor;
@end
