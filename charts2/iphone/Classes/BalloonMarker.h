//
//  BalloonMarker.h
//  akylas.charts2
//
//  Created by Martin Guillon on 01/06/2017.
//
//

#import "ImageMarker.h"

@interface BalloonMarker : ImageMarker
@property (nonatomic, retain) UIColor* color;
@property (nonatomic, retain) UIColor* textColor;
@property (nonatomic, retain) UIFont* font;
@property (nonatomic) CGSize arrowSize;
@property (nonatomic) CGSize minimumSize;
@property (nonatomic) UIEdgeInsets insets;
-(id)initWithColor:(UIColor*)color font:(UIFont*)font textColor:(UIColor*)textColor insets:(UIEdgeInsets)insets;
@end
