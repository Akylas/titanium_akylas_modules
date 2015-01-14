/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */

#import "AkylasChartsModule.h"

#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"
#import "CorePlot-CocoaTouch.h"

typedef enum {
	kPlotTypeBar,
    kPlotTypeLine,
	kPlotTypePie
} ChartPlotType;

@implementation AkylasChartsModule
#pragma mark Public Constants


MAKE_SYSTEM_PROP(LOCATION_TOP,CPTRectAnchorTop);
MAKE_SYSTEM_PROP(LOCATION_BOTTOM,CPTRectAnchorBottom);
MAKE_SYSTEM_PROP(LOCATION_LEFT,CPTRectAnchorLeft);
MAKE_SYSTEM_PROP(LOCATION_RIGHT,CPTRectAnchorRight);
MAKE_SYSTEM_PROP(LOCATION_TOP_LEFT,CPTRectAnchorTopLeft);
MAKE_SYSTEM_PROP(LOCATION_TOP_RIGHT,CPTRectAnchorTopRight);
MAKE_SYSTEM_PROP(LOCATION_BOTTOM_LEFT,CPTRectAnchorBottomLeft);
MAKE_SYSTEM_PROP(LOCATION_BOTTOM_RIGHT,CPTRectAnchorBottomRight);
MAKE_SYSTEM_PROP(LOCATION_CENTER,CPTRectAnchorCenter);

MAKE_SYSTEM_STR(THEME_DARK_GRADIENT,kCPTDarkGradientTheme);
MAKE_SYSTEM_STR(THEME_WHITE,kCPTPlainWhiteTheme);
MAKE_SYSTEM_STR(THEME_BLACK,kCPTPlainBlackTheme);
MAKE_SYSTEM_STR(THEME_SLATE,kCPTSlateTheme);
MAKE_SYSTEM_STR(THEME_STOCKS,kCPTStocksTheme);

MAKE_SYSTEM_PROP(SIGN_POSITIVE,CPTSignPositive);
MAKE_SYSTEM_PROP(SIGN_NEGATIVE,CPTSignNegative);

MAKE_SYSTEM_PROP(DIRECTION_HORIZONTAL,YES);
MAKE_SYSTEM_PROP(DIRECTION_VERTICAL,NO);

MAKE_SYSTEM_PROP(SYMBOL_NONE,CPTPlotSymbolTypeNone);
MAKE_SYSTEM_PROP(SYMBOL_RECTANGLE,CPTPlotSymbolTypeRectangle);
MAKE_SYSTEM_PROP(SYMBOL_ELLIPSE,CPTPlotSymbolTypeEllipse);
MAKE_SYSTEM_PROP(SYMBOL_DIAMOND,CPTPlotSymbolTypeDiamond);
MAKE_SYSTEM_PROP(SYMBOL_TRIANGLE,CPTPlotSymbolTypeTriangle);
MAKE_SYSTEM_PROP(SYMBOL_STAR,CPTPlotSymbolTypeStar);
MAKE_SYSTEM_PROP(SYMBOL_PENTAGON,CPTPlotSymbolTypePentagon);
MAKE_SYSTEM_PROP(SYMBOL_HEXAGON,CPTPlotSymbolTypeHexagon);
MAKE_SYSTEM_PROP(SYMBOL_CROSS,CPTPlotSymbolTypeCross);
MAKE_SYSTEM_PROP(SYMBOL_PLUS,CPTPlotSymbolTypePlus);
MAKE_SYSTEM_PROP(SYMBOL_DASH,CPTPlotSymbolTypeDash);
MAKE_SYSTEM_PROP(SYMBOL_SNOW,CPTPlotSymbolTypeSnow);

MAKE_SYSTEM_PROP(DIRECTION_CLOCKWISE,CPTPieDirectionClockwise);
MAKE_SYSTEM_PROP(DIRECTION_COUNTERCLOCKWISE,CPTPieDirectionCounterClockwise);

MAKE_SYSTEM_PROP(PLOT_BAR,kPlotTypeBar);
MAKE_SYSTEM_PROP(PLOT_LINE,kPlotTypeLine);
MAKE_SYSTEM_PROP(PLOT_PIE,kPlotTypePie);

MAKE_SYSTEM_PROP(ALIGNMENT_LEFT,CPTAlignmentLeft);
MAKE_SYSTEM_PROP(ALIGNMENT_CENTER,CPTAlignmentCenter);
MAKE_SYSTEM_PROP(ALIGNMENT_RIGHT,CPTAlignmentRight);
MAKE_SYSTEM_PROP(ALIGNMENT_TOP,CPTAlignmentTop);
MAKE_SYSTEM_PROP(ALIGNMENT_MIDDLE,CPTAlignmentMiddle);
MAKE_SYSTEM_PROP(ALIGNMENT_BOTTOM,CPTAlignmentBottom);

MAKE_SYSTEM_PROP(SCATTER_LINEAR,CPTScatterPlotInterpolationLinear);
MAKE_SYSTEM_PROP(SCATTER_STEPPED,CPTScatterPlotInterpolationStepped);
MAKE_SYSTEM_PROP(SCATTER_HISTOGRAM,CPTScatterPlotInterpolationHistogram);

MAKE_SYSTEM_PROP(CAP_BUTT,kCGLineCapButt);
MAKE_SYSTEM_PROP(CAP_ROUND,kCGLineCapRound);
MAKE_SYSTEM_PROP(CAP_SQUARE,kCGLineCapSquare);
MAKE_SYSTEM_PROP(JOIN_MITER,kCGLineJoinMiter);
MAKE_SYSTEM_PROP(JOIN_ROUND,kCGLineCapRound);
MAKE_SYSTEM_PROP(JOIN_BEVEL,kCGLineJoinBevel);

MAKE_SYSTEM_PROP(HORIZONTAL,0);
MAKE_SYSTEM_PROP(VERTICAL,1);

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"2dfe3e23-ceca-405e-b119-04eca831949b";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"akylas.charts";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
	
	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup 

-(void)dealloc
{
	// release any resources that have been retained by the module
	[super dealloc];
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

//-(void)_listenerAdded:(NSString *)type count:(NSInteger)count
//{
//	if (count == 1 && [type isEqualToString:@"my_event"])
//	{
//		// the first (of potentially many) listener is being added 
//		// for event named 'my_event'
//	}
//}
//
//-(void)_listenerRemoved:(NSString *)type count:(NSInteger)count
//{
//	if (count == 0 && [type isEqualToString:@"my_event"])
//	{
//		// the last listener called for event named 'my_event' has
//		// been removed, we can optionally clean up any resources
//		// since no body is listening at this point for that event
//	}
//}

#pragma Public APIs

-(id)example:(id)args
{
	// example method
	return @"hello world";
}

-(id)exampleProp
{
	// example property getter
	return @"hello world";
}

-(void)setExampleProp:(id)value
{
	// example property setter
}

@end
