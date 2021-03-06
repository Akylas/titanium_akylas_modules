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


#import "NTMapRange.h"
#import "NTCullState.h"

/**
 * An abstract base class for all layers.
 */
__attribute__ ((visibility("default"))) @interface NTLayer : NSObject
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
+(NTLayer*)swigCreatePolymorphicInstance:(void*)cPtr swigOwnCObject:(BOOL)cMemoryOwn;


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
 * Returns the layer task priority of this layer.<br>
 * @return The priority level for the tasks of this layer.
 */
-(int)getUpdatePriority;
/**
 * Sets the layer task priority. Higher priority layers get to load data before<br>
 * lower priority layers. Normal layers and tile layers have seperate task queues and thus <br>
 * don't compete with each other for task queue access. The default is 0.<br>
 * @param priority The new task priority for this layer, higher values get better access.
 */
-(void)setUpdatePriority: (int)priority;
/**
 * Sets the layer culling delay. The culling delay is used to delay layer content rendering in case of user interaction,<br>
 * higher delay improves performance and battery life at the expense of interactivity. Default is 200ms-400ms, depending<br>
 * on layer type.<br>
 * @param delay The new culling delay in milliseconds.
 */
-(void)setCullDelay: (int)delay;
/**
 * Returns the visibility of this layer.<br>
 * @return True if the layer is visible.
 */
-(BOOL)isVisible;
/**
 * Sets the visibility of this layer.<br>
 * @param visible The new visibility state of the layer.
 */
-(void)setVisible: (BOOL)visible;
/**
 * Returns the visible zoom range of this layer.<br>
 * @return The visible zoom range of this layer.
 */
-(NTMapRange*)getVisibleZoomRange;
/**
 * Set the visible zoom range for this layer. Current zoom level must be within this range for the layer to be visible.<br>
 * This range is half-open, thus layer is visible if range.min &lt;= ZOOMLEVEL &lt; range.max.<br>
 * @param range new visible zoom range
 */
-(void)setVisibleZoomRange: (NTMapRange*)range;
/**
 * Tests whether this layer is being currently updated.<br>
 * @return True when the layer is being updated or false when the layer is in steady state.
 */
-(BOOL)isUpdateInProgress;
/**
 * Updates the layer using new visibility information. This method is periodically called when the map view moves.<br>
 * The visibilty info is saved, so the data can be refreshed later.<br>
 * @param cullState The new visibilty information.
 */
-(void)update: (NTCullState*)cullState;
/**
 * Refreshes the layer using old stored visibility information. This method might be called if some of the layer data<br>
 * changes.
 */
-(void)refresh;
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


#import "NTMapRange.h"
#import "NTCullState.h"

__attribute__ ((visibility("default"))) @interface NTLayerVector : NSObject
{
	void *swigCPtr;
	BOOL swigCMemOwn;
}
-(void*)getCptr;
-(id)initWithCptr: (void*)cptr swigOwnCObject: (BOOL)ownCObject;
-(id)init;
-(unsigned int)size;
-(unsigned int)capacity;
-(void)reserve: (unsigned int)n;
-(BOOL)isEmpty;
-(void)clear;
-(void)add: (NTLayer*)x;
-(NTLayer*)get: (int)i;
-(void)set: (int)i val: (NTLayer*)val;

-(void)dealloc;

@end


#ifdef __cplusplus
}
#endif

