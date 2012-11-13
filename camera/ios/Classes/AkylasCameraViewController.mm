
#import "AkylasCameraViewController.h"
#include <sys/types.h>
#include <sys/sysctl.h>
#include <sys/utsname.h>

#import <AVFoundation/AVFoundation.h>

#define CAMERA_SCALAR 1.12412 // scalar = (480 / (2048 / 480))
#define FIRST_TAKE_DELAY 1.0

@interface AkylasCameraViewController ()

- (void)initCapture;
- (void)stopCapture;

@end

@implementation AkylasCameraViewController

//#if HAS_AVFF
@synthesize captureSession;
//#endif
@synthesize delegate;
@synthesize preview;

- (id)initWithDelegate:(id<AkylasCameraViewControllerDelegate>)scanDelegate  {
    self = [super init];
    if (self) {
        NSLog(@"init");
        [self setDelegate:scanDelegate];
        _cameraPosition = AVCaptureDevicePositionBack;
        rotating = NO;
        mirrored = false;
        needsToTakePicture = false;
        torch = false;
    }
    
    return self;
}

- (void)cleanup {
    NSLog(@"cleanup");
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self stopCapture];
    [preview release];
    preview = nil;
}

- (void)dealloc {
        
    [self cleanup];
    [super dealloc];
}

- (void)loadView
{
    //    NSLog(@"loadview");
    CameraPreview* theCaptureView = [[CameraPreview alloc] initWithFrame:CGRectZero];
    self.preview = theCaptureView;
    self.view = theCaptureView;
    [theCaptureView release];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
}

-(void)viewDidUnload
{
    [self cleanup];
    [super viewDidUnload];
}

- (NSString *)getPlatform {
    struct utsname systemInfo;
    uname(&systemInfo);
    NSString* platform =  [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    
    return platform;
}

- (BOOL)fixedFocus {
    NSString *platform = [self getPlatform];
    if ([platform isEqualToString:@"iPhone1,1"] ||
        [platform isEqualToString:@"iPhone1,2"]) return YES;
    return NO;
}



- (void)viewWillAppear:(BOOL)animated {
}

- (void)viewWillDisappear:(BOOL)animated {
}


- (void)viewDidAppear:(BOOL)animated {    
    [self initCapture];
}

- (void)viewDidDisappear:(BOOL)animated {
    [self stopCapture];
    [super viewDidDisappear:animated];
}

-(UIView *) hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    return nil;
}

//- (BOOL) shouldAutorotateToInterfaceOrientation: (UIInterfaceOrientation) orient
//{
//    return YES;
//}

- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) orient
                                 duration: (NSTimeInterval) duration
{
//    NSLog(@"willRotate: orient=%d #%g", orient, duration);
    rotating = YES;
    if(preview)
        [preview willRotateToInterfaceOrientation: orient
                                         duration: duration];
}

- (void) willAnimateRotationToInterfaceOrientation: (UIInterfaceOrientation) orient
                                          duration: (NSTimeInterval) duration
{
//    NSLog(@"willAnimateRotation: orient=%d #%g", orient, duration);
    rotating = YES;
    if(preview)
    {
        [preview willRotateToInterfaceOrientation: orient
                                         duration: duration];
        [preview setNeedsLayout];
    }
}

-(void)captureSessionDidStart
{
    if (delegate!= nil)
        [delegate captureDidStart:self];
}

-(void)captureSessionDidStop
{
    if (delegate!= nil)
        [delegate captureDidStop:self];
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) orient
{
//    NSLog(@"didRotate(%d): orient=%d", rotating, orient);
    if(!rotating && preview) {
        // work around UITabBarController bug: willRotate is not called
        // for non-portrait initial interface orientation
        [preview willRotateToInterfaceOrientation: self.interfaceOrientation
                                         duration: 0];
        [preview setNeedsLayout];
    }
    rotating = NO;
}


#pragma mark -
#pragma mark AVFoundation

#include <sys/types.h>
#include <sys/sysctl.h>

