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
        mShapes = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void) dealloc
{
    for (ShapeProxy* proxy in mShapes) {
        [proxy setShapeViewProxy:nil];
        [self forgetProxy:proxy];
    }
	RELEASE_TO_NIL(mShapes);
	[super dealloc];
}

-(NSArray*)shapes
{
    return [NSArray arrayWithArray:mShapes];
}

-(void)detachView
{
    ENSURE_UI_THREAD_0_ARGS
    for (ShapeProxy* shape in mShapes) {
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
    if (CGSizeEqualToSize(bounds.size,CGSizeZero)) return;
    for (int i = 0; i < [mShapes count]; i++) {
        ShapeProxy* shapeProxy = [mShapes objectAtIndex:i];
        [shapeProxy boundsChanged:bounds];
    }
}

-(void)update:(id)arg
{
    if (![self viewAttached]) return;
    ENSURE_UI_THREAD_1_ARG(arg)
    [CATransaction begin];
    [CATransaction setDisableActions: YES];
    for (ShapeProxy* shapeProxy in mShapes) {
        [shapeProxy boundsChanged:[self view].bounds];
    }
    [[self view] setNeedsDisplay];
    [CATransaction commit];
    
}

-(void)redraw
{
    [[self view] setNeedsDisplay];
}

-(void)setShapes:(id)args
{
	// Clear the current list of plots
    [mShapes enumerateObjectsUsingBlock:^(ShapeProxy * child, NSUInteger idx, BOOL *stop) {
        [self forgetProxy:child];
        [mShapes removeObject:child];
        [[child layer] removeFromSuperlayer];
        [child setShapeViewProxy:nil];
    }];
	RELEASE_TO_NIL(mShapes);
	// Now set the current list to this new list
	[self add:args];
}

-(void)addProxy:(id)child atIndex:(NSInteger)position shouldRelayout:(BOOL)shouldRelayout
{
    if (![child isKindOfClass:[ShapeProxy class]]) {
		[super addProxy:child atIndex:position shouldRelayout:shouldRelayout];
        return;
	}
    
    if ([mShapes indexOfObject:child] == NSNotFound) {
        [mShapes addObject:child];
        [self rememberProxy:child];
        [child setShapeViewProxy:self];
        if (shouldRelayout && [self viewAttached]) {
            [[self view].layer addSublayer:[child layer]];
            [child boundsChanged:self.view.bounds];
            [[self view] setNeedsDisplay];
        }
    }
}
-(void)removeProxy:(id)child
{
    if (![child isKindOfClass:[ShapeProxy class]]) {
		[super removeProxy:child];
        return;
	}
	
    if ([mShapes indexOfObject:child] != NSNotFound) {
        [self forgetProxy:child];
        [mShapes removeObject:child];
        [[child layer] removeFromSuperlayer];
        [child setShapeViewProxy:nil];
        if ([self viewAttached]) {
            [[self view] setNeedsDisplay];
        }
    }
}

-(BOOL)animating
{
    return [self viewAttached] && [super animating];
}

-(BOOL)_hasListeners:(NSString *)type
{
    BOOL handledByChildren = NO;
    for (int i = 0; i < [mShapes count]; i++) {
        ShapeProxy* shapeProxy = [mShapes objectAtIndex:i];
        handledByChildren |= [shapeProxy _hasListeners:type];
    }
	return [super _hasListeners:type] || handledByChildren;
}

-(void)fireEvent:(NSString*)type withObject:(id)obj propagate:(BOOL)propagate reportSuccess:(BOOL)report errorCode:(int)code message:(NSString*)message checkForListener:(BOOL)checkForListener
{
	if ([[AkylasShapesViewProxy supportedEvents] indexOfObject:type] != NSNotFound && [mShapes count] > 0) {
        CGPoint point  = CGPointMake(-1, -1);
        if ([obj isKindOfClass:[NSDictionary class]]) {
            point.x = [[((NSDictionary*)obj) objectForKey:@"x"] intValue];
            point.y = [[((NSDictionary*)obj) objectForKey:@"y"] intValue];
        }
        BOOL handledByChildren = NO;
        
        for (int i = 0; i < [mShapes count]; i++) {
            ShapeProxy* shapeProxy = [mShapes objectAtIndex:i];
            handledByChildren |= [shapeProxy handleTouchEvent:type withObject:obj propagate:propagate point:point];
        }
        if (handledByChildren && yn) {
            return YES;
        }
    }
    return [super fireEvent:type withObject:obj propagate:propagate reportSuccess:report errorCode:code message:message checkForListener:checkForListener];
}


+(Class)proxyClassFromString:(NSString*)qualifiedName
{
    Class proxyClass = (Class)CFDictionaryGetValue([TiProxy classNameLookup], qualifiedName);
	if (proxyClass == nil) {
		NSString *prefix = [NSString stringWithFormat:@"%@%s",@"Ak","ylasShapes."];
		if ([qualifiedName hasPrefix:prefix]) {
			qualifiedName = [qualifiedName stringByReplacingOccurrencesOfString:prefix withString:@"AkylasShapes"];
		}
        else {
            return [[TiViewProxy class] proxyClassFromString:qualifiedName];
        }
		NSString *className = [[qualifiedName stringByReplacingOccurrencesOfString:@"." withString:@""] stringByAppendingString:@"Proxy"];
		proxyClass = NSClassFromString(className);
		if (proxyClass==nil) {
			DebugLog(@"[WARN] Attempted to load %@: Could not find class definition.", className);
			@throw [NSException exceptionWithName:@"org.appcelerator.module"
                                           reason:[NSString stringWithFormat:@"Class not found: %@", qualifiedName]
                                         userInfo:nil];
		}
		CFDictionarySetValue([TiProxy classNameLookup], qualifiedName, proxyClass);
	}
    return proxyClass;
}

@end
