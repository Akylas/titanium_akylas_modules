#import "TiUIView+ShapeMask.h"
#import "JRSwizzle.h"
#import "TiUIView.h"
#import "TiViewProxy.h"
#import <objc/runtime.h>
#import "ShapeProxy.h"

@implementation TiUIView (ShapeMask)
NSString * const kTiViewShapeMaskKey = @"kTiViewShapeMask";


+ (void) swizzleShapeMask
{
    [TiUIView jr_swizzleMethod:@selector(frameSizeChanged:bounds:) withMethod:@selector(maskShapeFrameSizeChanged:bounds:) error:nil];
    [TiUIView jr_swizzleMethod:@selector(setMaskFromView_:) withMethod:@selector(setMaskFromView_shapeModule:) error:nil];
}

-(void)maskShapeFrameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    //WARNING: this is the swizzle trick, will actually call [TiUIView frameSizeChanged:bounds:]
    [self maskShapeFrameSizeChanged:frame bounds:bounds];
    TiProxy* maskShape = [[self viewProxy] holdedProxyForKey:kTiViewShapeMaskKey];
    if (maskShape) {
        if (IS_OF_CLASS(maskShape, ShapeProxy)) {
            [(ShapeProxy*)maskShape boundsChanged:bounds];
        } else if (IS_OF_CLASS(maskShape, TiViewProxy)) {
            [(TiViewProxy*)maskShape setSandboxBounds:bounds];
            [(TiViewProxy*)maskShape refreshView];
        }
    }
}

-(void)setMaskFromView_shapeModule:(id)arg
{
    TiViewProxy* viewProxy = [self viewProxy];
    TiProxy* vp = [viewProxy createChildFromObject:arg];
    TiProxy* current = [[self viewProxy] holdedProxyForKey:kTiViewShapeMaskKey];
        if (IS_OF_CLASS(current, ShapeProxy)) {
        [(ShapeProxy*)current removeFromSuperLayer];
        [[self viewProxy] removeHoldedProxyForKey:kTiViewShapeMaskKey];
    } else if (IS_OF_CLASS(current, TiViewProxy)) {
        [[[(TiViewProxy*)current view] layer] removeFromSuperlayer];
        [[self viewProxy] removeProxy:current];
        [[self viewProxy] removeHoldedProxyForKey:kTiViewShapeMaskKey];
    }
    if (IS_OF_CLASS(vp, ShapeProxy)) {
        [[self viewProxy] addProxyToHold:vp forKey:kTiViewShapeMaskKey];
        self.layer.mask = [(ShapeProxy*)vp layer];
    } else if (IS_OF_CLASS(vp, TiViewProxy)) {
        [[self viewProxy] addProxyToHold:vp forKey:kTiViewShapeMaskKey];
        self.layer.mask = [[(TiViewProxy*)vp getAndPrepareViewForOpening] layer];
    }
    [self.layer setNeedsDisplay];
}

@end
