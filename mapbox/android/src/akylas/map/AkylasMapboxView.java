package akylas.map;

import java.lang.reflect.Array;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import android.util.Log;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

public class AkylasMapboxView extends AkylasMapDefaultView {

    private static final String TAG = "AkylasMapboxView";
    private MapView map;
    private MapController mapController;
    // private UserLocationOverlay myLocationOverlay;

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
        };
//        try {
//            map.setDefaultPinRes(TiRHelper.getResource("drawable.def_pin",
//                    false));
//        } catch (ResourceNotFoundException e) {
//        }
        mapController = map.getController();
        map.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

            @Override
            public boolean onTilesLoadStarted() {
                return false;
            }
        });
        map.setMapViewListener(new MapViewListener() {

            @Override
            public void onTapMarker(MapView pMapView, Marker pMarker) {
                fireEventOnMarker(TiC.EVENT_CLICK, getAkMarker(pMarker), "pin");
            }

            @Override
            public void onTapMap(MapView pMapView, ILatLng pPosition) {
                fireEventOnMap(TiC.EVENT_CLICK, pPosition);
            }

            @Override
            public void onShowMarker(MapView pMapView, Marker pMarker) {
                fireEventOnMarker(TiC.EVENT_FOCUS, getAkMarker(pMarker), "pin");
            }

            @Override
            public void onLongPressMarker(MapView pMapView, Marker pMarker) {
                fireEventOnMarker(TiC.EVENT_LONGPRESS, getAkMarker(pMarker),
                        "pin");
            }

            @Override
            public void onLongPressMap(MapView pMapView, ILatLng pPosition) {
                fireEventOnMap(TiC.EVENT_LONGPRESS, pPosition);
            }

            @Override
            public void onHidemarker(MapView pMapView, Marker pMarker) {
                fireEventOnMarker(TiC.EVENT_BLUR, getAkMarker(pMarker), "pin");

            }
        });

        setNativeView(map);
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
    public boolean customInterceptTouchEvent(MotionEvent event) {
        // to prevent double events
        return map.onTouchEvent(event)
                || super.customInterceptTouchEvent(event);
    }

    // private UserLocationOverlay getOrCreateLocationOverlay() {
    //     if (myLocationOverlay == null) {
    //         // Adds an icon that shows location
    //         try {
    //             myLocationOverlay = new UserLocationOverlay(
    //                     new GpsLocationProvider(getContext()),
    //                     map,
    //                     TiRHelper
    //                             .getResource("drawable.direction_arrow", false),
    //                     TiRHelper.getResource("drawable.person", false));
    //             myLocationOverlay.setDrawAccuracyEnabled(false);
    //         } catch (ResourceNotFoundException e) {
    //         }
    //     }
    //     return myLocationOverlay;
    // }

    // private void addLocationOverlay() {
    //     getOrCreateLocationOverlay().enableMyLocation();
    //     map.addOverlay(myLocationOverlay);
    // }

    // private void removeLocationOverlay() {
    //     if (myLocationOverlay != null) {
    //         myLocationOverlay.disableMyLocation();
    //         if (map.getOverlays().contains(myLocationOverlay)) {
    //             map.removeOverlay(myLocationOverlay);
    //         }
    //     }

    // }

    public KrollDict getUserLocation() {
        return AkylasMapModule.latlongToDict(map.getUserLocation());
    }

    // public Location getUserLocationFix() {
    //     if (myLocationOverlay != null) {
    //         return myLocationOverlay.getLastFix();
    //     }
    //     return null;
    // }

    public boolean getUserLocationEnabled() {
        return map.getUserLocationEnabled();
        // return (myLocationOverlay != null && map.getOverlays().contains(
        //         myLocationOverlay));
    }

    public int getUserTrackingMode() {
        return map.getUserLocationTrackingMode().ordinal();
        // return (myLocationOverlay != null && myLocationOverlay
        //         .isFollowLocationEnabled()) ? 1 : 0;
    }

    private void setTileSources(Object sources) {
        Object source = null;
        if (sources instanceof Object[]) {
            int length = Array.getLength(sources);
            source = new TileLayer[length];
            for (int i = 0; i < length; i++) {
                ((Object[]) source)[i] = AkylasMapModule.tileSourceFromObject(
                        this.proxy, ((Object[]) sources)[i]);
            }
            map.setTileSource((TileLayer[]) source);
        } else {
            source = AkylasMapModule.tileSourceFromObject(this.proxy, sources);
            map.setTileSource((TileLayer) source);
        }
        // if (source != null) {
        // map.setScrollableAreaLimit(map.getTileProvider().getBoundingBox());
        // map.setMinZoomLevel(map.getTileProvider().getMinimumZoomLevel());
        // map.setMaxZoomLevel(map.getTileProvider().getMaximumZoomLevel());
        // map.setCenter(map.getTileProvider().getCenterCoordinate());
        // map.setZoom(map.getTileProvider().getCenterZoom());
        // map.zoomToBoundingBox(map.getTileProvider().getBoundingBox());
        // }
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

    @Override
    public void processPreMapProperties(final KrollDict d) {
        super.processPreMapProperties(d);
        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(d
                    .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)));
        }
        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
            TiPoint point = TiConvert.toPoint(d
                    .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE));
            map.setDefaultPinAnchor(point.computeFloat(null, 0, 0));
        }
        if (d.containsKey(AkylasMapModule.PROPERTY_DISK_CACHE)) {
            setDiskCacheEnabled(TiConvert.toBoolean(d, AkylasMapModule.PROPERTY_DISK_CACHE));
        }
        super.processPreMapProperties(d);
        map.setConstraintRegionFit(regionFit);
    }
    
    @Override 
    public void processMapPositioningProperties(final KrollDict d, final boolean animated) {
        super.processMapPositioningProperties(d, animated);
        if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM)) {
            setUserLocationRequiredZoom(TiConvert.toFloat(d,
                    AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM, 10));
        }
    }

    @Override
    public void processPostMapProperties(final KrollDict d,
            final boolean animated) {
        if (d.containsKey(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
            setTileSources(d.get(AkylasMapModule.PROPERTY_TILE_SOURCE));
        }
        super.processPostMapProperties(d, animated);
    }

    @Override
    public void propertyChanged(String key, Object oldValue, Object newValue,
            KrollProxy proxy) {

        if (key.equals(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
            setTileSources(newValue);
        } else if (key.equals(AkylasMapModule.PROPERTY_DISK_CACHE)) {
            setDiskCacheEnabled(TiConvert.toBoolean(newValue));
        } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
        } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
            TiPoint point = TiConvert.toPoint(newValue);
            map.setDefaultPinAnchor(point.computeFloat(null, 0, 0));
        } else if (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM)) {
            setUserLocationRequiredZoom(TiConvert.toFloat(newValue));
        } else if (key.equals(AkylasMapModule.PROPERTY_REGION_FIT)) {
            regionFit = TiConvert.toBoolean(newValue, regionFit);
            map.setConstraintRegionFit(regionFit);
        } else {
            super.propertyChanged(key, oldValue, newValue, proxy);
        }
    }

    @Override
    public void zoomIn() {
        mapController.zoomIn();
    }

    @Override
    public void zoomIn(final LatLng about, final boolean userAction) {
        mapController.zoomInAbout(about, userAction);
    }

    @Override
    public void zoomOut() {
        mapController.zoomOut();
    }

    @Override
    public void zoomOut(final LatLng about, final boolean userAction) {
        mapController.zoomOutAbout(about, userAction);
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
        // if (enabled) {
        //     addLocationOverlay();
        // } else {
        //     removeLocationOverlay();
        // }
        map.setUserLocationEnabled(enabled);
    }

    protected void setDiskCacheEnabled(boolean enabled)
    {
        map.setDiskCacheEnabled(enabled);
    }

    @Override
    protected void setUserTrackingMode(int mode) {
        // if (mode == 0) {
        //     if (myLocationOverlay != null) {
        //         myLocationOverlay.disableFollowLocation();
        //     }
        // } else {
        //     getOrCreateLocationOverlay().enableFollowLocation();
        //     addLocationOverlay();
        // }
        map.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode.values()[mode]);
    }
    
    protected void setUserLocationRequiredZoom(float zoomLevel) {
        // if (mode == 0) {
        //     if (myLocationOverlay != null) {
        //         myLocationOverlay.disableFollowLocation();
        //     }
        // } else {
        //     getOrCreateLocationOverlay().enableFollowLocation();
        //     addLocationOverlay();
        // }
        map.setUserLocationRequiredZoom(zoomLevel);
    }


    @Override
    public float getMaxZoomLevel() {
        return map.getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() {
        return map.getMinZoomLevel();
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

    public TileSourceProxy addTileSource(Object object, int index) {
        TileSourceProxy sourceProxy = AkylasMapModule
                .tileSourceProxyFromObject(object);
        if (sourceProxy != null) {
            map.addTileSource(sourceProxy.getLayer(), index);
            return sourceProxy;
        }
        return null;
    }

    public void removeTileSource(Object object) {
        if (object instanceof Number) {
            map.removeTileSource(((Number) object).intValue());
        }
        TileLayer layer = AkylasMapModule.tileSourceFromObject(proxy, object);
        if (layer != null) {
            map.removeTileSource(layer);
        }
    }


    Marker getOrCreateMapboxMarker(AnnotationProxy proxy) {
        AkylasMarker marker = proxy.getMarker();
        if (marker == null) {
            marker = new MapboxMarker(proxy);
            proxy.setMarker(marker);
        }
        return ((MapboxMarker) marker).getMarker(map, proxy.getMarkerOptions());
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
        map.goToUserLocation(true);
    }

    @Override
    void handleDeselectMarker(AkylasMarker marker) {
        map.selectMarker(null);
    }

    @Override
    void handleSelectMarker(AkylasMarker marker) {
        map.selectMarker(((MapboxMarker) marker).getMarker());
    }

    @Override
    void handleAddRoute(RouteProxy route) {
        route.setMapView(map);
        map.addOverlay(route.getPath());
    }

    @Override
    void handleRemoveRoute(RouteProxy route) {
        map.removeOverlay(route.getPath());
        route.setMapView(null);
    }

    @Override
    void handleAddAnnotation(AnnotationProxy annotation) {
        Marker marker = getMapboxMarker(annotation);
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        } else {
            timarkers.add(annotation.getMarker());
        }
        map.addMarker(getOrCreateMapboxMarker(annotation));
    }

    @Override
    void handleRemoveMarker(AkylasMarker marker) {
        if (marker instanceof MapboxMarker
                && ((MapboxMarker) marker).getMarker() != null) {
            map.getOverlays().remove(((MapboxMarker) marker).getMarker());
        }
    }
}
