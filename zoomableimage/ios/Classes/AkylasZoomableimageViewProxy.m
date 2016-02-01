/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasZoomableimageViewProxy.h"
#import "AkylasZoomableimageView.h"
#import "ImageLoader.h"

#import "TiUtils.h"


@implementation AkylasZoomableimageViewProxy {

}
@synthesize imageURL;

-(NSArray *)keySequence
{
    static NSArray *keySequence = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        keySequence = [[[super keySequence] arrayByAddingObjectsFromArray:@[@"scaleType",@"localLoadSync", @"image"]] retain];;
    });
    return keySequence;
}

-(void)_initWithProperties:(NSDictionary *)properties
{
//    [self initializeProperty:@"showHorizontalScrollIndicator" defaultValue:@(NO)];
//    [self initializeProperty:@"showHVerticalScrollIndicator" defaultValue:@(NO)];
//    [self initializeProperty:@"scrollingEnabled" defaultValue:@(YES)];
    [super _initWithProperties:properties];
}

-(NSString*)apiName
{
    return @"Akylas.ZoomableImageView";
}


-(id)toBlob:(id)args
{
    id imageValue = [self valueForKey:@"image"];
    
    if ([imageValue isKindOfClass:[TiBlob class]])
    {
        //We already have it right here already!
        return imageValue;
    }
    
    if ([imageValue isKindOfClass:[TiFile class]])
    {
        return [(TiFile *)imageValue toBlob:nil];
    }
    
    if (imageValue!=nil)
    {
        AkylasZoomableimageView* imageView = (AkylasZoomableimageView*)[self view];
        UIImage* imageToUse = [imageView getImage];
        if (!imageToUse) {
            NSURL *url_ = [TiUtils toURL:[TiUtils stringValue:imageValue] proxy:self];
            id theimage = [[ImageLoader sharedLoader] loadImmediateImage:url_];
            if (theimage == nil)
            {
                theimage = [[ImageLoader sharedLoader] loadRemote:url_ withOptions:[self valueForUndefinedKey:@"httpOptions"]];
            }
            
            // we're on the non-UI thread, we need to block to load
            UIImage *imageToUse = [imageView prepareImage:[imageView convertToUIImage:theimage]];
        }
        
        return [[[TiBlob alloc] initWithImage:imageToUse] autorelease];
    }
    return nil;
}

- (void)prepareForReuse
{
    [(AkylasZoomableimageView *)view setReusing:YES];
    [super prepareForReuse];
}


- (void)configurationSet:(BOOL)recursive
{
    [super configurationSet:recursive];
    [(AkylasZoomableimageView *)view setReusing:NO];
}

#pragma mark Handling ImageLoader

-(void)setImage:(id)newImage
{
    id image = newImage;
    if ([image isEqual:@""])
    {
        image = nil;
    }
    [self cancelPendingImageLoads];
    [self replaceValue:image forKey:@"image" notification:YES];
}

-(void)startImageLoad:(NSURL *)url;
{
    [self cancelPendingImageLoads]; //Just in case we have a crusty old urlRequest.
    NSDictionary* info = nil;
    NSNumber* hires = [self valueForKey:@"hires"];
    if (hires) {
        info = [NSDictionary dictionaryWithObject:hires forKey:@"hires"];
    }
    urlRequest = [[[ImageLoader sharedLoader] loadImage:url delegate:self options:[self valueForUndefinedKey:@"httpOptions"] userInfo:info] retain];
}

-(void)cancelPendingImageLoads
{
    // cancel a pending request if we have one pending
    if (urlRequest!=nil)
    {
        [urlRequest cancel];
        RELEASE_TO_NIL(urlRequest);
    }
}



-(void)imageLoadSuccess:(ImageLoaderRequest*)request image:(id)image
{
    if (request != urlRequest)
    {
        return;
    }
    
    if (view != nil)
    {
        [(AkylasZoomableimageView *)[self view] imageLoadSuccess:request image:image];
    }
    [self setImageURL:[urlRequest url]];
    RELEASE_TO_NIL(urlRequest);
}

-(void)imageLoadFailed:(ImageLoaderRequest*)request error:(NSError*)error
{
    if (request == urlRequest)
    {
        if ([self _hasListeners:@"error" checkParent:NO])
        {
            [self fireEvent:@"error" withObject:[NSDictionary dictionaryWithObject:[request url] forKey:@"image"] propagate:NO reportSuccess:YES errorCode:[error code] message:[TiUtils messageFromError:error] checkForListener:NO];
        }
        RELEASE_TO_NIL(urlRequest);
    }
}


@end
