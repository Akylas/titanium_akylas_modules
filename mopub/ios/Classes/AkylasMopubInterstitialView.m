#import "AkylasMopubInterstitialView.h"
#import "AkylasMopubInterstitialViewProxy.h"
#import "MPInterstitialAdController.h"
#import "AkylasMopubModule.h"
#import "Webcolor.h"
#import "TiApp.h"

@implementation AkylasMopubInterstitialView
{
}
-(void) cleanup
{
}

-(void)dealloc
{
	[super dealloc];
}

-(id)ad
{
    return [(AkylasMopubInterstitialViewProxy*)self.proxy _controller];
}


@end
