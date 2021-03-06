/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsScatterPlotProxy.h"
	
@interface AkylasChartsPlotLineProxy : AkylasChartsScatterPlotProxy <CPTScatterPlotDelegate, CPTScatterPlotDataSource> {
	
@private
    NSUInteger highlightSymbolIndex;
    CPTPlotSymbol* highlightSymbol;
}

@end
