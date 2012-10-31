
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@interface CameraPreview : UIView
{
    NSTimeInterval animationDuration;
    AVCaptureVideoPreviewLayer *_prevLayer;
    CGAffineTransform previewTransform;
}
@property (nonatomic, retain) AVCaptureVideoPreviewLayer *prevLayer;
// additional transform applied to video preview.
// (NB *not* applied to scan crop)
@property (nonatomic) CGAffineTransform previewTransform;
@property (nonatomic) UIInterfaceOrientation interfaceOrientation;

// compensate for device/camera/interface orientation
- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) orient
                                 duration: (NSTimeInterval) duration;
@end
