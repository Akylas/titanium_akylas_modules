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
#import "NTPopup.h"
#import "NTClickType.h"

/**
 * A container class that provides information about a click performed on<br>
 * a popup.
 */
__attribute__ ((visibility("default"))) @interface NTPopupClickInfo : NSObject
{
  void *swigCPtr;
  BOOL swigCMemOwn;
}
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;

/**  
 * Checks if this object is equal to the specified object.
 * @param object The reference object.
 * @return True when objects are equal, false otherwise.
 */
-(BOOL)isEqual:(id)object;

/**
 * Returns the hash value of this object.
 * @return The hash value of this object.
 */
-(NSUInteger)hash;

/**
 * Constructs a PopupClickInfo object from a click position and a vector element.<br>
 * @param clickType The click type (SINGLE, DUAL, etc)<br>
 * @param clickPos The click position in the coordinate system of the base projection.<br>
 * @param elementClickPos The 2D click position on the popup.<br>
 * @param popup The popup on which the click was performed.
 */
-(id)initWithClickType: (enum NTClickType)clickType clickPos: (NTMapPos*)clickPos elementClickPos: (NTScreenPos*)elementClickPos popup: (NTPopup*)popup;
/**
 * Returns the click type.<br>
 * @return The type of the click performed.
 */
-(enum NTClickType)getClickType;
/**
 * Returns the click position.<br>
 * @return The click position in the coordinate system of the base projection.
 */
-(NTMapPos*)getClickPos;
/**
 * Returns the 2D click position on the clicked popup.<br>
 * @return The 2D element click position.
 */
-(NTScreenPos*)getElementClickPos;
/**
 * Returns the clicked popup.<br>
 * @return The popup on which the click was performed.
 */
-(NTPopup*)getPopup;

-(void)dealloc;

@end


#ifdef __cplusplus
}
#endif

