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
#import "NSString+AES.h"
#import "NSData+Additions.h"
#define COMMON_DIGEST_FOR_OPENSSL
#import "CommonCrypto/CommonDigest.h"

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

#pragma mark Lifecycle

-(NSString*) digest:(NSString*)input{
    NSData *data = [input dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
    uint8_t digest[CC_SHA1_DIGEST_LENGTH];
    CC_SHA1(data.bytes, data.length, digest);
    NSMutableString* output = [NSMutableString stringWithCapacity:CC_SHA1_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_SHA1_DIGEST_LENGTH; i++)
        [output appendFormat:@"%02x", digest[i]];
    
    return output;
}

-(void)startup
{
    
    NSString* appId = TI_APPLICATION_ID;
    NSDictionary* tiappProperties = [TiApp tiAppProperties];
   
    NSString* commonjsKey = [tiappProperties objectForKey:@"akylas.commonjs.key"];
    if ([appId isEqualToString:@"com.akylas.titanium.ks"]) {
        commonjsKey = @"b5500c8406217aa71d38f5db43118dac72049b5d";
    }
    NSAssert(commonjsKey, @"You need to set the \"akylas.commonjs.key\"");
    
    NSString* result = [self digest:[NSString stringWithFormat:@"%@%@", TI_APPLICATION_ID, stringWithHexString(@"7265745b496b2466553b486f736b7b4f")]];
    NSAssert([[commonjsKey lowercaseString] isEqualToString:result], @"wrong \"akylas.commonjs.key\" key!");
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
	DebugLog(@"[INFO] %@ loaded",self);
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

@end
