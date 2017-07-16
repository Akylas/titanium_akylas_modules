//
//  BalloonMarker.m
//  akylas.charts2
//
//  Created by Martin Guillon on 01/06/2017.
//
//

#import "BalloonMarker.h"

@implementation BalloonMarker
{
    NSString* label;
    NSMutableParagraphStyle* _paragraphStyle;
    NSMutableDictionary* _drawAttributes;
    CGSize _labelSize;
}

-(id)init
{
    if (self = [super init])
    {
        self.arrowSize = CGSizeMake(15, 11);
        _paragraphStyle = [[NSMutableParagraphStyle alloc] init];
        _paragraphStyle.alignment = NSTextAlignmentCenter;
    }
    return self;
}

-(id)initWithColor:(UIColor*)color font:(UIFont*)font textColor:(UIColor*)textColor insets:(UIEdgeInsets)insets
{
    if (self = [self init])
    {
        self.color = color;
        self.font = font;
        self.textColor = textColor;
        self.insets = insets;
    }
    return self;
}

-(void)dealloc {
    RELEASE_TO_NIL(label)
    RELEASE_TO_NIL(_paragraphStyle)
    RELEASE_TO_NIL(_color)
    RELEASE_TO_NIL(_font)
    RELEASE_TO_NIL(_textColor)
    RELEASE_TO_NIL(_drawAttributes)
    [super dealloc];
}

-(CGPoint)offsetForDrawingAtPoint:(CGPoint)point
{
    
    CGSize size = self.size;
    point.x -= size.width / 2.0;
    point.y -= size.height;
    return [super offsetForDrawingAtPoint: point];
}

-(void)refreshContentWithEntry:(ChartDataEntry *)entry highlight:(ChartHighlight *)highlight
{
    [self setLabel:[NSString stringWithFormat:@"%f",entry.y]];
}

-(void)setLabel:(NSString*)newLabel
{
    label = [newLabel retain];
    
    [_drawAttributes removeAllObjects];
    _drawAttributes[NSFontAttributeName] = self.font;
    _drawAttributes[NSParagraphStyleAttributeName] = _paragraphStyle;
    _drawAttributes[NSForegroundColorAttributeName] = self.textColor;
    
    _labelSize = label?[label sizeWithAttributes:_drawAttributes ]: CGSizeZero;
    
    CGSize size = CGSizeZero;
    size.width = _labelSize.width + self.insets.left + self.insets.right;
    size.height = _labelSize.height + self.insets.top + self.insets.bottom;
    size.width = MAX(self.minimumSize.width, size.width);
    size.height = MAX(self.minimumSize.height, size.height);
    self.size = size;
}
-(void)drawWithContext:(CGContextRef)context point:(CGPoint)point
{
    if (!label) {
        return;
    }
    
    CGPoint offset = [self offsetForDrawingAtPoint: point];
    CGSize size = self.size;
    
    CGRect rect = CGRectMake(point.x + offset.x,point.y + offset.y, size.width, size.height);
    rect.origin.x -= size.width / 2.0;
    rect.origin.y -= size.height;
    
    CGContextSaveGState(context);
    
    if (self.color)
    {
        CGSize arrowSize = self.arrowSize;
        CGContextSetFillColorWithColor(context, self.color.CGColor);
        CGContextBeginPath(context);
        CGContextMoveToPoint(context, rect.origin.x, rect.origin.y);
        CGContextAddLineToPoint(context, rect.origin.x + rect.size.width, rect.origin.y);
        CGContextAddLineToPoint(context, rect.origin.x + rect.size.width, rect.origin.y + rect.size.height - arrowSize.height);

        CGContextAddLineToPoint(context, rect.origin.x + (rect.size.width + arrowSize.width) / 2.0,
                                rect.origin.y + rect.size.height - arrowSize.height);
        CGContextAddLineToPoint(context, rect.origin.x + rect.size.width / 2.0,
                                rect.origin.y + rect.size.height);
        CGContextAddLineToPoint(context, rect.origin.x + (rect.size.width - arrowSize.width) / 2.0,
                                rect.origin.y + rect.size.height - arrowSize.height);
        CGContextAddLineToPoint(context, rect.origin.x,
                                rect.origin.y + rect.size.height - arrowSize.height);
        CGContextAddLineToPoint(context, rect.origin.x,
                                rect.origin.y);
        CGContextFillPath(context);
    }
    
    rect.origin.y += self.insets.top;
    rect.size.height -= self.insets.top + self.insets.bottom;
    
    UIGraphicsPushContext(context);
    [label drawInRect:rect withAttributes:_drawAttributes];
    UIGraphicsPopContext();
    
    CGContextRestoreGState(context);
    
}

@end
