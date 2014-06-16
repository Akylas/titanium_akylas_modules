/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotStepProxy.h"
#import "AkylasChartsChart.h"
#import "AkylasChartsParsers.h"
#import "TiUtils.h"

@implementation AkylasChartsPlotStepProxy

-(void)configurePlot:(NSDictionary*)props
{
	[super configurePlot:props];
	
	CPTScatterPlot* plot = (CPTScatterPlot*)[self plot];
	plot.interpolation = CPTScatterPlotInterpolationStepped;
}

-(id)init
{
	if (self = [super init]) {
	}
	
	return self;
}

-(void)dealloc
{
	[super dealloc];
}


@end
