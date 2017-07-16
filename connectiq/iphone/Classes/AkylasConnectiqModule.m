/**
 * connectiq
 *
 * Created by Your Name
 * Copyright (c) 2017 Your Company. All rights reserved.
 */

#import "AkylasConnectiqModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation AkylasConnectiqModule
{
    
}

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"8a4f40e5-085b-460b-9a1c-30ef47d5672a";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.connectiq";
}


-(void)initSDK:(id)args {
    ENSURE_TYPE(args, NSArray)
    NSString* urlScheme = nil;
    KrollCallback* callback = nil;
    ENSURE_ARG_AT_INDEX(urlScheme, args, 0, NSString);
    ENSURE_ARG_OR_NIL_AT_INDEX(callback, args, 1, KrollCallback)

    [[ConnectIQ sharedInstance] initializeWithUrlScheme:urlScheme uiOverrideDelegate:self];
}


- (void)needsToInstallConnectMobile {
    [[ConnectIQ sharedInstance] showAppStoreForConnectMobile];
}
@end
