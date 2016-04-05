#import "AkylasCameraView.h"
#import "AkylasCameraViewProxy.h"
#import "TiApp.h"
#import "TiUtils.h"
#import "CameraPreview.h"
#import "TiFileSystemHelper.h"

#include <sys/types.h>
#include <sys/sysctl.h>
#import <ImageIO/ImageIO.h>

#define IMG_PREFIX @"ak_camera_image"

@implementation AkylasCameraView
{
    CameraPreview *preview;
    //    id<AkylasCameraViewControllerDelegate> delegate;
    AVCaptureSession *captureSession;
    AVCaptureStillImageOutput* stillImageOutput;
    BOOL rotating;
    BOOL mirrored;
//    BOOL horizontal;
//    BOOL nightydegreesrotationadded;
    CGSize _currentImageSize;
    CGRect _currentPreviewRectInImage;
    CGFloat _currentRotation;
    CGFloat _currentScale;
    UIInterfaceOrientation _currentOrientation;
    NSInteger _cameraPosition;
    AVCaptureFlashMode _flash;
    BOOL needsToTakePicture;
    BOOL _torch;
    
    BOOL _isMirrored;
    NSString* _quality;
    AVCaptureWhiteBalanceMode _whitebalance;
    AVCaptureExposureMode _exposure;

}

-(id)init
{
    if ((self = [super init]))
    {
        _cameraPosition = AVCaptureDevicePositionBack;
        rotating = NO;
        mirrored = NO;
        needsToTakePicture = NO;
        _torch = NO;
        _flash = AVCaptureFlashModeAuto;
        preview = [[CameraPreview alloc] initWithFrame:CGRectZero];
        _quality = AVCaptureSessionPresetPhoto;
        _whitebalance = AVCaptureWhiteBalanceModeContinuousAutoWhiteBalance;
        _exposure = AVCaptureExposureModeContinuousAutoExposure;
        [self addSubview:preview];
    }
    return self;
}

-(void) cleanup
{
    [self stopCapture];
}

-(void)removeFromSuperview {
    [self stopCapture];
    [super removeFromSuperview];
}

-(void)configurationSet
{
    [super configurationSet];
    [self initCapture];
}


-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [preview willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}


-(void)dealloc
{
    NSLog(@"view dealloc");
    [self cleanup];
	[super dealloc];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [TiUtils setView:preview positionRect:bounds];
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
    if (hitView == preview)
        return self;
    return hitView;
}



-(CGSize)contentSizeForSize:(CGSize)size
{
    return [preview sizeThatFits:size];
}

-(void)setBackgroundColor_:(id)value
{
	if (value!=nil)
	{
		TiColor *color = [TiUtils colorValue:value];
		[preview setBackgroundColor:[color _color]];
	}
}

- (void) start
{
    [captureSession startRunning];
}

- (void) stop
{
    [captureSession stopRunning];
}

- (void) setTorch_:(id)value
{
    BOOL torch = [TiUtils boolValue:value def:_torch];
    if (torch == _torch) {
        return;
    }
    _torch = torch;
    if ([self isConfigurationSet]) {
        [self updateTorch];
    }
//    TiThreadPerformBlockOnMainThread(^{
//    },NO);
}

- (void) setWhiteBalance_:(id)value
{
    NSInteger whitebalance = [TiUtils intValue:value def:_whitebalance];
    if (whitebalance == _whitebalance) {
        return;
    }
    _whitebalance = whitebalance;
    if ([self isConfigurationSet]) {
        [self updateAdjustWhiteBalance];
    }

}

- (void) setExposure_:(id)value
{
    NSInteger exposure = [TiUtils intValue:value def:_exposure];
    if (exposure == _exposure) {
        return;
    }
    _exposure = exposure;
    if ([self isConfigurationSet]) {
        [self updateExposure];
    }
    
}

-(void)setWhichCamera_:(id)value
{
    NSInteger cameraPosition = [TiUtils intValue:value def:_cameraPosition];
    if (cameraPosition == _cameraPosition) {
        return;
    }
    _cameraPosition = cameraPosition;
    
//    TiThreadPerformBlockOnMainThread(^{
    if ([self isConfigurationSet]) {
        [self updateCameraPosition];
    }
//    },NO);
}

-(void)setFlash_:(id)value
{
    AVCaptureFlashMode flash = [TiUtils intValue:value def:_flash];
    if (flash == _flash) {
        return;
    }
    _flash = flash;
//    TiThreadPerformBlockOnMainThread(^{
    if ([self isConfigurationSet]) {
        [self updateFlash];
    }
//    },NO);
}