// Gross, I know. But you can't use the device idiom because it's not iPad when running
// in zoomed iphone mode but the camera still acts like an ipad.
#if HAS_AVFF
static bool isIPad() {
    static int is_ipad = -1;
    if (is_ipad < 0) {
        struct utsname systemInfo;
        uname(&systemInfo);
        NSString* platform =  [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
        
        is_ipad = [platform hasPrefix:@"iPad"];
    }
    return !!is_ipad;
}
#endif

- (void)initCapture {
#if HAS_AVFF
    AVCaptureDevice* inputDevice = [ self cameraWithPosition : _cameraPosition ];
    mirrored = ( _cameraPosition == AVCaptureDevicePositionFront );

    AVCaptureDeviceInput *captureInput = [AVCaptureDeviceInput deviceInputWithDevice:inputDevice error:nil];
    
    if ( torch)
    {
        [inputDevice lockForConfiguration:nil];
        [inputDevice setTorchMode:AVCaptureTorchModeOn];
        [inputDevice unlockForConfiguration];
    }
    
    AVCaptureVideoDataOutput *captureOutput = [[AVCaptureVideoDataOutput alloc] init];
    captureOutput.alwaysDiscardsLateVideoFrames = YES;
    [captureOutput setSampleBufferDelegate:self queue:dispatch_get_main_queue()];
    NSString* key = (NSString*)kCVPixelBufferPixelFormatTypeKey;
    NSNumber* value = [NSNumber numberWithUnsignedInt:kCVPixelFormatType_32BGRA];
    NSDictionary* videoSettings = [NSDictionary dictionaryWithObject:value forKey:key];
    [captureOutput setVideoSettings:videoSettings];
    self.captureSession = [[[AVCaptureSession alloc] init] autorelease];
    
    NSString* preset = 0;
    if (NSClassFromString(@"NSOrderedSet") && // Proxy for "is this iOS 5" ...
        [UIScreen mainScreen].scale > 1 &&
        isIPad() &&
        [inputDevice
         supportsAVCaptureSessionPreset:AVCaptureSessionPresetiFrame960x540]) {
            preset = AVCaptureSessionPresetiFrame960x540;
        }
    if (!preset) {
        preset = AVCaptureSessionPresetMedium;
    }
    self.captureSession.sessionPreset = preset;
    
    [self.captureSession addInput:captureInput];
    [self.captureSession addOutput:captureOutput];
    
    [captureOutput release];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStart)
     name:AVCaptureSessionDidStartRunningNotification
     object:self.captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStart)
     name:AVCaptureSessionInterruptionEndedNotification
     object:self.captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStop)
     name:AVCaptureSessionDidStopRunningNotification
     object:self.captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStop)
     name:AVCaptureSessionWasInterruptedNotification
     object:self.captureSession];
    
    if (!self.preview.prevLayer) {
        self.preview.prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:self.captureSession];
    }
    
    [self.captureSession startRunning];
#endif
}

-(AVCaptureDevice*) cameraWithPosition:(AVCaptureDevicePosition)position
{
    NSArray * Devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo] ;
    for (AVCaptureDevice* Device in Devices)
        if (Device.position == position )
            return Device ;
    return nil ;
}

- ( void ) updateCameraPosition:(int) cameraPosition
{
    int oldPosition = _cameraPosition;
    _cameraPosition = cameraPosition;

#if HAS_AVFF
    torch = false; //switching turns the torch off
    //Assume the session is already running
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * Device = INPUT.device ;
        if ( [ Device hasMediaType : AVMediaTypeVideo ] ) {
            AVCaptureDevicePosition position = Device.position ;
            AVCaptureDevice * newCamera = nil ;
            AVCaptureDeviceInput * newInput = nil ;
            
            if ( cameraPosition == AVCaptureDevicePositionFront )
            {
                if (position == AVCaptureDevicePositionFront) return; //nothing to do
                newCamera = [ self cameraWithPosition : cameraPosition ] ;
                mirrored = true;
            }
            else if ( cameraPosition == AVCaptureDevicePositionBack )
            {
                if (position == AVCaptureDevicePositionBack) return; //nothing to do
                newCamera = [ self cameraWithPosition : cameraPosition ] ;
                mirrored = false;
            }
            
            if (newCamera == nil)
            {
                _cameraPosition = oldPosition;
                return; //failure
            }
            
            
            newInput = [AVCaptureDeviceInput deviceInputWithDevice:newCamera error:nil ] ;
            
            //beginConfiguration ensures that pending changes are not applied immediately
            [self.captureSession beginConfiguration] ;
            
            [self.captureSession removeInput:INPUT] ;
            [self.captureSession addInput:newInput] ;
            
            //Changes take effect once the outermost commitConfiguration is invoked.
            [self.captureSession commitConfiguration] ;
            
            id localDel = [self delegate];
            if ([localDel respondsToSelector:@selector(controller:didChangeCamera:)])
            {
                [localDel controller:self didChangeCamera:cameraPosition];
            }
            break ;
        }
    }
