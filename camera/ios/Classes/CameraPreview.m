//
//  CameraPreview.m
//  IziPass
//
//  Created by Martin Guillon on 1/2/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "CameraPreview.h"

@implementation CameraPreview
@synthesize prevLayer = _prevLayer;
@synthesize previewTransform;
@synthesize interfaceOrientation;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        self.interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    }
    return self;
}

static inline CGFloat rotationForInterfaceOrientation (int orient)
{
    // resolve camera/device image orientation to view/interface orientation
    switch(orient)
    {
        case UIInterfaceOrientationLandscapeLeft:
            return(M_PI_2);
        case UIInterfaceOrientationPortraitUpsideDown:
            return(M_PI);
        case UIInterfaceOrientationLandscapeRight:
            return(3 * M_PI_2);
        case UIInterfaceOrientationPortrait:
            return(2 * M_PI);
    }
    return(0);
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    CGRect bounds = self.bounds;
    if(!bounds.size.width || !bounds.size.height)
        return;

    // orient view bounds to match camera image
    CGSize psize;
    if(UIInterfaceOrientationIsPortrait(interfaceOrientation))
        psize = CGSizeMake(bounds.size.height, bounds.size.width);
    else
        psize = bounds.size;
    [CATransaction begin];
    if (animationDuration)
    {
        [CATransaction setAnimationDuration: animationDuration];
        [CATransaction setAnimationTimingFunction:
         [CAMediaTimingFunction functionWithName:
          kCAMediaTimingFunctionEaseInEaseOut]];
    }
    else
        [CATransaction setDisableActions: YES];
    
    self.prevLayer.bounds = CGRectMake(0, 0, psize.height, psize.width);
    // center preview in view
    self.prevLayer.position = CGPointMake(bounds.size.width / 2,
                                          bounds.size.height / 2);

    CGFloat angle = rotationForInterfaceOrientation(self.interfaceOrientation);
    CATransform3D xform = CATransform3DMakeAffineTransform(previewTransform);
    self.prevLayer.transform = CATransform3DRotate(xform, angle, 0, 0, 1);
    
    [CATransaction commit];
    animationDuration = 0;
}


-(void)setFrame:(CGRect)frame
{
    [super setFrame:frame];
    if (_prevLayer) {
        CGRect bounds = self.bounds;
        bounds.origin = CGPointZero;
        self.prevLayer.bounds = bounds;
        self.prevLayer.position = CGPointMake(bounds.size.width / 2,
                                              bounds.size.height / 2);
    }
    [self setNeedsLayout];
}

- (void) setPreviewTransform: (CGAffineTransform) xfrm
{
    previewTransform = xfrm;
    [self setNeedsLayout];
}

-(void)setPrevLayer:(AVCaptureVideoPreviewLayer *)prevLayer
{
    if (_prevLayer) {
        [_prevLayer removeFromSuperlayer];
        [_prevLayer release];
    }
    previewTransform = CGAffineTransformIdentity;
    _prevLayer = [prevLayer retain];
    CGRect bounds = self.bounds;
    bounds.origin = CGPointZero;
   self.prevLayer.bounds = bounds;
    self.prevLayer.position = CGPointMake(bounds.size.width / 2,
                                          bounds.size.height / 2);
    self.prevLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    [self.layer addSublayer: self.prevLayer];
    [self setNeedsLayout];
}

- (void) willRotateToInterfaceOrientation: (UIInterfaceOrientation) orient
                                 duration: (NSTimeInterval) duration
{
    if(self.interfaceOrientation != orient) {
        self.interfaceOrientation = orient;
        animationDuration = duration;
    }
}



@end
