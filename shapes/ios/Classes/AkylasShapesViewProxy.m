//
//  ShapeViewProxy.m
//  Titanium
//
//  Created by Martin Guillon on 10/08/13.
//
//

#import "AkylasShapesViewProxy.h"
#import "ShapeProxy.h"

@implementation AkylasShapesViewProxy
- (id)init {
    if (self = [super init])
    {
    }
    return self;
}

- (void) dealloc
{
//    for (ShapeProxy* proxy in [self shapes]) {
//        [proxy setShapeViewProxy:nil];
//    }
	[super dealloc];
}

-(NSArray*)shapes
{
    if (childrenCount == 0) return nil;
    pthread_rwlock_rdlock(&childrenLock);
    NSArray* copy = [[children filteredArrayUsingPredicate:[NSPredicate predicateWithBlock:^BOOL(id object, NSDictionary *bindings) {
        return [object isKindOfClass:[ShapeProxy class]];
    }]] retain];
    pthread_rwlock_unlock(&childrenLock);

	return [copy autorelease];
}

-(void)detachView
{
    ENSURE_UI_THREAD_0_ARGS
    for (ShapeProxy* shape in [self shapes]) {
        [shape removeFromSuperLayer];
    }
	[super detachView];
}

static NSArray *supportedEvents;
+ (NSArray *)supportedEvents
{
    if (!supportedEvents)
        supportedEvents = [[NSArray arrayWithObjects:@"click",@"dbclick",@"singtap",@"doubletap"
                          ,@"longpress",@"touchstart", @"touchmove"
                          , @"touchend", @"touchcancel",nil] retain];
    
    return supportedEvents;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    if (bounds.size.width == 0 || bounds.size.height == 0) return;
    for (ShapeProxy* shape in [self shapes]) {
        [shape boundsChanged:bounds];
    }
}

-(void)update:(id)arg
{
    if (![self viewAttached]) return;
    ENSURE_UI_THREAD_1_ARG(arg)
    [CATransaction begin];
    [CATransaction setDisableActions: YES];
    for (ShapeProxy* shape in [self shapes]) {
        [shape boundsChanged:[self view].bounds];
    }
    [[self view] setNeedsDisplay];
    [CATransaction commit];
    
}

-(void)redraw
{
    [[self view] setNeedsDisplay];
}

-(void)childAdded:(TiProxy*)child atIndex:(NSInteger)position shouldRelayout:(BOOL)shouldRelayout
{
    [super childAdded:child atIndex:position shouldRelayout:shouldRelayout];
    if (![child isKindOfClass:[ShapeProxy class]]) {
        return;
	}
    ShapeProxy* shape = (ShapeProxy*)child;
//    [shape setShapeViewProxy:self];
    if (shouldRelayout && [self viewAttached]) {
        [[self view].layer addSublayer:[shape layer]];
        [shape boundsChanged:self.view.bounds];
        [[self view] setNeedsDisplay];
    }
}
-(void)childRemoved:(TiProxy*)child
{
    if (![child isKindOfClass:[ShapeProxy class]]) {
        return;
	}
	
    ShapeProxy* shape = (ShapeProxy*)child;
    [[shape layer] removeFromSuperlayer];
//    [shape setShapeViewProxy:nil];
    if ([self viewAttached]) {
        [[self view] setNeedsDisplay];
    }
}

-(BOOL)animating
{
    return [self viewAttached] && [super animating];
}

-(BOOL)_hasListeners:(NSString *)type
{
    BOOL handledByChildren = NO;
    for (ShapeProxy* shape in [self shapes]) {
        handledByChildren |= [shape _hasListeners:type checkParent:NO];
    }
	return [super _hasListeners:type] || handledByChildren;
}

-(void)fireEvent:(NSString*)type withObject:(id)obj propagate:(BOOL)propagate reportSuccess:(BOOL)report errorCode:(NSInteger)code message:(NSString*)message checkForListener:(BOOL)checkForListener
{
	if ([[AkylasShapesViewProxy supportedEvents] indexOfObject:type] != NSNotFound && view.userInteractionEnabled && childrenCount > 0) {
        CGPoint point  = CGPointMake(-1, -1);
        if ([obj isKindOfClass:[NSDictionary class]]) {
            point.x = [[((NSDictionary*)obj) objectForKey:@"x"] intValue];
            point.y = [[((NSDictionary*)obj) objectForKey:@"y"] intValue];
        }
        BOOL handledByChildren = NO;
        
        for (ShapeProxy* shape in [self shapes]) {
            handledByChildren |= [shape handleTouchEvent:type withObject:obj propagate:propagate point:point];
        }
        if (handledByChildren && yn) {
            return YES;
        }
    }
    return [super fireEvent:type withObject:obj propagate:propagate reportSuccess:report errorCode:code message:message checkForListener:checkForListener];
}

@end
