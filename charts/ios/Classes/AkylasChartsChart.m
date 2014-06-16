/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsChart.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"
#import "TiBase.h"
#import "AkylasChartsChartProxy.h"
#import "AkylasChartsPlotProxy.h"
#import "AkylasChartsMarkerAnnotation.h"
#import "AkylasChartsMarkerProxy.h"

@implementation AkylasChartsChart

@synthesize hostingView;

-(id)init
{
	if (self = [super init])
	{
		_needsConfigureGraph = YES;
        _needsConfigureLegend = YES;
        _needsConfigureChart = YES;
        _needsConfigureHost = YES;
        _needsConfigureTheme = YES;
        _needsConfigureTitle = YES;
        _needsConfigureFill = YES;
        _needsConfigurePadding = YES;
        _needsConfigurePlotArea = YES;
    }
	return self;
}


-(void)killGraph
{
	if (hostingView) {
		if (symbolTextAnnotation) {
			[graph.plotAreaFrame.plotArea removeAnnotation:symbolTextAnnotation];
			RELEASE_TO_NIL(symbolTextAnnotation);
		}
		
		[hostingView removeFromSuperview];
		hostingView.hostedGraph = nil;
		RELEASE_TO_NIL(hostingView);
	}
	RELEASE_TO_NIL(graph);
}

-(void)dealloc
{
	[self killGraph];
	[super dealloc];
}

-(CPTGraph*)graph {
    if (!graph && configurationSet) {
        [self initPlot];
        [self configurePlot:[self.proxy allProperties]];

    }
    return graph;
}

-(void)frameSizeChanged:(CGRect)frame bounds:(CGRect)bounds
{
	[super frameSizeChanged:frame bounds:bounds];
    
	if (hostingView != nil) {
		[TiUtils setView:hostingView positionRect:bounds];
	}
	[self refreshPlotSpaces];
}

-(void)configureTitle:(NSDictionary*)properties
{
	// NOTE: For some reason, directly setting the text style properties
	// using graph.titleTextStyle.xxxxxx does not work properly. It works
	// best by creating a new textStyle object, setting the properties of
	// this object, and then assigning it to the graph.titleTextStyle property
	
	// Configure the font name and size and color
	[self graph].titleTextStyle  = [AkylasChartsParsers parseTextStyle:properties def:graph.titleTextStyle];
	
	// The frame anchor defines the location of the title
	graph.titlePlotAreaFrameAnchor = [TiUtils intValue:@"location" properties:properties def:CPTRectAnchorTop];
	
	// The displacement defines the offset from the specified edge
	NSDictionary* offset = [properties objectForKey:@"offset"];
	if (offset) {
		graph.titleDisplacement = CGPointMake(
		  [TiUtils floatValue:@"x" properties:offset def:0.0],
		  [TiUtils floatValue:@"y" properties:offset def:0.0]);
	} else if (graph.title == nil) {
		graph.titleDisplacement = CGPointZero;
	} else {
		graph.titleDisplacement = CGPointMake(0.0f, graph.titleTextStyle.fontSize);
	}
	
	// Set the title after setting the font. For some reason, core-plot will crash on
	// the iPad simulator if the title is set before the font.
	graph.title = [TiUtils stringValue:@"text" properties:properties def:nil];
}
	
-(void)configurePadding:(NSDictionary*)properties
{	
	[self graph].paddingLeft = [TiUtils floatValue:@"left" properties:properties def:0];
	graph.paddingTop = [TiUtils floatValue:@"top" properties:properties def: 0];
	graph.paddingRight = [TiUtils floatValue:@"right" properties:properties def:0];
	graph.paddingBottom = [TiUtils floatValue:@"bottom" properties:properties def:0];
}

-(void)configureThemeWithName:(NSString*)themeName
{
	if (themeName != nil) {
		CPTTheme *theme = [CPTTheme themeNamed:themeName];
		if (theme != nil) {
			[graph applyTheme:theme];
			return;
		}
	}
	
	// Apply the default theme -- this also sets up default values for
	// a number of parameters of the graph.
//	[graph applyTheme:[CPTTheme themeNamed:kCPTDarkGradientTheme]];
}

