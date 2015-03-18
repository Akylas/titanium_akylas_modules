#import "TiUIView.h"
#import "MPAdView.h"

@interface AkylasMopubView : TiUIView<MPAdViewDelegate>
{
    UIActivityIndicatorView *_spinner;
}
-(UIView*)adView;
-(id)ad;
-(void)loadAd;
-(void)fireAdEvent:(NSString *)event;
@end
