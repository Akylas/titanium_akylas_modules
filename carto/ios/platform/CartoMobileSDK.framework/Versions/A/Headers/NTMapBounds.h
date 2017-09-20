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
#import "NTMapVec.h"

/**
 * A container class that defines an axis aligned cuboid on the map using minimum and maximum map positions.<br>
 * Valid ranges for map bounds depend on the projection used.
 */
__attribute__ ((visibility("default"))) @interface NTMapBounds : NSObject
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
 * Constructs an empty MapBounds object. The coordinates of the minimum map position will be <br>
 * set to positive infinity and the coordinates of the maximum map position will be<br>
 * set to negative infinity.
 */
-(id)init;
/**
 * Constructs a MapBounds object from a minimum and maximum map position. If a coordinate of the <br>
 * minimum map positon is larger than the same coordinate of the maximum map position then those<br>
 * coordinates will be swapped.<br>
 * @param min The minimum map position (south-west).<br>
 * @param max The maximum map position (north-east).
 */
-(id)initWithMin: (NTMapPos*)min max: (NTMapPos*)max;
/**
 * Checks for equality between this and another map bounds object.<br>
 * @param mapBounds The other map bounds object.<br>
 * @return True if equal.
 */
-(BOOL)isEqualInternal: (NTMapBounds*)mapBounds;
/**
 * Calculates the center map position of this map envelope object.<br>
 * @return The center postion if this map envelope object.
 */
-(NTMapPos*)getCenter;
/**
 * Calculates the difference vector between the maximum and minimum map positions of this map bounds object.<br>
 * @return The difference vector between maximum and minimum map positions of this map bounds object.
 */
-(NTMapVec*)getDelta;
/**
 * Returns the minimum (south west) map position of this map envelope object.<br>
 * @return The minimum (south west) map position of this map envelope object.
 */
-(NTMapPos*)getMin;
/**
 * Returns the maximum (north east) map position of this map envelope object.<br>
 * @return The maximum (north east) map position of this map envelope object.
 */
-(NTMapPos*)getMax;
/**
 * Tests whether this map bounds object contains a map position.<br>
 * @param pos The map position.<br>
 * @return True if this map bounds object contains the map position.
 */
-(BOOL)containsPos: (NTMapPos*)pos;
/**
 * Tests whether this map bounds object contains a another map bounds object.<br>
 * @param bounds The other map bounds object.<br>
 * @return True if this map bounds object contains the other map bounds object.
 */
-(BOOL)containsBounds: (NTMapBounds*)bounds;
/**
 * Tests whether this map bounds object intersects with a another map bounds object.<br>
 * @param bounds The other map bounds object.<br>
 * @return True if this map bounds object intersects with the other map bounds object.
 */
-(BOOL)intersects: (NTMapBounds*)bounds;
/**
 * Returns the hash value of this object.<br>
 * @return The hash value of this object.
 */
-(int)hashInternal;
/**
 * Creates a string representation of this map bounds object, useful for logging.<br>
 * @return The string representation of this map bounds object.
 */
-(NSString*)description;

-(void)dealloc;

@end


#ifdef __cplusplus
}
#endif

