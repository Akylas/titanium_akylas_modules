#import "AkylasScancodeView.h"
#import "TiApp.h"

#import "QRCodeReader.h"
#import "EAN8Reader.h"
#import "EAN13Reader.h"
#import "MultiFormatUPCEANReader.h"
#import "MultiFormatOneDReader.h"
#import "DataMatrixReader.h"
#import "AztecReader.h"

@implementation AkylasScancodeView

NSDictionary *const symbolDict = [NSDictionary dictionaryWithObjectsAndKeys:
                                  [QRCodeReader class], @"qrcode",
                                  [EAN8Reader class], @"ean8",
                                  [MultiFormatOneDReader class], @"oned",
                                  [MultiFormatUPCEANReader class], @"upcean",
                                  [EAN13Reader class], @"ean13",
                                  [DataMatrixReader class], @"datamatrix",
                                  [AztecReader class], @"aztec"
                                  , nil];

-(UIView*)scanview
{
	if (scanview==nil)
	{
        _controller = [[AkylasScancodeViewController alloc] initWithDelegate:self];
        scanview = _controller.view;
        [self addSubview:scanview];
	}
	return scanview;
}

-(void)dealloc
{
//	if (listeners!=nil)
//	{
//		RELEASE_TO_NIL(listeners);
//	}
	RELEASE_TO_NIL(scanview);
	RELEASE_TO_NIL(_controller);
	[super dealloc];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [TiUtils setView:[self scanview] positionRect:bounds];
    [super frameSizeChanged:frame bounds:bounds];
}

-(BOOL)hasTouchableListener
{
	// since this guy only works with touch events, we always want them
	// just always return YES no matter what listeners we have registered
	return YES;
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];
    if (hitView == [self scanview])
        return self;
    return hitView;
}


-(CGFloat)autoWidthForWidth:(CGFloat)value
{
	return [[self scanview] sizeThatFits:CGSizeMake(value, 0)].width;
}

-(CGFloat)autoHeightForWidth:(CGFloat)value
{
	return [[self scanview] sizeThatFits:CGSizeMake(value, 0)].height;
}

-(void)setBackgroundColor_:(id)value
{
	if (value!=nil)
	{
		TiColor *color = [TiUtils colorValue:value];
		[[self scanview] setBackgroundColor:[color _color]];
	}
}

- (void)viewWillAppear:(BOOL)animated
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller viewWillAppear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller viewWillAppear:animated];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module willAnimateRotationToInterfaceOrientation");
    if ([self scanview]) //ensure _controller is initiated
        [_controller willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module willRotateToInterfaceOrientation");
    if ([self scanview]) //ensure _controller is initiated
        [_controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
//    NSLog(@"module didRotateFromInterfaceOrientation");
    if ([self scanview]) //ensure _controller is initiated
        [_controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void) setTorch_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL bval = [value boolValue];
//    NSLog(@"setTorch_ %@", value);
    if ([self scanview]) //ensure _controller is initiated
        [_controller setTorch:bval];
}

- (void) setCenteredCropRect_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL bval = [value boolValue];
//    NSLog(@"setCenteredCropRect_ %@", value);
    if ([self scanview]) //ensure _controller is initiated
        [_controller setCenteredCropRect:bval];
}

- (void) setCropRect_:(id)value
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
        if ([self scanview] && value != nil) //ensure _controller is initiated
            [_controller setCropRect:rect];
    }
    else if([value isKindOfClass:[TiRect class]])
    {
        TiRect *tirect = value;
//        NSLog(@"setCropRect_ %@", NSStringFromCGRect(tirect.rect));
        if ([self scanview] && tirect != nil) //ensure _controller is initiated
            [_controller setCropRect:tirect.rect];

    }
}

- (void) setReaders_:(id)value
{
    ENSURE_TYPE_OR_NIL(value,NSArray);
//    NSLog(@"setReaders_ %@", (NSArray*)value);
    if ([self scanview]) //ensure _controller is initiated
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
}



- (void) setCameraPosition_:(id)value
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller setCameraPosition:[AkylasScancodeViewController cameraPositionValue:value]];
}

- (void) start
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller.captureSession startRunning];
}

- (void) stop
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller.captureSession stopRunning];
}

- (BOOL) torchOn
{
    if ([self scanview]) //ensure _controller is initiated
        return _controller.torchIsOn;
}

- (BOOL) onlyOneDimension
{
    if ([self scanview]) //ensure _controller is initiated
        return _controller.oneDMode;
}

- (BOOL) centeredCropRect
{
    if ([self scanview]) //ensure _controller is initiated
        return _controller.centeredCropRect;
}

- (int) cameraPosition
{
    if ([self scanview]) //ensure _controller is initiated
        return _controller.cameraPosition;
}


- (CGRect) cropRect
{
    if ([self scanview]) //ensure _controller is initiated
        return _controller.cropRect;
}

- (void) flush
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller flush];
}

- (void) swapCamera
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller swapCamera];
}

- (void) focus:(CGPoint)point
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller focusAtPoint:point];
}

- (void) autoFocus:(CGPoint)point
{
    if ([self scanview]) //ensure _controller is initiated
        [_controller autoFocusAtPoint:point];
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
    [self.proxy fireEvent:@"scan" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                              [result objectForKey:@"message"],@"message",
                                              points, @"points",
                                              blob, @"image",
                                              tiRect,@"cropRect",
                                              nil]];
}
- (void)controller:(AkylasScancodeViewController*)controller didFoundPossibleResultPoint:(CGPoint)point
{
    [self.proxy fireEvent:@"possiblepoint" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                              [[[TiPoint alloc] initWithPoint:point] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location
{
    [self.proxy fireEvent:@"autofocus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetFocusAtPoint:(CGPoint)location
{
    [self.proxy fireEvent:@"focus" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                         [[[TiPoint alloc] initWithPoint:location] autorelease], @"point", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didSetTorch:(BOOL)value
{
    [self.proxy fireEvent:@"torch" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                               NUMBOOL(value), @"on", nil]];
}

- (void)controller:(AkylasScancodeViewController*)controller didChangeCamera:(CameraPosition)cameraPosition
{
    [self.proxy fireEvent:@"camera" withObject:[NSDictionary dictionaryWithObjectsAndKeys:
                                                (cameraPosition == CAMERA_REAR)?@"rear":@"front", @"position", nil]];
}

- (void)captureDidStart:(AkylasScancodeViewController *)controller
{
    [self.proxy fireEvent:@"start" withObject:nil];
}

- (void)captureDidStop:(AkylasScancodeViewController *)controller
{
    [self.proxy fireEvent:@"stop" withObject:nil];
}

//- (void) serviceManagerWillLoadResources
//{
//    [self.proxy fireEvent:@"loadingResources" withObject:nil];
//}
//
//- (void) serviceManagerDidDownload3dZip:(NSString *)filePath forModel:(NSString *)model
//{
//    [self.proxy fireEvent:@"3dzipdownloaded" withObject:[NSDictionary dictionaryWithObjectsAndKeys:filePath,@"path", model, @"model", nil]];
//}

@end
