package akylas.map;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;

public class AkylasMapboxView extends AkylasMapDefaultView implements MapView.OnCameraChangeListener,
MapView.OnInfoWindowClickListener, MapView.OnInfoWindowShowListener, MapView.OnMapClickListener,
MapView.OnMapLongClickListener, MapView.OnMarkerClickListener, MapView.OnMarkerLongClickListener,
MapView.OnMyLocationChangeListener, MapView.OnMarkerDragListener{

    private static final String TAG = "AkylasMapboxView";
    private MapView map;
    private MapController mapController;



    
    public AkylasMapboxView(TiViewProxy proxy) {
        super(proxy);
        if (proxy.hasProperty(AkylasMapModule.PROPERTY_DEBUG))
        {
            MapView.setDebugMode(TiConvert.toBoolean(proxy.getProperty(AkylasMapModule.PROPERTY_DEBUG)));
        }
        map = new MapView(proxy.getActivity(), null) {
            private boolean canDetach = true;
            @Override
            public void onDetach() {
                if (!canDetach) return;
                super.onDetach();
            }
            @Override
            protected void onDetachedFromWindow() {
                canDetach = false;
                super.onDetachedFromWindow();
                canDetach = true;
            }
            
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev)
            {
                return interceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
            }
        };
        map.setDiskCacheEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnCameraChangeListener(this);
        map.setOnMarkerDragListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnInfoWindowShowListener(this);
        map.setOnMapLongClickListener(this);

        mapController = map.getController();

        setNativeView(map);
    }
    
 // Intercept the touch event to find out the correct clicksource if clicking on the info window.
    protected boolean interceptTouchEvent(MotionEvent ev)
    {
        if (ev.getAction() == MotionEvent.ACTION_UP && selectedAnnotation != null) {
            AkylasMapInfoView infoWindow = selectedAnnotation.getMapInfoWindow();
            AkylasMarker timarker = selectedAnnotation.getMarker();
            if (infoWindow != null && timarker != null) {
                Marker marker = ((MapboxMarker) timarker).getMarker();
                if (marker != null && marker.isInfoWindowShown()) {
                    PointF markerPoint = marker.getPositionOnScreen(null);
                    infoWindow.analyzeTouchEvent( ev, new Point((int) markerPoint.x, (int) markerPoint.y), 0);
                }
            }
        }
        return false;
    }
    
    @Override
    public void release() {
        if (map != null) {
            map.onDetach();
        }
        super.release();
    }

    private AkylasMarker getAkMarker(Marker pMarker) {
        return ((MapboxMarker.MapboxRealMarker) pMarker).getAkylasMarker();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // to prevent double events
        return super.onTouch(v, event);
    }

    public KrollDict getUserLocation() {
        return AkylasMapModule.latlongToDict(map.getUserLocation());
    }

    public boolean getUserLocationEnabled() {
        return map.getUserLocationEnabled();
    }

    public int getUserTrackingMode() {
        return map.getUserLocationTrackingMode().ordinal();
    }

    protected void handleMinZoomLevel(final float level) {
        map.setMinZoomLevel(level);
    }

    protected void handleMaxZoomLevel(final float level) {
        map.setMaxZoomLevel(level);
    }

    protected void changeZoomLevel(final float level, final boolean animated) {
        if (animated) {
            mapController.setZoomAnimated(level);
        } else {
            mapController.setZoom(level);
        }
    }
    
    protected static final ArrayList<String> KEY_SEQUENCE;
    static{
      ArrayList<String> tmp = AkylasMapDefaultView.KEY_SEQUENCE;
      tmp.add(0, AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE);
      tmp.add(0, AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR);
      tmp.add(0, AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR);
      tmp.add(0, AkylasMapModule.PROPERTY_DISK_CACHE);
      tmp.add(0, AkylasMapModule.PROPERTY_REGION_FIT);
      tmp.add(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
      KEY_SEQUENCE = tmp;
    }
    
    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE:
            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
            break;
        case AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR:
            map.setDefaultPinAnchor(TiConvert.toPointF(newValue));
            break;
        case AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR:
            map.setDefaultInfoWindowAnchor(TiConvert.toPointF(newValue));
            break;
        case AkylasMapModule.PROPERTY_DISK_CACHE:
            setDiskCacheEnabled(TiConvert.toBoolean(newValue));
            break;
        case AkylasMapModule.PROPERTY_REGION_FIT:
            super.propertySet(key, newValue, oldValue, changedProperty);
            map.setConstraintRegionFit(regionFit);
            break;
        case AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            map.setUserLocationRequiredZoom(TiConvert.toFloat(newValue, 10));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

//    @Override
//    public void processPreMapProperties(final KrollDict d) {
//        super.processPreMapProperties(d);
//         
//        
//        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
//            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(d
//                    .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)));
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
//            map.setDefaultPinAnchor(TiConvert.toPointF(d
//                    .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)));
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)) {
//            map.setDefaultInfoWindowAnchor(TiConvert.toPointF(d
//                    .get(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)));
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_DISK_CACHE)) {
//            setDiskCacheEnabled(TiConvert.toBoolean(d, AkylasMapModule.PROPERTY_DISK_CACHE));
//        }
//        super.processPreMapProperties(d);
//        map.setConstraintRegionFit(regionFit);
//    }
    
//    @Override 
//    public void processMapPositioningProperties(final KrollDict d, final boolean animated) {
//        super.processMapPositioningProperties(d, animated);
//        if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM)) {
//            setUserLocationRequiredZoom(TiConvert.toFloat(d,
//                    AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM, 10));
//        }
//    }

//    @Override
//    public void processPostMapProperties(final KrollDict d,
//            final boolean animated) {
//        if (d.containsKey(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
//            setTileSources(d.get(AkylasMapModule.PROPERTY_TILE_SOURCE));
//        }
//        super.processPostMapProperties(d, animated);
//    }

//    @Override
//    public void propertyChanged(String key, Object oldValue, Object newValue,
//            KrollProxy proxy) {
//
//        if (key.equals(AkylasMapModule.PROPERTY_DISK_CACHE)) {
//            setDiskCacheEnabled(TiConvert.toBoolean(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
//            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
//            map.setDefaultPinAnchor(TiConvert.toPointF(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)) {
//            map.setDefaultInfoWindowAnchor(TiConvert.toPointF(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM)) {
//            setUserLocationRequiredZoom(TiConvert.toFloat(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_REGION_FIT)) {
//            regionFit = TiConvert.toBoolean(newValue, regionFit);
//            map.setConstraintRegionFit(regionFit);
//        } else {
//            super.propertyChanged(key, oldValue, newValue, proxy);
//        }
//    }

    @Override
    public void zoomIn() {
        if (TiApplication.isUIThread()) {
            mapController.zoomIn();
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomIn();
                }
            });
        }
    }

    @Override
    public void zoomIn(final LatLng about, final boolean userAction) {
        if (TiApplication.isUIThread()) {
            mapController.zoomInAbout(about, userAction);
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomInAbout(about, userAction);
                }
            });
        }
    }

    @Override
    public void zoomOut() {
        if (TiApplication.isUIThread()) {
            mapController.zoomOut();
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomOut();
                }
            });
        }
    }

    @Override
    public void zoomOut(final LatLng about, final boolean userAction) {
        if (TiApplication.isUIThread()) {
            mapController.zoomOutAbout(about, userAction);
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapController.zoomOutAbout(about, userAction);
                }
            });
        }
    }

    @Override
    public KrollDict getRegionDict() {
        return AkylasMapModule.regionToDict(map.getBoundingBox());
    }

    public MapView getMap() {
        return map;
    }

    @Override
    protected void setUserLocationEnabled(boolean enabled) {
        map.setUserLocationEnabled(enabled);
    }

    protected void setDiskCacheEnabled(boolean enabled)
    {
        map.setDiskCacheEnabled(enabled);
    }

    @Override
    protected void setUserTrackingMode(int mode) {
        map.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.values()[mode]);
    }
    
