

#import "AkylasCameraViewProxy.h"
#import "AkylasCameraView.h"
#import "TiUtils.h"

@implementation AkylasCameraViewProxy
{
}

-(void)cleanup{
}


-(id)init
{
	if ((self = [super init]))
	{
	}
	return self;
}

USE_VIEW_FOR_CONTENT_SIZE


-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [(AkylasCameraView*)view willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}


-(void)_destroy
{
    [self cleanup];
    [super _destroy];
}


-(AkylasCameraView*)cameraView {
    return (AkylasCameraView*)view;
}

- (void) start:(id)args
{
    TiThreadPerformBlockOnMainThread(^{
        [[self cameraView] start];
    },NO);
}

- (void) stop:(id)args
{
    TiThreadPerformBlockOnMainThread(^{
        [[self cameraView] stop];
    },NO);
}


- (void) swapCamera:(id)args
{
//    TiThreadPerformBlockOnMainThread(^{
        NSInteger cameraPosition = [TiUtils intValue:[self valueForKey:@"whichCamera"] def:AVCaptureDevicePositionBack];
        if (cameraPosition == AVCaptureDevicePositionFront)
            [self replaceValue:@(AVCaptureDevicePositionBack) forKey:@"whichCamera" notification:YES];
        else
            [self replaceValue:@(AVCaptureDevicePositionFront) forKey:@"whichCamera" notification:YES];
//    },NO);
}

- (void) cameraFocus:(id)args
{
    TiThreadPerformBlockOnMainThread(^{
        CGPoint p = [TiUtils pointValue:args];
        [[self cameraView] focusAtPoint:p];
    },NO);
}

- (void) cameraAutoFocus:(id)args
{
    TiThreadPerformBlockOnMainThread(^{
        CGPoint p = [TiUtils pointValue:args];
        [[self cameraView] autoFocusAtPoint:p];
    },NO);
}

-(void)takePicture:(id)options
{
	ENSURE_SINGLE_ARG(options,NSDictionary);
    TiThreadPerformBlockOnMainThread(^{
        [[self cameraView] takePicture:options];
    },NO);
}



@end
