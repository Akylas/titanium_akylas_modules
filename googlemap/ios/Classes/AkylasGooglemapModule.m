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
