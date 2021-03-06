//
//  ShapeCustomProxy.m
//  Titanium
//
//  Created by Martin Guillon on 24/08/13.
//
//

#import "ShapeCustomProxy.h"
#import "CustomShapeLayer.h"
#import "AkylasShapesViewProxy.h"
#import "ImageLoader.h"
#import "AkylasShapesAnimation+Friend.h"


@implementation ShapeCustomProxy
@synthesize center = _center, anchor, clockwise;
+ (Class)layerClass {
    return [CustomShapeLayer class];
}

- (id)init {
    if (self = [super init])
    {
        ((CustomShapeLayer*)_layer).proxy = self;
        _center = [[TiPoint alloc] initWithObject:[NSDictionary dictionaryWithObjectsAndKeys:@"0%", @"x", @"0%", @"y", nil]];
        anchor = ShapeAnchorCenter;
        clockwise = YES;
        _padding = UIEdgeInsetsZero;
    }
    return self;
}

- (UIBezierPath *)getBPath
{
    return [UIBezierPath bezierPath];
}

-(void)setPadding:(id)value
{
    _padding = [TiUtils insetValue:value];
}

-(void)boundsChanged:(CGRect)bounds
{
    BOOL animating = IS_OF_CLASS(self.parent, TiAnimatableProxy) && [(TiAnimatableProxy*)self.parent animating];
    if (!animating) {
        [CATransaction begin];
        [CATransaction setDisableActions: YES];
    }
    [super boundsChanged:bounds];
       if (!animating) {
        [CATransaction commit];
    }
}

-(CGPoint) computeCenterInSize:(CGSize)size_ decale:(CGSize)decale_
{
    return [self computePoint:_center withAnchor:self.anchor inSize:size_ decale:decale_];
}

-(void)updateRect:(CGRect) parentBounds
{
    _parentBounds = parentBounds;
    CGRect bounds = UIEdgeInsetsInsetRect(parentBounds, _padding);
    _layer.frame = bounds;
    CGSize radius = [self getRadius:bounds.size inProperties:[self allProperties]];
    CGPoint cgCenter = [self computeCenterInSize:bounds.size decale:radius];
    
    [_layer setValue:[NSValue valueWithCGSize:radius] forKey:kAnimRadius];
    [_layer setValue:[NSValue valueWithCGPoint:cgCenter] forKey:kAnimCenter];
    
//    CGRect shapeBounds;
    self.currentBounds = [(CustomShapeLayer*)_layer getBoundingBox];
//    self.currentShapeBounds = shapeBounds;
}

-(void)updateRealTransform
{
    CGAffineTransform transform = [self getRealTransform:_currentShapeBounds parentSize:_layer.frame.size];
    [self setLayerValue:[NSValue valueWithCATransform3D:CATransform3DMakeAffineTransform(transform)] forKey:kAnimShapeTransform];
}


- (void) dealloc
{
    ((CustomShapeLayer*)self.layer).proxy = nil;
    RELEASE_TO_NIL(_center)
	[super dealloc];
}

-(CALayer*)getLayer
{
    return _layer;
}


-(CGLineJoin)lineJoinFromString:(NSString*)value
{
	if ([value isEqualToString:@"miter"])
	{
		return kCGLineJoinMiter;
	}
	else if ([value isEqualToString:@"round"])
	{
		return kCGLineJoinRound;
	}
	return kCGLineJoinBevel;
}

-(CGLineCap)lineCapFromString:(NSString*)value
{
	if ([value isEqualToString:@"square"])
	{
		return kCGLineCapSquare;
	}
	else if ([value isEqualToString:@"round"])
	{
		return kCGLineCapRound;
	}
	return kCGLineCapButt;
}


-(void)setLayerValue:(id)value forKey:(NSString*)key {
    if (![NSThread isMainThread]) {
        TiThreadPerformOnMainThread(^{
            [self setLayerValue:value forKey:key];
        },NO);
    }
    else {
        [CATransaction begin];
        [CATransaction setDisableActions: YES];
        [_layer setValue:value forKey:key];
        [_layer setNeedsDisplay];
        [CATransaction commit];
    }
}

