/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotProxy.h"

@interface AkylasChartsPieSegmentProxy : TiProxy {
	
@private
	CPTFill             *fill;
	CPTLineStyle        *border;
	CPTTextStyle        *labelStyle;
	NSNumber            *value;
	NSString			*title;
    CGFloat             explodeOffset;
}
@property(nonatomic,readwrite,retain) AkylasChartsChartProxy* chartProxy;
@property(nonatomic,readwrite,retain) CPTFill *fill;
@property(nonatomic,readwrite,retain) CPTLineStyle *border;
@property(nonatomic,readwrite,retain) CPTTextStyle *labelStyle;
@property(nonatomic,readwrite,retain) NSNumber *value;
@property(nonatomic,readwrite,retain) NSString *title;
@property(nonatomic,readwrite,assign) CGFloat explodeOffset;

-(void)renderInChart:(CPTGraph*)graph;
-(void)removeFromChart:(CPTGraph*)graph;

@end
