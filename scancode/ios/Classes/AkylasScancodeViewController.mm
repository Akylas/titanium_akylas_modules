
#import "AkylasScancodeViewController.h"
#import "Decoder.h"
#import "NSString+HTML.h"
#import "resultParsers/ResultParser.h"
#import "parsedResults/ParsedResult.h"
#import "actions/ResultAction.h"
#import "TwoDDecoderResult.h"
#include <sys/types.h>
#include <sys/sysctl.h>
#include <sys/utsname.h>
#import "QRCodeReader.h"

#import <AVFoundation/AVFoundation.h>

#define CAMERA_SCALAR 1.12412 // scalar = (480 / (2048 / 480))
#define FIRST_TAKE_DELAY 1.0
#define ONE_D_BAND_HEIGHT 10.0

@interface AkylasScancodeViewController ()

- (void)initCapture;
- (void)stopCapture;

@end

@implementation AkylasScancodeViewController

//#if HAS_AVFF
@synthesize captureSession;
//#endif
@synthesize delegate;
//@synthesize result;
@synthesize preview;
@synthesize centeredCropRect;
@synthesize readers;
@synthesize oneDMode, needsFlip;

- (id)initWithDelegate:(id<AkylasScancodeViewControllerDelegate>)scanDelegate  {
    self = [super init];
    if (self) {
        [self setDelegate:scanDelegate];
        decoding = NO;
        _cropRect = CGRectZero;
        centeredCropRect = TRUE;
        _cameraPosition = CAMERA_REAR;
        rotating = NO;
        mirrored = false;
        _currentScale =1.0;
        self.oneDMode = FALSE;
        QRCodeReader* qrcodeReader = [[QRCodeReader alloc] init];
        self.readers = [NSSet setWithObject:qrcodeReader];
        [qrcodeReader release];
    }
    
    return self;
}

- (void)dealloc {
    
    [self stopCapture];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    //    [result release];
    [preview release];
    [readers release];
    [super dealloc];
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

- (void)loadView
{
//    NSLog(@"loadview");
    ZxingPreview* theCaptureView = [[ZxingPreview alloc] initWithFrame:CGRectZero];
    self.preview = theCaptureView;
    self.view = theCaptureView;
    [theCaptureView release];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
//    self.view.autoresizingMask = self.preview.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//    preview.frame = self.view.bounds;
//    [self.view addSubview:preview];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    decoding = YES;
    
    [self initCapture];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [self stopCapture];
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
    [delegate captureDidStart:self];
}

-(void)captureSessionDidStop
{
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

- (CGImageRef)CGImageTransform:(CGImageRef)imgRef withAngle:(CGFloat)angle scaleX:(CGFloat)scalex scaleY:(CGFloat)scaley
{
    CGFloat angleInRadians = angle * (M_PI / 180);
    
    if (((int)angleInRadians % 360) == 0 && scalex == 1.0f && scaley == 1.0f)
    {
        CGImageRef result = CGImageCreateCopy(imgRef);
        [(id)CGImageCreateCopy(result) autorelease];
        return result;
    }
    CGFloat width = CGImageGetWidth(imgRef);
    CGFloat height = CGImageGetHeight(imgRef);
    // calculate the size of the rotated view's containing box for our drawing space
    UIView *rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0,0,width, height)];
    CGAffineTransform t = CGAffineTransformMakeRotation(angleInRadians);
    rotatedViewBox.transform = t;
    CGSize rotatedSize = rotatedViewBox.frame.size;
    [rotatedViewBox release];
    // Create the bitmap context
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef bmContext = CGBitmapContextCreate(NULL,
                                                   rotatedSize.width,
                                                   rotatedSize.height,
                                                   8,
                                                   0,
                                                   colorSpace,
                                                   kCGImageAlphaPremultipliedFirst);
    CGContextSetAllowsAntialiasing(bmContext, FALSE);
    CGContextSetInterpolationQuality(bmContext, kCGInterpolationNone);
    CGColorSpaceRelease(colorSpace);

    
    // Move the origin to the middle of the image so we will rotate and scale around the center.
    CGContextTranslateCTM(bmContext, rotatedSize.width/2, rotatedSize.height/2);
    
    //   // Rotate the image context
    CGContextRotateCTM(bmContext, angleInRadians);
    
    // Now, draw the rotated/scaled image into the context
    CGContextScaleCTM(bmContext, scalex, scaley);
    CGContextDrawImage(bmContext, CGRectMake(-width / 2, -height / 2, width, height), imgRef);
    CGImageRef rotatedImage = CGBitmapContextCreateImage(bmContext);
    CFRelease(bmContext);
    [(id)rotatedImage autorelease];
    
    return rotatedImage;
}