-(void)setQuality_:(id)value
{
    NSString* quality = [TiUtils stringValue:value def:_quality];
    if (quality == _quality) {
        return;
    }
    _quality = quality;
    if ([self isConfigurationSet]) {
    [self updateCameraSessionPreset];
    }
    
}

#pragma mark AVFoundation


- (void)initCapture {
    if (captureSession) {
        return;
    }
#if HAS_AVFF
    AVCaptureDevice* inputDevice = [ self cameraWithPosition : _cameraPosition ];
    mirrored = ( _cameraPosition == AVCaptureDevicePositionFront );
    
    AVCaptureDeviceInput *captureInput = [AVCaptureDeviceInput deviceInputWithDevice:inputDevice error:nil];
    
    [inputDevice lockForConfiguration:nil];
    if ([inputDevice hasTorch]) {
        if (_torch)
        {
            [inputDevice setTorchMode:AVCaptureTorchModeOn];
        }
    }
    if ([inputDevice hasFlash]) {
        [inputDevice setFlashMode:_flash];
    }
    if ( [inputDevice isExposureModeSupported:_exposure] ) {
        [inputDevice setExposureMode:_exposure];
    }
    if ( [inputDevice isWhiteBalanceModeSupported:_whitebalance] ) {
        [inputDevice setWhiteBalanceMode:_whitebalance];
    }
    [inputDevice unlockForConfiguration];
    
    AVCaptureVideoDataOutput *captureOutput = [[AVCaptureVideoDataOutput alloc] init];
    captureOutput.alwaysDiscardsLateVideoFrames = YES;
    [captureOutput setSampleBufferDelegate:self queue:dispatch_get_main_queue()];
    NSString* key = (NSString*)kCVPixelBufferPixelFormatTypeKey;
    NSNumber* value = [NSNumber numberWithUnsignedInt:kCVPixelFormatType_32BGRA];
    NSDictionary* videoSettings = [NSDictionary dictionaryWithObject:value forKey:key];
    [captureOutput setVideoSettings:videoSettings];
    captureSession = [[AVCaptureSession alloc] init];
    

    captureSession.sessionPreset = _quality;
    
    [captureSession addInput:captureInput];
    [captureSession addOutput:captureOutput];
    
    stillImageOutput = [[AVCaptureStillImageOutput alloc] init]; //when the photo gets taken
    NSDictionary *outputSettings = [[NSDictionary alloc] initWithObjectsAndKeys:AVVideoCodecJPEG, AVVideoCodecKey, nil];
    [stillImageOutput setOutputSettings:outputSettings];
    [captureSession addOutput:stillImageOutput];
    
    [captureOutput release];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStart)
     name:AVCaptureSessionDidStartRunningNotification
     object:captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStart)
     name:AVCaptureSessionInterruptionEndedNotification
     object:captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStop)
     name:AVCaptureSessionDidStopRunningNotification
     object:captureSession];
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(captureSessionDidStop)
     name:AVCaptureSessionWasInterruptedNotification
     object:captureSession];
    
    if (!preview.prevLayer) {
        preview.prevLayer = [AVCaptureVideoPreviewLayer layerWithSession:captureSession];
    }
    
    [captureSession startRunning];
#endif
}

-(void)captureSessionDidStart
{
    [[self viewProxy] fireEvent:@"start" withObject:nil propagate:NO];

}

-(void)captureSessionDidStop
{
    [[self viewProxy] fireEvent:@"stop" withObject:nil propagate:NO];
}


-(AVCaptureDevice*) cameraWithPosition:(AVCaptureDevicePosition)position
{
    NSArray * Devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo] ;
    for (AVCaptureDevice* Device in Devices)
        if (Device.position == position )
            return Device ;
    return nil ;
}

- (void)updateCameraPosition
{
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    _torch = NO;
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * Device = INPUT.device ;
        if ( [ Device hasMediaType : AVMediaTypeVideo ] ) {
            AVCaptureDevicePosition position = Device.position ;
            AVCaptureDevice * newCamera = nil ;
            AVCaptureDeviceInput * newInput = nil ;
            
            if ( _cameraPosition == AVCaptureDevicePositionFront )
            {
                if (position == AVCaptureDevicePositionFront) return; //nothing to do
                newCamera = [ self cameraWithPosition : _cameraPosition ] ;
                mirrored = true;
            }
            else if ( _cameraPosition == AVCaptureDevicePositionBack )
            {
                if (position == AVCaptureDevicePositionBack) return; //nothing to do
                newCamera = [ self cameraWithPosition : _cameraPosition ] ;
                mirrored = false;
            }
            
            if (newCamera == nil)
            {
                _cameraPosition = position;
                return; //failure
            }
            
            
            newInput = [AVCaptureDeviceInput deviceInputWithDevice:newCamera error:nil ] ;
            
            //beginConfiguration ensures that pending changes are not applied immediately
            [captureSession beginConfiguration] ;
            
            [captureSession removeInput:INPUT] ;
            [captureSession addInput:newInput] ;
            
            //Changes take effect once the outermost commitConfiguration is invoked.
            [captureSession commitConfiguration] ;
            if ([[self viewProxy] _hasListeners:@"camera" checkParent:NO]) {
                [[self viewProxy] fireEvent:@"camera" withObject:@{
                                                                   @"position":@(_cameraPosition)} propagate:NO checkForListener:NO];
            }
            break ;
        }
    }
