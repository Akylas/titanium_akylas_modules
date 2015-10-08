/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasGooglemapModule.h"
#import "AkylasGooglemapTileSourceProxy.h"
#import "AkylasGooglemapViewProxy.h"
#import "AkylasGooglemapRouteProxy.h"
#import "AkylasGooglemapAnnotationProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#import "JRSwizzle.h"
#import <objc/runtime.h>

#define MODULE_ID @"akylas.googlemap"
#define REGEX @"(?!"  MODULE_ID  @"/)(GoogleMaps)\\.bundle$"
#define TOADD @"modules/"  MODULE_ID
#define TEMPLATE TOADD @"/$0"

GMSCoordinateBounds* boundsFromRegion(AkRegion trapez)
{
    return [[[GMSCoordinateBounds alloc] initWithCoordinate:trapez.northEast coordinate:trapez.southWest] autorelease];
}

//@interface NSURLConnection (InspectionDelegates)
//+ (NSMutableSet *)inspectedDelegates;
//@end
//
//@implementation NSURLConnection (InspectionDelegates)
//
//static NSMutableSet *s_delegates = nil;
//
//+ (NSMutableSet *)inspectedDelegates
//{
//    if (! s_delegates)
//        s_delegates = [[NSMutableSet alloc] init];
//    return s_delegates;
//}
//
//@end

@interface InspectedConnectionDelegate : NSObject <NSURLConnectionDelegate, NSURLConnectionDataDelegate>
//@property (nonatomic, strong) NSMutableData *received;
@property (nonatomic, strong) id <NSURLConnectionDelegate> actualDelegate;
//@property (nonatomic, strong) NSURLResponse *response;
@end


@implementation InspectedConnectionDelegate
@synthesize actualDelegate;

- (id) initWithActualDelegate:(id <NSURLConnectionDelegate>)actual
{
    self = [super init];
    if (self) {
//        self.received = [[NSMutableData alloc] init];
//        [self.received setLength:0];
        self.actualDelegate = actual;
//        self.response = nil;
    }
    return self;
}

- (void) cleanup:(NSError *)error
{
//    NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
//    if (self.response)
//        [userInfo setObject:self.response forKey:@"response"];
//    if (self.received.length > 0)
//        [userInfo setObject:self.received forKey:@"body"];
//    if (error)
//        [userInfo setObject:error forKey:@"error"];
//    
//    self.response = nil;
//    self.received = nil;
    self.actualDelegate = nil;
//    [[NSURLConnection inspectedDelegates] removeObject:self];
}

// ------------------------------------------------------------------------
//
#pragma mark NSURLConnectionDelegate
- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:didFailWithError:)])
        [self.actualDelegate connection:connection didFailWithError:error];
    
    [self cleanup:error];
}

// ------------------------------------------------------------------------
#pragma mark NSURLConnectionDataDelegate
//
- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)aResponse
{
//    self.response = aResponse;
    NSURL* url  =connection.originalRequest.URL;
    if ([AkylasGooglemapModule sharedInstance].offlineMode && [url.absoluteString containsString:@"mmap"] || [url.absoluteString containsString:@"geosdk"]) {
        //        URL = nil;
//        cachePolicy = NSURLRequestReturnCacheDataDontLoad;
        if ([self.actualDelegate respondsToSelector:@selector(connection:didFailWithError:)]) {
            [self.actualDelegate connection:connection didFailWithError:[NSError errorWithDomain:NSURLErrorDomain
                                                                                            code:NSURLErrorNetworkConnectionLost
                                                                                        userInfo:nil]];
        }
    } else {
        if ([self.actualDelegate respondsToSelector:@selector(connection:didReceiveResponse:)]) {
            id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
            [actual connection:connection didReceiveResponse:aResponse];
        }

    }
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:didReceiveData:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        [actual connection:connection didReceiveData:data];
    }
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    if ([self.actualDelegate respondsToSelector:@selector(connectionDidFinishLoading:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        [actual connectionDidFinishLoading:connection];
    }
    
    [self cleanup:nil];
}

- (void)connection:(NSURLConnection *)connection didSendBodyData:(NSInteger)bytesWritten totalBytesWritten:(NSInteger)totalBytesWritten totalBytesExpectedToWrite:(NSInteger)totalBytesExpectedToWrite
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:didSendBodyData:totalBytesWritten:totalBytesExpectedToWrite:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        [actual connection:connection didSendBodyData:bytesWritten totalBytesWritten:totalBytesWritten totalBytesExpectedToWrite:totalBytesExpectedToWrite];
    }
}

