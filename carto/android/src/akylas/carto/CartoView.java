/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.carto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.AkylasMapInfoView;
import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseAnnotationProxy;
import akylas.map.common.BaseTileSourceProxy;
import akylas.map.common.ReusableView;
import android.graphics.RectF;

import org.appcelerator.kroll.KrollDict;
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

import com.carto.components.PanningMode;
import com.carto.core.BinaryData;
import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapRange;
import com.carto.core.ScreenBounds;
import com.carto.core.ScreenPos;
import com.carto.datasources.CartoOnlineTileDataSource;
import com.carto.datasources.LocalVectorDataSource;
import com.carto.datasources.PackageManagerTileDataSource;
import com.carto.datasources.PersistentCacheTileDataSource;
import com.carto.datasources.TileDataSource;
import com.carto.layers.CartoBaseMapStyle;
import com.carto.layers.Layer;
import com.carto.layers.TileLayer;
import com.carto.layers.VectorElementEventListener;
import com.carto.layers.VectorLayer;
import com.carto.layers.VectorTileLayer;
import com.carto.packagemanager.CartoPackageManager;
import com.carto.projections.Projection;
import com.carto.styles.CompiledStyleSet;
import com.carto.ui.MapView;
import com.carto.ui.VectorElementClickInfo;
import com.carto.utils.AssetUtils;
import com.carto.utils.ZippedAssetPackage;
import com.carto.vectorelements.Line;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.VectorElement;
import com.carto.vectortiles.MBVectorTileDecoder;
import com.carto.vectortiles.VectorTileDecoder;
import com.carto.ui.MapClickInfo;
import com.carto.ui.MapEventListener;

public class CartoView extends AkylasMapBaseView implements OnLifecycleEvent {
    private final MapEventListener mapEventListener = new MapEventListener() {

        private void handleMapMoved(boolean idle) {
            MapPos pos = getCenterPos();
            float zoom = mapView.getZoom();
            mpp = 156543.03392 * Math.cos(pos.getX() * Math.PI / 180)
                    / Math.pow(2, zoom);
            if (userAction) {
                setShouldFollowUserLocation(false);
                // userAction = false;
            }
            // if (preLayout) {
            //
            // // moveCamera will trigger another callback, so we do this to
            // make
            // // sure
            // // we don't fire event when region is set initially
            // preLayout = false;
            // handleCameraUpdate();
            // } else
            if (mapView != null) {
                if (proxy != null && proxy.viewInitialised() && proxy
                        .hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
                    MapBounds bounds = getScreenRegion();
                    KrollDict result = new KrollDict();
                    result.put(TiC.PROPERTY_REGION, AkylasCartoModule
                            .getFactory().regionToDict(bounds));
                    result.put(AkylasCartoModule.PROPERTY_ZOOM, zoom);
                    result.put("mpp", mpp);
                    result.put("mapdistance", mpp * nativeView.getWidth());
                    result.put("bearing", mapView.getRotation());
                    result.put("tilt", mapView.getTilt());
                    result.put(AkylasCartoModule.PROPERTY_USER_ACTION,
                            userAction);
                    result.put("idle", idle);
                    proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false,
                            false);
                }
            }
        }

        @Override
        public void onMapMoved() {
            super.onMapMoved();
            if (positionUpdaterRunnable != null) {
                handler.post(positionUpdaterRunnable);
            }
            handleMapMoved(false);
        }

        @Override
        public void onMapIdle() {
            super.onMapIdle();
            // map fully loaded
            if (proxy.hasListeners(TiC.EVENT_COMPLETE, false)) {
                proxy.fireEvent(TiC.EVENT_COMPLETE, null, false, false);
            }
        }

        @Override
        public void onMapStable() {
            super.onMapStable();
            targetZoom = -1;
            handleMapMoved(true);
        }

