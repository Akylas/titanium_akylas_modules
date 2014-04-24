//
//  AkylasTileSource.h
//  MapBox
//
//  Created by Martin Guillon on 07/03/2014.
//
//

#import "AkylasMapboxTileSourceProxy.h"

@interface AkylasTileSource : NSObject
@property (nonatomic, retain) id<RMTileSource> tileSource;
@property (nonatomic, assign) AkylasMapboxTileSourceProxy *proxy;

+tileSourceWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy;
@end
