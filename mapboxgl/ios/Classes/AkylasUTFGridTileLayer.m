//
//  AkylasUTFGridTileLayer.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 02/02/2016.
//
//

#import "AkylasUTFGridTileLayer.h"
#import "CLLocation+measuring.h"
#import "TiUtils.h"
#import "AkylasGooglemapModule.h"
#import "AkylasGooglemapView.h"

int utfDecode(int c) {
    if (c >= 93) {
        c--;
    }
    if (c >= 35) {
        c--;
    }
    return c - 32;
}

@interface AkylasGMSURLTileLayer()
{
    @protected
    NSOperationQueue* _queue;
    id<GMSTileReceiver>  _tileReceiver;
    NSString* _cacheKey;
    NSDateFormatter * _dateFormat;
}
@end

@implementation AkylasUTFGridTileLayer
{
    UIImage* returnImage;
//    NSURLSessionConfiguration *sessionConfiguration;
    NSURLCache* cache;
}

- (id)initWithConstructor:(GMSTileURLConstructor)constructor
{
    if (!(self = [super initWithConstructor:constructor]))
        return nil;
    
    cache = [[NSURLCache alloc] initWithMemoryCapacity:0
                                         diskCapacity:100 * 1024 * 1024
                                             diskPath:@"akylas.utfgrid"];
//    sessionConfiguration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    
    self.resolution = 4;
    return self;
}

-(void)dealloc
{
    [returnImage release];
    [cache release];
//    [sessionConfiguration release];
    [super dealloc];
}


-(NSString*)getDataForTileX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom
              cacheOnly:(BOOL)cacheOnly
              completion:(void(^)(NSString*)) completion
{
    NSURL *theUrl = [self getURLForTileX:x y:y zoom:zoom];
    
    if (!theUrl)
    {
        if (completion) {
            completion(nil);
        }
        return;
    }
    
    
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:theUrl cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:self.requestTimeoutSeconds];
    if (cacheOnly || [AkylasGooglemapModule sharedInstance].offlineMode || !((AkylasGMSMapView*)self.map).networkConnected)  {
        NSString* result = nil;
        NSCachedURLResponse* response = [cache cachedResponseForRequest:request];
        if (response) {
            result = [[NSString alloc] initWithData:[response data] encoding:NSUTF8StringEncoding];
        }
        if (completion) {
            completion(result);
        }
        if (!cacheOnly || result) {
            return result;
        } else {
            NSLog(@"test cache was removed")
        }
    }

    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    
    [request setTimeoutInterval:self.requestTimeoutSeconds];
    if (self.userAgent) {
        [request setValue:self.userAgent forHTTPHeaderField:@"User-Agent"];
    }

    NSURLSessionDataTask *task = [session dataTaskWithRequest:request
                                            completionHandler:
                                  ^(NSData *data, NSURLResponse *response, NSError *error) {
//                                      NSLog(@"currentDiskUsage %d",[cache currentDiskUsage]);  // logs 0 bytes
//                                      NSLog(@"currentMemoryUsage %d",[cache currentMemoryUsage]);  // logs 0 bytes
                                      NSHTTPURLResponse* httpResponse = (NSHTTPURLResponse*)response;
                                      NSInteger responseStatusCode = [httpResponse statusCode];
                                      NSString* result = nil;
                                      if (responseStatusCode >= 400 && responseStatusCode < 500) {
                                          if (completion) {
                                              completion(result);
                                          }
                                      } else if (!error) {
                                          result = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                                          NSCachedURLResponse* cachedResponse = [[NSCachedURLResponse alloc]
                                                            initWithResponse:httpResponse
                                                            data:data
                                                            userInfo:nil
                                                            storagePolicy:NSURLCacheStorageAllowed];
                                          [cache storeCachedResponse:cachedResponse forRequest:request];
                                          [cachedResponse release];
                                      }
                                      if (completion) {
                                          completion([result autorelease]);
                                      }
                                  }];
    
    [task resume];
    return nil;
}

