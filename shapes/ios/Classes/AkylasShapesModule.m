/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "AkylasShapesModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "ShapeProxy.h"
#import "AkylasShapesArcProxy.h"
#import "AkylasShapesCircleProxy.h"
#import "AkylasShapesEllipseProxy.h"
#import "AkylasShapesRectProxy.h"
#import "AkylasShapesRoundedRectProxy.h"
#import "AkylasShapesViewProxy.h"
#import "AkylasShapesLineProxy.h"
#import "AkylasShapesPieSliceProxy.h"
#import "TiUIView+ShapeMask.h"
#import "NSData+Additions.h"

@interface TiUIView ()
+ (void) swizzleShapeMask;

@end

@implementation AkylasShapesModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"763bf950-a27d-4af1-8dc8-046026e52956";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.shapes";
}

#pragma mark Lifecycle

-(void)startup
{
    [TiUIView swizzleShapeMask];
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.View", [AkylasShapesViewProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.RoundedRect", [AkylasShapesRoundedRectProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.Rect", [AkylasShapesRectProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.PieSlice", [AkylasShapesPieSliceProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.Line", [AkylasShapesLineProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.Ellipse", [AkylasShapesEllipseProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.Circle", [AkylasShapesCircleProxy class]);
//    CFDictionarySetValue([TiProxy classNameLookup], @"AkylasShapes.Arc", [AkylasShapesArcProxy class]);
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
}

#pragma mark Password
-(NSString*)getPasswordKey {
    return @"akylas.modules.key";
}
-(NSString*) getPassword {
    return stringWithHexString(@"7265745b496b2466553b486f736b7b4f");
}

#pragma Public APIs

MAKE_SYSTEM_PROP(CAP_BUTT,kCGLineCapButt);
MAKE_SYSTEM_PROP(CAP_ROUND,kCGLineCapRound);
MAKE_SYSTEM_PROP(CAP_SQUARE,kCGLineCapSquare);
MAKE_SYSTEM_PROP(JOIN_MITER,kCGLineJoinMiter);
MAKE_SYSTEM_PROP(JOIN_ROUND,kCGLineJoinRound);
MAKE_SYSTEM_PROP(JOIN_BEVEL,kCGLineJoinBevel);

MAKE_SYSTEM_PROP(HORIZONTAL,0);
MAKE_SYSTEM_PROP(VERTICAL,1);

MAKE_SYSTEM_PROP(CW,YES);
MAKE_SYSTEM_PROP(CCW,NO);

MAKE_SYSTEM_PROP(TOP_MIDDLE,ShapeAnchorTopMiddle);
MAKE_SYSTEM_PROP(LEFT_TOP,ShapeAnchorLeftTop);
MAKE_SYSTEM_PROP(LEFT_MIDDLE,ShapeAnchorLeftMiddle);
MAKE_SYSTEM_PROP(LEFT_BOTTOM,ShapeAnchorLeftBottom);
MAKE_SYSTEM_PROP(RIGHT_TOP,ShapeAnchorRightTop);
MAKE_SYSTEM_PROP(RIGHT_MIDDLE,ShapeAnchorRightMiddle);
MAKE_SYSTEM_PROP(RIGHT_BOTTOM,ShapeAnchorRightBottom);
MAKE_SYSTEM_PROP(BOTTOM_MIDDLE,ShapeAnchorBottomMiddle);
MAKE_SYSTEM_PROP(CENTER,ShapeAnchorCenter);

MAKE_SYSTEM_PROP(OP_RECT,ShapeOpRect);
MAKE_SYSTEM_PROP(OP_ROUNDRECT,ShapeOpRoundedRect);
MAKE_SYSTEM_PROP(OP_CIRCLE,ShapeOpCircle);
MAKE_SYSTEM_PROP(OP_ELLIPSE,ShapeOpEllipse);
MAKE_SYSTEM_PROP(OP_POINTS,ShapeOpPoints);
MAKE_SYSTEM_PROP(OP_ARC,ShapeOpArc);

-(id)createAnimation:(id)args
{
	if (args!=nil && [args isKindOfClass:[NSArray class]])
	{
		id properties = [args objectAtIndex:0];
		id callback = [args count] > 1 ? [args objectAtIndex:1] : nil;
		ENSURE_TYPE_OR_NIL(callback,KrollCallback);
		if ([properties isKindOfClass:[NSDictionary class]])
		{
			TiAnimation *a = [[[TiAnimation alloc] initWithDictionary:properties context:[self pageContext] callback:callback] autorelease];
			return a;
		}
	}
	return [[[TiAnimation alloc] _initWithPageContext:[self executionContext]] autorelease];
}

@end
