#import "AkylasShapesAnimationStep.h"
#import "HLSLayerAnimation+Friend.h"

#import "CAAnimation+Blocks.h"
#import "CALayer+HLSExtensions.h"
#import "HLSAnimationStep+Friend.h"
#import "HLSAnimationStep+Protected.h"
#import "AkylasShapesAnimation+Friend.h"
#import "HLSLogger.h"
#import "HLSFloat.h"
#import "ShapeProxy.h"
#import "ShapeCustomProxy.h"
#import "TiAnimation.h"

#if TARGET_IPHONE_SIMULATOR
#import <dlfcn.h>
#endif


static NSString * const kLayerAnimationGroupKey = @"AkylasShapesAnimation";

@interface HLSLayerAnimationStep()
@property (nonatomic, retain) UIView *dummyView;
@end

@interface CALayer()
- (void)resetAnimations;
@end

@interface AkylasShapesAnimationStep ()

@property (nonatomic, retain) NSString *animationKey;

@end

@implementation AkylasShapesAnimationStep

#pragma mark Object creation and destruction

- (id)init
{
    if ((self = [super init])) {
        self.timingFunction = nil;
        self.animationKey = [NSString stringWithFormat:@"%@_%f", kLayerAnimationGroupKey, [[NSDate date] timeIntervalSince1970]];
    }
    return self;
}

- (void)dealloc
{
    RELEASE_TO_NIL(animationKey);
    [super dealloc];
}

#pragma mark Accessors and mutators

@synthesize animationKey;

#pragma mark Managing the animation

- (void)addShapeAnimation:(AkylasShapesAnimation *)shapeAnimation forLayer:(CALayer *)layer
{
    [self addObjectAnimation:shapeAnimation forObject:layer];
}

- (void)addShapeAnimation:(AkylasShapesAnimation *)shapeAnimation forShape:(ShapeProxy *)shape
{
    shapeAnimation.shapeProxy = shape;
    if ([shape isKindOfClass:[ShapeCustomProxy class]]) {
        [self addShapeAnimation:shapeAnimation forLayer:[(ShapeCustomProxy*)shape getLayer]];
    }
}

- (void)playAnimationWithStartTime:(NSTimeInterval)startTime animated:(BOOL)animated
{
    if (!animated || self.terminating) return;
    NSAssert(doublele(startTime, self.duration), @"The start time of a step cannot be greater than its duration");
    
    NSTimeInterval duration = self.duration;
    if (animated) {
        // This dummy view is always animated. There is no way to set a start callback for a CATransaction.
        // Therefore, we always ensure the transaction is never empty by animating a dummy view, and we set
        // animation callbacks for its associated animation (which, since it is part of the transaction,
        // will be triggered when the transaction begins / ends animating)
        self.dummyView = [[[UIView alloc] initWithFrame:CGRectZero] autorelease];
        [[UIApplication sharedApplication].keyWindow addSubview:self.dummyView];
        
        // For tests within the iOS simulator only: Slow down Core Animations as UIView block-based animations (when
        // quickly pressing the shift key three times)
        //
        // Credits to Cédric Luthi, see http://twitter.com/0xced/statuses/232860477317869568
//#if TARGET_IPHONE_SIMULATOR
//        static CGFloat (*s_UIAnimationDragCoefficient)(void) = NULL;
//        static BOOL s_firstLoad = YES;
//        if (s_firstLoad) {
//            void *UIKitDylib = dlopen([[[NSBundle bundleForClass:[UIApplication class]] executablePath] fileSystemRepresentation], RTLD_LAZY);
//            s_UIAnimationDragCoefficient = (CGFloat (*)(void))dlsym(UIKitDylib, "UIAnimationDragCoefficient");
//            if (! s_UIAnimationDragCoefficient) {
//                HLSLoggerInfo(@"UIAnimationDragCoefficient not found. Slow animations won't be available for animations based on Core Animation");
//            }
//            
//            s_firstLoad = NO;
//        }
//        
//        if (s_UIAnimationDragCoefficient) {
//            duration *= s_UIAnimationDragCoefficient();
//            startTime *= s_UIAnimationDragCoefficient();
//        }
//#endif
    }
//    [CATransaction begin];
//    [CATransaction setDisableActions:YES];
    // Animate all layers involved in the animation step
    for (CALayer *layer in [self objects]) {
        AkylasShapesAnimation *shapeAnimation = (AkylasShapesAnimation *)[self objectAnimationForObject:layer];
        NSAssert(shapeAnimation != nil, @"Missing layer animation; data consistency failure");
        // Remark: For each property we animate, we still must set the final value manually (CoreAnimations animate properties
        // but do not set them). Since we do not need to support delays (which are implemented at the HLSAnimation level), we
        // can do it right here, eliminating potentially flickering animations (for more information, see HLSAnimation.m)
        NSMutableArray *animations = nil;
        if ([shapeAnimation.shapeProxy isKindOfClass:[ShapeCustomProxy class]]) {
            animations = [(ShapeCustomProxy*)shapeAnimation.shapeProxy animationsForShapeAnimation:shapeAnimation];
        }
//        CFTimeInterval beginTime = CACurrentMediaTime() + 0.75;
//        if (animated) {
//            for (CABasicAnimation* anim in animations) {
//                anim.duration = duration;
//                anim.fillMode = kCAFillModeForwards;
//                anim.timeOffset = startTime;
////                anim.beginTime = beginTime;
//                anim.timingFunction = self.timingFunction;
//                anim.autoreverses = shapeAnimation.autoreverse;
//                NSString* key = [NSString stringWithFormat:@"%@_%@", self.animationKey, [anim keyPath]];
//                if ([anim isEqual:[animations objectAtIndex:0]])
//                {
//                    anim.completion = ^void(BOOL finished)
//                    {
//                        if (!animated || self.terminating) {
//                            return;
//                        }
//                        [self notifyAsynchronousAnimationStepDidStopFinished:finished];
//                    };
//                }
//                [layer addAnimation:anim forKey:key];
//            }
//        }
        
        
        // Create the animation group and attach it to the layer
        if (animations && animated) {
            CAAnimationGroup *animationGroup = [CAAnimationGroup animation];
            animationGroup.animations = [NSArray arrayWithArray:animations];
            animationGroup.duration = duration;
            animationGroup.timeOffset = startTime;
            animationGroup.fillMode = kCAFillModeBoth;
            animationGroup.removedOnCompletion = NO;
            animationGroup.timingFunction = self.timingFunction;
            animationGroup.autoreverses = shapeAnimation.autoreverse;
            animationGroup.completion = ^void(BOOL finished)
            {
                if (!animated || self.terminating) {
                    return;
                }
//                if (finished && !animationGroup.autoreverses) {
//                    [self applyAnimProps:animationGroup toLayer:layer];
//                }
                [self notifyAsynchronousAnimationStepDidStopFinished:finished];
                [layer removeAnimationForKey:self.animationKey];
            };
            
            [layer addAnimation:animationGroup forKey:self.animationKey];
        }
    }
    
    // Animate the dummy view. It is also used to set a delegate (one for all animations in the transaction)
    // which will receive the start / end animation events
    if (animated) {
        CABasicAnimation *dummyViewOpacityAnimation = [CABasicAnimation animationWithKeyPath:@"opacity"];
        dummyViewOpacityAnimation.fromValue = [NSNumber numberWithFloat:self.dummyView.layer.opacity];
        dummyViewOpacityAnimation.toValue = [NSNumber numberWithFloat:1.f - self.dummyView.layer.opacity];
        dummyViewOpacityAnimation.fillMode = kCAFillModeRemoved;
        dummyViewOpacityAnimation.duration = duration;
        dummyViewOpacityAnimation.delegate = self;
        [self.dummyView.layer addAnimation:dummyViewOpacityAnimation forKey:self.animationKey];
    }
    
    // Animated
    if (animated) {
        // We need to keep track of animations which have started / ended (there is no way to known when a
        // CATransaction has started or ended, and there order in which the child animations are started or
        // ended is unspecified)
        m_numberOfStartedLayerAnimations = 0;
        m_numberOfFinishedLayerAnimations = 0;
        
        // We want to be able to test the number of animations in the animation stop callback. If the animated
        // layers are dead when the end callback is called (which can happen if the layer they are on is
        // destroyed while the animation was running), we cannot compare to self.objects anymore (otherwise
        // the application will crash). We therefore keep track of how animations are expected, but in a safe way
        // (+ 1 for the dummy view animation)
        m_numberOfLayerAnimations = [self.objects count] + 1;
        
        // When a start time has been defined, the animation must look like it started earlier
        m_startTime = CACurrentMediaTime() - startTime;
        
//        [CATransaction commit];
    }
//    [CATransaction commit];
}

