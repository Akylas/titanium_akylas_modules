/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasAdmobView.h"
#import "AkylasAdmobViewProxy.h"
#import "TiApp.h"
#import "TiUtils.h"

@implementation AkylasAdmobView{
    GADBannerView *ad;
}

#pragma mark -
#pragma mark Ad Lifecycle


-(void)clearAdView {
    if (ad != nil) {
        ad.delegate = nil;
        [ad removeFromSuperview];
        RELEASE_TO_NIL(ad);
    }
}


-(void)refreshAdForAdSize:(GADAdSize)adSize
{
    [self clearAdView];
    
    ad = [[GADBannerView alloc] initWithAdSize:adSize];
    
    // Specify the ad's "unit identifier." This is your AdMob Publisher ID.
    ad.adUnitID = [self.proxy valueForKey:@"adUnitId"];
    
    
    // Let the runtime know which UIViewController to restore after taking
    // the user wherever the ad goes and add it to the view hierarchy.
    ad.rootViewController = [[TiApp app] controller];
    
    // Initiate a generic request to load it with an ad.
    GADRequest* request = [GADRequest request];
    
    // Go through the configurable properties, populating our request with their values (if they have been provided).
    request.keywords = [self.proxy valueForKey:@"keywords"];
    request.birthday = [self.proxy valueForKey:@"birthday"];
    request.testDevices = [self.proxy valueForKey:@"testDevices"];
  
    NSString* backgroundColor = [self.proxy valueForKey:@"adBackgroundColor"];
    if (backgroundColor != nil) {
        ad.backgroundColor = [[TiUtils colorValue:backgroundColor] _color];
    }
    
    NSDictionary* location = [self.proxy valueForKey:@"location"];
    if (location != nil) {
        [request setLocationWithLatitude:[[location valueForKey:@"latitude"] floatValue]
                               longitude:[[location valueForKey:@"longitude"] floatValue]
                                accuracy:[[location valueForKey:@"accuracy"] floatValue]];
    }
    
    NSString* gender = [self.proxy valueForKey:@"gender"];
    if ([gender isEqualToString:@"male"]) {
        request.gender = kGADGenderMale;
    } else if ([gender isEqualToString:@"female"]) {
        request.gender = kGADGenderFemale;
    } else {
        request.gender = kGADGenderUnknown;
    }
    
    [self addSubview:ad];
    ad.delegate = self;
    [ad loadRequest:request];
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [ad setFrame:bounds];
    [super frameSizeChanged:frame bounds:bounds];
}

-(void)dealloc
{
    [self clearAdView];
    [super dealloc];
}

#pragma mark -
#pragma mark Ad Delegate

- (void)adViewDidReceiveAd:(GADBannerView *)view
{
    [self.proxy fireEvent:@"load"];
}

- (void)adView:(GADBannerView *)view didFailToReceiveAdWithError:(GADRequestError *)error
{
    [self.proxy fireEvent:@"error"];
}

- (void)adViewWillPresentScreen:(GADBannerView *)adView
{
    [self.proxy fireEvent:@"open"];
}

- (void)adViewWillDismissScreen:(GADBannerView *)adView
{
//    [self.proxy fireEvent:@"willDismissScreen"];
}

- (void)adViewDidDismissScreen:(GADBannerView *)adView
{
    [self.proxy fireEvent:@"close"];
}

- (void)adViewWillLeaveApplication:(GADBannerView *)adView
{
    [self.proxy fireEvent:@"leftapp"];
}



@end
