//
//  CustomChartsMarker.m
//  akylas.charts2
//
//  Created by Martin Guillon on 30/07/16.
//
//

#import "ImageMarker.h"

@implementation ImageMarker


-(void)dealloc {
    RELEASE_TO_NIL(_image)
    [super dealloc];
}

-(CGPoint)offsetForDrawingAtPoint:(CGPoint)point
{
    
    CGSize size = self.size;
    CGPoint offset = self.offset;
    ChartViewBase* chart = self.chartView;

    if (size.width == 0.0 && self.image != nil)
    {
        size.width = self.image.size.width;
    }
    if (size.height == 0.0 && self.image != nil)
    {
        size.height = self.image.size.height;
    }
    
    NSInteger width = size.width;
    NSInteger height = size.height;
    
    if (point.x + offset.x < 0.0)
    {
        offset.x = -point.x;
    }
    else if (chart != nil && point.x + width + offset.x > chart.bounds.size.width)
    {
        offset.x = chart.bounds.size.width - point.x - width;
    }
    
    if (point.y + offset.y < 0)
    {
        offset.y = -point.y;
    }
    else if (chart != nil && point.y + height + offset.y > chart.bounds.size.height)
    {
        offset.y = chart.bounds.size.height - point.y - height;
    }
    
    return offset;
}

-(void)refreshContentWithEntry:(ChartDataEntry *)entry highlight:(ChartHighlight *)highlight
{
    // Do nothing here...
}

-(void)drawWithContext:(CGContextRef)context point:(CGPoint)point
{
    CGPoint offset = [self offsetForDrawingAtPoint: point];
    
    CGSize size = self.size;
    
    if (size.width == 0.0 && self.image != nil)
    {
        size.width = self.image.size.width;
    }
    if (size.height == 0.0 && self.image != nil)
    {
        size.height = self.image.size.height;
    }
    
    CGRect rect = CGRectMake(point.x + offset.x, point.y + offset.y, size.width, size.height);
    UIGraphicsPushContext(context);
    CGContextDrawImage( context, rect, self.image.CGImage);
    UIGraphicsPopContext();

}
@end
