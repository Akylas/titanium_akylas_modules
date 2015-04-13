package akylas.mapbox;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiActivityHelper.CommandNoReturn;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.AkylasMapInfoView;
import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseRouteProxy;
import akylas.map.common.BaseTileSourceProxy;
import akylas.map.common.ReusableView;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Location;
import android.view.MotionEvent;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;

public class MapboxView extends AkylasMapBaseView implements
        MapView.OnCameraChangeListener, MapView.OnInfoWindowClickListener,
        MapView.OnInfoWindowShowListener, MapView.OnMapClickListener,
        MapView.OnMapLongClickListener, MapView.OnMarkerClickListener,
        MapView.OnMarkerLongClickListener, MapView.OnMyLocationChangeListener,
        MapView.OnMarkerDragListener {

    private static final String TAG = "AkylasMapboxView";
    private MapView map;
    private MapController mapController;

    public MapboxView(TiViewProxy proxy) {
        super(proxy);
        if (proxy.hasProperty(AkylasMapboxModule.PROPERTY_DEBUG)) {
            MapView.setDebugMode(TiConvert.toBoolean(proxy
                    .getProperty(AkylasMapboxModule.PROPERTY_DEBUG)));
        }
        map = new MapView(proxy.getActivity(), null) {
            private boolean canDetach = true;

            @Override
            public void onDetach() {
                if (!canDetach)
                    return;
                super.onDetach();
            }

            @Override
            protected void onDetachedFromWindow() {
                canDetach = false;
                super.onDetachedFromWindow();
                canDetach = true;
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
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

    // Intercept the touch event to find out the correct clicksource if clicking
    // on the info window.
    protected boolean interceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP
                && selectedAnnotation != null) {
            AkylasMapInfoView infoWindow = selectedAnnotation
                    .getMapInfoWindow();
            AkylasMarker timarker = selectedAnnotation.getMarker();
            if (infoWindow != null && timarker != null) {
                Marker marker = ((MapboxMarker) timarker).getMarker();
                if (marker != null && marker.isInfoWindowShown()) {
                    PointF markerPoint = marker.getPositionOnScreen(null);
                    infoWindow.analyzeTouchEvent(ev, new Point(
                            (int) markerPoint.x, (int) markerPoint.y), 0);
                }
            }
        }
        return false;
    }

    @Override
    protected Object cacheAskForView(final String key) {
        if (key.equals("window")) {
            return new MapboxInfoWindow(getProxy().getActivity()); // new view;
        }
        return super.cacheAskForView(key);
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

    public KrollDict getUserLocation() {
        return AkylasMapboxModule.getFactory().latlongToDict(
                map.getUserLocation());
    }

    public boolean getUserLocationEnabled() {
        return map.getUserLocationEnabled();
    }

    public int getUserTrackingMode() {
        return map.getUserLocationTrackingMode().ordinal();
    }

    @Override
    public void handleMinZoomLevel(final float level) {
        map.setMinZoomLevel(level);
    }

    @Override
    public void handleMaxZoomLevel(final float level) {
        map.setMaxZoomLevel(level);
    }

    @Override
    public void changeZoomLevel(final float level, final boolean animated) {
        if (animated) {
            mapController.setZoomAnimated(level);
        } else {
            mapController.setZoom(level);
        }
    }

    @Override
    public void clearCache(int type) {
        switch (type) {
        case 0:
        default:
            map.clearCache();
            break;
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;
    static {
        ArrayList<String> tmp = AkylasMapBaseView.KEY_SEQUENCE;
        tmp.add(0, AkylasMapboxModule.PROPERTY_DEFAULT_PIN_IMAGE);
        tmp.add(0, AkylasMapboxModule.PROPERTY_DEFAULT_PIN_ANCHOR);
        tmp.add(0, AkylasMapboxModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR);
        tmp.add(0, AkylasMapboxModule.PROPERTY_DISK_CACHE);
        tmp.add(0, AkylasMapboxModule.PROPERTY_REGION_FIT);
        tmp.add(AkylasMapboxModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
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
        case AkylasMapboxModule.PROPERTY_DEFAULT_PIN_IMAGE:
            map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
            break;
        case AkylasMapboxModule.PROPERTY_ROTATE_ENABLED:
            map.setMapRotationEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapboxModule.PROPERTY_DEFAULT_PIN_ANCHOR:
            map.setDefaultPinAnchor(TiConvert.toPointF(newValue));
            break;
        case AkylasMapboxModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR:
            map.setDefaultInfoWindowAnchor(TiConvert.toPointF(newValue));
            break;
        case AkylasMapboxModule.PROPERTY_DISK_CACHE:
            setDiskCacheEnabled(TiConvert.toBoolean(newValue));
            break;

        case AkylasMapboxModule.PROPERTY_REGION_FIT:
            super.propertySet(key, newValue, oldValue, changedProperty);
            map.setConstraintRegionFit(regionFit);
            break;
        case AkylasMapboxModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            map.setUserLocationRequiredZoom(TiConvert.toFloat(newValue, 10));
            break;

        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    // @Override
    // public void processPreMapProperties(final KrollDict d) {
    // super.processPreMapProperties(d);
    //
    //
    // if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
    // map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(d
    // .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)));
    // }
    // if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
    // map.setDefaultPinAnchor(TiConvert.toPointF(d
    // .get(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)));
    // }
    // if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)) {
    // map.setDefaultInfoWindowAnchor(TiConvert.toPointF(d
    // .get(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)));
    // }
    // if (d.containsKey(AkylasMapModule.PROPERTY_DISK_CACHE)) {
    // setDiskCacheEnabled(TiConvert.toBoolean(d,
    // AkylasMapModule.PROPERTY_DISK_CACHE));
    // }
    // super.processPreMapProperties(d);
    // map.setConstraintRegionFit(regionFit);
    // }

    // @Override
    // public void processMapPositioningProperties(final KrollDict d, final
    // boolean animated) {
    // super.processMapPositioningProperties(d, animated);
    // if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM))
    // {
    // setUserLocationRequiredZoom(TiConvert.toFloat(d,
    // AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM, 10));
    // }
    // }

    // @Override
    // public void processPostMapProperties(final KrollDict d,
    // final boolean animated) {
    // if (d.containsKey(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
    // setTileSources(d.get(AkylasMapModule.PROPERTY_TILE_SOURCE));
    // }
    // super.processPostMapProperties(d, animated);
    // }

    // @Override
    // public void propertyChanged(String key, Object oldValue, Object newValue,
    // KrollProxy proxy) {
    //
    // if (key.equals(AkylasMapModule.PROPERTY_DISK_CACHE)) {
    // setDiskCacheEnabled(TiConvert.toBoolean(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
    // map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
    // map.setDefaultPinAnchor(TiConvert.toPointF(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_ANCHOR)) {
    // map.setDefaultInfoWindowAnchor(TiConvert.toPointF(newValue));
    // } else if
    // (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM)) {
    // setUserLocationRequiredZoom(TiConvert.toFloat(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_REGION_FIT)) {
    // regionFit = TiConvert.toBoolean(newValue, regionFit);
    // map.setConstraintRegionFit(regionFit);
    // } else {
    // super.propertyChanged(key, oldValue, newValue, proxy);
    // }
    // }

    @Override
    public void zoomIn() {
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                mapController.zoomIn();
            }
        });
    }

    @Override
    public void zoomIn(final Object about, final boolean userAction) {
        final LatLng point = (LatLng) AkylasMapboxModule
                .latlongFromObject(about);
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                mapController.zoomInAbout(point, userAction);

            }
        });
    }

    @Override
    public void zoomOut() {
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                mapController.zoomOut();
            }
        });
    }

    @Override
    public void zoomOut(final Object about, final boolean userAction) {
        final LatLng point = (LatLng) AkylasMapboxModule
                .latlongFromObject(about);
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                mapController.zoomOutAbout(point, userAction);

            }
        });
    }

    @Override
    public KrollDict getRegionDict() {
        return AkylasMapBaseModule.getFactory().regionToDict(
                map.getBoundingBox());
    }

    public MapView getMap() {
        return map;
    }

    @Override
    public void setUserLocationEnabled(boolean enabled) {
        map.setUserLocationEnabled(enabled);
    }

    protected void setDiskCacheEnabled(boolean enabled) {
        map.setDiskCacheEnabled(enabled);
    }

    @Override
    public void setUserTrackingMode(int mode) {
        map.setUserLocationTrackingMode(UserLocationOverlay.TrackingMode
                .values()[mode]);
    }

    // protected void setUserLocationRequiredZoom(float zoomLevel) {
    // map.setUserLocationRequiredZoom(zoomLevel);
    // }

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
        final LatLng center = (LatLng) AkylasMapboxModule
                .latlongFromObject(dict);
        if (center != null) {
            if (animated) {
                mapController.animateTo(center);
            } else
                map.setCenter(center, true);
        }
    }

    @Override
    public void updateRegion(Object dict, final boolean animated) {
        Log.d(TAG, "updateRegion " + dict.toString());
        final BoundingBox box = (BoundingBox) AkylasMapboxModule
                .regionFromObject(dict);
        if (box != null) {
            map.zoomToBoundingBox(box, regionFit, animated, true, true);
        }
    }

    @Override
    public void updateScrollableAreaLimit(Object dict) {
        final BoundingBox box = (BoundingBox) AkylasMapboxModule
                .regionFromObject(dict);
        if (box != null) {
            map.setScrollableAreaLimit(box);
        }
    }

    @Override
    public BaseTileSourceProxy addTileSource(Object object, int index) {
        BaseTileSourceProxy sourceProxy = tileSourceProxyFromObject(object);
        if (sourceProxy instanceof TileSourceProxy) {
            map.addTileSource(((TileSourceProxy) sourceProxy).getLayer(), index);
            return sourceProxy;
        }
        return null;
    }

    @Override
    public void removeTileSource(Object object) {
        if (object instanceof TileSourceProxy) {
            TileLayer layer = ((TileSourceProxy) object).getLayer();
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

    public MapboxInfoWindow createInfoWindow(AnnotationProxy annotationProxy) {
        MapboxInfoWindow result = (MapboxInfoWindow) mInfoWindowCache
                .get("window");
        result.setProxy(annotationProxy);
        return result;
    }

    public void infoWindowDidClose(MapboxInfoWindow mabpoxInfoWindow) {
        mabpoxInfoWindow.setProxy(null);
        mabpoxInfoWindow.getBoundMarker().setInfoWindow(null);
        if (_calloutUsesTemplates) {
            AkylasMapInfoView infoView = (AkylasMapInfoView) mabpoxInfoWindow
                    .getInfoView();
            Object view = infoView.getLeftView();
            if (view != null && view instanceof TiCompositeLayout) {
                view = ((TiCompositeLayout) view).getView();
            }
            if (view instanceof ReusableView) {
                mInfoWindowCache.put(
                        ((ReusableView) view).getReusableIdentifier(), view);
                infoView.setLeftOrRightPane(null, AkylasMapInfoView.LEFT_PANE);
            }
            view = infoView.getRightView();
            if (view != null && view instanceof TiCompositeLayout) {
                view = ((TiCompositeLayout) view).getView();
            }
            if (view instanceof ReusableView) {
                mInfoWindowCache.put(
                        ((ReusableView) view).getReusableIdentifier(), view);
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
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.goToUserLocation(true);
            }
        });
    }

    @Override
    public void handleDeselectMarker(AkylasMarker marker) {
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.selectMarker(null);
            }
        });
    }

    @Override
    public void handleSelectMarker(final AkylasMarker marker) {
        final Marker fMarker = ((MapboxMarker) marker).getMarker();
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.selectMarker(fMarker);
            }
        });
    }

    @Override
    public void handleAddRoute(final BaseRouteProxy route) {
        ((RouteProxy) route).setMapView(map);
        final PathOverlay path = ((RouteProxy) route).getPath();
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.addOverlay(path);
            }
        });
    }

    @Override
    public void handleRemoveRoute(final BaseRouteProxy route) {
        final PathOverlay path = ((RouteProxy) route).getPath();
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.removeOverlay(path);
            }
        });
        ((RouteProxy) route).setMapView(null);
    }

    @Override
    public void handleAddAnnotation(BaseAnnotationProxy annotation) {
        Marker marker = getMapboxMarker((AnnotationProxy) annotation);
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        } else {
            marker = getOrCreateMapboxMarker((AnnotationProxy) annotation);
        }

        final Marker fMarker = marker;
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.addMarker(fMarker);
            }
        });
    }

    @Override
    public void handleRemoveMarker(AkylasMarker marker) {
        if (marker instanceof MapboxMarker) {
            final Marker mapBoxMarker = ((MapboxMarker) marker).getMarker();
            if (mapBoxMarker != null) {
                proxy.runInUiThread(new CommandNoReturn() {
                    @Override
                    public void execute() {
                        map.addMarker(mapBoxMarker);
                    }
                });
            }
            BaseAnnotationProxy annotation = marker.getProxy();
            if (annotation != null) {
                annotation.setMarker(null);
                annotation.setParentForBubbling(null);
                annotation.setMapView(null);
            }
        }
    }

    @Override
    protected void removeAllAnnotations() {
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                map.clear();
            }
        });
    }

    public void fireLongClickEvent(ILatLng point) {
        fireEventOnMap(TiC.EVENT_LONGPRESS, point);
    }

    public void fireClickEvent(final Marker marker, final String source) {
        fireEventOnMarker(TiC.EVENT_CLICK, marker, source);
    }

    protected void fireEventOnMarker(String type, Marker marker,
            String clickSource) {
        fireEventOnMarker(type, getAkMarker(marker), clickSource);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        fireClickEvent(marker, AkylasMapBaseModule.PROPERTY_PIN);
        return true;
    }

    @Override
    public void onMapClick(ILatLng point) {
        fireEventOnMap(TiC.EVENT_CLICK, point);
    }

    @Override
    public void onMapLongClick(ILatLng point) {
        fireLongClickEvent(point);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AkylasMarker akMarker = getAkMarker(marker);
        if (akMarker != null) {
            String clicksource = akMarker.getProxy().getMapInfoWindow()
                    .getClicksource();
            // The clicksource is null means the click event is not inside
            // "leftPane", "title", "subtible"
            // or "rightPane". In this case, use "infoWindow" as the
            // clicksource.
            if (clicksource == null) {
                clicksource = AkylasMapBaseModule.PROPERTY_INFO_WINDOW;
            }
            fireClickEvent(marker, clicksource);
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
    }

    @Override
    public boolean onMarkerLongClick(Marker marker) {
        fireEventOnMarker(TiC.EVENT_LONGPRESS, marker,
                AkylasMapBaseModule.PROPERTY_PIN);
        return false;
    }

    @Override
    public void onInfoWindowShow(Marker marker) {
        selectedAnnotation = getAkMarker(marker).getProxy();
        fireEventOnMarker(TiC.EVENT_FOCUS, getAkMarker(marker),
                AkylasMapBaseModule.PROPERTY_PIN);
    }

    @Override
    public void onInfoWindowHide(Marker marker) {
        selectedAnnotation = null;
        fireEventOnMarker(TiC.EVENT_BLUR, getAkMarker(marker),
                AkylasMapBaseModule.PROPERTY_PIN);
    }

    @Override
    public void onCameraChange(BoundingBox box, float zoom) {
        if (proxy != null
                && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
            KrollDict result = new KrollDict();
            result.put(TiC.PROPERTY_REGION, AkylasMapboxModule.getFactory()
                    .regionToDict(box));
            result.put(AkylasMapBaseModule.PROPERTY_ZOOM, zoom);
            proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
        }

    }

    @Override
    public void onMarkerDragCancel(Marker marker) {
        BaseAnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(annoProxy,
                    AkylasMapBaseModule.ANNOTATION_DRAG_STATE_CANCEL);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        BaseAnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(annoProxy,
                    AkylasMapBaseModule.ANNOTATION_DRAG_STATE_DRAGGING);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        BaseAnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            LatLng position = marker.getPosition();
            annoProxy.setProperty(TiC.PROPERTY_LONGITUDE,
                    position.getLongitude());
            annoProxy
                    .setProperty(TiC.PROPERTY_LATITUDE, position.getLatitude());
            firePinChangeDragStateEvent(annoProxy,
                    AkylasMapBaseModule.ANNOTATION_DRAG_STATE_END);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        BaseAnnotationProxy annoProxy = getAkMarker(marker).getProxy();
        if (annoProxy != null) {
            firePinChangeDragStateEvent(annoProxy,
                    AkylasMapBaseModule.ANNOTATION_DRAG_STATE_START);
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
