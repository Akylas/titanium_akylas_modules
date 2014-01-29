//
//  ShapeView.m
//  Titanium
//
//  Created by Martin Guillon on 10/08/13.
//
//

#import "AkylasShapesView.h"
#import "AkylasShapesViewProxy.h"
#import "ShapeProxy.h"

@implementation AkylasShapesView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
//        self.layer.opaque = NO;
//        self.layer.needsDisplayOnBoundsChange = YES;
        
    }
    return self;
}
-(void)initializeState
{
    [super initializeState];
    NSArray* shapes = [(AkylasShapesViewProxy*)[self proxy] shapes];
    for (int i = 0; i < [shapes count]; i++) {
        ShapeProxy* shapeProxy = [shapes objectAtIndex:i];
        [self.layer addSublayer:[shapeProxy layer]];
    }
    [(AkylasShapesViewProxy*)self.proxy frameSizeChanged:self.frame bounds:self.bounds];
    
}

- (void) dealloc
{
	[super dealloc];
}

- (BOOL)hasTouchableListener
{
	return YES;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
    [super frameSizeChanged:frame bounds:bounds];
    [(AkylasShapesViewProxy*)self.proxy frameSizeChanged:frame bounds:bounds];
	[self setNeedsDisplay];
}

@end
