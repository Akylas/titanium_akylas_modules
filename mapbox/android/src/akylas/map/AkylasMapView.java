/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;

import akylas.map.AnnotationProxy;
import akylas.map.RouteProxy;
import akylas.map.AkylasMarker;
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

public class AkylasMapView extends AkylasMapDefaultView implements
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
    private static AkylasMapView currentMapHolder;

    private float mRequiredZoomLevel = 10;
    private CameraPosition currentCameraPosition = null;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    private static final int MSG_GET_PROJECTION = 10001;
    private static final int MSG_GET_MYLOCATION = 10002;
    private static final int MSG_GET_MYLOCATION_ENABLED = 10003;

    private static boolean INITIALIZED = false;
    // private FollowMeLocationSource followMeLocationSource = new
    // FollowMeLocationSource();

    private boolean googlePlayServicesAvailable = false;

    private List<RouteProxy> addedRoutes = new ArrayList<RouteProxy>();
    private List<TileSourceProxy> addedTileSources = new ArrayList<TileSourceProxy>();

    protected static final int TIFLAG_NEEDS_CAMERA = 0x00000001;

    public AkylasMapView(final TiViewProxy proxy, final Activity activity) {
        super(proxy);
        googlePlayServicesAvailable = ((MapViewProxy) proxy)
                .googlePlayServicesAvailable();
        if (googlePlayServicesAvailable) {
            try {
                MapsInitializer.initialize(activity);
            } catch (Exception e) {
            }
        }

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

        if (googlePlayServicesAvailable) {
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

    private void setMapListeners(GoogleMap theMap, AkylasMapView mapView) {
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

        for (TileSourceProxy tileSource : addedTileSources) {
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
                    proxy.getProperty(AkylasMapModule.PROPERTY_ZORDER_ON_TOP),
                    false);
            GoogleMapOptions gOptions = new GoogleMapOptions();
            gOptions.zOrderOnTop(zOrderOnTop);
            return SupportMapFragment.newInstance(gOptions);
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;
    static {
        ArrayList<String> tmp = AkylasMapDefaultView.KEY_SEQUENCE;
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
        case AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            mRequiredZoomLevel = TiConvert.toFloat(newValue, 10);
            break;
        case AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON:
            map.getUiSettings().setMyLocationButtonEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_ZOOM_CONTROLS_ENABLED:
            map.getUiSettings().setZoomControlsEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_COMPASS_ENABLED:
            map.getUiSettings().setCompassEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_TOOLBAR_ENABLED:
            map.getUiSettings().setMapToolbarEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_INDOOR_CONTROLS_ENABLED:
            map.getUiSettings().setIndoorLevelPickerEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_SCROLL_ENABLED:
            map.getUiSettings().setScrollGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case TiC.PROPERTY_ZOOM_ENABLED:
            map.getUiSettings().setZoomGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_ROTATE_ENABLED:
            map.getUiSettings().setRotateGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_TILT_ENABLED:
            map.getUiSettings().setTiltGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_BUILDINGS_ENABLED:
            map.setBuildingsEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_INDOOR_ENABLED:
            map.setIndoorEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapModule.PROPERTY_TRAFFIC:
            map.setTrafficEnabled(TiConvert.toBoolean(newValue, false));
            break;
        case TiC.PROPERTY_BEARING:
            getCameraBuilder().bearing(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasMapModule.PROPERTY_TILT:
            getCameraBuilder().tilt(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasMapModule.PROPERTY_ZOOM:
            getCameraBuilder().zoom(TiConvert.toFloat(newValue, 0));
            break;
        case TiC.PROPERTY_REGION:
            getCameraBuilder();
            mCameraRegion = AkylasMapModule.mapBoxToGoogle(AkylasMapModule
                    .regionFromDict(newValue));
            mCameraRegionUpdate = mCameraRegion != null;
            break;
        case AkylasMapModule.PROPERTY_CENTER_COORDINATE:
            getCameraBuilder().target(
                    AkylasMapModule.mapBoxToGoogle(AkylasMapModule
                            .latlongFromObject(newValue)));
            break;
        case TiC.PROPERTY_MAP_TYPE:
            map.setMapType(TiConvert.toInt(newValue, GoogleMap.MAP_TYPE_NORMAL));
            break;

        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    // protected void onViewCreated() {
    // acquireMap();
    // if (map == null)
    // return;
    // // A workaround for
    // // https://code.google.com/p/android/issues/detail?id=11676 pre Jelly
    // // Bean.
    // // This problem doesn't exist on 4.1+ since the map base view changes to
    // // TextureView from SurfaceView.
    //
    // // processMapProperties(proxy.getProperties());
    // }

    // private static final ArrayList<String> KEY_SEQUENCE;
    // static{
    // ArrayList<String> tmp = AkylasMapDefaultView.KEY_SEQUENCE;
    // tmp.add(TiC.PROPERTY_COLOR);
    // KEY_SEQUENCE = tmp;
    // }
    // @Override
    // protected ArrayList<String> keySequence() {
    // return KEY_SEQUENCE;
    // }
    // @Override
    // public void processProperties(KrollDict d) {
    // super.processProperties(d);
    // }
    //
    // @Override
    // public void processPreMapProperties(final KrollDict d) {
    // super.processPreMapProperties(d);
    // if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON)) {
    // setUserLocationButtonEnabled(TiConvert.toBoolean(d,
    // AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON, true));
    // }
    // if (d.containsKey(TiC.PROPERTY_MAP_TYPE)) {
    // setMapType(d.getInt(TiC.PROPERTY_MAP_TYPE));
    // }
    // if (d.containsKey(AkylasMapModule.PROPERTY_TRAFFIC)) {
    // setTrafficEnabled(d.getBoolean(AkylasMapModule.PROPERTY_TRAFFIC));
    // }
    // if (d.containsKey(TiC.PROPERTY_ENABLE_ZOOM_CONTROLS)) {
    // setZoomControlsEnabled(TiConvert.toBoolean(d,
    // TiC.PROPERTY_ENABLE_ZOOM_CONTROLS, true));
    // }
    // if (d.containsKey(AkylasMapModule.PROPERTY_COMPASS_ENABLED)) {
    // setCompassEnabled(TiConvert.toBoolean(d,
    // AkylasMapModule.PROPERTY_COMPASS_ENABLED, true));
    // }
    // }
    //
    // @Override
    // public void processMapProperties(final KrollDict d) {
    // if (acquireMap() == null)
    // return;
    // super.processMapProperties(d);
    // }
    //
    // @Override
    // public void processPostMapProperties(final KrollDict d,
    // final boolean animated) {
    // super.processPostMapProperties(d, animated);
    // }

    // @Override
    // public void propertyChanged(String key, Object oldValue, Object newValue,
    // KrollProxy proxy) {
    // if (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON)) {
    // setUserLocationButtonEnabled(TiConvert.toBoolean(newValue));
    // } else if (key.equals(TiC.PROPERTY_MAP_TYPE)) {
    // setMapType(TiConvert.toInt(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_TRAFFIC)) {
    // setTrafficEnabled(TiConvert.toBoolean(newValue));
    // } else if (key.equals(AkylasMapModule.PROPERTY_COMPASS_ENABLED)) {
    // setCompassEnabled(TiConvert.toBoolean(newValue, true));
    // } else if (key.equals(TiC.PROPERTY_ENABLE_ZOOM_CONTROLS)) {
    // setZoomControlsEnabled(TiConvert.toBoolean(newValue, true));
    // } else {
    // super.propertyChanged(key, oldValue, newValue, proxy);
    // }
    // }

    // @Override
    // public void processMapPositioningProperties(final KrollDict d,
    // final boolean animated) {
    // updateCamera(d, animated);
    // }
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

    // public void updateCamera(HashMap<String, Object> dict,
    // final boolean animated) {
    // if (preLayout)
    // return;
    // float bearing = TiConvert.toFloat(dict, TiC.PROPERTY_BEARING, 0);
    // float tilt = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_TILT, 0);
    // float zoom = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_ZOOM, 0);
    // boolean anim = animated
    // && TiConvert.toBoolean(dict, TiC.PROPERTY_ANIMATE, true);
    //
    // boolean regionUpdate = false;
    // LatLngBounds region =
    // map.getProjection().getVisibleRegion().latLngBounds;
    // LatLng center = region.getCenter();
    // if (dict.containsKey(TiC.PROPERTY_REGION)) {
    // region = AkylasMapModule.mapBoxToGoogle(AkylasMapModule
    // .regionFromDict(dict.get(TiC.PROPERTY_REGION)));
    // regionUpdate = region != null;
    // }
    //
    // if (dict.containsKey(AkylasMapModule.PROPERTY_CENTER_COORDINATE)) {
    // center = AkylasMapModule.mapBoxToGoogle(AkylasMapModule
    // .latlongFromObject(dict
    // .get(AkylasMapModule.PROPERTY_CENTER_COORDINATE)));
    // }
    //
    // CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
    // cameraBuilder.target(center);
    // cameraBuilder.bearing(bearing);
    // cameraBuilder.tilt(tilt);
    // cameraBuilder.zoom(zoom);
    //
    // if (regionUpdate) {
    // moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0), anim);
    // } else {
    // CameraPosition position = cameraBuilder.build();
    // CameraUpdate camUpdate = CameraUpdateFactory
    // .newCameraPosition(position);
    // moveCamera(camUpdate, anim);
    // }
    // }

    protected void moveCamera(CameraUpdate camUpdate, boolean anim) {
        if (map == null)
            return;
        if (anim) {
            map.animateCamera(camUpdate);
        } else {
            map.moveCamera(camUpdate);
        }
    }

    // public GoogleMap acquireMap() {
    //
    // if (googlePlayServicesAvailable) {
    // if (map == null) {
    // mapView = ((SupportMapFragment) getFragment()).getView();
    // map = ((SupportMapFragment) getFragment()).getMap();
    // if (map != null) {
    // proxy.realizeViews(this, true, true);
    // if (Build.VERSION.SDK_INT < 16) {
    // View rootView = proxy.getActivity().findViewById(
    // android.R.id.content);
    // setBackgroundTransparent(rootView);
    // }
    // map.setOnMarkerClickListener(this);
    // map.setOnMapClickListener(this);
    // map.setOnCameraChangeListener(this);
    // map.setOnMarkerDragListener(this);
    // map.setOnInfoWindowClickListener(this);
    // map.setInfoWindowAdapter(this);
    // map.setOnMapLongClickListener(this);
    // map.setOnMapLoadedCallback(this);
    // map.setOnMyLocationChangeListener(this);
    // // addAnnotations(((MapDefaultViewProxy) proxy)
    // // .getAnnotations());
    // // setTileSources(((MapDefaultViewProxy) proxy).getHandledTileSources());
    // }
    // }
    // }
    // return map;
    // }

    public GoogleMap getMap() {
        return map;
    }

    protected void setUserLocationEnabled(boolean enabled) {
        map.setMyLocationEnabled(enabled);
    }

    protected void setCompassEnabled(boolean enabled) {
        map.getUiSettings().setCompassEnabled(enabled);
    }

    // protected void setUserLocationButtonEnabled(boolean enabled) {
    // map.getUiSettings().setMyLocationButtonEnabled(enabled);
    // }

    public float getMaxZoomLevel() {
        if (map == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasMapModule.PROPERTY_MAXZOOM), 0);
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
                    proxy.getProperty(AkylasMapModule.PROPERTY_MAXZOOM), 0);
        }
    }

    public float getMinZoomLevel() {
        if (map == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasMapModule.PROPERTY_MINZOOM), 0);
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
                    proxy.getProperty(AkylasMapModule.PROPERTY_MINZOOM), 0);
        }
    }

    @Override
    public float getZoomLevel() {
        if (currentCameraPosition == null) {
            return TiConvert.toFloat(
                    proxy.getProperty(AkylasMapModule.PROPERTY_ZOOM), 0);
        }
        return currentCameraPosition.zoom;
    }

    // protected void setMapType(int type) {
    // map.setMapType(type);
    // }

    // protected void setTrafficEnabled(boolean enabled) {
    // map.setTrafficEnabled(enabled);
    // }

    // protected void setZoomControlsEnabled(boolean enabled) {
    // map.getUiSettings().setZoomControlsEnabled(enabled);
    // }

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

    // protected void changeZoomLevel(final float level) {
    // changeZoomLevel(level, animate);
    // }
    //
    @Override
    protected void changeZoomLevel(final float level, final boolean animated) {
        // handled by propertySet
        // if (preLayout)
        // return;
        // CameraUpdate camUpdate = CameraUpdateFactory.zoomBy(level);
        // moveCamera(camUpdate, animated);
    }

    protected void fireEventOnMap(String type, LatLng point) {
        if (!hasListeners(type, false))
            return;
        KrollDict d = new KrollDict();
        d.put(TiC.PROPERTY_LATITUDE, point.latitude);
        d.put(TiC.PROPERTY_LONGITUDE, point.longitude);
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasMapModule.PROPERTY_ZOOM, currentCameraPosition.zoom);
        d.put(AkylasMapModule.PROPERTY_MAP, proxy);
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
        if (proxy.hasListeners(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE,
                false)) {
            KrollDict d = new KrollDict();

            d.put(TiC.PROPERTY_TITLE, annoProxy.getTitle());
            d.put(TiC.PROPERTY_SUBTITLE, annoProxy.getSubtitle());
            d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
            d.put(TiC.PROPERTY_SOURCE, proxy);
            d.put(AkylasMapModule.PROPERTY_NEWSTATE, dragState);
            proxy.fireEvent(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE, d,
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
        fireClickEvent(marker, AkylasMapModule.PROPERTY_PIN);

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
            // LatLng position = marker.getPosition();
            // annoProxy.setProperty(TiC.PROPERTY_LONGITUDE,
            // position.longitude);
            // annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
            firePinChangeDragStateEvent(marker, annoProxy,
                    AkylasMapModule.ANNOTATION_DRAG_STATE_DRAGGING);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            LatLng position = marker.getPosition();
            annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.longitude);
            annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
            firePinChangeDragStateEvent(marker, annoProxy,
                    AkylasMapModule.ANNOTATION_DRAG_STATE_END);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            firePinChangeDragStateEvent(marker, annoProxy,
                    AkylasMapModule.ANNOTATION_DRAG_STATE_START);
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
                clicksource = AkylasMapModule.PROPERTY_INFO_WINDOW;
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
        if (preLayout) {

            // moveCamera will trigger another callback, so we do this to make
            // sure
            // we don't fire event when region is set initially
            preLayout = false;
            handleCameraUpdate();
        } else if (map != null) {
            for (RouteProxy route : addedRoutes) {
                route.onMapCameraChange(map, position);
            }
            if (proxy != null
                    && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                KrollDict result = new KrollDict();
                result.put(TiC.PROPERTY_REGION,
                        AkylasMapModule.regionToDict(bounds));
                result.put(AkylasMapModule.PROPERTY_ZOOM, position.zoom);
                proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (proxy.hasListeners(AkylasMapModule.EVENT_LOCATION_BUTTON, false)) {
            proxy.fireEvent(AkylasMapModule.EVENT_LOCATION_BUTTON, null, false,
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
                if (proxy.hasListeners(AkylasMapModule.EVENT_ON_SNAPSHOT_READY, false)) {
                    TiBlob sblob = TiBlob.blobFromObject(snapshot);
                    KrollDict data = new KrollDict();
                    data.put("snapshot", sblob);
                    data.put("source", proxy);
                    proxy.fireEvent(AkylasMapModule.EVENT_ON_SNAPSHOT_READY,
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
            return AkylasMapModule.locationToDict(map.getMyLocation());
        } else {
            return AkylasMapModule.locationToDict((Location) TiMessenger
                    .sendBlockingMainMessage(mainHandler
                            .obtainMessage(MSG_GET_MYLOCATION)));
        }
    }

    @Override
    boolean getUserLocationEnabled() {
        if (TiApplication.isUIThread()) {
            return map.isMyLocationEnabled();
        } else {
            return (Boolean) TiMessenger.sendBlockingMainMessage(mainHandler
                    .obtainMessage(MSG_GET_MYLOCATION_ENABLED));
        }
    }

    @Override
    int getUserTrackingMode() {
        return 0;
    }

    @Override
    void handleMinZoomLevel(float level) {
    }

    @Override
    void handleMaxZoomLevel(float level) {

    }

    /*
     * Our custom LocationSource. We register this class to receive location
     * updates from the Location Manager and for that reason we need to also
     * implement the LocationListener interface.
     */
    // private class FollowMeLocationSource implements LocationSource,
    // LocationListener {
    //
    // private OnLocationChangedListener mListener;
    // private LocationManager locationManager;
    // private final Criteria criteria = new Criteria();
    // private String bestAvailableProvider;
    // /*
    // * Updates are restricted to one every 10 seconds, and only when
    // * movement of more than 10 meters has been detected.
    // */
    // private final int minTime = 10000; // minimum time interval between
    // // location updates, in milliseconds
    // private final int minDistance = 10; // minimum distance between location
    // // updates, in meters
    // private boolean withBearing = false;
    //
    // private FollowMeLocationSource() {
    // // Get reference to Location Manager
    //
    // locationManager = (LocationManager) proxy.getActivity()
    // .getSystemService(Context.LOCATION_SERVICE);
    //
    // // Specify Location Provider criteria
    // criteria.setAccuracy(Criteria.ACCURACY_FINE);
    // criteria.setPowerRequirement(Criteria.POWER_LOW);
    // criteria.setAltitudeRequired(true);
    // criteria.setBearingRequired(true);
    // criteria.setSpeedRequired(true);
    // criteria.setCostAllowed(true);
    // }
    //
    // public void setBearingEnabled(final boolean value) {
    // withBearing = value;
    // }
    //
    // private void getBestAvailableProvider() {
    // /*
    // * The preffered way of specifying the location provider (e.g. GPS,
    // * NETWORK) to use is to ask the Location Manager for the one that
    // * best satisfies our criteria. By passing the 'true' boolean we ask
    // * for the best available (enabled) provider.
    // */
    // bestAvailableProvider = locationManager.getBestProvider(criteria,
    // true);
    // }
    //
    // /*
    // * Activates this provider. This provider will notify the supplied
    // * listener periodically, until you call deactivate(). This method is
    // * automatically invoked by enabling my-location layer.
    // */
    // @Override
    // public void activate(OnLocationChangedListener listener) {
    // // We need to keep a reference to my-location layer's listener so we
    // // can push forward
    // // location updates to it when we receive them from Location
    // // Manager.
    // mListener = listener;
    //
    // // Request location updates from Location Manager
    // if (bestAvailableProvider != null) {
    // locationManager.requestLocationUpdates(bestAvailableProvider,
    // minTime, minDistance, this);
    // } else {
    // // (Display a message/dialog) No Location Providers currently
    // // available.
    // }
    // }
    //
    // /*
    // * Deactivates this provider. This method is automatically invoked by
    // * disabling my-location layer.
    // */
    // @Override
    // public void deactivate() {
    // // Remove location updates from Location Manager
    // locationManager.removeUpdates(this);
    //
    // mListener = null;
    // }
    //
    // @Override
    // public void onLocationChanged(Location location) {
    // /*
    // * Push location updates to the registered listener.. (this ensures
    // * that my-location layer will set the blue dot at the new/received
    // * location)
    // */
    // if (mListener != null) {
    // mListener.onLocationChanged(location);
    // }
    //
    // }
    //
    // @Override
    // public void onStatusChanged(String s, int i, Bundle bundle) {
    //
    // }
    //
    // @Override
    // public void onProviderEnabled(String s) {
    //
    // }
    //
    // @Override
    // public void onProviderDisabled(String s) {
    //
    // }
    // }

    private AkylasMapModule.TrackingMode mUserTrackingMode = AkylasMapModule.TrackingMode.NONE;

    @Override
    void setUserTrackingMode(int value) {
        mUserTrackingMode = AkylasMapModule.TrackingMode.values()[value];
        setShouldFollowUserLocation(true);
        // switch (mUserTrackingMode) {
        // case NONE:
        // followMeLocationSource.deactivate();
        // map.setLocationSource(null);
        // break;
        // case FOLLOW:
        // map.setMyLocationEnabled(true);
        // map.setLocationSource(followMeLocationSource);
        // followMeLocationSource.setBearingEnabled(false);
        // followMeLocationSource.activate(this);
        // break;
        // case FOLLOW_BEARING:
        // map.setMyLocationEnabled(true);
        // map.setLocationSource(followMeLocationSource);
        // followMeLocationSource.setBearingEnabled(true);
        // followMeLocationSource.activate(this);
        // break;
        // default:
        // break;
        // }
    }

    @Override
    void updateCenter(Object dict, boolean animated) {
        // handled by propertySet
        // LatLng center = AkylasMapModule.mapBoxToGoogle(AkylasMapModule
        // .latlongFromObject(dict));
        // if (center != null) {
        // if (preLayout)
        // return;
        // CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
        // cameraBuilder.target(center);
        // CameraPosition position = cameraBuilder.build();
        // CameraUpdate camUpdate = CameraUpdateFactory
        // .newCameraPosition(position);
        // moveCamera(camUpdate, animated);
        //
        // }
    }

    //
    @Override
    void updateRegion(Object dict, boolean animated) {
        // handled by propertySet
        // LatLngBounds region = AkylasMapModule.mapBoxToGoogle(AkylasMapModule
        // .regionFromDict(dict));
        // if (region != null) {
        // if (preLayout)
        // return;
        // moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0), animated);
        // }
    }

    @Override
    void updateScrollableAreaLimit(Object dict) {
    }

    @Override
    void selectUserAnnotation() {
        updateCenter(getUserLocation(), animate);
    }

    @Override
    void zoomIn() {
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
    void zoomIn(com.mapbox.mapboxsdk.geometry.LatLng about, boolean userAction) {
        zoomIn();
    }

    @Override
    void zoomOut() {
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
    void zoomOut(com.mapbox.mapboxsdk.geometry.LatLng about, boolean userAction) {
        zoomOut();
    }

    private Projection getProjection() {
        if (TiApplication.isUIThread()) {
            return map.getProjection();
        } else {
            return (Projection) TiMessenger.sendBlockingMainMessage(mainHandler
                    .obtainMessage(MSG_GET_PROJECTION));
        }
    }

    @Override
    KrollDict getRegionDict() {
        LatLngBounds region = getProjection().getVisibleRegion().latLngBounds;
        return AkylasMapModule.regionToDict(region);
    }

    @Override
    void handleDeselectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).hideInfoWindow();
    }

    @Override
    void handleSelectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).showInfoWindow();
    }

    @Override
    void handleAddRoute(final RouteProxy route) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleAddRoute(route);
                }
            });
            return;
        }
        route.setPolyline(map.addPolyline(route
                .getAndSetOptions(currentCameraPosition)));
        addedRoutes.add(route);
    }

    @Override
    void handleRemoveRoute(final RouteProxy route) {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleRemoveRoute(route);
                }
            });
            return;
        }
        route.removePolyline();
        addedRoutes.remove(route);
    }

    @Override
    void handleAddAnnotation(final AnnotationProxy annotation) {
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
        GoogleMapMarker gMarker = new GoogleMapMarker(annotation);
        Marker googlemarker = map.addMarker(gMarker.getMarkerOptions());
        // we need to set the position again because addMarker can be long and
        // position might already have changed
        googlemarker.setPosition(AkylasMapModule.mapBoxToGoogle(annotation
                .getPosition()));
        gMarker.setMarker(googlemarker);
        annotation.setMarker(gMarker);
        annotation.setParentForBubbling(this.proxy);
        timarkers.add(annotation.getMarker());
    }

    @Override
    void handleRemoveMarker(final AkylasMarker marker) {
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
        AnnotationProxy annotation = marker.getProxy();
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
        for (TileSourceProxy tileSource : addedTileSources) {
            tileSource.release();
        }
        addedTileSources.clear();
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (shouldFollowUserLocation
                && mUserTrackingMode != AkylasMapModule.TrackingMode.NONE) {
            CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
            cameraBuilder.target(new LatLng(location.getLatitude(), location
                    .getLongitude()));
            if (mUserTrackingMode == AkylasMapModule.TrackingMode.FOLLOW_BEARING
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
            d.put(AkylasMapModule.PROPERTY_MAP, proxy);
            proxy.fireEvent(TiC.EVENT_LOCATION, d, false, false);
        }
    }

    private void onUpdateMapAfterUserInterection() {
        setShouldFollowUserLocation(false);
    }

    @Override
    public TileSourceProxy addTileSource(Object object, int index) {

        TileSourceProxy sourceProxy = AkylasMapModule
                .tileSourceProxyFromObject(object);
        if (map == null) {
            return sourceProxy;
        }
        if (sourceProxy != null) {
            if (!getProxy().hasProperty(TiC.PROPERTY_MAP_TYPE)) {
                map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
            TileOverlayOptions options = sourceProxy.getTileOverlayOptions();
            if (options != null) {
                sourceProxy.setTileOverlay(map.addTileOverlay(options
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
                    && props.containsKey(AkylasMapModule.PROPERTY_CENTER_COORDINATE)) {
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
}
