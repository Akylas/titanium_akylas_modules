

#import "AkylasScancodeViewProxy.h"
#import "AkylasScancodeView.h"
#import "TiUtils.h"

@implementation AkylasScancodeViewProxy

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
//	if (pageToken!=nil)
//	{
//		[[self host] unregisterContext:(id<TiEvaluator>)self forToken:pageToken];
//		RELEASE_TO_NIL(pageToken);
//	}
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

//- (void)fireEvent:(id)listener withObject:(id)obj remove:(BOOL)yn thisObject:(id)thisObject_
//{
//	TiThreadPerformOnMainThread(^{
//		[(AkylasScancodeView*)[self view] fireEvent:listener withObject:obj remove:yn thisObject:thisObject_];
//	}, NO);
//}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
//    NSLog(@"module proxy shouldAutorotateToInterfaceOrientation");
    return [(AkylasScancodeView*)[self view] shouldAutorotateToInterfaceOrientation:interfaceOrientation];
}

- (void)viewWillAppear:(BOOL)animated
{
//    NSLog(@"module proxy viewWillAppear");
    TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] viewWillAppear:animated];
	}, NO);
}

- (void)viewDidAppear:(BOOL)animated
{
//    NSLog(@"module proxy viewDidAppear");
    TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] viewDidAppear:animated];
	}, NO);
}

- (void)viewWillDisappear:(BOOL)animated
{
//    NSLog(@"module proxy viewWillDisappear");
	TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] viewWillDisappear:animated];
	}, NO);
}

- (void)viewDidDisappear:(BOOL)animated
{
//    NSLog(@"module proxy viewDidDisappear");
	TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] viewDidDisappear:animated];
	}, NO);
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module proxy willAnimateRotationToInterfaceOrientation");
	TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
	}, NO);
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module proxy willRotateToInterfaceOrientation");
	TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
	}, NO);
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
//    NSLog(@"module proxy didRotateFromInterfaceOrientation");
	TiThreadPerformOnMainThread(^{
		[(AkylasScancodeView*)[self view] didRotateFromInterfaceOrientation:fromInterfaceOrientation];
	}, NO);
}


//- (KrollContext*)krollContext
//{
//	return nil;
//}



- (void) start:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] start];
    },NO);
}

- (void) stop:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] stop];
    },NO);
}

- (id) torch
{
    return NUMBOOL([(AkylasScancodeView*)[self view] torchOn]);
}

- (id) cameraPosition
{
    return NUMINT([(AkylasScancodeView*)[self view] cameraPosition]);
}

- (id) onlyOneDimension
{
    return NUMBOOL([(AkylasScancodeView*)[self view] onlyOneDimension]);
}

- (id) centeredCropRect
{
    return NUMBOOL([(AkylasScancodeView*)[self view] centeredCropRect]);
}

- (id) cropRect
{
    TiRect *result = [[[TiRect alloc] init] autorelease];
    [result setRect:([(AkylasScancodeView*)[self view] cropRect])];
    return result;
}

- (void) flush:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] flush];
    },NO);
}

- (void) swapCamera:(id)args
{
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] swapCamera];
    },NO);
}

- (void) focus:(id)args
{
	CGPoint p = [TiUtils pointValue:args];
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] focus:p];
    },NO);
}

- (void) autoFocus:(id)args
{
	CGPoint p = [TiUtils pointValue:args];
    TiThreadPerformOnMainThread(^{
        [(AkylasScancodeView*)[self view] autoFocus:p];
    },NO);
}


@end
