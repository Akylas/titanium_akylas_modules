/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.os.Message;
import android.util.Log;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
    AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON,
	TiC.PROPERTY_MAP_TYPE,
	AkylasMapModule.PROPERTY_TRAFFIC,
	TiC.PROPERTY_ENABLE_ZOOM_CONTROLS,
	AkylasMapModule.PROPERTY_COMPASS_ENABLED,
	AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED,
    TiC.PROPERTY_USER_LOCATION,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE, 
    AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
    AkylasMapModule.PROPERTY_REGION_FIT,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE,
    AkylasMapModule.PROPERTY_ZOOM,
    AkylasMapModule.PROPERTY_MINZOOM,
    AkylasMapModule.PROPERTY_MAXZOOM,
    AkylasMapModule.PROPERTY_ROUTES,
    TiC.PROPERTY_REGION,
    TiC.PROPERTY_ANNOTATIONS, 
    AkylasMapModule.PROPERTY_ANIMATE_CHANGES,
    TiC.PROPERTY_ENABLE_ZOOM_CONTROLS 
})
public class MapViewProxy extends MapDefaultViewProxy
{
	private static final String TAG = "MapViewProxy";
	
	private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    private static final int MSG_SNAP_SHOT = MSG_FIRST_ID;
    private final int googlePlayServicesState;
    private final boolean googlePlayServicesAvailable;
	
	public MapViewProxy() {
		super();
		googlePlayServicesState = AkylasMapModule.googlePlayServicesAvailable();
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


	
	@Kroll.method
	public void snapshot()
	{
		if (TiApplication.isUIThread()) {
			handleSnapshot();
		} else {
			getMainHandler().obtainMessage(MSG_SNAP_SHOT).sendToTarget();
		}
	}
	
	private void handleSnapshot() 
	{
		TiUIView view = peekView();
		if (view instanceof AkylasMapView) {
			((AkylasMapView) view).snapshot();
		}
	}

}
