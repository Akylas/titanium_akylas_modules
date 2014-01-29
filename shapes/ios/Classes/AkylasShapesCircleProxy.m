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


@end
