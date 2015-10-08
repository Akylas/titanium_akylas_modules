//
//  AkylasGMSURLTileLayer.m
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//

#import "AkylasGMSURLTileLayer.h"
#import "AkylasGooglemapView.h"
#import "AkylasGooglemapModule.h"

#define HTTP_404_NOT_FOUND 404
@interface AkylasGMSURLTileLayer()
@property (nonatomic, copy) GMSTileURLConstructor urlBlock;

@end

@interface NSString (ReplaceExtensions)
- (NSString *)stringByReplacingStringsFromDictionary:(NSDictionary *)dict;
@end

@implementation NSString (ReplaceExtensions)
- (NSString *)stringByReplacingStringsFromDictionary:(NSDictionary *)dict
{
    NSMutableString *string = [self mutableCopy];
    for (NSString *target in dict) {
        id object = [dict objectForKey:target];
        [string replaceOccurrencesOfString:target withString:(IS_OF_CLASS(object, NSString)?object:[object stringValue])
                                   options:0 range:NSMakeRange(0, [string length])];
    }
    return [string autorelease];
}
@end

@implementation AkylasGMSURLTileLayer
{
    NSOperationQueue* _queue;
    id<GMSTileReceiver>  _tileReceiver;
    NSString* _cacheKey;
    NSDateFormatter * _dateFormat;
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
    self.subdomains = @"abc";
    self.timeFormat = @"yyyy-MM-dd";
    self.showTileAfterMaxZoom = YES;
    _dateFormat = [[NSDateFormatter alloc] init];
    [_dateFormat setDateFormat:self.timeFormat];
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
    [_dateFormat release];
    [_subdomains release];
    [_timeFormat release];
    RELEASE_TO_NIL(_errorImage);
    [super dealloc];
}


-(void)setTimeFormat:(NSString *)timeFormat
{
    _timeFormat = [timeFormat copy];
    [_dateFormat setDateFormat:_timeFormat];

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

-(NSString*)getSubdomainForX:(NSUInteger)x
                           y:(NSUInteger)y {
    NSInteger index = (x + y) % [self.subdomains length];
    return [self.subdomains substringWithRange:NSMakeRange(index, 1)];
}

-(void)getImageForTileX:(NSUInteger)x
                     y:(NSUInteger)y
                  zoom:(NSUInteger)zoom
              comletion:(void(^)(UIImage*)) completion
{
    
    NSString* urlString = self.url;
    if (self.urlBlock) {
        urlString = [self.urlBlock(x, y, zoom) absoluteString];
    }
    NSMutableDictionary* args = [NSMutableDictionary dictionaryWithObjectsAndKeys:@(x), @"{x}",
                                 @(y), @"{y}",
                                 [_dateFormat stringFromDate:[NSDate date]], @"{time}",
                                 @(zoom), @"{z}", nil];
    if (self.subdomains) {
        [args setObject:[self getSubdomainForX:x y:y] forKey:@"{s}"];
    }
    
    urlString = [urlString stringByReplacingStringsFromDictionary:args];
    
    
    //    NSLog(@"%@ %@", self.cacheKey, urlString)
    
    NSURL *theUrl = [NSURL URLWithString:urlString];
    
    if (!theUrl)
    {
        completion(kGMSTileLayerNoTile);
        return;
    }
    NSNumber* key = [NSNumber numberWithUnsignedLongLong:TileKey(x, y, zoom)];
    if ([self.map isKindOfClass:[AkylasGMSMapView class]]) {
        if (self.cacheable) {
            TiCache* cache = ((AkylasGMSMapView*)self.map).tileCache;
            UIImage* image = [cache cachedImage:key withCacheKey:[self cacheKey]];
            if (image) {
                completion(image);
                return;
            }
        }
        if ([AkylasGooglemapModule sharedInstance].offlineMode || !((AkylasGMSMapView*)self.map).networkConnected) {
            completion(nil);
            return;
        }
    }
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:theUrl];
    [request setCachePolicy:NSURLRequestReloadIgnoringLocalCacheData];
    [request setTimeoutInterval:self.requestTimeoutSeconds];
    if (self.userAgent) {
        [request setValue:self.userAgent forHTTPHeaderField:@"User-Agent"];
    }
    
    [NSURLConnection sendAsynchronousRequest:request queue:_queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *error) {
        UIImage *image = self.errorImage;
        NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
        NSInteger responseStatusCode = [httpResponse statusCode];
        if (responseStatusCode >= 400 && responseStatusCode < 500) {
            image = kGMSTileLayerNoTile;
            
        } else if (!error) {
            image = [UIImage imageWithData:data];
            //https://c.tile.openstreetmap.org/13/4226/2940.png
            //            if (zoom == 13 && x == 4226 && y == 2940) {
            //                NSString* filePath = @"/Volumes/data/dev/titanium/Akylas/akylas.mapme/Resources/images/tilesnew";
            //                filePath = [filePath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.png", self.cacheKey]];
            //                NSData *imageData = UIImagePNGRepresentation(image);
            //                [imageData writeToFile:filePath atomically:YES];
            //            }
            if (image && self.cacheable && [self.map isKindOfClass:[AkylasGMSMapView class]]) {
                TiCache* cache = ((AkylasGMSMapView*)self.map).tileCache;
                [cache addImage:image forKey:key withCacheKey:[self cacheKey]];
            }
        }
        completion(image);
//        dispatch_async(dispatch_get_main_queue(), ^(void)
//                       {
//                           [receiver receiveTileWithX:x y:y zoom:zoom image:image];
//                       });
        
    }];
    

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
    if (self.opacity == 0) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:nil];
        return;
    }
    if (_minZoom >= 0 && zoom < _minZoom) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:self.errorImage];
        return;
    }
    if (_maxZoom >= 0 && zoom > _maxZoom) {
        if (!self.showTileAfterMaxZoom)
        {
            [receiver receiveTileWithX:x y:y zoom:zoom image:self.errorImage];
            return;
        }
        else
        {
            NSUInteger currentTileDepth = zoom - _maxZoom;
                float nextX = x / powf(2.0, (float)currentTileDepth),
                nextY = y / powf(2.0, (float)currentTileDepth);
                float nextTileX = floor(nextX),
                nextTileY = floor(nextY);
            
            [self getImageForTileX:nextTileX y:nextTileY zoom:_maxZoom comletion:^(UIImage * tileImage) {
                float cropSize = 1.0 / powf(2.0, (float)currentTileDepth);
                CGRect cropBounds = CGRectMake(tileImage.size.width * (nextX - nextTileX),
                                               tileImage.size.height * (nextY - nextTileY),
                                               tileImage.size.width * cropSize,
                                               tileImage.size.height * cropSize);

                CGImageRef imageRef = CGImageCreateWithImageInRect([tileImage CGImage], cropBounds);
                [receiver receiveTileWithX:x y:y zoom:zoom image:[UIImage imageWithCGImage:imageRef]];
                CGImageRelease(imageRef);
            }];
        }

    } else {
        [self getImageForTileX:x y:y zoom:zoom comletion:^(UIImage * image) {
            dispatch_async(dispatch_get_main_queue(), ^(void)
                           {
                               [receiver receiveTileWithX:x y:y zoom:zoom image:image];
                           });
        }];
    }
}
@end
