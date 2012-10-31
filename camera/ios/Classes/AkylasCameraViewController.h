
#include <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "CameraPreview.h"

@protocol AkylasCameraViewControllerDelegate;

#if !TARGET_IPHONE_SIMULATOR
#define HAS_AVFF 1
#endif

typedef enum {
    CAMERA_FRONT,
    CAMERA_REAR
} CameraPosition;

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
        CameraPosition _cameraPosition;
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
+(CameraPosition)cameraPositionValue:(id)pos;
-(void)setCameraPosition:(CameraPosition)cameraPosition;
- ( void ) updateCameraPosition:(CameraPosition) cameraPosition;
-(CameraPosition) cameraPosition;
-(void)autoFocusAtPoint:(CGPoint)point;
-(void)focusAtPoint:(CGPoint)point;
@end

@protocol AkylasCameraViewControllerDelegate
- (void)captureDidStart:(AkylasCameraViewController*)controller ;
- (void)captureDidStop:(AkylasCameraViewController*)controller;
- (void)controller:(AkylasCameraViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasCameraViewController*)controller didSetFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasCameraViewController*)controller didSetTorch:(BOOL)value;
- (void)controller:(AkylasCameraViewController*)controller didChangeCamera:(CameraPosition)cameraPosition;
@end
