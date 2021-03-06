/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package akylas.itinerarium.sas;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;

import com.smartadserver.android.library.ui.SASAdView;

@Kroll.module(name="SASModule", id="akylas.itinerarium.sas")
public class SASModule extends KrollModule
{
	static int mSiteId = -1;
	static String mBaseUrl = null;
	// Standard Debugging variables
	private static final String TAG = "AndroidModule";

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public SASModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	// Methods
	@Kroll.method
	public void setSiteIdAndBaseUrl(Integer siteId, String baseUrl)
	{
		mSiteId = siteId;
		mBaseUrl = baseUrl;
		SASAdView.setBaseUrl(mBaseUrl);
	}
	
	static public int getSiteId()
	{
		return mSiteId;
	}
	
	static public String getBaseUrl()
	{
		return mBaseUrl;
	}

}