//    protected void setUserLocationRequiredZoom(float zoomLevel) {
//        map.setUserLocationRequiredZoom(zoomLevel);
//    }

    @Override
    public float getMaxZoomLevel() {
        return map.getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() {
        return map.getMinZoomLevel();
    }
    @Override
    public float getZoomLevel() {
        return map.getZoomLevel();
    }

    @Override
    public void updateCenter(Object dict, final boolean animated) {
        LatLng center = AkylasMapModule.latlongFromObject(dict);
        if (center != null) {
            if (animated) {
                mapController.animateTo(center);
            } else
                map.setCenter(center, true);
        }
    }

    @Override
    public void updateRegion(Object dict, final boolean animated) {
        BoundingBox box = AkylasMapModule.regionFromDict(dict);
        if (box != null) {
            map.zoomToBoundingBox(box, regionFit, animated, true,
                    true);
        }
    }

    @Override
    public void updateScrollableAreaLimit(Object dict) {
        BoundingBox box = AkylasMapModule.regionFromDict(dict);
        if (box != null) {
            map.setScrollableAreaLimit(box);
        }
    }

    @Override
    public TileSourceProxy addTileSource(Object object, int index) {
        TileSourceProxy sourceProxy = AkylasMapModule
                .tileSourceProxyFromObject(object);
        if (sourceProxy != null) {
            map.addTileSource(sourceProxy.getLayer(), index);
            return sourceProxy;
        }
        return null;
    }

    @Override
    public void removeTileSource(Object object) {
        if (object instanceof TileSourceProxy) {
            TileLayer layer = ((TileSourceProxy)object).getLayer();
            if (layer != null) {
                map.removeTileSource(layer);
            }
        }
    }

    Marker getOrCreateMapboxMarker(AnnotationProxy proxy) {
        AkylasMarker marker = proxy.getMarker();
        if (marker == null) {
            marker = new MapboxMarker(proxy);
            proxy.setMarker(marker);
            proxy.setMapView(this);
            proxy.setParentForBubbling(this.proxy);
        }
        return ((MapboxMarker) marker).getMarker(map);
    }
    
    public MabpoxInfoWindow createInfoWindow(AnnotationProxy annotationProxy) {
        MabpoxInfoWindow result = (MabpoxInfoWindow) mInfoWindowCache.get("window");
        result.setProxy(annotationProxy);
        return result;
    }
    
    public void infoWindowDidClose(MabpoxInfoWindow mabpoxInfoWindow) {
        mabpoxInfoWindow.setProxy(null);
        mabpoxInfoWindow.getBoundMarker().setInfoWindow(null);
        if (_calloutUsesTemplates) {
            AkylasMapInfoView infoView = (AkylasMapInfoView) mabpoxInfoWindow.getInfoView();
            Object view = infoView.getLeftView();
            if (view != null && view instanceof TiCompositeLayout) {
                view = ((TiCompositeLayout)view).getView();
            }
            if (view instanceof ReusableView) {
                mInfoWindowCache.put(((ReusableView)view).getReusableIdentifier(), view);
                infoView.setLeftOrRightPane(null, AkylasMapInfoView.LEFT_PANE);
            }
            view = infoView.getRightView();
            if (view != null && view instanceof TiCompositeLayout) {
                view = ((TiCompositeLayout)view).getView();
            }
            if (view instanceof ReusableView) {
                mInfoWindowCache.put(((ReusableView)view).getReusableIdentifier(), view);
                infoView.setLeftOrRightPane(null, AkylasMapInfoView.RIGHT_PANE);
            }
            infoView.setCustomView(null);
        }
        mInfoWindowCache.put("window", mabpoxInfoWindow);
    }

    Marker getMapboxMarker(AnnotationProxy proxy) {
        AkylasMarker marker = proxy.getMarker();
        if (marker != null) {
            return ((MapboxMarker) marker).getMarker();
        }
        return null;
    }

    @Override
    public void selectUserAnnotation() {
        if (TiApplication.isUIThread()) {
            map.goToUserLocation(true);
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.goToUserLocation(true);
                }
            });
        }
    }

    @Override
    void handleDeselectMarker(AkylasMarker marker) {
        if (TiApplication.isUIThread()) {
            map.selectMarker(null);
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.selectMarker(null);
                }
            });
        }
    }

    @Override
    void handleSelectMarker(final AkylasMarker marker) {
        if (TiApplication.isUIThread()) {
            map.selectMarker(((MapboxMarker) marker).getMarker());
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.selectMarker(((MapboxMarker) marker).getMarker());
                }
            });
        }
    }

    @Override
    void handleAddRoute(final RouteProxy route) {
        route.setMapView(map);
        if (TiApplication.isUIThread()) {
            map.addOverlay(route.getPath());
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addOverlay(route.getPath());
                }
            });
        }
    }

    @Override
    void handleRemoveRoute(final RouteProxy route) {
        if (TiApplication.isUIThread()) {
            map.removeOverlay(route.getPath());
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.removeOverlay(route.getPath());
                }
            });
        }
        route.setMapView(null);
    }

    @Override
    void handleAddAnnotation(AnnotationProxy annotation) {
        Marker marker = getMapboxMarker(annotation);
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        } else {
            marker = getOrCreateMapboxMarker(annotation);
        }
        
        if (TiApplication.isUIThread()) {
            map.addMarker(marker);
        } else {
            final Marker fMarker = marker;
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addMarker(fMarker);
                }
            });
        }
    }

    @Override
    void handleRemoveMarker(AkylasMarker marker) {
        if (marker instanceof MapboxMarker) {
            final Marker mapBoxMarker = ((MapboxMarker) marker).getMarker();
            if (mapBoxMarker != null) {
                if (TiApplication.isUIThread()) {
                    map.removeMarker(mapBoxMarker);
                } else {
                    proxy.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            map.removeMarker(mapBoxMarker);
                        }
                    });
                }
            }
            AnnotationProxy annotation = marker.getProxy();
            if (annotation != null) {
                annotation.setMarker(null);
                annotation.setParentForBubbling(null);
                annotation.setMapView(null);
            }
        }
    }
    
    @Override
    protected void removeAllAnnotations() {
        if (TiApplication.isUIThread()) {
            map.clear();
        } else {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.clear();
                }
            });
        }
    }
    
    public void fireLongClickEvent(ILatLng point)
    {
        fireEventOnMap(TiC.EVENT_LONGPRESS, point);
    }
    
    public void fireClickEvent(final Marker marker, final String source)
    {
        fireEventOnMarker(TiC.EVENT_CLICK, marker, source);
    }
    
    protected void fireEventOnMarker(String type, Marker marker, String clickSource) {
        fireEventOnMarker(type, getAkMarker(marker), clickSource);
    }
    

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        fireClickEvent(marker, AkylasMapModule.PROPERTY_PIN);
        return true;
    }

    @Override
    public void onMapClick(ILatLng point)
    {
        fireEventOnMap(TiC.EVENT_CLICK, point);
    }
    
    @Override
    public void onMapLongClick(ILatLng point)
    {
        fireLongClickEvent(point);
    }

