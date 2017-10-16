//
//  AkylasUTFGridTileLayer.h
//  akylas.googlemap
//
//  Created by Martin Guillon on 02/02/2016.
//
//

#import "AkylasGMSURLTileLayer.h"

@interface AkylasUTFGridTileLayer : AkylasGMSURLTileLayer<NSURLSessionDelegate>
@property (nonatomic, assign) CGFloat resolution;
-(id)getData: (CLLocationCoordinate2D)latlng atZoom:(NSUInteger) zoom;
@end
