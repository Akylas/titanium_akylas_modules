
#import "TiUIView.h"
#import "AkylasScancodeViewController.h"

@class AkylasScancodeViewController;
extern NSDictionary *const symbolDict;

@interface AkylasScancodeView : TiUIView<AkylasScancodeViewControllerDelegate> {
    AkylasScancodeViewController* _controller;
    UIView *scanview;

}
//-(void)fireEvent:(id)listener withObject:(id)obj remove:(BOOL)yn thisObject:(id)thisObject_;

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation;
- (void)viewWillAppear:(BOOL)animated;
- (void)viewDidAppear:(BOOL)animated;
- (void)viewWillDisappear:(BOOL)animated;
- (void)viewDidDisappear:(BOOL)animated;
- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration;
- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration;
- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation;

- (void) start;
- (void) stop;
- (BOOL) torchOn;
- (BOOL) onlyOneDimension;
- (int) cameraPosition;
- (CGRect) cropRect;
- (void) flush;
-(void)swapCamera;
- (BOOL) centeredCropRect;
- (void) focus:(CGPoint)point;
- (void) autoFocus:(CGPoint)point;

@end
