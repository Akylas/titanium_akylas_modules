/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasSlidemenuSlideMenuProxy.h"
#import "AkylasSlidemenuSlideMenu.h"

#import "TiBase.h"
#import "TiUtils.h"
#import "TiApp.h"

@interface AkylasSlidemenuSlideMenu()
-(void)setLeftViewWidth_:(id)value withObject:(id)object;
-(void)setRightViewWidth_:(id)value withObject:(id)object;
@end

@implementation AkylasSlidemenuSlideMenuProxy
{
}

-(id)init
{
	if ((self = [super init]))
	{
        [self setDefaultReadyToCreateView:YES];
	}
	return self;
}
-(void)viewDidAttach
{
    [super viewDidAttach];
    [self dirtyItAll];
}

-(void)layoutChild:(TiViewProxy*)child optimize:(BOOL)optimize withMeasuredBounds:(CGRect)bounds
{
    [super layoutChild:child optimize:optimize withMeasuredBounds:bounds];
}
//
//-(void)refreshViewIfNeeded:(BOOL)recursive
//{
//    if ([self viewAttached]==NO) {
//        return;
//    }
//   [super refreshViewIfNeeded:recursive];
//}
//
//-(void)windowWillOpen
//{
//    [super windowWillOpen];
//}
//
//-(void) dealloc {
//    [super dealloc];
//}

-(void)viewWillDetach
{
    [(AkylasSlidemenuSlideMenu*)view detach];
}

-(AkylasSlidemenuDrawerController *)_controller {
	return [(AkylasSlidemenuSlideMenu*)[self view] controller];
}
//
//-(TiViewController *)controller {
//	return [self _controller];
//}

-(TiUIView*)newView {
    CGRect frame = [TiUtils appFrame];
    AkylasSlidemenuSlideMenu* menu = [[AkylasSlidemenuSlideMenu alloc] initWithFrame:frame];
	return menu;
}


-(void)windowDidClose
{
    [super windowDidClose];
}

-(UIView *)parentViewForChild:(TiViewProxy *)child
{
    NSArray* keys = [self allKeysForHoldedProxy:child];
    if ([keys count] > 0) {
        return nil;
    }
    return [super parentViewForChild:child];
}

#pragma mark - TiOrientationController

-(TiOrientationFlags) orientationFlags
{
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiWindowProxy * thisProxy = (TiWindowProxy *)[(TiViewController *)topVC proxy];
        if ([thisProxy conformsToProtocol:@protocol(TiOrientationController)]) {
            TiOrientationFlags result = [thisProxy orientationFlags];
            if (result != TiOrientationNone)
            {
                return result;
            }
        }
    }
    return [super orientationFlags];
}

#pragma mark - TiWindowProtocol
-(void)viewWillAppear:(BOOL)animated
{
    if ([self viewAttached]) {
        [[self _controller] viewWillAppear:animated];
    }
    [super viewWillAppear:animated];
}
-(void)viewWillDisappear:(BOOL)animated
{
    if ([self viewAttached]) {
        [[self _controller] viewWillDisappear:animated];
    }
    [super viewWillDisappear:animated];
}

-(void)viewDidAppear:(BOOL)animated
{
    if ([self viewAttached]) {
        [[self _controller] viewDidAppear:animated];
    }
    [super viewDidAppear:animated];
}
-(void)viewDidDisappear:(BOOL)animated
{
    if ([self viewAttached]) {
        [[self _controller] viewDidDisappear:animated];
    }
    [super viewDidDisappear:animated];
    
}

-(BOOL) hidesStatusBar
{
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiViewProxy* theProxy = [(TiViewController*)topVC proxy];
        if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
            return [(id<TiWindowProtocol>)theProxy hidesStatusBar];
        }
    }
    return [super hidesStatusBar];
}

-(void)gainFocus
{
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiViewProxy* theProxy = [(TiViewController*)topVC proxy];
        if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
            [(id<TiWindowProtocol>)theProxy gainFocus];
        }
    }
    [super gainFocus];
}

-(void)resignFocus
{
    if (!view) return;
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiViewProxy* theProxy = [(TiViewController*)topVC proxy];
        if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
            [(id<TiWindowProtocol>)theProxy resignFocus];
        }
    }
    [super resignFocus];
}


-(BOOL)containsChild:(TiProxy*)child
{
    MMDrawerSide side = [self _controller].openSide;
    return [super containsChild:child] ||
    (side == MMDrawerSideNone && [[self holdedProxyForKey:@"centerView"] containsChild:child]) ||
    (side == MMDrawerSideLeft && [[self holdedProxyForKey:@"leftView"] containsChild:child]) ||
    (side == MMDrawerSideRight && [[self holdedProxyForKey:@"rightView"] containsChild:child]);
}

-(TiProxy *)topWindow
{
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiViewProxy* theProxy = [(TiViewController*)topVC proxy];
        if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
            return [(id<TiWindowProtocol>)theProxy topWindow];
        }
    }
    return self;
}

