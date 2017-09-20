package akylas.carto;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapPosVector;
import com.carto.styles.LineStyle;
import com.carto.styles.MarkerStyle;
import com.carto.vectorelements.Line;
import com.carto.vectorelements.Marker;

import akylas.map.common.BaseRouteProxy;
import android.graphics.Color;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class)
public class RouteProxy extends BaseRouteProxy<MapPos, MapBounds> {
    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

//    private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
    private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
    private static final int MSG_SET_ZINDEX = MSG_FIRST_ID + 403;

//    private PolylineOptions options = null;
//    private Polyline polyline;
    
    class AkLine extends Line {
        AnnotationProxy proxy;
        public AkLine(AnnotationProxy proxy, MapPosVector pos, LineStyle style) {
            super(pos, style);
            this.proxy = proxy;
        }
        public AnnotationProxy getProxy() {
            // TODO Auto-generated method stub
            return this.proxy;
        }
        
    }


    public RouteProxy() {
        super();
        mBoundingBox = AkylasCartoModule.MIN_BOUNDING_BOX;
    }

    @Override
    public String getApiName() {
        return "Akylas.Carto.Route";
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
//            polyline.setWidth(mStrokeWidth);
            result.setResult(null);
            return true;
        }

        case MSG_SET_ZINDEX: {
            result = (AsyncResult) msg.obj;
//            polyline.setZIndex(zIndex);
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
        case "lineWidth":
            mStrokeWidth = TiUIHelper.getInPixels(newValue, mStrokeWidth);
//            if (polyline != null && (!selected || mSelectedStrokeWidth < 0)) {
//                polyline.setWidth(mStrokeWidth);
//            }
            break;
        case "selectedLineWidth":
            mSelectedStrokeWidth = TiUIHelper.getInPixels(newValue, mStrokeWidth);
//            if (polyline != null && (selected && mSelectedStrokeWidth >= 0)) {
//                polyline.setWidth(mSelectedStrokeWidth);
//            }
            break;
        case TiC.PROPERTY_COLOR:
            tintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
//            if (polyline != null && (!selected || (selectedTintColor == Color.TRANSPARENT))) {
//                polyline.setColor(tintColor);
//            }
            break;
        case TiC.PROPERTY_SELECTED_COLOR:
            selectedTintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
//            if (polyline != null && selected) {
//                polyline.setColor(selectedTintColor);
//            }
            break;
        case TiC.PROPERTY_ZINDEX:
            zIndex = TiConvert.toInt(newValue);
//            if (polyline != null) {
//                polyline.setZIndex(zIndex);
//            }
            break;
        case TiC.PROPERTY_VISIBLE:
//            if (polyline != null) {
//                polyline.setVisible(TiConvert.toBoolean(newValue));
//            }
            break;
        default:
            break;
        }
    }
    
    
    @Override
    protected void didProcessProperties() {
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_TOUCHABLE) != 0) {
//            if (polyline != null) {
//                polyline.setClickable(touchable);
//            }
        }
        super.didProcessProperties();
    }
    
    private void updatePolyline() {
//        if (polyline != null) {
//            runInUiThread(new CommandNoReturn() {
//                @Override
//                public void execute() {
//                    synchronized(mPoints) {
//                        if (polyline != null) {
//                            polyline.setPoints(mPoints);
//                        }
//                    }
//                }
//            }, true);
//        }
    }
    

    @Override
    protected void replacePoints(Object points) {

        super.replacePoints(points);
        updatePolyline();
    }
    
    @Override
    protected void onPointAdded(final MapPos point, final boolean shouldUpdate) {
        super.onPointAdded(point, shouldUpdate);
        if (shouldUpdate) {
            updatePolyline();
        }
    }
    
//    public PolylineOptions getAndSetOptions(final CameraPosition position) {
//        options = new PolylineOptions();
//        synchronized (mPoints) {
//            return options.width(selected ? mSelectedStrokeWidth : mStrokeWidth)
//                    .addAll(mPoints).clickable(touchable)
//                    .color((selected && selectedTintColor != Color.TRANSPARENT)
//                            ? selectedTintColor : tintColor)
//                    .zIndex(selected ? 10000 : zIndex);
//        }
//    }
//
//    public void setPolyline(Polyline r) {
//        polyline = r;
//        polyline.setTag(this);
//    }
//
//    public Polyline getPolyline() {
//        return polyline;
//    }

    @Override
    public void removeFromMap() {
//        if (polyline != null) {
//            polyline.remove();
//            polyline.setTag(null);
//            polyline = null;
//        }
    }

//    public void onMapCameraChange(final GoogleMap map,
//            final CameraPosition position) {
//        // if (polyline != null) {
//        // if (widthThickness != 1.0f) {
//        // float newWidth = getPathPaint().getStrokeWidth() * widthThickness;
//        // polyline.setWidth(newWidth);
//        // }
//        // }
//    }
    
    @Override
    public void onDeselect() {
        super.onDeselect();
//        if (polyline == null) {
//            return;
//        }
        if (!TiApplication.isUIThread()) {
            runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    onDeselect();
                }
            }, false);
            return;
        }
//        polyline.setZIndex(zIndex);
        if (mSelectedStrokeWidth >= 0 && mSelectedStrokeWidth != mStrokeWidth ) {
//            polyline.setWidth(mStrokeWidth);
        }
    }
    
    @Override
    public void onSelect() {
        super.onSelect();
//        if (polyline == null) {
//            return;
//        }
        if (!TiApplication.isUIThread()) {
            runInUiThread(new CommandNoReturn() {
                @Override
                public void execute() {
                    onSelect();
                }
            }, false);
            return;
        }
        
//        polyline.setZIndex(10000);
//        if (mSelectedStrokeWidth >= 0 && mSelectedStrokeWidth != mStrokeWidth ) {
//            polyline.setWidth(mSelectedStrokeWidth);
//        }
    }
}
