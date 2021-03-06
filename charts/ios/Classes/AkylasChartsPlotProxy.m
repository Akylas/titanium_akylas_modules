/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsPlotProxy.h"
#import "AkylasChartsChartProxy.h"
#import "AkylasChartsChart.h"
#import "TiUtils.h"
#import <libkern/OSAtomic.h>

// Define deferred processing bits
#define NEEDS_UPDATE_DATA	1
#define NEEDS_RECONFIGURE   2

@implementation AkylasChartsPlotProxy
@synthesize dataKey, propertyChangedProperties, plot;
@synthesize minXValue, minYValue, maxXValue, maxYValue;

-(CPTPlot*)allocPlot
{
	// Override this method
	return nil;
}

-(void)configurePlot:(NSDictionary*)props
{
	// Override this method
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    minXValue = 0;
    minYValue = 0;
    maxXValue = 0;
    maxYValue = 0;
//	self.modelDelegate = self;
    
	[super _initWithProperties:properties];
	// Set up for property change notifications
    
}

-(void)dealloc
{
	RELEASE_TO_NIL(plot);
	RELEASE_TO_NIL(dataX);
	RELEASE_TO_NIL(dataY);
	RELEASE_TO_NIL(dataKey);
	RELEASE_TO_NIL(propertyChangedProperties);
	[super dealloc];
}

-(void)removeFromChart:(CPTGraph*)fromGraph
{
	if (plot != nil) {
		[fromGraph removePlot:plot];
	}
}

-(void)renderInChart:(CPTGraph*)toGraph
{
	RELEASE_TO_NIL(plot);

	plot = [self allocPlot];
	if (plot == nil) {
		return;
	}
	
	[self configurePlot: [self allProperties]];
    
    if (self.bindId) {
        plot.identifier = self.bindId;
    }
	
	// Make sure to set the frame to match the graph
	plot.frame = [toGraph frame];

	// Set up data source and delegate for plotting
    plot.dataSource = self;
	plot.delegate = self;
	
	// Add the plot to the plot space
    [toGraph addPlot:plot toPlotSpace:toGraph.defaultPlotSpace];	
}

