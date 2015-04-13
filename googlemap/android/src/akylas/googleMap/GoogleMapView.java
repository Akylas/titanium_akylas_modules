/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.googleMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.AkylasMapInfoView;
import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseRouteProxy;
import akylas.map.common.BaseTileSourceProxy;
import akylas.map.common.ReusableView;
import android.graphics.RectF;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiActivityHelper.Command;
import org.appcelerator.titanium.util.TiActivityHelper.CommandNoReturn;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class GoogleMapView extends AkylasMapBaseView implements
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMyLocationChangeListener, OnMyLocationButtonClickListener,
        OnMapReadyCallback {
    private static final String TAG = "AkylasMapView";
    private GoogleMap map;
    protected boolean animate = false;
    protected boolean preLayout = true;
    protected LatLngBounds preLayoutUpdateBounds;
    protected ArrayList<AkylasMarker> timarkers;
    private Fragment fragment;
    private static GoogleMapView currentMapHolder;

    private float mRequiredZoomLevel = 10;
    private CameraPosition currentCameraPosition = null;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    private static final int MSG_GET_PROJECTION = 10001;
    private static final int MSG_GET_MYLOCATION = 10002;
    private static final int MSG_GET_MYLOCATION_ENABLED = 10003;
    
    private static int CAMERA_UPDATE_DURATION = 500;

    private static boolean INITIALIZED = false;
    // private FollowMeLocationSource followMeLocationSource = new
    // FollowMeLocationSource();


    private List<RouteProxy> addedRoutes = new ArrayList<RouteProxy>();
    private List<BaseTileSourceProxy> addedTileSources = new ArrayList<BaseTileSourceProxy>();

    protected static final int TIFLAG_NEEDS_CAMERA = 0x00000001;

    public GoogleMapView(final TiViewProxy proxy, final Activity activity) {
        super(proxy);
        final TiCompositeLayout container = new TiCompositeLayout(activity,
                this) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                return interceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
            }

            // @Override
            // protected void onMeasure(int widthMeasureSpec, int
            // heightMeasureSpec) {
            // LatLngBounds region = null;
            // if (map != null) {
            // region = map.getProjection().getVisibleRegion().latLngBounds;
            // }
            // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // if (region != null) {
            // CameraUpdate update;
            // if (regionFit) {
            // update = CameraUpdateFactory.newLatLngBounds(region,
            // getMeasuredWidth(), getMeasuredHeight(), 0);
            // } else {
            // update = CameraUpdateFactory.newLatLngBounds(region, 0);
            // }
            // moveCamera(update, animate);
            // }
            // }
        };
        setNativeView(container);

        if (AkylasGoogleMapModule.googlePlayServicesAvailable()) {
            try {
                if (!INITIALIZED) {
                    MapsInitializer.initialize(activity);
                    INITIALIZED = true;
                }
                // if (currentMapHolder != null) {
                // //there is already an existing view
                // currentMapHolder.releaseFragment();
                // }
                fragment = createFragment();
                fragment.setRetainInstance(false);
                // currentMapHolder = this;

                TiUIHelper.transactionFragment(fragment, container,
                        (FragmentActivity) activity);

                ((SupportMapFragment) fragment).getMapAsync(this);
            } catch (Exception e) {
            }
        }

        timarkers = new ArrayList<AkylasMarker>();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        setMap(map);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

        case MSG_GET_PROJECTION: {
            AsyncResult result = (AsyncResult) msg.obj;
            result.setResult(map.getProjection());
            return true;
        }
        case MSG_GET_MYLOCATION: {
            AsyncResult result = (AsyncResult) msg.obj;
            result.setResult(map.getMyLocation());
            return true;
        }
        case MSG_GET_MYLOCATION_ENABLED: {
            AsyncResult result = (AsyncResult) msg.obj;
            result.setResult(map.isMyLocationEnabled());
            return true;
        }
        default:
            return false;

        }
    }

    private void setMapListeners(GoogleMap theMap, GoogleMapView mapView) {
        theMap.setOnMarkerClickListener(mapView);
        theMap.setOnMapClickListener(mapView);
        theMap.setOnCameraChangeListener(mapView);
        theMap.setOnMarkerDragListener(mapView);
        theMap.setOnInfoWindowClickListener(mapView);
        theMap.setInfoWindowAdapter(mapView);
        theMap.setOnMapLongClickListener(mapView);
        theMap.setOnMapLoadedCallback(mapView);
        theMap.setOnMyLocationChangeListener(mapView);
        theMap.setOnMyLocationButtonClickListener(mapView);
    }

    private void setMap(GoogleMap newMap) {
        if (this.map != null) {
            setMapListeners(this.map, null);
        }
        this.map = newMap;
        if (map != null) {
            proxy.realizeViews(this, true, true);
            if (Build.VERSION.SDK_INT < 16) {
                View rootView = proxy.getActivity().findViewById(
                        android.R.id.content);
                setBackgroundTransparent(rootView);
            }
            setMapListeners(this.map, this);
        }
    }

    /**
     * Traverses through the view hierarchy to locate the SurfaceView and set
     * the background to transparent.
     * 
     * @param v
     *            the root view
     */
    private void setBackgroundTransparent(View v) {
        if (v instanceof SurfaceView) {
            SurfaceView sv = (SurfaceView) v;
            sv.setBackgroundColor(Color.TRANSPARENT);
        }

        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setBackgroundTransparent(viewGroup.getChildAt(i));
            }
        }
    }

    public Fragment getFragment() {
        return fragment;
    }

    //
    // public boolean handleMessage(Message msg) {
    // // we know here that the view is available, so we can process properties
    // acquireMap();
    // return true;
    // }

    private void releaseFragment() {
        if (fragment != null) {
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null && !fragmentManager.isDestroyed()) {
                FragmentTransaction transaction = null;
                Fragment tabFragment = fragmentManager
                        .findFragmentById(android.R.id.tabcontent);
                if (tabFragment != null) {
                    FragmentManager childManager = tabFragment
                            .getChildFragmentManager();
                    transaction = childManager.beginTransaction();
                } else {
                    transaction = fragmentManager.beginTransaction();
                }
                transaction.remove(fragment);
                transaction.commit();
            }
            fragment = null;
            // currentMapHolder = null;
        }
        if (map != null) {
            map.clear();
            map = null;
        }
    }

    @Override
    public void release() {
        releaseFragment();
        selectedAnnotation = null;

        addedRoutes.clear();

        for (BaseTileSourceProxy tileSource : addedTileSources) {
            tileSource.release();
        }
        addedTileSources.clear();

        timarkers.clear();
        super.release();
    }

    protected Fragment createFragment() {
        if (proxy == null) {
            return SupportMapFragment.newInstance();
        } else {
            boolean zOrderOnTop = TiConvert.toBoolean(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_ZORDER_ON_TOP),
                    false);
            GoogleMapOptions gOptions = new GoogleMapOptions();
            gOptions.zOrderOnTop(zOrderOnTop);
            return SupportMapFragment.newInstance(gOptions);
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;
    static {
        ArrayList<String> tmp = AkylasMapBaseView.KEY_SEQUENCE;
        tmp.add(AkylasGoogleMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
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
        case AkylasGoogleMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            mRequiredZoomLevel = TiConvert.toFloat(newValue, 10);
            break;
        case AkylasGoogleMapModule.PROPERTY_USER_LOCATION_BUTTON:
            map.getUiSettings().setMyLocationButtonEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_ZOOM_CONTROLS_ENABLED:
            map.getUiSettings().setZoomControlsEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_COMPASS_ENABLED:
            map.getUiSettings().setCompassEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_TOOLBAR_ENABLED:
            map.getUiSettings().setMapToolbarEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_INDOOR_CONTROLS_ENABLED:
            map.getUiSettings().setIndoorLevelPickerEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_SCROLL_ENABLED:
            map.getUiSettings().setScrollGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case TiC.PROPERTY_ZOOM_ENABLED:
            map.getUiSettings().setZoomGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_ROTATE_ENABLED:
            map.getUiSettings().setRotateGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_TILT_ENABLED:
            map.getUiSettings().setTiltGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_BUILDINGS_ENABLED:
            map.setBuildingsEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_INDOOR_ENABLED:
            map.setIndoorEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGoogleMapModule.PROPERTY_TRAFFIC:
            map.setTrafficEnabled(TiConvert.toBoolean(newValue, false));
            break;
        case TiC.PROPERTY_BEARING:
            getCameraBuilder().bearing(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasGoogleMapModule.PROPERTY_TILT:
            getCameraBuilder().tilt(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasGoogleMapModule.PROPERTY_ZOOM:
            getCameraBuilder().zoom(TiConvert.toFloat(newValue, 0));
            break;
        case TiC.PROPERTY_REGION:
            getCameraBuilder();
            mCameraRegion = AkylasGoogleMapModule.regionFromObject(newValue);
            mCameraRegionUpdate = mCameraRegion != null;
            break;
        case AkylasGoogleMapModule.PROPERTY_CENTER_COORDINATE:
            getCameraBuilder().target((LatLng) AkylasGoogleMapModule.latlongFromObject(newValue));
            break;
        case TiC.PROPERTY_MAP_TYPE:
            int type = TiConvert.toInt(newValue, AkylasGoogleMapModule.MAP_TYPE_NORMAL);
            int googleType = GoogleMap.MAP_TYPE_NORMAL;
            switch (type) {
            case AkylasGoogleMapModule.MAP_TYPE_HYBRID:
                googleType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case AkylasGoogleMapModule.MAP_TYPE_SATELLITE:
                googleType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case AkylasGoogleMapModule.MAP_TYPE_TERRAIN:
                googleType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            default:
                break;
            }
            map.setMapType(googleType);
            break;
        case TiC.PROPERTY_PADDING:
            RectF padding  =  TiConvert.toPaddingRect(newValue);
            map.setPadding((int)padding.left, (int)padding.top, (int)padding.right,
                (int)padding.bottom);
            break;
        
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    boolean mCameraRegionUpdate = false;
    boolean mCameraAnimate = false;
    LatLngBounds mCameraRegion = null;
    LatLng mCameraCenter = null;
    CameraPosition.Builder mCameraBuilder = null;

    private CameraPosition.Builder getCameraBuilder() {
        if (mCameraBuilder == null) {
            mProcessUpdateFlags |= TIFLAG_NEEDS_CAMERA;
            mCameraBuilder = new CameraPosition.Builder();
            if (currentCameraPosition != null) {
                mCameraBuilder.target(currentCameraPosition.target)
                        .zoom(currentCameraPosition.zoom)
                        .tilt(currentCameraPosition.tilt)
                        .bearing(currentCameraPosition.bearing);
            }
        }
        return mCameraBuilder;
    }

    private void handleCameraUpdate() {
        if (preLayout || mCameraBuilder == null)
            return;

        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleCameraUpdate();
                }
            });
            return;
        }
        boolean animate = mCameraAnimate || shouldAnimate();
        if (mCameraRegionUpdate) {
            CameraUpdate update;
            if (regionFit) {
                update = CameraUpdateFactory.newLatLngBounds(mCameraRegion,
                        nativeView.getMeasuredWidth(),
                        nativeView.getMeasuredHeight(), 0);
            } else {
                update = CameraUpdateFactory.newLatLngBounds(mCameraRegion, 0);
            }
            moveCamera(update, animate);
        } else {
            CameraPosition position = mCameraBuilder.build();
            CameraUpdate camUpdate = CameraUpdateFactory
                    .newCameraPosition(position);
            moveCamera(camUpdate, animate);
        }
        mCameraBuilder = null;
        mCameraRegionUpdate = false;
        mCameraAnimate = false;
        mCameraRegion = null;
        mCameraCenter = null;
    }

    @Override
    protected void didProcessProperties() {

        if ((mProcessUpdateFlags & TIFLAG_NEEDS_CAMERA) != 0) {
            handleCameraUpdate();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_CAMERA;
        }
        super.didProcessProperties();
    }

    protected void moveCamera(CameraUpdate camUpdate, boolean anim) {
        if (map == null)
            return;
        if (anim) {
            map.animateCamera(camUpdate, CAMERA_UPDATE_DURATION, null);
        } else {
            map.moveCamera(camUpdate);
        }
    }

    public GoogleMap getMap() {
        return map;
    }
    
    @Override
    public void setUserLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    protected void setCompassEnabled(boolean enabled) {
        map.getUiSettings().setCompassEnabled(enabled);
    }

    public float getMaxZoomLevel() {
        if (map == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_MAXZOOM), 0);
        }
        FutureTask<Float> futureResult = new FutureTask<Float>(
                new Callable<Float>() {
                    @Override
                    public Float call() throws Exception {
                        return map.getMaxZoomLevel();
                    }
                });
        // this block until the result is calculated!
        getProxy().getActivity().runOnUiThread(futureResult);
        try {
            return futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_MAXZOOM), 0);
        }
    }

    public float getMinZoomLevel() {
        if (map == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_MINZOOM), 0);
        }

        FutureTask<Float> futureResult = new FutureTask<Float>(
                new Callable<Float>() {
                    @Override
                    public Float call() throws Exception {
                        return map.getMinZoomLevel();
                    }
                });
        // this block until the result is calculated!
        getProxy().getActivity().runOnUiThread(futureResult);
        try {
            return futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_MINZOOM), 0);
        }
    }
    
    private float targetZoom = -1;
    @Override
    public float getZoomLevel() {
        if (targetZoom != -1) {
            return targetZoom;
        }
        if (currentCameraPosition == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasGoogleMapModule.PROPERTY_ZOOM), 0);
        }
        return currentCameraPosition.zoom;
    }

    private AnnotationProxy getProxyByMarker(Marker m) {
        if (m != null) {
            for (int i = 0; i < timarkers.size(); i++) {
                GoogleMapMarker timarker = (GoogleMapMarker) timarkers.get(i);
                if (m.equals(timarker.getMarker())) {
                    return timarker.getProxy();
                }
            }
        }
        return null;
    }

    @Override
    public void changeZoomLevel(final float level, final boolean animated) {
        targetZoom = level;
         CameraUpdate camUpdate = CameraUpdateFactory.zoomBy(level);
         moveCamera(camUpdate, animated);
    }

    protected void fireEventOnMap(String type, LatLng point) {
        if (!hasListeners(type, false))
            return;
        KrollDict d = new KrollDict();
        d.put(TiC.PROPERTY_LATITUDE, point.latitude);
        d.put(TiC.PROPERTY_LONGITUDE, point.longitude);
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasGoogleMapModule.PROPERTY_ZOOM, currentCameraPosition.zoom);
        d.put(AkylasGoogleMapModule.PROPERTY_MAP, proxy);
        fireEvent(type, d, false, false);
    }

    public void fireLongClickEvent(LatLng point) {
        fireEventOnMap(TiC.EVENT_LONGPRESS, point);
    }

    public void fireClickEvent(final Marker marker, final String source) {
        AnnotationProxy proxy = getProxyByMarker(marker);
        fireEventOnMarker(TiC.EVENT_CLICK, proxy.getMarker(), source);
    }

    public void firePinChangeDragStateEvent(final Marker marker,
            final AnnotationProxy annoProxy, int dragState) {
        if (proxy.hasListeners(AkylasGoogleMapModule.EVENT_PIN_CHANGE_DRAG_STATE,
                false)) {
            KrollDict d = new KrollDict();

            d.put(TiC.PROPERTY_TITLE, annoProxy.getTitle());
            d.put(TiC.PROPERTY_SUBTITLE, annoProxy.getSubtitle());
            d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
            d.put(TiC.PROPERTY_SOURCE, proxy);
            d.put(AkylasGoogleMapModule.PROPERTY_NEWSTATE, dragState);
            proxy.fireEvent(AkylasGoogleMapModule.EVENT_PIN_CHANGE_DRAG_STATE, d,
                    false, false);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy == null) {
            Log.e(TAG, "Marker can not be found, click event won't fired.",
                    Log.DEBUG_MODE);
            return false;
        }
        if (!annoProxy.equals(selectedAnnotation)) {
            if (selectedAnnotation != null) {
                selectedAnnotation.hideInfo();
            }
            selectedAnnotation = annoProxy;

        }
        fireClickEvent(marker, AkylasGoogleMapModule.PROPERTY_PIN);

        // Returning false here will enable native behavior, which shows the
        // info window.
        return !annoProxy.canShowInfoWindow();
    }

    @Override
    public void onMapClick(LatLng point) {
        if (selectedAnnotation != null) {
            deselectAnnotation(selectedAnnotation);
        }
        fireEventOnMap(TiC.EVENT_CLICK, point);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        fireLongClickEvent(point);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {

            firePinChangeDragStateEvent(annoProxy,
                    AkylasGoogleMapModule.ANNOTATION_DRAG_STATE_DRAGGING);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            LatLng position = marker.getPosition();
            annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.longitude);
            annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
            firePinChangeDragStateEvent(annoProxy,
                    AkylasGoogleMapModule.ANNOTATION_DRAG_STATE_END);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            firePinChangeDragStateEvent(annoProxy,
                    AkylasGoogleMapModule.ANNOTATION_DRAG_STATE_START);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            String clicksource = annoProxy.getMapInfoWindow().getClicksource();
            // The clicksource is null means the click event is not inside
            // "leftPane", "title", "subtible"
            // or "rightPane". In this case, use "infoWindow" as the
            // clicksource.
            if (clicksource == null) {
                clicksource = AkylasGoogleMapModule.PROPERTY_INFO_WINDOW;
            }
            fireClickEvent(marker, clicksource);
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                annoProxy.prepareInfoView(annoProxy.getMapInfoWindow());
                marker.showInfoWindow();
            } else {
                AkylasMapInfoView infoView = (AkylasMapInfoView) mInfoWindowCache
                        .get("infoView");
                annoProxy.prepareInfoView(infoView);
                return infoView;
            }
            return annoProxy.getMapInfoWindow();
        }
        return null;
    }

    public void infoWindowDidClose(AkylasMapInfoView infoView) {
        if (infoView == null)
            return;
        if (_calloutUsesTemplates) {
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
        mInfoWindowCache.put("infoView", infoView);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        currentCameraPosition = position;
        targetZoom = -1;
        if (preLayout) {

            // moveCamera will trigger another callback, so we do this to make
            // sure
            // we don't fire event when region is set initially
            preLayout = false;
            handleCameraUpdate();
        } else if (map != null) {
//            for (BaseRouteProxy route : addedRoutes) {
//                ((RouteProxy) route).onMapCameraChange(map, position);
//            }
            if (proxy != null
                    && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                KrollDict result = new KrollDict();
                result.put(TiC.PROPERTY_REGION, AkylasGoogleMapModule.getFactory().regionToDict(bounds));
                result.put(AkylasGoogleMapModule.PROPERTY_ZOOM, position.zoom);
                proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (proxy.hasListeners(AkylasGoogleMapModule.EVENT_LOCATION_BUTTON, false)) {
            proxy.fireEvent(AkylasGoogleMapModule.EVENT_LOCATION_BUTTON, null, false,
                    false);
        }
        return false;
    }

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you
                                                  // can adjust that to your
                                                  // liking

    // Intercept the touch event to find out the correct clicksource if clicking
    // on the info window.
    protected boolean interceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            lastTouched = SystemClock.uptimeMillis();
            break;
        case MotionEvent.ACTION_UP:
            final long now = SystemClock.uptimeMillis();
            if (now - lastTouched > SCROLL_TIME) {
                // Update the map
                onUpdateMapAfterUserInterection();
            }
            break;
        }
        if (selectedAnnotation != null) {
            AkylasMapInfoView infoWindow = selectedAnnotation
                    .getMapInfoWindow();
            AkylasMarker timarker = selectedAnnotation.getMarker();
            if (infoWindow != null && timarker != null) {
                GoogleMapMarker gmarker = ((GoogleMapMarker) timarker);
                Marker marker = gmarker.getMarker();
                if (marker != null && marker.isInfoWindowShown()) {
                    // Get a marker position on the screen
                    Point markerPoint = map.getProjection().toScreenLocation(
                            marker.getPosition());
                    return infoWindow.dispatchMapTouchEvent(ev, markerPoint,
                            gmarker.getIconImageHeight());
                }
            }
        }
        return false;
    }

    public void snapshot() {
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                if (proxy.hasListeners(AkylasGoogleMapModule.EVENT_ON_SNAPSHOT_READY, false)) {
                    TiBlob sblob = TiBlob.blobFromObject(snapshot);
                    KrollDict data = new KrollDict();
                    data.put("snapshot", sblob);
                    data.put("source", proxy);
                    proxy.fireEvent(AkylasGoogleMapModule.EVENT_ON_SNAPSHOT_READY,
                            data, false, false);
                }
            }
        });
    }

    @Override
    public void onMapLoaded() {
        if (proxy.hasListeners(TiC.EVENT_COMPLETE, false)) {
            proxy.fireEvent(TiC.EVENT_COMPLETE, null, false, false);
        }
    }

    @Override
    public KrollDict getUserLocation() {
        if (!getUserLocationEnabled()) {
            return null;
        }
        if (TiApplication.isUIThread()) {
            return AkylasGoogleMapModule.locationToDict(map.getMyLocation());
        } else {
            return AkylasGoogleMapModule.locationToDict((Location) TiMessenger
                    .sendBlockingMainMessage(mainHandler
                            .obtainMessage(MSG_GET_MYLOCATION)));
        }
    }

    @Override
    public boolean getUserLocationEnabled() {
        if (TiApplication.isUIThread()) {
            return map.isMyLocationEnabled();
        } else {
            return (Boolean) TiMessenger.sendBlockingMainMessage(mainHandler
                    .obtainMessage(MSG_GET_MYLOCATION_ENABLED));
        }
    }

    @Override
    public int getUserTrackingMode() {
        return 0;
    }

    @Override
    public void handleMinZoomLevel(float level) {
    }

    @Override
    public void handleMaxZoomLevel(float level) {

    }

    private AkylasGoogleMapModule.TrackingMode mUserTrackingMode = AkylasGoogleMapModule.TrackingMode.NONE;

    @Override
    public void setUserTrackingMode(int value) {
        mUserTrackingMode = AkylasGoogleMapModule.TrackingMode.values()[value];
        setShouldFollowUserLocation(true);
    }

    @Override
    public void updateCenter(Object dict, boolean animated) {
    }

    @Override
    public void updateRegion(Object dict, boolean animated) {
    }

    @Override
    public void updateScrollableAreaLimit(Object dict) {
    }

    @Override
    public void selectUserAnnotation() {
        updateCenter(getUserLocation(), animate);
    }

    @Override
    public void zoomIn() {
        float currentZoom = (currentCameraPosition != null) ? currentCameraPosition.zoom
                : 0;
        float targetZoom = (float) (Math.ceil(currentZoom) + 1);
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 2.25) {
            targetZoom = (float) Math.ceil(currentZoom);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    public void zoomIn(Object about, boolean userAction) {
        zoomIn();
    }

    @Override
    public  void zoomOut() {
        float currentZoom = (currentCameraPosition != null) ? currentCameraPosition.zoom
                : 0;
        float targetZoom = (float) (Math.floor(currentZoom));
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 0.75) {
            targetZoom = (float) (Math.floor(currentZoom) - 1);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    public void zoomOut(Object about, boolean userAction) {
        zoomOut();
    }

    private Projection getProjection() {
        if (map == null) {
            return null;
        }
        return proxy.getValueInUIThread(new Command<Projection>() {
            
            @Override
            public Projection execute() {
                return map.getProjection();
            }            
        }, null);
    }
        

    @Override
    public KrollDict getRegionDict() {
        LatLngBounds region = getProjection().getVisibleRegion().latLngBounds;
        return AkylasGoogleMapModule.getFactory().regionToDict(region);
    }

    @Override
    public void handleDeselectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).hideInfoWindow();
    }

    @Override
    public void handleSelectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).showInfoWindow();
    }

    @Override
    public void handleAddRoute(final BaseRouteProxy route) {
        if (map == null) {
            return;
        }
        final RouteProxy fRoute = (RouteProxy) route;
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                fRoute.setPolyline(map.addPolyline(fRoute.getAndSetOptions(currentCameraPosition)));
                addedRoutes.add(fRoute);
            }
        });
    }

    @Override
    public void handleRemoveRoute(final BaseRouteProxy route) {
        final RouteProxy fRoute = (RouteProxy) route;
        proxy.runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                fRoute.removePolyline();
                addedRoutes.remove(fRoute);
            }
        });
    }

    @Override
    public void handleAddAnnotation(final BaseAnnotationProxy annotation) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleAddAnnotation(annotation);
                }
            });
            return;
        }
        AkylasMarker marker = annotation.getMarker();
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        }
        annotation.setMapView(this);
        GoogleMapMarker gMarker = new GoogleMapMarker((AnnotationProxy) annotation);
        Marker googlemarker = map.addMarker(gMarker.getMarkerOptions());
        // we need to set the position again because addMarker can be long and
        // position might already have changed
        googlemarker.setPosition((LatLng) annotation.getPosition());
        gMarker.setMarker(googlemarker);
        annotation.setMarker(gMarker);
        annotation.setParentForBubbling(this.proxy);
        timarkers.add(annotation.getMarker());
    }

    @Override
    public void handleRemoveMarker(final AkylasMarker marker) {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleRemoveMarker(marker);
                }
            });
            return;
        }
        ((GoogleMapMarker) marker).removeFromMap();
        timarkers.remove(marker);
        AnnotationProxy annotation = (AnnotationProxy) marker.getProxy();
        if (annotation != null) {
            annotation.setMarker(null);
            annotation.setParentForBubbling(null);
            annotation.setMapView(null);
        }
    }

    @Override
    protected void removeAllAnnotations() {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAllAnnotations();
                }
            });
            return;
        }
        map.clear();
    }

    protected void removeAllRoutes() {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAllRoutes();
                }
            });
            return;
        }
        for (RouteProxy route : addedRoutes) {
            route.removePolyline();
        }
        addedRoutes.clear();
    }

    protected void removeAllTileSources() {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAllTileSources();
                }
            });
            return;
        }
        for (BaseTileSourceProxy tileSource : addedTileSources) {
            tileSource.release();
        }
        addedTileSources.clear();
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (shouldFollowUserLocation
                && mUserTrackingMode != AkylasGoogleMapModule.TrackingMode.NONE) {
            CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
            cameraBuilder.target(new LatLng(location.getLatitude(), location
                    .getLongitude()));
            if (mUserTrackingMode == AkylasGoogleMapModule.TrackingMode.FOLLOW_BEARING
                    && location.hasBearing()) {
                cameraBuilder.bearing(location.getBearing());
            }

            float currentZoom = (currentCameraPosition != null) ? currentCameraPosition.zoom
                    : 0;
            if (currentZoom < mRequiredZoomLevel) {
                if (location.hasAccuracy()) {
                    // approx meterPerDegree latitude, plus some margin
                    double delta = (location.getAccuracy() / 110000) * 1.2;
                    final LatLngBounds currentBox = map.getProjection()
                            .getVisibleRegion().latLngBounds;
                    LatLng desiredSouthWest = new LatLng(location.getLatitude()
                            - delta, location.getLongitude() - delta);

                    LatLng desiredNorthEast = new LatLng(location.getLatitude()
                            + delta, location.getLongitude() + delta);

                    if (desiredNorthEast.latitude != currentBox.northeast.latitude
                            || desiredNorthEast.longitude != currentBox.northeast.longitude
                            || desiredSouthWest.latitude != currentBox.southwest.latitude
                            || desiredSouthWest.longitude != currentBox.southwest.longitude) {
                        cameraBuilder.zoom(mRequiredZoomLevel);
                    }

                } else {
                    cameraBuilder.zoom(mRequiredZoomLevel);
                }

            } else {
                cameraBuilder.zoom(currentZoom);
            }
            if (currentCameraPosition != null) {
                cameraBuilder.tilt(currentCameraPosition.tilt);
            }
            CameraPosition position = cameraBuilder.build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
        if (proxy.hasListeners(TiC.EVENT_LOCATION, false)) {
            KrollDict d = new KrollDict();
            d.put(TiC.PROPERTY_LATITUDE, location.getLatitude());
            d.put(TiC.PROPERTY_LONGITUDE, location.getLongitude());
            d.put(TiC.PROPERTY_ALTITUDE, location.getAltitude());
            d.put(TiC.PROPERTY_ACCURACY, location.getAccuracy());
            // d.put(TiC.PROPERTY_ALTITUDE_ACCURACY, null); // Not provided
            d.put(TiC.PROPERTY_HEADING, location.getBearing());
            d.put(TiC.PROPERTY_SPEED, location.getSpeed());
            d.put(TiC.PROPERTY_TIMESTAMP, location.getTime());
            d.put(TiC.PROPERTY_REGION, getRegionDict());
            d.put(AkylasGoogleMapModule.PROPERTY_MAP, proxy);
            proxy.fireEvent(TiC.EVENT_LOCATION, d, false, false);
        }
    }

    private void onUpdateMapAfterUserInterection() {
        setShouldFollowUserLocation(false);
    }

    @Override
    public BaseTileSourceProxy addTileSource(Object object, int index) {
        BaseTileSourceProxy sourceProxy = tileSourceProxyFromObject(object);
        if (map == null) {
            return sourceProxy;
        }
        if (sourceProxy instanceof TileSourceProxy) {
            TileOverlayOptions options = ((TileSourceProxy) sourceProxy).getTileOverlayOptions();
            if (options != null) {
                ((TileSourceProxy) sourceProxy).setTileOverlay(map.addTileOverlay(options
                        .zIndex(index)));
                addedTileSources.add(sourceProxy);
            }
            return sourceProxy;
        }
        return null;
    }

    @Override
    public void removeTileSource(Object object) {
        if (object instanceof TileSourceProxy) {
            ((TileSourceProxy) object).release();
            addedTileSources.remove(object);
        }
    }

    void updateCamera(final KrollDict props) {
        if (props == null)
            return;
        if (preLayout || map == null) {
            props.remove(TiC.PROPERTY_ANIMATE);
            if (!props.containsKey(TiC.PROPERTY_REGION)
                    && props.containsKey(AkylasGoogleMapModule.PROPERTY_CENTER_COORDINATE)) {
                props.put(TiC.PROPERTY_REGION, null);
            }
            proxy.applyProperties(props);
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCamera(props);
                }
            });
            return;
        }

        mCameraAnimate = TiConvert.toBoolean(props, TiC.PROPERTY_ANIMATE,
                shouldAnimate());
        processApplyProperties(props);
    }
    
    
    public void updateMarkerPosition( final Marker marker, final LatLng toPosition) {
        if (!shouldAnimate()) {
            marker.setPosition(toPosition);
            return;
        }
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = CAMERA_UPDATE_DURATION;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
    
    public void updateMarkerHeading( final Marker marker, final float heading) {
//        if (!shouldAnimate()) {
            marker.setRotation(heading);
            return;
//        }
//        final Handler handler = new Handler();
//        final long start = SystemClock.uptimeMillis();
//        Projection proj = map.getProjection();
//        final float startHeading = marker.getRotation();
//        final long duration = CAMERA_UPDATE_DURATION;
//
//        final LinearInterpolator interpolator = new LinearInterpolator();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed
//                        / duration);
//                marker.setRotation(t * heading + (1 - t) * startHeading);
//                if (t < 1.0) {
//                    handler.postDelayed(this, 16);
//                }
//            }
//        });
    }
}
