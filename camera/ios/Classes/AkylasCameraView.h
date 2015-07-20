
#import "TiUIView.h"
#import <AVFoundation/AVFoundation.h>

#if !TARGET_IPHONE_SIMULATOR
#define HAS_AVFF 1
#endif
@interface AkylasCameraView : TiUIView
#if HAS_AVFF
<AVCaptureVideoDataOutputSampleBufferDelegate, AVCaptureMetadataOutputObjectsDelegate>
#endif
{

}

-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration;
- (void) start;
- (void) stop;
-(void)focusAtPoint:(CGPoint)point;
-(void)autoFocusAtPoint:(CGPoint)point;
-(void)takePicture:(NSDictionary*)options;
@end
