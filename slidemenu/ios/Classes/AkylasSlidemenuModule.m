#import "AkylasSlidemenuModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "MMDrawerController+Subclass.h"
#import "AkylasSlidemenuSlideMenuProxy.h"
#import "NSData+Additions.h"

@implementation AkylasSlidemenuModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"9ed69418-fbbc-45a8-aa1b-35185bc2b7b5";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.slidemenu";
}

#pragma mark Password
-(NSString*)getPasswordKey {
    return @"akylas.modules.key";
}
-(NSString*) getPassword {
    return stringWithHexString(@"7265745b496b2466553b486f736b7b4f");
}


#pragma Public APIs

MAKE_SYSTEM_PROP(LEFT_VIEW, 0);
MAKE_SYSTEM_PROP(RIGHT_VIEW, 1);

MAKE_SYSTEM_PROP(MENU_PANNING_NONE, MMOpenDrawerGestureModeNone);
MAKE_SYSTEM_PROP(MENU_PANNING_ALL_VIEWS, MMOpenDrawerGestureModeAll);
MAKE_SYSTEM_PROP(MENU_PANNING_CENTER_VIEW, MMOpenDrawerGestureModePanningCenterView);
MAKE_SYSTEM_PROP(MENU_PANNING_BORDERS, MMOpenDrawerGestureModeBezelPanningCenterView);
MAKE_SYSTEM_PROP(MENU_PANNING_NAVBAR, MMOpenDrawerGestureModePanningNavigationBar);


MAKE_SYSTEM_PROP(ANIMATION_NONE, 0);
MAKE_SYSTEM_PROP(ANIMATION_ZOOM, 1);
MAKE_SYSTEM_PROP(ANIMATION_SCALE, 2);
MAKE_SYSTEM_PROP(ANIMATION_SLIDE, 3);


MAKE_SYSTEM_PROP(PROPERTY_ANIMATION_LEFT, @"animationLeft");
MAKE_SYSTEM_PROP(PROPERTY_ANIMATION_RIGHT, @"animationRight");
MAKE_SYSTEM_PROP(PROPERTY_LEFT_VIEW, @"leftView");
MAKE_SYSTEM_PROP(PROPERTY_LEFT_VIEW_DISPLACEMENT, @"leftViewDisplacement");
MAKE_SYSTEM_PROP(PROPERTY_LEFT_VIEW_WIDTH, @"leftViewWidth");
MAKE_SYSTEM_PROP(PROPERTY_RIGHT_VIEW, @"rightView");
MAKE_SYSTEM_PROP(PROPERTY_RIGHT_VIEW_DISPLACEMENT, @"rightViewDisplacement");
MAKE_SYSTEM_PROP(PROPERTY_RIGHT_VIEW_WIDTH, @"rightViewWidth");
MAKE_SYSTEM_PROP(PROPERTY_PANNING_MODE, @"panningMode");
MAKE_SYSTEM_PROP(PROPERTY_CENTER_VIEW, @"centerView");
MAKE_SYSTEM_PROP(PROPERTY_FADING, @"fading");

//-(id)createSlideMenu:(id)args
//{
//    return [[AkylasSlidemenuSlideMenuProxy alloc] _initWithPageContext:[self executionContext] args:args];
//}

@end
