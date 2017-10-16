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


#import "NTMapPos.h"
#import "NTScreenPos.h"
#import "NTBitmap.h"
#import "NTGeometry.h"
#import "NTBalloonPopupStyle.h"
#import "NTPopup.h"

/**
 * A highly configurable popup implementation that allows the user to specify a title, description, colors, images, font sizes etc.
 */
__attribute__ ((visibility("default"))) @interface NTBalloonPopup : NTPopup
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;

/**
 * Creates a polymorphic instance of the given native object. This is used internally by the SDK.
 * @param cPtr The native pointer of the instance.
 * @param cMemoryOwn The ownership flag.
 * @return The new instance.
 */
+(NTBalloonPopup*)swigCreatePolymorphicInstance:(void*)cPtr swigOwnCObject:(BOOL)cMemoryOwn;

/**
 * Constructs a BalloonPopup object with the specified style and attaches it to a billboard element.<br>
 * If an empty string is passed for the title, it will not be drawn. The same applies to the description.<br>
 * @param baseBillboard The billboard this balloon popup will be attached to.<br>
 * @param style The style that defines what this balloon popup looks like.<br>
 * @param title The text this balloon popup will display.<br>
 * @param desc The description this balloon popup will display.
 */
-(id)initWithBaseBillboard: (NTBillboard*)baseBillboard style: (NTBalloonPopupStyle*)style title: (NSString*)title desc: (NSString*)desc;
/**
 * Constructs a BalloonPopup object from a geometry object and a style.<br>
 * If an empty string is passed for the title, it will not be drawn. The same applies to the description.<br>
 * @param geometry The geometry object that defines the location of this balloon popup.<br>
 * @param style The style that defines what this balloon popup looks like.<br>
 * @param title The text this balloon popup will display.<br>
 * @param desc The description this balloon popup will display.
 */
-(id)initWithGeometry: (NTGeometry*)geometry style: (NTBalloonPopupStyle*)style title: (NSString*)title desc: (NSString*)desc;
/**
 * Constructs a BalloonPopup object from a map position and a style.<br>
 * If an empty string is passed for the title, it will not be drawn. The same applies to the description.<br>
 * @param pos The map position that defines the location of this balloon popup.<br>
 * @param style The style that defines what this balloon popup looks like.<br>
 * @param title The text this balloon popup will display.<br>
 * @param desc The description this balloon popup will display.
 */
-(id)initWithPos: (NTMapPos*)pos style: (NTBalloonPopupStyle*)style title: (NSString*)title desc: (NSString*)desc;
-(NTBitmap*)drawBitmap: (NTScreenPos*)anchorScreenPos screenWidth: (float)screenWidth screenHeight: (float)screenHeight dpToPX: (float)dpToPX;
/**
 * Returns the title of this balloon popup.<br>
 * @return The title of this balloon popup.
 */
-(NSString*)getTitle;
/**
 * Sets the title this balloon popup will display. If an empty string is passed<br>
 * the title will not be drawn.<br>
 * @param title The new title this balloon popup will display.
 */
-(void)setTitle: (NSString*)title;
/**
 * Returns the description of this balloon popup.<br>
 * @return The description of this balloon popup.
 */
-(NSString*)getDescription;
/**
 * Sets the description this balloon popup will display. If an empty string is passed<br>
 * the description will not be drawn.<br>
 * @param desc The new description this balloon popup will display.
 */
-(void)setDescription: (NSString*)desc;
/**
 * Returns the style of this balloon popup.<br>
 * @return The style that defines what this balloon popup looks like.
 */
-(NTBalloonPopupStyle*)getStyle;
/**
 * Sets the style for this balloon popup.<br>
 * @param style The new style that defines what this balloon popup looks like.
 */
-(void)setStyle: (NTBalloonPopupStyle*)style;
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