-(UIStatusBarStyle)preferredStatusBarStyle;
{
    UIViewController* topVC = [[self _controller] centerViewController];
    if ([topVC isKindOfClass:[TiViewController class]]) {
        TiViewProxy* theProxy = [(TiViewController*)topVC proxy];
        if ([theProxy conformsToProtocol:@protocol(TiWindowProtocol)]) {
            return [(id<TiWindowProtocol>)theProxy preferredStatusBarStyle];
        }
    }
    return [super preferredStatusBarStyle];
}

-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if ([self viewAttached]) {
        [[self _controller] willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
    }
    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}
-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    if ([self viewAttached]) {
        [[self _controller] willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    }
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}
-(void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    if ([self viewAttached]) {
        [[self _controller] didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    }
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

//API

-(id)getRealLeftViewWidth:(id)args
{
    return NUMFLOAT([self _controller].maximumLeftDrawerWidth);
}

-(id)getRealRightViewWidth:(id)args
{
    return NUMFLOAT([self _controller].maximumRightDrawerWidth);
}

-(void)toggleLeftView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    BOOL animated = YES;
	if (args != nil)
		animated = [args boolValue];
    if ([self _controller].openSide == MMDrawerSideLeft) {
        [[self holdedProxyForKey:@"centerView"] focus:nil];
        [[self holdedProxyForKey:@"leftView"] blur:nil];
    } else {
        [[self holdedProxyForKey:@"centerView"] blur:nil];
        [[self holdedProxyForKey:@"leftView"] focus:nil];
    }
//    [self blur:nil];
    [[self _controller] toggleDrawerSide:MMDrawerSideLeft animated:animated completion:nil];
}
-(void)toggleRightView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    BOOL animated = YES;
	if (args != nil)
		animated = [args boolValue];
    if ([self _controller].openSide == MMDrawerSideRight) {
        [[self holdedProxyForKey:@"rightView"] blur:nil];
        [[self holdedProxyForKey:@"centerView"] focus:nil];
    } else {
        [[self holdedProxyForKey:@"centerView"] blur:nil];
        [[self holdedProxyForKey:@"rightView"] focus:nil];
    }
//    [self blur:nil];
    [[self _controller] toggleDrawerSide:MMDrawerSideRight animated:animated completion:nil];

}

-(void)openLeftView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    if ([self _controller].openSide == MMDrawerSideLeft) {
        return;
    }
    TiViewProxy* vp = [self holdedProxyForKey:@"leftView"];
    if (vp) {
        BOOL animated = YES;
        if (args != nil)
            animated = [args boolValue];
        [[self _controller] openDrawerSide:MMDrawerSideLeft animated:animated completion:nil];
        [[self holdedProxyForKey:@"rightView"] blur:nil];
        [[self holdedProxyForKey:@"centerView"] blur:nil];
        [vp focus:nil];
    }
}

-(void)openRightView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    if ([self _controller].openSide == MMDrawerSideRight) {
        return;
    }
    TiViewProxy* vp = [self holdedProxyForKey:@"rightView"];
    if (vp) {
        BOOL animated = YES;
        if (args != nil)
            animated = [args boolValue];
        [[self _controller] openDrawerSide:MMDrawerSideRight animated:animated completion:nil];
        [[self holdedProxyForKey:@"leftView"] blur:nil];
        [[self holdedProxyForKey:@"centerView"] blur:nil];
        [vp focus:nil];
    }
}

-(void)closeLeftView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    if ([self _controller].openSide != MMDrawerSideLeft) {
        return;
    }
    TiViewProxy* vp = [self holdedProxyForKey:@"leftView"];
    if (vp) {
        BOOL animated = YES;
        if (args != nil)
            animated = [args boolValue];
        [[self _controller] closeDrawerAnimated:animated completion:nil];
        [vp blur:nil];
        [[self holdedProxyForKey:@"centerView"] focus:nil];
    }
}

-(void)closeRightView:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    if ([self _controller].openSide != MMDrawerSideRight) {
        return;
    }
    TiViewProxy* vp = [self holdedProxyForKey:@"rightView"];
    if (vp) {
        BOOL animated = YES;
        if (args != nil)
            animated = [args boolValue];
        [[self _controller] closeDrawerAnimated:animated completion:nil];
        [vp blur:nil];
        [[self holdedProxyForKey:@"centerView"] focus:nil];
    }
}


-(void)closeViews:(id)args
{
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber);
    ENSURE_UI_THREAD_1_ARG(args);
    if ([self _controller].openSide == MMDrawerSideNone) {
        return;
    }
    BOOL animated = YES;
	if (args != nil)
		animated = [args boolValue];
//    [self blur:nil];
    [[self holdedProxyForKey:@"leftView"] blur:nil];
    [[self holdedProxyForKey:@"rightView"] blur:nil];
    [[self holdedProxyForKey:@"centerView"] focus:nil];
    [[self _controller] closeDrawerAnimated:animated completion:nil];
}

-(void)setLeftViewWidth:(id)value withObject:(id)object
{
    TiThreadPerformBlockOnMainThread(^{
        [(AkylasSlidemenuSlideMenu*)[self view] setLeftViewWidth_:value withObject:object];
    }, NO);
}

-(void)setRightViewWidth:(id)value withObject:(id)object
{
    TiThreadPerformBlockOnMainThread(^{
        [(AkylasSlidemenuSlideMenu*)[self view] setRightViewWidth_:value withObject:object];
    }, NO);
}

@end
