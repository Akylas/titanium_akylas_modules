/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasMapModule.h"
#import "AkylasMapTileSourceProxy.h"
#import "AkylasMapMapViewProxy.h"
#import "AkylasMapMapIOS7ViewProxy.h"
#import "AkylasMapMapboxViewProxy.h"
#import "AkylasMapRouteProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#if defined(USE_TI_FILESYSTEM)
#import "TiFilesystemFileProxy.h"
#endif
#import <Mapbox/RMOpenStreetMapSource.h>
#import <Mapbox/RMOpenSeaMapSource.h>
#import <Mapbox/RMOpenCycleMapSource.h>
#import <Mapbox/RMTileMillSource.h>
#import <Mapbox/RMMapQuestOSMSource.h>

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
	return @"akylas.mapbox";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.MapboxView", [AkylasMapMapboxViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.MapView", [AkylasMapMapViewProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.Annotation", [AkylasMapAnnotationProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.TileSource", [AkylasMapTileSourceProxy class]);
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasMap.Route", [AkylasMapRouteProxy class]);
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
	
	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup 

-(void)dealloc
{
	// release any resources that have been retained by the module
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

-(void)_listenerAdded:(NSString *)type count:(NSInteger)count
{
	if (count == 1 && [type isEqualToString:@"my_event"])
	{
		// the first (of potentially many) listener is being added 
		// for event named 'my_event'
	}
}

-(void)_listenerRemoved:(NSString *)type count:(NSInteger)count
{
	if (count == 0 && [type isEqualToString:@"my_event"])
	{
		// the last listener called for event named 'my_event' has
		// been removed, we can optionally clean up any resources
		// since no body is listening at this point for that event
	}
}

#pragma Public APIs

+(void)logAddedIniOS7Warning:(NSString*)name
{
    NSLog(@"[WARN] `%@` is only supported on iOS 7 and greater.", name);
}

#pragma mark Public APIs


-(AkylasMapMapViewProxy*)createMapView:(id)args
{
    Class mapViewProxyClass = [AkylasMapMapIOS7ViewProxy class];
    return [[[mapViewProxyClass alloc] _initWithPageContext:[self pageContext] args:args] autorelease];
}

-(AkylasMapCameraProxy*)createCamera:(id)args
{
    return [[[AkylasMapCameraProxy alloc] _initWithPageContext:[self pageContext] args:args] autorelease];
}

MAKE_SYSTEM_PROP(STANDARD_TYPE,MKMapTypeStandard);
MAKE_SYSTEM_PROP(NORMAL_TYPE,MKMapTypeStandard); // For parity with Android
MAKE_SYSTEM_PROP(SATELLITE_TYPE,MKMapTypeSatellite);
MAKE_SYSTEM_PROP(HYBRID_TYPE,MKMapTypeHybrid);
MAKE_SYSTEM_PROP(ANNOTATION_RED,MKPinAnnotationColorRed);
MAKE_SYSTEM_PROP(ANNOTATION_GREEN,MKPinAnnotationColorGreen);
MAKE_SYSTEM_PROP(ANNOTATION_PURPLE,MKPinAnnotationColorPurple);

MAKE_SYSTEM_PROP(ANNOTATION_DRAG_STATE_NONE,MKAnnotationViewDragStateNone);
MAKE_SYSTEM_PROP(ANNOTATION_DRAG_STATE_START,MKAnnotationViewDragStateStarting);
MAKE_SYSTEM_PROP(ANNOTATION_DRAG_STATE_DRAG,MKAnnotationViewDragStateDragging);
MAKE_SYSTEM_PROP(ANNOTATION_DRAG_STATE_CANCEL,MKAnnotationViewDragStateCanceling);
MAKE_SYSTEM_PROP(ANNOTATION_DRAG_STATE_END,MKAnnotationViewDragStateEnding);

MAKE_SYSTEM_PROP(OVERLAY_LEVEL_ABOVE_LABELS,MKOverlayLevelAboveLabels);
MAKE_SYSTEM_PROP(OVERLAY_LEVEL_ABOVE_ROADS,MKOverlayLevelAboveRoads);


+(CLLocationCoordinate2D)locationFromDict:(NSDictionary*)dict
{
    return CLLocationCoordinate2DMake([TiUtils doubleValue:@"latitude" properties:dict def:0], [TiUtils doubleValue:@"longitude" properties:dict def:0]);
}

+(CLLocation*)cllocationFromDict:(NSDictionary*)dict
{
    if ([dict objectForKey:@"latitude"] && [dict objectForKey:@"longitude"]) {
        CLLocationCoordinate2D loc = [self locationFromDict:dict];
        double altitude = [TiUtils doubleValue:@"altitude" properties:dict def:0];
        return [[[CLLocation alloc] initWithCoordinate:loc altitude:altitude horizontalAccuracy:0 verticalAccuracy:0 course:[TiUtils doubleValue:@"bearing" properties:dict def:0] speed:[TiUtils doubleValue:@"speed" properties:dict def:0] timestamp:[TiUtils dateValue:@"timestamp" properties:dict]] autorelease];
    }
    return nil;
}
+(CLLocation*)cllocationFromArray:(NSArray*)array
{
    NSUInteger count = [array count];
    if (count <2) return nil;
    CLLocationCoordinate2D loc = CLLocationCoordinate2DMake([[array objectAtIndex:0] doubleValue],  [[array objectAtIndex:1] doubleValue]);

    double altitude = (count > 2)?[[array objectAtIndex:2] doubleValue]:0;
    NSString* timestamp = (count > 3)?[[array objectAtIndex:3] stringValue]:nil;
    double speed = (count > 4)?[[array objectAtIndex:4] doubleValue]:0;
    double course = (count > 5)?[[array objectAtIndex:5] doubleValue]:0;
    
    return [[[CLLocation alloc] initWithCoordinate:loc altitude:altitude horizontalAccuracy:0 verticalAccuracy:0 course:course speed:speed timestamp:[TiUtils dateValue:timestamp]] autorelease];
}
+(RMSphericalTrapezium)regionFromDict:(NSDictionary*)dict
{
    RMSphericalTrapezium result;
    result.southWest = result.northEast = kCLLocationCoordinate2DInvalid;
    if ([dict objectForKey:@"sw"] && [dict objectForKey:@"ne"]) {
        result.southWest = [self locationFromDict:[dict objectForKey:@"sw"]];
        result.northEast = [self locationFromDict:[dict objectForKey:@"ne"]];
    } else if ([dict objectForKey:@"latitude"] && [dict objectForKey:@"longitude"]) {
        CGFloat latitudeDelta_2 = [TiUtils floatValue:@"latitudeDelta" properties:dict]/2.0f;
        CGFloat longitudeDelta_2 = [TiUtils floatValue:@"longitudeDelta" properties:dict]/2.0f;
        CLLocationCoordinate2D center;
        center.latitude = [TiUtils floatValue:@"latitude" properties:dict];
        center.longitude = [TiUtils floatValue:@"longitude" properties:dict];
        result.southWest = CLLocationCoordinate2DMake(center.latitude - latitudeDelta_2, center.longitude - longitudeDelta_2);
        result.northEast = CLLocationCoordinate2DMake(center.latitude + latitudeDelta_2, center.longitude + longitudeDelta_2);
    }
    
	return result;
}

+(NSDictionary*)dictFromLocation2D:(CLLocationCoordinate2D)coord
{
    return @{
             @"latitude":NUMDOUBLE(coord.latitude),
             @"longitude":NUMDOUBLE(coord.longitude)
             };
}

+(NSDictionary*)dictFromLocation:(CLLocation*)location
{
    if (!location) return @{};
    return @{
             @"latitude":NUMDOUBLE(location.coordinate.latitude),
             @"longitude":NUMDOUBLE(location.coordinate.longitude),
             @"altitude":NUMDOUBLE(location.altitude),
             @"horizontalAccuracy":NUMDOUBLE(location.horizontalAccuracy),
             @"verticalAccuracy":NUMDOUBLE(location.verticalAccuracy),
             @"heading":NUMDOUBLE(location.course),
             @"speed":NUMDOUBLE(location.speed),
             @"timestamp":NUMDOUBLE([location.timestamp timeIntervalSince1970]*1000),
             };
}

+(NSDictionary*)dictFromHeading:(CLHeading*)heading
{
    if (!heading) return @{};
    return @{
             @"magneticHeading":NUMDOUBLE(heading.magneticHeading),
             @"heading":NUMDOUBLE(heading.trueHeading),
             @"headingAccuracy":NUMDOUBLE(heading.headingAccuracy),
             @"x":NUMDOUBLE(heading.x),
             @"y":NUMDOUBLE(heading.y),
             @"z":NUMDOUBLE(heading.z),
             @"timestamp":NUMDOUBLE([heading.timestamp timeIntervalSince1970]*1000),
             };
}

+(NSDictionary*)dictFromRegion:(RMSphericalTrapezium)trapez
{
    if (trapez.northEast.latitude == trapez.southWest.latitude ||
        trapez.northEast.longitude == trapez.southWest.longitude)
        return nil;
    return @{
             @"sw":[self dictFromLocation2D:trapez.southWest],
             @"ne":[self dictFromLocation2D:trapez.northEast]
             };
}

+(id)sourceFromObject:(id)value proxy:(TiProxy*)proxy
{
    if ([value isKindOfClass:[AkylasMapTileSourceProxy class]]) {
        return [((AkylasMapTileSourceProxy*)value) tileSource];
    }
    NSString* type = nil;
    if ([value isKindOfClass:[NSString class]]) {
        type = value;
    }
#if defined(USE_TI_FILESYSTEM)
    else if ([value isKindOfClass:[TiFilesystemFileProxy class]]) {
        type = [(TiFilesystemFileProxy*)value nativePath];
    }
#endif
    else if ([value isKindOfClass:[NSDictionary class]]) {
        type = [(NSDictionary*)value valueForKey:@"source"];
    }
    if (type == nil) return nil;
    NSString* typeLower = [type lowercaseString];
    id result = nil;
    if ([typeLower isEqualToString:@"openstreetmap"])
    {
        result = [[RMOpenStreetMapSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"openseamap"])
    {
        result = [[RMOpenSeaMapSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"mapquest"])
    {
        result = [[RMMapQuestOSMSource alloc] init];
    }
    else if ([typeLower isEqualToString:@"tilemill"] &&[value isKindOfClass:[NSDictionary class]] )
    {
        NSString* name = [TiUtils stringValue:@"mapName" properties:value];
        NSString* cacheKey = [TiUtils stringValue:@"cacheKey" properties:value def:name];
        result = [[RMTileMillSource alloc] initWithHost:[TiUtils stringValue:@"host" properties:value] mapName:name tileCacheKey:cacheKey minZoom:[TiUtils floatValue:@"minZoom" properties:value] maxZoom:[TiUtils floatValue:@"maxZoom" properties:value]];
    }
    else if ([typeLower hasSuffix:@"mbtiles"])
    {
        result = [[RMMBTilesSource alloc] initWithTileSetURL:[TiUtils toURL:type proxy:proxy]];
    }
    else {
        result = [[RMMapboxSource alloc] initWithMapID:type];
    }
    return [result autorelease];
}

@end
