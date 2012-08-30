
#include <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#include "Decoder.h"
#include "parsedResults/ParsedResult.h"
#include "ZxingPreview.h"

@protocol AkylasScancodeViewControllerDelegate;

#if !TARGET_IPHONE_SIMULATOR
#define HAS_AVFF 1
#endif

typedef enum {
    CAMERA_FRONT,
    CAMERA_REAR
} CameraPosition;

@interface AkylasScancodeViewController : UIViewController<DecoderDelegate
#if HAS_AVFF
    , AVCaptureVideoDataOutputSampleBufferDelegate
#endif
    > {
        NSSet *readers;
        ZxingPreview *preview;
        id<AkylasScancodeViewControllerDelegate> delegate;
        AVCaptureSession *captureSession;                                                            
        BOOL decoding;
        BOOL rotating;
        BOOL mirrored;
        BOOL horizontal;
        BOOL nightydegreesrotationadded;
        CGRect _cropRect;
        CGRect _currentPreviewCropRect;
//        CGRect _currentReorientedPreviewCropRect;
        CGRect _currentImageCropRect;
        CGSize _currentImageSize;
        CGRect _currentPreviewRectInImage;
        float _currentRotation;
        BOOL _currentFlippedH;
        float _currentScale;
        float _currentScaleX;
        float _currentScaleY;
        UIInterfaceOrientation _currentOrientation;
        CameraPosition _cameraPosition;
}

//#if HAS_AVFF
@property (nonatomic, retain) AVCaptureSession *captureSession;
//#endif
@property (nonatomic, retain ) NSSet *readers;
@property (nonatomic, assign) id<AkylasScancodeViewControllerDelegate> delegate;
@property (nonatomic, retain) ZxingPreview *preview;
@property (nonatomic, assign) BOOL centeredCropRect;
@property (nonatomic, assign) BOOL oneDMode;
@property (nonatomic, assign) BOOL needsFlip;

- (id)initWithDelegate:(id<AkylasScancodeViewControllerDelegate>)delegate;

- (BOOL)fixedFocus;
- (void)setTorch:(BOOL)status;
- (BOOL)torchIsOn;
- (void)flush;
-(void)swapCamera;
- (CGRect)cropRect;
-(void)setCropRect:(CGRect)cropRect;
+(CameraPosition)cameraPositionValue:(id)pos;
-(void)setCameraPosition:(CameraPosition)cameraPosition;
- ( void ) updateCameraPosition:(CameraPosition) cameraPosition;
-(CameraPosition) cameraPosition;
-(void)autoFocusAtPoint:(CGPoint)point;
-(void)focusAtPoint:(CGPoint)point;
@end

@protocol AkylasScancodeViewControllerDelegate
- (void)captureDidStart:(AkylasScancodeViewController*)controller ;
- (void)captureDidStop:(AkylasScancodeViewController*)controller;
- (void)controller:(AkylasScancodeViewController*)controller didScanResult:(NSDictionary *)result;
- (void)controller:(AkylasScancodeViewController*)controller didSetAutoFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasScancodeViewController*)controller didSetFocusAtPoint:(CGPoint)location;
- (void)controller:(AkylasScancodeViewController*)controller didSetTorch:(BOOL)value;
- (void)controller:(AkylasScancodeViewController*)controller didChangeCamera:(CameraPosition)cameraPosition;
- (void)controller:(AkylasScancodeViewController*)controller didFoundPossibleResultPoint:(CGPoint)point;
@end
