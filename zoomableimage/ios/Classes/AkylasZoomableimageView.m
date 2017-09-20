/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2010-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasZoomableimageView.h"
#import "AkylasZoomableimageViewProxy.h"

#import "TiBase.h"
#import "TiProxy.h"
#import "TiBlob.h"
#import "TiFile.h"
#import "UIImage+Resize.h"
#import "UIImage+UserInfo.h"
#import "NSDictionary+Merge.h"
#import "ImageLoader.h"
#import "TiTransition.h"
#import "TiImageHelper.h"
#import "TiSVGImage.h"

@interface CustomImageView: UIImageView
@end

@interface AkylasZoomableimageView()
{
    TDUIScrollView *mainScrollView;
    UIImageView *imageView;
    
    UIImage* _currentImage;
    id _currentImageSource;
    UIViewContentMode scaleType;
    BOOL localLoadSync;
    BOOL shouldTransition;
    BOOL onlyTransitionIfRemote;
    BOOL _reusing;
    NSDictionary* _filterOptions;
    TiSVGImage* _svg;
    NSURL *_defaultImageUrl;
    BOOL _preventDefaultImage;
    BOOL _needsSetImage;
    BOOL _minScaleDefined;
}
@property(nonatomic,retain) NSDictionary *transition;
@end

@implementation CustomImageView
- (void)setFrame:(CGRect)newSize {
    [super setFrame:newSize];
}
- (void)setBounds:(CGRect)newSize {
    [super setBounds:newSize];
}
@end
@implementation AkylasZoomableimageView

#pragma mark Internal

DEFINE_EXCEPTIONS

#ifdef TI_USE_AUTOLAYOUT
-(void)initializeTiLayoutView
{
    [super initializeTiLayoutView];
    [self setDefaultHeight:TiDimensionAutoSize];
    [self setDefaultWidth:TiDimensionAutoSize];
}
#endif

-(id)init
{
    if (self = [super init]) {
        localLoadSync = NO;
        _needsSetImage= NO;
        scaleType = UIViewContentModeScaleAspectFit;
        self.transition = nil;
        _reusing = NO;
        _preventDefaultImage = NO;
        _filterOptions = nil;
        onlyTransitionIfRemote = NO;
        _minScaleDefined = NO;
        _filterOptions = nil;
    }
    return self;
}

-(void)dealloc
{
    RELEASE_TO_NIL(imageView);
    RELEASE_TO_NIL(mainScrollView);
    RELEASE_TO_NIL(_filterOptions);
    RELEASE_TO_NIL(_svg);
    [super dealloc];
}

-(UIImageView *)imageView
{
    if (imageView==nil)
    {
        imageView = [[CustomImageView alloc] initWithFrame:[self bounds]];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        imageView.autoresizingMask = UIViewAutoresizingNone;
        imageView.backgroundColor = [UIColor clearColor];
    }
    return imageView;
}

-(TDUIScrollView *)scrollView
{
    if(mainScrollView == nil)
    {

        mainScrollView = [[TDUIScrollView alloc] initWithFrame:[self bounds]];
        [mainScrollView addSubview:[self imageView]];
        [mainScrollView setAutoresizingMask:UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight];
        
        [mainScrollView setBackgroundColor:[UIColor clearColor]];
//        [mainScrollView setShowsHorizontalScrollIndicator:NO];
//        [mainScrollView setShowsVerticalScrollIndicator:NO];
        [mainScrollView setDelegate:self];
        [mainScrollView setTouchDelegate:self];
        [self addSubview:mainScrollView];
        
        CGFloat zoomScale = mainScrollView.zoomScale;
        CGFloat minimumZoomScale = mainScrollView.minimumZoomScale;

        [self doubleTapRecognizer];
    }
    return mainScrollView;
}


-(void)updateImageSize
{
    CGSize size = imageView.image.size;
    imageView.frame = CGRectMake(0, 0, size.width, size.height);
    CGFloat scale = mainScrollView.zoomScale;
    size.width *= scale;
    size.height *= scale;
    mainScrollView.contentSize = size;
    [self setupScales];
    mainScrollView.zoomScale = mainScrollView.minimumZoomScale;
}


