//
//  CustomShapeLayer.m
//  Titanium
//
//  Created by Martin Guillon on 24/08/13.
//
//

#import "CustomShapeLayer.h"
#import "ShapeCustomProxy.h"

#import "UIBezierPath+Additions.h"

@implementation CustomShapeLayer
@synthesize dashPhase, lineWidth, lineOpacity, fillOpacity, lineColor = _lineColor, proxy = _proxy, fillColor = _fillColor, fillGradient, lineGradient, lineCap, lineJoin, center, lineShadowRadius, lineShadowColor = _lineShadowColor, lineShadowOffset, radius, fillShadowOffset, fillShadowColor = _fillShadowColor, fillShadowRadius, lineImage, fillImage, lineClipped, lineInversed, fillInversed, retina, shapeTransform, antialiasing;

- (id)init {
    if (self = [super init])
    {
        self.needsDisplayOnBoundsChange = YES;
//        self.shouldRasterize = YES;
        self.contentsScale = self.rasterizationScale = [UIScreen mainScreen].scale;
        
        fillOpacity = lineOpacity = 1.0f;
        lineShadowOffset = fillShadowOffset = CGSizeZero;
        lineInversed = fillInversed = NO;
        lineClipped = NO;
        self.shapeTransform = CATransform3DIdentity;
        antialiasing = YES;
        retina = YES;
    }
    return self;
}

// Make sure that, when the layer is copied, so is the custom ivar
- (id) initWithLayer:(id)layer
{
    self = [super initWithLayer:layer];
    if (self) {
        CustomShapeLayer *customLayer = (CustomShapeLayer *)layer;
        self.proxy = customLayer.proxy;
        self.dashPhase = customLayer.dashPhase;
        self.dashPattern = customLayer.dashPattern;
        self.center = customLayer.center;
        self.radius = customLayer.radius;
        self.lineOpacity = customLayer.lineOpacity;
        self.fillOpacity = customLayer.fillOpacity;
        self.fillColor = customLayer.fillColor;
        self.lineColor = customLayer.lineColor;
        self.fillGradient = customLayer.fillGradient;
        self.lineGradient = customLayer.lineGradient;
        self.lineCap = customLayer.lineCap;
        self.lineJoin = customLayer.lineJoin;
        self.lineWidth = customLayer.lineWidth;
        self.lineShadowOffset = customLayer.lineShadowOffset;
        self.lineShadowColor = customLayer.lineShadowColor;
        self.lineShadowRadius = customLayer.lineShadowRadius;
        self.fillShadowOffset = customLayer.fillShadowOffset;
        self.fillShadowColor = customLayer.fillShadowColor;
        self.fillShadowRadius = customLayer.fillShadowRadius;
        self.lineImage = customLayer.lineImage;
        self.fillImage = customLayer.fillImage;
        self.fillInversed = customLayer.fillInversed;
        self.lineInversed = customLayer.lineInversed;
        self.shapeTransform = customLayer.shapeTransform;
        self.retina = customLayer.retina;
        self.antialiasing = customLayer.antialiasing;
    }
    return self;
}

- (void) dealloc
{
    _proxy = nil;
	if (_fillColor) {
        [(id)_fillColor release];
        _fillColor = nil;
    }
    if (_lineColor) {
        [(id)_lineColor release];
        _lineColor = nil;
    }
    if (_fillShadowColor) {
        [(id)_fillShadowColor release];
        _fillShadowColor = nil;
    }
    if (_lineShadowColor) {
        [(id)_lineShadowColor release];
        _lineShadowColor = nil;
    }

	RELEASE_TO_NIL(fillGradient)
	RELEASE_TO_NIL(lineGradient)
	RELEASE_TO_NIL(_dashPattern)
	RELEASE_TO_NIL(lineImage)
	RELEASE_TO_NIL(fillImage)
//	RELEASE_TO_NIL(shapeTransform)
    if (_cgDashPattern) free(_cgDashPattern);
    
	[super dealloc];
}

static NSArray *animationKeys;
+ (NSArray *)animationKeys
{
    if (!animationKeys)
        animationKeys = [@[@"lineColor",@"lineCap",@"lineJoin",@"lineOpacity"
                         ,@"fillColor",@"fillOpacity", @"lineWidth"
                         , @"center", @"dashPattern", @"dashPhase", @"radius", @"center",@"lineShadowColor",@"fillShadowColor",@"lineShadowOffset",@"fillShadowOffset",@"lineShadowRadius",@"fillShadowRadius", @"shapeTransform"] retain];
    
    return animationKeys;
}

