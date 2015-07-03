//
//  AkylasMapBaseClusterSetProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//
#import "AkylasMapBaseAnnotationProxy.h"

@interface AkylasMapBaseClusterProxy : AkylasMapBaseAnnotationProxy<AkylasMapBaseAnnotationProxyDelegate>

-(Class)annotationClass;
-(void)internalAddAnnotations:(NSArray*)annots;
-(void)internalRemoveAnnotations:(id)annots;
-(void)removeAllAnnotations:(id)unused;
@end
