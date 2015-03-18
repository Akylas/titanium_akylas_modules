#import "AkylasMapAnnotationProxy.h"
#import "AkylasMKOverlayPathUniversal.h"

@interface RMRouteAnnotation : RMShapeAnnotation
@end


@class AkylasMapMapView;
@interface AkylasMapRouteProxy : AkylasMapAnnotationProxy {
}

@property (nonatomic, readonly) NSArray *routeLine;

@property (nonatomic, readonly) NSUInteger level;
@property (nonatomic, readonly) id <AkylasMKOverlayPathUniversal> routeRenderer;


-(id <AkylasMKOverlayPathUniversal>)rendererForMapView:(AkylasMapMapView*)mapView;
-(MKPolyline*) getPolyline;

@end