+ (BOOL)needsDisplayForKey:(NSString *)key {
    if ([[self  animationKeys] indexOfObject:key] != NSNotFound)
        return YES;
    return [super needsDisplayForKey:key];
}

- (UIBezierPath *)getBPath
{
    return [UIBezierPath bezierPath];
}

-(void)addPathToContext:(CGContextRef)context path:(CGPathRef)path_ transform:(CGAffineTransform)transform_
{
    CGContextBeginPath(context);
    if (!CGAffineTransformIsIdentity(transform_)) {
        CGPathRef transformedPath = CGPathCreateCopyByTransformingPath(path_, &transform_);
        CGContextAddPath(context, transformedPath);
        CGPathRelease(transformedPath);
    }
    else {
        CGContextAddPath(context, path_);
    }
}

-(void)fillContext:(CGContextRef)context color:(CGColorRef)color image:(UIImage*)image gradient:(TiGradient*)gradient opacity:(CGFloat)opacity
{
    CGContextSetAlpha(context, opacity);
    if (color) {
        CGContextSetFillColorWithColor(context, color);
        CGContextFillRect(context, self.bounds);
    }
    if (gradient || image) {
        if (gradient) {
            [gradient paintContext:context bounds:self.bounds];
        }
        if (image) {
            CGContextScaleCTM (context, 1, -1);
            CGRect imageRect = CGRectMake(0, 0, image.size.width, image.size.height);
            CGContextDrawTiledImage(context, imageRect, image.CGImage);
        }
    }
}

-(void)fillPath:(CGContextRef)context color:(CGColorRef)color opacity:(CGFloat)opacity
{
    CGContextSetAlpha(context, opacity);
    if (color) {
        CGContextSetFillColorWithColor(context, color);
        CGContextFillPath(context);
    }
}

CG_INLINE CGContextRef CGContextCreate(CGSize size)
{
	CGColorSpaceRef space = CGColorSpaceCreateDeviceRGB();
	CGContextRef ctx = CGBitmapContextCreate(nil, size.width, size.height, 8, size.width * (CGColorSpaceGetNumberOfComponents(space) + 1), space, kCGImageAlphaPremultipliedLast);
	CGColorSpaceRelease(space);
    
	return ctx;
}

-(CGRect)getBoundingBox
{
    UIBezierPath* bPath = [self getBPath];
    CGRect currentBounds = bPath.bounds;
//    CGRect currentFrame = self.frame;
   _proxy.currentShapeBounds = currentBounds;
    CGContextRef context = CGContextCreate(self.frame.size);
    CGContextSetLineWidth(context, self.lineWidth);
    CGContextSetLineCap(context, lineCap);
    CGContextSetLineJoin(context, lineJoin);
    if (_dashPattern) {
        CGContextSetLineDash(context, dashPhase, _cgDashPattern, _dashPatternCount);
    }
    CGContextBeginPath(context);
    CGContextAddPath(context, bPath.CGPath);
    CGContextReplacePathWithStrokedPath(context);
    //    currentBounds = CGContextGetPathBoundingBox(context); //needs fixing while rotating
    CGContextRelease(context);
    return currentBounds;
}

