/**
 * Akylas
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasTestfairyModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"
#import "TestFairy.h"


#define TF_PREFIX @"testflight."
extern NSString * const TI_APPLICATION_DEPLOYTYPE;
static BOOL asyncLog = false;
static BOOL testFilghtOn = false;
//@implementation TiApp (TFLog)
//
//+(void)TiNSLog:(NSString*) message
//{
//#pragma push
//#undef NSLog
//    if (testFilghtOn == false || [TI_APPLICATION_DEPLOYTYPE isEqualToString:@"development"]) {
//        NSLog(@"%@",message);
//    }
//    else {
//        if (asyncLog) {
//            TFLog_async(@"[TFA] %@",message);
//        }
//        else {
//            TFLog(@"[TF] %@",message);
//        }
//    }
//#pragma pop
//}
//@end

@implementation AkylasTestfairyModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"397c89fb-9d51-4c88-becd-9e10a1bc7e97";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.testfairy";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
//    NSMutableDictionary* optionsToSet = [NSMutableDictionary dictionary];
    NSDictionary* tiappProperties = [TiApp tiAppProperties];
//    [[AkylasTestfairyModule mapping] enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
//        id value = [tiappProperties objectForKey:[NSString stringWithFormat:@"%@%@", TF_PREFIX, key]];
//        if (value) {
//            [optionsToSet setObject:value forKey:obj];
//        }
//    }];
//    [TestFlight setOptions:optionsToSet];
//    testFilghtOn = YES;
//    [self replaceValue:@(testFilghtOn) forKey:@"enabled" notification:NO];
    NSString* token = [tiappProperties objectForKey:@"testfairy.token"];
//    if ([tiappProperties objectForKey:@"testflight.asyncLog"]) {
//        DebugLog(@"[INFO] TestFlight asyncLogging");
//        asyncLog = [[tiappProperties objectForKey:@"testflight.asyncLog"] boolValue];
//    }
//    [self replaceValue:@(asyncLog) forKey:@"asyncLog" notification:NO];
    
    if (token) {
        DebugLog(@"[INFO] TestFairy begin %@", token);
        [TestFairy begin:token];
    }
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

#pragma Public APIs

-(id)getObjectProperty:(NSString*)key
{
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    return [defaults objectForKey:key];
}

-(BOOL)getBoolProperty:(NSString*)key defaultValue:(BOOL)defaultValue
{
    id object = [self getObjectProperty:key];
    if (object) {
        return [object boolValue];
    }
    return defaultValue;
}

-(NSString*)getStringProperty:(NSString*)key defaultValue:(NSString*)defaultValue
{
    id object = [self getObjectProperty:key];
    if (object) {
        return [object stringValue];
    }
    return defaultValue;
}

-(void)begin:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    ENSURE_SINGLE_ARG_OR_NIL(args,NSString);
    
    [TestFairy begin:args];
    DebugLog(@"[INFO] TestFairy begin %@", args);
}

-(void)pause:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    [TestFairy pause];
}

-(void)resume:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    [TestFairy resume];
}

-(void)passCheckpoint:(id)args
{    
    ENSURE_UI_THREAD_1_ARG(args);
    ENSURE_SINGLE_ARG(args, NSString)
    [TestFairy checkpoint:args];
}

-(void)submitFeedback:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);

    [TestFairy pushFeedbackController];
}

-(void)setCorrelationId:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    ENSURE_SINGLE_ARG(args, NSString)
    [TestFairy setCorrelationId:args];
}

-(void)updateLocation:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    ENSURE_SINGLE_ARG(args, NSArray)
    [TestFairy updateLocation:args];
}

@end
