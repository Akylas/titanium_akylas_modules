//
//  CircleProxy.m
//  Titanium
//
//  Created by Martin Guillon on 10/08/13.
//
//

#import "AkylasShapesCircleProxy.h"
#import "CustomShapeLayer.h"

@interface CustomCircleShapeLayer: CustomShapeLayer
@end

@implementation CustomCircleShapeLayer

- (UIBezierPath *)getBPath
{
    return [UIBezierPath bezierPathWithArcCenter:self.center radius:self.radius.width startAngle:-M_PI_2 endAngle:M_PI_2*3 clockwise:_proxy.clockwise];
}
@end

@implementation AkylasShapesCircleProxy

+ (Class)layerClass {
    return [CustomCircleShapeLayer class];
}



-(CGPoint) computeCenterInSize:(CGSize)size_ decale:(CGSize)decale_
{
    CGFloat lineWidth_2 = [TiUtils floatValue:[self valueForKey:kAnimLineWidth] def:0.0f] / 2;
    decale_.width += lineWidth_2 - 2;
    decale_.height += lineWidth_2 - 2;
    return [super computeCenterInSize:size_ decale:decale_];
}

@end