-(void)setupScales {
    
    if (mainScrollView.contentSize.width == 0 ||
        mainScrollView.contentSize.height == 0) {
        return;
    }
    // Tell the scroll view the size of the contents
    // Set up the minimum & maximum zoom scales
    CGFloat currentScale = mainScrollView.zoomScale;
    CGRect scrollViewFrame = mainScrollView.frame;
    if (scrollViewFrame.size.width == 0 ||
        scrollViewFrame.size.height == 0) {
        return;
    }
    CGFloat scaleWidth = scrollViewFrame.size.width / mainScrollView.contentSize.width * currentScale;
    CGFloat scaleHeight = scrollViewFrame.size.height / mainScrollView.contentSize.height * currentScale;
    CGFloat minScale = MIN(scaleWidth, scaleHeight);
    mainScrollView.minimumZoomScale = minScale;
    if (currentScale < minScale) {
        mainScrollView.zoomScale = minScale;
    }
    
    [self scrollViewDidZoom:mainScrollView];
    [self centerScrollViewContents];
}



- (void)centerScrollViewContents {
    // This method centers the scroll view contents also used on did zoom
    CGSize boundsSize = mainScrollView.bounds.size;
    CGRect contentsFrame = imageView.frame;
    if (contentsFrame.size.width < boundsSize.width) {
        contentsFrame.origin.x = (boundsSize.width - contentsFrame.size.width) / 2.0f;
    } else {
        contentsFrame.origin.x = 0.0f;
    }
    if (contentsFrame.size.height < boundsSize.height) {
        contentsFrame.origin.y = (boundsSize.height - contentsFrame.size.height) / 2.0f;
    } else {
        contentsFrame.origin.y = 0.0f;
    }
    imageView.frame = contentsFrame;
}


-(UIView*)viewForHitTest
{
    return imageView;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    BOOL wasOnMin = mainScrollView.minimumZoomScale == mainScrollView.zoomScale;
    [TiUtils setView:mainScrollView positionRect:bounds];
    if (_svg != nil) {
        [self imageView].image = [_svg imageForSize:bounds.size];
    }
    [super frameSizeChanged:frame bounds:bounds];
    [self setupScales];
    if (wasOnMin) {
        mainScrollView.zoomScale = mainScrollView.minimumZoomScale;
    }
}

//- (void)layoutSubviews {
//    [super layoutSubviews];
//    
//    // Center the image as it becomes smaller than the size of the screen
//    CGSize boundsSize = self.bounds.size;
//    CGRect frameToCenter = mainImageView.frame;
//    
//    // Horizontally
//    if (frameToCenter.size.width < boundsSize.width) {
//        frameToCenter.origin.x = floorf((boundsSize.width - frameToCenter.size.width) / 2.0);
//    } else {
//        frameToCenter.origin.x = 0;
//    }
//    
//    // Vertically
//    if (frameToCenter.size.height < boundsSize.height) {
//        frameToCenter.origin.y = floorf((boundsSize.height - frameToCenter.size.height) / 2.0);
//    } else {
//        frameToCenter.origin.y = 0;
//    }
//    
//    // Center
//    if (!CGRectEqualToRect(mainImageView.frame, frameToCenter)) {
//        mainImageView.frame = frameToCenter;
//    }
//}

#pragma mark Keyboard delegate stuff

-(void)keyboardDidShowAtHeight:(CGFloat)keyboardTop
{
}

-(void)scrollToShowView:(UIView *)firstResponderView withKeyboardHeight:(CGFloat)keyboardTop
{

}


#pragma mark -

- (void)zoomToPoint:(CGPoint)touchPoint withScale: (CGFloat)scale animated: (BOOL)animated
{
    touchPoint.x -= imageView.frame.origin.x;
    touchPoint.y -= imageView.frame.origin.y;
    [super zoomToPoint:touchPoint withScale:scale animated:animated];
}
#pragma mark scrollView delegate stuff

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return [self imageView];
}

- (void)scrollViewDidZoom:(UIScrollView *)scrollView_
{
    [self centerScrollViewContents];
    [super scrollViewDidZoom:scrollView_];
}

-(id)zoomScale_ {
    return @(mainScrollView.zoomScale);
}


-(id)center_ {
    CGPoint offset = mainScrollView.contentOffset;
    CGSize size = self.frame.size;
    return [TiUtils sizeToDictionary:CGSizeMake(offset.x + size.width, offset.y + size.height)];
}

-(id)minZoomScale_ {
    return @(mainScrollView.minimumZoomScale);
}

