/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPieChart.h"
#import "AkylasChartsPieChartProxy.h"
#import "AkylasChartsParsers.h"
#import "AkylasChartsChartProxy.h"
#import "AkylasChartsPieSegmentProxy.h"


@implementation AkylasChartsPieChart


#pragma mark CPTPlotSpaceDelegate methods

-(CPTGraphHostingView*)hostingView
{
	if (hostingView == nil) {
		[super hostingView];
	}
	
	return hostingView;
}


-(void)dealloc
{
	RELEASE_TO_NIL(pieChart);
	[super dealloc];
}

-(CPTGraph*)newGraph
{
    return [[CPTXYGraph alloc] initWithFrame:CGRectZero];
}

-(void)initPlot
{
    [super initPlot];
    RELEASE_TO_NIL(pieChart);
    pieChart = [[CPTPieChart alloc] init];
    pieChart.dataSource = (AkylasChartsPieChartProxy*)self.proxy;
    pieChart.delegate = (AkylasChartsPieChartProxy*)self.proxy;
    [graph addPlot:pieChart];
}

-(void)configureChart
{
    [super configureChart];

    pieChart.identifier = graph.title;
    pieChart.shadow = [AkylasChartsParsers parseShadow:@"shadow" inProperties:self.proxy def:nil];
    
    float startDegrees = 90 + [TiUtils floatValue:[self.proxy valueForKey:@"startAngle"] def:0.0f];
    float endDegrees = 90 + [TiUtils floatValue:[self.proxy valueForKey:@"endAngle"] def:360.0f];
    pieChart.startAngle = fmod(startDegrees, 360.0f) * M_PI / 180 ;
    pieChart.endAngle = fmod(endDegrees, 360.0f) * M_PI / 180 ;

    pieChart.sliceDirection = CPTPieDirectionClockwise;
    
    pieChart.overlayFill = [AkylasChartsParsers parseFillColor:[self.proxy valueForKey:@"overlayColor"]
                                              withGradient:[self.proxy valueForKey:@"overlayGradient"]
                                                andOpacity:[self.proxy valueForKey:@"overlayOpacity"]
                                                       def:nil];
	pieChart.borderLineStyle = [AkylasChartsParsers parseLineColor:[self.proxy valueForKey:@"borderColor"]
                                                     withWidth:[self.proxy valueForKey:@"borderWidth"]
                                                  withGradient:[self.proxy valueForKey:@"borderGradient"]
                                                    andOpacity:[self.proxy valueForKey:@"borderOpacity"]
                                                           def:nil];
}

-(void)configureGraph:(NSDictionary*)props
{
    [super configureGraph:props];
    graph.axisSet = nil;
}


-(void)refreshPlotSpaces
{
    pieChart.pieRadius = 0.95 * MIN([self getAvailableWidth] / 2.0, [self getAvailableHeight] / 2.0);
    CGFloat innerRadius = TiDimensionCalculateValue([TiUtils dimensionValue:[self.proxy valueForKey:@"donutSize"]], pieChart.pieRadius);
    if (innerRadius < 0) {
        innerRadius = pieChart.pieRadius + innerRadius;
    }
    pieChart.pieInnerRadius = innerRadius;
    [super refreshPlotSpaces];
}

-(CGPoint)viewPointFromGraphPoint:(CGPoint)point
{
    // Convert the point from the graph's coordinate system to the view. Note that the
    // graph's coordinate system has (0,0) in the lower left hand corner and the
    // view's coordinate system has (0,0) in the upper right hand corner
    CGPoint viewPoint = [self.hostingView.hostedGraph convertPoint:point toLayer:self.hostingView.layer];
    return viewPoint;
}

-(void)notifyOfTouchEvent:(NSString*)type atPoint:(CGPoint)viewPoint
{
	if ([self.proxy _hasListeners:type]) {
        NSDictionary *evt = [NSDictionary dictionaryWithObjectsAndKeys:
                             NUMFLOAT(viewPoint.x), @"x",
                             NUMFLOAT(viewPoint.y), @"y",
                             nil
                             ];
        [self.proxy fireEvent:type withObject:evt];
    }
}
@end