//    @Override
//    public void onMarkerDrag(Marker marker)
//    {
//        Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
//    }

//    @Override
//    public void onMarkerDragEnd(Marker marker)
//    {
//        AnnotationProxy annoProxy = getProxyByMarker(marker);
//        if (annoProxy != null) {
//            LatLng position = marker.getPosition();
//            annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.longitude);
//            annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
//            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_END);
//        }
//    }

//    @Override
//    public void onMarkerDragStart(Marker marker)
//    {
//        AnnotationProxy annoProxy = getProxyByMarker(marker);
//        if (annoProxy != null) {
//            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_START);
//        }
//    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        AkylasMarker akMarker = getAkMarker(marker);
        if (akMarker != null) {
            String clicksource = akMarker.getProxy().getMapInfoWindow().getClicksource();
            // The clicksource is null means the click event is not inside "leftPane", "title", "subtible"
            // or "rightPane". In this case, use "infoWindow" as the clicksource.
            if (clicksource == null) {
                clicksource = AkylasMapModule.PROPERTY_INFO_WINDOW;
            }
            fireClickEvent(marker, clicksource);
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
    }

    @Override
    public boolean onMarkerLongClick(Marker marker) {
        fireEventOnMarker(TiC.EVENT_LONGPRESS, marker, AkylasMapModule.PROPERTY_PIN);
        return false;
    }

    @Override
    public void onInfoWindowShow(Marker marker) {
        selectedAnnotation = getAkMarker(marker).getProxy();
        fireEventOnMarker(TiC.EVENT_FOCUS, getAkMarker(marker), AkylasMapModule.PROPERTY_PIN);
    }

    @Override
    public void onInfoWindowHide(Marker marker) {
        selectedAnnotation = null;
        fireEventOnMarker(TiC.EVENT_BLUR, getAkMarker(marker), AkylasMapModule.PROPERTY_PIN);
    }

    @Override
    public void onCameraChange(BoundingBox box, float zoom) {
        if (proxy != null
                && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
            KrollDict result = new KrollDict();
            result.put(TiC.PROPERTY_REGION, AkylasMapModule.regionToDict(box));
            result.put(AkylasMapModule.PROPERTY_ZOOM, zoom);
            proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
        }
        
    }

    @Override
    public void onMarkerDragCancel(Marker marker) {
        AnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_CANCEL);
        }
    }
    
    @Override
    public void onMarkerDrag(Marker marker)
    {
        AnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_DRAGGING);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        AnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            LatLng position = marker.getPosition();
            annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.getLongitude());
            annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.getLatitude());
            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_END);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {
        AnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_START);
        }
    }
    
    public float getMetersPerPixel() {
        final float density = getContext().getResources().getDisplayMetrics().density - 1;
        float result = (float) map.getProjection().groundResolution(0);
        if (density != 0) {
            result *= density;
        }
        return result;
    }
}
