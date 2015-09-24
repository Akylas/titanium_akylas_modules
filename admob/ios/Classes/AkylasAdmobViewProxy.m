/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasAdmobViewProxy.h"
#import "AkylasAdmobView.h"

#import "TiUtils.h"

GADAdSize adSizeFromString(NSString* value)
{
    if ([value isEqual:@"banner" ])
        return kGADAdSizeBanner;
    else if ([value isEqual:@"largeBanner" ])
        return kGADAdSizeLargeBanner;
    else if ([value isEqual:@"leaderboard" ])
        return kGADAdSizeLeaderboard;
    else if ([value isEqual:@"fullBanner" ])
        return kGADAdSizeFullBanner;
    else if ([value isEqual:@"skyscraper" ])
        return kGADAdSizeSkyscraper;
    else if ([value isEqual:@"mediumRect" ])
        return kGADAdSizeMediumRectangle;
    else if ([value isEqual:@"smartBanner" ]) {
        if ([TiUtils isOrientationLandscape]) {
            return kGADAdSizeSmartBannerLandscape;
        }
        return kGADAdSizeSmartBannerPortrait;
    }
    return kGADAdSizeInvalid;
}

@implementation AkylasAdmobViewProxy {
    BOOL smartBanner;
}
@synthesize gadSize;

-(TiDimension)defaultAutoWidthBehavior:(id)unused
{
    return TiDimensionAutoFill;
}
-(TiDimension)defaultAutoHeightBehavior:(id)unused
{
    return TiDimensionAutoSize;
}

-(void)_configure
{
    [self setValue:@"smartBanner" forKey:@"adSize"];
    [super _configure];
}

-(void)refreshAd {
    if (view != nil) {
        [(AkylasAdmobView*)view refreshAdForAdSize:gadSize];
    }
    if ([self viewLayedOut]){
        [self contentsWillChange];
    }
}

-(void)viewDidInitialize
{
    [self refreshAd];
}

-(void)setAdSize:(id)arg
{
    NSString* value =[TiUtils stringValue:arg];
    gadSize = adSizeFromString(value);
    smartBanner = [value isEqualToString:@"smartBanner"];
    [self replaceValue:arg forKey:@"adSize" notification:YES];
    if ([self viewLayedOut]){
        [self refreshAd];
    }
}

//-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//    if (smartBanner) {
//        GADAdSize newSize;
//        if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
//            newSize = kGADAdSizeSmartBannerLandscape;
//        } else {
//            newSize = kGADAdSizeSmartBannerPortrait;
//        }
//        if (!GADAdSizeEqualToSize(gadSize, newSize)) {
//            gadSize = newSize;
//            [self refreshAd];
//        }
//    }
//    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}

-(CGSize)contentSizeForSize:(CGSize)size
{
    if (smartBanner) {
        GADAdSize newSize;
        if ([TiUtils isOrientationLandscape]) {
            newSize = kGADAdSizeSmartBannerLandscape;
        } else {
            newSize = kGADAdSizeSmartBannerPortrait;
        }
        if (!GADAdSizeEqualToSize(gadSize, newSize)) {
            gadSize = newSize;
            [self refreshAd];
        }
    }
    return CGSizeFromGADAdSize(gadSize);
}

@end
