//
//  SlideMenuDrawerController.m
//  Titanium
//
//  Created by Martin Guillon on 11/10/13.
//
//

#import "AkylasSlidemenuDrawerController.h"
#import "TiProxy.h"
#import "TiApp.h"

@interface AkylasSlidemenuDrawerController ()
{
    TiViewProxy* _proxy;
    
    UIView* _leftViewFadingView;
    UIView* _rightViewFadingView;
//    CGFloat _fadeDegree; //between 0.0f and 1.0f
}
-(UIView*)childControllerContainerView;
@property (nonatomic, assign) CGRect startingPanRect;

@end

@implementation AkylasSlidemenuDrawerController
@synthesize proxy = _proxy;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        _fadeDegree = 0.0f;
    }
    return self;
}

- (void) removeFromParentViewController
{
    self.centerViewController = nil;
    self.leftDrawerViewController = nil;
    self.rightDrawerViewController = nil;
    _proxy = nil;
    [self  setDrawerVisualStateBlock:nil];
    self.leftVisualBlock = nil;
    self.rightVisualBlock = nil;
    [super removeFromParentViewController];
}

- (void)dealloc
{
	RELEASE_TO_NIL(_leftViewFadingView);
	RELEASE_TO_NIL(_rightViewFadingView);
    
    [super dealloc];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
	[[self childControllerContainerView] setBackgroundColor:[UIColor clearColor]];
    [[self view] setBackgroundColor:[UIColor clearColor]];
    self.statusBarViewBackgroundColor = [UIColor clearColor];
    self.showsStatusBarBackgroundView = YES;
    
    [self
     setDrawerVisualStateBlock:^(MMDrawerController *drawerController, MMDrawerSide drawerSide, CGFloat percentVisible) {
         MMDrawerControllerDrawerVisualStateBlock block;
         block = [self drawerVisualStateBlockForDrawerSide:drawerSide];
         if(block){
             block(drawerController, drawerSide, percentVisible);
         }
     }];

}

-(MMDrawerControllerDrawerVisualStateBlock) drawerVisualStateBlockForDrawerSide:(MMDrawerSide)drawerSide
{
    if (drawerSide == MMDrawerSideLeft) {
        return self.leftVisualBlock;
    }
    return self.rightVisualBlock;
}

-(CGRect)childControllerContainerViewFrame
{
    return [[self childControllerContainerView] frame];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)closeDrawerAnimated:(BOOL)animated velocity:(CGFloat)velocity animationOptions:(UIViewAnimationOptions)options completion:(void (^)(BOOL))completion __attribute((objc_requires_super))
{
    [super closeDrawerAnimated:animated velocity:velocity animationOptions:options completion:completion];
        if ([_proxy _hasListeners:@"closemenu"])
        {
            CGFloat distance = ABS(CGRectGetMinX(self.centerContainerView.frame));
            NSTimeInterval duration = MAX(distance/ABS(velocity),0.15f);
            NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:NUMINT(self.openSide == MMDrawerSideRight?1:0), @"side",
                                 NUMFLOAT(duration), @"duration",
                                 NUMBOOL(duration > 0), @"animated", nil];
            [_proxy fireEvent:@"closemenu" withObject:evt propagate:NO checkForListener:NO];
        }
    
}

-(void)openDrawerSide:(MMDrawerSide)drawerSide animated:(BOOL)animated velocity:(CGFloat)velocity animationOptions:(UIViewAnimationOptions)options completion:(void (^)(BOOL))completion __attribute((objc_requires_super))
{
    [super openDrawerSide:drawerSide animated:animated velocity:velocity animationOptions:options completion:completion];
    if ([_proxy _hasListeners:@"openmenu"])
    {
        CGFloat distance = ABS(CGRectGetMinX(self.centerContainerView.frame));
        NSTimeInterval duration = MAX(distance/ABS(velocity),0.15f);
        NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:NUMINT(self.openSide == MMDrawerSideRight?1:0), @"side",
                             NUMFLOAT(duration), @"duration",
                             NUMBOOL(duration > 0), @"animated", nil];
        [_proxy fireEvent:@"openmenu" withObject:evt propagate:NO checkForListener:NO];
    }
    
}

