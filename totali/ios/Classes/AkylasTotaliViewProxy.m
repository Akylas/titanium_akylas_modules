

#import "AkylasTotaliViewProxy.h"
#import "AkylasTotaliView.h"
#import "TiUtils.h"

@implementation AkylasTotaliViewProxy

#ifdef DEBUG_MEMORY
-(void)dealloc
{
	[super dealloc];
}

-(id)retain
{
	return [super retain];
}

-(void)release
{
	[super release];
}
#endif

-(BOOL)shouldDetachViewForSpace
{
	return NO;
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    
    // allocate the Component
    //    [self replaceValue:[NSArray arrayWithObject:NUMINT(UIDataDetectorTypePhoneNumber)] forKey:@"autoDetect" notification:NO];
//    [self initializeProperty:@"willHandleTouches" defaultValue:NUMBOOL(YES)];
    [super _initWithProperties:properties];
}



//USE_VIEW_FOR_UI_METHOD(show)
//USE_VIEW_FOR_AUTO_HEIGHT
//USE_VIEW_FOR_AUTO_WIDTH

//-(void)goBack:(id)args
//{
//	TiThreadPerformOnMainThread(^{[(XeroxRepairmanlibWebGLView*)[self view] goBack];}, NO);
//}

//-(id)canGoBack:(id)args
//{
//	if ([self viewAttached])
//	{
//		__block BOOL result;
//		TiThreadPerformOnMainThread(^{result = [(XeroxRepairmanlibWebGLView*)[self view] canGoBack];}, YES);
//		return NUMBOOL(result);
//	}
//	return NUMBOOL(NO);
//}

//-(void)setBasicAuthentication:(NSArray*)args
//{
//	[self makeViewPerformSelector:@selector(setBasicAuthentication:) withObject:args createIfNeeded:YES waitUntilDone:NO];
//}

-(void)repaint:(id)unused
{
	[self contentsWillChange];
}

-(void)windowDidClose
{
	[self _destroy];
	NSNotification *notification = [NSNotification notificationWithName:kTiContextShutdownNotification object:self];
	WARN_IF_BACKGROUND_THREAD_OBJ;	//NSNotificationCenter is not threadsafe!
	[[NSNotificationCenter defaultCenter] postNotification:notification];
	[super windowDidClose];
}

-(void)_destroy
{
    
    [super _destroy];
}

//-(void)setPageToken:(NSString*)pageToken_
//{
//	if (pageToken != nil)
//	{
//		[[self host] unregisterContext:(id<TiEvaluator>)self forToken:pageToken];
//		RELEASE_TO_NIL(pageToken);
//	}
//	pageToken = [pageToken_ retain];
//	[[self host] registerContext:self forToken:pageToken];
//}

#pragma mark Evaluator

- (TiHost*)host
{
	return [self _host];
}

//- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
//{
////    NSLog(@"module proxy shouldAutorotateToInterfaceOrientation");
//    return [(AkylasTotaliView*)[self view] shouldAutorotateToInterfaceOrientation:interfaceOrientation];
//}
//
//- (void)viewWillAppear:(BOOL)animated
//{
////    NSLog(@"module proxy viewWillAppear");
//    TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] viewWillAppear:animated];
//	}, NO);
//}
//
//- (void)viewDidAppear:(BOOL)animated
//{
////    NSLog(@"module proxy viewDidAppear");
//    TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] viewDidAppear:animated];
//	}, NO);
//}
//
//- (void)viewWillDisappear:(BOOL)animated
//{
////    NSLog(@"module proxy viewWillDisappear");
//	TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] viewWillDisappear:animated];
//	}, NO);
//}
//
//- (void)viewDidDisappear:(BOOL)animated
//{
////    NSLog(@"module proxy viewDidDisappear");
//	TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] viewDidDisappear:animated];
//	}, NO);
//}
//
//- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
////    NSLog(@"module proxy willAnimateRotationToInterfaceOrientation");
//	TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
//	}, NO);
//}
//
//- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
////    NSLog(@"module proxy willRotateToInterfaceOrientation");
//	TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
//	}, NO);
//}
//
//- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
//{
////    NSLog(@"module proxy didRotateFromInterfaceOrientation");
//	TiThreadPerformOnMainThread(^{
//		[(AkylasTotaliView*)[self view] didRotateFromInterfaceOrientation:fromInterfaceOrientation];
//	}, NO);
//}


//- (KrollContext*)krollContext
//{
//	return nil;
//}

-(NSString*) scenario
{
    return [(AkylasTotaliView*)[self view] scenario];
}

-(void) start:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasTotaliView*)[self view] start];
    },NO);
}

-(void) stop:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasTotaliView*)[self view] stop];
    },NO);
}

-(void)registerCallback:(id)value
{
    ENSURE_TYPE_OR_NIL(value, NSString);
    TiThreadPerformOnMainThread(^{
        [(AkylasTotaliView*)[self view] registerCallback:value];
    },NO);
}

-(void)unregisterCallback:(id)value
{
    ENSURE_TYPE_OR_NIL(value, NSString);
    TiThreadPerformOnMainThread(^{
        [(AkylasTotaliView*)[self view] unregisterCallback:value];
    },NO);
}


//
//- (id) centeredCropRect
//{
//    return NUMBOOL([(AkylasTotaliView*)[self view] centeredCropRect]);
//}

//- (id) cropRect
//{
//    TiRect *result = [[[TiRect alloc] init] autorelease];
//    [result setRect:([(AkylasTotaliView*)[self view] cropRect])];
//    return result;
//}
//
//- (void) flush:(id)args
//{
//    TiThreadPerformOnMainThread(^{
//        [(AkylasTotaliView*)[self view] flush];
//    },NO);
//}
//
//- (void) swapCamera:(id)args
//{
//    TiThreadPerformOnMainThread(^{
//        [(AkylasTotaliView*)[self view] swapCamera];
//    },NO);
//}
//
//- (void) focus:(id)args
//{
//	CGPoint p = [TiUtils pointValue:args];
//    TiThreadPerformOnMainThread(^{
//        [(AkylasTotaliView*)[self view] focus:p];
//    },NO);
//}
//
//- (void) autoFocus:(id)args
//{
//	CGPoint p = [TiUtils pointValue:args];
//    TiThreadPerformOnMainThread(^{
//        [(AkylasTotaliView*)[self view] autoFocus:p];
//    },NO);
//}


@end
