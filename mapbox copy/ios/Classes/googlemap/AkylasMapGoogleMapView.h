
#import "AkylasMapView.h"
#import <Mapbox/SMCalloutView.h>
#import <GoogleMaps/GoogleMaps.h>
#import "TiCache.h"

@interface AkylasGMSMapView : GMSMapView
@property (nonatomic, readwrite, retain) TiCache *tileCache;
@property (nonatomic, readwrite, assign) BOOL networkConnected;
@end

@interface AkylasMapGoogleMapView : AkylasMapView<SMCalloutViewDelegate, GMSMapViewDelegate>
{
}

-(AkylasGMSMapView*)map;

@end
