/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.mapboxgl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import akylas.googlemap.ClusterProxy;
import akylas.googlemap.GoogleMapMarker;
import akylas.googlemap.GoogleMapView;
import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.AkylasMapInfoView;
import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseTileSourceProxy;
import akylas.map.common.ReusableView;
import android.graphics.RectF;
import android.location.Location;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.kroll.common.TiMessenger.Command;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiViewHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;

import com.carto.datasources.LocalVectorDataSource;
import com.mapbox.mapboxsdk.annotations.Annotation;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.InfoWindowAdapter;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnCameraIdleListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnCameraMoveCanceledListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnCameraMoveListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnCameraMoveStartedListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnInfoWindowClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapLongClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMyLocationChangeListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnPolygonClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnPolylineClickListener;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterManagerPlugin;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AkMapView extends AkylasMapBaseView
        implements OnLifecycleEvent, OnMapReadyCallback, OnMarkerClickListener,
        OnMapClickListener, OnInfoWindowClickListener, InfoWindowAdapter,
        OnMapLongClickListener, OnPolygonClickListener, OnPolylineClickListener,
        OnMyLocationChangeListener, OnCameraIdleListener,
        OnCameraMoveCanceledListener, OnCameraMoveListener,
        OnCameraMoveStartedListener {

    private static final String TAG = "AkylasMapboxView";
    private MapboxMap map;
    protected boolean animate = true;
    protected boolean preLayout = true;
    protected HashMap preLayoutUpdateCamera;
    AkylasMapInfoView infoWindow = null;
    // protected ArrayList<AkylasMarker> timarkers;
    // protected WeakHashMap<Polyline, RouteProxy> handledPolylines;
    protected WeakHashMap<Annotation, BaseAnnotationProxy> handledAnnotations;
    protected WeakHashMap<Layer, Object> handledLayers;
    private Fragment fragment;

    private float mRequiredZoomLevel = 10;
    private CameraPosition currentCameraPosition = null;

    // private Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    // private static final int MSG_GET_PROJECTION = 10001;
    // private static final int MSG_GET_MYLOCATION = 10002;
    // private static final int MSG_GET_MYLOCATION_ENABLED = 10003;

    private static int CAMERA_UPDATE_DURATION = 500;

    // private static boolean INITIALIZED = false;
    private RectF padding = null;
    private int cameraAnimationDuration = CAMERA_UPDATE_DURATION;

    private MapView mapView;
    protected Projection baseProjection;

    final FrameLayout container;
    protected static final int TIFLAG_NEEDS_CAMERA = 0x00000001;
    protected static final int TIFLAG_NEEDS_MAP_INVALIDATE = 0x00000002;

    // INFOWINDOW
    private FrameLayout.LayoutParams overlayLayoutParams;
    private int popupXOffset;
    private int popupYOffset;
    // private static final int POPUP_POSITION_REFRESH_INTERVAL = 10;
    private ViewTreeObserver.OnGlobalLayoutListener infoWindowLayoutListener;
    private LinearLayout infoWindowContainer;
    private LatLng trackedPosition;
    private Runnable positionUpdaterRunnable;

    // GeoJsonSource annotsSource;
    // SymbolLayer annotsLayer;
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
        private float lastXPosition = Float.MIN_VALUE;
        private float lastYPosition = Float.MIN_VALUE;
        final private int markerWidth;
        final private int markerHeight;

        public PositionUpdaterRunnable(int markerWidth, int markerHeight) {
            super();
            this.markerWidth = markerWidth;
            this.markerHeight = markerHeight;
        }

        @Override
        public void run() {
            // handler.postDelayed(this, POPUP_POSITION_REFRESH_INTERVAL);
            if (trackedPosition != null
                    && infoWindowContainer.getVisibility() == View.VISIBLE) {
                PointF targetPosition = getMap().getProjection()
                        .toScreenLocation(trackedPosition);
                if (lastXPosition != targetPosition.x
                        || lastYPosition != targetPosition.y) {
                    overlayLayoutParams.leftMargin = (int) (targetPosition.x
                            - popupXOffset + markerWidth);
                    overlayLayoutParams.topMargin = (int) (targetPosition.y
                            - popupYOffset + markerHeight);
                    lastXPosition = (int) targetPosition.x;
                    lastYPosition = (int) targetPosition.y;
                    infoWindowContainer.setLayoutParams(overlayLayoutParams);
                }
            }
        }
    }

    public AkMapView(final TiViewProxy proxy, final Activity activity) {
        super(proxy);
        container = new FrameLayout(activity) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                boolean shouldNot = AkMapView.this.touchPassThrough == true;
                if (shouldNot) {
                    return false;
                }
                boolean result = false;
                if (AkMapView.this.isTouchEnabled == true) {
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

            @Override
            protected void onLayout(boolean changed, int left, int top,
                    int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                if (preLayout) {
                    preLayout = false;
                    handleCameraUpdate();
                }
            }
        };
        setNativeView(container);

        // if (TiApplication.isGooglePlayServicesAvailable()) {
        // try {
        // if (!INITIALIZED) {
        // MapsInitializer.initialize(activity);
        // INITIALIZED = true;
        // }
        // GoogleMapOptions gOptions = new GoogleMapOptions();
        // if (proxy != null) {
        // boolean zOrderOnTop = TiConvert.toBoolean(
        // proxy.getProperty(
        // AkylasMapboxGLModule.PROPERTY_ZORDER_ON_TOP),
        // false);
        // gOptions.zOrderOnTop(zOrderOnTop);
        // boolean liteMode = TiConvert.toBoolean(
        // proxy.getProperty(
        // AkylasMapboxGLModule.PROPERTY_LITE_MODE),
        // false);
        // gOptions.liteMode(liteMode);
        // }

        mapView = new MapView(activity);
        mapView.onCreate(new Bundle());
        mapView.getMapAsync(this);
        container.addView(mapView);

        // } catch (Exception e) {
        // }
        // } else {
        // KrollDict data = new KrollDict();
        // data.putCodeAndMessage(-213,
        // TiApplication.getGooglePlayServicesErrorString());
        // fireEvent(TiC.EVENT_ERROR, data, false,
        // false);
        //
        // }
        if (activity instanceof TiBaseActivity) {
            ((TiBaseActivity) activity).addOnLifecycleEventListener(this);
        }
    }

    // public Projection getProjection() {
    // return baseProjection;
    // }
    //
    // public VectorTileDecoder getTileDecoder() {
    // return baseStyleDecoder;
    // }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onMapReady(MapboxMap map) {
        setMap(map);
    }

    // @Override
    // public boolean handleMessage(Message msg) {
    // switch (msg.what) {
    //
    // case MSG_GET_PROJECTION: {
    // AsyncResult result = (AsyncResult) msg.obj;
    // result.setResult(map.getProjection());
    // return true;
    // }
    // case MSG_GET_MYLOCATION: {
    // AsyncResult result = (AsyncResult) msg.obj;
    // result.setResult(map.getMyLocation());
    // return true;
    // }
    // case MSG_GET_MYLOCATION_ENABLED: {
    // AsyncResult result = (AsyncResult) msg.obj;
    // result.setResult(map.isMyLocationEnabled());
    // return true;
    // }
    // default:
    // return false;
    //
    // }
    // }

    private void setMapListeners(MapboxMap theMap, AkMapView mapView) {
        theMap.setOnMarkerClickListener(mapView);
        theMap.setOnMapClickListener(mapView);
        // theMap.setOnMarkerDragListener(mapView);
        theMap.setOnInfoWindowClickListener(mapView);
        theMap.setInfoWindowAdapter(mapView);
        theMap.setOnMapLongClickListener(mapView);
        // theMap.setOnMapLoadedCallback(mapView);
        theMap.setOnPolygonClickListener(mapView);
        theMap.setOnPolylineClickListener(mapView);
        theMap.setOnMyLocationChangeListener(mapView);
        // theMap.setOnMyLocationButtonClickListener(mapView);
        // theMap.setOnPoiClickListener(mapView);
        theMap.addOnCameraIdleListener(mapView);
        theMap.addOnCameraMoveCancelListener(mapView);
        theMap.addOnCameraMoveListener(mapView);
        theMap.addOnCameraMoveStartedListener(mapView);
    }

    private void setMap(MapboxMap newMap) {
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

    private void clearPositionUpdater() {
        if (positionUpdaterRunnable != null) {
            // handler.removeCallbacks(positionUpdaterRunnable);
            positionUpdaterRunnable = null;
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
            if (TiC.JELLY_BEAN_OR_GREATER) {
                infoWindowContainer.getViewTreeObserver()
                        .removeOnGlobalLayoutListener(infoWindowLayoutListener);

            } else {
                infoWindowContainer.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(infoWindowLayoutListener);
            }
        }
        clearPositionUpdater();
        if (_clusterManager != null) {
            _clusterManager.setRenderer(null);
            _clusterManager.setOnClusterClickListener(null);
            _clusterManager.setOnClusterInfoWindowClickListener(null);
            _clusterManager.setOnClusterItemClickListener(null);
            _clusterManager.setOnClusterItemInfoWindowClickListener(null);
            _clusterManager.setOnCameraMoveListener(null);
            _clusterManager = null;
        }
        if (this.mapView != null) {
            setMapListeners(this.map, null);
            this.map.clear();
            this.mapView = null;
        }
        releaseFragment();

        if (handledAnnotations != null) {
            handledAnnotations.clear();
        }
        if (handledLayers != null) {
            handledLayers.clear();
        }
        // if (handledPolylines != null) {
        // handledPolylines.clear();
        // }
    }

    protected Fragment createFragment() {
        if (proxy == null) {
            return SupportMapFragment.newInstance();
        } else {
            HashMap properties = proxy.getProperties();
            MapboxMapOptions gOptions = new MapboxMapOptions();
            gOptions.setPrefetchesTiles(
                    TiConvert.toBoolean(properties, "prefetchesTiles", false));
            gOptions.renderSurfaceOnTop(TiConvert.toBoolean(properties,
                    AkylasMapboxGLModule.PROPERTY_ZORDER_ON_TOP, false));
            gOptions.logoEnabled(
                    TiConvert.toBoolean(properties, "showLogo", false));
            // gOptions.zOrderOnTop(zOrderOnTop);
            return SupportMapFragment.newInstance(gOptions);
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;

    static {
        ArrayList<String> tmp = AkylasMapBaseView.KEY_SEQUENCE;
        tmp.add(AkylasMapboxGLModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
        KEY_SEQUENCE = tmp;
    }

    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
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

    HashMap cameraBuilderMap = null;

    private void addCameraUpdate(String key, Object value) {
        if (cameraBuilderMap == null) {
            cameraBuilderMap = new HashMap();
            mProcessUpdateFlags |= TIFLAG_NEEDS_CAMERA;
        }
        cameraBuilderMap.put(key, value);
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasMapboxGLModule.PROPERTY_MINZOOM:
            map.setMinZoomPreference(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasMapboxGLModule.PROPERTY_MAXZOOM:
            map.setMaxZoomPreference(TiConvert.toFloat(newValue, 22));
            break;
        case AkylasMapboxGLModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
            mRequiredZoomLevel = TiConvert.toFloat(newValue, 10);
            break;
        // case AkylasMapboxGLModule.PROPERTY_USER_LOCATION_BUTTON:
        // map.getUiSettings().setMyLocationButtonEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        case AkylasMapboxGLModule.PROPERTY_ZOOM_CONTROLS_ENABLED:
            map.getUiSettings().setZoomControlsEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapboxGLModule.PROPERTY_COMPASS_ENABLED:
            map.getUiSettings()
                    .setCompassEnabled(TiConvert.toBoolean(newValue, true));
            break;
        // case AkylasMapboxGLModule.PROPERTY_TOOLBAR_ENABLED:
        // map.getUiSettings()
        // .setMapToolbarEnabled(TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasMapboxGLModule.PROPERTY_INDOOR_CONTROLS_ENABLED:
        // map.getUiSettings().setIndoorLevelPickerEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        case AkylasMapboxGLModule.PROPERTY_SCROLL_ENABLED:
            map.getUiSettings().setScrollGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case TiC.PROPERTY_ZOOM_ENABLED:
            map.getUiSettings().setZoomGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapboxGLModule.PROPERTY_ROTATE_ENABLED:
            map.getUiSettings().setRotateGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;
        case AkylasMapboxGLModule.PROPERTY_TILT_ENABLED:
            map.getUiSettings().setTiltGesturesEnabled(
                    TiConvert.toBoolean(newValue, true));
            break;

        case TiC.PROPERTY_BEARING:
            getCameraBuilder().bearing(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasMapboxGLModule.PROPERTY_TILT:
            getCameraBuilder().tilt(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasMapboxGLModule.PROPERTY_ZOOM:
            targetZoom = TiConvert.toFloat(newValue, 0);
            getCameraBuilder().zoom(targetZoom);
            break;
        case "animationDuration":
            cameraAnimationDuration = TiConvert.toInt(newValue,
                    CAMERA_UPDATE_DURATION);
            break;
        case TiC.PROPERTY_REGION:
            mCameraRegion = AkylasMapboxGLModule.regionFromObject(newValue);
            if (mCameraRegion != null) {
                getCameraBuilder();
            }
            mCameraRegionUpdate = mCameraRegion != null;
            break;
        case AkylasMapboxGLModule.PROPERTY_CENTER_COORDINATE:
            LatLng pos = (LatLng) AkylasMapboxGLModule
                    .latlongFromObject(newValue);
            if (pos != null) {
                getCameraBuilder().target(pos);
            }
            break;
        case TiC.PROPERTY_MAP_TYPE:
            int type = TiConvert.toInt(newValue,
                    AkylasMapboxGLModule.MAP_TYPE_NORMAL);
            String mbStyle = Style.MAPBOX_STREETS;
            switch (type) {
            case AkylasMapboxGLModule.MAP_TYPE_HYBRID:
                mbStyle = Style.SATELLITE_STREETS;
                break;
            case AkylasMapboxGLModule.MAP_TYPE_DARK:
                mbStyle = Style.DARK;
                break;
            case AkylasMapboxGLModule.MAP_TYPE_LIGHT:
                mbStyle = Style.LIGHT;
                break;
            case AkylasMapboxGLModule.MAP_TYPE_SATELLITE:
                mbStyle = Style.SATELLITE;
                break;
            case AkylasMapboxGLModule.MAP_TYPE_TERRAIN:
                mbStyle = Style.OUTDOORS;
                break;
            case AkylasMapboxGLModule.MAP_TYPE_NONE:
                mbStyle = null;
                break;
            default:
                break;
            }
            // if (mbStyle != null) {
            // mbStyle = "mapbox://styles/mapbox/" + mbStyle;
            // }
            map.setStyle(mbStyle);
            break;
        // case AkylasMapboxGLModule.PROPERTY_FOCUS_OFFSET:
        // PointF offset = TiConvert.toPointF(newValue);
        // mapView.getOptions()
        // .setFocusPointOffset(new ScreenPos(offset.x, offset.y));
        // break;
        case TiC.PROPERTY_PADDING:
            padding = TiConvert.toPaddingRect(newValue, padding);
            mapView.setPadding((int) padding.left, (int) padding.top,
                    (int) padding.right, (int) padding.bottom);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MAP_INVALIDATE;
            break;
        // case AkylasMapboxGLModule.PROPERTY_MAPSTYLE:
        // if (newValue instanceof HashMap) {
        // try {
        // map.setMapStyle(new MapStyleOptions(
        // new JSONObject((HashMap) newValue).toString()));
        // } catch (Exception e) {
        //
        // }
        // } else if (newValue instanceof String) {
        // map.setMapStyle(new MapStyleOptions((String) newValue));
        // } else {
        // map.setMapStyle(null);
        // }
        // break;
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
                        nativeView.getMeasuredHeight(), 0, 0);
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

    public MapboxMap getMap() {
        return map;
    }

    @Override
    public void setUserLocationEnabled(boolean enabled) {
        // try {
        // map.setMyLocationEnabled(enabled);
        // } catch (Exception e) {
        // e.printStackTrace();
        // KrollDict data = new KrollDict();
        // data.putCodeAndMessage(-1, e.getMessage());
        // fireEvent(TiC.EVENT_ERROR, data, false, false);
        // }
    }
    //
    // protected void setCompassEnabled(boolean enabled) {
    // map.getUiSettings().setCompassEnabled(enabled);
    // }

    public float getMaxZoomLevel() {
        float defaultValue = TiConvert.toFloat(
                proxy.getProperty(AkylasMapboxGLModule.PROPERTY_MAXZOOM), -1);
        if (defaultValue != -1) {
            return defaultValue;
        }
        return proxy.getValueInUIThread(new Command<Float>() {
            @Override
            public Float execute() {
                return (float) map.getMaxZoomLevel();
            }
        }, defaultValue);
    }

    public float getMinZoomLevel() {
        float defaultValue = TiConvert.toFloat(
                proxy.getProperty(AkylasMapboxGLModule.PROPERTY_MINZOOM), -1);
        if (defaultValue != -1) {
            return defaultValue;
        }
        return proxy.getValueInUIThread(new Command<Float>() {
            @Override
            public Float execute() {
                return (float) map.getMinZoomLevel();
            }
        }, defaultValue);
    }

    public static float metersToEquatorPixels(MapboxMap map,
            final LatLng location, final float zoom, final float meters) {
        CameraPosition position = map.getCameraPosition();
        LatLng center = location;
        if (center == null) {
            center = position.target;
            if (center == null) {
                center = new LatLng(0, 0);
            }
        }
        double zoomLevel = zoom;
        if (zoomLevel < 0) {
            zoomLevel = position.zoom;
        }
        double latRadians = center.getLatitude() * Math.PI / 180;
        double metersPerPixel = 40075016.68 / (256 * Math.pow(2, zoomLevel));
        return (float) (meters / Math.cos(latRadians) / metersPerPixel);
    }

    @Override
    public float getMetersPerPixel(final float zoomToCheck,
            final Object position) {
        if (map == null) {
            return 0.0f;
        }
        final LatLng pos = (LatLng) AkylasMapboxGLModule
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
                    proxy.getProperty(AkylasMapboxGLModule.PROPERTY_ZOOM), 0);
        }
        return (float) currentCameraPosition.zoom;
    }

    public BaseAnnotationProxy getProxyByMarker(Annotation m) {
        // Object tag = m.getTag();
        // if (tag instanceof AnnotationProxy) {
        // return (AnnotationProxy) tag;
        // }
        if (m != null && handledAnnotations != null) {
            return handledAnnotations.get(m);
        }
        return null;
    }

    public Object getProxyByLayer(Layer l) {
        // Object tag = m.getTag();
        // if (tag instanceof AnnotationProxy) {
        // return (AnnotationProxy) tag;
        // }
        if (l != null && handledLayers != null) {
            return handledLayers.get(l);
        }
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

    protected KrollDict dictFromPoint(LatLng pos) {
        KrollDict d = TiViewHelper.dictFromMotionEvent(getTouchView(),
                lastDownEvent);
        setDictData(d, pos);
        return d;
    }

    protected void setDictData(KrollDict d, LatLng pos) {
        d.put(TiC.PROPERTY_LATITUDE, pos.getLatitude());
        d.put(TiC.PROPERTY_LONGITUDE, pos.getLongitude());
        // d.put(TiC.PROPERTY_ALTITUDE, point.getZ());
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasMapboxGLModule.PROPERTY_ZOOM, getZoomLevel());
        d.put(AkylasMapboxGLModule.PROPERTY_MAP, proxy);
    }

    // public void fireClickEvent(BaseAnnotationProxy proxy, final String
    // source) {
    // fireEventOnAnnotProxy(TiC.EVENT_CLICK, element, proxy, source);
    // }

    public boolean handleMarkerClick(Annotation element) {
        if (element == null) {
            return false;
        }
        Object theObj = getObjectForElement(element);
        if (theObj == null || (theObj instanceof BaseAnnotationProxy
                && !((BaseAnnotationProxy) theObj).touchable)) {
            // trick for untouchable as googlemap does not support it
            // onMapClick((LatLng) annoProxy.getPosition());
            return true;
        }

        if (theObj instanceof BaseAnnotationProxy) {
            fireEventOnAnnotProxy(TiC.EVENT_CLICK, (BaseAnnotationProxy) theObj,
                    element, AkylasMapboxGLModule.PROPERTY_PIN);
        } else {
            fireEventOnAnnotData(TiC.EVENT_CLICK, (HashMap) theObj, element,
                    AkylasMapboxGLModule.PROPERTY_PIN);
        }

        // make sure we fire the click first
        // fireClickEvent(annoProxy, AkylasMapboxGLModule.PROPERTY_PIN);
        setSelectedAnnotation(element);

        // Returning false here will enable native behavior, which shows the
        // info window.
        return !canShowInfoWindow(theObj);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        BaseAnnotationProxy annoProxy = getProxyByMarker(marker);
        if (annoProxy == null || !annoProxy.touchable) {
            return false;
        }
        handleMarkerClick(marker);
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

    // @Override
    // public void onMarkerDrag(Marker marker) {
    // Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    //
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasMapboxGLModule.ANNOTATION_DRAG_STATE_DRAGGING);
    // }
    // }

    // @Override
    // public void onMarkerDragEnd(Marker marker) {
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    // LatLng position = annoProxy.getPosition();
    // annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.getX());
    // annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.getY());
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasMapboxGLModule.ANNOTATION_DRAG_STATE_END);
    // }
    // }

    // @Override
    // public void onMarkerDragStart(Marker marker) {
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasMapboxGLModule.ANNOTATION_DRAG_STATE_START);
    // }
    // }

    private void handleInfoWindowClick(Marker marker) {
        AnnotationProxy annoProxy = (AnnotationProxy) getProxyByMarker(marker);
        if (annoProxy != null) {
            String clicksource = annoProxy.getMapInfoWindow().getClicksource();
            // The clicksource is null means the click event is not inside
            // "leftPane", "title", "subtible"
            // or "rightPane". In this case, use "infoWindow" as the
            // clicksource.
            if (clicksource == null) {
                clicksource = AkylasMapboxGLModule.PROPERTY_INFO_WINDOW;
            }
            fireEventOnAnnotProxy(TiC.EVENT_CLICK,
                    (BaseAnnotationProxy) annoProxy, marker, clicksource);
        }
    }

    @Override
    public boolean onInfoWindowClick(Marker marker) {
        handleInfoWindowClick(marker);
        return true;
    }

    // private BaseAnnotationProxy showingInfoProxy;

    // @Override
    // public View getInfoContents(Point marker) {
    // return null;
    // }

    public void infoWindowDidClose(AkylasMapInfoView infoView) {
        // showingInfoProxy = null;
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
        mpp = 156543.03392 * Math
                .cos(currentCameraPosition.target.getLatitude() * Math.PI / 180)
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
                result.put(TiC.PROPERTY_REGION,
                        AkylasMapboxGLModule.getFactory().regionToDict(bounds));
                result.put(AkylasMapboxGLModule.PROPERTY_ZOOM,
                        currentCameraPosition.zoom);
                result.put("mpp", mpp);
                result.put("mapdistance", mpp * nativeView.getWidth());
                result.put("bearing", currentCameraPosition.bearing);
                result.put("tilt", currentCameraPosition.tilt);
                result.put(AkylasMapboxGLModule.PROPERTY_USER_ACTION,
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

    // @Override
    // public void onPoiClick(PointOfInterest poi) {
    // if (proxy.hasListeners(AkylasMapboxGLModule.EVENT_POI, false)) {
    // KrollDict d = dictFromPoint(poi.latLng);
    // d.put("name", poi.name);
    // d.put("placeId", poi.placeId);
    // proxy.fireEvent(AkylasMapboxGLModule.EVENT_POI, d, false, false);
    // }
    // }
    //
    // @Override
    // public boolean onMyLocationButtonClick() {
    // if (proxy.hasListeners(AkylasMapboxGLModule.EVENT_LOCATION_BUTTON,
    // false)) {
    // proxy.fireEvent(AkylasMapboxGLModule.EVENT_LOCATION_BUTTON, null,
    // false, false);
    // }
    // return false;
    // }

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
        if (selectedElement instanceof Marker) {
            AnnotationProxy annot = (AnnotationProxy) getObjectForElement(
                    selectedElement);
            AkylasMapInfoView infoWindow = annot.getMapInfoWindow();
            Marker marker = getAnnotMarker(annot);
            if (infoWindow != null && marker != null
                    && marker.isInfoWindowShown()) {
                // Get a marker position on the screen
                PointF markerPoint = getProjection()
                        .toScreenLocation(marker.getPosition());
                MGLMarker gMarker = getMbMarker(annot);
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

    private MGLMarker getMbMarker(AnnotationProxy proxy) {
        return (MGLMarker) proxy.getMarker();
    }

    private Marker getAnnotMarker(BaseAnnotationProxy proxy) {
        MGLMarker timarker = (MGLMarker) proxy.getMarker();
        if (timarker != null) {
            return timarker.getMarker();
        }
        return null;
    }

    public void snapshot() {
        // map.snapshot(new .SnapshotReadyCallback() {
        //
        // @Override
        // public void onSnapshotReady(Bitmap snapshot) {
        // if (proxy.hasListeners(
        // AkylasMapboxGLModule.EVENT_ON_SNAPSHOT_READY, false)) {
        // TiBlob sblob = TiBlob.blobFromObject(snapshot);
        // KrollDict data = new KrollDict();
        // data.put("snapshot", sblob);
        // data.put("source", proxy);
        // proxy.fireEvent(AkylasMapboxGLModule.EVENT_ON_SNAPSHOT_READY,
        // data, false, false);
        // }
        // }
        // });
    }

    // @Override
    // public void onMapLoaded() {
    // if (proxy.hasListeners(TiC.EVENT_COMPLETE, false)) {
    // proxy.fireEvent(TiC.EVENT_COMPLETE, null, false, false);
    // }
    // }

    @Override
    public KrollDict getUserLocation() {
        if (!getUserLocationEnabled()) {
            return null;
        }
        // if (TiApplication.isUIThread()) {
        return AkylasMapboxGLModule.locationToDict(map.getMyLocation());
        // } else {
        // return AkylasMapboxGLModule.locationToDict(
        // (Location) TiMessenger.sendBlockingMainMessage(
        // mainHandler.obtainMessage(MSG_GET_MYLOCATION)));
        // }
        // return null;
    }

    @Override
    public boolean getUserLocationEnabled() {
        if (map == null) {
            return false;
        }
        return map.isMyLocationEnabled();

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
        float currentZoom = (float) ((currentCameraPosition != null)
                ? currentCameraPosition.zoom : 0);
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
        float currentZoom = (float) ((currentCameraPosition != null)
                ? currentCameraPosition.zoom : 0);
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
        // return proxy.getInUiThread(new Command<Projection>() {

        // @Override
        // public Projection execute() {
        return map.getProjection();
        // }
        // });
    }

    @Override
    public KrollDict getRegionDict() {
        LatLngBounds region = getProjection().getVisibleRegion().latLngBounds;
        return AkylasMapboxGLModule.getFactory().regionToDict(region);
    }

    AnimatorSet currentInfoWindowAnim = null;
    AkylasMapInfoView currentInfoInfoView = null;

    @Override
    public void handleDeselectElement(final Object element) {
        selectedElement = null;
        if (!TiApplication.isUIThread()) {
            proxy.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleDeselectElement(element);
                }
            });
            return;
        }
        // if (!proxy.canShowInfoWindow()) {
        // return;
        // }
        clearPositionUpdater();

        Object obj = getObjectForElement(element);
        if (obj instanceof BaseAnnotationProxy) {
            ((BaseAnnotationProxy) obj).onDeselect();
            // showingInfoProxy = null;
        } else {
            // if (element instanceof Marker) {
            // ((Marker) element).setStyle(AnnotationProxy.getMarkerStyle(this,
            // baseProjection, (HashMap) obj, false));
            // }
        }
        if (infoWindowContainer != null) {
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
                    if (currentInfoInfoView != null) {
                        infoWindowDidClose(currentInfoInfoView);
                        currentInfoInfoView = null;
                    }
                    // if (obj instanceof BaseAnnotationProxy) {
                    // ((BaseAnnotationProxy<LatLng>) obj).infoWindowDidClose();
                    // }
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

    private boolean canShowInfoWindow(Object data) {
        if (data instanceof BaseAnnotationProxy) {
            return ((BaseAnnotationProxy) data).canShowInfoWindow();
        } else if (data instanceof HashMap) {
            return TiConvert.toBoolean((HashMap) data,
                    AkylasMapBaseModule.PROPERTY_SHOW_INFO_WINDOW);
        }
        return false;
    }

    @Override
    public void handleSelectElement(final Object element) {
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    handleSelectElement(element);
                }
            }, false);
            return;
        }
        Object obj = getObjectForElement(element);
        if (element instanceof BaseAnnotationProxy) {
            obj = element;
            selectedElement = getAnnotMarker((BaseAnnotationProxy) obj);
        } else {
            selectedElement = element;

        }
        // if (proxy.getMarker() == null) {
        // return;
        // }
        // if (proxy instanceof AnnotationProxy) {
        // this is to make sure the marker is on top
        // Marker marker = getAnnotMarker((AnnotationProxy) proxy);
        // if (marker != null) {
        // marker.showInfoWindow();
        // }
        // }

        if (obj instanceof BaseAnnotationProxy) {
            // if (proxy != showingInfoMarker) {
            ((BaseAnnotationProxy) obj).onSelect();
            // }
        } else {
            // if (selectedElement instanceof Marker) {
            // ((Marker) selectedElement)
            // .setStyle(AnnotationProxy.getMarkerStyle(this,
            // baseProjection, (HashMap) obj, true));
            // }
        }

        if (!_canShowInfoWindow || !canShowInfoWindow(obj)) {
            return;
        }
        clearPositionUpdater();

        // AkylasMapInfoView infoView = null;
        if (currentInfoWindowAnim != null) {
            // needs to be done before the prepareInfoView
            currentInfoWindowAnim.cancel();
        }
        currentInfoInfoView = (AkylasMapInfoView) mInfoWindowCache
                .get("infoView");
        if (obj instanceof BaseAnnotationProxy) {
            // if (proxy != showingInfoMarker) {
            ((BaseAnnotationProxy) obj).onSelect();
            ((BaseAnnotationProxy) obj).prepareInfoView(currentInfoInfoView);
            // }
        } else {
            // if (selectedElement instanceof Marker) {
            // ((Marker) selectedElement)
            // .setStyle(AnnotationProxy.getMarkerStyle(this,
            // baseProjection, (HashMap) obj, true));
            // }
            BaseAnnotationProxy.prepareInfoViewForData(this,
                    currentInfoInfoView, (HashMap) obj);
        }
        if (currentInfoInfoView == null) {
            return;
        }

        if (infoWindowContainer == null) {
            infoWindowLayoutListener = new InfoWindowLayoutListener();
            infoWindowContainer = new LinearLayout(getContext());

            infoWindowContainer.setBackground(TiUIHelper.buildImageDrawable(
                    getContext(), calloutBgdImage, false, proxy));
            infoWindowContainer.getViewTreeObserver()
                    .addOnGlobalLayoutListener(infoWindowLayoutListener);
            infoWindowContainer.setLayoutParams(new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            overlayLayoutParams = (FrameLayout.LayoutParams) infoWindowContainer
                    .getLayoutParams();
            infoWindowContainer.setGravity(Gravity.LEFT | Gravity.TOP);
            container.addView(infoWindowContainer);
        } else {

            infoWindowContainer.removeAllViews();
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(currentInfoInfoView.getPaddingLeft(),
                currentInfoInfoView.getPaddingTop(),
                currentInfoInfoView.getPaddingRight(),
                currentInfoInfoView.getPaddingBottom()); // arrow
        // padding
        infoWindowContainer.addView(currentInfoInfoView, params);
        infoWindowContainer.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        infoWindowContainer.layout(0, 0, infoWindowContainer.getMeasuredWidth(),
                infoWindowContainer.getMeasuredHeight());

        if (obj instanceof BaseAnnotationProxy) {
            BaseAnnotationProxy proxy = (BaseAnnotationProxy) obj;
            trackedPosition = (LatLng) proxy.getPosition();
            final float iconWidth = ((MGLMarker) proxy.getMarker())
                    .getIconImageWidth();
            final float iconHeight = ((MGLMarker) proxy.getMarker())
                    .getIconImageHeight();
            float deltaX = iconWidth * (proxy.calloutAnchor.x - proxy.anchor.x);
            float deltaY = iconHeight
                    * (proxy.calloutAnchor.y - proxy.anchor.y);
            positionUpdaterRunnable = new PositionUpdaterRunnable((int) deltaX,
                    (int) deltaY);
            positionUpdaterRunnable.run();
            // handler.post(positionUpdaterRunnable);
        }

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
        if (mapView == null) {
            return;
        }

        final Activity activity = proxy.getActivity();
        for (Object data : (ArrayList<Object>) value) {
            if (data instanceof AnnotationProxy) {
                AnnotationProxy annotProxy = (AnnotationProxy) data;
                annotProxy.setActivity(activity);
                addAnnotationToMap(annotProxy);
            } else if (data instanceof HashMap) {
//                addAnnotationHashMapToMap((HashMap) data);
            }
        }
    }

    @Override
    public void handleRemoveAnnotation(final ArrayList value) {
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleRemoveAnnotation(value);
        // }
        // }, false);
        // return;
        // }

        for (Object data : (ArrayList<Object>) value) {
            if (data instanceof AnnotationProxy) {
                AnnotationProxy annotProxy = (AnnotationProxy) data;
                handleRemoveSingleAnnotation(annotProxy);
            }
        }
    }

    public void handleRemoveSingleAnnotation(BaseAnnotationProxy proxy) {
        Marker marker = getAnnotMarker((AnnotationProxy) proxy);
        // if (handledMarkers != null) {
        // handledMarkers.remove(marker);
        // }
        if (handledAnnotations == null) {
            handledAnnotations.remove(marker);
        }
        deselectAnnotation(proxy);
        proxy.wasRemoved();
    }

    @Override
    public void handleAddRoute(final ArrayList value) {
        if (mapView == null) {
            return;
        }
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleAddRoute(value);
        // }
        // }, false);
        // return;
        // }
        final Activity activity = proxy.getActivity();
        for (RouteProxy proxy : (ArrayList<RouteProxy>) value) {

            if (proxy.getPolyline() == null) {
                proxy.setPolyline(map.addPolyline(
                        proxy.getAndSetOptions(currentCameraPosition)));
                proxy.setMapView(AkMapView.this);
                proxy.setActivity(activity);
                proxy.setParentForBubbling(AkMapView.this.proxy);
                if (handledAnnotations == null) {
                    handledAnnotations = new WeakHashMap<Annotation, BaseAnnotationProxy>();
                }
                handledAnnotations.put(proxy.getPolyline(), proxy);
            }
        }
    }

    @Override
    public void handleRemoveRoute(final ArrayList value) {
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleRemoveRoute(value);
        // }
        // }, false);
        // return;
        // }

        for (RouteProxy proxy : (ArrayList<RouteProxy>) value) {
            Polyline line = proxy.getPolyline();

            if (line != null) {
                if (handledAnnotations == null) {
                    handledAnnotations.remove(line);
                }

            }
            deselectAnnotation(proxy);
            proxy.wasRemoved();
        }

    }

    @Override
    public void handleAddGroundOverlay(final ArrayList value) {
        if (mapView == null) {
            return;
        }
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleAddGroundOverlay(value);
        // }
        // }, false);
        // return;
        // }

        final Activity activity = proxy.getActivity();
        for (GroundOverlayProxy proxy : (ArrayList<GroundOverlayProxy>) value) {
//            proxy.setGroundOverlay(map.addGroundOverlay(
//                    proxy.getAndSetOptions(currentCameraPosition)));
//            proxy.setActivity(activity);
//            proxy.setMapView(AkMapView.this);
//            proxy.setParentForBubbling(AkMapView.this.proxy);
            // addedGroundOverlays.add(proxy);
        }
    }

    @Override
    public void handleRemoveGroundOverlay(final ArrayList value) {
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleRemoveGroundOverlay(value);
        // }
        // }, false);
        // return;
        // }

        for (GroundOverlayProxy proxy : (ArrayList<GroundOverlayProxy>) value) {
            proxy.wasRemoved();
        }
    }

    @Override
    public void handleAddCluster(final ArrayList value) {
        if (mapView == null) {
            return;
        }
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleAddCluster(value);
        // }
        // }, false);
        // return;
        // }

        final Activity activity = proxy.getActivity();
        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            proxy.setMapView(AkMapView.this);
            proxy.setParentForBubbling(AkMapView.this.proxy);
            proxy.setActivity(activity);
            getClusterManager()
                    .addClusterAlgorithm(proxy.getOrCreateAlgorithm());
        }
    }

    List<Layer> tileLayers = new ArrayList();
//    List<Layer> clusterLayers = new ArrayList();
    private final Comparator tileLayerComparator = new Comparator<Layer>() {
        @Override
        public int compare(Layer lhs, Layer rhs) {
            final float zIndexL = ((BaseTileSourceProxy) getProxyByLayer(lhs))
                    .getZIndex();
            final float zIndexR = ((BaseTileSourceProxy) getProxyByLayer(rhs))
                    .getZIndex();
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for
            // descending
            return zIndexL > zIndexR ? 1 : (zIndexL < zIndexR) ? -1 : 0;
        }
    };
//    private final Comparator clusterLayerComparator = new Comparator<Layer>() {
//        @Override
//        public int compare(Layer lhs, Layer rhs) {
//            final float zIndexL = ((BaseAnnotationProxy) getProxyByLayer(lhs))
//                    .getZIndex();
//            final float zIndexR = ((BaseAnnotationProxy) getProxyByLayer(rhs))
//                    .getZIndex();
//            // -1 - less than, 1 - greater than, 0 - equal, all inversed for
//            // descending
//            return zIndexL > zIndexR ? 1 : (zIndexL < zIndexR) ? -1 : 0;
//        }
//    };

    private void addTileLayer(Layer layer) {
        tileLayers.add(layer);
        tileLayers.sort(tileLayerComparator);
        resortLayers();
    }
//
//    private void addClusterLayer(Layer layer) {
//
//        clusterLayers.add(layer);
//        clusterLayers.sort(clusterLayerComparator);
//        resortLayers();
//    }
//
    private void removeTileLayer(Layer layer) {
        if (handledLayers != null) {
            handledLayers.remove(layer);
        }
        tileLayers.remove(layer);
        resortLayers();
    }
//
//    private void removeClusterLayer(Layer layer) {
//        if (handledLayers != null) {
//            handledLayers.remove(layer);
//        }
//        clusterLayers.remove(layer);
//        resortLayers();
//    }
//
    private boolean processingTileLayers = false;
//
    public void resortLayers() {
        if (processingTileLayers) {
            return;
        }
//        if (!TiApplication.isUIThread()) {
//            proxy.runInUiThread(new CommandNoReturn() {
//                @Override
//                public void execute() {
//                    resortLayers();
//                }
//            }, false);
//            return;
//        }
        mapView.getLayers().clear();
        if (baseLayer != null) {
            mapView.getLayers().add(baseLayer);
        }
        for (Layer layer : tileLayers) {
            mapView.getLayers().add(layer);
        }
//        for (Layer layer : clusterLayers) {
//            mapView.getLayers().add(layer);
//        }
//        mapView.getLayers().add(annotsLayer);
//        if (debugLayer != null) {
//            mapView.getLayers().add(debugLayer);
//        }
    }

    @Override
    public void handleRemoveCluster(final ArrayList value) {
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleRemoveCluster(value);
        // }
        // }, false);
        // return;
        // }

        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            proxy.wasRemoved();
        }
    }

    @Override
    public void handleAddTileSource(final ArrayList value, final int index) {
        if (mapView == null) {
            return;
        }
        // if (!TiApplication.isUIThread()) {
        // proxy.runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // handleAddTileSource(value, index);
        // }
        // }, false);
        // return;
        // }
        // int realIndex = index;
        final Activity activity = proxy.getActivity();
        processingTileLayers = true;
        for (TileSourceProxy proxy : (ArrayList<TileSourceProxy>) value) {
            proxy.setMapView(AkMapView.this);
            proxy.setParentForBubbling(AkMapView.this.proxy);
            proxy.setActivity(activity);
            if (proxy.getZIndex() == -1) {
                proxy.setZIndex(index);
            }
            Layer layer = proxy.getOrCreateLayer();
            if (handledLayers == null) {
                handledLayers = new WeakHashMap<Layer, Object>();
            }
            handledLayers.put(layer, proxy);
            // TileOverlayOptions options = ((TileSourceProxy) proxy)
            // .getTileOverlayOptions();
            if (layer != null) {
                // if (realIndex != -1) {
                // options.zIndex(realIndex);
                // }
                addTileLayer(layer);

                // proxy.setTileOverlay(map.addTileOverlay(options));
                // addedTileSources.add(proxy);
            }
            // if (realIndex != -1) {
            // realIndex++;
            // }
        }
        processingTileLayers = false;
        resortLayers();
    }

    @Override
    public void handleRemoveTileSource(final ArrayList value) {
        if (mapView == null) {
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

        processingTileLayers = true;
        for (TileSourceProxy proxy : (ArrayList<TileSourceProxy>) value) {
            TileLayer layer = proxy.getLayer();
            if (layer != null) {
                removeTileLayer(layer);
            }
            // layer.setAnnotationEventListener(null);
            proxy.wasRemoved();
        }
        processingTileLayers = false;
        resortLayers();
    }

    public void prepareAnnotation(AnnotationProxy proxy) {
        AkylasMarker marker = proxy.getMarker();
        if (marker != null) {
            // already in
            removeAnnotation(marker);
        }
        proxy.setMapView(AkMapView.this);
        if (proxy.getParentForBubbling() == null) {
            proxy.setParentForBubbling(AkMapView.this.proxy);
        }
        MGLMarker gMarker = new MGLMarker((AnnotationProxy) proxy);
        proxy.setMarker(gMarker);
    }

//    public Marker addAnnotationToSource(AnnotationProxy proxy,
//            GeoJsonSource source) {
//        // proxy.setActivity(activity);
//        prepareAnnotation(proxy);
//        Marker marker = proxy.createMarker(baseProjection);
//        if (marker != null) {
//            source.add(marker);
//            // we need to set the position again because addMarker can be long
//            // and
//            // position might already have changed
//            // marker.setPosition((LatLng) proxy.getPosition());
//            ((MGLMarker) proxy.getMarker()).setMarker(marker);
//            // googlemarker.setTag(proxy);
//            if (handledMarkers == null) {
//                handledMarkers = new HashMap<Annotation, BaseAnnotationProxy>();
//            }
//            handledMarkers.put(marker, proxy);
//        }
//
//        // timarkers.add(proxy.getMarker());
//        return marker;
//    }

    public Marker addAnnotationToMap(AnnotationProxy proxy) {
        prepareAnnotation(proxy);
        MGLMarker gMarker = (MGLMarker) proxy.getMarker();
        Marker marker = map.addMarker(gMarker.getMarkerOptions());
        // we need to set the position again because addMarker can be long and
        // position might already have changed
        marker.setPosition((LatLng) proxy.getPosition());
        gMarker.setMarker(marker);
        if (handledAnnotations == null) {
            handledAnnotations = new WeakHashMap<Annotation, BaseAnnotationProxy>();
        }
        handledAnnotations.put(marker, proxy);
        // googlemarker.setTag(proxy);
        if (marker == selectedElement) {
            handleSelectElement(marker);
        }
        return marker;
    }

//    public Marker addAnnotationHashMapToMap(HashMap data) {
//        Marker marker = AnnotationProxy.createMarker(this, baseProjection, data,
//                false);
//        if (marker != null) {
//            annotsSource.add(marker);
//        }
//        return marker;
//    }

    // @Override
    // public void onMyLocationChange(Location location) {
    // if (shouldFollowUserLocation
    // && mUserTrackingMode != AkylasMapboxGLModule.TrackingMode.NONE) {
    // CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
    // cameraBuilder.target(new LatLng(location.getLatitude(),
    // location.getLongitude()));
    // if (mUserTrackingMode ==
    // AkylasMapboxGLModule.TrackingMode.FOLLOW_BEARING
    // && location.hasBearing()) {
    // cameraBuilder.bearing(location.getBearing());
    // }
    //
    // float currentZoom = (currentCameraPosition != null)
    // ? currentCameraPosition.zoom : 0;
    // if (currentZoom < mRequiredZoomLevel) {
    // if (location.hasAccuracy()) {
    // // approx meterPerDegree latitude, plus some margin
    // double delta = (location.getAccuracy() / 110000) * 1.2;
    // final LatLngBounds currentBox = map.getProjection()
    // .getVisibleRegion().latLngBounds;
    // LatLng desiredSouthWest = new LatLng(
    // location.getLatitude() - delta,
    // location.getLongitude() - delta);
    //
    // LatLng desiredNorthEast = new LatLng(
    // location.getLatitude() + delta,
    // location.getLongitude() + delta);
    //
    // if (desiredNorthEast.latitude != currentBox.northeast.latitude
    // || desiredNorthEast.longitude != currentBox.northeast.longitude
    // || desiredSouthWest.latitude != currentBox.southwest.latitude
    // || desiredSouthWest.longitude != currentBox.southwest.longitude) {
    // cameraBuilder.zoom(mRequiredZoomLevel);
    // }
    //
    // } else {
    // cameraBuilder.zoom(mRequiredZoomLevel);
    // }
    //
    // } else {
    // cameraBuilder.zoom(currentZoom);
    // }
    // if (currentCameraPosition != null) {
    // cameraBuilder.tilt(currentCameraPosition.tilt);
    // }
    // CameraPosition position = cameraBuilder.build();
    // map.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    // }
    // if (proxy.hasListeners(TiC.EVENT_LOCATION, false)) {
    // KrollDict d = new KrollDict();
    // d.put(TiC.PROPERTY_LATITUDE, location.getLatitude());
    // d.put(TiC.PROPERTY_LONGITUDE, location.getLongitude());
    // d.put(TiC.PROPERTY_ALTITUDE, location.getAltitude());
    // d.put(TiC.PROPERTY_ACCURACY, location.getAccuracy());
    // // d.put(TiC.PROPERTY_ALTITUDE_ACCURACY, null); // Not provided
    // d.put(TiC.PROPERTY_HEADING, location.getBearing());
    // d.put(TiC.PROPERTY_SPEED, location.getSpeed());
    // d.put(TiC.PROPERTY_TIMESTAMP, location.getTime());
    // d.put(TiC.PROPERTY_REGION, getRegionDict());
    // d.put(AkylasMapboxGLModule.PROPERTY_MAP, proxy);
    // proxy.fireEvent(TiC.EVENT_LOCATION, d, false, false);
    // }
    // }

    void updateCamera(final HashMap props) {
        if (props == null)
            return;
        if (mapView == null) {
            props.remove(TiC.PROPERTY_ANIMATE);
            if (!props.containsKey(TiC.PROPERTY_REGION) && props.containsKey(
                    AkylasMapboxGLModule.PROPERTY_CENTER_COORDINATE)) {
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

    public void updateMarkerPosition(final BaseAnnotationProxy annotProxy,
            final Marker marker, final LatLng toPosition,
            final long animationDuration) {
        boolean animated = animationDuration > 0;
        if (!animated || !shouldAnimate() || annotProxy.getPosition() == null
                || toPosition == null) {
            marker.setPos(toPosition);
            return;
        }
        final LatLng startLatLng = (LatLng) annotProxy.getPosition();
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
                    double lat = t * toPosition.getY()
                            + (1 - t) * startLatLng.getY();
                    double lng = t * toPosition.getX()
                            + (1 - t) * startLatLng.getX();
                    marker.setPos(baseProjection.fromLatLong(lat, lng));

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
        marker.setRotation(-heading);
        return;
    }

    private ClusterManagerPlugin _clusterManager = null;

    public ClusterManagerPlugin getClusterManager() {
        if (_clusterManager == null && map != null) {
            _clusterManager = new ClusterManagerPlugin<AnnotationProxy>(
                    getContext(), map);
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
            // Projection proj = getProjection();
            List<Object> result = new ArrayList<>();
            Object[] array = (Object[]) arg;
            TiPoint pt;
            LatLng res;
            for (int i = 0; i < array.length; i++) {
                pt = TiConvert.toPoint(array[i]);
                if (pt != null) {
                    Point point = pt.compute(nativeView.getWidth(),
                            nativeView.getHeight());
                    res = getScreenPos(point.x, point.y);
                    result.add(new Object[] { res.getY(), res.getX() });
                }

            }
            return result.toArray();
        }

        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected KrollDict fromScreenLocation(Point p) {
        return AkylasMapboxGLModule.latLongToDict(getScreenPos(p.x, p.y));
    }

    @Override
    public boolean isElementARoute(Object element) {
        return element instanceof RouteProxy || element instanceof Line;
    }

    @Override
    public Object getObjectForElement(Object element) {
        BaseAnnotationProxy annotProxy = proxyForElement(element);
        if (annotProxy != null) {
            return annotProxy;
        }
        return null;
    }

    @Override
    public Object getElementFromObject(Object element) {
        if (element instanceof Annotation) {
            return element;
        } else if (element instanceof AnnotationProxy) {
            return ((AnnotationProxy) element).getAnnotation();
        } else if (element instanceof RouteProxy) {
            return ((RouteProxy) element).getAnnotation();
        }
        return null;
    }

    @Override
    protected boolean isElementSelectable(Object element) {
        if (element instanceof BaseAnnotationProxy) {
            return ((BaseAnnotationProxy<LatLng>) element).getSelectable();
        } else if (element instanceof HashMap) {
            return TiConvert.toBoolean((HashMap) element,
                    AkylasMapBaseModule.PROPERTY_SELECTABLE);
        } else {
            BaseAnnotationProxy annotProxy = proxyForElement(element);
            if (annotProxy != null) {
                return annotProxy.getSelectable();
            }
        }
        return false;
    }

    @Override
    protected BaseAnnotationProxy proxyForElement(Object element) {
        if (element instanceof BaseAnnotationProxy) {
            return (BaseAnnotationProxy) element;
        }
        return getProxyByMarker((Annotation) element);
    }

    // public void removeElement(Annotation m) {
    // annotsSource.remove(m);
    // BaseAnnotationProxy proxy = getProxyByMarker(m);
    // if (proxy != null) {
    // // GoogleMapMarker marker = (GoogleMapMarker) proxy.getMarker();
    // // marker.setTag(null);
    // // if (handledMarkers != null) {
    // handledMarkers.remove(m);
    // // }
    // deselectAnnotation(proxy);
    // // proxy.removeFromMap(); // only remove from map
    // } else {
    // // m.remove();
    // }
    //
    // }

}