#endif
}

-(void)updateCameraSessionPreset:(NSString*)preset
{
#if HAS_AVFF
    //Assume the session is already running
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * Device = INPUT.device ;
        if ( [ Device hasMediaType : AVMediaTypeVideo ] ) {
            //beginConfiguration ensures that pending changes are not applied immediately
            [self.captureSession beginConfiguration] ;
            
            self.captureSession.sessionPreset = preset;
            
            //Changes take effect once the outermost commitConfiguration is invoked.
            [self.captureSession commitConfiguration] ;

            break ;
        }
    }
#endif

}

- (CGPoint)rotatePoint:(CGPoint)point withinRect:(CGRect)rect withAngle: (CGFloat)angle
{
    CGPoint center;
    center.x = rect.size.width/2;
    center.y = rect.size.height/2;
    float x = point.x - center.x;
    float y = point.y - center.y;
    int modAngle = ((int)angle % 360);
    switch(modAngle) {
        case 90: // 90
            point.x = -y;
            point.y = x;
            point.x += center.y;
            point.y += center.x;
            break;
        case 180: //180 degrees
            point.x = -x;
            point.y = -y;
            point.x += center.x;
            point.y += center.y;
            break;
        case 270: //-90 degrees
            point.x = y;
            point.y = -x;
            point.x += center.y;
            point.y += center.x;
            break;
        case 0: //0 degrees
            point.x = x;
            point.y = y;
            point.x += center.x;
            point.y += center.y;
            break;
    }
    
    return point;
}

- (CGFloat)capturedImageRotation:(UIInterfaceOrientation)orient forCamera:(int)cameraPosition
{
    CGFloat angle;
    switch(orient) {
        case UIInterfaceOrientationPortraitUpsideDown: // -90
            angle = 270;
            break;
        case UIInterfaceOrientationLandscapeLeft: //180
            angle = 180;
            break;
        case UIInterfaceOrientationPortrait: // 90
            angle = 90;
            break;
        case UIInterfaceOrientationLandscapeRight:
            angle = 0;
            break;
    }
    if (cameraPosition == AVCaptureDevicePositionFront) angle = 180.0f - angle;
    return angle;
}

-(CGRect) getPreviewRectInImage
{
    CGRect previewCropInImage;
    
    CGSize psize;
    if(UIInterfaceOrientationIsPortrait(_currentOrientation))
    {
        psize = CGSizeMake(self.preview.bounds.size.height, self.preview.bounds.size.width);
    }
    else
    {
        psize = self.preview.bounds.size;
    }
    // FIXME assumes AVLayerVideoGravityResizeAspectFill
    if (_currentImageSize.height > 0.0f && self.preview.bounds.size.height > 0.0f)
    {
        CGFloat imgRatio = _currentImageSize.width / _currentImageSize.height;
        CGFloat previewRatio = psize.width / psize.height;
        if(imgRatio < previewRatio)
        {
            _currentScale = _currentImageSize.width / psize.width;
            previewCropInImage.size = CGSizeMake(_currentImageSize.width, floorf(psize.height * _currentScale));
        }
        else
        {
            _currentScale = _currentImageSize.height / psize.height;
            previewCropInImage.size = CGSizeMake(floorf( psize.width * _currentScale), _currentImageSize.height);
        }
    }
    else
    {
        _currentScale = 1;
        previewCropInImage.size = _currentImageSize;
    }
    previewCropInImage.origin = CGPointMake((_currentImageSize.width - previewCropInImage.size.width) / 2.0f,
                                            (_currentImageSize.height - previewCropInImage.size.height) / 2.0f);
    return previewCropInImage;
}

