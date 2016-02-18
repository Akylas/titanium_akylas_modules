//
//  AkylasGMSURLTileLayer.h
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//


@interface AkylasGMSURLTileLayer : GMSTileLayer

/** The network timeout for each attempt to download a tile image. */
@property (nonatomic, assign) NSTimeInterval requestTimeoutSeconds;
@property (nonatomic, assign) CGFloat minZoom;
@property (nonatomic, assign) CGFloat maxZoom;
@property (nonatomic, assign) BOOL showTileAfterMaxZoom;
@property (nonatomic, assign) BOOL autoHd;
@property (nonatomic, assign) BOOL wmsFormat;

@property (nonatomic, readwrite, copy) NSString* cacheKey;
@property (nonatomic, readwrite, copy) NSString* userAgent;
@property (nonatomic, readwrite, copy) NSString* subdomains;
@property (nonatomic, readwrite, copy) NSString* url;
@property (nonatomic, readwrite, copy) NSString* timeFormat;
@property (nonatomic, readwrite, assign) UIImage* errorImage;

@property (nonatomic, assign) BOOL cacheable;
- (id)initWithConstructor:(GMSTileURLConstructor)constructor;
-(void)getImageForTileX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom
              comletion:(void(^)(UIImage*)) completion;
-(NSURL*)getURLForTileX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom;
-(BOOL)hasTileForX:(NSUInteger)x y:(NSUInteger)y zoom:(NSUInteger)zoom;
@end