        @Override
        public void onMapClicked(MapClickInfo mapClickInfo) {
            super.onMapClicked(mapClickInfo);
            switch (mapClickInfo.getClickType()) {
//            case CLICK_TYPE_LONG: {
//                if (!hasListeners(TiC.EVENT_LONGPRESS, false))
//                    return;
//                MapPos point = getConvertedMapPos(mapClickInfo.getClickPos());
//                fireEvent(TiC.EVENT_LONGPRESS, dictFromPoint(point), false,
//                        false);
//                break;
//            }
            // // case CLICK_TYPE_DOUBLE: {
            // // mapView.zoom(1, point,
            // // animate ? cameraAnimationDuration / 1000 : 0);
            // // }
            case CLICK_TYPE_SINGLE: {
                MapPos point = getConvertedMapPos(mapClickInfo.getClickPos());
                onMapClick(point);
                break;
            }
            default:
                break;
            }
        }
    };

    private MapPos getCenterPos() {
        return getConvertedMapPos(mapView.getFocusPos());
        // return baseProjection.toLatLong(pos.getX(), pos.getY());
    }

    private MapBounds getScreenRegion() {
        final MapPos ne = getScreenPos(mapView.getWidth(), 0);
        final MapPos sw = getScreenPos(0, mapView.getHeight());
        return new MapBounds(sw, ne);
    }

    // @Override
    protected KrollDict dictFromMotionEvent(String type, MotionEvent e) {
        if (type.equals(TiC.EVENT_CLICK) || type.equals(TiC.EVENT_SINGLE_TAP)) {
            if (_selectOnTap) {
                setSelectedAnnotation(null);
            }
        }
        KrollDict res = dictFromMotionEvent(e);
        MapPos point = getScreenPos((int) e.getX(), (int) e.getY());
        if (point != null) {
            setDictData(res, point);

        }
        return res;
    }

    private void onMapClick(MapPos point) {
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

    private MapPos getConvertedMapPos(MapPos pos) {
        return baseProjection.toWgs84(pos);
    }

    private MapPos getScreenPos(int x, int y) {
        return getConvertedMapPos(mapView.screenToMap(new ScreenPos(x, y)));
    }

    private final VectorElementEventListener vectorEventListener = new VectorElementEventListener() {
        @Override
        public boolean onVectorElementClicked(VectorElementClickInfo info) {
            VectorElement element = info.getVectorElement();
            // Object annoProxy = getObjectForElement(element);
            if (element instanceof Marker) {
                // if (annoProxy == null
                // || !((AnnotationProxy) annoProxy).touchable) {
                // return false;
                // }
                handleMarkerClick(element);
                return !handleMarkerClick(element);
            } else if (element instanceof Line) {
                if (_canSelectRoute) {
                    // if (handledPolylines != null) {
                    // BaseRouteProxy route = handledPolylines.get(polyline);
                    return !handleMarkerClick(element);
                    // }
                } else if (lastDownEvent != null) {
                    onMapClick(getConvertedMapPos(info.getClickPos()));
                }
            }
            return true;

        }
    };

    private static final String TAG = "AkylasMapView";
    // private MapView map;
    protected boolean animate = true;
    protected boolean preLayout = true;
    protected HashMap preLayoutUpdateCamera;
    AkylasMapInfoView infoWindow = null;
    // protected ArrayList<AkylasMarker> timarkers;
    // protected WeakHashMap<Polyline, RouteProxy> handledPolylines;
    protected HashMap<VectorElement, BaseAnnotationProxy> handledMarkers;
    protected HashMap<Layer, Object> handledLayers;
    // private Fragment fragment;

    // private float mRequiredZoomLevel = 10;
    // private CameraPosition currentCameraPosition = null;

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
    private MapPos trackedPosition;
    private Runnable positionUpdaterRunnable;

    LocalVectorDataSource annotsSource;
    VectorLayer annotsLayer;
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
            // handler.postDelayed(this, POPUP_POSITION_REFRESH_INTERVAL);
            if (trackedPosition != null
                    && infoWindowContainer.getVisibility() == View.VISIBLE) {
                ScreenPos targetPosition = mapView.mapToScreen(trackedPosition);
                if (lastXPosition != targetPosition.getX()
                        || lastYPosition != targetPosition.getY()) {
                    overlayLayoutParams.leftMargin = (int) (targetPosition
                            .getX() - popupXOffset + markerWidth);
                    overlayLayoutParams.topMargin = (int) (targetPosition.getY()
                            - popupYOffset + markerHeight);
                    lastXPosition = (int) targetPosition.getX();
                    lastYPosition = (int) targetPosition.getY();
                    infoWindowContainer.setLayoutParams(overlayLayoutParams);
                }
            }
        }
    }

    public CartoView(final TiViewProxy proxy, final Activity activity) {
        super(proxy);
        container = new FrameLayout(activity) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                boolean shouldNot = CartoView.this.touchPassThrough == true;
                if (shouldNot) {
                    return false;
                }
                boolean result = false;
                if (CartoView.this.isTouchEnabled == true) {
                    result = interceptTouchEvent(ev)
                            || super.dispatchTouchEvent(ev);
                    int action = ev.getAction();
                    if (result && action != MotionEvent.ACTION_DOWN
                            && pointerDown) { // use is moving on the map
                        // if (_clusterManager != null) {
                        // _clusterManager.onCameraMove();
                        // } else {
                        // onCameraMove();

                        // }
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
        // AkylasCartoModule.PROPERTY_ZORDER_ON_TOP),
        // false);
        // gOptions.zOrderOnTop(zOrderOnTop);
        // boolean liteMode = TiConvert.toBoolean(
        // proxy.getProperty(
        // AkylasCartoModule.PROPERTY_LITE_MODE),
        // false);
        // gOptions.liteMode(liteMode);
        // }

        mapView = new MapView(activity);
        mapView.setMapEventListener(mapEventListener);
        // baseProjection = new EPSG3857();
        mapView.getOptions().setZoomGestures(true);
        baseProjection = mapView.getOptions().getBaseProjection();
        // mapView.getOptions().setBaseProjection(baseProjection);
        // baseProjection = mapView.getOptions().getBaseProjection();
        annotsSource = new LocalVectorDataSource(baseProjection);
        // mapView.onCreate(new Bundle());
        mapView.onResume();
        // mapView.getMapAsync(this);
        container.addView(mapView);

        // Initialize a vector layer with the previous data source
        annotsLayer = new VectorLayer(annotsSource);
        // Set visible zoom range for the vector layer
        annotsLayer.setVectorElementEventListener(vectorEventListener);
        // Add the previous vector layer to the map
        mapView.getLayers().add(annotsLayer);
        annotsLayer.setVisibleZoomRange(new MapRange(0, 24));

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

    public Projection getProjection() {
        return baseProjection;
    }

    public VectorTileDecoder getTileDecoder() {
        return baseStyleDecoder;
    }

    public void setMapType(int type) {
        CartoBaseMapStyle cartoType = CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER;
        switch (type) {
        case AkylasCartoModule.MAP_TYPE_VOYAGER:
            cartoType = CartoBaseMapStyle.CARTO_BASEMAP_STYLE_VOYAGER;
            break;
        case AkylasCartoModule.MAP_TYPE_POSITRON:
            cartoType = CartoBaseMapStyle.CARTO_BASEMAP_STYLE_POSITRON;
            break;
        case AkylasCartoModule.MAP_TYPE_DARKMATTER:
            cartoType = CartoBaseMapStyle.CARTO_BASEMAP_STYLE_DARKMATTER;
            break;
        case AkylasCartoModule.MAP_TYPE_NONE:
            cartoType = null;
            break;
        default:
            break;
        }
        (new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                setBaseLayer((CartoBaseMapStyle) params[0]);
                return null;
            }
        }).execute(cartoType);
    }

    VectorTileLayer baseLayer = null;
    TileDataSource source = null;
    MBVectorTileDecoder baseStyleDecoder;
    String langCode = null;
    boolean mBuildingsEnabled = false;
    ZippedAssetPackage styleAsset;

    public ZippedAssetPackage getStyleZippedAsset() {
        if (styleAsset == null) {
            BinaryData data = AssetUtils.loadAsset("carto.zip");
            styleAsset = new ZippedAssetPackage(data);
        }
        return styleAsset;
    }

    public CartoPackageManager getOfflineManager() {

        return AkylasCartoModule.getOfflineManager();
    }

    VectorTileListener currentListener;

    protected void setBaseLayer(CartoBaseMapStyle style) {
        if (baseLayer != null) {
            mapView.getLayers().remove(baseLayer);
            baseLayer = null;
        }
        if (style == null) {
            return;
        }
        String type = "voyager";
        if (style == CartoBaseMapStyle.CARTO_BASEMAP_STYLE_DARKMATTER) {
            type = "darkmatter";
        } else if (style == CartoBaseMapStyle.CARTO_BASEMAP_STYLE_POSITRON) {
            type = "positron";
        }
        baseStyleDecoder = new MBVectorTileDecoder(
                new CompiledStyleSet(getStyleZippedAsset(), type));

        if (langCode != null) {
            baseStyleDecoder.setStyleParameter("lang", langCode);
        }
        if (mBuildingsEnabled) {
            baseStyleDecoder.setStyleParameter("buildings", "2");
        }
        if (source == null) {
            // source = new OrderedDataSource(
            // new PackageManagerTileDataSource(getOfflineManager()),
            // new PersistentCacheTileDataSource(
            // new CartoOnlineTileDataSource("carto.streets"),
            // AkylasCartoModule.getMapCacheFolder()
            // + "/carto.db"));
            source = new PersistentCacheTileDataSource(
                    new CartoOnlineTileDataSource("carto.streets"),
                    AkylasCartoModule.getMapCacheFolder() + "/carto.db");
        }
        // create layer
        baseLayer = new VectorTileLayer(source, baseStyleDecoder);
        // baseLayer.setVectorTileEventListener(getVectorTileListener());

        // add it to the map
        mapView.getLayers().insert(0, baseLayer);

        // currentListener = getVectorTileListener();
        // if (style != null) {
        // baseLayer = new CartoOnlineVectorTileLayer(style);
        // if (langCode != null) {
        // MBVectorTileDecoder decoder = (MBVectorTileDecoder) baseLayer
        // .getTileDecoder();
        // decoder.setStyleParameter("lang", langCode);
        // }
        // mapView.getLayers().insert(0, baseLayer);
        // }

    }

    VectorLayer debugLayer;

    private VectorTileListener initializeVectorTileListener() {

        Projection projection = mapView.getOptions().getBaseProjection();
        LocalVectorDataSource source = new LocalVectorDataSource(projection);

        debugLayer = new VectorLayer(source);
        mapView.getLayers().add(debugLayer);

        // Layer layer = mapView.getLayers().get(0);

        VectorTileListener listener = new VectorTileListener(debugLayer);

        // if (layer instanceof VectorTileLayer) {
        // ((VectorTileLayer)layer).setVectorTileEventListener(listener);
        // }

        return listener;
    }

    public VectorTileListener getVectorTileListener() {
        if (currentListener == null) {
            currentListener = initializeVectorTileListener();
        }
        return currentListener;
    }

    protected void updateLandCode(String code) {
        if (code != langCode) {
            langCode = code;
            baseStyleDecoder.setStyleParameter("lang", langCode);
        }
    }

    protected void updateBuildings(boolean value) {
        if (value != mBuildingsEnabled) {
            mBuildingsEnabled = value;
            baseStyleDecoder.setStyleParameter("buildings",
                    mBuildingsEnabled ? "2" : "1");
        }
    }
    // @Override
    // public void onMapReady(GoogleMap map) {
    // setMap(map);
    // }
    //
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

    // private void setMapListeners(GoogleMap theMap, CartoView mapView) {
    // theMap.setOnMarkerClickListener(mapView);
    // theMap.setOnMapClickListener(mapView);
    // theMap.setOnMarkerDragListener(mapView);
    // theMap.setOnInfoWindowClickListener(mapView);
    // theMap.setInfoWindowAdapter(mapView);
    // theMap.setOnMapLongClickListener(mapView);
    // theMap.setOnMapLoadedCallback(mapView);
    // theMap.setOnPolygonClickListener(mapView);
    // theMap.setOnPolylineClickListener(mapView);
    // theMap.setOnMyLocationChangeListener(mapView);
    // theMap.setOnMyLocationButtonClickListener(mapView);
    // theMap.setOnPoiClickListener(mapView);
    // theMap.setOnCameraMoveCanceledListener(mapView);
    // theMap.setOnCameraIdleListener(mapView);
    // theMap.setOnCameraMoveListener(mapView);
    // theMap.setOnCameraMoveStartedListener(mapView);
    // }

    // private void setMap(GoogleMap newMap) {
    // if (this.map != null) {
    // setMapListeners(this.map, null);
    // }
    // this.map = newMap;
    // if (map != null) {
    // setMapListeners(this.map, this);
    // proxy.realizeViews(this, true, true);
    // }
    // }

    // public Fragment getFragment() {
    // return fragment;
    // }

    // private void releaseFragment() {
    // if (map != null) {
    // map.clear();
    // map = null;
    // }
    // if (fragment != null) {
    // FragmentManager fragmentManager = fragment.getFragmentManager();
    // if (fragmentManager != null && !fragmentManager.isDestroyed()) {
    // FragmentTransaction transaction = null;
    // Fragment tabFragment = fragmentManager
    // .findFragmentById(android.R.id.tabcontent);
    // if (tabFragment != null) {
    // FragmentManager childManager = tabFragment
    // .getChildFragmentManager();
    // transaction = childManager.beginTransaction();
    // } else {
    // transaction = fragmentManager.beginTransaction();
    // }
    // transaction.remove(fragment);
    // transaction.commitAllowingStateLoss();
    // }
    // fragment = null;
    // }
    // }

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
        // if (_clusterManager != null) {
        // _clusterManager.setRenderer(null);
        // _clusterManager.setOnClusterClickListener(null);
        // _clusterManager.setOnClusterInfoWindowClickListener(null);
        // _clusterManager.setOnClusterItemClickListener(null);
        // _clusterManager.setOnClusterItemInfoWindowClickListener(null);
        // _clusterManager.setOnCameraMoveListener(null);
        // _clusterManager = null;
        // }
        if (this.mapView != null) {
            // setMapListeners(this.map, null);
            // this.map.clear();
            this.mapView = null;
        }
        // releaseFragment();

        if (handledMarkers != null) {
            handledMarkers.clear();
        }
        if (handledLayers != null) {
            handledLayers.clear();
        }
        // if (handledPolylines != null) {
        // handledPolylines.clear();
        // }
    }

    // protected Fragment createFragment() {
    // if (proxy == null) {
    // return SupportMapFragment.newInstance();
    // } else {
    // boolean zOrderOnTop = TiConvert.toBoolean(
    // proxy.getProperty(
    // AkylasCartoModule.PROPERTY_ZORDER_ON_TOP),
    // false);
    // GoogleMapOptions gOptions = new GoogleMapOptions();
    // gOptions.zOrderOnTop(zOrderOnTop);
    // return SupportMapFragment.newInstance(gOptions);
    // }
    // }

    protected static final ArrayList<String> KEY_SEQUENCE;

    static {
        ArrayList<String> tmp = AkylasMapBaseView.KEY_SEQUENCE;
        tmp.add(AkylasCartoModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM);
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
        case AkylasCartoModule.PROPERTY_MINZOOM:
            // map.setMinZoomPreference(TiConvert.toFloat(newValue, 0));
            break;
        case AkylasCartoModule.PROPERTY_MAXZOOM:
            // map.setMaxZoomPreference(TiConvert.toFloat(newValue, 22));
            break;
        // case AkylasCartoModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM:
        // mRequiredZoomLevel = TiConvert.toFloat(newValue, 10);
        // break;
        // case AkylasCartoModule.PROPERTY_USER_LOCATION_BUTTON:
        // map.getUiSettings().setMyLocationButtonEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_ZOOM_CONTROLS_ENABLED:
        // map.getUiSettings().setZoomControlsEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_COMPASS_ENABLED:
        // map.getUiSettings()
        // .setCompassEnabled(TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_TOOLBAR_ENABLED:
        // map.getUiSettings()
        // .setMapToolbarEnabled(TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_INDOOR_CONTROLS_ENABLED:
        // map.getUiSettings().setIndoorLevelPickerEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_SCROLL_ENABLED:
        // map.getUiSettings().setScrollGesturesEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        case TiC.PROPERTY_ZOOM_ENABLED:
            mapView.getOptions()
                    .setZoomGestures(TiConvert.toBoolean(newValue, true));
            break;
        case AkylasCartoModule.PROPERTY_ROTATE_ENABLED:
            mapView.getOptions()
                    .setRotatable(TiConvert.toBoolean(newValue, true));
            break;
        case "panningMode":
            mapView.getOptions()
                    .setPanningMode(PanningMode.values()[TiConvert.toInt(newValue, 0)]);
            break;
        // case AkylasCartoModule.PROPERTY_TILT_ENABLED:
        // map.getUiSettings().setTiltGesturesEnabled(
        // TiConvert.toBoolean(newValue, true));
        // break;
        case AkylasCartoModule.PROPERTY_BUILDINGS_ENABLED:
            updateBuildings(TiConvert.toBoolean(newValue, true));
            // map.setBuildingsEnabled(TiConvert.toBoolean(newValue, true));
            break;
        // case AkylasCartoModule.PROPERTY_INDOOR_ENABLED:
        // map.setIndoorEnabled(TiConvert.toBoolean(newValue, true));
        // break;
        // case AkylasCartoModule.PROPERTY_TRAFFIC:
        // map.setTrafficEnabled(TiConvert.toBoolean(newValue, false));
        // break;

        case TiC.PROPERTY_BEARING:
        case AkylasCartoModule.PROPERTY_TILT:
        case AkylasCartoModule.PROPERTY_ZOOM:
        case TiC.PROPERTY_REGION:
        case AkylasCartoModule.PROPERTY_CENTER_COORDINATE:
            addCameraUpdate(key, newValue);

            break;
        case AkylasCartoModule.PROPERTY_ANIMATION_DURATION:
            cameraAnimationDuration = TiConvert.toInt(newValue,
                    CAMERA_UPDATE_DURATION);
            break;

        case TiC.PROPERTY_MAP_TYPE:
            int type = TiConvert.toInt(newValue,
                    AkylasCartoModule.MAP_TYPE_VOYAGER);
            setMapType(type);
            break;
        case AkylasCartoModule.PROPERTY_FOCUS_OFFSET:
            PointF offset = TiConvert.toPointF(newValue);
            mapView.getOptions()
                    .setFocusPointOffset(new ScreenPos(offset.x, offset.y));
            break;
        case TiC.PROPERTY_PADDING:
            padding = TiConvert.toPaddingRect(newValue, padding);
            mapView.setPadding((int) padding.left, (int) padding.top,
                    (int) padding.right, (int) padding.bottom);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MAP_INVALIDATE;
            break;
        // case AkylasCartoModule.PROPERTY_MAPSTYLE:
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

    // boolean mCameraRegionUpdate = false;
    boolean mCameraAnimate = false;
    // LatLngBounds mCameraRegion = null;
    // LatLng mCameraCenter = null;
    // CameraPosition.Builder mCameraBuilder = null;

    // private CameraPosition.Builder getCameraBuilder() {
    // if (mCameraBuilder == null) {
    // mProcessUpdateFlags |= TIFLAG_NEEDS_CAMERA;
    // mCameraBuilder = new CameraPosition.Builder();
    // if (currentCameraPosition == null) {
    // currentCameraPosition = map.getCameraPosition();
    // }
    // if (currentCameraPosition != null) {
    // mCameraBuilder.target(currentCameraPosition.target)
    // .zoom(currentCameraPosition.zoom)
    // .tilt(currentCameraPosition.tilt)
    // .bearing(currentCameraPosition.bearing);
    // }
    // }
    // return mCameraBuilder;
    // }

    private void handleCameraUpdate() {
        if (preLayout || cameraBuilderMap == null)
            return;

        boolean animate = mCameraAnimate && shouldAnimate();
        float animationDuration = animate ? (cameraAnimationDuration / 1000.0f)
                : 0;

        if (cameraBuilderMap.containsKey(TiC.PROPERTY_REGION)) {
            updateRegion(cameraBuilderMap.get(TiC.PROPERTY_REGION), animate);
        }

        if (cameraBuilderMap
                .containsKey(AkylasCartoModule.PROPERTY_CENTER_COORDINATE)
                && cameraBuilderMap.get(
                        AkylasCartoModule.PROPERTY_CENTER_COORDINATE) != null) {
            updateCenter(
                    cameraBuilderMap
                            .get(AkylasCartoModule.PROPERTY_CENTER_COORDINATE),
                    animate);
        }

        if (cameraBuilderMap.containsKey(AkylasCartoModule.PROPERTY_ZOOM)
                && cameraBuilderMap
                        .get(AkylasCartoModule.PROPERTY_ZOOM) != null) {
            targetZoom = TiConvert.toFloat(cameraBuilderMap,
                    AkylasCartoModule.PROPERTY_ZOOM, 0);
            mapView.setZoom(targetZoom, animationDuration);
        }

        if (cameraBuilderMap.containsKey(AkylasCartoModule.PROPERTY_TILT)) {
            mapView.setTilt(
                    TiConvert.toFloat(cameraBuilderMap,
                            AkylasCartoModule.PROPERTY_TILT, 0),
                    animationDuration);
        }

        if (cameraBuilderMap.containsKey(TiC.PROPERTY_BEARING)) {
            mapView.setMapRotation(-TiConvert.toFloat(cameraBuilderMap,
                    TiC.PROPERTY_BEARING, 0), animationDuration);
        }

        mCameraAnimate = false;
        cameraBuilderMap = null;
    }

    @Override
    protected void didProcessProperties() {
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_CAMERA) != 0) {
            handleCameraUpdate();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_CAMERA;
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MAP_INVALIDATE;
        }
        super.didProcessProperties();
    }

    // protected void moveCamera(CameraUpdate camUpdate, boolean anim) {
    // Log.d(TAG, "moveCamera " + anim);
    // if (map == null)
    // return;
    // if (anim) {
    // map.animateCamera(camUpdate, cameraAnimationDuration, null);
    // } else {
    // map.moveCamera(camUpdate);
    // }
    // }

    // public GoogleMap getMap() {
    // return map;
    // }

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
                proxy.getProperty(AkylasCartoModule.PROPERTY_MAXZOOM), -1);
        // if (defaultValue != -1) {
        return defaultValue;
        // }
        // return proxy.getValueInUIThread(new Command<Float>() {
        // @Override
        // public Float execute() {
        // return map.getMaxZoomLevel();
        // }
        // }, defaultValue);
    }

    public float getMinZoomLevel() {
        float defaultValue = TiConvert.toFloat(
                proxy.getProperty(AkylasCartoModule.PROPERTY_MINZOOM), -1);
        // if (defaultValue != -1) {
        return defaultValue;
        // }
        // return proxy.getValueInUIThread(new Command<Float>() {
        // @Override
        // public Float execute() {
        // return map.getMinZoomLevel();
        // }
        // }, defaultValue);
    }

    public static float metersToEquatorPixels(MapView map,
            final MapPos location, final float zoom, final float meters) {
        // CameraPosition position = map.getCameraPosition();
        MapPos center = location;
        if (center == null) {
            center = map.getFocusPos();
            if (center == null) {
                center = new MapPos(0, 0);
            }
        }
        float zoomLevel = zoom;
        if (zoomLevel < 0) {
            zoomLevel = map.getZoom();
        }
        double latRadians = center.getX() * Math.PI / 180;
        double metersPerPixel = 40075016.68 / (256 * Math.pow(2, zoomLevel));
        return (float) (meters / Math.cos(latRadians) / metersPerPixel);
    }

    @Override
    public float getMetersPerPixel(final float zoomToCheck,
            final Object position) {
        if (mapView == null) {
            return 0.0f;
        }
        final MapPos pos = (MapPos) AkylasCartoModule
                .latlongFromObject(position);

        FutureTask<Float> futureResult = new FutureTask<Float>(
                new Callable<Float>() {
                    @Override
                    public Float call() throws Exception {
                        return 1.0f / metersToEquatorPixels(mapView, pos,
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
        // if (currentCameraPosition == null) {
        // return TiConvert.toFloat(
        // proxy.getProperty(AkylasCartoModule.PROPERTY_ZOOM), 0);
        // }
        // return currentCameraPosition.zoom;
        return mapView.getZoom();
    }

    public BaseAnnotationProxy getProxyByMarker(VectorElement m) {
        // Object tag = m.getTag();
        // if (tag instanceof AnnotationProxy) {
        // return (AnnotationProxy) tag;
        // }
        if (m != null && handledMarkers != null) {
            return handledMarkers.get(m);
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
        // CameraUpdate camUpdate = CameraUpdateFactory.zoomBy(level);
        // moveCamera(camUpdate, animated);
        mapView.setZoom(level, animated ? cameraAnimationDuration / 1000 : 0);
    }

    protected KrollDict dictFromPoint(MapPos pos) {
        KrollDict d = TiViewHelper.dictFromMotionEvent(getTouchView(),
                lastDownEvent);
        setDictData(d, pos);
        return d;
    }

    protected void setDictData(KrollDict d, MapPos pos) {
        d.put(TiC.PROPERTY_LATITUDE, pos.getY());
        d.put(TiC.PROPERTY_LONGITUDE, pos.getX());
        // d.put(TiC.PROPERTY_ALTITUDE, point.getZ());
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasCartoModule.PROPERTY_ZOOM, getZoomLevel());
        d.put(AkylasCartoModule.PROPERTY_MAP, proxy);
    }

    // public void fireClickEvent(BaseAnnotationProxy proxy, final String
    // source) {
    // fireEventOnAnnotProxy(TiC.EVENT_CLICK, element, proxy, source);
    // }

    public boolean handleMarkerClick(VectorElement element) {
        if (element == null) {
            return false;
        }
        Object theObj = getObjectForElement(element);
        if (theObj == null || (theObj instanceof BaseAnnotationProxy
                && !((BaseAnnotationProxy) theObj).touchable)) {
            // trick for untouchable as googlemap does not support it
            // onMapClick((MapPos) annoProxy.getPosition());
            return true;
        }

        if (theObj instanceof BaseAnnotationProxy) {
            fireEventOnAnnotProxy(TiC.EVENT_CLICK, (BaseAnnotationProxy) theObj,
                    element, AkylasCartoModule.PROPERTY_PIN);
        } else {
            fireEventOnAnnotData(TiC.EVENT_CLICK, (HashMap) theObj, element,
                    AkylasCartoModule.PROPERTY_PIN);
        }

        // make sure we fire the click first
        // fireClickEvent(annoProxy, AkylasCartoModule.PROPERTY_PIN);
        setSelectedAnnotation(element);

        // Returning false here will enable native behavior, which shows the
        // info window.
        return !canShowInfoWindow(theObj);
    }

    // private AnnotationProxy getProxyByMarker(VectorElement m) {
    // if (m instanceof AkMarker) {
    // return ((AkMarker) m).getProxy();
    // }
    // return null;
    // }
    // @Override
    // public void onMapLongClick(MapPos point) {
    // if (!hasListeners(TiC.EVENT_LONGPRESS, false))
    // return;
    // fireEvent(TiC.EVENT_LONGPRESS, dictFromPoint(point), false, false);
    // }

    // @Override
    // public void onMarkerDrag(Marker marker) {
    // Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    //
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasCartoModule.ANNOTATION_DRAG_STATE_DRAGGING);
    // }
    // }

    // @Override
    // public void onMarkerDragEnd(Marker marker) {
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    // MapPos position = annoProxy.getPosition();
    // annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.getX());
    // annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.getY());
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasCartoModule.ANNOTATION_DRAG_STATE_END);
    // }
    // }

    // @Override
    // public void onMarkerDragStart(Marker marker) {
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // if (annoProxy != null) {
    // firePinChangeDragStateEvent(annoProxy,
    // AkylasCartoModule.ANNOTATION_DRAG_STATE_START);
    // }
    // }

    // private void handleInfoWindowClick(BaseAnnotationProxy annoProxy) {
    // if (annoProxy != null) {
    // String clicksource = annoProxy.getMapInfoWindow().getClicksource();
    // // The clicksource is null means the click event is not inside
    // // "leftPane", "title", "subtible"
    // // or "rightPane". In this case, use "infoWindow" as the
    // // clicksource.
    // if (clicksource == null) {
    // clicksource = AkylasCartoModule.PROPERTY_INFO_WINDOW;
    // }
    // fireClickEvent(annoProxy, clicksource);
    // }
    // }

    // @Override
    // public void onInfoWindowClick(Point marker) {
    // AnnotationProxy annoProxy = getProxyByMarker(marker);
    // handleInfoWindowClick(annoProxy);
    // }

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

    // @Override
    // public View getInfoWindow(Poin marker) {
    //
    // return null;
    // }

    // @Override
    // public void onCameraMove() {
    //
    // currentCameraPosition = map.getCameraPosition();
    // mpp = 156543.03392
    // * Math.cos(
    // currentCameraPosition.target.latitude * Math.PI / 180)
    // / Math.pow(2, currentCameraPosition.zoom);
    // targetZoom = -1;
    // if (userAction) {
    // setShouldFollowUserLocation(false);
    // userAction = false;
    // }
    // if (preLayout) {
    //
    // // moveCamera will trigger another callback, so we do this to make
    // // sure
    // // we don't fire event when region is set initially
    // preLayout = false;
    // handleCameraUpdate();
    // } else if (map != null) {
    //
    // if (proxy != null
    // && proxy.hasListeners(TiC.EVENT_REGION_CHANGED, false)) {
    // LatLngBounds bounds = map.getProjection()
    // .getVisibleRegion().latLngBounds;
    // KrollDict result = new KrollDict();
    // result.put(TiC.PROPERTY_REGION,
    // AkylasCartoModule.getFactory().regionToDict(bounds));
    // result.put(AkylasCartoModule.PROPERTY_ZOOM,
    // currentCameraPosition.zoom);
    // result.put("mpp", mpp);
    // result.put("mapdistance", mpp * nativeView.getWidth());
    // result.put("bearing", currentCameraPosition.bearing);
    // result.put("tilt", currentCameraPosition.tilt);
    // result.put(AkylasCartoModule.PROPERTY_USER_ACTION, userAction);
    // result.put("idle", !pointerDown);
    // proxy.fireEvent(TiC.EVENT_REGION_CHANGED, result, false, false);
    // }
    // }
    // }

    // @Override
    // public void onCameraIdle() {
    // onCameraMove();
    // }
    //
    // @Override
    // public void onCameraMoveCanceled() {
    // onCameraMove();
    // }
    //
    // @Override
    // public void onCameraMoveStarted(int reason) {
    // onCameraMove();
    // }

    // @Override
    // public void onPoiClick(PointOfInterest poi) {
    // if (proxy.hasListeners(AkylasCartoModule.EVENT_POI, false)) {
    // KrollDict d = dictFromPoint(poi.latLng);
    // d.put("name", poi.name);
    // d.put("placeId", poi.placeId);
    // proxy.fireEvent(AkylasCartoModule.EVENT_POI, d, false, false);
    // }
    // }
    //
    // @Override
    // public boolean onMyLocationButtonClick() {
    // if (proxy.hasListeners(AkylasCartoModule.EVENT_LOCATION_BUTTON,
    // false)) {
    // proxy.fireEvent(AkylasCartoModule.EVENT_LOCATION_BUTTON, null,
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
            // AnnotationProxy annot = (AnnotationProxy) selectedAnnotation;
            // AkylasMapInfoView infoWindow = annot.getMapInfoWindow();
            // Marker marker = getAnnotMarker(annot);
            if (infoWindow != null
            // && marker.isInfoWindowShown()
            ) {
                // Get a marker position on the screen
                // MapPos pos = ((Marker)selectedElement).get();
                // ScreenPos markerPoint = mapView.mapToScreen(pos);
                // CartoMarker gMarker = getCartoMarker(annot);
                // if (infoWindow.dispatchMapTouchEvent(event,
                // new Point((int) markerPoint.getX(),
                // (int) markerPoint.getY()),
                // selectedElement.set())) {
                // return true;
                // }
            }
        }
        // onTouch(mapView, event);
        return false;
    }

    public MapView getMapView() {
        return mapView;
    }

    // private CartoMarker getCartoMarker(AnnotationProxy proxy) {
    // return (CartoMarker) proxy.getMarker();
    // }

    private Marker getAnnotMarker(BaseAnnotationProxy proxy) {
        CartoMarker timarker = (CartoMarker) proxy.getMarker();
        if (timarker != null) {
            return timarker.getMarker();
        }
        return null;
    }

    public void snapshot() {
        // map.snapshot(new GoogleMap.SnapshotReadyCallback() {
        //
        // @Override
        // public void onSnapshotReady(Bitmap snapshot) {
        // if (proxy.hasListeners(
        // AkylasCartoModule.EVENT_ON_SNAPSHOT_READY, false)) {
        // TiBlob sblob = TiBlob.blobFromObject(snapshot);
        // KrollDict data = new KrollDict();
        // data.put("snapshot", sblob);
        // data.put("source", proxy);
        // proxy.fireEvent(AkylasCartoModule.EVENT_ON_SNAPSHOT_READY,
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
        // if (!getUserLocationEnabled()) {
        // return null;
        // }
        // if (TiApplication.isUIThread()) {
        // return AkylasCartoModule.locationToDict(map.getMyLocation());
        // } else {
        // return AkylasCartoModule.locationToDict(
        // (Location) TiMessenger.sendBlockingMainMessage(
        // mainHandler.obtainMessage(MSG_GET_MYLOCATION)));
        // }
        return null;
    }

    @Override
    public boolean getUserLocationEnabled() {
        // if (map == null) {
        return false;
        // }
        // if (TiApplication.isUIThread()) {
        // return map.isMyLocationEnabled();
        // } else {
        // return (Boolean) TiMessenger.sendBlockingMainMessage(
        // mainHandler.obtainMessage(MSG_GET_MYLOCATION_ENABLED));
        // }
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
        MapPos pos = (MapPos) AkylasCartoModule.latlongFromObject(dict);
        if (pos != null) {
            mapView.setFocusPos(baseProjection.fromWgs84(pos),
                    animated ? (cameraAnimationDuration / 1000.0f) : 0.0f);
        }
    }

    @Override
    public void updateRegion(Object dict, boolean animated) {
        MapBounds bounds = AkylasCartoModule.regionFromObject(dict);
        if (bounds != null) {
            mapView.moveToFitBounds(
                    new MapBounds(baseProjection.fromWgs84(bounds.getMin()),
                            baseProjection.fromWgs84(bounds.getMax())),
                    new ScreenBounds(new ScreenPos(0, 0),
                            new ScreenPos(nativeView.getMeasuredWidth(),
                                    nativeView.getMeasuredHeight())),
                    false,
                    animated ? (cameraAnimationDuration / 1000.0f) : 0.0f);

        }
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
        // float currentZoom = (currentCameraPosition != null)
        // ? currentCameraPosition.zoom : 0;
        // float targetZoom = (float) (Math.ceil(currentZoom) + 1);
        // float factor = (float) Math.pow(2, targetZoom - currentZoom);
        //
        // if (factor > 2.25) {
        // targetZoom = (float) Math.ceil(currentZoom);
        // }
        mapView.zoom(1, animate ? cameraAnimationDuration / 1000 : 0);
    }

    @Override
    public void zoomIn(Object about, boolean userAction) {
        this.userAction = true;
        zoomIn();
    }

    @Override
    public void zoomOut() {
        // float currentZoom = (currentCameraPosition != null)
        // ? currentCameraPosition.zoom : 0;
        // float targetZoom = (float) (Math.floor(currentZoom));
        // float factor = (float) Math.pow(2, targetZoom - currentZoom);
        //
        // if (factor > 0.75) {
        // targetZoom = (float) (Math.floor(currentZoom) - 1);
        // }
        // changeZoomLevel(targetZoom, animate);
        mapView.zoom(-1, animate ? cameraAnimationDuration / 1000 : 0);

    }

    @Override
    public void zoomOut(Object about, boolean userAction) {
        this.userAction = true;
        zoomOut();
    }

    // private Projection getProjection() {
    // if (map == null) {
    // return null;
    // }
    // return proxy.getInUiThread(new Command<Projection>() {
    //
    // @Override
    // public Projection execute() {
    // return map.getProjection();
    // }
    // });
    // }

    @Override
    public KrollDict getRegionDict() {
        MapBounds bounds = getScreenRegion();
        return AkylasCartoModule.getFactory().regionToDict(bounds);
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
            if (element instanceof Marker) {
                ((Marker) element).setStyle(AnnotationProxy.getMarkerStyle(this,
                        baseProjection, (HashMap) obj, false));
            }
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
                    // ((BaseAnnotationProxy<MapPos>) obj).infoWindowDidClose();
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
        if (element instanceof AnnotationProxy) {
            obj = element;
            selectedElement = getAnnotMarker((AnnotationProxy) obj);
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
            if (selectedElement instanceof Marker) {
                ((Marker) selectedElement)
                        .setStyle(AnnotationProxy.getMarkerStyle(this,
                                baseProjection, (HashMap) obj, true));
            }
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
            if (selectedElement instanceof Marker) {
                ((Marker) selectedElement)
                        .setStyle(AnnotationProxy.getMarkerStyle(this,
                                baseProjection, (HashMap) obj, true));
            }
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
            trackedPosition = ((VectorElement) selectedElement).getGeometry()
                    .getCenterPos();
            final float size = ((CartoMarker) proxy.getMarker()).getIconSize();
            // Geometry gem =((VectorElement) element).getGeometry()
            // final float iconWidth = ((VectorElement) element).getGeometry()
            // .getIconImageWidth();
            // final float iconHeight = ((VectorElementMarker)
            // proxy.getMarker())
            // .getIconImageHeight();
            float deltaX = size * (proxy.calloutAnchor.x - proxy.anchor.x);
            float deltaY = size * (proxy.calloutAnchor.y - proxy.anchor.y);
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
                addAnnotationHashMapToMap((HashMap) data);
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
                handleRemoveSingleAnnotation(annotProxy, annotsSource);
            }
        }
    }

    public void handleRemoveSingleAnnotation(BaseAnnotationProxy proxy,
            LocalVectorDataSource source) {
        Marker marker = ((CartoMarker) proxy.getMarker()).getMarker();
        if (handledMarkers != null) {
            handledMarkers.remove(marker);
        }
        if (source != null && marker != null) {
            source.remove(marker);
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

            proxy.setMapView(CartoView.this);
            proxy.setActivity(activity);
            proxy.setParentForBubbling(CartoView.this.proxy);

            Line line = proxy.getOrCreateLine(baseProjection);
            if (line != null) {
                if (handledMarkers == null) {
                    handledMarkers = new HashMap<VectorElement, BaseAnnotationProxy>();
                }
                handledMarkers.put(line, proxy);
                annotsSource.add(line);
            }

            // if (handledPolylines == null) {
            // handledPolylines = new WeakHashMap<Polyline, RouteProxy>();
            // }
            // handledPolylines.put(proxy.getPolyline(), proxy);
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
            Line line = proxy.getLine();

            if (line != null) {
                if (handledMarkers == null) {
                    handledMarkers.remove(line);
                }
                if (annotsSource != null) {
                    annotsSource.remove(line);
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

        // final Activity activity = proxy.getActivity();
        // for (GroundOverlayProxy proxy : (ArrayList<GroundOverlayProxy>)
        // value) {
        // proxy.setGroundOverlay(map.addGroundOverlay(
        // proxy.getAndSetOptions(currentCameraPosition)));
        // proxy.setActivity(activity);
        // proxy.setMapView(CartoView.this);
        // proxy.setParentForBubbling(CartoView.this.proxy);
        // addedGroundOverlays.add(proxy);
        // }
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
        processingTileLayers = true;
        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            proxy.setMapView(CartoView.this);
            proxy.setParentForBubbling(CartoView.this.proxy);
            proxy.setActivity(activity);

            VectorLayer layer = proxy.getLayer();
            layer.setVectorElementEventListener(vectorEventListener);
            if (handledLayers == null) {
                handledLayers = new HashMap<Layer, Object>();
            }
            handledLayers.put(layer, proxy);
            addClusterLayer(layer);
        }
        processingTileLayers = false;
        resortLayers();
    }

    List<Layer> tileLayers = new ArrayList();
    List<Layer> clusterLayers = new ArrayList();
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
    private final Comparator clusterLayerComparator = new Comparator<Layer>() {
        @Override
        public int compare(Layer lhs, Layer rhs) {
            final float zIndexL = ((BaseAnnotationProxy) getProxyByLayer(lhs))
                    .getZIndex();
            final float zIndexR = ((BaseAnnotationProxy) getProxyByLayer(rhs))
                    .getZIndex();
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for
            // descending
            return zIndexL > zIndexR ? 1 : (zIndexL < zIndexR) ? -1 : 0;
        }
    };

    private void addTileLayer(Layer layer) {
        tileLayers.add(layer);
        tileLayers.sort(tileLayerComparator);
        resortLayers();
    }

    private void addClusterLayer(Layer layer) {

        clusterLayers.add(layer);
        clusterLayers.sort(clusterLayerComparator);
        resortLayers();
    }

    private void removeTileLayer(Layer layer) {
        if (handledLayers != null) {
            handledLayers.remove(layer);
        }
        tileLayers.remove(layer);
        resortLayers();
    }

    private void removeClusterLayer(Layer layer) {
        if (handledLayers != null) {
            handledLayers.remove(layer);
        }
        clusterLayers.remove(layer);
        resortLayers();
    }

    private boolean processingTileLayers = false;

    public void resortLayers() {
        if (processingTileLayers) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            proxy.runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    resortLayers();
                }
            }, false);
            return;
        }
        mapView.getLayers().clear();
        if (baseLayer != null) {
            mapView.getLayers().add(baseLayer);
        }
        for (Layer layer : tileLayers) {
            mapView.getLayers().add(layer);
        }
        for (Layer layer : clusterLayers) {
            mapView.getLayers().add(layer);
        }
        mapView.getLayers().add(annotsLayer);
        if (debugLayer != null) {
            mapView.getLayers().add(debugLayer);
        }
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

        processingTileLayers = true;
        for (ClusterProxy proxy : (ArrayList<ClusterProxy>) value) {
            VectorLayer layer = proxy.getLayer();
            layer.setVectorElementEventListener(null);
            removeClusterLayer(layer);
            proxy.wasRemoved();
        }
        processingTileLayers = false;
        resortLayers();
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
            proxy.setMapView(CartoView.this);
            proxy.setParentForBubbling(CartoView.this.proxy);
            proxy.setActivity(activity);
            if (proxy.getZIndex() == -1) {
                proxy.setZIndex(index);
            }
            Layer layer = proxy.getOrCreateLayer();
            if (handledLayers == null) {
                handledLayers = new HashMap<Layer, Object>();
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
            // layer.setVectorElementEventListener(null);
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
        proxy.setMapView(CartoView.this);
        if (proxy.getParentForBubbling() == null) {
            proxy.setParentForBubbling(CartoView.this.proxy);
        }
        CartoMarker gMarker = new CartoMarker((AnnotationProxy) proxy);
        proxy.setMarker(gMarker);
    }

    public Marker addAnnotationToSource(AnnotationProxy proxy,
            LocalVectorDataSource source) {
        // proxy.setActivity(activity);
        prepareAnnotation(proxy);
        Marker marker = proxy.createMarker(baseProjection);
        if (marker != null) {
            source.add(marker);
            // we need to set the position again because addMarker can be long
            // and
            // position might already have changed
            // marker.setPosition((LatLng) proxy.getPosition());
            ((CartoMarker) proxy.getMarker()).setMarker(marker);
            // googlemarker.setTag(proxy);
            if (handledMarkers == null) {
                handledMarkers = new HashMap<VectorElement, BaseAnnotationProxy>();
            }
            handledMarkers.put(marker, proxy);
        }

        // timarkers.add(proxy.getMarker());
        return marker;
    }

    public Marker addAnnotationToMap(AnnotationProxy proxy) {
        Marker marker = addAnnotationToSource(proxy, annotsSource);
        if (marker == selectedElement) {
            handleSelectElement(marker);
        }
        return marker;
    }

    public Marker addAnnotationHashMapToMap(HashMap data) {
        Marker marker = AnnotationProxy.createMarker(this, baseProjection, data,
                false);
        if (marker != null) {
            annotsSource.add(marker);
        }
        return marker;
    }

    // @Override
    // public void onMyLocationChange(Location location) {
    // if (shouldFollowUserLocation
    // && mUserTrackingMode != AkylasGooglemapModule.TrackingMode.NONE) {
    // CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
    // cameraBuilder.target(new LatLng(location.getLatitude(),
    // location.getLongitude()));
    // if (mUserTrackingMode ==
    // AkylasGooglemapModule.TrackingMode.FOLLOW_BEARING
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
    // d.put(AkylasCartoModule.PROPERTY_MAP, proxy);
    // proxy.fireEvent(TiC.EVENT_LOCATION, d, false, false);
    // }
    // }

    void updateCamera(final HashMap props) {
        if (props == null)
            return;
        if (mapView == null) {
            props.remove(TiC.PROPERTY_ANIMATE);
            if (!props.containsKey(TiC.PROPERTY_REGION) && props.containsKey(
                    AkylasCartoModule.PROPERTY_CENTER_COORDINATE)) {
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
            final Marker marker, final MapPos toPosition,
            final long animationDuration) {
        boolean animated = animationDuration > 0;
        if (!animated || !shouldAnimate() || annotProxy.getPosition() == null
                || toPosition == null) {
            marker.setPos(toPosition);
            return;
        }
        final MapPos startLatLng = (MapPos) annotProxy.getPosition();
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

    // private ClusterManager _clusterManager = null;
    //
    // public ClusterManager getClusterManager() {
    // if (_clusterManager == null && map != null) {
    // _clusterManager = new ClusterManager<AnnotationProxy>(getContext(),
    // map);
    // _clusterManager.setRenderer(new ClusterRenderer(getContext(), map,
    // _clusterManager, this));
    // _clusterManager.setOnClusterClickListener(this);
    // _clusterManager.setOnClusterItemClickListener(this);
    // _clusterManager.setOnCameraMoveListener(this);
    // _clusterManager.setOnMarkerClickListener(this);
    // map.setOnCameraMoveListener(_clusterManager);
    // map.setOnMarkerClickListener(_clusterManager);
    //
    // }
    // return _clusterManager;
    // }
    //
    // @Override
    // public void onClusterInfoWindowClick(Cluster cluster) {
    // if (cluster instanceof AkylasCluster) {
    // handleInfoWindowClick(((AkylasCluster) cluster).proxy);
    // }
    // }

    // @Override
    // public void onClusterItemInfoWindowClick(ClusterItem item) {
    // if (item instanceof AnnotationProxy) {
    // handleInfoWindowClick((AnnotationProxy) item);
    // }
    // }
    //
    // @Override
    // public boolean onClusterItemClick(ClusterItem item) {
    // if (item instanceof AnnotationProxy) {
    // handleMarkerClick((AnnotationProxy) item);
    // }
    // return true;
    // }

    // @Override
    // public void onPolygonClick(Polygon arg0) {
    // }
    //
    // @Override
    // public void onPolylineClick(Polyline polyline) {
    // if (_canSelectRoute) {
    // // if (handledPolylines != null) {
    // // BaseRouteProxy route = handledPolylines.get(polyline);
    // Object route = polyline.getTag();
    // if (route instanceof BaseRouteProxy) {
    // handleMarkerClick((BaseAnnotationProxy) route);
    // }
    // // }
    // } else if (lastDownEvent != null) {
    // Point p = new Point((int) lastDownEvent.getX(),
    // (int) lastDownEvent.getY());
    // onMapClick(getProjection().fromScreenLocation(p));
    // }
    //
    // }

    // @Override
    // public boolean onClusterClick(Cluster cluster) {
    // if (cluster instanceof AkylasCluster) {
    // handleMarkerClick(((AkylasCluster) cluster).proxy);
    // }
    // return true;
    // }

    @Override
    public Object coordinateForPoints(Object arg) {
        if (arg instanceof Object[]) {
            // Projection proj = getProjection();
            List<Object> result = new ArrayList<>();
            Object[] array = (Object[]) arg;
            TiPoint pt;
            MapPos res;
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
    public void onDestroy(Activity activity) {
        // if (mapView != null) {
        // mapView.o();
        // }
    }

    @Override
    public void onPause(Activity activity) {
        // if (mapView != null) {
        // mapView.onPause();
        // }
    }

    @Override
    public void onResume(Activity activity) {
        // if (mapView != null) {
        // mapView.onResume();
        // }
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
        return AkylasCartoModule.latLongToDict(getScreenPos(p.x, p.y));
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
        if (element instanceof VectorElement) {
            return element;
        } else if (element instanceof AnnotationProxy) {
            return ((AnnotationProxy) element).getVectorElement();
        } else if (element instanceof RouteProxy) {
            return ((RouteProxy) element).getVectorElement();
        }
        return null;
    }

    @Override
    protected boolean isElementSelectable(Object element) {
        if (element instanceof BaseAnnotationProxy) {
            return ((BaseAnnotationProxy<MapPos>) element).getSelectable();
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
        return getProxyByMarker((VectorElement) element);
    }

    // public void removeElement(VectorElement m) {
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
