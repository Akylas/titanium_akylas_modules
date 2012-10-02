
#import "TiUIView.h"

@interface AkylasScancodeView : TiUIView {
    UIView *_scanview;

}
@property (nonatomic, retain ) UIView *scanview;
//-(void)fireEvent:(id)listener withObject:(id)obj remove:(BOOL)yn thisObject:(id)thisObject_;

//- (void) start;
//- (void) stop;
//- (BOOL) torchOn;
//- (BOOL) onlyOneDimension;
//- (int) cameraPosition;
//- (CGRect) cropRect;
//- (void) flush;
//-(void)swapCamera;
//- (BOOL) centeredCropRect;
//- (void) focus:(CGPoint)point;
//- (void) autoFocus:(CGPoint)point;


-(void) cleanup;

@end
