/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasCommonjsModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"
#import "NSData+Additions.h"

extern NSString * const TI_APPLICATION_ID;
@implementation AkylasCommonjsModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"473dacaa-f088-4cdf-a2b2-78a238fdf09e";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.commonjs";
}

#pragma mark Password
-(NSString*)getPasswordKey {
    return @"akylas.modules.key";
}
-(NSString*) getPassword {
    return stringWithHexString(@"7265745b496b2466553b486f736b7b4f");
}

@end
