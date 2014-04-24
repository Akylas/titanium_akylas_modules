/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"



#define MAKE_IOS7_SYSTEM_PROP(name,map) \
-(NSNumber*)name \
{\
if (![TiUtils isIOS7OrGreater]) {\
const char *propName =  #name;\
[AkylasMapboxModule logAddedIniOS7Warning:[NSString stringWithUTF8String:propName]];\
return nil;\
}\
return [NSNumber numberWithInt:map];\
}\

@interface AkylasMapboxModule : TiModule
{
}

+(void)logAddedIniOS7Warning:(NSString*)name;

@property(nonatomic,readonly) NSNumber *STANDARD_TYPE;
@property(nonatomic,readonly) NSNumber *NORMAL_TYPE; // For parity with Android
@property(nonatomic,readonly) NSNumber *SATELLITE_TYPE;
@property(nonatomic,readonly) NSNumber *HYBRID_TYPE;
@property(nonatomic,readonly) NSNumber *ANNOTATION_RED;
@property(nonatomic,readonly) NSNumber *ANNOTATION_GREEN;
@property(nonatomic,readonly) NSNumber *ANNOTATION_PURPLE;

@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_NONE;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_START;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_DRAG;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_CANCEL;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_END;

+(CLLocationCoordinate2D)locationFromDict:(NSDictionary*)dict;
+(RMSphericalTrapezium)regionFromDict:(NSDictionary*)dict;
+(NSDictionary*)dictFromLocation2D:(CLLocationCoordinate2D)coord;
+(NSDictionary*)dictFromRegion:(RMSphericalTrapezium)trapez;
+(CLLocation*)cllocationFromDict:(NSDictionary*)dict;
+(id)sourceFromObject:(id)value proxy:(TiProxy*)proxy;

@end
