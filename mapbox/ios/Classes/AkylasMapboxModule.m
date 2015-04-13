/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasMapboxModule.h"
#import "AkylasMapboxTileSourceProxy.h"
#import "AkylasMapboxViewProxy.h"
#import "AkylasMapboxRouteProxy.h"
#import "AkylasMapboxAnnotationProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#import "JRSwizzle.h"
#import <objc/runtime.h>

#define MODULE_ID @"akylas.mapbox"

@implementation NSBundle (GoogleMapsFix)

+ (void) swizzle
{
    [NSBundle jr_swizzleMethod:@selector(pathForResource:ofType:) withMethod:@selector(correctedPathForResource:ofType:) error:nil];
}


- (NSString *)correctedPathForResource:(NSString *)name ofType:(NSString *)ext;
{
    if ([name isEqualToString:@"Mapbox"] && [ext isEqualToString:@"bundle"]) {
        return [self pathForResource:name ofType:ext inDirectory:@"modules/" MODULE_ID];
    }
    return [self correctedPathForResource:name ofType:ext];
}

@end

@implementation AkylasMapboxModule

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
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Mapbox.View", [AkylasMapboxViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Mapbox.Annotation", [AkylasMapboxAnnotationProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Mapbox.TileSource", [AkylasMapboxTileSourceProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"Akylas.Mapbox.Route", [AkylasMapboxRouteProxy class]);
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

#pragma mark Public APIs


-(void)setMapboxAccessToken:(id)value
{
    [[RMConfiguration configuration] setAccessToken:[TiUtils stringValue:value]];
    [self replaceValue:value forKey:@"mapboxAccessToken" notification:NO];
}

@end
