//
//  AkylasChartsScatterPlot.h
//  Titanium
//
//  Created by Martin Guillon on 15/07/13.
//
//

#import "CorePlot-CocoaTouch.h"

@interface AkylasChartsScatterPlot : CPTScatterPlot
{
@private
    CGPoint labelDisplacement;
}
@property(nonatomic,readwrite,assign) CGPoint labelDisplacement;
@end