#endif
}

-(void)updateCameraSessionPreset
{
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    //Assume the session is already running
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * Device = INPUT.device ;
        if ( [ Device hasMediaType : AVMediaTypeVideo ] ) {
            //beginConfiguration ensures that pending changes are not applied immediately
            [captureSession beginConfiguration] ;
            
            captureSession.sessionPreset = _quality;
            
            //Changes take effect once the outermost commitConfiguration is invoked.
            [captureSession commitConfiguration] ;
            
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

- (CGFloat)capturedImageRotation:(UIInterfaceOrientation)orient forCamera:(NSInteger)cameraPosition
{
    CGFloat angle;
    switch(orient) {
        case UIInterfaceOrientationPortraitUpsideDown: // -90
            angle = 270;
            break;
        case UIInterfaceOrientationLandscapeLeft: //180
            angle = 180;
            break;
        case UIInterfaceOrientationUnknown: // 90
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
        psize = CGSizeMake(preview.bounds.size.height, preview.bounds.size.width);
    }
    else
    {
        psize = preview.bounds.size;
    }
    // FIXME assumes AVLayerVideoGravityResizeAspectFill
    if (_currentImageSize.height > 0.0f && preview.bounds.size.height > 0.0f)
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
//    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
    size_t width = CVPixelBufferGetWidth(imageBuffer);
    size_t height = CVPixelBufferGetHeight(imageBuffer);
    
//    uint8_t* baseAddress = (uint8_t*)CVPixelBufferGetBaseAddress(imageBuffer);
//    void* free_me = 0;
//    if (true) { // iOS bug?
//        uint8_t* tmp = baseAddress;
//        unsigned long bytes = bytesPerRow*height;
//        free_me = baseAddress = (uint8_t*)malloc(bytes);
//        baseAddress[0] = 0xdb;
//        memcpy(baseAddress,tmp,bytes);
//    }
//    
//    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
//    CGContextRef newContext =
//    CGBitmapContextCreate(baseAddress, width, height, 8, bytesPerRow, colorSpace,
//                          kCGBitmapByteOrder32Little | kCGImageAlphaNoneSkipFirst);
    
//    CGImageRef capture = CGBitmapContextCreateImage(newContext);
    //    [(id)capture autorelease];
    CVPixelBufferUnlockBaseAddress(imageBuffer,0);
//    free(free_me);
//
//    CGContextRelease(newContext);
//    CGColorSpaceRelease(colorSpace);
    
    // N.B.
    // - iOS always takes videos in landscape
    // - images are always 4x3; device is not
    // - iOS uses virtual pixels for non-image stuff
    //Picture taken is always landscape (Right rear and left front)!
//    CGSize psize;
//    BOOL flipped;
//    if(UIInterfaceOrientationIsPortrait(_currentOrientation))
//    {
//        flipped = false;
//        horizontal = false;
//        psize = CGSizeMake(preview.bounds.size.height, preview.bounds.size.width);
//    }
//    else
//    {
//        flipped = true;
//        horizontal = true;
//        psize = preview.bounds.size;
//    }
    _currentOrientation = preview.interfaceOrientation;
    _currentRotation = [self capturedImageRotation:_currentOrientation forCamera:_cameraPosition];
    
    
    _currentImageSize = CGSizeMake(width, height);
    
    _currentPreviewRectInImage = [self getPreviewRectInImage];
    
//    nightydegreesrotationadded = flipped;
    
    //    if (nightydegreesrotationadded)
    //    {
    //        _currentRotation += 90;
    //    }
//    if (needsToTakePicture)
//    {
//        needsToTakePicture = false;
//        UIImageOrientation orientation;
//        switch (_currentOrientation) {
//            case UIInterfaceOrientationLandscapeRight:
//                orientation = mirrored?UIImageOrientationDownMirrored:UIImageOrientationUp;
//                break;
//            case UIInterfaceOrientationLandscapeLeft:
//                orientation = mirrored?UIImageOrientationUpMirrored:UIImageOrientationDown;
//                break;
//            default:
//            case UIInterfaceOrientationPortrait:
//                orientation = mirrored?UIImageOrientationLeftMirrored:UIImageOrientationRight;
//                break;
//            case UIInterfaceOrientationPortraitUpsideDown:
//                orientation = mirrored?UIImageOrientationRightMirrored:UIImageOrientationLeft;
//                break;
//        }
//        [(AkylasCameraViewProxy*)[self proxy] didTakePicture:[UIImage imageWithCGImage:capture scale:[TiUtils screenScale] orientation:orientation] withRotation:_currentRotation];
//    }
//    CGImageRelease(capture);
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
    [captureSession removeOutput:stillImageOutput];
    RELEASE_TO_NIL(stillImageOutput)
    [preview.prevLayer removeFromSuperlayer];
    
    preview.prevLayer = nil;
#endif
    RELEASE_TO_NIL(captureSession)
}

- (void)updateFlash {
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        [device lockForConfiguration:nil];
        if ( [device hasFlash] ) {
            BOOL didchange = false;
            if ( _flash != [device flashMode] ) {
                [device setFlashMode:_flash];
                didchange = true;
            }
//            if (didchange)
//            {
//                if ([[self viewProxy] _hasListeners:@"flash" checkParent:NO]) {
//                    [[self viewProxy] fireEvent:@"flash" withObject:@{
//                                                                      @"value":@(_flash)} propagate:NO checkForListener:NO];
//                }
//            }
        }
        [device unlockForConfiguration];
    }
#endif
}

- (void)updateAdjustWhiteBalance {
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        [device lockForConfiguration:nil];
        
        if ( [device isWhiteBalanceModeSupported:_whitebalance] ) {
            BOOL didchange = false;
            if ( _whitebalance && [device whiteBalanceMode] ) {
                [device setWhiteBalanceMode:_whitebalance];
                didchange = true;
            }
//            if (didchange)
//            {
//                if ([[self viewProxy] _hasListeners:@"whiteBalance" checkParent:NO]) {
//                    [[self viewProxy] fireEvent:@"whiteBalance" withObject:@{
//                                                                      @"value":@(_whitebalance)} propagate:NO checkForListener:NO];
//                }
//            }
        }
        [device unlockForConfiguration];
    }
#endif
}

