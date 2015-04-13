/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiUtils.h"
#import "AkylasMapCameraProxy.h"
#import "AkylasMapModule.h"

@implementation AkylasMapCameraProxy

-(id)init
{
	return [self initWithCamera:nil];
}

-(id)initWithCamera:(MKMapCamera*)camera
{
    if (self = [super init]) {
        camera_ = [camera retain];
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(camera_);
	
	[super dealloc];
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    id eyeCoord = [properties valueForKey:@"eyeCoordinate"];
    id eyeAlt = [properties valueForKey:@"altitude"];
    id centerCoord = [properties valueForKey:@"centerCoordinate"];
    
    if (eyeCoord != nil && eyeAlt != nil && centerCoord != nil) {
        CLLocationCoordinate2D eyeCoordinate = [AkylasMapModule locationFromDict:eyeCoord];
        double eyeAltitude = [TiUtils doubleValue:eyeAlt];
        CLLocationCoordinate2D centerCoordinate = [AkylasMapModule locationFromDict:eyeCoord];
        
        camera_ = [[MKMapCamera cameraLookingAtCenterCoordinate:centerCoordinate
                                             fromEyeCoordinate:eyeCoordinate
                                                   eyeAltitude:eyeAltitude] retain];
    }
	
	[super _initWithProperties:properties];
}

-(NSString*)apiName
{
    return @"Ti.Map.Camera";
}

-(MKMapCamera*)camera
{
    if (camera_ == nil) {
        camera_ = [[MKMapCamera camera] retain];
    }
    return camera_;
}

-(void)setAltitude:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    [self camera].altitude = [TiUtils doubleValue:value];
}
-(NSNumber*)altitude
{
    return [NSNumber numberWithDouble:[self camera].altitude];
}

-(void)setCenterCoordinate:(id)args
{
    ENSURE_SINGLE_ARG(args, NSDictionary);
    [self camera].centerCoordinate = [AkylasMapModule locationFromDict:args];
}
-(NSDictionary*)centerCoordinate
{
    CLLocationCoordinate2D centerCord = [self camera].centerCoordinate;
    NSDictionary *result = [NSDictionary dictionaryWithObjectsAndKeys:
                            [NSNumber numberWithDouble:centerCord.latitude], @"latitude",
                            [NSNumber numberWithDouble:centerCord.longitude], @"longitude",
                            nil];
    return result;
}

-(void)setHeading:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    [self camera].heading = [TiUtils doubleValue:value];
}
-(NSNumber *)heading
{
    return [NSNumber numberWithDouble:[self camera].heading];
}

-(void)setPitch:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    [self camera].pitch = [TiUtils doubleValue:value];
}
-(NSNumber *)pitch
{
    return [NSNumber numberWithDouble:[self camera].pitch];
}

@end
