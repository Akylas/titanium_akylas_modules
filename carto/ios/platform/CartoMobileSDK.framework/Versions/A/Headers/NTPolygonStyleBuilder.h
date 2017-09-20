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


#import "NTPolygonStyle.h"
#import "NTStyleBuilder.h"

/**
 * A builder class for PolygonStyle.
 */
__attribute__ ((visibility("default"))) @interface NTPolygonStyleBuilder : NTStyleBuilder
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;

/**
 * Creates a polymorphic instance of the given native object. This is used internally by the SDK.
 * @param cPtr The native pointer of the instance.
 * @param cMemoryOwn The ownership flag.
 * @return The new instance.
 */
+(NTPolygonStyleBuilder*)swigCreatePolymorphicInstance:(void*)cPtr swigOwnCObject:(BOOL)cMemoryOwn;

/**
 * Constructs a PolygonStyleBuilder object with all parameters set to defaults.
 */
-(id)init;
/**
 * Returns the line style of the edges of the polygon.<br>
 * @return The line style of the edges of the polygon. May be null.
 */
-(NTLineStyle*)getLineStyle;
/**
 * Sets the line style that will be used to draw the edges of the polygon. If null is passed<br>
 * no edges will be drawn. The default is null.<br>
 * @param lineStyle The new style for the edges of the polygon.
 */
-(void)setLineStyle: (NTLineStyle*)lineStyle;
/**
 * Builds a new instance of the PolygonStyle object using previously set parameters.<br>
 * @return A new PolygonStyle object.
 */
-(NTPolygonStyle*)buildStyle;
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

