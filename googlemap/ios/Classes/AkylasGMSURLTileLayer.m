//
//  AkylasGMSURLTileLayer.m
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//

#import "AkylasGMSURLTileLayer.h"
#import "AkylasGooglemapView.h"

#define HTTP_404_NOT_FOUND 404
@interface AkylasGMSURLTileLayer()
@property (nonatomic, copy) GMSTileURLConstructor urlBlock;

@end

@implementation AkylasGMSURLTileLayer
{
    NSOperationQueue* _queue;
    id<GMSTileReceiver>  _tileReceiver;
    NSString* _cacheKey;
}

@synthesize requestTimeoutSeconds;

- (id)initWithConstructor:(GMSTileURLConstructor)constructor
{
    if (!(self = [super init]))
        return nil;
    self.userAgent = nil;
    self.urlBlock = constructor;
    self.cacheable = YES;
    self.requestTimeoutSeconds = 15;
    self.minZoom = -1;
    self.maxZoom = -1;
    _queue = [NSOperationQueue new];
    [_queue setMaxConcurrentOperationCount:10];
    return self;
}

-(NSString*)cacheKey {
    return _cacheKey;
}

-(void)dealloc
{
    [_queue release];
    [_userAgent release];
    [_cacheKey release];
    [super dealloc];
}

-(BOOL)hasTileForX:(NSUInteger)x y:(NSUInteger)y zoom:(NSUInteger)zoom {
    return YES;
}

uint64_t TileKey(NSUInteger theX, NSUInteger theY, NSUInteger theZ)
{
    uint64_t zoom = (uint64_t)theZ & 0xFFLL; // 8bits, 256 levels
    uint64_t x = (uint64_t)theX & 0xFFFFFFFLL;  // 28 bits
    uint64_t y = (uint64_t)theY & 0xFFFFFFFLL;  // 28 bits
    
    uint64_t key = (zoom << 56) | (x << 28) | (y << 0);
    
    return key;
}

- (void)requestTileForX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom
               receiver:(id<GMSTileReceiver>)receiver
{
    
    // Return NSNull here so that the RMMapTiledLayerView will try to
    // fetch another tile if missingTilesDepth > 0
    if ( ! [self hasTileForX:x y:YES zoom:zoom])
    {
        [receiver receiveTileWithX:x y:y zoom:zoom image:kGMSTileLayerNoTile];
        return;
    }
    
    NSURL *url = self.urlBlock(x, y, zoom);
    
    if (!url)
    {
        [receiver receiveTileWithX:x y:y zoom:zoom image:kGMSTileLayerNoTile];
        return;
    }
    if (self.opacity == 0 || (_minZoom >= 0 && zoom < _minZoom)
         || (_maxZoom >= 0 && zoom > _maxZoom)) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:nil];
        return;
    }
    NSNumber* key = [NSNumber numberWithUnsignedLongLong:TileKey(x, y, zoom)];
    if (self.cacheable && [self.map isKindOfClass:[AkylasGMSMapView class]]) {
        TiCache* cache = ((AkylasGMSMapView*)self.map).tileCache;
        UIImage* image = [cache cachedImage:key withCacheKey:[self cacheKey]];
        if (image) {
            [receiver receiveTileWithX:x y:y zoom:zoom image:image];
            return;
        }
    }
    if (!((AkylasGMSMapView*)self.map).networkConnected) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:nil];
        return;
    }
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [request setTimeoutInterval:self.requestTimeoutSeconds];
    if (self.userAgent) {
        [request setValue:self.userAgent forHTTPHeaderField:@"User-Agent"];
    }

    [NSURLConnection sendAsynchronousRequest:request queue:_queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
        UIImage *image = nil;
        NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
        NSInteger responseStatusCode = [httpResponse statusCode];
        if (responseStatusCode == HTTP_404_NOT_FOUND ) {
            image = kGMSTileLayerNoTile;
            
        } else if (!error) {
            image = [UIImage imageWithData:data];
            if (image && self.cacheable && [self.map isKindOfClass:[AkylasGMSMapView class]]) {
                TiCache* cache = ((AkylasGMSMapView*)self.map).tileCache;
                [cache addImage:image forKey:key withCacheKey:[self cacheKey]];
            }
        }
        dispatch_async(dispatch_get_main_queue(), ^(void)
                       {
                           [receiver receiveTileWithX:x y:y zoom:zoom image:image];
                       });
        
    }];

}
@end
