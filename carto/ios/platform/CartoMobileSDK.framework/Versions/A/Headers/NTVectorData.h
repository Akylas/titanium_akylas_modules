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


#import "NTVectorElement.h"
@class NTVectorElementVector;

/**
 * A wrapper class for vector element data.
 */
__attribute__ ((visibility("default"))) @interface NTVectorData : NSObject
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
 * Constructs a VectorData object from a list of vector elements.<br>
 * @param elements The list of vector elements.
 */
-(id)initWithElements: (NTVectorElementVector*)elements;
/**
 * Returns the list of vector elements.<br>
 * @return The list of vector elements.
 */
-(NTVectorElementVector*)getElements;

-(void)dealloc;

@end


#ifdef __cplusplus
}
#endif

