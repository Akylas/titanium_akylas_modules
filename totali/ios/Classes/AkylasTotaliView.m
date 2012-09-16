#import "AkylasTotaliView.h"
#import "TiApp.h"
#import "AkylasTotaliViewProxy.h"
#import "tiDFusionMobile.h"

@implementation AkylasTotaliView


-(UIView*)totaliview
{
	if (totaliview==nil)
	{
        NSLog(@"AkylasTotali: totaliview");
        _scenario = @"";
        _registeredCallbacks = [[NSMutableArray alloc] init];
        totaliview = [[UIView alloc] init];
//        _controller = [[AkylasTotaliViewController alloc] initWithDelegate:self];
//        totaliview = _controller.view;
        [self addSubview:totaliview];
        mPlayer = [[tiComponent alloc] init];
        
        // set correct renderer
        [mPlayer setRendererType:( [tiComponent TI_RENDERER_GLES1] ) ];
	}
	return totaliview;
}

-(void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
	RELEASE_TO_NIL(_registeredCallbacks);
    //	if (pageToken!=nil)
    //	{
    //		[[self host] unregisterContext:(id<TiEvaluator>)self forToken:pageToken];
    //		RELEASE_TO_NIL(pageToken);
    //	}
    if (mPlayer)
	{
		[mPlayer terminate];
        RELEASE_TO_NIL(mPlayer);
	}
//	if (listeners!=nil)
//	{
//		RELEASE_TO_NIL(listeners);
//	}
	RELEASE_TO_NIL(totaliview);
	[super dealloc];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [TiUtils setView:[self totaliview] positionRect:bounds];
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
    if (hitView == [self totaliview])
        return self;
    return hitView;
}

- (void)fireEvent:(id)listener withObject:(id)obj
{
	TiThreadPerformOnMainThread(^{
		[[self proxy] fireEvent:listener withObject:obj];
	}, NO);
}

-(CGFloat)autoWidthForWidth:(CGFloat)value
{
	return [[self totaliview] sizeThatFits:CGSizeMake(value, 0)].width;
}

-(CGFloat)autoHeightForWidth:(CGFloat)value
{
	return [[self totaliview] sizeThatFits:CGSizeMake(value, 0)].height;
}

//-(void)setBackgroundColor_:(id)value
//{
//	if (value!=nil)
//	{
//		TiColor *color = [TiUtils colorValue:value];
//		[[self totaliview] setBackgroundColor:[color _color]];
//	}
//}

- (void)viewWillAppear:(BOOL)animated
{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller viewWillAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller viewWillAppear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller viewWillAppear:animated];
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module willAnimateRotationToInterfaceOrientation");
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
//    NSLog(@"module willRotateToInterfaceOrientation");
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
//    NSLog(@"module didRotateFromInterfaceOrientation");
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller didRotateFromInterfaceOrientation:fromInterfaceOrientation];
}

- (void) setScenario:(id)value
{
    ENSURE_TYPE_OR_NIL(value, NSString);
    NSLog(@"AkylasTotali: setScenario: %@", value);
    _scenario = (NSString*)value;
}

- (void) start
{
        NSLog(@"AkylasTotali: Start");
        if (mPlayer) //ensure _controller is initiated
        {
            [mPlayer initialize:[self totaliview]];
           //        [mPlayer registerCommunicationCallback:@"setTrackingInfo" callbackObject:self callbackMethod:@"TI_CommIntTrackingInfo"];
            //        //register an observer of the TI_CommIntTrackingInfo NSNotification to update tracking information
            //        [[NSNotificationCenter defaultCenter]	addObserver:self
            //                                                 selector:@selector(setTrackingInfo:)
            //                                                     name:@"TI_CommIntTrackingInfo"
            //                                                   object:nil];
            //        NSString* toLoad = @"Scenario/project.dpd";
            [self loadScenario:_scenario];
        }
}

-(void)registerCallback:(NSString*)value
{
    ENSURE_TYPE_OR_NIL(value, NSString);
    NSLog(@"AkylasTotali: registerCallback: %@", value);
    NSString* callback = (NSString*)value;
    NSString* TIcallback = [NSString stringWithFormat:@"TI_CommInt_%@", callback];
    if ([_registeredCallbacks indexOfObject:callback] == NSNotFound)
    {
        [mPlayer registerCommunicationCallback:callback callbackObject:self callbackMethod:TIcallback];
        //register an observer of the TI_CommIntTrackingInfo NSNotification to update tracking information
        [[NSNotificationCenter defaultCenter]	addObserver:self
                                                 selector:@selector(onComNotifation:)
                                                     name:TIcallback
                                                   object:nil];
        [_registeredCallbacks addObject:callback];
    }
}

-(void)unregisterCallback:(NSString*)value
{
    NSLog(@"AkylasTotali: unregisterCallback: %@", value);
    NSString* callback = (NSString*)value;
    NSString* TIcallback = [NSString stringWithFormat:@"TI_CommInt_%@", callback];
    if ([_registeredCallbacks indexOfObject:callback] != NSNotFound)
    {
        //            [mPlayer registerCommunicationCallback:callback callbackObject:self callbackMethod:TIcallback];
        [[NSNotificationCenter defaultCenter]	removeObserver:self
                                                        name:TIcallback
                                                      object:nil];
        [_registeredCallbacks removeObject:callback];
    }
}

- (void)onComNotifation:(NSNotification *)notification
{
    NSString* TIcallback = notification.name;
    NSString* callback = [TIcallback stringByReplacingOccurrencesOfString:@"TI_CommInt_" withString:@""];
    if ([_registeredCallbacks indexOfObject:callback] == NSNotFound)
    {
        NSDictionary* l_userInfo = [notification userInfo];
        [self fireEvent:callback withObject:l_userInfo];
    }
}


//-----------------------------------
// When a scenario is choosen, this callback will launch it
- (void)loadScenario:(NSString *)toload
{
    if (mPlayer == nil) return;
    NSLog(@"AkylasTotali: loadScenario: %@", toload);
    BOOL isLoaded = [mPlayer loadScenario:toload];
    if (isLoaded) {
        
        if ([mPlayer playScenario] == NO)
        {
            NSLog(@"Failed to play : %@",toload);
        }
    }
    else {
        NSLog(@"Failed to load : %@",toload);
    }
}


- (void) stop
{
        NSLog(@"AkylasTotali: stop");
        if (mPlayer && ![mPlayer isScenarioPaused]) {
            [mPlayer pauseScenario];
        }
}

-(NSString*) scenario
{
    return _scenario;
}


//- (BOOL) torchOn
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        return _controller.torchIsOn;
//}
//
//- (BOOL) onlyOneDimension
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        return _controller.oneDMode;
//}
//
//- (BOOL) centeredCropRect
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        return _controller.centeredCropRect;
//}
//
//- (int) cameraPosition
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        return _controller.cameraPosition;
//}
//
//
//- (CGRect) cropRect
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        return _controller.cropRect;
//}
//
//- (void) flush
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller flush];
//}
//
//- (void) swapCamera
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller swapCamera];
//}
//
//- (void) focus:(CGPoint)point
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller focusAtPoint:point];
//}
//
//- (void) autoFocus:(CGPoint)point
//{
//    if ([self totaliview]) //ensure _controller is initiated
//        [_controller autoFocusAtPoint:point];
//}

@end
