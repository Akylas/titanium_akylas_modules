/**
 * Ti.Charts Module
 * Copyright (c) 2011-2013 by Appcelerator, Inc. All Rights Reserved.
 * Please see the LICENSE included with this distribution for details.
 */

#import "AkylasChartsLineChartProxy.h"
#import "NSDictionary+Merge.h"

@implementation AkylasChartsLineChartProxy


-(void)setXAxis:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)
    [self replaceValue:[NSDictionary dictionaryByMerging:[self valueForUndefinedKey:@"xAxis"] with:value force:YES] forKey:@"xAxis" notification:YES];
}


-(void)setYAxis:(id)value
{
    ENSURE_SINGLE_ARG_OR_NIL(value, NSDictionary)

    [self replaceValue:[NSDictionary dictionaryByMerging:[self valueForUndefinedKey:@"yAxis"] with:value force:YES] forKey:@"yAxis" notification:YES];
}

@end
