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
#import <mach/mach.h>
#import <mach/mach_host.h>
#include <Availability.h>

NSString *kCrashReportAnalyzerStarted = @"CrashReportAnalyzerStarted";		// flags if the crashlog analyzer is started. since this may crash we need to track it
NSString *kLastRunMemoryWarningReached = @"LastRunMemoryWarningReached";	// is the last crash because of lowmemory warning?
NSString *kLastStartupFreeMemory = @"LastStartupFreeMemory";							// the amount of memory available on startup on the run of the app the crash happened
NSString *kLastShutdownFreeMemory = @"LastShutdownFreeMemory";						// the amount of memory available on shutdown on the run of the app the crash happened


@implementation AkylasPlcrashreporterModule
{
    time_t _memoryWarningTimestamp;		// timestamp when memory warning appeared, we check on terminate if that timestamp is within a reasonable range to avoid false alarms
    BOOL _memoryWarningReached;				// true if memory warning notification is run at least once
    NSInteger _startupFreeMemory;						// amount of memory available at startup
    NSInteger _lastStartupFreeMemory;				// free memory at the last startup run
    
    NSInteger	_crashReportAnalyzerStarted;	// flags if the crashlog analyzer is started. since this may crash we need to track it
    NSInteger _shutdownFreeMemory;					// amount of memory available at shutdown
    NSInteger _lastShutdownFreeMemory;			// free memory at the last shutdown run
}

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

- (void) getMemory:(BOOL)startup
{
    mach_port_t host_port;
    mach_msg_type_number_t host_size;
    vm_size_t pagesize;
    
    host_port = mach_host_self();
    host_size = sizeof(vm_statistics_data_t) / sizeof(integer_t);
    host_page_size(host_port, &pagesize);
    
    vm_statistics_data_t vm_stat;
    
    if (host_statistics(host_port, HOST_VM_INFO, (host_info_t)&vm_stat, &host_size) != KERN_SUCCESS)
        NSLog(@"Failed to fetch vm statistics");
    
    natural_t mem_free = vm_stat.free_count * pagesize;
    if (startup)
        _startupFreeMemory = (mem_free / 1024 );
    else
        _shutdownFreeMemory = (mem_free / 1024 );
}



#pragma mark Lifecycle

-(void)startup
{
//    [self getMemory:YES];
//    _memoryWarningTimestamp = 0;
//    NSString *testValue = nil;
//    testValue = [[NSUserDefaults standardUserDefaults] stringForKey:kLastStartupFreeMemory];
//    if (testValue == nil)
//    {
//        _lastStartupFreeMemory = 0;
//    } else {
//        _lastStartupFreeMemory = [[NSNumber numberWithInteger:[[NSUserDefaults standardUserDefaults] integerForKey:kLastStartupFreeMemory]] integerValue];
//    }
//    
//    testValue = nil;
//    testValue = [[NSUserDefaults standardUserDefaults] stringForKey:kLastShutdownFreeMemory];
//    if (testValue == nil)
//    {
//        _lastShutdownFreeMemory = 0;
//    } else {
//        _lastShutdownFreeMemory = [[NSNumber numberWithInteger:[[NSUserDefaults standardUserDefaults] integerForKey:kLastShutdownFreeMemory]] integerValue];
//    }
	[super startup];
}

-(void)shutdown:(id)sender
{
//    [self getMemory:NO];
//    
//    // save current memory
//    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInteger:_memoryWarningReached] forKey:kLastRunMemoryWarningReached];
//    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInteger:_startupFreeMemory] forKey:kLastStartupFreeMemory];
//    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInteger:_shutdownFreeMemory] forKey:kLastShutdownFreeMemory];
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
