package akylas.map;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.views.MapView;

import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
        AkylasMapModule.PROPERTY_POINTS, TiC.PROPERTY_COLOR, TiC.PROPERTY_WIDTH })
public class RouteProxy extends ReusableProxy {
    private ArrayList<LatLng> mPoints;
    private ArrayList<com.google.android.gms.maps.model.LatLng> mGooglePoints;

    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    private static final int MSG_SET_POINTS = MSG_FIRST_ID + 400;
    private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
    private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
    private static final int MSG_SET_ZINDEX = MSG_FIRST_ID + 403;
    private static final int MSG_REFRESH_MAP = MSG_FIRST_ID + 404;
    private BoundingBox mBoundingBox = TileLayerConstants.WORLD_BOUNDING_BOX;

    private MapView mMapView;
    private PathOverlay mPath;

    private PolylineOptions options = null;
    private Polyline polyline;
    private int zIndex = 10;

    public RouteProxy() {
        super();
        mPoints = new ArrayList<LatLng>();
        mGooglePoints = new ArrayList<com.google.android.gms.maps.model.LatLng>();
    }

    public RouteProxy(TiContext tiContext) {
        this();
    }

    private void updateBoundingBox() {
        mBoundingBox = BoundingBox.fromLatLngs(this.mPoints);
    }