- (CGImageRef)CGImageTransform:(CGImageRef)imgRef withAngle:(CGFloat)angle
{
    [self CGImageTransform:imgRef withAngle:angle scaleX:1.0f scaleY:1.0f];
}

- (UIImage*)UIImageTransform:(UIImage*)img withAngle:(CGFloat)angle scaleX:(CGFloat)scalex scaleY:(CGFloat)scaley
{
    CGImageRef imageRef = [img CGImage];
    return [UIImage imageWithCGImage:[self CGImageTransform:imageRef withAngle:angle scaleX:scalex scaleY:scaley]];
}

- (UIImage*)UIImageTransform:(UIImage*)img withAngle:(CGFloat)angle
{
    return [self UIImageTransform:img withAngle:angle scaleX:1.0f scaleY:1.0f];
}

// DecoderDelegate methods

- (void)decoder:(Decoder *)decoder willDecodeImage:(UIImage *)image usingSubset:(UIImage *)subset{
//#ifdef ZXING_DEBUG
//    NSLog(@"DecoderViewController MessageWhileDecodingWithDimensions: Decoding image (%.0fx%.0f) ...", image.size.width, image.size.height);
//#endif
}

- (void)decoder:(Decoder *)decoder didDecodeImage:(UIImage *)image usingSubset:(UIImage *)subset withResult:(TwoDDecoderResult *)twoDResult {
    // now, in a selector, call the delegate to give this overlay time to show the points
//    NSLog(@"image size %@", NSStringFromCGSize(image.size));
//    NSLog(@"_currentReorientedPreviewCropRect %@", NSStringFromCGRect(_currentReorientedPreviewCropRect));
//    NSLog(@"_currentImageCropRect %@", NSStringFromCGRect(_currentImageCropRect));
//    NSLog(@"_currentRotation %f", _currentRotation);
    
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSArray* points = [twoDResult points];
//    NSLog(@"current scale %f",_currentScale);
    
    NSMutableArray* scaledpoints = [NSMutableArray array];
    CGRect imageRect  = CGRectMake(0.0, 0.0, image.size.width, image.size.height);
    for (NSValue* pointValue in points)
    {
        CGPoint point = [pointValue CGPointValue];
        point = [self rotatePoint:point withinRect:imageRect withAngle:(_currentRotation)];
        if (mirrored)
            point = CGPointMake((_currentPreviewCropRect.size.width - point.x/_currentScale), point.y/_currentScale);
        else
            point = CGPointMake(point.x/_currentScale, point.y/_currentScale);
            
        [scaledpoints addObject:[NSValue valueWithCGPoint:point]];
        
    }
    points = [NSArray arrayWithArray:scaledpoints];
    
    float imgRotation = (360.0f - _currentRotation);
    if (mirrored)
    {
        if (!horizontal || nightydegreesrotationadded)
            imgRotation = (float) (((int)imgRotation + 180) % 360);
    }
    float scaleX = 1.0f/_currentScale;
    float scaleY = 1.0f/_currentScale;
    if (mirrored) scaleX = - scaleX;

    UIImage* scaledImage = [self UIImageTransform:image withAngle:imgRotation scaleX:scaleX scaleY:scaleY];
    
    [delegate controller:self didScanResult:[NSDictionary dictionaryWithObjectsAndKeys:
                                             [twoDResult text], @"message",
                                             scaledImage, @"image",
                                             points, @"points",
                                             [NSValue valueWithCGRect:_currentPreviewCropRect], @"cropRect",nil]];
    
    [pool release];
    decoder.delegate = nil;
}

- (void)decoder:(Decoder *)decoder failedToDecodeImage:(UIImage *)image usingSubset:(UIImage *)subset reason:(NSString *)reason {
    decoder.delegate = nil;
}

-(void)setCropRect:(CGRect)cropRect {
    _cropRect = cropRect;
}

