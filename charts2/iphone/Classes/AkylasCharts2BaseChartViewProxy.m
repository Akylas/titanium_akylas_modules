/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2016 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasCharts2BaseChartViewProxy.h"
#import "BaseChart.h"

@implementation AkylasCharts2BaseChartViewProxy
{
    TiProxy* _rootProxy;
}

-(void)_initWithProperties:(NSDictionary*)properties
{
    [super _initWithProperties:properties];
    [self setRootProxy:self];
}

-(void)dealloc
{
    RELEASE_TO_NIL(_rootProxy)
    [super dealloc];
}

-(TiUIView*)newView
{
    BaseChart *newView = [[BaseChart alloc] init];
    return newView;
}

-(ChartViewBase*)chartView {
    if (view) {
        return [(BaseChart*)view getOrCreateChartView];
    }
    return nil;
}

-(void)setRootProxy:(TiProxy*)rootProxy {
    if (!rootProxy) {
        _rootProxy = rootProxy;
        return;
    }
    if (_rootProxy != self) {
        RELEASE_TO_NIL(_rootProxy)
    } else {
        _rootProxy = nil;
    }
    if (view) {
        [(BaseChart*)view unarchivedWithRootProxy:rootProxy];
    } else {
        //store the root proxy until the view is created
        if (_rootProxy != self) {
            _rootProxy = [rootProxy retain];
        } else {
            _rootProxy = self;

        }
    }
}
-(void)_destroy
{
    [super _destroy];
}

-(void)windowDidOpen
{
    [super windowDidOpen];
    if (_rootProxy) {
        //not the best thing because we have to create the view while it might not
        //yet be necessary. However in listview we need to bindings to be set here
        //so we have to make sure the dataSets bindings are seen
        [self getOrCreateView];
        [self setRootProxy:_rootProxy];
    }
}

- (void)prepareForReuse
{
    [(BaseChart*)[self view] prepareForReuse];
}

- (void)unarchiveFromTemplate:(id)viewTemplate_ withEvents:(BOOL)withEvents rootProxy:(TiProxy*)rootProxy
{
    [super unarchiveFromTemplate:viewTemplate_ withEvents:withEvents rootProxy:rootProxy];
    [self setRootProxy:rootProxy];
}

-(void)highlightValue:(id)args {
    ENSURE_SINGLE_ARG(args, NSDictionary)
    [[self chartView] highlightValueWithXIndex:[TiUtils intValue:@"xIndex" properties:args] dataSetIndex:[TiUtils intValue:@"dataSetIndex" properties:args] callDelegate:YES];
}

-(void) notifyDataSetChanged:(id)ununsed
{
    [[self chartView] notifyDataSetChanged];
}

-(void) redraw:(id)ununsed
{
    [[self chartView] setNeedsDisplay];

}
@end
