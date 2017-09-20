/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.11
 *
 * This file is not intended to be easily readable and contains a number of
 * coding conventions designed to improve portability and efficiency. Do not make
 * changes to this file unless you know what you are doing--modify the SWIG
 * interface file instead.
 * ----------------------------------------------------------------------------- */

#import <Foundation/Foundation.h>


#ifdef __cplusplus
extern "C" {
#endif


#import "NTColor.h"
#import "NTLabelStyle.h"

/**
 * A style for text labels. Contains attributes for configuring how the text label is drawn on the screen.
 */
__attribute__ ((visibility("default"))) @interface NTTextStyle : NTLabelStyle
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;

/**
 * Creates a polymorphic instance of the given native object. This is used internally by the SDK.
 * @param cPtr The native pointer of the instance.
 * @param cMemoryOwn The ownership flag.
 * @return The new instance.
 */
+(NTTextStyle*)swigCreatePolymorphicInstance:(void*)cPtr swigOwnCObject:(BOOL)cMemoryOwn;

/**
 * Constructs a TextStyle object from various parameters. Instantiating the object directly is<br>
 * not recommended, TextStyleBuilder should be used instead.<br>
 * @param color The color for the text.<br>
 * @param attachAnchorPointX The horizontal attaching anchor point.<br>
 * @param attachAnchorPointY The vertical attaching anchor point.<br>
 * @param causesOverlap The causes overlap flag for the billboard.<br>
 * @param hideIfOverlapped The hide if overlapped flag for the billboard.<br>
 * @param horizontalOffset The horizontal offset.<br>
 * @param verticalOffset The vertical offset.<br>
 * @param placementPriority The placement priority.<br>
 * @param scaleWithDPI The scale with DPI flag for the label.<br>
 * @param anchorPointX The horizontal anchor point.<br>
 * @param anchorPointY The vertical anchor point.<br>
 * @param flippable The fliappble flag.<br>
 * @param orientationMode The orientation mode.<br>
 * @param scalingMode The scaling mode.<br>
 * @param fontName The font's name.<br>
 * @param textField The text field variable to use.<br>
 * @param fontSize The font's size.<br>
 * @param strokeColor The width of the color.<br>
 * @param strokeWidth The width of the stroke.
 */
-(id)initWithColor: (NTColor*)color attachAnchorPointX: (float)attachAnchorPointX attachAnchorPointY: (float)attachAnchorPointY causesOverlap: (BOOL)causesOverlap hideIfOverlapped: (BOOL)hideIfOverlapped horizontalOffset: (float)horizontalOffset verticalOffset: (float)verticalOffset placementPriority: (int)placementPriority scaleWithDPI: (BOOL)scaleWithDPI anchorPointX: (float)anchorPointX anchorPointY: (float)anchorPointY flippable: (BOOL)flippable orientationMode: (enum NTBillboardOrientation)orientationMode scalingMode: (enum NTBillboardScaling)scalingMode fontName: (NSString*)fontName textField: (NSString*)textField fontSize: (int)fontSize strokeColor: (NTColor*)strokeColor strokeWidth: (float)strokeWidth;
/**
 * Returns the font's color.<br>
 * @return The color of the font.
 */
-(NTColor*)getFontColor;
/**
 * Returns the font's name.<br>
 * @return The platform dependent name of the font.
 */
-(NSString*)getFontName;
/**
 * Returns the text field variable to use.<br>
 * @return The text field variable.
 */
-(NSString*)getTextField;
/**
 * Returns the font's size.<br>
 * @return The size of the font in points.
 */
-(int)getFontSize;
/**
 * Returns the color of the stroke.<br>
 * @return The color of the stroke around the text.
 */
-(NTColor*)getStrokeColor;
/**
 * Returns the width of the stroke.<br>
 * @return The width of the stroke around the text.
 */
-(float)getStrokeWidth;
/**
 * Returns the actual class name of this object. This is used internally by the SDK.<br>
 * @return The class name of this object.
 */
-(NSString*)swigGetClassName;
/**
 * Returns the pointer to the connected director object. This is used internally by the SDK.<br>
 * @return The pointer to the connected director object or null if director is not connected.
 */
-(void *)swigGetDirectorObject;

-(void)dealloc;

@end


#ifdef __cplusplus
}
#endif