-(void)configurePlotArea:(NSDictionary*)properties
{
    // Border
	[self graph].plotAreaFrame.borderLineStyle = [AkylasChartsParsers parseLineColor:[properties objectForKey:@"borderColor"]
																withWidth:[properties objectForKey:@"borderWidth"]
                                                             withGradient:[properties objectForKey:@"borderGradient"]
															   andOpacity:[properties objectForKey:@"borderOpacity"]
																	  def:nil];
    graph.plotAreaFrame.cornerRadius = [TiUtils floatValue:@"borderRadius" properties:properties def:0];
	
	// Inner padding
	NSDictionary *padding = [properties objectForKey:@"padding"];
	if (padding != nil) {
		graph.plotAreaFrame.paddingLeft = [TiUtils floatValue:@"left" properties:padding def:0];
		graph.plotAreaFrame.paddingTop = [TiUtils floatValue:@"top" properties:padding def:0];
		graph.plotAreaFrame.paddingRight = [TiUtils floatValue:@"right" properties:padding def:0];
		graph.plotAreaFrame.paddingBottom = [TiUtils floatValue:@"bottom" properties:padding def:0];
	}
	
	// Plot area frame fill
	graph.plotAreaFrame.fill = [AkylasChartsParsers parseFillColor:[properties objectForKey:@"backgroundColor"]
												  withGradient:[properties objectForKey:@"backgroundGradient"]
													andOpacity:[properties objectForKey:@"backgroundOpacity"]
														   def:nil];
    
    // Plot area fill
    graph.plotAreaFrame.plotArea.fill = [AkylasChartsParsers parseFillColor:[properties objectForKey:@"fillColor"]
                                                           withGradient:[properties objectForKey:@"fillGradient"]
                                                             andOpacity:[properties objectForKey:@"fillOpacity"]
                                                                    def:nil];
}

-(void)removeAllPlots
{
	if (graph != nil) {
		for (id plot in ((AkylasChartsChartProxy*)self.proxy).plots) {
			[plot removeFromChart:graph];
		}
	}
}


-(void)addPlot:(id)plot
{
	if ([plot respondsToSelector:@selector(renderInChart:)]) {
		[plot renderInChart:[self graph]];
	}
}

-(void)removePlot:(id)plot
{
    if (graph != nil && [plot respondsToSelector:@selector(removeFromChart:)]) {
		[plot removeFromChart:graph];
	}
}

-(void)configurationSet
{
    [super configurationSet];
    if (graph != nil) {
		for (id plot in ((AkylasChartsChartProxy*)self.proxy).plots) {
			[plot configurePlot: [plot allProperties]];
		}
	}
    [self configurePlot:[self.proxy allProperties]];
}

-(void)refreshPlotSpaces
{
//    for (AkylasChartsMarkerAnnotation* marker in ((AkylasChartsChartProxy*)self.proxy).markers) {
//        CPTAnnotation *annot = marker.layer;
//        annot.contentLayer/.frame = CGRectMake(newXCoord, newYCoord, logoWidth, logoHeight);
//    }
}

-(CPTGraph*)newGraph
{
    return [[CPTGraph alloc] initWithFrame:CGRectZero];
}

#pragma mark - Chart behavior
-(void)initPlot {
    hostingView = [[CPTGraphHostingView alloc] initWithFrame:[self bounds]];
    [self addSubview:hostingView];
    
    // Create graph object
    graph = [self newGraph];
    [graph setTopDownLayerOrder:[NSArray arrayWithObjects:
                                        [NSNumber numberWithInt:CPTGraphLayerTypeAxisTitles],
                                        [NSNumber numberWithInt:CPTGraphLayerTypeAxisLabels],
                                        [NSNumber numberWithInt:CPTGraphLayerTypeAxisLines],
                                        [NSNumber numberWithInt:CPTGraphLayerTypePlots],
                                        [NSNumber numberWithInt:CPTGraphLayerTypeMajorGridLines],
                                        [NSNumber numberWithInt:CPTGraphLayerTypeMinorGridLines],
                                        nil]];
    hostingView.hostedGraph = graph;
    hostingView.collapsesLayers = NO; // Setting to YES reduces GPU memory usage, but can slow drawing/scrolling
}

