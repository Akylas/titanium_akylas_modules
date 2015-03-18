#import "AkylasMopubView.h"
#import "AkylasMopubModule.h"
#import "AkylasMopubBannerViewProxy.h"
#import "Webcolor.h"
#import "TiApp.h"

@implementation AkylasMopubView
{
    MPAdView* _adView;
    BOOL _loaded;
}
-(void) cleanup
{
    RELEASE_TO_NIL(_spinner)
    RELEASE_TO_NIL(_adView)
}

-(void)dealloc
{
    [self cleanup];
	[super dealloc];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [TiUtils setView:[self adView] positionRect:bounds];
    [super frameSizeChanged:frame bounds:bounds];
    if (!_loaded) {
        [self loadAd];
    }
}


- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];
    if (hitView == [self adView])
        return self;
    return hitView;
}

-(void)loadAd
{
    _loaded = YES;
    [_adView setHidden:NO];
    [[self spinner] startAnimating];
    [[self ad] loadAd];
}

-(id)ad
{
    return [self adView];
}

-(UIView*)adView
{
    if (_adView == nil) {
        _loaded = NO;
        CGSize size = MOPUB_BANNER_SIZE;
        NSValue* value =[self.proxy valueForKey:PROP_ADSIZE];
        if (value) {
            size = [value CGSizeValue];
        }
        NSString* adId =[TiUtils stringValue:[self.proxy valueForKey:PROP_ADUNITID]];
        _adView = [[MPAdView alloc] initWithAdUnitId:adId size:size];
		_adView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
		_adView.backgroundColor = [UIColor greenColor];
        ((MPAdView*)_adView).delegate = self;
        [self addSubview:_adView];
//        [self loadAd];
    }
    return _adView;
}

-(UIActivityIndicatorView*)spinner
{
    if (_spinner == nil)
    {
        TiColor *bgcolor = [TiUtils colorValue:[self.proxy valueForKey:@"backgroundColor"]];
        UIActivityIndicatorViewStyle style = UIActivityIndicatorViewStyleGray;
        if (bgcolor!=nil)
        {
            // check to see if the background is a dark color and if so, we want to
            // show the white indicator instead
            if ([Webcolor isDarkColor:[bgcolor _color]])
            {
                style = UIActivityIndicatorViewStyleWhite;
            }
        }
        _spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:style];
        _spinner.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin;
        [_spinner sizeToFit];
//        [_spinner setHidesWhenStopped:NO];
        
        _spinner.center = [self center];
        [self addSubview:_spinner];
    }
    return _spinner;
}

-(void)setBackgroundColor_:(id)color
{
    [super setBackgroundColor_:color];
    if (_spinner) {
        TiColor *bgcolor = [TiUtils colorValue:color];
        UIActivityIndicatorViewStyle style = UIActivityIndicatorViewStyleGray;
        if (bgcolor!=nil)
        {
            // check to see if the background is a dark color and if so, we want to
            // show the white indicator instead
            if ([Webcolor isDarkColor:[bgcolor _color]])
            {
                style = UIActivityIndicatorViewStyleWhite;
            }
        }
        if (style != _spinner.activityIndicatorViewStyle ) {
            [_spinner setActivityIndicatorViewStyle:style];
            [_spinner sizeToFit];
        }
    }
    
}

-(void)setAdUnitId_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSString)
    [[self ad] setAdUnitId: [TiUtils stringValue:value]];
    if (_loaded) [[self ad] refreshAd];
}

-(void)setKeywords_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSString)
    [[self ad] setKeywords: [TiUtils stringValue:value]];
    if (_loaded) [[self ad] refreshAd];
}

-(void)setAutorefresh_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSString)
    BOOL active = [TiUtils boolValue:value def:false];
    [[self ad] setIgnoresAutorefresh:active];
    if(active) {
        [[self ad] stopAutomaticallyRefreshingContents];
    }
    else {
        [[self ad] startAutomaticallyRefreshingContents];
    }
}

-(void)setLocation_:(id)value
{
    ENSURE_SINGLE_ARG(value, NSDictionary)
    CLLocationDegrees lat = [TiUtils floatValue:@"latitude" properties:value def:-1];
    CLLocationDegrees lon = [TiUtils floatValue:@"longitude" properties:value def:-1];
    if (lat == -1 || lon == -1) {
        NSLog(@"[ERROR]: wrong location coordinates");
    }
    else {
        [[self ad] setLocation: [[[CLLocation alloc] initWithLatitude:lat longitude:lon] autorelease]];
    }
}

-(NSDictionary*)adContentViewSize
{
    return [ TiUtils sizeToDictionary:[[self ad] adContentViewSize]];
}

-(CGSize)contentSizeForSize:(CGSize)size
{
    return [[self adView] sizeThatFits:size];
}

-(void)fireAdEvent:(NSString *)event
{
    if ([self.proxy _hasListeners:event])
    {
        [self.proxy fireEvent:event withObject:nil propagate:NO checkForListener:NO];
    }
}

- (void)adViewDidLoadAd:(MPAdView *)view {
    [_spinner stopAnimating];
    
    [self fireAdEvent:@"load"];
}

- (void)adViewDidFailToLoadAd:(MPAdView *)view {
    [_spinner stopAnimating];
    [view setHidden:YES];
    [self fireAdEvent:@"fail"];
}

/** @name Detecting When a User Interacts With the Ad View */
- (void)willPresentModalViewForAd:(MPAdView *)view {
    [self fireAdEvent:@"showmodal"];
}

/**
 * Sent when an ad view has dismissed its modal content, returning control to your application.
 */
- (void)didDismissModalViewForAd:(MPAdView *)view {
    [self fireAdEvent:@"hidemodal"];
}

/**
 * Sent when a user is about to leave your application as a result of tapping
 * on an ad.
 */
- (void)willLeaveApplicationFromAd:(MPAdView *)view {
    [self fireAdEvent:@"external"];
}

- (UIViewController *)viewControllerForPresentingModalView {
    return [[TiApp app] controller];
}

@end
