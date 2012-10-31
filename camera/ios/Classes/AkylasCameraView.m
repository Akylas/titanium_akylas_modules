#import "AkylasCameraView.h"
#import "AkylasCameraViewProxy.h"
#import "TiApp.h"

@implementation AkylasCameraView
@synthesize scanview = _scanview;

-(void) cleanup
{
    NSLog(@"view cleanup");
}

-(void)dealloc
{
    NSLog(@"view dealloc");
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

- (void) setTorch_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL bval = [value boolValue];
    [(AkylasCameraViewProxy*)self.proxy setTorch:bval];
}


- (void) setCameraPosition_:(id)value
{
    [(AkylasCameraViewProxy*)self.proxy setCameraPosition:value];
}

@end