-(void)setCenter:(id)arg
{
    RELEASE_TO_NIL(_center)
    _center = [[TiUtils tiPointValue:arg def:[self defaultCenter]] retain];
	[self replaceValue:arg forKey:kAnimCenter notification:NO];
}

-(void)setLineColor:(id)color
{
    [self setLayerValue:(id)[[TiUtils colorValue:color] cgColor] forKey:kAnimLineColor];
	[self replaceValue:color forKey:kAnimLineColor notification:NO];
}

-(void)setFillColor:(id)color
{
    [self setLayerValue:(id)[[TiUtils colorValue:color] cgColor] forKey:kAnimFillColor];
	[self replaceValue:color forKey:kAnimFillColor notification:NO];
}

-(void)setLineJoin:(id)arg
{
    NSInteger result;
    if ([arg isKindOfClass:[NSString class]]) {
        result = [self lineJoinFromString:[TiUtils stringValue:arg]];
    }
    else {
        result = [TiUtils intValue:arg def:kCGLineJoinMiter];
    }
    [self setLayerValue:NUMINTEGER(result) forKey:kAnimLineJoin];
	[self replaceValue:arg forKey:kAnimLineJoin notification:NO];
}

-(void)setLineCap:(id)arg
{
    NSInteger result;
    if ([arg isKindOfClass:[NSString class]]) {
        result = [self lineCapFromString:[TiUtils stringValue:arg]];
    }
    else {
        result = [TiUtils intValue:arg def:kCGLineCapButt];
    }
    [self setLayerValue:NUMINTEGER(result) forKey:kAnimLineCap];
	[self replaceValue:arg forKey:kAnimLineCap notification:NO];
}

-(void)setLineDash:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    if (!arg) {
        [self setLayerValue:nil forKey:@"dashPattern"];
        [self setLayerValue:nil forKey:@"dashPhase"];
    } else {
        if ([arg objectForKey:@"pattern"]) {
            [self setLayerValue:[arg objectForKey:@"pattern"] forKey:@"dashPattern"];
        }
        if ([arg objectForKey:@"phase"]) {
            [self setLayerValue:[NSNumber numberWithFloat:[TiUtils floatValue:[arg objectForKey:@"phase"]]] forKey:@"dashPhase"];
        }
    }
    
	[self replaceValue:arg forKey:@"lineDash" notification:NO];
}

-(void)setLineWidth:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSNumber);
    [self setLayerValue:(arg!=nil)?arg:[NSNumber numberWithFloat:1.0f] forKey:kAnimLineWidth];
	[self replaceValue:arg forKey:kAnimLineWidth notification:NO];
}

-(void)setLineShadow:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    TiShadow* shadow = [TiUIHelper getShadow:arg];
    [self setLayerValue:(id)((UIColor*)shadow.shadowColor).CGColor forKey:@"lineShadowColor"];
    [self setLayerValue:[NSValue valueWithCGSize:shadow.shadowOffset]  forKey:@"lineShadowOffset"];
    [self setLayerValue:[NSNumber numberWithFloat:shadow.shadowBlurRadius]  forKey:@"lineShadowRadius"];
    [self replaceValue:arg forKey:@"lineShadow" notification:NO];
}

-(void)setFillShadow:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    TiShadow* shadow = [TiUIHelper getShadow:arg];
    [self setLayerValue:(id)((UIColor*)shadow.shadowColor).CGColor forKey:@"fillShadowColor"];
    [self setLayerValue:[NSValue valueWithCGSize:shadow.shadowOffset]  forKey:@"fillShadowOffset"];
    [self setLayerValue:[NSNumber numberWithFloat:shadow.shadowBlurRadius]  forKey:@"fillShadowRadius"];
    [self replaceValue:arg forKey:@"fillShadow" notification:NO];
}

-(void)setLineGradient:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    [self setLayerValue:[TiGradient gradientFromObject:arg proxy:self] forKey:kAnimLineGradient];
	[self replaceValue:arg forKey:kAnimLineGradient notification:NO];
}

