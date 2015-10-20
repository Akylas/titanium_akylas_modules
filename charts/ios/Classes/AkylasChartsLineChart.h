/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsChart.h"


@interface AkylasChartsLineChart : AkylasChartsChart <CPTPlotSpaceDelegate>{
@private
    BOOL                userInteractionEnabled;
    BOOL                panEnabled;
    BOOL                zoomEnabled;
    BOOL                clampInteraction;
    float               minXValue;
    float               maxXValue;
    float               minYValue;
    float               maxYValue;
}
@end
