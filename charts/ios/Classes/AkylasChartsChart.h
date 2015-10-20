/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "TiUIView.h"

#import "CorePlot-CocoaTouch.h"

@class AkylasChartsPlotProxy;

@interface AkylasChartsChart : TiUIView {

@protected
	CPTGraphHostingView	*hostingView;
	CPTGraph			*graph;
	CPTLayerAnnotation	*symbolTextAnnotation;
    BOOL _needsConfigureGraph;
    BOOL _needsConfigureLegend;
    BOOL _needsConfigureChart;
    BOOL _needsConfigureHost;
    BOOL _needsConfigureTheme;
    BOOL _needsConfigureTitle;
    BOOL _needsConfigureFill;
    BOOL _needsConfigurePadding;
    BOOL _needsConfigurePlotArea;
}
@property(nonatomic,readonly) CPTGraphHostingView* hostingView;

-(void)configurePlot:(NSDictionary*)props;
-(void)initPlot;
-(void)configureHost:(NSDictionary*)propst;
-(void)configureGraph:(NSDictionary*)props;
-(void)configureChart:(NSDictionary*)props;
-(void)configureLegend:(NSDictionary*)props;
-(void)configureTheme:(NSDictionary*)props;

-(void)removeAllPlots;
-(void)addPlot:(id)plot;
-(void)removePlot:(id)plot;
-(void)refreshPlotSpaces;

-(CGFloat) getAvailableWidth;
-(CGFloat) getAvailableHeight;

-(void)removeAllMarkers;
-(void)addMarker:(id)marker;
-(void)removeMarker:(id)marker;

-(CGPoint)viewPointFromGraphPoint:(CGPoint)point;
-(void)notifyOfTouchEvent:(NSString*)type atPoint:(CGPoint)viewPoint;
-(NSMutableDictionary*)eventDictAtPoint:(CGPoint)point onSpace:(CPTPlotSpace *)space;

@end
