#import "TiHLSLayerAnimationStep.h"

@class AkylasShapesAnimation, ShapeProxy;
@interface AkylasShapesAnimationStep : TiHLSLayerAnimationStep {
@private
}

- (void)addShapeAnimation:(AkylasShapesAnimation *)shapeAnimation forShape:(ShapeProxy *)shape;

@end
