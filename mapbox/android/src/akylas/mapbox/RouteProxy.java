package akylas.mapbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.PathOverlay;

import android.graphics.Paint;
import android.os.Message;

@Kroll.proxy(creatableInModule=AkylasMapboxModule.class, propertyAccessors = {
	AkylasMapboxModule.PROPERTY_POINTS,
	TiC.PROPERTY_COLOR,
	TiC.PROPERTY_WIDTH
})
public class RouteProxy extends KrollProxy
{
	private List<LatLng> mPoints;
		
	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;
	
	private static final int MSG_SET_POINTS = MSG_FIRST_ID + 400;
	private static final int MSG_SET_COLOR = MSG_FIRST_ID + 401;
	private static final int MSG_SET_WIDTH = MSG_FIRST_ID + 402;

	public RouteProxy() {
		super();
		mPoints = new ArrayList<LatLng>();
	}
	
	public RouteProxy(TiContext tiContext) {
		this();
	}
	
	@Override
	public boolean handleMessage(Message msg) 
	{
		AsyncResult result = null;
		switch (msg.what) {

			case MSG_SET_POINTS: {
				result = (AsyncResult) msg.obj;
	//			route.setPoints(processPoints(result.getArg(), true));
				result.setResult(null);
				return true;
			}
			
			case MSG_SET_COLOR: {
				result = (AsyncResult) msg.obj;
	//			route.setColor((Integer)result.getArg());
				result.setResult(null);
				return true;
			}
			
			case MSG_SET_WIDTH: {
				result = (AsyncResult) msg.obj;
	//			route.setWidth((Float)result.getArg());
				result.setResult(null);
				return true;
			}
			default : {
				return super.handleMessage(msg);
			}
		}
	}
	
	@Override
	public void handleCreationDict(KrollDict dict) {
		super.handleCreationDict(dict);
		if (dict.containsKey(AkylasMapboxModule.PROPERTY_POINTS)) {
			 processPoints(dict.get(AkylasMapboxModule.PROPERTY_POINTS));
		}
		if (dict.containsKey(TiC.PROPERTY_WIDTH)) {
			getPathPaint().setStrokeWidth(TiConvert.toFloat(dict, TiC.PROPERTY_WIDTH));
		}
		if (dict.containsKey(TiC.PROPERTY_COLOR)) {
			getPathPaint().setColor(TiConvert.toColor(dict, TiC.PROPERTY_COLOR));
		}
		
	}
	
	private PathOverlay mPath;
	public PathOverlay getPath() {
		if (mPath == null) {
			mPath = new PathOverlay();
			mPath.addPoints(mPoints);
		}
		return mPath;
	}
	public Paint getPathPaint() {
		return getPath().getPaint();
	}
 	
	public void addLocation(Object loc) {
		LatLng point = AkylasMapboxModule.latlongFromDict(loc);
		if (point != null) {
			mPoints.add(point);
		}
		if (mPath != null) {
			mPath.addPoint(point);
		}
	}

	public void processPoints(Object points) {
		
		mPoints.clear();
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
	
	@Override
	public void onPropertyChanged(String name, Object value) {
		super.onPropertyChanged(name, value);
//		if (route == null) {
//			return;
//		}
//		
		if (name.equals(AkylasMapboxModule.PROPERTY_POINTS)) {
			processPoints(value);
		}

		else if (name.equals(TiC.PROPERTY_COLOR)) {
			getPathPaint().setColor(TiConvert.toColor(value));
		}
		
		else if (name.equals(TiC.PROPERTY_WIDTH)) {
			getPathPaint().setStrokeWidth(TiConvert.toFloat(value));
		}
		
	}
	
}
