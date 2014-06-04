//
//  AkylasTileSource.h
//  MapBox
//
//  Created by Martin Guillon on 07/03/2014.
//
//

#import "AkylasMapTileSourceProxy.h"

@interface AkylasTileSource : NSObject
@property (nonatomic, retain) id<RMTileSource> tileSource;
@property (nonatomic, assign) AkylasMapTileSourceProxy *proxy;

+(AkylasTileSource*)tileSourceWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy;
+(AkylasMapTileSourceProxy*)tileSourceProxyWithSource:(id)source proxyForSourceURL:(TiProxy *)proxy;
@end
