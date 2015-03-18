//
//  AkylasTileSource.h
//  MapBox
//
//  Created by Martin Guillon on 07/03/2014.
//
//

#import "AkylasMapTileSourceProxy.h"

@interface AkylasMapboxTileSource : NSObject
@property (nonatomic, retain) id<RMTileSource> tileSource;
@property (nonatomic, assign) AkylasMapTileSourceProxy *proxy;

+(AkylasMapboxTileSource*)tileSourceWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy;
+(AkylasMapTileSourceProxy*)tileSourceProxyWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy;
@end
