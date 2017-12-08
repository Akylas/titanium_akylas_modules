package akylas.carto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiImageHelper;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiImageHelper.TiDrawableTarget;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapPosVector;
import com.carto.projections.Projection;
import com.carto.styles.BillboardScaling;
import com.carto.styles.LineEndType;
import com.carto.styles.LineJoinType;
import com.carto.styles.LineStyle;
import com.carto.styles.LineStyleBuilder;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;
import com.carto.utils.BitmapUtils;
import com.carto.vectorelements.Line;
import com.carto.vectorelements.Marker;
import com.carto.vectorelements.VectorElement;
import com.squareup.picasso.Picasso.LoadedFrom;

import akylas.map.common.AkylasMarker;
import akylas.map.common.BaseRouteProxy;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class)
public class RouteProxy extends BaseRouteProxy<MapPos, MapBounds, List<MapPos>>
        implements TiDrawableTarget {
    // private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    // private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
    // private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
    // private static final int MSG_SET_ZINDEX = MSG_FIRST_ID + 403;

    // private PolylineOptions options = null;
    // private Polyline polyline;
    LineStyle lineStyle;
    LineStyleBuilder lineStyleBuilder;
    Line line;

    class AkLine extends Line {
        AnnotationProxy proxy;

        public AkLine(AnnotationProxy proxy, MapPosVector pos,
                LineStyle style) {
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
        mPoints = Collections.synchronizedList(new ArrayList<MapPos>());
        mBoundingBox = AkylasCartoModule.MIN_BOUNDING_BOX;
    }

    @Override
    public String getApiName() {
        return "Akylas.Carto.Route";
    }

    // @Override
    // public boolean handleMessage(Message msg) {
    // AsyncResult result = null;
    // switch (msg.what) {
    //
    //// case MSG_SET_COLOR: {
    //// result = (AsyncResult) msg.obj;
    //// polyline.setColor(mColor);
    //// result.setResult(null);
    //// return true;
    //// }
    //
    // case MSG_SET_WIDTH: {
    // result = (AsyncResult) msg.obj;
    //// polyline.setWidth(mStrokeWidth);
    // result.setResult(null);
    // return true;
    // }
    //
    // case MSG_SET_ZINDEX: {
    // result = (AsyncResult) msg.obj;
    //// polyline.setZIndex(zIndex);
    // result.setResult(null);
    // return true;
    // }
    // default: {
    // return super.handleMessage(msg);
    // }
    // }
    // }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case "lineWidth":
            mStrokeWidth = TiUIHelper.getInPixels(newValue, mStrokeWidth);
            setLineWidth(mStrokeWidth);

            break;
        case "selectedLineWidth":
            mSelectedStrokeWidth = TiUIHelper.getInPixels(newValue,
                    mStrokeWidth);
            if (lineStyleBuilder != null
                    && (selected && mSelectedStrokeWidth >= 0)) {
                lineStyleBuilder.setWidth(mSelectedStrokeWidth);
            }
            break;
        case TiC.PROPERTY_COLOR:
            tintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
            setColor(tintColor);
            break;
        case TiC.PROPERTY_SELECTED_COLOR:
            selectedTintColor = TiConvert.toColor(newValue, Color.TRANSPARENT);
            setSelectedColor(selectedTintColor);
            break;
        case TiC.PROPERTY_ZINDEX:
            zIndex = TiConvert.toInt(newValue);
            // if (lineStyleBuilder != null) {
            // line.setZIndex(zIndex);
            // }
            break;
        case TiC.PROPERTY_VISIBLE:
            // if (polyline != null) {
            // polyline.setVisible(TiConvert.toBoolean(newValue));
            // }
            break;
        default:
            break;
        }
    }

    @Override
    protected void didProcessProperties() {
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_TOUCHABLE) != 0) {
            // if (polyline != null) {
            // polyline.setClickable(touchable);
            // }
        }
        if (line != null) {
            invalidate();
        }
        super.didProcessProperties();
    }

    private void updatePolyline() {
        // if (line != null) {
        // runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        synchronized (mPoints) {
            if (line != null && this.mapView != null) {
                line.setPoses(getPosVector(((CartoView)this.mapView).getProjection()));
            }
        }
        // }
        // }, true);
        // }
    }

    @Override
    protected void replacePoints(Object points) {

        super.replacePoints(points);
        updatePolyline();
    }

    @Override
    protected void onPointAdded(final MapPos point,
            final boolean shouldUpdate) {
        super.onPointAdded(point, shouldUpdate);
        if (shouldUpdate) {
            updatePolyline();
        }
    }

    // public PolylineOptions getAndSetOptions(final CameraPosition position) {
    // options = new PolylineOptions();
    // synchronized (mPoints) {
    // return options.width(selected ? mSelectedStrokeWidth : mStrokeWidth)
    // .addAll(mPoints).clickable(touchable)
    // .color((selected && selectedTintColor != Color.TRANSPARENT)
    // ? selectedTintColor : tintColor)
    // .zIndex(selected ? 10000 : zIndex);
    // }
    // }
    //
    // public void setPolyline(Polyline r) {
    // polyline = r;
    // polyline.setTag(this);
    // }
    //
    // public Polyline getPolyline() {
    // return polyline;
    // }

    @Override
    public void removeFromMap() {
        if (line != null) {
            // polyline.remove();
            // polyline.setTag(null);
            line = null;
        }
    }

    // public void onMapCameraChange(final GoogleMap map,
    // final CameraPosition position) {
    // // if (polyline != null) {
    // // if (widthThickness != 1.0f) {
    // // float newWidth = getPathPaint().getStrokeWidth() * widthThickness;
    // // polyline.setWidth(newWidth);
    // // }
    // // }
    // }

    @Override
    public void onDeselect() {
        super.onDeselect();
        if (line == null) {
            return;
        }
        invalidate();
        // if (!TiApplication.isUIThread()) {
        // runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // onDeselect();
        // }
        // }, false);
        // return;
        // }
        //// polyline.setZIndex(zIndex);
        // if (mSelectedStrokeWidth >= 0 && mSelectedStrokeWidth != mStrokeWidth
        // ) {
        //// polyline.setWidth(mStrokeWidth);
        // }
    }

    @Override
    public void onSelect() {
        super.onSelect();
        if (line == null) {
            return;
        }
        invalidate();
        // if (!TiApplication.isUIThread()) {
        // runInUiThread(new CommandNoReturn() {
        // @Override
        // public void execute() {
        // onSelect();
        // }
        // }, false);
        // return;
        // }

        // polyline.setZIndex(10000);
        // if (mSelectedStrokeWidth >= 0 && mSelectedStrokeWidth != mStrokeWidth
        // ) {
        // polyline.setWidth(mSelectedStrokeWidth);
        // }
    }

    @Override
    protected long getPointsSize() {
        return mPoints.size();
    }

    @Override
    protected MapPos getPoint(int i) {
        return mPoints.get(i);
    }

    @Override
    protected void addPos(MapPos point) {
        mPoints.add(point);
    }

    @Override
    protected void clearPoints() {
        mPoints.clear();
    }

    @Override
    protected Object[] getPointsArray() {
        final int size = (int) mPoints.size();
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = mPoints.get(i);
        }
        return result;
    }

    public void invalidate() {
        if (line == null) {
            return;
        }
        lineStyle = null;

        // here we also invalidate the builder as some props are handled by the
        // superclass
        lineStyleBuilder = null;
        line.setStyle(getBuildStyle());

    }

    private void setColor(int color) {
        if (lineStyleBuilder != null
                && (!selected || (selectedTintColor == Color.TRANSPARENT))) {
            lineStyleBuilder.setColor(new com.carto.graphics.Color(color));
        }
    }

    private void setSelectedColor(int color) {
        if (lineStyleBuilder != null && selected && color != Color.TRANSPARENT) {
            lineStyleBuilder.setColor(new com.carto.graphics.Color(color));
        }
    }

    private void setLineWidth(float width) {
        if (lineStyleBuilder != null
                && (!selected || mSelectedStrokeWidth < 0)) {
            lineStyleBuilder.setWidth(width);
        }
    }

    private void setSelectedLineWidth(float width) {
        if (lineStyleBuilder != null
                && (selected && mSelectedStrokeWidth >= 0)) {
            lineStyleBuilder.setWidth(width);
        }
    }

    protected TiDrawableReference getImage() {
        TiDrawableReference imageref = getCurrentImageRef();
        if (imageref != null) {
            TiImageHelper.downloadDrawable(this, imageref, true, this);
        }
        return imageref;
    }

    protected void setImage(final TiDrawableReference ref) {
        if (ref != null) {
            if (!TiApplication.isUIThread()) {
                runInUiThread(new CommandNoReturn() {
                    public void execute() {
                        TiImageHelper.downloadDrawable(RouteProxy.this, ref,
                                true, RouteProxy.this);
                    }
                }, true);
                return;
            } else {
                TiImageHelper.downloadDrawable(this, ref, true, this);
            }
        }
    }

    @Override
    public void onDrawableLoaded(Drawable drawable, LoadedFrom from) {
        if (drawable instanceof BitmapDrawable) {
            onBitmapLoaded(((BitmapDrawable) drawable).getBitmap(), from);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        handleSetImage(null);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        handleSetImage(bitmap);
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {
    }

    protected void handleSetImage(Bitmap bitmap) {
        // if (bitmap == null) {
        // setIconImageWidth(-1);
        // setIconImageHeight(-1);
        // return;
        // }
        // setIconImageWidth(bitmap.getWidth());
        // setIconImageHeight(bitmap.getHeight());
        // setIconSize(Math.max(bitmap.getWidth(), bitmap.getHeight()));
        com.carto.graphics.Bitmap icon = BitmapUtils
                .createBitmapFromAndroidBitmap(bitmap);
        if (lineStyleBuilder != null) {
            // markerOptions.setSize(Math.max(bitmap.getWidth(),
            // bitmap.getHeight()));
            lineStyleBuilder.setBitmap(icon);
        }
        if (line != null) {
            invalidate();
        }
    };

    private LineJoinType getJoinType() {
        switch (mStrokeJoin) {
        case BEVEL:
            return LineJoinType.LINE_JOIN_TYPE_BEVEL;
        case MITER:
            return LineJoinType.LINE_JOIN_TYPE_MITER;
        case ROUND:
            return LineJoinType.LINE_JOIN_TYPE_ROUND;
        default:
            return LineJoinType.LINE_JOIN_TYPE_NONE;

        }
    }

    private LineEndType getEndType() {
        switch (mStrokeCap) {
        case ROUND:
            return LineEndType.LINE_END_TYPE_ROUND;
        case SQUARE:
            return LineEndType.LINE_END_TYPE_SQUARE;
        default:
            return LineEndType.LINE_END_TYPE_NONE;
        }
    }

    public LineStyleBuilder getBuilder() {
        if (lineStyleBuilder == null) {

            lineStyleBuilder = new LineStyleBuilder();
            setColor(tintColor);
            setSelectedColor(selectedTintColor);
            setLineWidth(mStrokeWidth);
            setSelectedLineWidth(mSelectedStrokeWidth);
            lineStyleBuilder.setLineJoinType(getJoinType());
            lineStyleBuilder.setLineEndType(getEndType());
            getImage();

        }

        return lineStyleBuilder;
    }

    public LineStyle getBuildStyle() {
        if (lineStyle == null) {
            lineStyle = getBuilder().buildStyle();
        }
        return lineStyle;
    }
    
    @Override
    public void setVisible(Object value) {
        super.setVisible(value);
        if (line != null) {
            line.setVisible(visible);
        }
    }

    public MapPosVector getPosVector(Projection baseProjection) {
        long size = getPointsSize();
        MapPosVector result = new MapPosVector(size);
        for (int i = 0; i < size; i++) {
            result.set(i, baseProjection.fromWgs84(getPoint(i)));
        }
        return result;
    }

    public Line getOrCreateLine(Projection baseProjection) {
        if (line == null) {
            line = new Line(getPosVector(baseProjection), getBuildStyle());
            line.setVisible(visible);
        }
        return line;
    }

    public Line getLine() {
        return line;
    }
    
    public VectorElement getVectorElement() {
        return line;
    }
}
