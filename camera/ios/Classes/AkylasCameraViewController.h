
#include <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraPreview.h"

@protocol AkylasCameraViewControllerDelegate;

#if !TARGET_IPHONE_SIMULATOR
#define HAS_AVFF 1
#endif

@interface AkylasCameraViewController : UIViewController
#if HAS_AVFF
    <AVCaptureVideoDataOutputSampleBufferDelegate>
#endif
    {
        CameraPreview *preview;
        id<AkylasCameraViewControllerDelegate> delegate;
        AVCaptureSession *captureSession;                                                            
        BOOL rotating;
        BOOL mirrored;
        BOOL horizontal;
        BOOL nightydegreesrotationadded;
        CGSize _currentImageSize;
        CGRect _currentPreviewRectInImage;
        float _currentRotation;
        float _currentScale;
        UIInterfaceOrientation _currentOrientation;
        int _cameraPosition;
        BOOL needsToTakePicture;
        BOOL torch;
}

//#if HAS_AVFF
@property (nonatomic, retain) AVCaptureSession *captureSession;
//#endif
@property (nonatomic, assign) id<AkylasCameraViewControllerDelegate> delegate;
@property (nonatomic, retain) CameraPreview *preview;

- (id)initWithDelegate:(id<AkylasCameraViewControllerDelegate>)delegate;

- (BOOL)fixedFocus;
- (void)setTorch:(BOOL)status;
- (BOOL)torchIsOn;
-(void)swapCamera;
+(int)cameraPositionValue:(id)pos;
-(void)setCameraPosition:(int)cameraPosition;
- ( void ) updateCameraPosition:(int) cameraPosition;
-(int) cameraPosition;
-(void)autoFocusAtPoint:(CGPoint)point;
-(void)focusAtPoint:(CGPoint)point;
-(void)takePicture;
@end

@protocol AkylasCameraViewControllerDelegate
- (void)captureDidStart:(AkylasCameraViewController*)controller ;
- (void)captureDidStop:(AkylasCameraViewController*)controller;
- (void)controller:(AkylasCameraViewController*)controller didTakePicture:(UIImage*)image withRotation:(CGFloat)rotation;
- (void)controller:(AkylasCameraViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasCameraViewController*)controller didSetFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasCameraViewController*)controller didSetTorch:(BOOL)value;
- (void)controller:(AkylasCameraViewController*)controller didChangeCamera:(int)cameraPosition;
@end
