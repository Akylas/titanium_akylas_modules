#import "AkylasMopubViewProxy.h"
#import "MPInterstitialAdController.h"
@interface AkylasMopubInterstitialViewProxy : AkylasMopubViewProxy<MPInterstitialAdControllerDelegate>
-(MPInterstitialAdController*)_controller;

@end
