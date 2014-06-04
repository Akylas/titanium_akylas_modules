package akylas.map;

import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.SafePaint;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.os.Message;

@Kroll.proxy(creatableInModule=AkylasMapModule.class, propertyAccessors = {
	AkylasMapModule.PROPERTY_POINTS,
	TiC.PROPERTY_COLOR,
	TiC.PROPERTY_WIDTH
})
public class RouteProxy extends KrollProxy
{
    private ArrayList<LatLng> mPoints;
    private ArrayList<com.google.android.gms.maps.model.LatLng> mGooglePoints;
		
	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;
	
	private static final int MSG_SET_POINTS = MSG_FIRST_ID + 400;
	private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
	private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;
	private static final int MSG_REFRESH_MAP = MSG_FIRST_ID + 403;
    private BoundingBox mBoundingBox = TileLayerConstants.WORLD_BOUNDING_BOX;
    
    private MapView mMapView;
	private PathOverlay mPath;
	
	private PolylineOptions options = null;
    private Polyline polyline;

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
	public boolean handleMessage(Message msg) 
	{
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
				polyline.setColor((Integer)result.getArg());
				result.setResult(null);
				return true;
			}
			
			case MSG_SET_WIDTH: {
				result = (AsyncResult) msg.obj;
				polyline.setWidth(getPathPaint().getStrokeWidth());
				result.setResult(null);
				return true;
			}
			
			case MSG_REFRESH_MAP: {
				if (mMapView != null) {
					mMapView.invalidate();
				}
				return true;
			}
			default : {
				return super.handleMessage(msg);
			}
		}
	}
	
	public static float getRawSize(KrollDict dict, String property,
            String defaultValue, Context context) {
        return TiUIHelper.getRawSize(dict.optString(property, defaultValue),
                context);
    }
	
	public static float getRawSize(KrollDict dict, String property, String defaultValue) {
        return getRawSize(dict, property, defaultValue, null);
    }
	
	public static float getRawSize(KrollDict dict, String property, float defaultValue) {
        return getRawSize(dict, property, String.valueOf(defaultValue), null);
    }
	
	@Override
	public void handleCreationDict(KrollDict dict) {
		super.handleCreationDict(dict);
		if (dict.containsKey(AkylasMapModule.PROPERTY_POINTS)) {
			 processPoints(dict.get(AkylasMapModule.PROPERTY_POINTS));
		}
		if (dict.containsKey(TiC.PROPERTY_WIDTH)) {
			getPathPaint().setStrokeWidth(getRawSize(dict, TiC.PROPERTY_WIDTH, getPathPaint().getStrokeWidth()));
		}
		if (dict.containsKey(TiC.PROPERTY_COLOR)) {
			getPathPaint().setColor(TiConvert.toColor(dict, TiC.PROPERTY_COLOR));
		}
		if (dict.containsKey(AkylasMapModule.PROPERTY_LINE_JOIN)) {
			getPathPaint().setStrokeJoin(joinFromString(TiConvert.toString(dict, AkylasMapModule.PROPERTY_LINE_JOIN)));
		}
		if (dict.containsKey(AkylasMapModule.PROPERTY_LINE_CAP)) {
			getPathPaint().setStrokeCap(capFromString(TiConvert.toString(dict, AkylasMapModule.PROPERTY_LINE_CAP)));
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
 	
	public void addLocation(Object loc) {
		LatLng point = AkylasMapModule.latlongFromObject(loc);
		if (point != null) {
			mPoints.add(point);
		}
		if (mPath != null) {
			mPath.addPoint(point);
		}
		mGooglePoints.add(AkylasMapModule.mapBoxToGoogle(point));
	}

	public void processPoints(Object points) {
		
        mPoints.clear();
        mGooglePoints.clear();
        if (mPath != null) {
            mPath.clearPath();
        }
		//multiple points
		if (points instanceof Object[]) {
			Object[] pointsArray = (Object[]) points;
			for (int i = 0; i < pointsArray.length; i++) {
				Object obj = pointsArray[i];
				addLocation(obj);
			}
		}
		else {
			addLocation(points);
		}
		updateBoundingBox();
		if (polyline != null) {
		    TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_POINTS));
        }
		
		if (mPoints.size() > 1) invalidate();
	}
	
	private void invalidate() {
		if (TiApplication.isUIThread()) {
			if (mMapView != null) {
				mMapView.invalidate();
			}
		} else {
			getRuntimeHandler().obtainMessage(MSG_REFRESH_MAP);
		}
		
	}
	
//	public PolylineOptions getOptions() {
//		return options;
//	}
//	
//	public void setRoute(Polyline r) {
//		route = r;
//	}
//	
//	public Polyline getRoute() {
//		return route;
//	}
	
	
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

	@Override
	public void onPropertyChanged(String name, Object value) {
		super.onPropertyChanged(name, value);

		if (name.equals(AkylasMapModule.PROPERTY_POINTS)) {
			processPoints(value);
		}

		else if (name.equals(TiC.PROPERTY_COLOR)) {
			getPathPaint().setColor(TiConvert.toColor(value));
			if (polyline != null) {
	            TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_COLOR));
	        }
		}
		
		else if (name.equals(TiC.PROPERTY_WIDTH)) {
			getPathPaint().setStrokeWidth(TiConvert.toFloat(value));
			if (polyline != null) {
	            TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SET_WIDTH));
	        }
		}
		else if (name.equals(AkylasMapModule.PROPERTY_LINE_JOIN)) {
			getPathPaint().setStrokeJoin(joinFromString(TiConvert.toString(value)));
		}
		else if (name.equals(AkylasMapModule.PROPERTY_LINE_CAP)) {
			getPathPaint().setStrokeCap(capFromString(TiConvert.toString(value)));
		}
		
	}
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getRegion() {

		return AkylasMapModule.regionToDict(mBoundingBox);
	}
	
	@Kroll.method
	public void addPoint(Object point) {
		addLocation(point);
		updateBoundingBox();
		invalidate();
	}
	
	public PolylineOptions getAndSetOptions() {
        options = new PolylineOptions();
	    Paint paint = getPathPaint();
        options.width(paint.getStrokeWidth());
        options.addAll(mGooglePoints);
        options.color(paint.getColor());
        return options;
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
}
