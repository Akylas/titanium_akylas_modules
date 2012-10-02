

#import "AkylasScancodeViewProxy.h"
#import "AkylasScancodeView.h"
#import "TiUtils.h"

#import "QRCodeReader.h"
#import "EAN8Reader.h"
#import "EAN13Reader.h"
#import "MultiFormatUPCEANReader.h"
#import "MultiFormatOneDReader.h"
#import "DataMatrixReader.h"
#import "AztecReader.h"

@implementation AkylasScancodeViewProxy


NSDictionary *const symbolDict = [NSDictionary dictionaryWithObjectsAndKeys:
                                  [QRCodeReader class], @"qrcode",
                                  [EAN8Reader class], @"ean8",
                                  [MultiFormatOneDReader class], @"oned",
                                  [MultiFormatUPCEANReader class], @"upcean",
                                  [EAN13Reader class], @"ean13",
                                  [DataMatrixReader class], @"datamatrix",
                                  [AztecReader class], @"aztec"
                                  , nil];

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
//    [self replaceValue:[NSArray arrayWithObject:NUMINT(UIDataDetectorTypePhoneNumber)] forKey:@"autoDetect" notification:NO];
//    [self initializeProperty:@"willHandleTouches" defaultValue:NUMBOOL(YES)];
    [super _initWithProperties:properties];
}

-(id)init
{
	if ((self = [super init]))
	{
        _controller = [[AkylasScancodeViewController alloc] initWithDelegate:self];
	}
	return self;
}

-(void)viewDidAttach
{
    NSLog(@"viewDidAttach");
    ((AkylasScancodeView*)[self view]).scanview = _controller.view;
    [[self view] addSubview:_controller.view];
}

//-(void)viewDidDetach
//{
//	reallyAttached = NO;
//    _controller.delegate = nil;
//}


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
    NSLog(@"destroying proxy");
    [self cleanup];
//    [(AkylasScancodeView*)[self view] cleanup];
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
    NSLog(@"module proxy willAnimateRotationToInterfaceOrientation");
    [_controller willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    NSLog(@"module proxy willRotateToInterfaceOrientation");
    [_controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    NSLog(@"module proxy didRotateFromInterfaceOrientation");
    [_controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void)viewWillAppear:(BOOL)animated
{
	NSLog(@"module proxy viewWillAppear");
    [_controller viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
	NSLog(@"module proxy viewDidAppear");
    [_controller viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	NSLog(@"module proxy viewWillDisappear");
    [_controller viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	NSLog(@"module proxy viewDidDisappear");
    [_controller viewDidDisappear:animated];
}

- (void) setTorch:(BOOL)value
{
    [_controller setTorch:value];
}

- (void) setCenteredCropRect:(BOOL)value
{
    [_controller setCenteredCropRect:value];
}

- (void) setCropRect:(id)value
{
    if ([value isKindOfClass:[NSDictionary class]])
    {
        CGRect rect = CGRectZero;
        if ([value objectForKey:@"x"] && [value objectForKey:@"y"] &&
            [value objectForKey:@"width"] && [value objectForKey:@"height"])
        {
            rect.origin.x = [[value objectForKey:@"x"] integerValue];
            rect.origin.y = [[value objectForKey:@"y"] integerValue];
            rect.size.width = [[value objectForKey:@"width"] integerValue];
            rect.size.height = [[value objectForKey:@"height"] integerValue];
        }
        //        NSLog(@"setCropRect_ %@", NSStringFromCGRect(rect));
        [_controller setCropRect:rect];
    }
    else if([value isKindOfClass:[TiRect class]])
    {
        TiRect *tirect = value;
        //        NSLog(@"setCropRect_ %@", NSStringFromCGRect(tirect.rect));
        [_controller setCropRect:tirect.rect];
    }
}

- (void) setReaders:(NSArray*)value
{
        NSMutableSet *readers = [[NSMutableSet alloc ] init];
        BOOL oneD = true;
        BOOL needsFlip = false;
        for (id item in value)
        {
            NSString* sreader = [TiUtils stringValue:item];
            if ([sreader isEqualToString:@"qrcode"] || [sreader isEqualToString:@"datamatrix"] || [sreader isEqualToString:@"aztec"])
                oneD = false;
            else
                needsFlip = true;
            Class readerclass = [symbolDict objectForKey:sreader];
            //            NSLog(@"adding reader %@", NSStringFromClass(readerclass));
            if (readerclass != nil)
            {
                id reader = [[readerclass alloc] init];
                [readers addObject:reader];
                [reader release];
            }
        }
        
        needsFlip = (needsFlip && !oneD);
        _controller.readers = [NSSet setWithSet:readers];
        _controller.oneDMode = oneD;
        _controller.needsFlip = needsFlip;
        //        NSLog(@"readers oneD %d", oneD);
        //        NSLog(@"readers needsFlip %d", needsFlip);
        [readers release];
}



- (void) setCameraPosition:(id)value
{
    [_controller setCameraPosition:[AkylasScancodeViewController cameraPositionValue:value]];
}


//- (KrollContext*)krollContext
//{
//	return nil;
//}


- (void) start:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller.captureSession startRunning];
//        [_controller startCapture];
    },NO);
}

- (void) stop:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller.captureSession stopRunning];
//        [_controller stopCapture];
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

