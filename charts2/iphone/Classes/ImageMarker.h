//
//  CustomChartsMarker.h
//  akylas.charts2
//
//  Created by Martin Guillon on 30/07/16.
//
//

@interface ImageMarker : NSObject<IChartMarker>
@property (nonatomic) CGPoint offset;
@property (nonatomic) CGSize size;
@property (nonatomic, retain) UIImage* image;
@property (nonatomic, assign) ChartViewBase* chartView;

@end