-(void)setFillGradient:(id)arg
{
    ENSURE_SINGLE_ARG_OR_NIL(arg, NSDictionary);
    [self setLayerValue:[TiGradient gradientFromObject:arg proxy:self] forKey:kAnimFillGradient];
	[self replaceValue:arg forKey:kAnimFillGradient notification:NO];
}


-(void)setLineOpacity:(id)arg
{
    [self setLayerValue:(arg!=nil)?arg:@(1.0f) forKey:kAnimLineOpacity];
    [self replaceValue:arg forKey:kAnimLineOpacity notification:NO];
}

-(void)setFillOpacity:(id)arg
{
    [self setLayerValue:(arg!=nil)?arg:@(1.0f) forKey:kAnimFillOpacity];
    [self replaceValue:arg forKey:kAnimFillOpacity notification:NO];
}

-(void)setVisible:(id)arg
{
    BOOL visible= [TiUtils boolValue:arg def:YES];
    [self setLayerValue:@(!visible) forKey:@"hidden"];
    [self replaceValue:arg forKey:@"visible" notification:NO];
}


-(UIImage*)loadImage:(id)arg
{
    if (arg==nil) return nil;
    UIImage *image = nil;
	
    if ([arg isKindOfClass:[TiBlob class]]) {
        TiBlob *blob = (TiBlob*)arg;
        image = [blob image];
    }
    else if ([arg isKindOfClass:[UIImage class]]) {
		// called within this class
        image = (UIImage*)arg;
    }
    else {
        NSURL *url;
        if ([arg isKindOfClass:[TiFile class]]) {
            TiFile *file = (TiFile*)arg;
            url = [NSURL fileURLWithPath:[file path]];
        }
        else {
            url = [TiUtils toURL:arg proxy:self];
        }
        image =  [[ImageLoader sharedLoader]loadImmediateImage:url];
    }
	return image;
}

-(void)setLineImage:(id)arg
{
    [self setLayerValue:[self loadImage:arg] forKey:kAnimLineImage];
	[self replaceValue:arg forKey:kAnimLineImage notification:NO];
}

-(void)setFillImage:(id)arg
{
    [self setLayerValue:[self loadImage:arg] forKey:kAnimFillImage];
	[self replaceValue:arg forKey:kAnimFillImage notification:NO];
}

-(void)setLineInversed:(id)arg
{
    [self setLayerValue:arg forKey:kAnimLineInversed];
	[self replaceValue:arg forKey:kAnimLineInversed notification:NO];
}

-(void)setFillInversed:(id)arg
{
    [self setLayerValue:arg forKey:kAnimFillInversed];
	[self replaceValue:arg forKey:kAnimFillInversed notification:NO];
}

-(void)setLineClipped:(id)arg
{
    [self setLayerValue:arg forKey:@"lineClipped"];
	[self replaceValue:arg forKey:@"lineClipped" notification:NO];
}

-(void)setRetina:(id)arg
{
    [self setLayerValue:arg forKey:@"retina"];
	[self replaceValue:arg forKey:@"retina" notification:NO];
}

-(void)setAntialiasing:(id)arg
{
    [self setLayerValue:arg forKey:@"antialiasing"];
	[self replaceValue:arg forKey:@"antialiasing" notification:NO];
}
//
//-(void)setTransform:(id)transform
//{
//    [super setTransform:transform];
//    [self replaceValue:transform forKey:@"transform" notification:NO];
//}

-(CABasicAnimation *)animationForKeyPath:(NSString*)keyPath_ value:(id)value_ restartFromBeginning:(BOOL)restartFromBeginning_
{
    CABasicAnimation *caAnim = [self animation];
    caAnim.keyPath = keyPath_;
    caAnim.toValue = (!value_ || IS_OF_CLASS(value_, NSNull))?[_layer valueForKeyPath:keyPath_]:value_;
    if (restartFromBeginning_) {
        caAnim.fromValue = [_layer valueForKeyPath:keyPath_];
    }
    return caAnim;
}

