
#import "AkylasMapBaseView.h"
#import "SMCalloutView.h"
#import "TiCache.h"

#import "GClusterManager.h"

#import "AkylasGooglemapClusterRenderer.h"

@interface AkylasGMSMapView : GMSMapView
@property (nonatomic, readwrite, retain) TiCache *tileCache;
@property (nonatomic, readwrite, assign) BOOL networkConnected;
@end

@interface AkylasGooglemapView : AkylasMapBaseView<SMCalloutViewDelegate, GMSMapViewDelegate>
{
}

-(GMSMapView*)map;
-(GClusterManager*)clusterManager;
-(AkylasGooglemapClusterRenderer*) clusterRenderer;
@end