    @Override
    public boolean handleMessage(Message msg) {
        AsyncResult result = null;
        switch (msg.what) {

        case MSG_SET_POINTS: {
            result = (AsyncResult) msg.obj;
            polyline.setPoints(mGooglePoints);
            result.setResult(null);
            return true;
        }

        case MSG_SET_COLOR: {
            result = (AsyncResult) msg.obj;
            polyline.setColor((Integer) result.getArg());
            result.setResult(null);
            return true;
        }

        case MSG_SET_WIDTH: {
            result = (AsyncResult) msg.obj;
            polyline.setWidth(getPathPaint().getStrokeWidth());
            result.setResult(null);
            return true;
        }

        case MSG_SET_ZINDEX: {
            result = (AsyncResult) msg.obj;
            polyline.setZIndex(zIndex);
            result.setResult(null);
            return true;
        }

        case MSG_REFRESH_MAP: {
            if (mMapView != null) {
                mMapView.invalidate();
            }
            return true;
        }
        default: {
            return super.handleMessage(msg);
        }
        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasMapModule.PROPERTY_POINTS:
            replacePoints(newValue);
            break;
        case TiC.PROPERTY_WIDTH:
            getPathPaint().setStrokeWidth(
                    TiUIHelper.getInPixels(newValue, getPathPaint()
                            .getStrokeWidth()));
            if (polyline != null) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_WIDTH));
            }
            break;
        case TiC.PROPERTY_COLOR:
            getPathPaint().setColor(TiConvert.toColor(newValue));
            if (polyline != null) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_COLOR));
            }
            break;
        case TiC.PROPERTY_ZINDEX:
            zIndex = TiConvert.toInt(newValue);
            if (polyline != null) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_ZINDEX));
            }
            break;
        case TiC.PROPERTY_VISIBLE:
            if (polyline != null) {
                polyline.setVisible(TiConvert.toBoolean(newValue));
            }
            break;
        case AkylasMapModule.PROPERTY_LINE_JOIN:
            getPathPaint().setStrokeJoin(
                    joinFromString(TiConvert.toString(newValue)));
            break;
        case AkylasMapModule.PROPERTY_LINE_CAP:
            getPathPaint().setStrokeCap(
                    capFromString(TiConvert.toString(newValue)));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    public void setMapView(final MapView aMapView) {
        mMapView = aMapView;
    }

    public PathOverlay getPath() {
        if (mPath == null) {
            mPath = new PathOverlay();
            mPath.getPaint().setStrokeJoin(Join.ROUND);
            mPath.getPaint().setStrokeCap(Cap.ROUND);
            mPath.addPoints(mPoints);
        }
        return mPath;
    }

    public Paint getPathPaint() {
        return getPath().getPaint();
    }

    public void addLocations(Object locs) {

        if (locs instanceof Object[]) {
            Object[] pointsArray = (Object[]) locs;
            for (int i = 0; i < pointsArray.length; i++) {
                Object loc = pointsArray[i];
                LatLng point = AkylasMapModule.latlongFromObject(loc);
                if (point != null) {
                    mPoints.add(point);
                    mGooglePoints.add(AkylasMapModule.mapBoxToGoogle(point));
                }
            }
        } else {
            LatLng point = AkylasMapModule.latlongFromObject(locs);
            if (point != null) {
                mPoints.add(point);
                mGooglePoints.add(AkylasMapModule.mapBoxToGoogle(point));
            }
        }
    }

    public LatLng addLocation(Object loc) {
        LatLng point = AkylasMapModule.latlongFromObject(loc);
        if (point != null) {
            mPoints.add(point);
            mGooglePoints.add(AkylasMapModule.mapBoxToGoogle(point));
        }
        return point;
    }

    public void replacePoints(Object points) {

        mPoints.clear();
        mGooglePoints.clear();
        addLocations(points);
        updateBoundingBox();

        if (polyline != null) {
            if (!TiApplication.isUIThread()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        polyline.setPoints(mGooglePoints);
                    }
                });
                return;
            }
            polyline.setPoints(mGooglePoints);
        }
        updatePolyline();
        if (mPath != null) {
            mPath.setPoints(mPoints);
            mapboxInvalidate();
        }
    }
    
    private void updatePolyline() {
        if (polyline != null) {
            if (!TiApplication.isUIThread()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        polyline.setPoints(mGooglePoints);
                    }
                });
                return;
            }
            polyline.setPoints(mGooglePoints);
        }
    }

    private void mapboxInvalidate() {
        if (mPath == null) {
            return;
        }
        if (TiApplication.isUIThread()) {
            if (mMapView != null) {
                mMapView.invalidate();
            }
        } else {
            getRuntimeHandler().obtainMessage(MSG_REFRESH_MAP);
        }

    }

    // public PolylineOptions getOptions() {
    // return options;
    // }
    //
    // public void setRoute(Polyline r) {
    // route = r;
    // }
    //
    // public Polyline getRoute() {
    // return route;
    // }

    private Join joinFromString(final String value) {
        if (value != null) {
            if (value == "miter") {
                return Join.MITER;
            } else if (value == "bevel") {
                return Join.BEVEL;
            }
        }
        return Join.ROUND;
    }

    private Cap capFromString(final String value) {
        if (value != null) {
            if (value == "square") {
                return Cap.SQUARE;
            } else if (value == "round") {
                return Cap.ROUND;
            }
        }
        return Cap.BUTT;
    }

    @Kroll.method
    @Kroll.getProperty
    public KrollDict getRegion() {

        return AkylasMapModule.regionToDict(mBoundingBox);
    }

    @Kroll.method
    public void addPoint(Object point) {
        LatLng added = addLocation(point);
        if (added != null) {
            updateBoundingBox();
            if (mPath != null) {
                mPath.addPoint(added);
                mapboxInvalidate();
            }
            updatePolyline();
        }
    }

    public PolylineOptions getAndSetOptions(final CameraPosition position) {
        options = new PolylineOptions();
        Paint paint = getPathPaint();
        return options.width(paint.getStrokeWidth()).addAll(mGooglePoints)
                .color(paint.getColor()).zIndex(zIndex);
    }

    public void setPolyline(Polyline r) {
        polyline = r;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void removePolyline() {
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }
    }

    private float widthThickness = 1.2f;

    public void onMapCameraChange(final GoogleMap map,
            final CameraPosition position) {
        // if (polyline != null) {
        // if (widthThickness != 1.0f) {
        // float newWidth = getPathPaint().getStrokeWidth() * widthThickness;
        // polyline.setWidth(newWidth);
        // }
        // }
    }
}
