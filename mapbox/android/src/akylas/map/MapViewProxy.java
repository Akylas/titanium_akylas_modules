/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
//  AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED,
  AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM,
//  AkylasMapModule.PROPERTY_USER_TRACKING_MODE,
//  TiC.PROPERTY_USER_LOCATION,
  AkylasMapModule.PROPERTY_CENTER_COORDINATE, 
  AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
  AkylasMapModule.PROPERTY_REGION_FIT,
  AkylasMapModule.PROPERTY_CENTER_COORDINATE,
  AkylasMapModule.PROPERTY_ZOOM,
//  AkylasMapModule.PROPERTY_MINZOOM,
//  AkylasMapModule.PROPERTY_MAXZOOM,
  AkylasMapModule.PROPERTY_ROUTES,
  AkylasMapModule.PROPERTY_MAX_ANNOTATIONS,
//  TiC.PROPERTY_REGION,
//  TiC.PROPERTY_ANNOTATIONS, 
  AkylasMapModule.PROPERTY_ANIMATE_CHANGES,
    
    AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON,
	TiC.PROPERTY_MAP_TYPE,
	AkylasMapModule.PROPERTY_TRAFFIC,
	TiC.PROPERTY_ENABLE_ZOOM_CONTROLS,
	AkylasMapModule.PROPERTY_COMPASS_ENABLED
})
public class MapViewProxy extends MapDefaultViewProxy
{
	private static final String TAG = "MapViewProxy";
	
	private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    private static final int MSG_SNAP_SHOT = MSG_FIRST_ID;
    private int googlePlayServicesState;
    private final boolean googlePlayServicesAvailable;
	
	public MapViewProxy() {
		super();
		
		try {
	        googlePlayServicesState = AkylasMapModule.googlePlayServicesAvailable();
        } catch (Exception e) {
            googlePlayServicesState = -1;
            e.printStackTrace();
        }
        googlePlayServicesAvailable = googlePlayServicesState == 0;
        if (!googlePlayServicesAvailable) {
            Log.e(TAG, "Google Play Services not available: Error " + AkylasMapModule.getGoogleServiceStateMessage(googlePlayServicesState));
        }
        defaultValues.put(AkylasMapModule.PROPERTY_COMPASS_ENABLED, true);
        defaultValues.put(TiC.PROPERTY_MAP_TYPE, true);
	}
	
	public final boolean googlePlayServicesAvailable() {
        return googlePlayServicesAvailable;
    }
    
	public TiUIView createView(Activity activity) {
		return new AkylasMapView(this, activity);
	}

	@Override
	public boolean handleMessage(Message msg) 
	{
//		AsyncResult result = null;
		switch (msg.what) {

		case MSG_SNAP_SHOT: {
			handleSnapshot();
			return true;
		}

		default : {
			return super.handleMessage(msg);
		}
		}
	}
	
	private void handleSnapshot() 
	{
		TiUIView view = peekView();
		if (view instanceof AkylasMapView) {
			((AkylasMapView) view).snapshot();
		}
	}
	
    //KROLL ACCESSORS
	
	 @Kroll.method
    public void snapshot()
    {
        if (TiApplication.isUIThread()) {
            handleSnapshot();
        } else {
            getMainHandler().obtainMessage(MSG_SNAP_SHOT).sendToTarget();
        }
    }
    
    @Kroll.method
    @Override
    public void addAnnotation(Object annotation) {
        super.addAnnotation(annotation);
    }

    @Kroll.method
    @Override
    public void addAnnotations(Object annos) {
        super.addAnnotations(annos);
    }

    @Kroll.method
    @Override
    public void removeAllAnnotations() {
        super.removeAllAnnotations();
    }
    

    @Kroll.method
    @Override
    public void removeAnnotation(Object annotation) {
        super.removeAnnotation(annotation);
    }

    @Kroll.method
    @Override
    public void removeAnnotations(Object annos) {
        super.removeAnnotations(annos);
    }

    @Kroll.method
    @Override
    public void selectAnnotation(Object annotation) {
        super.selectAnnotation(annotation);
    }
    
       
    @Kroll.method
    @Kroll.setProperty
    @Override
    public void setAnnotations(Object annos) {
        super.setAnnotations(annos);
    }
    
    @Kroll.method
    @Kroll.getProperty
    @Override
    public Object getAnnotations() {
        return super.getAnnotations();
    }
    
    @Kroll.method
    @Override
    public void selectUserAnnotation() {
        super.selectUserAnnotation();
    }

    @Kroll.method
    @Override
    public void deselectAnnotation(Object annotation) {
        super.deselectAnnotation(annotation);
    }

    @Kroll.method
    @Override
    public void addRoute(Object route) {
        super.addRoute(route);
    }
    

    @Kroll.method
    @Kroll.getProperty
    public float getMaxZoom() {
        return super.getMaxZoom();
    }

    @Kroll.method
    @Kroll.getProperty
    @Override
    public float getMinZoom() {
        return super.getMinZoom();
    }
    
    @Kroll.method
    @Kroll.getProperty
    @Override
    public float getZoom() {
        return super.getZoom();
    }
    
    @Kroll.method
    @Kroll.getProperty
    @Override
    public KrollDict getRegion() {
        return super.getRegion();
    }
    
    @Kroll.method
    @Kroll.getProperty
    @Override
    public boolean getUserLocationEnabled() {
        return super.getUserLocationEnabled();
    }
    
    @Kroll.method
    @Kroll.getProperty
    @Override
    public int getUserTrackingMode() {
        return super.getUserTrackingMode();
    }

    @Kroll.method
    @Kroll.getProperty
    @Override
    public KrollDict getUserLocation() {
        return super.getUserLocation();
    }
    

    @Kroll.method
    @Override
    public void removeRoute(RouteProxy route) {
        super.removeRoute(route);
    }

    @Kroll.method
    @Override
    public void zoom(int delta) {
        super.zoom(delta);
    }

    @Kroll.method
    public void zoomIn(@Kroll.argument(optional = true) final Object about) {
        super.zoomIn(about);
    }

    @Kroll.method
    @Override
    public void zoomOut(@Kroll.argument(optional = true) final Object about) {
        super.zoomOut(about);
    }

}
