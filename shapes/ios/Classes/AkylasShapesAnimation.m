
#import "AkylasShapesAnimation+Friend.h"
#import "AkylasShapesAnimation.h"
#import "AkylasShapesAnimationStep.h"

#import "ShapeProxy.h"

/**
 * Please read the remarks at the top of HLSLayerAnimation.m
 */

@interface AkylasShapesAnimation ()

@property (nonatomic, retain) ShapeProxy* shapeProxy;
@property (nonatomic, assign) BOOL autoreverse;
@property (nonatomic, assign) BOOL restartFromBeginning;

- (ShapeProxy*)shapeProxy;

@end

@implementation AkylasShapesAnimation

#pragma mark Object creation and destruction

- (id)init
{
    if ((self = [super init])) {
        autoreverse = NO;
        restartFromBeginning = NO;
    }
    return self;
}

-(void)dealloc
{
	RELEASE_TO_NIL(m_shapeProxy);
    [super dealloc];
}

#pragma mark Private API

#pragma mark Accessors and mutators

@synthesize shapeProxy = m_shapeProxy;
@synthesize autoreverse;
@synthesize restartFromBeginning;

- (ShapeProxy*)shapeProxy
{
    return m_shapeProxy;
}

#pragma mark Reverse animation

- (id)reverseObjectAnimation
{
    // See remarks at the beginning
    AkylasShapesAnimation *reverseViewAnimation = [super reverseObjectAnimation];
    reverseViewAnimation.shapeProxy = self.shapeProxy;
    reverseViewAnimation.autoreverse = self.autoreverse;
    reverseViewAnimation.restartFromBeginning = self.restartFromBeginning;
    return reverseViewAnimation;
}

#pragma mark NSCopying protocol implementation

- (id)copyWithZone:(NSZone *)zone
{
    AkylasShapesAnimation *viewAnimationCopy = [super copyWithZone:zone];
    viewAnimationCopy.shapeProxy = self.shapeProxy;
    viewAnimationCopy.autoreverse = self.autoreverse;
    viewAnimationCopy.restartFromBeginning = self.restartFromBeginning;
    return viewAnimationCopy;
}

@end
