//
//  RMIgnMapSource.h
//  akylas.map
//
//  Created by Martin Guillon on 01/04/2015.
//
//

#import <Mapbox/Mapbox.h>

@interface RMIgnMapSource : RMAbstractWebMapSource

@property (nonatomic, readwrite, copy) NSString* key;
@property (nonatomic, readwrite, copy) NSString* layer;
@property (nonatomic, readwrite, copy) NSString* format;
@end
