/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasImageModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

#import "GPUImage.h"
#import "TiViewProxy.h"

#define USE_TI_MEDIA

#import "MediaModule.h"

@implementation AkylasImageModule

MAKE_SYSTEM_PROP(FILTER_GAUSSIAN_BLUR,0);

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"5c27b566-0a7d-458f-8ee1-2299537e8d09";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.image";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
	
	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup 

-(void)dealloc
{
	// release any resources that have been retained by the module
    RELEASE_TO_NIL(currentFilter);
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

-(NSString*)getPathToApplicationAsset:(NSString*) fileName
{
	// The application assets can be accessed by building a path from the mainBundle of the application.
    
	NSString *result = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:fileName];
    
	return result;
}

-(void)setCurrentFilter:(Class)class {
    if (currentFilter == nil || ![currentFilter isKindOfClass:class])
    {
        currentFilter = [[class alloc] init];
    }
}

-(UIImage*)getFilteredImage:(UIImage*)inputImage withFilter:(NSNumber*)filterType options:(NSDictionary*)options
{
    int type = [filterType intValue];
    switch (type) {
        case 0:
        {
            [self setCurrentFilter:[GPUImageFastBlurFilter class]];
            CGFloat blurSize = [TiUtils intValue:@"blurSize" properties:options def:1.0f];
            ((GPUImageFastBlurFilter*)currentFilter).blurSize = blurSize;
            return [currentFilter imageByFilteringImage:inputImage];
            break;
        }
        default:
            break;
    }
    return nil;
}

-(id)getFilteredImage:(id)args
{
    ENSURE_TYPE(args, NSArray)
    NSNumber *filterType;
    NSDictionary *options;
	ENSURE_ARG_AT_INDEX(filterType, args, 1, NSNumber);
    ENSURE_ARG_OR_NIL_AT_INDEX(options, args, 2, NSDictionary);
    id imageArg = [args objectAtIndex:0];
    UIImage* image = nil;
    if ([imageArg isKindOfClass:[NSString class]]) {
        NSString *imagePath = [self getPathToApplicationAsset:imageArg];
        image = [UIImage imageWithContentsOfFile:imagePath];
    }
    else if([imageArg isKindOfClass:[UIImage class]]) {
        image = imageArg;
    }
    else if([imageArg isKindOfClass:[TiBlob class]]) {
        image = ((TiBlob*)imageArg).image;
    }
    
    if (image == nil) {
        NSLog(@"[ERROR] getFilteredImage: could not load image from object of type: %@",[imageArg class]);
		return;
    }
    
    UIImage* result = [self getFilteredImage:image withFilter:filterType options:options];
    return [[[TiBlob alloc] initWithImage:result] autorelease];
}

-(id)getFilteredViewToImage:(id)args
{
    ENSURE_TYPE(args, NSArray)
    TiViewProxy *viewProxy;
    NSNumber *filterType;
    NSNumber *scale;
    NSDictionary *options;
	ENSURE_ARG_AT_INDEX(viewProxy, args, 0, TiViewProxy);
	ENSURE_ARG_AT_INDEX(scale, args, 1, NSNumber);
	ENSURE_ARG_AT_INDEX(filterType, args, 2, NSNumber);
    ENSURE_ARG_OR_NIL_AT_INDEX(options, args, 3, NSDictionary);
    
    __block TiBlob* result =[[TiBlob alloc] init];
    TiThreadPerformOnMainThread(^{
		UIImage *inputImage = [viewProxy toImageWithScale:[scale floatValue]];
        result.image = [self getFilteredImage:inputImage withFilter:filterType options:options];
        [result setMimeType:@"image/png" type:TiBlobTypeImage];
	}, YES);
    return [result autorelease];
    
}

-(id)getFilteredScreenshot:(id)args
{
    ENSURE_TYPE(args, NSArray)
    NSNumber *filterType;
    NSNumber *scale;
    NSDictionary *options;
	ENSURE_ARG_AT_INDEX(scale, args, 0, NSNumber);
	ENSURE_ARG_AT_INDEX(filterType, args, 1, NSNumber);
    ENSURE_ARG_OR_NIL_AT_INDEX(options, args, 2, NSDictionary);
    
    __block TiBlob* result =[[TiBlob alloc] init];
    TiThreadPerformOnMainThread(^{
		UIImage *inputImage = [MediaModule takeScreenshotWithScale:[scale floatValue]];
        result.image = [self getFilteredImage:inputImage withFilter:filterType options:options];
        [result setMimeType:@"image/png" type:TiBlobTypeImage];
        
	}, YES);
    return [result autorelease];
}

@end
