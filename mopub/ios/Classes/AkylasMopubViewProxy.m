#import "AkylasMopubViewProxy.h"
#import "AkylasMopubView.h"
#import "AkylasMopubViewController.h"
#import "MPAdView.h"
#import "AkylasMopubModule.h"

@implementation AkylasMopubViewProxy
{
//    AkylasMopubViewController* _controller;
}

-(void)_destroy
{
//    RELEASE_TO_NIL(_controller)
	[super _destroy];
}

-(void)_configure
{
	[self replaceValue:NUMBOOL(YES) forKey:@"expandsFromTop" notification:YES];
	[super _configure];
}


-(id)init
{
	if ((self = [super init]))
	{
//        _controller = [[AkylasMopubViewController alloc] initWithProxy:self];
	}
	return self;
}


-(TiDimension)defaultAutoWidthBehavior:(id)unused
{
    return TiDimensionAutoFill;
}
-(TiDimension)defaultAutoHeightBehavior:(id)unused
{
    return TiDimensionAutoSize;
}


-(CGSize)contentSizeForSize:(CGSize)size
{
    if (view != nil)
        return [(AkylasMopubView*)view contentSizeForSize:size];
    else
    {
        CGSize size = MOPUB_BANNER_SIZE;
        NSValue* value =[self valueForKey:PROP_ADSIZE];
        if (value) {
            size = [value CGSizeValue];
        }
        return size;
    }
}

-(void)viewDidAttach
{
//    [(AkylasMopubView*)view loadAd];
}

-(void)viewWillDetach
{
}

-(void)viewDidDetach
{
}

//-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
//{
//    [super willAnimateRotationToInterfaceOrientation:toInterfaceOrientation duration:duration];
//}

-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [[self ad] rotateToOrientation:toInterfaceOrientation];
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

//-(void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
//{
//    CGSize size = [[self adView] adContentViewSize];
//    NSLog(@"%@", NSStringFromCGSize(size));
//}

-(id)ad
{
    return [(AkylasMopubView*)[self view] ad];
}

-(void)refreshAd:(id)unused
{
    ENSURE_UI_THREAD_1_ARG(unused)
    [[self ad] refreshAd];
}

-(void)loadAd:(id)unused
{
    ENSURE_UI_THREAD_1_ARG(unused)
    [(AkylasMopubView*)[self view] loadAd];
}
//-(void)loadFormatIdAndPageId:(id)args
//{
//    ENSURE_SINGLE_ARG(args, NSDictionary);
//    ENSURE_UI_THREAD_1_ARG(args)
//    NSInteger formatId = [TiUtils intValue:@"formatId" properties:args def:-1];
//    NSString* pageId = [TiUtils stringValue:@"pageId" properties:args def:nil];
//    BOOL master = [TiUtils boolValue:@"master" properties:args def:FALSE];
//    NSString* target = [TiUtils stringValue:@"target" properties:args def:nil];
//    float timeout = [TiUtils intValue:@"timeout" properties:args def:3000]/1000;
//    [[self adView] loadFormatId:formatId pageId:pageId master:master target:target timeout:timeout];
//}

@end
