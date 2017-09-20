/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasCartoModule.h"
#import "AkylasCartoTileSourceProxy.h"
#import "AkylasCartoViewProxy.h"
#import "AkylasCartoRouteProxy.h"
#import "AkylasCartoAnnotationProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#define MODULE_ID @"akylas.carto"
#define REGEX @"(?!"  MODULE_ID  @"/)(GoogleMaps)\\.bundle$"
#define TOADD MODULE_ID
#define TEMPLATE TOADD @"/$0"

NTMapBounds* boundsFromRegion(AkRegion trapez)
{
    return [[[NTMapBounds alloc]  initWithMin:[[[NTMapPos alloc] initWithX:trapez.southWest.longitude y:trapez.southWest.latitude] autorelease] max:[[[NTMapPos alloc] initWithX:trapez.northEast.longitude y:trapez.northEast.latitude] autorelease]] autorelease];
}

@implementation AkylasCartoModule

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
    [NTLog setShowDebug:YES];
    [NTLog setShowInfo:YES];
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

#pragma mark Public APIs


-(void)setLicense:(id)value
{
    ENSURE_STRING(value)
    [NTMapView registerLicense:value];
}

+(NTEPSG3857 *)baseProjection
{
    static NTEPSG3857 *baseProjection = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        baseProjection = [[NTEPSG3857 alloc] init];
    });
    return baseProjection;
}


@end
