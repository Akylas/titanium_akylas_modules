#import "CPTAnnotationHostLayer.h"
#import "CPTLineStyle.h"

/// @file

/**
 *  @brief Enumeration of line drawing directions.
 **/
typedef enum AkylasChartsLineDirection {
    CPTLineDirectionHorizontal,       ///< Horizontal Line.
    CPTLineDirectionVertical ///< Vertical Line.
}
AkylasChartsLineDirection;

@interface AkylasChartsLineLayer : CPTLayer {
@protected
    CPTLineStyle *lineStyle;
    AkylasChartsLineDirection direction;
    CPTLayer* parentLayer;
}

@property (readwrite, retain, nonatomic) CPTLineStyle *lineStyle;
@property (readwrite, retain, nonatomic) CPTLayer *parentLayer;
@property (readwrite, assign, nonatomic) AkylasChartsLineDirection direction;

/// @name Initialization
/// @{
-(id)initWithDirection:(AkylasChartsLineDirection)direction;
-(id)initWithDirection:(AkylasChartsLineDirection)direction style:(CPTLineStyle *)newStyle;
/// @}

/// @name Layout
/// @{
-(CGSize)sizeThatFits;
-(void)sizeToFit;
/// @}

@end
