#import "AkylasScancodeView.h"
#import "AkylasScancodeViewProxy.h"
#import "TiApp.h"

@implementation AkylasScancodeView
@synthesize scanview = _scanview;

//-(id)init{
//    self = [super init];
//    if (self) {
////        scanview = _controller.view;
////        [self addSubview:scanview];
//    }
//    return self;
//}

//-(UIView*)scanview
//{
//	if (scanview==nil  && _controller != nil)
//	{
//        scanview = _controller.view;
//        [self addSubview:scanview];
//	}
//	return scanview;
//}

-(void) cleanup
{
    NSLog(@"view cleanup");
    
//	RELEASE_TO_NIL(scanview);
}

-(void)dealloc
{
    NSLog(@"view dealloc");
//	if (listeners!=nil)
//	{
//		RELEASE_TO_NIL(listeners);
//	}
    [self cleanup];
	[super dealloc];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [TiUtils setView:[self scanview] positionRect:bounds];
    [super frameSizeChanged:frame bounds:bounds];
}

-(BOOL)hasTouchableListener
{
	// since this guy only works with touch events, we always want them
	// just always return YES no matter what listeners we have registered
	return YES;
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];
    if (hitView == [self scanview])
        return self;
    return hitView;
}


-(CGFloat)autoWidthForWidth:(CGFloat)value
{
	return [[self scanview] sizeThatFits:CGSizeMake(value, 0)].width;
}

-(CGFloat)autoHeightForWidth:(CGFloat)value
{
	return [[self scanview] sizeThatFits:CGSizeMake(value, 0)].height;
}

-(void)setBackgroundColor_:(id)value
{
	if (value!=nil)
	{
		TiColor *color = [TiUtils colorValue:value];
		[_scanview setBackgroundColor:[color _color]];
	}
}
//
//- (void)viewWillAppear:(BOOL)animated
//{
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller viewWillAppear:animated];
//}
//
//- (void)viewDidAppear:(BOOL)animated
//{
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller viewDidAppear:animated];
//}
//
//- (void)viewWillDisappear:(BOOL)animated
//{
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller viewWillDisappear:animated];
//}
//
//- (void)viewDidDisappear:(BOOL)animated
//{
//    NSLog(@"viewDidDisappear");
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller viewDidDisappear:animated];
//}
//
//- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
////    NSLog(@"module willAnimateRotationToInterfaceOrientation");
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}
//
//- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
//{
//    return YES;
//}
//
//- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
////    NSLog(@"module willRotateToInterfaceOrientation");
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}
//
//- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
//{
////    NSLog(@"module didRotateFromInterfaceOrientation");
////    if ([self scanview]) //ensure _controller is initiated
//        [_controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
//}

- (void) setTorch_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL bval = [value boolValue];
    [(AkylasScancodeViewProxy*)self.proxy setTorch:bval];
}

- (void) setCenteredCropRect_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL bval = [value boolValue];
    [(AkylasScancodeViewProxy*)self.proxy setCenteredCropRect:bval];
}

- (void) setCropRect_:(id)value
{
    [(AkylasScancodeViewProxy*)self.proxy setCropRect:value];
}

- (void) setReaders_:(id)value
{
    ENSURE_TYPE_OR_NIL(value,NSArray);
    [(AkylasScancodeViewProxy*)self.proxy setReaders:(NSArray*)value];
}

- (void) setCameraPosition_:(id)value
{
    [(AkylasScancodeViewProxy*)self.proxy setCameraPosition:value];
}

@end
