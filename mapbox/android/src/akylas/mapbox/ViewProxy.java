package akylas.mapbox;


import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutParams;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.views.MapView;

import akylas.map.common.MapDefaultViewProxy;
import android.app.Activity;

@Kroll.proxy(creatableInModule = AkylasMapboxModule.class, propertyAccessors = {
    AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
    AkylasMapboxModule.PROPERTY_DISK_CACHE,
    AkylasMapboxModule.PROPERTY_DEBUG
})
public class ViewProxy extends MapDefaultViewProxy {
    private static final String TAG = "MapboxViewProxy";
    
    private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    
    public ViewProxy() {
        super();
        
    }
    @Override
    public String getApiName() {
        return "Akylas.Mapbox.View";
    }
    
    public TiUIView createView(Activity activity) {
        if (hasProperty(AkylasMapboxModule.PROPERTY_DEBUG)) {
            boolean debug = TiConvert.toBoolean(getProperty(AkylasMapboxModule.PROPERTY_DEBUG));
            MapView.setDebugMode(debug);
        }
        TiUIView view = new MapboxView(this);
        LayoutParams params = view.getLayoutParams();
        params.sizeOrFillWidthEnabled = true;
        params.sizeOrFillHeightEnabled = true;
        params.autoFillsHeight = true;
        params.autoFillsHeight = true;
        params.autoFillsWidth = true;
        return view;
    }
    
    @Override
    protected Class annotationClass() {
        return AnnotationProxy.class;
    }
    @Override
    protected Class routeClass() {
        return RouteProxy.class;
    }
    @Override
    protected Class tileSourceClass() {
        return TileSourceProxy.class;
    }

    @Kroll.method
    @Kroll.getProperty
    public float getMetersPerPixel() {
        
        MapboxView mapView = (MapboxView) peekView();
        if (mapView != null) {
            return mapView.getMetersPerPixel();
        }
        return 0;
    }
    
}
