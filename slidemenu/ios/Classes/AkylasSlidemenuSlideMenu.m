/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiBase.h"
#import "TiUIWindowProxy.h"
#import "AkylasSlidemenuSlideMenu.h"
#import "AkylasSlidemenuSlideMenuProxy.h"
#import "TiUtils.h"
#import "UIViewController+ADTransitionController.h"
#import "AkylasSlidemenuVisualState.h"
#import "TiTransition.h"

@interface AkylasSlidemenuSlideMenu()
{
@private
	AkylasSlidemenuDrawerController *_controller;    
//    TiViewProxy* leftView;
//    TiViewProxy* rightView;
//    TiViewProxy* centerView;
    TiDimension _leftScrollScale;
    TiDimension _rightScrollScale;
    TiDimension _leftViewWidth;
    TiDimension _rightViewWidth;
    TiTransition* _leftTransition;
    TiTransition* _rightTransition;
}
@end

@implementation AkylasSlidemenuSlideMenu

-(UIViewController *) controllerForViewProxy:(TiViewProxy * )theProxy withFrame:(CGRect)frame
{
    theProxy.sandboxBounds = CGRectMake(0, 0, frame.size.width, frame.size.height);
    if ([theProxy isKindOfClass:[TiWindowProxy class]]) {
        TiWindowProxy* window = (TiWindowProxy*)theProxy;
        [window setIsManaged:YES]; // has to be done before windowWillOpen!
    }
    UIViewController* controller = nil;
    if([theProxy respondsToSelector:@selector(hostingController)])
    {
        controller = [theProxy hostingController];
    }
    [theProxy windowWillOpen];
//    [proxy setParent:(AkylasSlidemenuSlideMenuProxy*)self.proxy];
    [theProxy windowDidOpen];
    return controller;
}

-(void)updateBounds:(CGRect)newBounds
{
    [super updateBounds:newBounds];
}


-(id)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame])
    {
        _leftScrollScale = TiDimensionDip(0.0f);
        _rightScrollScale = TiDimensionDip(0.0f);
        _leftViewWidth = TiDimensionDip(200.0f);
        _rightViewWidth = TiDimensionDip(200.0f);
    }
    return self;
}

- (void)closeWindowProxy:(id)view
{
    if ([view isKindOfClass:[TiWindowProxy class]])
    {
        TiWindowProxy* window = (TiWindowProxy*)view;
        [window windowDidClose];
    }
}
#define RELEASE_WITH_DETACH_TO_NIL(x) { if (x!=nil) { x.parent = nil; [x detachView]; [x release]; x = nil; } }
#define RELEASE_WITH_PARENT_TO_NIL(x) { if (x!=nil) { x.parent = nil; [x release]; x = nil; } }

-(void)cleanup
{
    TiThreadPerformBlockOnMainThread(^{
        [_controller removeFromParentViewController];
        RELEASE_TO_NIL(_controller);
//        [self closeWindowProxy:centerView];
//        [self closeWindowProxy:leftView];
//        [self closeWindowProxy:rightView];
//        RELEASE_WITH_DETACH_TO_NIL(centerView);
//        RELEASE_WITH_DETACH_TO_NIL(leftView);
//        RELEASE_WITH_DETACH_TO_NIL(rightView);
        RELEASE_TO_NIL(_leftTransition);
        RELEASE_TO_NIL(_rightTransition);
    }, YES);
}


-(void)detach
{
    [self cleanup];
}

-(void)windowDidClose
{

}

-(void)dealloc
{
    [self cleanup];
    [super dealloc];
}

-(AkylasSlidemenuDrawerController*)controller
{
	if (_controller==nil)
	{
        _controller = [[AkylasSlidemenuDrawerController alloc] initWithNibName:nil bundle:nil];
        _controller.proxy = [self viewProxy];
        
        UIView * controllerView = [_controller view];
        [controllerView setFrame:[self bounds]];
        [self addSubview:controllerView];
        _controller.openDrawerGestureModeMask = MMOpenDrawerGestureModePanningCenterView;
        _controller.closeDrawerGestureModeMask = MMCloseDrawerGestureModePanningCenterView | MMCloseDrawerGestureModeTapCenterView;

	}
	return _controller;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    if (![(TiViewProxy*)self.proxy isRotating])
    {
        [self update];
    }
    [super frameSizeChanged:frame bounds:bounds];
}

//API

-(void)setShowUnderStatusBar_:(id)args
{
    ENSURE_UI_THREAD(setShowUnderStatusBar_,args);
    ENSURE_SINGLE_ARG_OR_NIL(args, NSNumber)
    [self controller].showsStatusBarBackgroundView = ![TiUtils boolValue:args def:false];
}

