/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.googlemap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.AkylasMapInfoView;
import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseRouteProxy;
import akylas.map.common.ReusableView;
import android.graphics.RectF;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.kroll.common.TiMessenger.Command;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiViewHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import ti.modules.titanium.ui.widget.TiImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnPolygonClickListener;
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterInfoWindowClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemInfoWindowClickListener;

@SuppressWarnings("deprecation")
public class GoogleMapView extends AkylasMapBaseView implements
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapLoadedCallback, GoogleMap.OnMyLocationChangeListener,
        GoogleMap.OnPoiClickListener, OnMyLocationButtonClickListener,
        GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback, OnClusterClickListener, OnClusterItemClickListener,
        OnClusterItemInfoWindowClickListener, OnClusterInfoWindowClickListener,
        OnPolylineClickListener, OnPolygonClickListener, OnLifecycleEvent {
    private static final String TAG = "AkylasMapView";
    private GoogleMap map;
    protected boolean animate = false;
    protected boolean preLayout = true;
    protected LatLngBounds preLayoutUpdateBounds;
    // protected ArrayList<AkylasMarker> timarkers;
    // protected WeakHashMap<Polyline, RouteProxy> handledPolylines;
    // protected WeakHashMap<Marker, AnnotationProxy> handledMarkers;
    private Fragment fragment;

    private float mRequiredZoomLevel = 10;
    private CameraPosition currentCameraPosition = null;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    private static final int MSG_GET_PROJECTION = 10001;
    private static final int MSG_GET_MYLOCATION = 10002;
    private static final int MSG_GET_MYLOCATION_ENABLED = 10003;

    private static int CAMERA_UPDATE_DURATION = 500;

    private static boolean INITIALIZED = false;
    private RectF padding = null;
    private int cameraAnimationDuration = CAMERA_UPDATE_DURATION;

    private MapView mapView;
    final AbsoluteLayout container;
    protected static final int TIFLAG_NEEDS_CAMERA = 0x00000001;
    protected static final int TIFLAG_NEEDS_MAP_INVALIDATE = 0x00000002;

    // INFOWINDOW
    private AbsoluteLayout.LayoutParams overlayLayoutParams;
    private int popupXOffset;
    private int popupYOffset;
    private static final int POPUP_POSITION_REFRESH_INTERVAL = 16;
    private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;
    private LinearLayout infoWindowContainer;
    private LatLng trackedPosition;
    private Runnable positionUpdaterRunnable;

    private String calloutBgdImage = "bubble_shadow.9.png";

    private class InfoWindowLayoutListener
            implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            popupXOffset = infoWindowContainer.getWidth() / 2;
            popupYOffset = infoWindowContainer.getHeight();
        }
    }

    private class PositionUpdaterRunnable implements Runnable {
        private int lastXPosition = Integer.MIN_VALUE;
        private int lastYPosition = Integer.MIN_VALUE;
        final private int markerWidth;
        final private int markerHeight;

        public PositionUpdaterRunnable(int markerWidth, int markerHeight) {
            super();
            this.markerWidth = markerWidth;
            this.markerHeight = markerHeight;
        }

        @Override
        public void run() {
            handler.postDelayed(this, POPUP_POSITION_REFRESH_INTERVAL);
            if (trackedPosition != null
                    && infoWindowContainer.getVisibility() == View.VISIBLE) {
                Point targetPosition = getMap().getProjection()
                        .toScreenLocation(trackedPosition);
                if (lastXPosition != targetPosition.x
                        || lastYPosition != targetPosition.y) {
                    overlayLayoutParams.x = targetPosition.x - popupXOffset
                            + markerWidth;
                    overlayLayoutParams.y = targetPosition.y - popupYOffset
                            + markerHeight;
                    lastXPosition = targetPosition.x;
                    lastYPosition = targetPosition.y;
                    infoWindowContainer.setLayoutParams(overlayLayoutParams);
                }
            }
        }
    }

    public GoogleMapView(final TiViewProxy proxy, final Activity activity) {
        super(proxy);
        container = new AbsoluteLayout(activity) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                boolean shouldNot = GoogleMapView.this.touchPassThrough == true;
                if (shouldNot) {
                    return false;
                }
                boolean result = false;
                if (GoogleMapView.this.isTouchEnabled == true) {
                    result = interceptTouchEvent(ev)
                            || super.dispatchTouchEvent(ev);
                    int action = ev.getAction();
                    if (result && action != MotionEvent.ACTION_DOWN
                            && pointerDown) { // use is moving on the map
                        if (_clusterManager != null) {
                            _clusterManager.onCameraMove();
                        } else {
                            onCameraMove();

                        }
                    }
                }
                return result;
            }
        };
        setNativeView(container);

        if (TiApplication.isGooglePlayServicesAvailable()) {
            try {
                if (!INITIALIZED) {
                    MapsInitializer.initialize(activity);
                    INITIALIZED = true;
                }
                GoogleMapOptions gOptions = new GoogleMapOptions();
                if (proxy != null) {
                    boolean zOrderOnTop = TiConvert.toBoolean(
                            proxy.getProperty(
                                    AkylasGooglemapModule.PROPERTY_ZORDER_ON_TOP),
                            false);
                    gOptions.zOrderOnTop(zOrderOnTop);
                    boolean liteMode = TiConvert.toBoolean(
                            proxy.getProperty(
                                    AkylasGooglemapModule.PROPERTY_LITE_MODE),
                            false);
                    gOptions.liteMode(liteMode);
                }

                mapView = new MapView(activity, gOptions);
                mapView.onCreate(new Bundle());
                mapView.onResume();
                mapView.getMapAsync(this);
                container.addView(mapView);
            } catch (Exception e) {
            }
        } else {
            KrollDict data = new KrollDict();
            data.putCodeAndMessage(-213,
                    TiApplication.getGooglePlayServicesErrorString());
            fireEvent(TiC.EVENT_ERROR, data, false, false);

        }
        if (activity instanceof TiBaseActivity) {
            ((TiBaseActivity) activity).addOnLifecycleEventListener(this);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
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
        theMap.setOnMarkerDragListener(mapView);
        theMap.setOnInfoWindowClickListener(mapView);
        theMap.setInfoWindowAdapter(mapView);
        theMap.setOnMapLongClickListener(mapView);
        theMap.setOnMapLoadedCallback(mapView);
        theMap.setOnPolygonClickListener(mapView);
        theMap.setOnPolylineClickListener(mapView);
        theMap.setOnMyLocationChangeListener(mapView);
        theMap.setOnMyLocationButtonClickListener(mapView);
//        theMap.setOnPoiClickListener(mapView);
        theMap.setOnCameraMoveCanceledListener(mapView);
        theMap.setOnCameraIdleListener(mapView);
        theMap.setOnCameraMoveListener(mapView);
        theMap.setOnCameraMoveStartedListener(mapView);
    }

    private void setMap(GoogleMap newMap) {
        if (this.map != null) {
            setMapListeners(this.map, null);
        }
        this.map = newMap;
        if (map != null) {
            setMapListeners(this.map, this);
            proxy.realizeViews(this, true, true);
        }
    }

    public Fragment getFragment() {
        return fragment;
    }

    private void releaseFragment() {
        if (map != null) {
            map.clear();
            map = null;
        }
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
                transaction.commitAllowingStateLoss();
            }
            fragment = null;
        }
    }

    @Override
    public void release() {
        if (proxy != null && proxy.getActivity() != null) {
            ((TiBaseActivity) proxy.getActivity())
                    .removeOnLifecycleEventListener(this);
        }
        super.release();
        if (infoWindowContainer != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                infoWindowContainer.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(infoWindowLayoutListener);
            } else {
                infoWindowContainer.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(infoWindowLayoutListener);
            }
        }
        if (positionUpdaterRunnable != null) {
            handler.removeCallbacks(positionUpdaterRunnable);
            positionUpdaterRunnable = null;
        }
        if (_clusterManager != null) {
            _clusterManager.setRenderer(null);
            _clusterManager.setOnClusterClickListener(null);
            _clusterManager.setOnClusterInfoWindowClickListener(null);
            _clusterManager.setOnClusterItemClickListener(null);
            _clusterManager.setOnClusterItemInfoWindowClickListener(null);
            _clusterManager.setOnCameraMoveListener(null);
            _clusterManager = null;
        }
        if (this.map != null) {
            setMapListeners(this.map, null);
            this.map.clear();
            this.map = null;
        }
        releaseFragment();

        // if (handledMarkers != null) {
        // handledMarkers.clear();
        // }
        // if (handledPolylines != null) {
        // handledPolylines.clear();
        // }
    }

    protected Fragment createFragment() {
        if (proxy == null) {
            return SupportMapFragment.newInstance();
        } else {
            boolean zOrderOnTop = TiConvert.toBoolean(
                    proxy.getProperty(
                            AkylasGooglemapModule.PROPERTY_ZORDER_ON_TOP),
                    false);
            GoogleMapOptions gOptions = new GoogleMapOptions();
            gOptions.zOrderOnTop(zOrderOnTop);
            return SupportMapFragment.newInstance(gOptions);
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;

    static {
        ArrayList<String> tmp = AkylasMapBaseView.KEY_SEQUENCE;
        tmp.add(AkylasGooglemapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
        KEY_SEQUENCE = tmp;
    }

    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }
    

    @Override
    public void setReusing(boolean value) {
        super.setReusing(value);
        if (value) {
            if (map != null) {
                map.clear();
            }
        }
    }

    @Override
    public void aboutToProcessProperties(HashMap d) {
        if (d != null) {
            cameraAnimationDuration = TiConvert.toInt(d, "animationDuration",
                    500);
            d.remove("animationDuration");
        }

        super.aboutToProcessProperties(d);
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasGooglemapModule.PROPERTY_MINZOOM:
            map.setMinZoomPreference(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasGooglemapModule.PROPERTY_MAXZOOM:
            map.setMaxZoomPreference(TiConvert.toFloat(newValue, 22));
            break;
        case AkylasGooglemapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            mRequiredZoomLevel = TiConvert.toFloat(newValue, 10);
            break;
        case AkylasGooglemapModule.PROPERTY_USER_LOCATION_BUTTON:
            map.getUiSettings().setMyLocationButtonEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_ZOOM_CONTROLS_ENABLED:
            map.getUiSettings().setZoomControlsEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_COMPASS_ENABLED:
            map.getUiSettings()
                    .setCompassEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_TOOLBAR_ENABLED:
            map.getUiSettings()
                    .setMapToolbarEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_INDOOR_CONTROLS_ENABLED:
            map.getUiSettings().setIndoorLevelPickerEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_SCROLL_ENABLED:
            map.getUiSettings().setScrollGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case TiC.PROPERTY_ZOOM_ENABLED:
            map.getUiSettings().setZoomGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_ROTATE_ENABLED:
            map.getUiSettings().setRotateGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_TILT_ENABLED:
            map.getUiSettings().setTiltGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_BUILDINGS_ENABLED:
            map.setBuildingsEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_INDOOR_ENABLED:
            map.setIndoorEnabled(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasGooglemapModule.PROPERTY_TRAFFIC:
            map.setTrafficEnabled(TiConvert.toBoolean(newValue, false));
            break;
        case TiC.PROPERTY_BEARING:
            getCameraBuilder().bearing(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasGooglemapModule.PROPERTY_TILT:
            getCameraBuilder().tilt(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasGooglemapModule.PROPERTY_ZOOM:
            targetZoom = TiConvert.toFloat(newValue, 0);
            getCameraBuilder().zoom(targetZoom);
            break;
        case "animationDuration":
            cameraAnimationDuration = TiConvert.toInt(newValue,
                    CAMERA_UPDATE_DURATION);
            break;
        case TiC.PROPERTY_REGION:
            mCameraRegion = AkylasGooglemapModule.regionFromObject(newValue);
            if (mCameraRegion != null) {
                getCameraBuilder();
            }
            mCameraRegionUpdate = mCameraRegion != null;
            break;
        case AkylasGooglemapModule.PROPERTY_CENTER_COORDINATE:
            LatLng pos = (LatLng) AkylasGooglemapModule
                    .latlongFromObject(newValue);
            if (pos != null) {
                getCameraBuilder().target(pos);
            }
            break;
        case TiC.PROPERTY_MAP_TYPE:
            int type = TiConvert.toInt(newValue,
                    AkylasGooglemapModule.MAP_TYPE_NORMAL);
            int googleType = GoogleMap.MAP_TYPE_NORMAL;
            switch (type) {
            case AkylasGooglemapModule.MAP_TYPE_HYBRID:
                googleType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case AkylasGooglemapModule.MAP_TYPE_SATELLITE:
                googleType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case AkylasGooglemapModule.MAP_TYPE_TERRAIN:
                googleType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            case AkylasGooglemapModule.MAP_TYPE_NONE:
                googleType = GoogleMap.MAP_TYPE_NONE;
                break;
            default:
                break;
            }
            map.setMapType(googleType);
            break;
        case TiC.PROPERTY_PADDING:
            padding = TiConvert.toPaddingRect(newValue, padding);
            map.setPadding((int) padding.left, (int) padding.top,
                    (int) padding.right, (int) padding.bottom);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MAP_INVALIDATE;
            break;
        case AkylasGooglemapModule.PROPERTY_MAPSTYLE:
            if (newValue instanceof HashMap) {
                try {
                    map.setMapStyle(new MapStyleOptions(
                            new JSONObject((HashMap) newValue).toString()));
                } catch (Exception e) {

                }
            } else if (newValue instanceof String) {
                map.setMapStyle(new MapStyleOptions((String) newValue));
            } else {
                map.setMapStyle(null);
            }
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
            if (currentCameraPosition == null) {
                currentCameraPosition = map.getCameraPosition();
            }
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
        boolean animate = mCameraAnimate && shouldAnimate();
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
            try {
                CameraPosition position = mCameraBuilder.build();
                CameraUpdate camUpdate = CameraUpdateFactory
                        .newCameraPosition(position);
                moveCamera(camUpdate, animate);
            } catch (Exception e) {
            }
        }
        mCameraBuilder = null;
        mCameraRegionUpdate = false;
        mCameraAnimate = false;
        mCameraRegion = null;
        mCameraCenter = null;
    }

    @Override
    protected void didProcessProperties() {
        Log.d(TAG, "didProcessProperties " + mProcessUpdateFlags);
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_CAMERA) != 0) {
            handleCameraUpdate();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_CAMERA;
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MAP_INVALIDATE;
        }
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_MAP_INVALIDATE) != 0) {
            if (currentCameraPosition != null) {
                map.moveCamera(CameraUpdateFactory
                        .newCameraPosition(currentCameraPosition));
            }
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MAP_INVALIDATE;
        }
        super.didProcessProperties();
    }

    protected void moveCamera(CameraUpdate camUpdate, boolean anim) {
        Log.d(TAG, "moveCamera " + anim);
        if (map == null)
            return;
        if (anim) {
            map.animateCamera(camUpdate, cameraAnimationDuration, null);
        } else {
            map.moveCamera(camUpdate);
        }
    }

    public GoogleMap getMap() {
        return map;
    }

    @Override
    public void setUserLocationEnabled(boolean enabled) {
        try {
            map.setMyLocationEnabled(enabled);
        } catch (Exception e) {
            e.printStackTrace();
            KrollDict data = new KrollDict();
            data.putCodeAndMessage(-1, e.getMessage());
            fireEvent(TiC.EVENT_ERROR, data, false, false);
        }
    }

    protected void setCompassEnabled(boolean enabled) {
        map.getUiSettings().setCompassEnabled(enabled);
    }

    public float getMaxZoomLevel() {
        float defaultValue = TiConvert.toFloat(
                proxy.getProperty(AkylasGooglemapModule.PROPERTY_MAXZOOM), -1);
        if (defaultValue != -1) {
            return defaultValue;
        }
        return proxy.getValueInUIThread(new Command<Float>() {
            @Override
            public Float execute() {
                return map.getMaxZoomLevel();
            }
        }, defaultValue);
    }

    public float getMinZoomLevel() {
        float defaultValue = TiConvert.toFloat(
                proxy.getProperty(AkylasGooglemapModule.PROPERTY_MINZOOM), -1);
        if (defaultValue != -1) {
            return defaultValue;
        }
        return proxy.getValueInUIThread(new Command<Float>() {
            @Override
            public Float execute() {
                return map.getMinZoomLevel();
            }
        }, defaultValue);
    }

    public static float metersToEquatorPixels(GoogleMap map,
            final LatLng location, final float zoom, final float meters) {
        CameraPosition position = map.getCameraPosition();
        LatLng center = location;
        if (center == null) {
            center = position.target;
            if (center == null) {
                center = new LatLng(0, 0);
            }
        }
        float zoomLevel = zoom;
        if (zoomLevel < 0) {
            zoomLevel = position.zoom;
        }
        double latRadians = center.latitude * Math.PI / 180;
        double metersPerPixel = 40075016.68 / (256 * Math.pow(2, zoomLevel));
        return (float) (meters / Math.cos(latRadians) / metersPerPixel);
    }

    @Override
    public float getMetersPerPixel(final float zoomToCheck,
            final Object position) {
        if (map == null) {
            return 0.0f;
        }
        final LatLng pos = (LatLng) AkylasGooglemapModule
                .latlongFromObject(position);

        FutureTask<Float> futureResult = new FutureTask<Float>(
                new Callable<Float>() {
                    @Override
                    public Float call() throws Exception {
                        return 1.0f / metersToEquatorPixels(map, pos,
                                zoomToCheck, 1.0f);
                    }
                });
        // this block until the result is calculated!
        getProxy().getActivity().runOnUiThread(futureResult);
        try {
            return futureResult.get();
        } catch (InterruptedException | ExecutionException e) {
            return 0.0f;
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
                    proxy.getProperty(AkylasGooglemapModule.PROPERTY_ZOOM), 0);
        }
        return currentCameraPosition.zoom;
    }

    public AnnotationProxy getProxyByMarker(Marker m) {
        Object tag = m.getTag();
        if (tag instanceof AnnotationProxy) {
            return (AnnotationProxy) tag;
        }
        // if (m != null && handledMarkers != null) {
        // return handledMarkers.get(m);
        // }
        return null;
    }

    @Override
    public void changeZoomLevel(final float level, final boolean animated) {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeZoomLevel(level, animated);
                }
            });
            return;
        }
        targetZoom = level;
        CameraUpdate camUpdate = CameraUpdateFactory.zoomBy(level);
        moveCamera(camUpdate, animated);
    }

    protected KrollDict dictFromPoint(LatLng point) {
        KrollDict d = TiViewHelper.dictFromMotionEvent(getTouchView(),
                lastDownEvent);
        d.put(TiC.PROPERTY_LATITUDE, point.latitude);
        d.put(TiC.PROPERTY_LONGITUDE, point.longitude);
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasGooglemapModule.PROPERTY_ZOOM, getZoomLevel());
        d.put(AkylasGooglemapModule.PROPERTY_MAP, proxy);
        return d;
    }

    public void fireClickEvent(BaseAnnotationProxy proxy, final String source) {
        fireEventOnAnnotProxy(TiC.EVENT_CLICK, proxy, source);
    }

    public boolean handleMarkerClick(BaseAnnotationProxy annoProxy) {
        if (annoProxy == null) {
            return false;
        }
        if (!annoProxy.touchable) {
            // trick for untouchable as googlemap does not support it
            onMapClick((LatLng) annoProxy.getPosition());
            return true;
        }

        // make sure we fire the click first
        fireClickEvent(annoProxy, AkylasGooglemapModule.PROPERTY_PIN);
        setSelectedAnnotation(annoProxy);

        // Returning false here will enable native behavior, which shows the
        // info window.
        return !annoProxy.canShowInfoWindow();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy == null || !annoProxy.touchable) {
            return false;
        }
        handleMarkerClick(annoProxy);
        return true;
    }

    @Override
    public void onMapClick(LatLng point) {
        if (_selectOnTap) {
            setSelectedAnnotation(null);
        }
        boolean hasClick = hasListeners(TiC.EVENT_CLICK);
        boolean hasSingleTap = hasListeners(TiC.EVENT_SINGLE_TAP, false);
        if (hasClick || hasSingleTap) {
            KrollDict d = dictFromPoint(point);
            if (hasClick) {
                fireEvent(TiC.EVENT_CLICK, d, true, false);
            }
            if (hasSingleTap) {
                fireEvent(TiC.EVENT_SINGLE_TAP, d, false, false);
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        if (!hasListeners(TiC.EVENT_LONGPRESS, false))
            return;
        fireEvent(TiC.EVENT_LONGPRESS, dictFromPoint(point), false, false);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {

            firePinChangeDragStateEvent(annoProxy,
                    AkylasGooglemapModule.ANNOTATION_DRAG_STATE_DRAGGING);
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
                    AkylasGooglemapModule.ANNOTATION_DRAG_STATE_END);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy != null) {
            firePinChangeDragStateEvent(annoProxy,
                    AkylasGooglemapModule.ANNOTATION_DRAG_STATE_START);
        }
    }

    private void handleInfoWindowClick(BaseAnnotationProxy annoProxy) {
        if (annoProxy != null) {
            String clicksource = annoProxy.getMapInfoWindow().getClicksource();
            // The clicksource is null means the click event is not inside
            // "leftPane", "title", "subtible"
            // or "rightPane". In this case, use "infoWindow" as the
            // clicksource.
            if (clicksource == null) {
                clicksource = AkylasGooglemapModule.PROPERTY_INFO_WINDOW;
            }
            fireClickEvent(annoProxy, clicksource);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        AnnotationProxy annoProxy = getProxyByMarker(marker);
        handleInfoWindowClick(annoProxy);
    }

    private AnnotationProxy showingInfoMarker;

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void infoWindowDidClose(AkylasMapInfoView infoView) {
        showingInfoMarker = null;
        container.removeView(infoView);

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
    public void onCameraMove() {

        currentCameraPosition = map.getCameraPosition();
        mpp = 156543.03392
                * Math.cos(
                        currentCameraPosition.target.latitude * Math.PI / 180)
                / Math.pow(2, currentCameraPosition.zoom);
        targetZoom = -1;
        if (userAction) {
            setShouldFollowUserLocation(false);
            userAction = false;
        }
        if (preLayout) {

            // moveCamera will trigger another callback, so we do this to make
            // sure
            // we don't fire event when region is set initially
            preLayout = false;
            handleCameraUpdate();
        } else if (map != null) {

            if (proxy != null
                    && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
                LatLngBounds bounds = map.getProjection()
                        .getVisibleRegion().latLngBounds;
                KrollDict result = new KrollDict();
                result.put(TiC.PROPERTY_REGION, AkylasGooglemapModule
                        .getFactory().regionToDict(bounds));
                result.put(AkylasGooglemapModule.PROPERTY_ZOOM,
                        currentCameraPosition.zoom);
                result.put("mpp", mpp);
                result.put("mapdistance", mpp * nativeView.getWidth());
                result.put("bearing", currentCameraPosition.bearing);
                result.put("tilt", currentCameraPosition.tilt);
                result.put(AkylasGooglemapModule.PROPERTY_USER_ACTION,
                        userAction);
                result.put("idle", !pointerDown);
                proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
            }
        }
    }

    @Override
    public void onCameraIdle() {
        onCameraMove();
    }

    @Override
    public void onCameraMoveCanceled() {
        onCameraMove();
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        onCameraMove();
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        if (proxy.hasListeners(AkylasGooglemapModule.EVENT_POI, false)) {
            KrollDict d = dictFromPoint(poi.latLng);
            d.put("name", poi.name);
            d.put("placeId", poi.placeId);
            proxy.fireEvent(AkylasGooglemapModule.EVENT_POI, d, false, false);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (proxy.hasListeners(AkylasGooglemapModule.EVENT_LOCATION_BUTTON,
                false)) {
            proxy.fireEvent(AkylasGooglemapModule.EVENT_LOCATION_BUTTON, null,
                    false, false);
        }
        return false;
    }

    private boolean userAction = false;

    // Intercept the touch event to find out the correct clicksource if clicking
    // on the info window.
    protected boolean interceptTouchEvent(MotionEvent event) {
        if (!isTouchEnabled) {
            return false;
        }
        final int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            userAction = false;
            lastDownEvent = event;
            setPointerDown(true);
            break;
        case MotionEvent.ACTION_MOVE:
            userAction = true;
            lastDownEvent = event;
            setPointerDown(true);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_OUTSIDE:
            lastUpEvent = event;
            setPointerDown(false);
            break;
        }
        if (selectedAnnotation instanceof AnnotationProxy) {
            AnnotationProxy annot = (AnnotationProxy) selectedAnnotation;
            AkylasMapInfoView infoWindow = annot.getMapInfoWindow();
            Marker marker = getAnnotMarker(annot);
            if (infoWindow != null && marker != null
                    && marker.isInfoWindowShown()) {
                // Get a marker position on the screen
                Point markerPoint = map.getProjection()
                        .toScreenLocation(marker.getPosition());
                GoogleMapMarker gMarker = getGoogleMarker(annot);
                if (infoWindow.dispatchMapTouchEvent(event, markerPoint,
                        gMarker.getIconImageHeight())) {
                    return true;
                }
            }
        }
        handleTouchEvent(event);
        return false;
    }

    public MapView getMapView() {
        return mapView;
    }

    private GoogleMapMarker getGoogleMarker(AnnotationProxy proxy) {
        return (GoogleMapMarker) proxy.getMarker();
    }

    private Marker getAnnotMarker(BaseAnnotationProxy proxy) {
        GoogleMapMarker timarker = (GoogleMapMarker) proxy.getMarker();
        if (timarker != null) {
            return timarker.getMarker();
        }
        return null;
    }

    public void snapshot() {
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                if (proxy.hasListeners(
                        AkylasGooglemapModule.EVENT_ON_SNAPSHOT_READY, false)) {
                    TiBlob sblob = TiBlob.blobFromObject(snapshot);
                    KrollDict data = new KrollDict();
                    data.put("snapshot", sblob);
                    data.put("source", proxy);
                    proxy.fireEvent(
                            AkylasGooglemapModule.EVENT_ON_SNAPSHOT_READY, data,
                            false, false);
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
            return AkylasGooglemapModule.locationToDict(map.getMyLocation());
        } else {
            return AkylasGooglemapModule.locationToDict(
                    (Location) TiMessenger.sendBlockingMainMessage(
                            mainHandler.obtainMessage(MSG_GET_MYLOCATION)));
        }
    }

    @Override
    public boolean getUserLocationEnabled() {
        if (map == null) {
            return false;
        }
        if (TiApplication.isUIThread()) {
            return map.isMyLocationEnabled();
        } else {
            return (Boolean) TiMessenger.sendBlockingMainMessage(
                    mainHandler.obtainMessage(MSG_GET_MYLOCATION_ENABLED));
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
        float currentZoom = (currentCameraPosition != null)
                ? currentCameraPosition.zoom : 0;
        float targetZoom = (float) (Math.ceil(currentZoom) + 1);
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 2.25) {
            targetZoom = (float) Math.ceil(currentZoom);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    public void zoomIn(Object about, boolean userAction) {
        this.userAction = true;
        zoomIn();
    }

    @Override
    public void zoomOut() {
        float currentZoom = (currentCameraPosition != null)
                ? currentCameraPosition.zoom : 0;
        float targetZoom = (float) (Math.floor(currentZoom));
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 0.75) {
            targetZoom = (float) (Math.floor(currentZoom) - 1);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    public void zoomOut(Object about, boolean userAction) {
        this.userAction = true;
        zoomOut();
    }

    private Projection getProjection() {
        if (map == null) {
            return null;
        }
        return proxy.getInUiThread(new Command<Projection>() {

            @Override
            public Projection execute() {
                return map.getProjection();
            }
        });
    }

    @Override
    public KrollDict getRegionDict() {
        LatLngBounds region = getProjection().getVisibleRegion().latLngBounds;
        return AkylasGooglemapModule.getFactory().regionToDict(region);
    }

    AnimatorSet currentInfoWindowAnim = null;

    @Override
    public void handleDeselectAnnotation(final BaseAnnotationProxy proxy) {
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleDeselectAnnotation(proxy);
                }
            });
            return;
        }
        if (!proxy.canShowInfoWindow()) {
            return;
        }
        if (positionUpdaterRunnable != null) {
            handler.removeCallbacks(positionUpdaterRunnable);
            positionUpdaterRunnable = null;
        }
        if (showingInfoMarker != null && infoWindowContainer != null) {
            if (currentInfoWindowAnim != null) {
                currentInfoWindowAnim.cancel();
                currentInfoWindowAnim = null;
            }
            TiViewHelper.setPivotFloatX(infoWindowContainer, 0.5f);
            TiViewHelper.setPivotFloatY(infoWindowContainer, 1f);
            currentInfoWindowAnim = new AnimatorSet();
            currentInfoWindowAnim.addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    infoWindowContainer.setVisibility(View.GONE);
                    currentInfoWindowAnim = null;
                    if (showingInfoMarker != null) {
                        showingInfoMarker.infoWindowDidClose();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    currentInfoWindowAnim = null;
                }
            });
            currentInfoWindowAnim.setDuration(220);
            currentInfoWindowAnim
                    .setInterpolator(new DecelerateInterpolator(2.5f));
            currentInfoWindowAnim.playTogether(
                    ObjectAnimator.ofFloat(infoWindowContainer, "alpha", 1.0f,
                            0.0f),
                    ObjectAnimator.ofFloat(infoWindowContainer, "scaleX", 1f,
                            0.9f),
                    ObjectAnimator.ofFloat(infoWindowContainer, "scaleY", 1f,
                            0.9f));
            currentInfoWindowAnim.start();
        }
    }

    @Override
    public void handleSelectAnnotation(final BaseAnnotationProxy proxy) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleSelectAnnotation(selectedAnnotation);
                }
            }, false);
            return;
        }
        GoogleMapMarker gmarker = (GoogleMapMarker) proxy.getMarker();
        if (gmarker == null) {
            return;
        }
        Marker marker;
        if (proxy instanceof AnnotationProxy) {
            // this is to make sure the marker is on top
            marker = getAnnotMarker((AnnotationProxy) proxy);
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
        if (!_canShowInfoWindow || !proxy.canShowInfoWindow()) {
            return;
        }
        if (positionUpdaterRunnable != null) {
            handler.removeCallbacks(positionUpdaterRunnable);
            positionUpdaterRunnable = null;
        }
        AkylasMapInfoView infoView = null;
        if (currentInfoWindowAnim != null) {
            // needs to be done before the prepareInfoView
            currentInfoWindowAnim.cancel();
        }
        if (proxy != null) {
            // if (proxy != showingInfoMarker) {
            showingInfoMarker = (AnnotationProxy) proxy;
            infoView = (AkylasMapInfoView) mInfoWindowCache.get("infoView");
            proxy.prepareInfoView(infoView);
            // }
        }
        if (infoView == null) {
            return;
        }

        if (infoWindowContainer == null) {
            infoWindowLayoutListener = new InfoWindowLayoutListener();
            infoWindowContainer = new LinearLayout(getContext());

            infoWindowContainer.setBackground(TiUIHelper.buildImageDrawable(
                    getContext(), calloutBgdImage, false, proxy));
            infoWindowContainer.getViewTreeObserver()
                    .addOnGlobalLayoutListener(infoWindowLayoutListener);
            infoWindowContainer.setLayoutParams(
                    new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT, 0, 0));
            overlayLayoutParams = (AbsoluteLayout.LayoutParams) infoWindowContainer
                    .getLayoutParams();
            // infoWindowContainer.setGravity(Gravity.LEFT | Gravity.TOP);
            container.addView(infoWindowContainer);
        } else {

            infoWindowContainer.removeAllViews();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(infoView.getPaddingLeft(), infoView.getPaddingTop(),
                infoView.getPaddingRight(), infoView.getPaddingBottom()); // arrow
                                                                          // padding
        infoWindowContainer.addView(infoView, params);
        infoWindowContainer.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        infoWindowContainer.layout(0, 0, infoWindowContainer.getMeasuredWidth(),
                infoWindowContainer.getMeasuredHeight());

        trackedPosition = (LatLng) proxy.getPosition();
        final float iconWidth = ((GoogleMapMarker) proxy.getMarker())
                .getIconImageWidth();
        final float iconHeight = ((GoogleMapMarker) proxy.getMarker())
                .getIconImageHeight();
        float deltaX = 0;
        float deltaY = 0;
        if (gmarker != null) {
            deltaX = iconWidth * (gmarker.anchorX - gmarker.anchorY);
            deltaY = iconHeight * (gmarker.winAnchorX - gmarker.winAnchorY);
        }
        
        positionUpdaterRunnable = new PositionUpdaterRunnable((int) deltaX,
                (int) deltaY);
        handler.post(positionUpdaterRunnable);

        TiViewHelper.setPivotFloatX(infoWindowContainer, 0.5f);
        TiViewHelper.setPivotFloatY(infoWindowContainer, 1.0f);
        currentInfoWindowAnim = new AnimatorSet();
        currentInfoWindowAnim.setDuration(230);
        currentInfoWindowAnim.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentInfoWindowAnim = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentInfoWindowAnim = null;
            }
        });
        if (TiC.LOLLIPOP_OR_GREATER) {
            currentInfoWindowAnim.setInterpolator(new PathInterpolator(0.59367f,
                    0.12066f, 0.18878f, 1.5814f));
        }

        currentInfoWindowAnim.playTogether(
                ObjectAnimator.ofFloat(infoWindowContainer, "alpha", 0.0f,
                        1.0f),
                ObjectAnimator.ofFloat(infoWindowContainer, "scaleX", 0.7f,
                        1.0f),
                ObjectAnimator.ofFloat(infoWindowContainer, "scaleY", 0.7f,
                        1.0f));
        currentInfoWindowAnim.start();
        infoWindowContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void handleAddAnnotation(final ArrayList value) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleAddAnnotation(value);
                }
            }, false);
            return;
        }

        final Activity activity = proxy.getActivity();
        for (AnnotationProxy proxy : (ArrayList<AnnotationProxy>) value) {
            proxy.setActivity(activity);
            addAnnotationToMap(proxy);
        }
    }

    @Override
    public void handleRemoveAnnotation(final ArrayList value) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleRemoveAnnotation(value);
                }
            }, false);
            return;
        }

        for (AnnotationProxy proxy : (ArrayList<AnnotationProxy>) value) {
            handleRemoveSingleAnnotation(proxy);
        }
    }

    public void handleRemoveSingleAnnotation(AnnotationProxy proxy) {
        // GoogleMapMarker marker = (GoogleMapMarker) proxy.getMarker();
        // if (handledMarkers != null) {
        // handledMarkers.remove(marker.getMarker());
        // }
        deselectAnnotation(proxy);
        proxy.wasRemoved();
    }

    @Override
    public void handleAddRoute(final ArrayList value) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleAddRoute(value);
                }
            }, false);
            return;
        }
        final Activity activity = proxy.getActivity();
        for (RouteProxy proxy : (ArrayList<RouteProxy>) value) {
            if (proxy.getPolyline() == null) {
                proxy.setPolyline(map.addPolyline(
                        proxy.getAndSetOptions(currentCameraPosition)));
                proxy.setMapView(GoogleMapView.this);
                proxy.setActivity(activity);
                proxy.setParentForBubbling(GoogleMapView.this.proxy);
                // if (handledPolylines == null) {
                // handledPolylines = new WeakHashMap<Polyline, RouteProxy>();
                // }
                // handledPolylines.put(proxy.getPolyline(), proxy);
            }
        }
    }

    @Override
    public void handleRemoveRoute(final ArrayList value) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleRemoveRoute(value);
                }
            }, false);
            return;
        }

        for (RouteProxy proxy : (ArrayList<RouteProxy>) value) {
            // if (handledPolylines != null) {
            // handledPolylines.remove(proxy.getPolyline());
            // }
            deselectAnnotation(proxy);
            proxy.wasRemoved();
        }

    }

    @Override
    public void handleAddGroundOverlay(final ArrayList value) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleAddGroundOverlay(value);
                }
            }, false);
            return;
        }

        final Activity activity = proxy.getActivity();
        for (GroundOverlayProxy proxy : (ArrayList<GroundOverlayProxy>) value) {
            proxy.setGroundOverlay(map.addGroundOverlay(
                    proxy.getAndSetOptions(currentCameraPosition)));
            proxy.setActivity(activity);
            proxy.setMapView(GoogleMapView.this);
            proxy.setParentForBubbling(GoogleMapView.this.proxy);
            // addedGroundOverlays.add(proxy);
        }
    }

    @Override
    public void handleRemoveGroundOverlay(final ArrayList value) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleRemoveGroundOverlay(value);
                }
            }, false);
            return;
        }

        for (GroundOverlayProxy proxy : (ArrayList<GroundOverlayProxy>) value) {
            proxy.wasRemoved();
        }
    }

    @Override
    public void handleAddCluster(final ArrayList value) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleAddCluster(value);
                }
            }, false);
            return;
        }

        final Activity activity = proxy.getActivity();
        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            proxy.setMapView(GoogleMapView.this);
            proxy.setParentForBubbling(GoogleMapView.this.proxy);
            proxy.setActivity(activity);
            getClusterManager()
                    .addClusterAlgorithm(proxy.getOrCreateAlgorithm());
        }

    }

    @Override
    public void handleRemoveCluster(final ArrayList value) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleRemoveCluster(value);
                }
            }, false);
            return;
        }

        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            proxy.wasRemoved();
        }
    }

    @Override
    public void handleAddTileSource(final ArrayList value, final int index) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleAddTileSource(value, index);
                }
            }, false);
            return;
        }
        int realIndex = index;
        final Activity activity = proxy.getActivity();
        for (TileSourceProxy proxy : (ArrayList<TileSourceProxy>) value) {
            TileOverlayOptions options = ((TileSourceProxy) proxy)
                    .getTileOverlayOptions();
            if (options != null) {
                if (realIndex != -1) {
                    options.zIndex(realIndex);
                }
                proxy.setActivity(activity);
                proxy.setTileOverlay(map.addTileOverlay(options));
                proxy.setParentForBubbling(GoogleMapView.this.proxy);
                // addedTileSources.add(proxy);
            }
            if (realIndex != -1) {
                realIndex++;
            }
        }
    }

    @Override
    public void handleRemoveTileSource(final ArrayList value) {
        if (map == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleRemoveTileSource(value);
                }
            }, false);
            return;
        }

        for (TileSourceProxy proxy : (ArrayList<TileSourceProxy>) value) {
            proxy.wasRemoved();
        }
    }

    public void prepareAnnotation(AnnotationProxy proxy) {
        AkylasMarker marker = proxy.getMarker();
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        }
        proxy.setMapView(GoogleMapView.this);
        if (proxy.getParentForBubbling() == null) {
            proxy.setParentForBubbling(GoogleMapView.this.proxy);
        }
        GoogleMapMarker gMarker = new GoogleMapMarker((AnnotationProxy) proxy);
        proxy.setMarker(gMarker);
    }

    public Marker addAnnotationToMap(AnnotationProxy proxy) {
        prepareAnnotation(proxy);
        GoogleMapMarker gMarker = (GoogleMapMarker) proxy.getMarker();
        Marker googlemarker = map.addMarker(gMarker.getMarkerOptions());
        // we need to set the position again because addMarker can be long and
        // position might already have changed
        googlemarker.setPosition((LatLng) proxy.getPosition());
        gMarker.setMarker(googlemarker);
        googlemarker.setTag(proxy);
        // if (handledMarkers == null) {
        // handledMarkers = new WeakHashMap<Marker, AnnotationProxy>();
        // }
        // handledMarkers.put(googlemarker, proxy);
        // timarkers.add(proxy.getMarker());
        if (proxy.selected && proxy == selectedAnnotation) {
            handleSelectAnnotation(selectedAnnotation);
        }
        return googlemarker;
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (shouldFollowUserLocation
                && mUserTrackingMode != AkylasGooglemapModule.TrackingMode.NONE) {
            CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
            cameraBuilder.target(new LatLng(location.getLatitude(),
                    location.getLongitude()));
            if (mUserTrackingMode == AkylasGooglemapModule.TrackingMode.FOLLOW_BEARING
                    && location.hasBearing()) {
                cameraBuilder.bearing(location.getBearing());
            }

            float currentZoom = (currentCameraPosition != null)
                    ? currentCameraPosition.zoom : 0;
            if (currentZoom < mRequiredZoomLevel) {
                if (location.hasAccuracy()) {
                    // approx meterPerDegree latitude, plus some margin
                    double delta = (location.getAccuracy() / 110000) * 1.2;
                    final LatLngBounds currentBox = map.getProjection()
                            .getVisibleRegion().latLngBounds;
                    LatLng desiredSouthWest = new LatLng(
                            location.getLatitude() - delta,
                            location.getLongitude() - delta);

                    LatLng desiredNorthEast = new LatLng(
                            location.getLatitude() + delta,
                            location.getLongitude() + delta);

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
            d.put(AkylasGooglemapModule.PROPERTY_MAP, proxy);
            proxy.fireEvent(TiC.EVENT_LOCATION, d, false, false);
        }
    }

    void updateCamera(final HashMap props) {
        if (props == null)
            return;
        if (preLayout || map == null) {
            props.remove(TiC.PROPERTY_ANIMATE);
            if (!props.containsKey(TiC.PROPERTY_REGION) && props.containsKey(
                    AkylasGooglemapModule.PROPERTY_CENTER_COORDINATE)) {
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

    public void updateMarkerPosition(final Marker marker,
            final LatLng toPosition, final long animationDuration) {
        boolean animated = animationDuration > 0;
        if (!animated || !shouldAnimate() || marker.getPosition() == null
                || toPosition == null) {
            marker.setPosition(toPosition);
            return;
        }
        final LatLng startLatLng = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        // final long duration = animationDuration;

        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.min(interpolator.getInterpolation(
                            (float) elapsed / animationDuration), 1.0f);
                    double lng = t * toPosition.longitude
                            + (1 - t) * startLatLng.longitude;
                    double lat = t * toPosition.latitude
                            + (1 - t) * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));

                    if (t < 1.0) {
                        handler.postDelayed(this, 16);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void updateMarkerHeading(final Marker marker, final float heading) {
        marker.setRotation(heading);
        return;
    }

    private ClusterManager _clusterManager = null;

    public ClusterManager getClusterManager() {
        if (_clusterManager == null && map != null) {
            _clusterManager = new ClusterManager<AnnotationProxy>(getContext(),
                    map);
            _clusterManager.setRenderer(new ClusterRenderer(getContext(), map,
                    _clusterManager, this));
            _clusterManager.setOnClusterClickListener(this);
            _clusterManager.setOnClusterItemClickListener(this);
            _clusterManager.setOnCameraMoveListener(this);
            _clusterManager.setOnMarkerClickListener(this);
            map.setOnCameraMoveListener(_clusterManager);
            map.setOnMarkerClickListener(_clusterManager);

        }
        return _clusterManager;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster cluster) {
        if (cluster instanceof AkylasCluster) {
            handleInfoWindowClick(((AkylasCluster) cluster).proxy);
        }
    }

    @Override
    public void onClusterItemInfoWindowClick(ClusterItem item) {
        if (item instanceof AnnotationProxy) {
            handleInfoWindowClick((AnnotationProxy) item);
        }
    }

    @Override
    public boolean onClusterItemClick(ClusterItem item) {
        if (item instanceof AnnotationProxy) {
            handleMarkerClick((AnnotationProxy) item);
        }
        return true;
    }

    @Override
    public void onPolygonClick(Polygon arg0) {
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if (_canSelectRoute) {
            // if (handledPolylines != null) {
            // BaseRouteProxy route = handledPolylines.get(polyline);
            Object route = polyline.getTag();
            if (route instanceof BaseRouteProxy) {
                handleMarkerClick((BaseAnnotationProxy) route);
            }
            // }
        } else if (lastDownEvent != null) {
            Point p = new Point((int) lastDownEvent.getX(),
                    (int) lastDownEvent.getY());
            onMapClick(getProjection().fromScreenLocation(p));
        }

    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        if (cluster instanceof AkylasCluster) {
            handleMarkerClick(((AkylasCluster) cluster).proxy);
        }
        return true;
    }

    @Override
    public Object coordinateForPoints(Object arg) {
        if (arg instanceof Object[]) {
            Projection proj = getProjection();
            List<Object> result = new ArrayList<>();
            Object[] array = (Object[]) arg;
            TiPoint pt;
            LatLng res;
            for (int i = 0; i < array.length; i++) {
                pt = TiConvert.toPoint(array[i]);
                if (pt != null) {
                    res = proj.fromScreenLocation(pt.compute(
                            nativeView.getWidth(), nativeView.getHeight()));
                    result.add(new Object[] { res.latitude, res.longitude });
                }

            }
            return result.toArray();
        }

        return null;
    }

    @Override
    public void onDestroy(Activity activity) {
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (positionUpdaterRunnable != null) {
            handler.removeCallbacks(positionUpdaterRunnable);
        }
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (mapView != null) {
            mapView.onResume();
        }
        if (positionUpdaterRunnable != null) {
            handler.post(positionUpdaterRunnable);
        }
    }

    @Override
    public void onStart(Activity activity) {
    }

    @Override
    public void onStop(Activity activity) {
    }

    @Override
    public void onCreate(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onLowMemory(Activity activity) {
    }

    @Override
    protected KrollDict fromScreenLocation(Point p) {
        return AkylasGooglemapModule
                .latLongToDict(getProjection().fromScreenLocation(p));
    }

    public void removeMarker(Marker m) {
        AnnotationProxy proxy = getProxyByMarker(m);
        if (proxy != null) {
            // GoogleMapMarker marker = (GoogleMapMarker) proxy.getMarker();
            // marker.setTag(null);
            // if (handledMarkers != null) {
            // handledMarkers.remove(marker.getMarker());
            // }
            deselectAnnotation(proxy);
            proxy.removeFromMap(); // only remove from map
        } else {
            m.remove();
        }

    }

}
