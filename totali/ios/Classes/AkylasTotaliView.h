
#import "TiUIView.h"
//#import "AkylasTotaliViewController.h"

@class AkylasTotaliViewController;
extern NSDictionary *const symbolDict;

@class tiComponent;
@interface AkylasTotaliView : TiUIView {
    UIView *totaliview;
    tiComponent* mPlayer;
    NSString* _scenario;
    
    NSMutableArray* _registeredCallbacks;
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

- (NSString*) scenario;
- (void) start;
- (void) stop;
-(void)registerCallback:(NSString*)value;
-(void)unregisterCallback:(NSString*)value;
//- (BOOL) torchOn;
//- (BOOL) onlyOneDimension;
//- (int) cameraPosition;
//- (CGRect) cropRect;
//- (void) flush;
//-(void)swapCamera;
//- (BOOL) centeredCropRect;
//- (void) focus:(CGPoint)point;
//- (void) autoFocus:(CGPoint)point;

@end
