//
//  ShapeViewProxy.h
//  Titanium
//
//  Created by Martin Guillon on 10/08/13.
//
//

#import "TiViewProxy.h"

@interface AkylasShapesViewProxy : TiViewProxy
{
}
-(NSArray*)shapes;
-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds;
-(BOOL)animating;
-(void)redraw;
@end