-(void)configurePlot:(NSDictionary*)props {
    if (_needsConfigureHost) {
        _needsConfigureHost = NO;
        [self configureHost:props];
    }
    if (_needsConfigureGraph) {
        _needsConfigureGraph = NO;
        [self configureGraph:props];
    }
    if (_needsConfigureChart) {
        _needsConfigureChart = NO;
        [self configureChart:props];
    }
    if (_needsConfigureLegend) {
        _needsConfigureLegend = NO;
        [self configureLegend:props];
    }
    if (_needsConfigureTheme) {
        _needsConfigureTheme = NO;
        [self configureTheme:props];
    }
}

-(void)configureHost:(NSDictionary*)props {
}

-(void)configureGraph:(NSDictionary*)props
{
    
    
    
    // Background fill
    if (_needsConfigureFill) {
        _needsConfigureFill = NO;
        graph.fill = [AkylasChartsParsers parseFillColor:[props objectForKey:@"fillColor"]
                                    withGradient:[props objectForKey:@"fillGradient"]
                                      andOpacity:[props objectForKey:@"fillOpacity"]
                                             def:nil];
    }
    // Configure the graph title area
    if (_needsConfigureTitle) {
        _needsConfigureTitle = NO;
        [self configureTitle:[props objectForKey:@"title"]];
    }
	if (_needsConfigurePadding) {
        _needsConfigurePadding = NO;
	// Configure the padding on the outside of the graph
        [self configurePadding:[props objectForKey:@"padding"]];
    }
    
    if (_needsConfigurePlotArea) {
        _needsConfigurePlotArea = NO;
    // Configure the frame and inside padding for the graph
        [self configurePlotArea:[props objectForKey:@"plotArea"]];
    }
}

-(void)configureChart:(NSDictionary*)props {
}

-(void)configureLegend:(NSDictionary*)props {
}

-(void)configureTheme:(NSDictionary*)props {
    // Configure theme first -- it may set default options for the
	// entire graph, which would override any other settings if we don't
	// process it first
	[self configureThemeWithName:[props objectForKey:@"theme"]];
}

-(void)setTitle_:(id)value
{
	[self configureTitle:value];
}

-(void)setPadding_:(id)value
{
	[self configurePadding:value];
}

-(void)setPlotArea_:(id)value
{
	[self configurePlotArea:value];
}

-(void)refresh:(id)args
{
	[[self hostingView] setNeedsDisplay];
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	// On rotation, re-render the view
	[self refreshPlotSpaces];
}

-(CGFloat) getAvailableWidth {
    return hostingView.frame.size.width - graph.plotAreaFrame.paddingLeft - graph.plotAreaFrame.paddingRight - graph.paddingLeft - graph.paddingRight;
}

-(CGFloat) getAvailableHeight {
    return hostingView.frame.size.height - graph.paddingTop - graph.paddingBottom - graph.plotAreaFrame.paddingTop - graph.plotAreaFrame.paddingBottom;
}

-(void)removeAllMarkers
{
	if (graph != nil) {
		for (AkylasChartsMarkerProxy* annotation in ((AkylasChartsChartProxy*)self.proxy).markers) {
			[graph.plotAreaFrame.plotArea removeAnnotation:[annotation marker]];
		}
	}
}


-(void)addMarker:(AkylasChartsMarkerProxy*)marker
{
    ENSURE_UI_THREAD_1_ARG(marker)
	if (graph != nil) {
		[graph.plotAreaFrame.plotArea addAnnotation:[marker getMarkerAnnotationForGraph:graph.defaultPlotSpace]];
	}
}

-(void)removeMarker:(AkylasChartsMarkerAnnotation*)marker
{
    if (graph != nil) {
		[graph.plotAreaFrame.plotArea removeAnnotation:marker];
	}
}


@end