- (void)updateExposure {
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        [device lockForConfiguration:nil];
        
        if ( [device isExposureModeSupported:_exposure] ) {
            BOOL didchange = false;
            if ( _exposure && [device exposureMode] ) {
                [device setExposureMode:_exposure];
                didchange = true;
            }
//            if (didchange)
//            {
//                if ([[self viewProxy] _hasListeners:@"exposure" checkParent:NO]) {
//                    [[self viewProxy] fireEvent:@"exposure" withObject:@{
//                                                                             @"value":@(_exposure)} propagate:NO checkForListener:NO];
//                }
//            }
        }
        [device unlockForConfiguration];
    }
#endif
}


- (void)updateTorch {
    if (! captureSession) {
        return;
    }
#if HAS_AVFF
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        [device lockForConfiguration:nil];
        if ( [device hasTorch] ) {
            BOOL didchange = false;
            if ( _torch && [device torchMode] != AVCaptureTorchModeOn ) {
                [device setTorchMode:AVCaptureTorchModeOn];
                didchange = true;
            } else if( !_torch && [device torchMode] != AVCaptureTorchModeOff ){
                [device setTorchMode:AVCaptureTorchModeOff];
                didchange = true;
            }
//            if (didchange)
//            {
//                if ([[self viewProxy] _hasListeners:@"torch" checkParent:NO]) {
//                    [[self viewProxy] fireEvent:@"torch" withObject:@{
//                                                                       @"value":@(_torch)} propagate:NO checkForListener:NO];
//                }
//            }
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
    
//    AVCaptureVideoPreviewLayer *videoPreviewLayer = preview.prevLayer;
    CGSize frameSize = [preview frame].size;
    
    if (mirrored) {
        viewCoordinates.x = frameSize.width - viewCoordinates.x;
    }
    
    CGPoint realPoint = [self rotatePoint:viewCoordinates withinRect:preview.bounds withAngle:(-_currentRotation + 360.0f)];
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
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus])
        {
            NSError *error;
            if ([device lockForConfiguration:&error]) {
                [device setFocusPointOfInterest:[self convertToPointOfInterestFromViewCoordinates:point forInput:INPUT]];
                [device setFocusMode:AVCaptureFocusModeAutoFocus];
                [device unlockForConfiguration];
                
                if ([[self viewProxy] _hasListeners:@"focus" checkParent:NO]) {
                    [[self viewProxy] fireEvent:@"focus" withObject:@{
                                                                      @"point":[[[TiPoint alloc] initWithPoint:point] autorelease]} propagate:NO checkForListener:NO];
                }
            }
        }
    }