-(CABasicAnimation *)addAnimationForKeyPath:(NSString*)keyPath_ restartFromBeginning:(BOOL)restartFromBeginning_ animation:(TiAnimation*)animation holder:(NSMutableArray*)animations animProps:(NSDictionary*)animProps
{
    if ([animation valueForKey:keyPath_]) {
        [animations addObject:[self animationForKeyPath:keyPath_ value:[animProps objectForKey:keyPath_] restartFromBeginning:restartFromBeginning_]];
    }
}
-(void)prepareAnimation:(TiAnimation*)animation holder:(NSMutableArray*)animations animProps:(NSDictionary*)animProps {
 
    BOOL restartFromBeginning = animation.restartFromBeginning;
    if ([animation valueForKey:kAnimLineColor]) {
        UIColor* color = [[TiUtils colorValue:[animProps objectForKey:kAnimLineColor]] _color];
        if (color == nil) color = [UIColor clearColor];
        [animations addObject:[self animationForKeyPath:kAnimLineColor value:(id)color.CGColor restartFromBeginning:restartFromBeginning]];
    }
    if ([animation valueForKey:kAnimFillColor]) {
        UIColor* color = [[TiUtils colorValue:[animProps objectForKey:kAnimFillColor]] _color];
        if (color == nil) color = [UIColor clearColor];
        [animations addObject:[self animationForKeyPath:kAnimFillColor value:(id)color.CGColor restartFromBeginning:restartFromBeginning]];
    }
    
    if ([animation valueForKey:@"lineDash"]) {
        id lineDash = [animation valueForKey:@"lineDash"];
        if (IS_OF_CLASS(lineDash, NSNull)) {
            [animations addObject:[self animationForKeyPath:@"lineDashPhase" value:[NSNull null] restartFromBeginning:restartFromBeginning]];

        }
        else {
            [animations addObject:[self animationForKeyPath:@"lineDashPhase" value:[lineDash objectForKey:@"phase"] restartFromBeginning:restartFromBeginning]];
        }
    }
    
    [self addAnimationForKeyPath:kAnimLineWidth restartFromBeginning:restartFromBeginning animation:animation holder:animations animProps:animProps];
    [self addAnimationForKeyPath:kAnimLineOpacity restartFromBeginning:restartFromBeginning animation:animation holder:animations animProps:animProps];
    [self addAnimationForKeyPath:kAnimFillOpacity restartFromBeginning:restartFromBeginning animation:animation holder:animations animProps:animProps];
    
    if ([animation valueForKey:kAnimCenter] || [animation valueForKey:kAnimRadius]) {
        CGSize radius = [self getRadius:_layer.bounds.size inProperties:animProps];
        TiPoint* center_ = [self tiPointValue:kAnimCenter properties:animProps def:[self defaultCenter]];
        CGPoint cgCenter = [self computePoint:center_ withAnchor:anchor inSize:_parentBounds.size decale:radius];
        
        if ( !CGPointEqualToPoint(cgCenter, ((CustomShapeLayer*)_layer).center)) {
            [animations addObject:[self animationForKeyPath:kAnimCenter value:[NSValue valueWithCGPoint:cgCenter] restartFromBeginning:restartFromBeginning]];
        }
        if ( !CGSizeEqualToSize(radius, ((CustomShapeLayer*)_layer).radius)) {
            [animations addObject:[self animationForKeyPath:kAnimRadius value:[NSValue valueWithCGSize:radius] restartFromBeginning:restartFromBeginning]];
        }
    }
    
    if ([animation valueForKey:@"transform"]) {
        Ti2DMatrix* matrix = [TiUtils matrixValue:@"transform" properties:animProps];
        CGAffineTransform transform = [self prepareTransform:matrix bounds:_currentShapeBounds parentSize:_parentBounds.size];
        [animations addObject:[self animationForKeyPath:kAnimShapeTransform value:[NSValue valueWithCATransform3D:CATransform3DMakeAffineTransform(transform)] restartFromBeginning:restartFromBeginning]];
    }
}

-(CABasicAnimation*) animation
{
    CABasicAnimation *anim = [CABasicAnimation animation];
    anim.fillMode = kCAFillModeBoth;
    return anim;
}

-(NSMutableArray*)animationsForShapeAnimation:(AkylasShapesAnimation*)animation
{
    NSMutableArray* animations = [ NSMutableArray array];
    [self prepareAnimation:animation.animationProxy holder:animations animProps:[animation toProperties]];
    return animations;
}

@end