-(void)applyAnimProps:(CAAnimation*)anim toLayer:(CALayer*)layer
{
    if ([anim isKindOfClass:[CAAnimationGroup class]]) {
        for (CAAnimation *animation in [(CAAnimationGroup*)anim animations]) {
            [self applyAnimProps:animation toLayer:layer];
        }
    }
    else if ([anim isKindOfClass:[CABasicAnimation class]]) {
        [layer setValue:[(CABasicAnimation*)anim toValue] forKey:[(CABasicAnimation*)anim keyPath]];
    }
}

- (void)pauseAnimation
{
    [super pauseAnimation];
}

- (void)resumeAnimation
{
    [super resumeAnimation];
}

- (BOOL)isAnimationPaused
{
    return [super isAnimationPaused];
}

- (void)terminateAnimation
{
    for (CALayer *layer in [self objects]) {
        [layer resetAnimations];
        AkylasShapesAnimation *shapeAnimation = (AkylasShapesAnimation *)[self objectAnimationForObject:layer];
        if (shapeAnimation.animationProxy.animation.cancelling ||
            shapeAnimation.restartFromBeginning)[layer removeAnimationForKey:self.animationKey];
    }
    [self.dummyView.layer removeAnimationForKey:self.animationKey];
}

#pragma mark Reverse animation

- (id)reverseAnimationStep
{
    AkylasShapesAnimationStep *reverseAnimationStep = [super reverseAnimationStep];
    return reverseAnimationStep;
}

#pragma mark NSCopying protocol implementation

- (id)copyWithZone:(NSZone *)zone
{
    AkylasShapesAnimationStep *animationStepCopy = [super copyWithZone:zone];
    return animationStepCopy;
}

#pragma mark Animation callbacks

- (void)animationDidStart:(CAAnimation *)animation
{
    m_numberOfStartedLayerAnimations++;
}

- (void)animationDidStop:(CAAnimation *)animation finished:(BOOL)finished
{
    m_numberOfFinishedLayerAnimations++;
    
    if (m_numberOfFinishedLayerAnimations == m_numberOfLayerAnimations) {
        NSAssert(m_numberOfStartedLayerAnimations == m_numberOfFinishedLayerAnimations,
                 @"The number of started and finished animations must be the same");
        
        [self.dummyView removeFromSuperview];
        self.dummyView = nil;
        
        [self notifyAsynchronousAnimationStepDidStopFinished:finished];
    }
}
@end
