//
//  AkylasAdmobInterstitialProxy.m
//  akylas.admob
//
//  Created by Martin Guillon on 19/12/2014.
//
//

#import "AkylasAdmobInterstitialProxy.h"
#import "TiApp.h"

@implementation AkylasAdmobInterstitialProxy
{
    GADInterstitial* ad;
    BOOL showOnLoad;
}


-(void)_configure
{
    showOnLoad = NO;
    [self setValue:@(showOnLoad) forKey:@"showOnLoad"];
    [super _configure];
}


-(void)cleanAd {
    if (ad != nil) {
        ad.delegate = nil;
        RELEASE_TO_NIL(ad);
    }
}

-(void)refreshAd
{
    [self cleanAd];
    
    ad = [[GADInterstitial alloc] init];
    
    // Specify the ad's "unit identifier." This is your AdMob Publisher ID.
    ad.adUnitID = [self valueForKey:@"adUnitId"];
    
    // Initiate a generic request to load it with an ad.
    GADRequest* request = [GADRequest request];
    
    // Go through the configurable properties, populating our request with their values (if they have been provided).
    request.keywords = [self valueForKey:@"keywords"];
    request.birthday = [self valueForKey:@"birthday"];
    request.testDevices = [self valueForKey:@"testDevices"];
    
    NSDictionary* location = [self valueForKey:@"location"];
    if (location != nil) {
        [request setLocationWithLatitude:[[location valueForKey:@"latitude"] floatValue]
                               longitude:[[location valueForKey:@"longitude"] floatValue]
                                accuracy:[[location valueForKey:@"accuracy"] floatValue]];
    }
    
    NSString* gender = [self valueForKey:@"gender"];
    if ([gender isEqualToString:@"male"]) {
        request.gender = kGADGenderMale;
    } else if ([gender isEqualToString:@"female"]) {
        request.gender = kGADGenderFemale;
    } else {
        request.gender = kGADGenderUnknown;
    }
    
    ad.delegate = self;
    [ad loadRequest:request];
}

-(void)setShowOnLoad:(id)args
{
    showOnLoad = [TiUtils boolValue:args def:NO];
}

-(void)load:(id)args
{
    if(ad) {
        return;
    }
    ENSURE_UI_THREAD_1_ARG(args)
    [self refreshAd];
}

-(void)show:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args)
    if ([ad isReady]) {
        [ad presentFromRootViewController:[[TiApp app] controller]];
    }
}

-(id)loaded
{
    return @(ad && [ad isReady]);
}

/// Called when an interstitial ad request succeeded. Show it at the next transition point in your
/// application such as when transitioning between view controllers.
- (void)interstitialDidReceiveAd:(GADInterstitial *)ad {
    [self fireEvent:@"load"];
    if (showOnLoad) {
        [self show:nil];
    }
}


/// Called when an interstitial ad request completed without an interstitial to
/// show. This is common since interstitials are shown sparingly to users.
- (void)interstitial:(GADInterstitial *)ad didFailToReceiveAdWithError:(GADRequestError *)error {
    [self fireEvent:@"error"];
}

/// Called just before presenting an interstitial. After this method finishes the interstitial will
/// animate onto the screen. Use this opportunity to stop animations and save the state of your
/// application in case the user leaves while the interstitial is on screen (e.g. to visit the App
/// Store from a link on the interstitial).
- (void)interstitialWillPresentScreen:(GADInterstitial *)ad {
    [self fireEvent:@"open"];
}


/// Called before the interstitial is to be animated off the screen.
- (void)interstitialWillDismissScreen:(GADInterstitial *)ad {
//    [self fireEvent:@"leftapp"];
}


/// Called just after dismissing an interstitial and it has animated off the screen.
- (void)interstitialDidDismissScreen:(GADInterstitial *)ad {
    [self fireEvent:@"close"];
    [self cleanAd];
}

/// Called just before the application will background or terminate because the user clicked on an
/// ad that will launch another application (such as the App Store). The normal
/// UIApplicationDelegate methods, like applicationDidEnterBackground:, will be called immediately
/// before this.
- (void)interstitialWillLeaveApplication:(GADInterstitial *)ad {
    [self fireEvent:@"leftapp"];
}

@end