- (void)requestTileForX:(NSUInteger)x
                      y:(NSUInteger)y
                   zoom:(NSUInteger)zoom
               receiver:(id<GMSTileReceiver>)receiver
{
    if ( ! [self hasTileForX:x y:YES zoom:zoom])
    {
        [receiver receiveTileWithX:x y:y zoom:zoom image:kGMSTileLayerNoTile];
        return;
    }
    if (self.minZoom >= 0 && zoom < self.minZoom) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:self.errorImage];
        return;
    }
    if (self.maxZoom >= 0 && zoom > self.maxZoom) {
        [receiver receiveTileWithX:x y:y zoom:zoom image:self.errorImage];
    } else {
        
        [self getDataForTileX:x y:y zoom:zoom cacheOnly:false completion:^(NSString * data) {
            dispatch_async(dispatch_get_main_queue(), ^(void)
                           {
                               if (data) {
//                                   UIImage * image = nil;
//                                   if (!image) {
//                                       UIFont *font = [UIFont boldSystemFontOfSize:15];
//                                       UIGraphicsBeginImageContext(CGSizeMake(self.tileSize, self.tileSize));
//                                       CGContextStrokeRect(UIGraphicsGetCurrentContext(), CGRectMake(0, 0, self.tileSize, self.tileSize));
//                                       
//                                       [[NSString stringWithFormat:@"%lu,%lu,%lu",x,(unsigned long)y,(unsigned long)zoom] drawInRect:CGRectMake(0,0,self.tileSize,self.tileSize) withFont:font];
//                                       
//                                       image = [UIGraphicsGetImageFromCurrentImageContext() retain];
//                                       UIGraphicsEndImageContext();
//                                       
//                                   }
//                                   [receiver receiveTileWithX:x y:y zoom:zoom image:image];
//                                   [image release];
                                   
                                   if (!returnImage) {
                                       UIGraphicsBeginImageContext(CGSizeMake(self.tileSize, self.tileSize));
                                       returnImage = [UIGraphicsGetImageFromCurrentImageContext() retain];
                                       UIGraphicsEndImageContext();
                                   }
                                   [receiver receiveTileWithX:x y:y zoom:zoom image:returnImage];
                               } else {
                                   [receiver receiveTileWithX:x y:y zoom:zoom image:nil];
                               }
                               
                           });
        }];
    }
}


-(id)getData: (CLLocationCoordinate2D)latlng atZoom:(NSUInteger) zoom {
    NSUInteger realZoom = zoom + 1; // because we are on a retina device
    BOOL passedMax = self.maxZoom >= 0 && realZoom > self.maxZoom;
    if (passedMax && !self.showTileAfterMaxZoom) {
        return nil;
    }
    double lat_rad = latlng.latitude * kDegreesToRadians;
    int n = (int) pow(2, realZoom);
    double xfloat = (latlng.longitude + 180.0) / 360.0 * n;
    double yfloat = (1.0 - log(tan(lat_rad) + (1 / cos(lat_rad))) / M_PI) / 2.0 * n;
    if (passedMax) {
        float currentTileDepth = realZoom - self.maxZoom;
        xfloat = xfloat / pow(2.0, currentTileDepth);
        yfloat = yfloat / pow(2.0, currentTileDepth);
        realZoom = (int) self.maxZoom;
    }
    int x = (int) floor(xfloat);
    int y = (int) floor(yfloat);
    
    
    int gridX = (int) ((xfloat - x) * self.tileSize / self.resolution);
    int gridY = (int) ((yfloat - y) * self.tileSize / self.resolution);
//    NSLog(@"%lu,%lu,%lu, %d, %d",x,(unsigned long)y,(unsigned long)realZoom, gridX, gridY)
    NSString* gridData = [self getDataForTileX:x y:y zoom:realZoom cacheOnly:YES completion:nil];
    if (gridData != nil) {
        NSError *error = nil;
        id result = [TiUtils jsonParse:gridData error:&error];
        if (!error && IS_OF_CLASS(result, NSDictionary)) {
            @try {
                unichar theChar = [[TiUtils stringValue:[[result objectForKey:@"grid"] objectAtIndex:gridY]] characterAtIndex:gridX];
//                NSLog(@"theChar %C %d",theChar, (int)theChar)
                NSInteger idx = utfDecode((int)theChar); //converting to int is really important
                NSString* key  = [TiUtils stringValue:[[result objectForKey:@"keys"] objectAtIndex:idx]];
//                NSLog(@"idx %lu,",idx)
//                NSLog(@"key %@,",key)
                return [[result objectForKey:@"data"] objectForKey:key];
            }
            @catch (NSException * e) {
                return nil;
            }
            
        }
    }
    return nil;
}
@end
