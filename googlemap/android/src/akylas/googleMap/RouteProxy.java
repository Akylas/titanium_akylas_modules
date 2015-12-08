package akylas.googlemap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import akylas.map.common.BaseRouteProxy;
import android.graphics.Color;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class)
public class RouteProxy extends BaseRouteProxy<LatLng, LatLngBounds> {
    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
    private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
    private static final int MSG_SET_ZINDEX = MSG_FIRST_ID + 403;

    private PolylineOptions options = null;
    private Polyline polyline;

    private Cap mStrokeCap = Cap.ROUND;
    private Join mStrokeJoin = Join.ROUND;
    private float mStrokeWidth = 7;

    public RouteProxy() {
        super();
        mBoundingBox = AkylasGooglemapModule.MIN_BOUNDING_BOX;
    }

    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Route";
    }

    @Override
    public boolean handleMessage(Message msg) {
        AsyncResult result = null;
        switch (msg.what) {

//        case MSG_SET_COLOR: {
//            result = (AsyncResult) msg.obj;
//            polyline.setColor(mColor);
//            result.setResult(null);
//            return true;
//        }

        case MSG_SET_WIDTH: {
            result = (AsyncResult) msg.obj;
            polyline.setWidth(mStrokeWidth);
            result.setResult(null);
            return true;
        }

        case MSG_SET_ZINDEX: {
            result = (AsyncResult) msg.obj;
            polyline.setZIndex(zIndex);
            result.setResult(null);
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
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_WIDTH:
            mStrokeWidth = TiUIHelper.getInPixels(newValue, mStrokeWidth);
            if (polyline != null) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_WIDTH));
            }
            break;
        case TiC.PROPERTY_COLOR:
            tintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
            if (polyline != null && (!selected || (selectedTintColor == Color.TRANSPARENT))) {
                polyline.setColor(tintColor);
//                marker.setMarkerColor(tintColor);
            }
            break;
        case TiC.PROPERTY_SELECTED_COLOR:
            selectedTintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
            if (polyline != null && selected) {
                polyline.setColor(selectedTintColor);
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
        case AkylasGooglemapModule.PROPERTY_LINE_JOIN:
            mStrokeJoin = joinFromString(TiConvert.toString(newValue));
            break;
        case AkylasGooglemapModule.PROPERTY_LINE_CAP:
            mStrokeCap = capFromString(TiConvert.toString(newValue));
            break;
        default:
            break;
        }
    }
    
    private void updatePolyline() {
        if (polyline != null) {
            runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    polyline.setPoints(mPoints);
                }
            }, true);
        }
    }
    

    @Override
    protected void replacePoints(Object points) {

        super.replacePoints(points);
        updatePolyline();
    }
    
    @Override
    protected void onPointAdded(final LatLng point, final boolean shouldUpdate) {
        super.onPointAdded(point, shouldUpdate);
        if (shouldUpdate) {
            updatePolyline();
        }
    }
    
    public PolylineOptions getAndSetOptions(final CameraPosition position) {
        options = new PolylineOptions();
        return options.width(mStrokeWidth).addAll(mPoints).color(selected?selectedTintColor:tintColor).zIndex(zIndex);
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

//    private float widthThickness = 1.2f;

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
