
#import "AkylasMapBaseView.h"
#import "SMCalloutView.h"
//#import "TiCache.h"

//#import "GClusterManager.h"

//#import "AkylasCartoClusterRenderer.h"

@class AkylasCartoView;
@interface CartoMapEventListener: NTMapEventListener {
}
@property (nonatomic, readwrite, assign) AkylasCartoView* mapView;
@end

@interface AkylasNTMapView : NTMapView
//@property (nonatomic, readwrite, retain) TiCache *tileCache;
//@property (nonatomic, readwrite, assign) BOOL networkConnected;
//@property (nonatomic, readwrite, assign) BOOL ignoreSelectedMarkerChange;
@end

@interface AkylasCartoView : AkylasMapBaseView<SMCalloutViewDelegate, UIGestureRecognizerDelegate>
{
}
@property (nonatomic, readwrite, assign) BOOL shouldFollowUserLocation;

-(AkylasNTMapView*)map;
//-(GClusterManager*)clusterManager;
//-(AkylasCartoClusterRenderer*) clusterRenderer;
-(void)markerDidUpdate:(NTMarker*)marker;
-(void)showCalloutForOverlay:(NTVectorElement*)overlay;
-(void)hideCalloutForOverlay:(NTVectorElement*)overlay;
@end
