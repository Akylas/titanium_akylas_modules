/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"

@interface AkylasMapModule : TiModule
{
}

+(void)logAddedIniOS7Warning:(NSString*)name;

@property(nonatomic,readonly) NSNumber *STANDARD_TYPE;
@property(nonatomic,readonly) NSNumber *NORMAL_TYPE; // For parity with Android
@property(nonatomic,readonly) NSNumber *SATELLITE_TYPE;
@property(nonatomic,readonly) NSNumber *HYBRID_TYPE;
@property(nonatomic,readonly) NSNumber *NONE_TYPE;
@property(nonatomic,readonly) NSNumber *ANNOTATION_RED;
@property(nonatomic,readonly) NSNumber *ANNOTATION_GREEN;
@property(nonatomic,readonly) NSNumber *ANNOTATION_PURPLE;

@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_NONE;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_START;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_DRAG;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_CANCEL;
@property(nonatomic,readonly) NSNumber *ANNOTATION_DRAG_STATE_END;

+(CLLocationCoordinate2D)locationFromDict:(NSDictionary*)dict;
+(RMSphericalTrapezium)regionFromObject:(NSDictionary*)dict;
+(NSDictionary*)dictFromLocation2D:(CLLocationCoordinate2D)coord;
+(NSDictionary*)dictFromRegion:(RMSphericalTrapezium)trapez;
+(CLLocation*)cllocationFromDict:(NSDictionary*)dict;
+(CLLocation*)cllocationFromArray:(NSArray*)array;
+(NSDictionary*)dictFromLocation:(CLLocation*)coord;
+(NSDictionary*)dictFromHeading:(CLHeading*)headin;
+(CLLocationCoordinate2D)locationFromObject:(id)obj;

@end