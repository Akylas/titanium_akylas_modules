package akylas.map;


import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutParams;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.views.MapView;

import android.app.Activity;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
//    AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED,
    AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM,
//    AkylasMapModule.PROPERTY_USER_TRACKING_MODE,
//    TiC.PROPERTY_USER_LOCATION,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE, 
    AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
    AkylasMapModule.PROPERTY_REGION_FIT,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE,
//    AkylasMapModule.PROPERTY_ZOOM,
//    AkylasMapModule.PROPERTY_MINZOOM,
//    AkylasMapModule.PROPERTY_MAXZOOM,
    AkylasMapModule.PROPERTY_ROUTES,
    AkylasMapModule.PROPERTY_MAX_ANNOTATIONS,
//    TiC.PROPERTY_REGION,
//    TiC.PROPERTY_ANNOTATIONS, 
    AkylasMapModule.PROPERTY_ANIMATE_CHANGES,
    
    AkylasMapModule.PROPERTY_TILE_SOURCE,
    AkylasMapModule.PROPERTY_DISK_CACHE,
    AkylasMapModule.PROPERTY_DEBUG
})
public class MapboxViewProxy extends MapDefaultViewProxy {
    private static final String TAG = "MapboxViewProxy";
    
    private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    
    public MapboxViewProxy() {
        super();
        
    }
    
    public TiUIView createView(Activity activity) {
        if (hasProperty(AkylasMapModule.PROPERTY_DEBUG)) {
            boolean debug = TiConvert.toBoolean(getProperty(AkylasMapModule.PROPERTY_DEBUG));
            MapView.setDebugMode(debug);
        }
        TiUIView view = new AkylasMapboxView(this);
        LayoutParams params = view.getLayoutParams();
        params.sizeOrFillWidthEnabled = true;
        params.sizeOrFillHeightEnabled = true;
        params.autoFillsHeight = true;
        params.autoFillsHeight = true;
        params.autoFillsWidth = true;
        return view;
    }

    @Kroll.method
    @Kroll.getProperty
    public float getMetersPerPixel() {
        
        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
        if (mapView != null) {
            return mapView.getMetersPerPixel();
        }
        return 0;
    }
    
}
