/**
 * Copyright (c) 2015 by Studio Classics. All Rights Reserved.
 * Author: Martin Guillon
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.zoomableimage;

import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.ProtectedModule;
import org.appcelerator.titanium.TiApplication;

@Kroll.module(name = "AkylasZoomableimage", id = "akylas.zoomableimage")
public class AkylasZoomableimageModule extends ProtectedModule {
    // Standard Debugging variables
    private static final String TAG = "AkylasZoomableimageModule";

    public AkylasZoomableimageModule() {
        super();
        Log.d(TAG, "AkylasZoomableimageModule module instantiated");
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("Akylas.ZoomableImageView", akylas.zoomableimage.ViewProxy.class.getName());
        
        APIMap.addMapping(map);
    }
    
    @Kroll.onVerifyModule
    public static void onVerifyModule(TiApplication app)
    {
        verifyPassword(app, "akylas.modules.key", AeSimpleSHA1.hexToString("7265745b496b2466553b486f736b7b4f"));
    }

}