-(void)setCenterView_:(id)value
{
    __block TiViewProxy* oldCenterView = nil;
    UIViewController* ctlr;
    //to ensure childControllerContainerView existence
    [[self controller] childControllerContainerViewFrame];
    if ([value isKindOfClass:[UIViewController class]]) {
        ctlr = value;
    }
    else {
        ENSURE_UI_THREAD(setCenterView_,value);
//        ENSURE_TYPE_OR_NIL(args,TiViewProxy);
        
        oldCenterView = [[[self viewProxy] holdedProxyForKey:@"centerView"] retain];
        if (value == oldCenterView) {
            
            RELEASE_TO_NIL(oldCenterView);
            [[self controller] closeDrawerAnimated:YES completion:nil];
            return;
        }
        
        if ([oldCenterView isKindOfClass:[TiWindowProxy class]]) {
            TiWindowProxy* window = (TiWindowProxy*)oldCenterView;
            [window setIsManaged:NO];
            [window resignFocus];
        }
        
        id vp = [[self viewProxy] addObjectToHold:value forKey:@"centerView"];
        if (IS_OF_CLASS(vp, TiViewProxy)) {
            CGRect frame = [[self controller] childControllerContainerViewFrame];
            //        view = [(TiViewProxy*)vp getAndPrepareViewForOpening:frame];
            ctlr = [self controllerForViewProxy:vp withFrame:frame];
            [ctlr view];
            if (IS_OF_CLASS(vp, TiWindowProxy)) {
                TiWindowProxy* window = (TiWindowProxy*)vp;
                [window updateOrientationModes];
                [window gainFocus];
            }
        }

    }
    
    [[self controller] setCenterViewController:ctlr withFullCloseAnimation:YES completion:^(BOOL finished) {
        if (oldCenterView) {
//            [self closeWindowProxy:odlCenterView];
            RELEASE_TO_NIL(oldCenterView);
        }
    }];
}

-(void)setLeftView_:(id)value
{
    ENSURE_UI_THREAD(setLeftView_,value);
//    ENSURE_TYPE_OR_NIL(args,TiViewProxy);
    
//    UIView* view = nil;
    id vp = [[self viewProxy] addObjectToHold:value forKey:@"leftView"];
    if (IS_OF_CLASS(vp, TiViewProxy)) {
        CGRect frame = [[self controller] childControllerContainerViewFrame];
        frame.size.width = [self controller].maximumLeftDrawerWidth;
//        view = [(TiViewProxy*)vp getAndPrepareViewForOpening:frame];
        [self controller].leftDrawerViewController = [self controllerForViewProxy:vp withFrame:frame];
    } else {
        [self controller].leftDrawerViewController = nil;
    }

    
//	RELEASE_WITH_PARENT_TO_NIL(leftView);
//    if (args != nil) {
//        leftView = [args retain];
//        CGRect frame = [[self controller] childControllerContainerViewFrame];
//        frame.size.width = [self controller].maximumLeftDrawerWidth;
//        [self controller].leftDrawerViewController = [self controllerForViewProxy:leftView withFrame:frame];
//    }
//    else {
//        [self controller].leftDrawerViewController = nil;
//    }
}

-(void)setRightView_:(id)value
{
    ENSURE_UI_THREAD(setRightView_,value);
//    ENSURE_TYPE_OR_NIL(args,TiViewProxy);
//    
//	RELEASE_WITH_PARENT_TO_NIL(rightView);
//    if (args != nil) {
//        rightView = [args retain];
//        CGRect frame = [[self controller] childControllerContainerViewFrame];
//        frame.size.width = [self controller].maximumRightDrawerWidth;
//        [self controller].rightDrawerViewController = [self controllerForViewProxy:rightView withFrame:frame];
//    }
//    else {
//        [self controller].rightDrawerViewController = nil;
//    }
    
//    UIView* view = nil;
    id vp = [[self viewProxy] addObjectToHold:value forKey:@"rightView"];
    if (IS_OF_CLASS(vp, TiViewProxy)) {
        CGRect frame = [[self controller] childControllerContainerViewFrame];
        frame.size.width = [self controller].maximumRightDrawerWidth;
//        view = [(TiViewProxy*)vp getAndPrepareViewForOpening:frame];
        [self controller].rightDrawerViewController = [self controllerForViewProxy:vp withFrame:frame];
    } else {
        [self controller].rightDrawerViewController = nil;
    }
}

-(void)setLeftViewWidth_:(id)value withObject:(id)object
{
//	BOOL boolValue = [TiUtils boolValue:value];
	BOOL animated = [TiUtils boolValue:@"animated" properties:object def:NO];
	//TODO: Value checking and exception generation, if necessary.
    
	[self.proxy replaceValue:value forKey:@"leftViewWidth" notification:NO];
    _leftViewWidth = [TiUtils dimensionValue:value];
    [self updateLeftViewWidth:animated];
}

-(void)setRightViewWidth_:(id)value withObject:(id)object
{
//	BOOL boolValue = [TiUtils boolValue:value];
	BOOL animated = [TiUtils boolValue:@"animated" properties:object def:NO];
	//TODO: Value checking and exception generation, if necessary.
    
	[self.proxy replaceValue:value forKey:@"rightViewWidth" notification:NO];
    _rightViewWidth = [TiUtils dimensionValue:value];
    [self updateRightViewWidth:animated];
}


