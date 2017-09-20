//
//  AkylasCartoAnnotationProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 08/04/2015.
//
//

#import "AkylasMapBaseAnnotationProxy.h"

@class AkylasNTMapView;

@interface AkylasNTMarker : NTMarker{
    
}
@end

@interface AkylasCartoAnnotationProxy : AkylasMapBaseAnnotationProxy

+(int)gZIndexDelta;
@property (nonatomic, readwrite, assign) BOOL appearAnimation;
@property (nonatomic, readwrite, assign) BOOL tracksViewChanges;
@property (nonatomic, readonly, assign, getter=isSelected) BOOL selected;

-(AkylasNTMarker*)getMarker;
-(void)removeFromMap;
-(NTMarkerStyleBuilder*) getMarkerStyleBuilder;
-(NTMarkerStyle*) getMarkerStyle;
@end
