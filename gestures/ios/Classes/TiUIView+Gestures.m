
#import "TiUtils.h"
#import "TiUIView+Gestures.h"

#include <objc/runtime.h> //objc runtime api’s

#define RECOGNIZE_SIMULTANEOUSLY (1 << 16)

@implementation TiUIView (TiUIView_Gestures)

- (void)setRecognizeSimultaneously_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSNumber);
    BOOL value_ = [value boolValue];
    
    if(value_)
    {
        self.tag |= RECOGNIZE_SIMULTANEOUSLY;
    }
    else
    {
        self.tag &= ~RECOGNIZE_SIMULTANEOUSLY;
    }
}

- (NSDictionary*)touchToDictionary: (UITouch*) touch {
    NSMutableDictionary *result = [NSMutableDictionary dictionaryWithDictionary:[TiUtils pointToDictionary:[touch locationInView:self]]];
    [result setValue:[TiUtils pointToDictionary:[touch locationInView:nil]] forKey:@"globalPoint"];
    return result;
}

- (void)addTouches: (NSSet*)touches toEvent:(NSMutableDictionary*)target 
{
    NSMutableDictionary *ts = [NSMutableDictionary dictionary];
    int i = 0;
    for (UITouch* t in touches) {
        [ts setObject:[self touchToDictionary:t] forKey:[NSString stringWithFormat:@"%d",i]];
        i++;
    }
    [target setValue:[NSDictionary dictionaryWithDictionary:ts] forKey:@"points"];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event 
{
	int count = [[event touchesForView:self] count];
    
	if (count == 0) {
		//The touch events are not for this view. Propagate and return
		[super touchesBegan:touches withEvent:event];
		return;
	}
//    NSLog(@"nb touches %d", [touches count]);
	UITouch *touch = [touches anyObject];
    
//	if (handlesTouches)
//	{
		NSMutableDictionary *evt = [NSMutableDictionary dictionaryWithDictionary:[TiUtils pointToDictionary:[touch locationInView:self]]];
		[evt setValue:[TiUtils pointToDictionary:[touch locationInView:nil]] forKey:@"globalPoint"];
        [self addTouches:touches toEvent:evt];
		if ([proxy _hasListeners:@"touchstart"])
		{
			[proxy fireEvent:@"touchstart" withObject:[NSDictionary dictionaryWithDictionary:evt] propagate:YES];
			[self handleControlEvents:UIControlEventTouchDown];
		}
//	}
	[super touchesBegan:touches withEvent:event];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event 
{
	int count = [[event touchesForView:self] count];
    
	if (count == 0) {
		//The touch events are not for this view. Propagate and return
		[super touchesMoved:touches withEvent:event];
		return;
	}
    
	UITouch *touch = [touches anyObject];
//	if (handlesTouches)
//	{
		NSMutableDictionary *evt = [NSMutableDictionary dictionaryWithDictionary:[TiUtils pointToDictionary:[touch locationInView:self]]];
		[evt setValue:[TiUtils pointToDictionary:[touch locationInView:nil]] forKey:@"globalPoint"];
        [self addTouches:event.allTouches toEvent:evt];
		if ([proxy _hasListeners:@"touchmove"])
		{
			[proxy fireEvent:@"touchmove" withObject:[NSDictionary dictionaryWithDictionary:evt] propagate:YES];
		}
//	}
    
	if (touchDelegate!=nil)
	{
		[touchDelegate touchesMoved:touches withEvent:event];
	}
	[super touchesMoved:touches withEvent:event];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event 
{
	int count = [[event touchesForView:self] count];
    
	if (count == 0) {
		//The touch events are not for this view. Propagate and return
		[super touchesEnded:touches withEvent:event];
		return;
	}
    
//	if (handlesTouches)
//	{
		UITouch *touch = [touches anyObject];
		NSMutableDictionary *evt = [NSMutableDictionary dictionaryWithDictionary:[TiUtils pointToDictionary:[touch locationInView:self]]];
		[evt setValue:[TiUtils pointToDictionary:[touch locationInView:nil]] forKey:@"globalPoint"];
        [self addTouches:touches toEvent:evt];
		if ([proxy _hasListeners:@"touchend"])
		{
			[proxy fireEvent:@"touchend" withObject:[NSDictionary dictionaryWithDictionary:evt] propagate:YES];
			[self handleControlEvents:UIControlEventTouchCancel];
		}
//	}
    
	if (touchDelegate!=nil)
	{
		[touchDelegate touchesEnded:touches withEvent:event];
	}
	[super touchesEnded:touches withEvent:event];
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event 
{
	int count = [[event touchesForView:self] count];
    
	if (count == 0) {
		//The touch events are not for this view. Propagate and return
		[super touchesCancelled:touches withEvent:event];
		return;
	}
    
//	if (handlesTouches)
//	{
		UITouch *touch = [touches anyObject];
		CGPoint point = [touch locationInView:self];
        NSMutableDictionary *evt = [NSMutableDictionary dictionaryWithDictionary:[TiUtils pointToDictionary:point]];
        [evt setValue:[TiUtils pointToDictionary:[touch locationInView:nil]] forKey:@"globalPoint"];
        [self addTouches:touches toEvent:evt];
		if ([proxy _hasListeners:@"touchcancel"])
		{
			[proxy fireEvent:@"touchcancel" withObject:[NSDictionary dictionaryWithDictionary:evt] propagate:YES];
		}
//	}
    
	if (touchDelegate!=nil)
	{
		[touchDelegate touchesCancelled:touches withEvent:event];
	}
	[super touchesCancelled:touches withEvent:event];
}


@end