-(void)update
{
    [self updateLeftViewWidth];
    [self updateRightViewWidth];
}

-(void)updateLeftViewWidth:(BOOL)animated
{
    [[self controller] setMaximumLeftDrawerWidth:TiDimensionCalculateValue(_leftViewWidth, self.bounds.size.width) animated:animated completion:^(BOOL finished) {
        if ([[self viewProxy] _hasListeners:@"menuwidth" checkParent:NO])
        {
            NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:@(0), @"side",
                                 @(animated), @"animated", nil];
            [self.proxy fireEvent:@"menuwidth" withObject:evt propagate:NO checkForListener:NO];
        }
    }] ;
    [self updateLeftDisplacement];
}


-(void)updateLeftViewWidth
{
    [self updateLeftViewWidth:NO];
}

-(void)updateRightViewWidth:(BOOL)animated
{
    [[self controller] setMaximumRightDrawerWidth:TiDimensionCalculateValue(_rightViewWidth, self.bounds.size.width) animated:animated completion:^(BOOL finished) {
        if ([[self viewProxy] _hasListeners:@"menuwidth" checkParent:NO])
        {
            NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:@(1), @"side",
                                 @(animated), @"animated", nil];
            [self.proxy fireEvent:@"menuwidth" withObject:evt propagate:NO checkForListener:NO];
        }
    }] ;

    [self updateRightDisplacement];
}

-(void)updateRightViewWidth
{
    [self updateRightViewWidth:NO];
}

-(void)updateLeftDisplacement
{
    CGFloat leftMenuWidth = [self controller].maximumLeftDrawerWidth;

    [self controller].leftDisplacement = -TiDimensionCalculateValue(_leftScrollScale, leftMenuWidth);
}

-(void)updateRightDisplacement
{
    CGFloat rightMenuWidth = [self controller].maximumRightDrawerWidth;
    [self controller].rightDisplacement = -TiDimensionCalculateValue(_rightScrollScale, rightMenuWidth);
}

-(void)setLeftViewDisplacement_:(id)args
{
    _leftScrollScale = [TiUtils dimensionValue:args];
    [self updateLeftDisplacement];
}

-(void)setRightViewDisplacement_:(id)args
{
    _rightScrollScale = [TiUtils dimensionValue:args];
    [self updateRightDisplacement];
}

-(void)setFading_:(id)args
{
    ENSURE_TYPE_OR_NIL(args,NSNumber);
    [self controller].fadeDegree = [args floatValue];
}

-(id) navControllerForController:(UIViewController*)theController
{
    if ([theController transitionController] != nil)
        return [theController transitionController];
    return [theController navigationController];
}

//Properties
- (void)setPanningMode_:(id)args
{
    ENSURE_UI_THREAD(setPanningMode_,args);
    if(args !=nil){
//        NSInteger num = [TiUtils intValue:args];
        [self controller].openDrawerGestureModeMask = [TiUtils intValue:args];
    }
}

- (void)setShadowWidth_:(id)args
{
    ENSURE_TYPE_OR_NIL(args, NSNumber);
    [self controller].showsShadow = ([args floatValue] != 0.0f);
}

-(void)setLeftTransition_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    RELEASE_TO_NIL(_leftTransition);
    _leftTransition = [[TiTransitionHelper transitionFromArg:value containerView:self] retain];
    if (_leftTransition) {
        MMDrawerControllerDrawerVisualStateBlock visualStateBlock =
        ^(MMDrawerController * drawerController, MMDrawerSide drawerSide, CGFloat percentVisible){
            if(percentVisible <= 1.f){
//                CGFloat maxDrawerWidth = MAX(drawerController.maximumLeftDrawerWidth,drawerController.visibleLeftDrawerWidth);
                [_leftTransition transformView:drawerController.leftDrawerViewController.view withPosition:percentVisible-1];
            }
        };

        [self controller].leftVisualBlock = visualStateBlock;
    }
	else [self controller].leftVisualBlock = nil;
}


-(void)setRightTransition_:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    RELEASE_TO_NIL(_rightTransition);
    _rightTransition = [[TiTransitionHelper transitionFromArg:value containerView:self] retain];
    if (_rightTransition) {
        MMDrawerControllerDrawerVisualStateBlock visualStateBlock =
        ^(MMDrawerController * drawerController, MMDrawerSide drawerSide, CGFloat percentVisible){
            if(percentVisible <= 1.f){
//                CGFloat maxDrawerWidth = MAX(drawerController.maximumRightDrawerWidth,drawerController.visibleRightDrawerWidth);
                [_rightTransition transformView:drawerController.rightDrawerViewController.view withPosition:1-percentVisible];
            }
        };
        
        [self controller].rightVisualBlock = visualStateBlock;
    }
	else [self controller].rightVisualBlock = nil;
}

@end