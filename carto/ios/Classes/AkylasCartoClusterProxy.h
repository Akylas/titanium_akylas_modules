//
//  AkylasCartoClusterSetProxy.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 01/07/2015.
//
//

#import "AkylasMapBaseClusterProxy.h"


@interface AkylasCartoClusterProxy : AkylasMapBaseClusterProxy
@property (nonatomic, readwrite, assign) CGFloat maxDistance;
@property (nonatomic, readwrite, assign) CGFloat minDistance;
@property (nonatomic, readwrite, assign) BOOL showText;
@property (nonatomic, readwrite, assign) BOOL selectedShowText;

//-(NSUInteger) uniqueId;
-(void)cluster;
-(id)visibleAnnotations;
@end
