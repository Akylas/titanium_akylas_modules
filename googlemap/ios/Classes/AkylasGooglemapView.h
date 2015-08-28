
#import "AkylasMapBaseView.h"
#import "SMCalloutView.h"
#import "TiCache.h"

#import "GClusterManager.h"

#import "AkylasGooglemapClusterRenderer.h"

@interface AkylasGMSMapView : GMSMapView
@property (nonatomic, readwrite, retain) TiCache *tileCache;
@property (nonatomic, readwrite, assign) BOOL networkConnected;
@property (nonatomic, readwrite, assign) BOOL ignoreSelectedMarkerChange;
@end

@interface AkylasGooglemapView : AkylasMapBaseView<SMCalloutViewDelegate, GMSMapViewDelegate, UIGestureRecognizerDelegate>
{
}
@property (nonatomic, readwrite, assign) BOOL shouldFollowUserLocation;

-(AkylasGMSMapView*)map;
-(GClusterManager*)clusterManager;
-(AkylasGooglemapClusterRenderer*) clusterRenderer;
-(void)markerDidUpdate:(GMSMarker*)marker;
-(void)showCalloutForOverlay:(GMSOverlay*)overlay;
-(void)hideCalloutForOverlay:(GMSOverlay*)overlay;
@end
