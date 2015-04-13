/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasGoogleMapModule.h"
#import "AkylasGoogleMapTileSourceProxy.h"
#import "AkylasGoogleMapViewProxy.h"
#import "AkylasGoogleMapRouteProxy.h"
#import "AkylasGoogleMapAnnotationProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#import "JRSwizzle.h"
#import <objc/runtime.h>

#define MODULE_ID @"akylas.googleMap"
#define REGEX @"(?!"  MODULE_ID  @"/)(GoogleMaps)\\.bundle$"
#define TEMPLATE @"modules/"  MODULE_ID  @"/$0"

@implementation NSBundle (GoogleMapsFix)

+ (void) swizzle
{
    [NSBundle jr_swizzleMethod:@selector(initWithPath:) withMethod:@selector(initWithCorrectedPath:) error:nil];
}

-(NSString*)fixPath:(NSString *)path
{
    NSError *error = nil;
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:REGEX options:NSRegularExpressionCaseInsensitive error:&error];
    NSString *modifiedString = [regex stringByReplacingMatchesInString:path options:0 range:NSMakeRange(0, [path length]) withTemplate:TEMPLATE];
    return modifiedString;
}

- (instancetype)initWithCorrectedPath:(NSString *)path {
    return [self initWithCorrectedPath:[self fixPath:path]];
}
@end

@implementation AkylasGoogleMapModule

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
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.GoogleMap.View", [AkylasGoogleMapViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.GoogleMap.Annotation", [AkylasGoogleMapAnnotationProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.GoogleMap.TileSource", [AkylasGoogleMapTileSourceProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.GoogleMap.Route", [AkylasGoogleMapRouteProxy class]);
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
