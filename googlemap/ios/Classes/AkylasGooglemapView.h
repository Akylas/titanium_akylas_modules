
#import "AkylasMapBaseView.h"
#import "SMCalloutView.h"
#import "TiCache.h"

@interface AkylasGMSMapView : GMSMapView
@property (nonatomic, readwrite, retain) TiCache *tileCache;
@property (nonatomic, readwrite, assign) BOOL networkConnected;
@end

@interface AkylasGooglemapView : AkylasMapBaseView<SMCalloutViewDelegate, GMSMapViewDelegate>
{
}

-(AkylasGMSMapView*)map;

@end