- (void)drawBPath:(UIBezierPath *)bPath_ inContext:(CGContextRef)context
{
    UIGraphicsPushContext(context);
    CGContextSetAllowsAntialiasing(context, antialiasing);
    CGContextSetShouldAntialias(context, antialiasing);
//    CGRect currentBounds = bPath_.bounds; //bounds without stroke
//    CGAffineTransform transform = _proxy.realTransform;
    CGAffineTransform transform = CATransform3DGetAffineTransform(self.shapeTransform);
    
    if (_fillColor || fillGradient || fillImage) {
        CGContextSaveGState(context);
        if (_fillShadowColor) CGContextSetShadowWithColor(context, fillShadowOffset, fillShadowRadius, _fillShadowColor);
        if (fillInversed) {
            UIBezierPath* inversed = [UIBezierPath bezierPathWithRect:CGRectInfinite];
            [inversed appendPath:bPath_];
            inversed.usesEvenOddFillRule = YES;
            [inversed applyTransform:transform];
            CGContextSetAlpha(context, fillOpacity);
            CGContextSetFillColorWithColor(context, _fillColor);
            [inversed fill]; // to get the shadow
            [inversed addClip];
            [self fillContext:context color:nil image:fillImage gradient:fillGradient opacity:fillOpacity];
        }
        else {
            [self addPathToContext:context path:bPath_.CGPath transform:transform];
            CGContextClip(context);
            [self fillContext:context color:_fillColor image:fillImage gradient:fillGradient opacity:fillOpacity];
        }
       
        CGContextRestoreGState(context);
    }
    
    if (_lineColor || lineGradient || lineImage) {
        CGContextSaveGState(context);
        
        CGContextSetLineWidth(context, self.lineWidth);
        CGContextSetLineCap(context, lineCap);
        CGContextSetLineJoin(context, lineJoin);
        if (_dashPattern) {
            CGContextSetLineDash(context, dashPhase, _cgDashPattern, _dashPatternCount);
        }
        [self addPathToContext:context path:bPath_.CGPath transform:transform];
        CGContextReplacePathWithStrokedPath(context);
        CGPathRef strokePath = CGContextCopyPath(context);
//        currentBounds = CGContextGetPathBoundingBox(context);
        
        if (_lineShadowColor) CGContextSetShadowWithColor(context, lineShadowOffset, lineShadowRadius, _lineShadowColor);
        if (lineInversed) {
            CGContextReplacePathWithStrokedPath(context);
            UIBezierPath* inversed = [UIBezierPath bezierPathWithRect:CGRectInfinite];
            [inversed appendPath:[UIBezierPath bezierPathWithCGPath:strokePath]];
            inversed.usesEvenOddFillRule = YES;
            [inversed applyTransform:transform];
            CGContextSetAlpha(context, lineOpacity);
            CGContextSetFillColorWithColor(context, _lineColor);
            [inversed fill];
            [inversed addClip];
            [self fillContext:context color:nil image:lineImage gradient:lineGradient opacity:lineOpacity];
        }
        else {
            if (lineClipped) {
                [bPath_ addClip];
                CGContextBeginPath(context);
                CGContextAddPath(context, strokePath);
            }
            
            [self fillPath:context color:_lineColor opacity:lineOpacity]; //to get the shadow
            CGContextAddPath(context, strokePath);
            CGContextClip(context);
            [self fillContext:context color:nil image:lineImage gradient:lineGradient opacity:lineOpacity];
        }
        CGPathRelease(strokePath);
        CGContextRestoreGState(context);
    }
    UIGraphicsPopContext();
//    if (_proxy && !CGRectEqualToRect(currentBounds, CGRectZero)) _proxy.currentBounds = currentBounds;
}

- (void)drawInContext:(CGContextRef)context
{
    UIBezierPath* bPath = [self getBPath];
    [self drawBPath:bPath inContext:context];
}

-(void) setDashPattern:(NSArray *)dashPattern
{
    RELEASE_TO_NIL(_dashPattern);
    if (_cgDashPattern) free(_cgDashPattern);
    _dashPattern = [dashPattern retain];
    if (_dashPattern) {
        _cgDashPattern = [_proxy arrayFromNSArray:dashPattern];
        _dashPatternCount = [dashPattern count];
    }
    else {
        _cgDashPattern = nil;
        _dashPatternCount = 0;
    }
}

-(void)setFillColor:(CGColorRef)value
{
    if (_fillColor) {
        [(id)_fillColor release];
        _fillColor = nil;
    }
    _fillColor = (CGColorRef)[(id)value retain];
}

-(void)setLineColor:(CGColorRef)value
{
    if (_lineColor) {
        [(id)_lineColor release];
        _lineColor = nil;
    }
    _lineColor = (CGColorRef)[(id)value retain];
}

-(void)setFillShadowColor:(CGColorRef)value
{
    if (_fillShadowColor) {
        [(id)_fillShadowColor release];
        _fillShadowColor = nil;
    }
    _fillShadowColor = (CGColorRef)[(id)value retain];
}

-(void)setLineShadowColor:(CGColorRef)value
{
    if (_lineShadowColor) {
        [(id)_lineShadowColor release];
        _lineShadowColor = nil;
    }
    _lineShadowColor = (CGColorRef)[(id)value retain];
}

-(void)setRetina:(BOOL)retina_
{
    retina = retina_;
    self.contentsScale = self.rasterizationScale = retina?[UIScreen mainScreen].scale:1;
}

-(void)setAntialiasing:(BOOL)aliasing_
{
    antialiasing = aliasing_;
}

//-(void)setShapeTransform:(CGAffineTransform)shapeTransform_
//{
//    shapeTransform = shapeTransform_;
//}
@end
