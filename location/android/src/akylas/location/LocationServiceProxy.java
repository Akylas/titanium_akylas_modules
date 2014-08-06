/**
 * Martin Guillon
 * Copyright (c) 2009-2012 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 */
package akylas.location;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.ServiceProxy;

import android.app.Service;
import android.os.Message;

@Kroll.proxy(creatableInModule=AkylasLocationModule.class, propertyAccessors={
})
public class LocationServiceProxy extends ServiceProxy
{
	private static final String TAG = "LocationServiceProxy";

	private static final int MSG_FIRST_ID = ServiceProxy.MSG_LAST_ID + 1;

	private static final int MSG_TOGGLE_LEFT_VIEW = MSG_FIRST_ID + 100;
	protected static final int MSG_LAST_ID = MSG_FIRST_ID + 999;

	public LocationServiceProxy()
	{
		super();
	}

	public LocationServiceProxy(TiContext tiContext)
	{
		this();
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
//			case MSG_TOGGLE_LEFT_VIEW: {
//				handleToggleLeftView((Boolean)msg.obj);
//				return true;
//			}
			default : {
				return super.handleMessage(msg);
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
    @Override
	protected Class serviceClass() {
        return LocationService.class;
    }

}