#endif
}

-(void)autoFocusAtPoint:(CGPoint)point
{
#if HAS_AVFF
    NSArray * inputs = captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        if ([device isFocusPointOfInterestSupported] && [device isFocusModeSupported:AVCaptureFocusModeAutoFocus])
        {
            NSError *error;
            if ([device lockForConfiguration:&error]) {
                [device setFocusPointOfInterest:[self convertToPointOfInterestFromViewCoordinates:point forInput:INPUT]];
                [device setFocusMode:AVCaptureFocusModeContinuousAutoFocus];
                [device unlockForConfiguration];

                if ([[self viewProxy] _hasListeners:@"autofocus" checkParent:NO]) {
                    [[self viewProxy] fireEvent:@"autofocus" withObject:@{
                                                                      @"point":[[[TiPoint alloc] initWithPoint:point] autorelease]} propagate:NO checkForListener:NO];
                }
            }
        }
    }
#endif
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

-(void)takePicture:(NSDictionary*)options
{
//    needsToTakePicture  = true;
//}
//
//- (void)takePhoto{
    
    AVCaptureConnection *videoConnection = nil;
    //just some checks
    for(AVCaptureConnection *connection in stillImageOutput.connections){
        for(AVCaptureInputPort *port in [connection inputPorts]){
            if([[port mediaType] isEqual:AVMediaTypeVideo]){
                videoConnection = connection;
                break;
            }
        }
        if(videoConnection){
            break;
        }
    }
    if (!videoConnection) {
        return;
    }
    if ([videoConnection isVideoOrientationSupported])
    {
        [videoConnection setVideoOrientation:[[UIDevice currentDevice] orientation]];
    }
    [stillImageOutput captureStillImageAsynchronouslyFromConnection:videoConnection completionHandler:^(CMSampleBufferRef imageSampleBuffer, NSError *error) {
        if(imageSampleBuffer != NULL) { //this code gets executed if a photo is taken
            NSData *imageData = [AVCaptureStillImageOutput jpegStillImageNSDataRepresentation:imageSampleBuffer];
            UIImage* image = [[[UIImage alloc] initWithData:imageData] autorelease];
            id callback = [options objectForKey:@"callback"];
            TiBlob* media = [[[TiBlob alloc] initWithImage:image] autorelease];
            
            TiBlob* imageFile = nil;
            if ([TiUtils boolValue:@"save" properties:options def:false] ||
                [TiUtils boolValue:@"saveToGallery" properties:options def:false]) {
                imageFile = [[[TiBlob alloc] initWithImage:image] autorelease];
                
                if ([TiUtils boolValue:@"saveToGallery" properties:options def:false]) {
                    //imageData is the taken picture
                    UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil);
                } else {
                    NSString* path = [NSString stringWithFormat:@"%@\%@.jpg", [TiFileSystemHelper applicationDataDirectory], IMG_PREFIX];
                    [media writeTo:path error:nil];
                }
            }
            
            if (IS_OF_CLASS(callback, KrollCallback)) {
                NSMutableDictionary* event = [NSMutableDictionary dictionary];
                if ([TiUtils boolValue:@"metadata" properties:options def:false]) {
                    CFDictionaryRef exifDictRef = CMGetAttachment(imageSampleBuffer,kCGImagePropertyExifDictionary, NULL);
                    NSDictionary *exifDict = (NSDictionary *)exifDictRef;
                    [event setObject:exifDict forKey:@"metadata"];
                }
                if ([TiUtils boolValue:@"image" properties:options def:true]) {
                    [event setObject:media forKey:@"image"];
                    [event setObject:@(image.size.width) forKey:@"width"];
                    [event setObject:@(image.size.height) forKey:@"height"];
                    [event setObject:media.mimeType forKey:@"mimeType"];
                }
                if (imageFile) {
                    [event setObject:imageFile forKey:@"file"];
                }
                [self.proxy _fireEventToListener:@"image" withObject:event listener:callback thisObject:nil];
            }
            
            if (_torch)
            {
                [self updateTorch];
            }
        }
    }];
}

@end
