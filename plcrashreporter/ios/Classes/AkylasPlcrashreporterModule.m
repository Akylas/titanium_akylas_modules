/**
 * Akylas
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasPlcrashreporterModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "TiApp.h"
#import "CrashReporter/PLCrashReporter.h"

#import <sys/types.h>
#import <sys/sysctl.h>

#include <Availability.h>

@implementation AkylasPlcrashreporterModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"397c89fb-9a51-4c88-becd-9e10a1bc7e97";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.plcrashreporter";
}

#pragma mark Lifecycle

-(void)startup
{
    
	[super startup];
}

-(void)shutdown:(id)sender
{
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


-(PLCrashReporter *)crashReporter
{
    return [PLCrashReporter sharedReporter];
//    static PLCrashReporter *crashReporter = nil;
//    static dispatch_once_t onceToken;
//    dispatch_once(&onceToken, ^{
//        PLCrashReporterConfig *config = [[[PLCrashReporterConfig alloc]
//                                          initWithSignalHandlerType: [TiUtils intValue:[self valueForKey:@"signalHandlerType"] def:PLCrashReporterSignalHandlerTypeMach]
//                                          symbolicationStrategy: [TiUtils intValue:[self valueForKey:@"symbolicationStrategy"] def:PLCrashReporterSymbolicationStrategyAll]] autorelease];
//        crashReporter = [[PLCrashReporter alloc] initWithConfiguration: config];
//    });
//    return crashReporter;
}


#pragma Public APIs
-(id)hasPendingCrashReport
{
    PLCrashReporter *crashReporter = [self crashReporter];
    return @([crashReporter hasPendingCrashReport]);
}


-(void)purgePendingCrashReport:(id)args
{
    PLCrashReporter *crashReporter = [self crashReporter];
    [crashReporter purgePendingCrashReport];
}

-(id)generateLiveReport:(id)args
{
    PLCrashReporter *crashReporter = [self crashReporter];
    NSData* result = [crashReporter generateLiveReport];
    if (result) {
        return [[[TiBlob alloc] initWithData:result mimetype:@"application/octet-stream"] autorelease];
    }
    return nil;
}

-(id)loadPendingCrashReport:(id)args
{
    PLCrashReporter *crashReporter = [self crashReporter];
    NSData* result = [crashReporter loadPendingCrashReportData];
    
    if (result) {
        return [[[TiBlob alloc] initWithData:result mimetype:@"application/octet-stream"] autorelease];
    }
    return nil;
}

/* A custom post-crash callback */
static void post_crash_callback (siginfo_t *info, ucontext_t *uap, void *context) {
    // this is not async-safe, but this is a test implementation
    NSLog(@"post crash callback: signo=%d, uap=%p, context=%p", info->si_signo, uap, context);
}

static bool debugger_should_exit (void) {
#if !TARGET_OS_IPHONE
    return false;
#endif
    
    struct kinfo_proc info;
    size_t info_size = sizeof(info);
    int name[4];
    
    name[0] = CTL_KERN;
    name[1] = KERN_PROC;
    name[2] = KERN_PROC_PID;
    name[3] = getpid();
    
    if (sysctl(name, 4, &info, &info_size, NULL, 0) == -1) {
        NSLog(@"sysctl() failed: %s", strerror(errno));
        return false;
    }
    
    if ((info.kp_proc.p_flag & P_TRACED) != 0)
        return true;
    
    return false;
}

-(id)enableCrashReporter:(id)args
{
//    if (debugger_should_exit()) {
//        NSLog(@"The demo crash app should be run without a debugger present. Exiting ...");
//        return @{
//                 @"code":@(-1),
//                 @"error":@"should not run with a debugger!"
//                 
//        };
//    }
    PLCrashReporter *crashReporter = [self crashReporter];
    NSError *error;
    /* Set up post-crash callbacks */
    PLCrashReporterCallbacks cb = {
        .version = 0,
        .context = (void *) 0xABABABAB,
        .handleSignal = post_crash_callback
    };
    [crashReporter setCrashCallbacks: &cb];
    [crashReporter enableCrashReporter];
    if (error) {
        return [TiUtils dictionaryWithCode:[error code] message:[TiUtils messageFromError:error]];
    }
    return nil;
}

-(void)triggerCrash:(id)unused
{
    /* Trigger a crash */
    ((char *)NULL)[1] = 0;
}
@end