- (id) onlyOneDimension
{
    if (_controller == nil) return false;
    return NUMBOOL([_controller oneDMode]);
}

- (id) centeredCropRect
{
    if (_controller == nil) return false;
    return NUMBOOL([_controller centeredCropRect]);
}

- (id) cropRect
{
    TiRect *result = [[[TiRect alloc] init] autorelease];
    if (_controller != nil)
        [result setRect:([_controller cropRect])];
    return result;
}

- (void) flush:(id)args
{
    TiThreadPerformOnMainThread(^{
        [_controller flush];
    },NO);
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

#define pragma mark - AkylasScancodeViewControllerDelegate

- (void)controller:(AkylasScancodeViewController*)controller didScanResult:(NSDictionary *)result
{
    NSMutableArray* points = [NSMutableArray array];
    for( NSValue* value in [result objectForKey:@"points"] )
    {
        [points addObject:[[[TiPoint alloc] initWithPoint:[value CGPointValue]] autorelease]];
    }
    TiRect *tiRect = [[[TiRect alloc] init] autorelease];
    [tiRect setRect:[[result objectForKey:@"cropRect"] CGRectValue]];
    
    TiBlob *blob = [[[TiBlob alloc] initWithImage:[result objectForKey:@"image"]] autorelease];
    [self fireEvent:@"scan" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                              [result objectForKey:@"message"],@"message",
                                              points, @"points",
                                              blob, @"image",
                                              tiRect,@"cropRect",
                                              nil]];
}
- (void)controller:(AkylasScancodeViewController*)controller didFoundPossibleResultPoint:(CGPoint)point
{
    [self fireEvent:@"possiblepoint" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                       [[[TiPoint alloc] initWithPoint:point] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location
{
    [self fireEvent:@"autofocus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                   [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetFocusAtPoint:(CGPoint)location
{
    [self fireEvent:@"focus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetTorch:(BOOL)value
{
    [self fireEvent:@"torch" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               NUMBOOL(value), @"on", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didChangeCamera:(CameraPosition)cameraPosition
{
    [self fireEvent:@"camera" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                (cameraPosition == CAMERA_REAR)?@"rear":@"front", @"position", nil]];
}

- (void)captureDidStart:(AkylasScancodeViewController *)controller
{
    [self fireEvent:@"start" withObject:nil];
}

- (void)captureDidStop:(AkylasScancodeViewController *)controller
{
    [self fireEvent:@"stop" withObject:nil];
}



@end