-(NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot
{
    @synchronized(dataY) {
        return [dataY count];
    }
}

-(NSNumber*)numberForPlot:(NSUInteger)index
{
    @synchronized(dataY) {
        if (index < [dataY count]) {
            return [dataY objectAtIndex:index];
        }
    }
	return nil;
}


-(NSUInteger)indexForXValue:(CGFloat)value {
    __block NSUInteger result = NSNotFound;
    @synchronized(dataX) {
        __block NSNumber* last = nil;
        [dataX enumerateObjectsUsingBlock:^(NSNumber* number, NSUInteger idx, BOOL *stop) {
            if (number.floatValue > value) {
                result = idx;
                if (last && (fabs(last.floatValue - value) < fabs(number.floatValue - value))) {
                    result = idx - 1;
                }
                *stop = YES;
            }
            last = number;
        }];
    }
    return result;
}

-(NSArray*)rangeForXValue:(CGFloat)value {
    __block NSArray* result = nil;
    @synchronized(dataX) {
        __block NSInteger last = -1;
        [dataX enumerateObjectsUsingBlock:^(NSNumber* number, NSUInteger idx, BOOL *stop) {
            if (number.floatValue > value) {
                if (last > -1) {
                    result = [[NSArray alloc] initWithObjects:@(last), @(idx), nil];
                } else {
                    result = [[NSArray alloc] initWithObjects:@(idx), nil];
                }
                *stop = YES;
            }
            last = idx;
        }];
    }
    return [result autorelease];
}




-(NSArray*)numbersForPlotRange:(NSRange)indexRange forCoordinate:(CPTCoordinate)coordinate
{
    if (coordinate == CPTScatterPlotFieldY) {
        @synchronized(dataY) {
            return [dataY subarrayWithRange:indexRange];
        }
    }
    else {
        @synchronized(dataX) {
            return [dataX subarrayWithRange:indexRange];
        }
    }
    
    return nil;
}

-(NSNumber*)numberForPlot:(NSUInteger)index forCoordinate:(CPTCoordinate)coordinate
{
    if (coordinate == CPTScatterPlotFieldY) {
        @synchronized(dataY) {
            return [dataY objectAtIndex:index];
        }
    }
    else {
        @synchronized(dataX) {
            return [dataX objectAtIndex:index];
        }
    }
    
    return nil;
}

-(NSArray*)dataY
{
    @synchronized(dataY) {
        return dataY;
    }
}

-(NSArray*)dataX
{
    @synchronized(dataX) {
        return dataX;
    }
}

-(void)refreshData
{
	// Override this method if you need to perform any
	// action after the data has been updated
	
	// MUST RUN ON UI THREAD SO THAT UPDATE IS IMMEDIATE
	ENSURE_UI_THREAD_0_ARGS
	
	OSAtomicTestAndClearBarrier(NEEDS_UPDATE_DATA, &dirtyDataFlags);

	// Tell the plot that it needs to reload the data. This causes it
	// to clear its cache of data and requery the values from the delegate.
	[plot reloadData];

	[(AkylasChartsChartProxy*)parent refreshPlotSpaces];
}

-(void)triggerDataUpdate
{	
	if (OSAtomicTestAndSetBarrier(NEEDS_UPDATE_DATA, &dirtyDataFlags)) {
		return;
	}
	
	[self performSelectorOnMainThread:@selector(refreshData) withObject:nil waitUntilDone:YES];
}


-(void)reconfigurePlot
{
	// MUST RUN ON UI THREAD SO THAT UPDATE IS IMMEDIATE
	ENSURE_UI_THREAD_0_ARGS
	
	OSAtomicTestAndClearBarrier(NEEDS_RECONFIGURE, &dirtyDataFlags);
	
	[self configurePlot:[self allProperties]];
}

-(void)triggerReconfigure
{
	if (OSAtomicTestAndSetBarrier(NEEDS_RECONFIGURE, &dirtyDataFlags)) {
		return;
	}
	
	[self performSelectorOnMainThread:@selector(reconfigurePlot) withObject:nil waitUntilDone:NO];
}

-(void)addData:(NSArray*)values startingAtIndex:(NSUInteger)index
{
	if (values != nil) {
//		Class dictionaryClass = [NSDictionary class];
		Class arrayClass = [NSArray class];
		NSUInteger length = [values count];
        if (dataX == nil) {
            dataX = [[NSMutableArray arrayWithCapacity:length] retain];
        }
        if (dataY == nil) {
            dataY = [[NSMutableArray arrayWithCapacity:length] retain];
        }
		
        @synchronized(dataX) {
            @synchronized(dataY) {
               if([[values objectAtIndex:0] isKindOfClass:arrayClass]) {
                    NSArray* xs = [values objectAtIndex:0];
                    NSArray* ys = [values objectAtIndex:1];
                    length = [xs count];
                    
                    for (int i = 0; i < length; i++) {
                        id x = [xs objectAtIndex:i];
                        id y = [ys objectAtIndex:i];
                        if (x && ![x isEqual:[NSNull null]]) {
                            float floatX = [x floatValue];
                            float floatY = [y floatValue];
                            if (floatX < minXValue)
                                minXValue = floatX;
                            else if (floatX > maxXValue)
                                maxXValue = floatX;
                            if (floatY < minYValue)
                                minYValue = floatY;
                            else if (floatY > maxYValue)
                                maxYValue = floatY;
                            [dataX insertObject:x atIndex:index+i];
                            [dataY insertObject:y atIndex:index+i];
                        }
                        else {
                            float floatY = [TiUtils floatValue:y];
                            if (floatY < minYValue)
                                minYValue = floatY;
                            else if (floatY > maxYValue)
                                maxYValue = floatY;
                            [dataY insertObject:y atIndex:index+i];
                        }
                    }
                }
                else {
                    for (int i = 0; i < length; i++) {
                        id y = [values objectAtIndex:i];
                        float floatY = [y floatValue];
                        if (floatY < minYValue)
                            minYValue = floatY;
                        else if (floatY > maxYValue)
                            maxYValue = floatY;
                        [dataX insertObject:y atIndex:index+i];
                    }
                }
            }
        }
	}
}

-(void)setData:(id)values
{
	// Release any existing data values
	RELEASE_TO_NIL(dataX);
	RELEASE_TO_NIL(dataY);
	
	[self addData:values startingAtIndex:0];
	
	// Signal that the data needs to be reloaded
	[self triggerDataUpdate];
    [self replaceValue:values forKey:@"data" notification:NO];
}

-(void)appendData:(id)values
{
	ENSURE_SINGLE_ARG(values,NSArray);
	
	[self addData:values startingAtIndex:[dataX count]];
	
	// Signal that the data needs to be reloaded
	[self triggerDataUpdate];
}

-(void)insertDataBefore:(id)args
{
	enum InsertDataArgs {
		kInsertDataArgIndex  = 0,
		kInsertDataArgValues  = 1,
		kInsertDataArgCount
	};	
	
	// Validate arguments
	ENSURE_ARG_COUNT(args, kInsertDataArgCount);
	
	NSInteger index = [TiUtils intValue:[args objectAtIndex:kInsertDataArgIndex]];
	NSArray *values = [args objectAtIndex:kInsertDataArgValues];
	
	[self addData:values startingAtIndex:index];
	
	// Signal that the data needs to be reloaded
	[self triggerDataUpdate];
}

-(void)insertDataAfter:(id)args
{
	enum InsertDataArgs {
		kInsertDataArgIndex  = 0,
		kInsertDataArgValues  = 1,
		kInsertDataArgCount
	};	
	
	// Validate arguments
	ENSURE_ARG_COUNT(args, kInsertDataArgCount);
	
	NSInteger index = [TiUtils intValue:[args objectAtIndex:kInsertDataArgIndex]];
	NSArray *values = [args objectAtIndex:kInsertDataArgValues];
	
	[self addData:values startingAtIndex:index+1];

	// Signal that the data needs to be reloaded
	[self triggerDataUpdate];
}

-(void)deleteData:(id)args
{
	enum DeleteDataArgs {
		kDeleteDataArgIndex  = 0,
		kDeletaDataArgCount  = 1,
		kDeleteDataArgCount
	};	
	
	// Validate arguments
	ENSURE_ARG_COUNT(args, kDeleteDataArgCount);
	
	NSInteger index = [TiUtils intValue:[args objectAtIndex:kDeleteDataArgIndex]];
	NSInteger cnt = [TiUtils intValue:[args objectAtIndex:kDeleteDataArgCount]];
	
	[dataX removeObjectsInRange:NSMakeRange(index, cnt)];
	[dataY removeObjectsInRange:NSMakeRange(index, cnt)];
	
	// Signal that the data needs to be reloaded
	[self triggerDataUpdate];
}

-(CGPoint)viewPointFromGraphPoint:(CGPoint)point
{
    CGPoint viewPoint = [(AkylasChartsChart*)((AkylasChartsChartProxy*)parent).view viewPointFromGraphPoint:point];
    return viewPoint;
}


-(void)notifyOfDataClickedEvent:(NSUInteger)index atPlotPoint:(CGPoint)plotPoint
{
    // The point passed in is relative to the plot area.
    // - First convert from the plot area to the graph area.
    // - Then convert from the graph area to the chart view
    
    BOOL hasDataClicked = [((AkylasChartsChartProxy*)parent) _hasListeners:@"click" checkParent:YES];
    BOOL hasTouchEnd = [((AkylasChartsChartProxy*)parent) _hasListeners:@"touchend" checkParent:NO];
    if (hasDataClicked || hasTouchEnd) {
        CGPoint graphPoint = [self.plot.plotArea convertPoint:plotPoint toLayer:self.plot.graph];
        CGPoint viewPoint = [self viewPointFromGraphPoint:graphPoint];
        NSMutableDictionary *evt = [(AkylasChartsChart*)((AkylasChartsChartProxy*)parent).view eventDictAtPoint:viewPoint onSpace:self.plot.graph.defaultPlotSpace];
        if (hasTouchEnd) {
            [((AkylasChartsChartProxy*)parent) fireEvent:@"touchend" withObject:evt propagate:NO checkForListener:NO];
        }
        if (hasDataClicked) {
            [((AkylasChartsChartProxy*)parent) fireEvent:@"click" withObject:evt propagate:YES checkForListener:NO];
        }
        
    }

//    if ([self _hasListeners:@"dataClicked" checkParent:NO]) {
//		NSDictionary *event = [NSDictionary dictionaryWithObjectsAndKeys:
//								  NUMINTEGER(index),@"index",
//							      [self numberForPlot:index],@"value",
//							      self.plot.identifier,@"name",
//                                  NUMINTEGER(viewPoint.x),@"x",
//                                  NUMINTEGER(viewPoint.y),@"y",
//								  nil
//							  ];        
//		[self fireEvent:@"dataClicked" withObject:event propagate:NO checkForListener:NO];
//	}
//
//	// Since dataClicked events override the touchstart event we should generate one
//	[(AkylasChartsChart*)((AkylasChartsChartProxy*)parent).view notifyOfTouchEvent:@"touchstart" atPoint:viewPoint];
}

//-(void)propertyChanged:(NSString*)key oldValue:(id)oldValue newValue:(id)newValue proxy:(TiProxy*)proxy
//{
//	if ([propertyChangedProperties member:key]!=nil) {
//		[self triggerReconfigure];
//	}
//}

@end