- (NSURLRequest *)connection:(NSURLConnection *)connection willSendRequest:(NSURLRequest *)request redirectResponse:(NSURLResponse *)redirectResponse
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:willSendRequest:redirectResponse:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        return [actual connection:connection willSendRequest:request redirectResponse:redirectResponse];
    }
    return request;
}

- (NSInputStream *)connection:(NSURLConnection *)connection needNewBodyStream:(NSURLRequest *)request
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:needNewBodyStream:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        return [actual connection:connection needNewBodyStream:request];
    }
    return nil;
}

- (NSCachedURLResponse *)connection:(NSURLConnection *)connection willCacheResponse:(NSCachedURLResponse *)cachedResponse
{
    if ([self.actualDelegate respondsToSelector:@selector(connection:willCacheResponse:)]) {
        id <NSURLConnectionDataDelegate> actual = (id <NSURLConnectionDataDelegate>)self.actualDelegate;
        return [actual connection:connection willCacheResponse:cachedResponse];
    }
    return cachedResponse;
}

@end

@implementation NSURLConnection (Inspected)

#pragma mark -
#pragma mark Instance method swizzling

- (id)inspected_initWithRequest:(NSURLRequest *)request delegate:(id < NSURLConnectionDelegate >)delegate
{
    InspectedConnectionDelegate *inspectedDelegate = [[InspectedConnectionDelegate alloc] initWithActualDelegate:delegate];
    return [self inspected_initWithRequest:request delegate:inspectedDelegate];
}

- (id)inspected_initWithRequest:(NSURLRequest *)request delegate:(id < NSURLConnectionDelegate >)delegate startImmediately:(BOOL)startImmediately
{
    InspectedConnectionDelegate *inspectedDelegate = [[InspectedConnectionDelegate alloc] initWithActualDelegate:delegate];
    return [self inspected_initWithRequest:request delegate:inspectedDelegate startImmediately:startImmediately];
}

// ------------------------------------------------------------------------
#pragma mark -
#pragma mark Method swizzling magics.
+ (void) swizzleClassMethod:(SEL)from to:(SEL)to
{
    NSError *error = nil;
    BOOL swizzled = [NSURLConnection jr_swizzleClassMethod:from withClassMethod:to error:&error];
    if (!swizzled || error) {
        NSLog(@"Failed in replacing method: %@", error);
    }
}

+ (void) swizzleMethod:(SEL)from to:(SEL)to
{
    NSError *error = nil;
    BOOL swizzled = [NSURLConnection jr_swizzleMethod:from withMethod:to error:&error];
    if (!swizzled || error) {
        NSLog(@"Failed in replacing method: %@", error);
    }
}

+ (void) setupSwizzle
{
#define inspected_method(method) inspected_##method
#define swizzle_method_wrap(method) [NSURLConnection swizzleMethod:@selector(method) to:@selector(inspected_method(method))]
    
    swizzle_method_wrap(initWithRequest:delegate:);
    swizzle_method_wrap(initWithRequest:delegate:startImmediately:);
    
#undef swizzle_method_wrap
#undef inspected_method
}

@end


//@implementation NSURLRequest (UserAgentFix)
//+ (void) swizzleMethod:(SEL)from to:(SEL)to
//{
//    NSError *error = nil;
//    BOOL swizzled = [NSURLRequest jr_swizzleMethod:from withMethod:to error:&error];
//    if (!swizzled || error) {
//        NSLog(@"Failed in replacing method: %@", error);
//    }
//}
//
//
//+(void)setupSwizzle
//{
//    [self swizzleMethod:@selector(initWithURL:cachePolicy:timeoutInterval:)
//                     to:@selector(initWithURL2:cachePolicy:timeoutInterval:)];
//}
//-(id)initWithURL2:(NSURL *)URL cachePolicy:(NSURLRequestCachePolicy)cachePolicy timeoutInterval:(NSTimeInterval)timeoutInterval
//{
//    if ([AkylasGooglemapModule sharedInstance].offlineMode && [URL.absoluteString containsString:@"mmap"] || [URL.absoluteString containsString:@"geosdk"]) {
////        URL = nil;
//        cachePolicy = NSURLRequestReturnCacheDataDontLoad;
//    }
//
//    self = [self initWithURL2:URL cachePolicy:cachePolicy timeoutInterval:timeoutInterval];
//    
//    return self;
//}
//@end