-(id)maxZoomScale_ {
    return @(mainScrollView.maximumZoomScale);
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    [super scrollViewDidScroll:scrollView];
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    [super scrollViewWillBeginDragging:scrollView];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate
{
    [super scrollViewDidEndDragging:scrollView willDecelerate:decelerate];
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView
{
    [super scrollViewDidEndDecelerating:scrollView];
}

- (BOOL)scrollViewShouldScrollToTop:(UIScrollView *)scrollView
{
    return [super scrollViewShouldScrollToTop:scrollView];
}

- (void)scrollViewDidScrollToTop:(UIScrollView *)scrollView
{
    return [super scrollViewDidScrollToTop:scrollView];
}


#pragma mark -

#pragma mark - ScrollView gesture methods

-(void)recognizedDoubleTap:(UITapGestureRecognizer*)recognizer
{
    // Get the location within the image view where we tapped
    CGPoint pointInView = [recognizer locationInView:imageView];
    // Get a zoom scale that's zoomed in slightly, capped at the maximum zoom scale specified by the scroll view
    CGFloat newZoomScale = mainScrollView.zoomScale * 1.5f;
    newZoomScale = MIN(newZoomScale, mainScrollView.maximumZoomScale);
    
    // Figure out the rect we want to zoom to, then zoom to it
    CGSize scrollViewSize = mainScrollView.bounds.size;
    CGFloat w = scrollViewSize.width / newZoomScale;
    CGFloat h = scrollViewSize.height / newZoomScale;
    CGFloat x = pointInView.x - (w / 2.0f);
    CGFloat y = pointInView.y - (h / 2.0f);
    CGRect rectToZoomTo = CGRectMake(x, y, w, h);
    [mainScrollView zoomToRect:rectToZoomTo animated:YES];
    [super recognizedDoubleTap:recognizer];
}

- (void)scrollViewTwoFingerTapped:(UITapGestureRecognizer*)recognizer {
    
    // Zoom out slightly, capping at the minimum zoom scale specified by the scroll view
    CGFloat newZoomScale = mainScrollView.zoomScale / 1.5f;
    newZoomScale = MAX(newZoomScale, mainScrollView.minimumZoomScale);
    [mainScrollView setZoomScale:newZoomScale animated:YES];
}


- (id)accessibilityElement
{
    return [self scrollView];
}


-(UIImage*)rotatedImage:(UIImage*)originalImage
{
    //If autorotate is set to false and the image orientation is not UIImageOrientationUp create new image
    if (![TiUtils boolValue:[[self proxy] valueForUndefinedKey:@"autorotate"] def:YES] && (originalImage.imageOrientation != UIImageOrientationUp)) {
        UIImage* theImage = [UIImage imageWithCGImage:[originalImage CGImage] scale:[originalImage scale] orientation:UIImageOrientationUp];
        return theImage;
    }
    else {
        return originalImage;
    }
}

-(void)fireLoadEventWithState:(NSString *)stateString
{
    AkylasZoomableimageViewProxy* ourProxy = (AkylasZoomableimageViewProxy*)self.proxy;
    [ourProxy propagateLoadEvent:stateString];
}

- (id) cloneView:(id)source {
    NSData *archivedViewData = [NSKeyedArchiver archivedDataWithRootObject: source];
    id clone = [NSKeyedUnarchiver unarchiveObjectWithData:archivedViewData];
    return clone;
}

-(void) transitionToImage:(UIImage*)image
{
    ENSURE_UI_THREAD(transitionToImage,image);
    if (self.proxy==nil)
    {
        // this can happen after receiving an async callback for loading the image
        // but after we've detached our view.  In which case, we need to just ignore this
        return;
    }
    image = [self prepareImage:image];
    TiTransition* transition = [TiTransitionHelper transitionFromArg:self.transition containerView:self];
    [(TiViewProxy*)[self proxy] contentsWillChange];
    if (shouldTransition && transition != nil) {
        UIImageView *oldView = [[self imageView] retain];
        RELEASE_TO_NIL(imageView);
        imageView = [[self cloneView:oldView] retain];
        imageView.image = image;
        [self updateImageSize];
        [self fireLoadEventWithState:@"image"];
        [TiTransitionHelper transitionFromView:oldView toView:imageView insideView:mainScrollView withTransition:transition prepareBlock:^{
        } completionBlock:^{
            [oldView release];
        }];
    }
    else {
        [[self imageView] setImage:image];
        [self updateImageSize];
        [self fireLoadEventWithState:@"image"];
    }
}


-(id)prepareImage:(id)image
{
    UIImage* imageToUse = nil;
    if ([image isKindOfClass:[UIImage class]]) {
        imageToUse = [self rotatedImage:image];
    }
    
    float factor = 1.0f;
    float screenScale = [UIScreen mainScreen].scale;
    if ([TiUtils boolValue:[[self proxy] valueForKey:@"hires"] def:[TiUtils isRetinaDisplay]])
    {
        factor /= screenScale;
    }
    if (_tintColorImage) {
        imageToUse = [imageToUse imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
    }
    if (!TiCapIsUndefined(imageCap)) {
        return [TiUtils stretchedImage:imageToUse withCap:imageCap];
    }
    return imageToUse;
}

-(void)loadDefaultImage
{
    if (!_preventDefaultImage && _defaultImageUrl!=nil)
    {
        UIImage *poster = [[ImageLoader sharedLoader] loadImmediateImage:_defaultImageUrl];
        
        [self transitionToImage:poster];
    }
    else {
        [self transitionToImage:nil];
    }
}

-(void)setImageInternal:(id)img
{
    if (img!=nil)
    {
        NSURL* imageURL = [[self proxy] sanitizeURL:img];
        if (![imageURL isKindOfClass:[NSURL class]]) {
            NSLog(@"[ERROR] invalid image type: \"%@\" is not a TiBlob, URL, TiFile",imageURL);
            return;
        }
        NSURL *url_ = [TiUtils toURL:[imageURL absoluteString] proxy:self.proxy];
        
        __block UIImage *image = nil;
        if (localLoadSync)
        {
            image = [self convertToUIImage:[[ImageLoader sharedLoader] loadImmediateImage:url_]];
        }
        
        if (image==nil)
        {
            shouldTransition = YES;
            [(AkylasZoomableimageViewProxy *)[self proxy] startImageLoad:url_];
            return;
        } else {
            [(AkylasZoomableimageViewProxy*)[self proxy] setImageURL:url_];
            
            if (_filterOptions) {
                shouldTransition = YES;
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void)
                               {
                                   RELEASE_TO_NIL(_currentImage);
                                   _currentImage = [[TiImageHelper imageFiltered:image withOptions:_filterOptions] retain];
                                   TiThreadPerformOnMainThread(^{
                                       [self transitionToImage:_currentImage];
                                   }, NO);
                               });
            }
            else {
                RELEASE_TO_NIL(_currentImage);
                _currentImage = [image retain];
                [self transitionToImage:image];
            }
        }
    }
}

-(id)convertToUIImage:(id)arg
{
    return [TiImageHelper convertToUIImage:arg withProxy:self.proxy];
}

-(void)setScaleType_:(id)arg
{
    scaleType = [TiUtils intValue:arg];
    [self setupScales];
}

-(void)setLocalLoadSync_:(id)arg
{
    localLoadSync = [TiUtils boolValue:arg];
}

-(void)setOnlyTransitionIfRemote_:(id)arg
{
    onlyTransitionIfRemote = [TiUtils boolValue:arg];
}

-(id)getImage {
    return [[self imageView] image];
}

-(void)setReusing:(BOOL)value
{
    _reusing = value;
}

-(void)setImage_:(id)arg
{
    if (!configurationSet) {
        _needsSetImage = YES;
        //        if (_reusing) {
        //            [self loadDefaultImage];
        //        }
        return;
    }
    _needsSetImage = NO;
    if (_currentImageSource && [_currentImageSource isEqual:arg] && _currentImage) return;
    if ([[self viewProxy] _hasListeners:@"startload" checkParent:NO])
    {
        [self.proxy fireEvent:@"startload" withObject:@{
                                                        @"image":arg
                                                        } propagate:NO checkForListener:NO];
    }
    RELEASE_TO_NIL(_currentImageSource)
    _currentImageSource = [arg retain];
    
    shouldTransition = !onlyTransitionIfRemote && !_reusing;
    if (arg==nil || [arg isEqual:@""] || [arg isKindOfClass:[NSNull class]])
    {
        [self loadDefaultImage];
        return;
    }
    
    if (_reusing) {
        [self loadDefaultImage];
    }
    shouldTransition = !onlyTransitionIfRemote;
    
    id image = nil;
    NSURL* imageURL = nil;
    RELEASE_TO_NIL(_svg);

    if (localLoadSync || ![arg isKindOfClass:[NSString class]]) {
        image = [self convertToUIImage:arg];
    }
    
    if (image == nil)
    {
        [self setImageInternal:arg];
        return;
    }
    if (_filterOptions) {
        shouldTransition = YES;
        __block id imageSource = _currentImageSource;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void)
                       {
                           if (imageSource != _currentImageSource) return;
                           RELEASE_TO_NIL(_currentImage);
                           _currentImage = [[TiImageHelper imageFiltered:image withOptions:_filterOptions] retain];
                           TiThreadPerformOnMainThread(^{
                               if (imageSource != _currentImageSource) return;
                               [self transitionToImage:_currentImage];
                           }, NO);
                       });
    }
    else {
        RELEASE_TO_NIL(_currentImage);
        _currentImage = [image retain];
        [self transitionToImage:image];
    }
}

