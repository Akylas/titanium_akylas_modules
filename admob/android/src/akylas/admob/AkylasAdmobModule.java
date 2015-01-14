/**
 * Copyright (c) 2011 by Studio Classics. All Rights Reserved.
 * Author: Brian Kurzius
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.admob;

import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.ads.AdSize;

import android.location.Location;

@Kroll.module(name = "AkylasAdmob", id = "akylas.admob")
public class AkylasAdmobModule extends KrollModule {
    // Standard Debugging variables
    private static final String TAG = "AkylasAdmobModule";
    @Kroll.constant
    public static final String AD_RECEIVED = "load";
    @Kroll.constant
    public static final String AD_NOT_RECEIVED = "error";
    @Kroll.constant
    public static final String AD_OPENED = "open";
    @Kroll.constant
    public static final String AD_CLOSED = "close";
    @Kroll.constant
    public static final String AD_LEFT_APP = "leftapp";
    @Kroll.constant
    public static final String SIMULATOR_ID = "Simulator";

    public static Boolean TESTING = false;
    public static String PUBLISHER_ID;

    // *
    public static final String PROPERTY_COLOR_BG = "adBackgroundColor";
    public static final String PROPERTY_COLOR_BG_TOP = "backgroundTopColor";
    public static final String PROPERTY_COLOR_BORDER = "borderColor";
    public static final String PROPERTY_COLOR_TEXT = "textColor";
    public static final String PROPERTY_COLOR_LINK = "linkColor";
    public static final String PROPERTY_COLOR_URL = "urlColor";
    public static final String PROPERTY_GENDER = "gender";
    public static final String PROPERTY_TEST_DEVICES = "testDevices";
    public static final String PROPERTY_ADUNITID = "adUnitId";
    public static final String PROPERTY_ADSIZE = "adSize";
    public static final String PROPERTY_KEYWORDS = "keywords";

    public static final String PROPERTY_COLOR_TEXT_DEPRECATED = "primaryTextColor";
    public static final String PROPERTY_COLOR_LINK_DEPRECATED = "secondaryTextColor";

    // */

    public AkylasAdmobModule() {
        super();
        Log.d(TAG, "adMob module instantiated");
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("AkylasAdmob.View", akylas.admob.AdmobViewProxy.class.getName());
        
        map.put("AkylasAdmob.Interstitial", akylas.admob.InterstitialViewProxy.class.getName());
        APIMap.addMapping(map);
    }

    // use this to set the publisher id
    // must be done before the call to instantiate the view
    @Kroll.method
    public void setPublisherId(String pubId) {
        Log.d(TAG, "setPublisherId(): " + pubId);
        PUBLISHER_ID = pubId;
    }
    /**
     * Converts value into Location object and returns it.
     * 
     * @param value
     *            the value to convert.
     * @return a Date instance.
     * @module.api
     */
    public static Location toLocation(Object value) {
        if (value instanceof HashMap) {
            Location targetLocation = new Location("");// provider name is
                                                       // unecessary
            for (Map.Entry<String, Object> entry : ((HashMap<String, Object>) value)
                    .entrySet()) {
                switch (entry.getKey()) {
                case TiC.PROPERTY_LATITUDE:
                    targetLocation.setLatitude(TiConvert.toDouble(
                            entry.getValue(), 0.0d));
                    break;
                case TiC.PROPERTY_LONGITUDE:
                    targetLocation.setLongitude(TiConvert.toDouble(
                            entry.getValue(), 0.0d));
                    break;
                case TiC.PROPERTY_ACCURACY:
                    targetLocation.setAccuracy(TiConvert.toFloat(
                            entry.getValue(), 0.0f));
                    break;
                default:
                    break;
                }
            }
        }
        return null;
    }
    
    
    public static AdSize sizeFromString(final String size) {
        switch (size) {
        case "largeBanner":
            return AdSize.LARGE_BANNER;
        case "leaderboard":
            return AdSize.LEADERBOARD;
        case "fullBanner":
            return AdSize.FULL_BANNER;
        case "skyscraper":
            return AdSize.WIDE_SKYSCRAPER;
        case "mediumRect":
            return AdSize.MEDIUM_RECTANGLE;
        case "smartBanner":
            return AdSize.SMART_BANNER;
        case "banner":
        default:
            return AdSize.BANNER;
        }
    }
}
