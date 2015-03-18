
#import "AkylasMopubViewController.h"
#import "AkylasMopubBannerViewProxy.h"
#import "AkylasMopubInterstitialViewProxy.h"
#import "TiBase.h"
#import "TiUtils.h"
#import "AkylasMopubModule.h"
#import "TiApp.h"

@interface AkylasMopubViewController ()
@end

@implementation AkylasMopubViewController
{
//    SASAdView *_adView;
    AkylasMopubViewProxy *_proxy;
}

//@synthesize adView = _adView;

- (id)initWithProxy:(AkylasMopubBannerViewProxy*)proxy
{
    self = [super init];
    if (self) {
        _proxy = proxy; //do not retain
    }
    
    return self;
}

- (void)cleanup {
//    RELEASE_TO_NIL(_adView)
}

- (void)dealloc {
        
    [self cleanup];
    [super dealloc];
}


//-(SASAdView *)adView {
//    
//    if (_adView == nil) {
//        BOOL useSpinner = [TiUtils boolValue:[_proxy valueForKey:@"useSpinner"] def:NO];
//        BOOL hideStatusBar = [TiUtils boolValue:[_proxy valueForKey:@"hideStatusBar"] def:NO];
//        if ([_proxy isKindOfClass:[AkylasMopubBannerViewProxy class]]) {
//            _adView = [[SASBannerView alloc] initWithFrame:CGRectZero loader:useSpinner?SASLoaderActivityIndicatorStyleWhite:SASLoaderNone hideStatusBar:hideStatusBar];
//        }
//        else if([_proxy isKindOfClass:[AkylasMopubInterstitialViewProxy class]]) {
//            
//            _adView = [[SASInterstitialView alloc] initWithFrame:CGRectZero loader:useSpinner?SASLoaderActivityIndicatorStyleWhite:SASLoaderNone hideStatusBar:hideStatusBar];
//        }
//        [_adView setDelegate:self];
//    }
//    return _adView;
//}

//- (void)loadView
//{
//    self.view = [self adView];
//}

-(void)viewDidLoad
{
    [super viewDidLoad];
}

-(void)viewDidUnload
{
    [self cleanup];
    [super viewDidUnload];
}

- (void)viewWillAppear:(BOOL)animated {
}

- (void)viewWillDisappear:(BOOL)animated {
}

- (void)viewDidAppear:(BOOL)animated {    
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}
- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) orient
                                 duration: (NSTimeInterval) duration
{
}

- (void) willAnimateRotationToInterfaceOrientation: (UIInterfaceOrientation) orient
                                          duration: (NSTimeInterval) duration
{
}

- (void) didRotateFromInterfaceOrientation: (UIInterfaceOrientation) orient
{
}

@end