-(void)setTintColor_:(id)value
{
    ENSURE_TYPE_OR_NIL(value, NSString);
    UIImageRenderingMode renderingMode = value ? UIImageRenderingModeAlwaysTemplate : UIImageRenderingModeAlwaysOriginal;
    
    [imageView setImage:[[imageView image] imageWithRenderingMode:renderingMode]];
    [imageView setTintColor:value ? [[TiUtils colorValue:value] _color]: nil];
}

-(void)setImageMask_:(id)arg
{
    UIImage* image = [self loadImage:arg];
    UIImageView *imageview = [self imageView];
    if (image == nil) {
        imageview.layer.mask = nil;
    }
    else {
        if (imageview.layer.mask == nil) {
            imageview.layer.mask = [CALayer layer];
            imageview.layer.mask.frame = self.layer.bounds;
        }
        imageview.layer.opaque = NO;
        imageview.layer.mask.contentsScale = [image scale];
        imageview.layer.mask.magnificationFilter = @"nearest";
        imageview.layer.mask.contents = (id)image.CGImage;
    }
    
    [imageview.layer setNeedsDisplay];
}

-(void)setTransition_:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary)
    self.transition = arg;
}

-(void)configurationSet
{
    [super configurationSet];
    if (_needsSetImage) {
        [self setImage_:[self.proxy valueForKey:@"image"]];
    }
}

