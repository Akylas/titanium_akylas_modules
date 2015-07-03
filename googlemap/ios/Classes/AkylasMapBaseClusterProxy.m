//
//  AkylasMapBaseClusterSetProxy.m
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasMapBaseClusterProxy.h"
#import "AkylasMapBaseViewProxy.h"

@implementation AkylasMapBaseClusterProxy {
    NSMutableArray* _annotations;
}

-(Class)annotationClass
{
    return [AkylasMapBaseAnnotationProxy class];
}


-(void)_destroy
{
    if (_annotations) {
        for (id proxy in _annotations) {
            [proxy setDelegate:nil];
            [self forgetProxy:proxy];
        }
        RELEASE_TO_NIL(_annotations);
    }
     [super _destroy];
}


-(AkylasMapBaseAnnotationProxy*)annotationFromArg:(id)arg
{
    AkylasMapBaseAnnotationProxy *proxy = [self objectOfClass:[self annotationClass] fromArg:arg];
//    if (proxy) {
//        [proxy setPlaced:NO];
//    }
    return proxy;
}


-(void)internalAddAnnotations:(NSArray*)annots {
    
}

-(id)addAnnotation:(id)args
{
    PREPARE_ARRAY_ARGS(args)
    id newAnnotations = nil;
    if (IS_OF_CLASS(value, NSArray)) {
        NSArray* array = (NSArray*)value;
        newAnnotations = [NSMutableArray arrayWithCapacity:[array count]];
        for (id ann in array) {
            AkylasMapBaseAnnotationProxy* annProxy = [self annotationFromArg:ann];
            if (!annProxy || [_annotations containsObject:annProxy]) {
                continue;
            }
            [(NSMutableArray*)newAnnotations addObject:annProxy];
            annProxy.delegate = self;
            [self rememberProxy:annProxy];
        }
        if (!_annotations) {
            _annotations = [NSMutableArray new];
        }
        NSUInteger toAddCount = [newAnnotations count];
        [_annotations addObjectsFromArray:newAnnotations];
        
    } else {
        newAnnotations = [self annotationFromArg:value];
        if (!newAnnotations || [_annotations containsObject:newAnnotations]) return;
        [self rememberProxy:newAnnotations];
        if (!_annotations) {
            _annotations = [NSMutableArray new];
        }
        [_annotations addObject:newAnnotations];
        ((AkylasMapBaseAnnotationProxy*)newAnnotations).delegate = self;
    }
    [self internalAddAnnotations:newAnnotations];
    return newAnnotations;
}

-(void)setAnnotations:(id)arg{
    [self removeAllAnnotations:nil];
    return [self addAnnotation:@[arg]];
}

-(NSArray*)annotations
{
    return _annotations;
}

-(void)internalRemoveAnnotations:(id)annots {
    
}

-(void)removeAnnotation:(id)args
{
    PREPARE_ARRAY_ARGS(args)
    if (IS_OF_CLASS(value, NSArray)) {
        NSArray* array = (NSArray*)value;
        for (id ann in array) {
            if (IS_OF_CLASS(ann, AkylasMapBaseAnnotationProxy))
                [self forgetProxy:ann];
                ((AkylasMapBaseAnnotationProxy*)ann).delegate = nil;
        }
        [_annotations removeObjectsInArray:array];
    } else if (IS_OF_CLASS(value, AkylasMapBaseAnnotationProxy)) {
        [self forgetProxy:(AkylasMapBaseAnnotationProxy*)value];
        ((AkylasMapBaseAnnotationProxy*)value).delegate = nil;
        [_annotations removeObject:value];
    }
    [self internalRemoveAnnotations:value];

}



-(void)removeAllAnnotations:(id)unused
{
    if ([_annotations count] > 0) {
        for (id ann in _annotations) {
            [self forgetProxy:ann];
        }
        RELEASE_TO_NIL(_annotations)
    }
}


-(void)refreshAnnotation:(AkylasMapBaseAnnotationProxy*)proxy reAdd:(BOOL)yn {
    
}

@end