-(CGRect)cropRect {
    return _cropRect;
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
    AVCaptureDevice* inputDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    AVCaptureDevicePosition position = inputDevice.position ;
    if (position == AVCaptureDevicePositionFront)
        _cameraPosition = CAMERA_FRONT;
    else _cameraPosition = CAMERA_REAR;
    
    AVCaptureDeviceInput *captureInput = [AVCaptureDeviceInput deviceInputWithDevice:inputDevice error:nil];
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

- ( void ) updateCameraPosition:(CameraPosition) cameraPosition
{
#if HAS_AVFF
    //Assume the session is already running
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * Device = INPUT.device ;
        if ( [ Device hasMediaType : AVMediaTypeVideo ] ) {
            AVCaptureDevicePosition position = Device.position ;
            AVCaptureDevice * newCamera = nil ;
            AVCaptureDeviceInput * newInput = nil ;
            
            if ( cameraPosition == CAMERA_FRONT )
            {
                if (position == AVCaptureDevicePositionFront) return; //nothing to do
                newCamera = [ self cameraWithPosition : AVCaptureDevicePositionFront ] ;
                mirrored = true;
            }
            else if ( cameraPosition == CAMERA_REAR )
            {
                if (position == AVCaptureDevicePositionBack) return; //nothing to do
                newCamera = [ self cameraWithPosition : AVCaptureDevicePositionBack ] ;
                mirrored = false;
            }
            
            if (newCamera == nil) return; //failure
            
            _cameraPosition = cameraPosition;
            
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

- (CGRect)mirrorRect:(CGRect)rect vertical:(BOOL)vertical imageSize:(CGSize)size
{
    if (vertical)
        return CGRectMake( rect.origin.x,
                          size.height - (rect.origin.y + rect.size.height),
                          rect.size.width, rect.size.height);
    else
        return CGRectMake( size.width - (rect.origin.x + rect.size.width),
                       rect.origin.y,
                                      rect.size.width, rect.size.height);
}

- (CGRect)rotateImageCropRect:(CGRect)rect imageSize:(CGSize) size withAngle: (CGFloat)angle
{
    
    CGRect newRect;
    int modAngle = ((int)angle % 360);
    switch(modAngle) {
        case 270: //-90
            newRect.origin.x = rect.origin.y;
            newRect.origin.y = size.width - rect.origin.x - rect.size.width;
            newRect.size.width = rect.size.height;
            newRect.size.height = rect.size.width;
            break;
        case 180: //180
            newRect.origin.x = size.width - rect.origin.x - rect.size.width;
            newRect.origin.y = size.height - rect.origin.y - rect.size.height;
            newRect.size.width = rect.size.width;
            newRect.size.height = rect.size.height;
            break;
        case 90: // 90
            newRect.origin.x = size.height - rect.origin.y - rect.size.height;
            newRect.origin.y = rect.origin.x;
            newRect.size.width = rect.size.height;
            newRect.size.height = rect.size.width;
            break;
        case 0:
        {
            newRect = rect;
            break;
        }
    }
    return newRect;
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

- (CGFloat)capturedImageRotation:(UIInterfaceOrientation)orient forCamera:(CameraPosition)cameraPosition
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
    if (cameraPosition == CAMERA_FRONT) angle = 180.0f - angle;
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
    if (!decoding) {
        return;
    }
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
    
//    NSLog(@"_currentScale %f", _currentScale);
//    NSLog(@"_currentPreviewRectInImage %@", NSStringFromCGRect(_currentPreviewRectInImage));
//    NSLog(@"_cropRect %@", NSStringFromCGRect(_cropRect));

    if (CGRectIsEmpty(_cropRect))
        _cropRect = CGRectMake(0.0f, 0.0f, psize.width, psize.height);
    
    CGRect reorientedCropRectInPreview = [self rotateImageCropRect:_cropRect imageSize: self.preview.bounds.size withAngle:(-_currentRotation + 360.0f)];
    
    CGAffineTransform affine = CGAffineTransformMakeScale(_currentScale, _currentScale);
    _currentImageCropRect = CGRectApplyAffineTransform(reorientedCropRectInPreview, affine);
    
    _currentImageCropRect.origin.x += _currentPreviewRectInImage.origin.x;
    _currentImageCropRect.origin.y += _currentPreviewRectInImage.origin.y;
    
    if (centeredCropRect)
    {
        _currentImageCropRect.origin.x = (_currentImageSize.width - _currentImageCropRect.size.width) / 2;

        _currentImageCropRect.origin.y = (_currentImageSize.height - _currentImageCropRect.size.height) / 2;
    }
    
    if (mirrored)
    {
        _currentImageCropRect = [self mirrorRect:_currentImageCropRect vertical:!horizontal imageSize:_currentImageSize];
//        _currentRotation = (float) (((int)_currentRotation + 180) % 360);
    }
    
    if (self.oneDMode) {
        // let's just give the decoder a vertical band right above the red line
        //image is always landscape so inversion!
        if (horizontal)
        {
            _currentImageCropRect.origin.y = _currentImageCropRect.origin.y + (_currentImageCropRect.size.height / 2) - (ONE_D_BAND_HEIGHT + 1);
            _currentImageCropRect.size.height = ONE_D_BAND_HEIGHT;
        }
        else
        {
            _currentImageCropRect.origin.x = _currentImageCropRect.origin.x + (_currentImageCropRect.size.width / 2) - (ONE_D_BAND_HEIGHT + 1);
            _currentImageCropRect.size.width = ONE_D_BAND_HEIGHT;
        }
    }
    _currentImageCropRect = CGRectIntersection(_currentImageCropRect, CGRectMake(0.0f,0.0f, _currentImageSize.width, _currentImageSize.height));
    
    CGRect temprect = _currentImageCropRect;
    if (mirrored)
    {
        //mirror back
        temprect = [self mirrorRect:temprect vertical:!horizontal imageSize:_currentImageSize];
    }
    temprect.origin.x -= _currentPreviewRectInImage.origin.x;
    temprect.origin.y -= _currentPreviewRectInImage.origin.y;
    affine = CGAffineTransformMakeScale(1.0f/_currentScale , 1.0f/_currentScale);
    
    temprect = CGRectApplyAffineTransform(temprect, affine);

    temprect = [self rotateImageCropRect:temprect imageSize: psize withAngle:_currentRotation];
    _currentPreviewCropRect = temprect;
    
//    if (mirrored)
//    {
//        _currentRotation = (float) (((int)_currentRotation + 180) % 360);
//    }
        
//    NSLog(@"_currentReorientedPreviewCropRect %@", NSStringFromCGRect(_currentReorientedPreviewCropRect));
//    NSLog(@"_currentPreviewCropRect %@", NSStringFromCGRect(_currentPreviewCropRect));
//    NSLog(@"_currentImageCropRect %@", NSStringFromCGRect(_currentImageCropRect));
//    NSLog(@"preview size %@", NSStringFromCGSize(self.preview.bounds.size));
//    NSLog(@"image size %@", NSStringFromCGSize(_currentImageSize));
    
    if (CGRectIsNull(_currentImageCropRect) || CGRectIsInfinite(_currentImageCropRect) || CGRectIsEmpty(_currentImageCropRect)) return;
    CGImageRef newImage = CGImageCreateWithImageInRect(capture, _currentImageCropRect);//retained
    CGImageRelease(capture);
    
    UIImage *scrn;
    nightydegreesrotationadded = flipped && (self.needsFlip || self.oneDMode);
    
    if (nightydegreesrotationadded)
    {
        CGImageRef rotatedImage = [self CGImageTransform:newImage withAngle:90]; //auto released
        scrn = [[UIImage alloc] initWithCGImage:rotatedImage];
        _currentRotation += 90;
    }
    else
        scrn = [[UIImage alloc] initWithCGImage:newImage];

    CGImageRelease(newImage);
    Decoder *d = [[Decoder alloc] init];
    d.readers = readers;
    d.delegate = self;
    
    decoding = [d decodeImage:scrn] == YES ? NO : YES;

    [d release];
    [scrn release];
}
#endif

- (void)stopCapture {
    decoding = NO;
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
#if HAS_AVFF
    NSArray * inputs = self.captureSession.inputs;
    for ( AVCaptureDeviceInput * INPUT in inputs ) {
        AVCaptureDevice * device = INPUT.device ;
        if ( [device hasTorch] ) {
            return [device torchMode] == AVCaptureTorchModeOn;
        }
    }
#endif
    return NO;
}

-(void)flush
{
    decoding = YES;
}

-(void)swapCamera
{
    if (_cameraPosition == CAMERA_FRONT)
        [self setCameraPosition:CAMERA_REAR];
    else
        [self setCameraPosition:CAMERA_FRONT];
}

+(CameraPosition)cameraPositionValue:(id)pos
{
	CameraPosition newPos = CAMERA_FRONT;
    
	if ([pos isKindOfClass:[NSString class]])
	{
		if ([pos isEqualToString:@"rear"])
		{
			newPos = CAMERA_REAR;
		}
		else if ([pos isEqualToString:@"front"])
		{
			newPos = CAMERA_FRONT;
		}
	}
	else if ([pos isKindOfClass:[NSNumber class]])
	{
		newPos = (CameraPosition)[pos intValue];
	}
	return newPos;
}

-(void)setCameraPosition:(CameraPosition)cameraPosition
{
    if (_cameraPosition == cameraPosition)
        return;    
    [self updateCameraPosition:cameraPosition];
}

-(CameraPosition) cameraPosition
{
    return _cameraPosition;
}

@end
