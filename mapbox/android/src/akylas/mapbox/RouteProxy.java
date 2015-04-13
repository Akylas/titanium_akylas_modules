package akylas.mapbox;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiActivityHelper.CommandNoReturn;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.BaseRouteProxy;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;

@Kroll.proxy(creatableInModule = AkylasMapboxModule.class)
public class RouteProxy extends BaseRouteProxy<LatLng, BoundingBox> {
    private PathOverlay mPath;
    private MapView mMapView;
    
    public RouteProxy() {
        super();
        mBoundingBox = BoundingBox.MIN_BOUNDING_BOX;
    }
    
    @Override
    public String getApiName() {
        return "Akylas.Mapbox.Route";
    }
    
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_WIDTH:
            getPathPaint().setStrokeWidth(
                    TiUIHelper.getInPixels(newValue, getPathPaint()
                            .getStrokeWidth()));

            break;
        case TiC.PROPERTY_COLOR:
            getPathPaint().setColor(TiConvert.toColor(newValue));

            break;
        case AkylasMapBaseModule.PROPERTY_LINE_JOIN:
            getPathPaint().setStrokeJoin(
                    joinFromString(TiConvert.toString(newValue)));
            break;
        case AkylasMapBaseModule.PROPERTY_LINE_CAP:
            getPathPaint().setStrokeCap(
                    capFromString(TiConvert.toString(newValue)));
            break;
        default:
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
    
    private void invalidate() {
        if (mPath == null) {
            return;
        }
        runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                if (mMapView != null) {
                    mMapView.invalidate();
                }
                
            }
        });
    }
    
    @Override
    protected void replacePoints(Object points) {
        
        super.replacePoints(points);
        if (mPath != null) {
            mPath.setPoints(mPoints);
            invalidate();
        }
    }
    
    @Override
    protected void onPointAdded(final LatLng point, final boolean shouldUpdate) {
        super.onPointAdded(point, shouldUpdate);
        if (shouldUpdate) {
            invalidate();
        }
    }
}