#if HAS_AVFF
- (void)captureOutput:(AVCaptureOutput *)captureOutput
didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
       fromConnection:(AVCaptureConnection *)connection
{
    CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
    /*Lock the image buffer*/
    CVPixelBufferLockBaseAddress(imageBuffer,0);
    /*Get information about the image*/
    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
    size_t width = CVPixelBufferGetWidth(imageBuffer);
    size_t height = CVPixelBufferGetHeight(imageBuffer);
    
    uint8_t* baseAddress = (uint8_t*)CVPixelBufferGetBaseAddress(imageBuffer);
    void* free_me = 0;
    if (true) { // iOS bug?
        uint8_t* tmp = baseAddress;
        int bytes = bytesPerRow*height;
        free_me = baseAddress = (uint8_t*)malloc(bytes);
        baseAddress[0] = 0xdb;
        memcpy(baseAddress,tmp,bytes);
    }
    
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef newContext =
    CGBitmapContextCreate(baseAddress, width, height, 8, bytesPerRow, colorSpace,
                          kCGBitmapByteOrder32Little | kCGImageAlphaNoneSkipFirst);
    
    CGImageRef capture = CGBitmapContextCreateImage(newContext);
    //    [(id)capture autorelease];
    CVPixelBufferUnlockBaseAddress(imageBuffer,0);
    free(free_me);
    
    CGContextRelease(newContext);
    CGColorSpaceRelease(colorSpace);
   
    // N.B.
    // - iOS always takes videos in landscape
    // - images are always 4x3; device is not
    // - iOS uses virtual pixels for non-image stuff
    //Picture taken is always landscape (Right rear and left front)!       
    CGSize psize;
    BOOL flipped;
    _currentOrientation = self.preview.interfaceOrientation;
    if(UIInterfaceOrientationIsPortrait(_currentOrientation))
    {
        flipped = false;
        horizontal = false;
        psize = CGSizeMake(self.preview.bounds.size.height, self.preview.bounds.size.width);
    }
    else
    {
        flipped = true;
        horizontal = true;
        psize = self.preview.bounds.size;
    }
    _currentRotation = [self capturedImageRotation:_currentOrientation forCamera:_cameraPosition];

    
    _currentImageSize = CGSizeMake(CGImageGetWidth(capture), CGImageGetHeight(capture));
    
    _currentPreviewRectInImage = [self getPreviewRectInImage];
    
    nightydegreesrotationadded = flipped;
    
//    if (nightydegreesrotationadded)
//    {
//        _currentRotation += 90;
//    }
    if (needsToTakePicture)
    {
        needsToTakePicture = false;
        id localDel = [self delegate];
        if ([localDel respondsToSelector:@selector(controller:didTakePicture:withRotation:)])
        {
            [localDel controller:self didTakePicture:[UIImage imageWithCGImage:capture] withRotation:_currentRotation];
        }
    }
    CGImageRelease(capture);
}
#endif

- (void)stopCapture {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    if (captureSession == nil) return;
#if HAS_AVFF
    [captureSession stopRunning];
    AVCaptureInput* input = [captureSession.inputs objectAtIndex:0];
    [captureSession removeInput:input];
    AVCaptureVideoDataOutput* output = (AVCaptureVideoDataOutput*)[captureSession.outputs objectAtIndex:0];
    [captureSession removeOutput:output];
    [self.preview.prevLayer removeFromSuperlayer];
    
    self.preview.prevLayer = nil;
    self.captureSession = nil;
#endif
}

#pragma mark - Torch

- (void)setTorch:(BOOL)status {
    torch = status;
#if HAS_AVFF
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        [device lockForConfiguration:nil];
        if ( [device hasTorch] ) {
            BOOL didchange = false;
            if ( status && [device torchMode] != AVCaptureTorchModeOn ) {
                [device setTorchMode:AVCaptureTorchModeOn];
                didchange = true;
            } else if( !status && [device torchMode] != AVCaptureTorchModeOff ){
                [device setTorchMode:AVCaptureTorchModeOff];
                didchange = true;
            }
            if (didchange)
            {
                id localDel = [self delegate];
                if ([localDel respondsToSelector:@selector(controller:didSetTorch:)])
                {
                    [localDel controller:self didSetTorch:status];
                }
            }
        }
        [device unlockForConfiguration];
    }
