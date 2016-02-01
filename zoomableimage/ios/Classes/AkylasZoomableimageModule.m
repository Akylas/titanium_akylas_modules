/**
 * Akylas
 * Copyright (c) 2014-2015 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


#import "AkylasZoomableimageModule.h"
#import "AkylasTritonPlayerProxy.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation AkylasZoomableimageModule

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"0d005e93-9980-4739-9e41-fd1129c8ff32";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.triton";
}

#pragma mark Lifecycle

-(void)startup
{
    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasTriton.Player", [AkylasTritonPlayerProxy class]);
	[super startup];
	
	NSLog(@"[INFO] Triton module loaded",self);
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

@end
