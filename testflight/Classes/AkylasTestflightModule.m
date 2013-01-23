/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasTestflightModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TestFlight.h"

@implementation AkylasTestflightModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"1bfb9126-f027-4691-a432-5d49dacc0451";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.testflight";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
    [TestFlight setOptions:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"sendLogOnlyOnCrash"]];
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

-(void)_listenerAdded:(NSString *)type count:(int)count
{
	if (count == 1 && [type isEqualToString:@"my_event"])
	{
		// the first (of potentially many) listener is being added 
		// for event named 'my_event'
	}
}

-(void)_listenerRemoved:(NSString *)type count:(int)count
{
	if (count == 0 && [type isEqualToString:@"my_event"])
	{
		// the last listener called for event named 'my_event' has
		// been removed, we can optionally clean up any resources
		// since no body is listening at this point for that event
	}
}

#pragma Public APIs

-(void)token:(id)args
{
    ENSURE_UI_THREAD(token, args);
    NSString *value = [TiUtils stringValue:[args objectAtIndex:0]];
    BOOL testing = FALSE;
    
//#ifndef RELEASE 
    if ([args count] > 1) {
        testing = [TiUtils boolValue: [args objectAtIndex:1]];
        if (testing == TRUE) {
            [TestFlight setDeviceIdentifier: [[UIDevice currentDevice] uniqueIdentifier]];
        }
    }
//#endif
    
    [TestFlight takeOff:value];
}

-(void)checkpoint:(id)args
{    
    ENSURE_UI_THREAD_1_ARG(args);
    
    NSString *value = [TiUtils stringValue:[args objectAtIndex:0]];
    [TestFlight passCheckpoint:value];
}

-(void)feedback:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    [TestFlight openFeedbackView];
}

-(void)submitFeedback:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    
    NSString *value = [TiUtils stringValue:[args objectAtIndex:0]];
    [TestFlight submitFeedback:value];

}

-(void)customInfo:(id)args
{
    NSString *key = [TiUtils stringValue:[args objectAtIndex:0]];
    NSString *value = [TiUtils stringValue:[args objectAtIndex:1]];
    [TestFlight addCustomEnvironmentInformation: value forKey:key];
}

-(void)remoteLog:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    NSString *value = [TiUtils stringValue:[args objectAtIndex:0]];
    TFLog(@"[INFO] %@",value);
}

@end