#endif
}
// Convert from view coordinates to camera coordinates, where {0,0} represents the top left of the picture area, and {1,1} represents
// the bottom right in landscape mode with the home button on the right.
- (CGPoint)convertToPointOfInterestFromViewCoordinates:(CGPoint)viewCoordinates forInput:(AVCaptureDeviceInput*) input
{
    if (CGRectIsNull(_currentPreviewRectInImage) || CGRectIsNull(_currentPreviewRectInImage))
        return;
    
    AVCaptureVideoPreviewLayer *videoPreviewLayer = [self preview].prevLayer;
    CGSize frameSize = [[self preview] frame].size;
    
    if ([videoPreviewLayer isMirrored]) {
        viewCoordinates.x = frameSize.width - viewCoordinates.x;
    }

    CGPoint realPoint = [self rotatePoint:viewCoordinates withinRect:self.preview.bounds withAngle:(-_currentRotation + 360.0f)];
    realPoint.x = (realPoint.x * _currentScale + _currentPreviewRectInImage.origin.x);
    realPoint.y = realPoint.y * _currentScale + _currentPreviewRectInImage.origin.y;
    
    if (_currentImageSize.width > 0 && _currentImageSize.height > 0)
    {
        realPoint.x /= _currentImageSize.width;
        realPoint.y /= _currentImageSize.height;
    }
    if (realPoint.x < 0.0f || realPoint.x > 1.0f ||
        realPoint.y < 0.0f || realPoint.y > 1.0f)
        realPoint = CGPointMake(.5f, .5f);

    
    return realPoint;
}

-(void)focusAtPoint:(CGPoint)point
{
#if HAS_AVFF
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus])
        {
            NSError *error;
            if ([device lockForConfiguration:&error]) {
                [device setFocusPointOfInterest:[self convertToPointOfInterestFromViewCoordinates:point forInput:INPUT]];
                [device setFocusMode:AVCaptureFocusModeAutoFocus];
                [device unlockForConfiguration];
                
                id localDel = [self delegate];
                if ([localDel respondsToSelector:@selector(controller:didSetFocusAtPoint:)])
                {
                    [localDel controller:self didSetFocusAtPoint:point];
                }
            }        
        }
    }
#endif
}

-(void)autoFocusAtPoint:(CGPoint)point
{
#if HAS_AVFF
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus])
        {
            NSError *error;
            if ([device lockForConfiguration:&error]) {
                [device setFocusPointOfInterest:[self convertToPointOfInterestFromViewCoordinates:point forInput:INPUT]];
                [device setFocusMode:AVCaptureFocusModeContinuousAutoFocus];
                [device unlockForConfiguration];
                
                id localDel = [self delegate];
                if ([localDel respondsToSelector:@selector(controller:didSetAutoFocusAtPoint:)])
                {
                    [localDel controller:self didSetAutoFocusAtPoint:point];
                }
            }
        }
    }
#endif
}

- (BOOL)torchIsOn {
    return torch;
}

-(void)swapCamera
{
    if (_cameraPosition == AVCaptureDevicePositionFront)
        [self setCameraPosition:AVCaptureDevicePositionBack];
    else
        [self setCameraPosition:AVCaptureDevicePositionFront];
}

+(int)cameraPositionValue:(id)pos
{
	int newPos = AVCaptureDevicePositionFront;
    
	if ([pos isKindOfClass:[NSString class]])
	{
		if ([pos isEqualToString:@"back"])
		{
			newPos = AVCaptureDevicePositionBack;
		}
		else if ([pos isEqualToString:@"front"])
		{
			newPos = AVCaptureDevicePositionFront;
		}
	}
	else if ([pos isKindOfClass:[NSNumber class]])
	{
		newPos = [pos intValue];
        if (newPos != AVCaptureDevicePositionFront && newPos != AVCaptureDevicePositionBack)
            newPos = AVCaptureDevicePositionBack;
	}
	return newPos;
}

-(void)setCameraPosition:(int)cameraPosition
{
    if (_cameraPosition == cameraPosition)
        return;
    [self updateCameraPosition:cameraPosition];
}

-(int) cameraPosition
{
    return _cameraPosition;
}

-(void)takePicture
{
    needsToTakePicture  = true;
}
@end
