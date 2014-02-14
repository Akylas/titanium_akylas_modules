#import "MMDrawerController+Subclass.h"

@class TiViewProxy;
@interface AkylasSlidemenuDrawerController : MMDrawerController
@property(nonatomic,assign) CGFloat fadeDegree;
@property(nonatomic,assign) CGFloat leftDisplacement;
@property(nonatomic,assign) CGFloat rightDisplacement;
@property(nonatomic,assign) TiViewProxy * proxy;
@property (nonatomic, copy) MMDrawerControllerDrawerVisualStateBlock leftVisualBlock;
@property (nonatomic, copy) MMDrawerControllerDrawerVisualStateBlock rightVisualBlock;
-(CGRect)childControllerContainerViewFrame;

@end
