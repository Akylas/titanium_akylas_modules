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


#import "NTVectorData.h"
#import "NTProjection.h"
#import "NTCullState.h"
#import "NTViewState.h"

/**
 * Abstract base class for envelope based vector data sources. It provides default implementation<br>
 * for listener registration and other common data source methods.<br>
 * Subclasses need to define their own implementations of loadElements method.<br>
 * <br>
 * The draw order of vector elements within the data source is undefined.
 */
__attribute__ ((visibility("default"))) @interface NTVectorDataSource : NSObject
{
  void *swigCPtr;
  BOOL swigCMemOwn;
}
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;

/**
 * Creates a polymorphic instance of the given native object. This is used internally by the SDK.
 * @param cPtr The native pointer of the instance.
 * @param cMemoryOwn The ownership flag.
 * @return The new instance.
 */
+(NTVectorDataSource*)swigCreatePolymorphicInstance:(void*)cPtr swigOwnCObject:(BOOL)cMemoryOwn;

/**
 * Returns the projection used by this data source.<br>
 * @return The projection used by this data source.
 */
-(NTProjection*)getProjection;
/**
 * Loads all the elements within the defined envelope.<br>
 * @param cullState State for describing view parameters and conservative view envelope.<br>
 * @return The vector of loaded vector elements. If no elements are available, null may be returned.
 */
-(NTVectorData*)loadElements: (NTCullState*)cullState;
/**
 * Notifies listeners that all vector elements have changed. This method refreshes all the existing <br>
 * vector elements in the data source.
 */
-(void)notifyElementsChanged;
/**
 * Constructs an abstract VectorDataSource object.<br>
 * @param projection The projection used by this data source.
 */
-(id)initWithProjection: (NTProjection*)projection;
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