@implementation NSBundle (GoogleMapsFix)

+ (void) swizzle
{
    [NSBundle jr_swizzleMethod:@selector(initWithPath:) withMethod:@selector(initWithCorrectedPath:) error:nil];
//    [NSBundle jr_swizzleMethod:@selector(URLForResource:withExtension:subdirectory:inBundleWithURL:) withMethod:@selector(URLForResource:withExtension:subdirectory:inBundleWithCorrectedURL:) error:nil];
//    [NSBundle jr_swizzleMethod:@selector(URLsForResourcesWithExtension:subdirectory:inBundleWithURL:) withMethod:@selector(URLsForResourcesWithExtension:subdirectory:inBundleWithCorrectedURL:) error:nil];
}

+(NSString*)fixPath:(NSString *)path
{
    if ([path rangeOfString:TOADD].location != NSNotFound) {
        return path;
    }
    NSError *error = nil;
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:REGEX options:NSRegularExpressionCaseInsensitive error:&error];
    NSString *modifiedString = [regex stringByReplacingMatchesInString:path options:0 range:NSMakeRange(0, [path length]) withTemplate:TEMPLATE];
    return modifiedString;
}

+(NSURL*)fixURL:(NSURL *)url
{
    return [NSURL URLWithString:[NSBundle fixPath:url.absoluteString]];
}

- (instancetype)initWithCorrectedPath:(NSString *)path {
    return [self initWithCorrectedPath:[NSBundle fixPath:path]];
}
//+ (NSURL *)URLForResource:(NSString *)name withExtension:(NSString *)ext subdirectory:(NSString *)subpath inBundleWithCorrectedURL:(NSURL *)bundleURL{
//    return [self URLForResource:name withExtension:ext subdirectory:subpath inBundleWithCorrectedURL:[NSBundle fixURL:bundleURL]];
//}
//+ (NSArray *)URLsForResourcesWithExtension:(NSString *)ext subdirectory:(NSString *)subpath inBundleWithCorrectedURL:(NSURL *)bundleURL
//{
//    return [self URLsForResourcesWithExtension:ext subdirectory:subpath inBundleWithCorrectedURL:[NSBundle fixURL:bundleURL]];
//}
@end

@implementation AkylasGooglemapModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"4c80e438-6315-4936-b3fe-42525f7f46fb";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return MODULE_ID;
}

#pragma mark Lifecycle

-(void)startup
{
    [NSBundle swizzle];
//    [NSURLRequest setupSwizzle];
    [NSURLConnection setupSwizzle];
    // this method is called when the module is first loaded
	// you *must* call the superclass
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Googlemap.View", [AkylasGooglemapViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Googlemap.Annotation", [AkylasGooglemapAnnotationProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Googlemap.TileSource", [AkylasGooglemapTileSourceProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Googlemap.Route", [AkylasGooglemapRouteProxy class]);
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

#pragma mark Public APIs


//-(void)setMapboxAccessToken:(id)value
//{
////    [[RMConfiguration configuration] setAccessToken:[TiUtils stringValue:value]];
//    [self replaceValue:value forKey:@"mapboxAccessToken" notification:NO];
//}

-(void)setOfflineMode:(BOOL)offlineMode
{
    [super setOfflineMode:offlineMode];
}

-(void)setGoogleMapAPIKey:(id)value
{
    ENSURE_UI_THREAD_1_ARG(value)
    [GMSServices  provideAPIKey:[TiUtils stringValue:value]];
    static GMSServices * services;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        services = [[GMSServices sharedServices] retain];
    });
    [self replaceValue:value forKey:@"googleMapAPIKey" notification:NO];
}


-(id)googleMapLicenses
{
    return [GMSServices openSourceLicenseInfo];
}


-(id)googleMapSDKVersion
{
    return [GMSServices SDKVersion];
}


@end
