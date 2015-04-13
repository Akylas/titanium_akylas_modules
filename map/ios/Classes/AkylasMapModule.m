/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasMapModule.h"
#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapViewProxy.h"
#import "AkylasMapRouteProxy.h"
#import "AkylasMapAnnotationProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#define MODULE_ID @"akylas.map"
@implementation AkylasMapModule

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
	// this method is called when the module is first loaded
	// you *must* call the superclass
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.View", [AkylasMapViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.Annotation", [AkylasMapAnnotationProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.TileSource", [AkylasMapTileSourceProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.Route", [AkylasMapRouteProxy class]);
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

#pragma mark Public APIs

MAKE_SYSTEM_PROP(OVERLAY_LEVEL_ABOVE_LABELS,MKOverlayLevelAboveLabels);
MAKE_SYSTEM_PROP(OVERLAY_LEVEL_ABOVE_ROADS,MKOverlayLevelAboveRoads);

@end
