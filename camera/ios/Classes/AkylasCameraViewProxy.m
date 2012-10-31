

#import "AkylasCameraViewProxy.h"
#import "AkylasCameraView.h"
#import "TiUtils.h"

@implementation AkylasCameraViewProxy

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

-(void)cleanup{
    _controller.delegate = nil;
	RELEASE_TO_NIL(_controller);
}

-(BOOL)shouldDetachViewForSpace
{
	return NO;
}

-(void)_initWithProperties:(NSDictionary *)properties
{
    [super _initWithProperties:properties];
}

-(id)init
{
	if ((self = [super init]))
	{
        _controller = [[AkylasCameraViewController alloc] initWithDelegate:self];
	}
	return self;
}

-(void)viewDidAttach
{
    NSLog(@"viewDidAttach");
    ((AkylasCameraView*)[self view]).scanview = _controller.view;
    [[self view] addSubview:_controller.view];
}

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
    NSLog(@"destroying proxy");
    [self cleanup];
//    [(AkylasCameraView*)[self view] cleanup];
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

-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [_controller willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [_controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [_controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void)viewWillAppear:(BOOL)animated
{
    [_controller viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [_controller viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [_controller viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [_controller viewDidDisappear:animated];
}

- (void) setTorch:(BOOL)value
{
    [_controller setTorch:value];
}

- (void) setCameraPosition:(id)value
{
    [_controller setCameraPosition:[AkylasCameraViewController cameraPositionValue:value]];
}

- (void) start:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller.captureSession startRunning];
    },NO);
}

- (void) stop:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller.captureSession stopRunning];
    },NO);
}

- (id) torch
{
    if (_controller == nil) return false;
    return NUMBOOL([_controller torchIsOn]);
}

- (id) cameraPosition
{
    if (_controller == nil) return false;
    return NUMINT([_controller cameraPosition]);
}

- (void) swapCamera:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller swapCamera];
    },NO);
}

- (void) focus:(id)args
{
    TiThreadPerformOnMainThread(^{
        CGPoint p = [TiUtils pointValue:args];
        [_controller focusAtPoint:p];
    },NO);
}

- (void) autoFocus:(id)args
{
    TiThreadPerformOnMainThread(^{
        CGPoint p = [TiUtils pointValue:args];
        [_controller autoFocusAtPoint:p];
    },NO);
}

#define pragma mark - AkylasCameraViewControllerDelegate

- (void)controller:(AkylasCameraViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location
{
    [self fireEvent:@"autofocus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                   [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasCameraViewController*)controller didSetFocusAtPoint:(CGPoint)location
{
    [self fireEvent:@"focus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasCameraViewController*)controller didSetTorch:(BOOL)value
{
    [self fireEvent:@"torch" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               NUMBOOL(value), @"on", nil]];
}

- (void)controller:(AkylasCameraViewController*)controller didChangeCamera:(CameraPosition)cameraPosition
{
    [self fireEvent:@"camera" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                (cameraPosition == CAMERA_REAR)?@"rear":@"front", @"position", nil]];
}

- (void)captureDidStart:(AkylasCameraViewController *)controller
{
    [self fireEvent:@"start" withObject:nil];
}

- (void)captureDidStop:(AkylasCameraViewController *)controller
{
    [self fireEvent:@"stop" withObject:nil];
}



@end
