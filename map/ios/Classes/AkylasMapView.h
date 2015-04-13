
#import "AkylasMapBaseView.h"
#import "SMCalloutView.h"

@protocol AkylasMapAnnotation
@required
-(NSString *)lastHitName;
@end

@class AkylasMapCameraProxy;
@interface AkylasMapView : AkylasMapBaseView<SMCalloutViewDelegate, MKMapViewDelegate>
{
}
-(AkylasMapCameraProxy*)camera;
-(void)animateCamera:(id)args;
@end