-(void)updateDrawerVisualStateForDrawerSide:(MMDrawerSide)drawerSide percentVisible:(CGFloat)percentVisible __attribute((objc_requires_super))
{
    [super updateDrawerVisualStateForDrawerSide:drawerSide percentVisible:percentVisible];
    if (percentVisible > 0.0f) {
        if (drawerSide == MMDrawerSideLeft) {
            if (_fadeDegree > 0) {
                if (_leftViewFadingView.superview == nil) {
                    _leftViewFadingView.frame = self.leftDrawerViewController.view.bounds;
                    [self.leftDrawerViewController.view addSubview:_leftViewFadingView];
                }
                _leftViewFadingView.alpha = (1 - percentVisible)*_fadeDegree;
            }
            if (self.leftDisplacement != 0) {
                self.leftDrawerViewController.view.layer.transform = CATransform3DMakeTranslation(-self.leftDisplacement*(1 - percentVisible), 0.0, 0.0);
            }
        }
        else if (drawerSide == MMDrawerSideRight) {
            if (_fadeDegree > 0) {
                if (_rightViewFadingView.superview == nil) {
                    _rightViewFadingView.frame = self.rightDrawerViewController.view.bounds;
                    [self.rightDrawerViewController.view addSubview:_rightViewFadingView];
                }
                _rightViewFadingView.alpha = (1 - percentVisible)*_fadeDegree;
            }
            if (self.rightDisplacement != 0) {
                self.rightDrawerViewController.view.layer.transform = CATransform3DMakeTranslation(-self.rightDisplacement*(1 - percentVisible), 0.0, 0.0);
            }
        }
    }
    else {
        if (_fadeDegree > 0) {
            _leftViewFadingView.alpha = _rightViewFadingView.alpha = _fadeDegree;
        }
        if (self.leftDisplacement != 0) {
            self.leftDrawerViewController.view.layer.transform = CATransform3DMakeTranslation(-self.leftDisplacement, 0.0, 0.0);
        }
        if (self.rightDisplacement != 0) {
            self.rightDrawerViewController.view.layer.transform = CATransform3DMakeTranslation(-self.rightDisplacement, 0.0, 0.0);
        }
    }

}

-(void)setLeftDrawerViewController:(UIViewController *)leftDrawerViewController
{
    [super setLeftDrawerViewController:leftDrawerViewController];
    [_leftViewFadingView removeFromSuperview];

}

-(void)setRightDrawerViewController:(UIViewController *)rightDrawerViewController
{
    [super setRightDrawerViewController:rightDrawerViewController];
    [_rightViewFadingView removeFromSuperview];
    
}

-(void)setFadeDegree:(CGFloat)fadeDegree
{
    _fadeDegree = fadeDegree;
    if (_fadeDegree == 0.0f) {
        [_leftViewFadingView removeFromSuperview];
        [_rightViewFadingView removeFromSuperview];
    }
    else {
        if (_leftViewFadingView == nil) {
            _leftViewFadingView = [[UIView alloc] initWithFrame:[self view].bounds];
            _leftViewFadingView.backgroundColor = [UIColor blackColor];
            _leftViewFadingView.alpha = 0.0f;
            _leftViewFadingView.userInteractionEnabled = NO;
            _leftViewFadingView.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
        }
        if (_rightViewFadingView == nil) {
            _rightViewFadingView = [[UIView alloc] initWithFrame:[self view].bounds];
            _rightViewFadingView.backgroundColor = [UIColor blackColor];
            _rightViewFadingView.alpha = 0.0f;
            _rightViewFadingView.userInteractionEnabled = NO;
            _rightViewFadingView.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
        }
    }
}

-(void)panGestureCallback:(UIPanGestureRecognizer *)panGesture __attribute((objc_requires_super))
{
    [super panGestureCallback:panGesture];
    switch (panGesture.state) {
        case UIGestureRecognizerStateBegan:
            self.startingPanRect = self.centerContainerView.frame;
            if ([(TiViewProxy*)_proxy _hasListeners:@"scrollstart" checkParent:NO])
            {
                NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:NUMFLOAT(0), @"offset", nil];
                [_proxy fireEvent:@"scrollstart" withObject:evt propagate:NO checkForListener:NO];
            }
        case UIGestureRecognizerStateChanged:{
            CGRect newFrame = self.startingPanRect;
            CGPoint translatedPoint = [panGesture translationInView:self.centerContainerView];
            newFrame.origin.x = [self roundedOriginXForDrawerConstriants:CGRectGetMinX(self.startingPanRect)+translatedPoint.x];
            newFrame = CGRectIntegral(newFrame);
            CGFloat xOffset = newFrame.origin.x;
            if ([(TiViewProxy*)_proxy _hasListeners:@"scroll" checkParent:NO])
            {
                NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:NUMFLOAT(xOffset), @"offset", nil];
                [_proxy fireEvent:@"scroll" withObject:evt propagate:NO checkForListener:NO];
            }
            break;
        }
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateEnded:{
            CGRect newFrame = self.startingPanRect;
            CGPoint translatedPoint = [panGesture translationInView:self.centerContainerView];
            newFrame.origin.x = [self roundedOriginXForDrawerConstriants:CGRectGetMinX(self.startingPanRect)+translatedPoint.x];
            newFrame = CGRectIntegral(newFrame);
            CGFloat xOffset = newFrame.origin.x;
            self.startingPanRect = CGRectNull;
            if ([(TiViewProxy*)_proxy _hasListeners:@"scrollend" checkParent:NO])
            {
                NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:NUMFLOAT(xOffset), @"offset", nil];
                [_proxy fireEvent:@"scrollend" withObject:evt propagate:NO checkForListener:NO];
            }
            break;
        }
        default:
            break;
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	return [[[TiApp app] controller] shouldAutorotateToInterfaceOrientation:interfaceOrientation];
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation
{
    return [[[TiApp app] controller] preferredInterfaceOrientationForPresentation];
}

@end