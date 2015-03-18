#import "AkylasMopubInterstitialViewProxy.h"
#import "AkylasMopubInterstitialView.h"
#import "MPInterstitialAdController.h"
#import "AkylasMopubModule.h"

@implementation AkylasMopubInterstitialViewProxy
{
    MPInterstitialAdController* _controller;
    BOOL _needsShowing;
}

-(id)init
{
	if ((self = [super init]))
	{
        [self setDefaultReadyToCreateView:YES];
        _needsShowing = NO;
	}
	return self;
}

-(void)_destroy
{
    [self cleanup];
	[super _destroy];
}
-(void) cleanup
{
    [MPInterstitialAdController removeSharedInterstitialAdController:_controller];
    RELEASE_TO_NIL(_controller)
}


-(MPInterstitialAdController*)_controller
{
    if (_controller == nil) {
        NSString* adId =[TiUtils stringValue:[self valueForKey:PROP_ADUNITID]];
        _controller = [MPInterstitialAdController interstitialAdControllerForAdUnitId:adId];
        _controller.delegate = self;
    }
    return _controller;

}

-(id)ad
{
    return [self _controller];
}


-(void)open:(id)args
{
    [self rememberSelf];
    if ([self _controller].ready) {
        [_controller showFromViewController:[(AkylasMopubInterstitialView*)[self view] viewControllerForPresentingModalView]];
    }
    else {
        _needsShowing = YES;
        [_controller loadAd];
    }
}

-(void)close:(id)args
{
    [_controller dismissViewControllerAnimated:YES completion:nil];
    [self forgetSelf];
}


//-(void)refreshAd:(id)unused
//{
//    ENSURE_UI_THREAD_1_ARG(unused)
//    [[self ad] refreshAd];
//}
//
//-(void)loadAd:(id)unused
//{
//    ENSURE_UI_THREAD_1_ARG(unused)
//    [(AkylasMopubView*)view loadAd];
//}

#pragma mark - TiWindowProtocol
//-(void)viewWillAppear:(BOOL)animated
//{
//    if ([self viewAttached]) {
//        [[self _controller] viewWillAppear:animated];
//    }
//    [super viewWillAppear:animated];
//}
//-(void)viewWillDisappear:(BOOL)animated
//{
//    if ([self viewAttached]) {
//        [[self _controller] viewWillDisappear:animated];
//    }
//    [super viewWillDisappear:animated];
//}
//
//-(void)viewDidAppear:(BOOL)animated
//{
//    if ([self viewAttached]) {
//        [[self _controller] viewDidAppear:animated];
//    }
//    [super viewDidAppear:animated];
//}
//-(void)viewDidDisappear:(BOOL)animated
//{
//    if ([self viewAttached]) {
//        [[self _controller] viewDidDisappear:animated];
//    }
//    [super viewDidDisappear:animated];
//    
//}
//
//-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//    if ([self viewAttached]) {
//        [[self _controller] willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
//    }
//    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}
//-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//    if ([self viewAttached]) {
//        [[self _controller] willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
//    }
//    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}
//-(void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
//{
//    if ([self viewAttached]) {
//        [[self _controller] didRotateFromInterfaceOrientation:fromInterfaceOrientation];
//    }
//    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
//}


/** @name Detecting When an Interstitial Ad is Loaded */

/**
 * Sent when an interstitial ad object successfully loads an ad.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialDidLoadAd:(MPInterstitialAdController *)interstitial {
    if (_needsShowing) {
        _needsShowing = NO;
        [interstitial showFromViewController:[(AkylasMopubInterstitialView*)[self view] viewControllerForPresentingModalView]];
    }
    [self fireEvent:@"load" propagate:NO];
}

/**
 * Sent when an interstitial ad object fails to load an ad.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialDidFailToLoadAd:(MPInterstitialAdController *)interstitial {
    [self fireEvent:@"fail" propagate:NO];
}

/** @name Detecting When an Interstitial Ad is Presented */

/**
 * Sent immediately before an interstitial ad object is presented on the screen.
 *
 * Your implementation of this method should pause any application activity that requires user
 * interaction.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialWillAppear:(MPInterstitialAdController *)interstitial {
}

/**
 * Sent after an interstitial ad object has been presented on the screen.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialDidAppear:(MPInterstitialAdController *)interstitial {
    [self fireEvent:@"appear" propagate:NO];
}

/** @name Detecting When an Interstitial Ad is Dismissed */

/**
 * Sent immediately before an interstitial ad object will be dismissed from the screen.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialWillDisappear:(MPInterstitialAdController *)interstitial {
    [self fireEvent:@"disappear" propagate:NO];
}

/**
 * Sent after an interstitial ad object has been dismissed from the screen, returning control
 * to your application.
 *
 * Your implementation of this method should resume any application activity that was paused
 * prior to the interstitial being presented on-screen.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialDidDisappear:(MPInterstitialAdController *)interstitial {
    
}

/** @name Detecting When an Interstitial Ad Expires */

/**
 * Sent when a loaded interstitial ad is no longer eligible to be displayed.
 *
 * Interstitial ads from certain networks (such as iAd) may expire their content at any time,
 * even if the content is currently on-screen. This method notifies you when the currently-
 * loaded interstitial has expired and is no longer eligible for display.
 *
 * If the ad was on-screen when it expired, you can expect that the ad will already have been
 * dismissed by the time this message is sent.
 *
 * Your implementation may include a call to `loadAd` to fetch a new ad, if desired.
 *
 * @param interstitial The interstitial ad object sending the message.
 */
- (void)interstitialDidExpire:(MPInterstitialAdController *)interstitial {
    [self fireEvent:@"expire" propagate:NO];
}


@end