-(void)setPreventDefaultImage_:(id)value
{
    _preventDefaultImage = [TiUtils boolValue:value];
    if (configurationSet) [self setImage_:[self.proxy valueForKey:@"image"]];
}

-(void)setDefaultImage_:(id)value
{
    RELEASE_TO_NIL(_defaultImageUrl)
    _defaultImageUrl = [[TiUtils toURL:value proxy:self.proxy] retain];
    if (configurationSet) {
        [self setImage_:[self.proxy valueForKey:@"image"]];
    } else if (_defaultImageUrl) {
        _needsSetImage = YES;
    }
}

-(void)setFilterOptions_:(id)value
{
    RELEASE_TO_NIL(_filterOptions)
    _filterOptions = [value retain];
    if (configurationSet) [self setImage_:[self.proxy valueForKey:@"image"]];
}
#pragma mark ImageLoader delegates

-(void)imageLoadSuccess:(ImageLoaderRequest*)request image:(id)image
{
    RELEASE_TO_NIL(_currentImage);
    shouldTransition = YES;
    _currentImage = [image retain];
    if (_filterOptions) {
        __block id imageSource = _currentImageSource;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void)
                       {
                           if (imageSource != _currentImageSource) return;
                           RELEASE_TO_NIL(_currentImage);
                           _currentImage = [[TiImageHelper imageFiltered:[self convertToUIImage:image] withOptions:_filterOptions] retain];
                           TiThreadPerformOnMainThread(^{
                               if (imageSource != _currentImageSource) return;
                               [self transitionToImage:_currentImage];
                           }, NO);
                       });
        
    }
    else {
        TiThreadPerformOnMainThread(^{
            [self transitionToImage:[self convertToUIImage:image]];
        }, NO);
    }
    
}

-(void)imageLoadFailed:(ImageLoaderRequest*)request error:(NSError*)error
{
    NSLog(@"[ERROR] Failed to load image: %@, Error: %@",[request url], error);
    // NOTE: Loading from URL means we can't pre-determine any % value.
    [self loadDefaultImage];
}

@end
