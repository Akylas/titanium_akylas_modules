//
//  TDAdLoader.h
//  TritonPlayerSDK
//
//  Copyright (c) 2015 Triton Digital. All rights reserved.
//

#import <Foundation/Foundation.h>

/// Represents Triton Mobile SDk generated error codes
extern NSString *const TDErrorDomain;

/// A network error occurred while requesting the ad
extern const NSInteger TDErrorCodeInvalidRequest;

/// The width and height of the ad was not specified
extern const NSInteger TDErrorCodeUndefinedSize;

/// There's no ad to be displayed for the request
extern const NSInteger TDErrorCodeNoInventory;

/// The ad request or media url is malformed
extern const NSInteger TDErrorCodeInvalidAdURL;

/// Unable to parse the response
extern const NSInteger TDErrorCodeResponseParsingFailed;

@class TDAd;
@class TDAdRequestURLBuilder;

/** 
 * TDAdLoader loads a Triton ad from an ad request. 
 *
 * The ad returned is represented by a TDAd object and contains all the information needed to display and manage an ad. The ad returned can be 
 * presented using custom application UI or it can be passed directly to TDBannerView or TDInterstitialAd for display.
 *
 */

@interface TDAdLoader : NSObject

/// @name Creating a TDAdLoader

/**
 * Loads an ad asynchronously from a request string. The string can be built manually by following Triton Digital On-Demand advertising guide or 
 * by the help of TDAdRequestURLBuilder class (recommended).
 *
 * @param request A NSString containing the request with the targeting parameters.
 * @param completionHandler a block that will execute when the request is finished, with the ad loaded or an error object.
 */

- (void)loadAdWithStringRequest:(NSString*)request
       completionHandler:(void (^) (TDAd *loadedAd, NSError *error))completionHandler;

/**
 * Loads an ad asynchronously directly from a TDAdRequestURLBuilder.
 *
 * @param builder A TDAdRequestURLBuilder containing the request with the targeting parameters.
 * @param completionHandler a block that will execute when the request is finished, with the ad loaded or an error object.
 */

- (void)loadAdWithBuilder:(TDAdRequestURLBuilder*)builder
        completionHandler:(void (^) (TDAd *loadedAd, NSError *error))completionHandler;

@end
