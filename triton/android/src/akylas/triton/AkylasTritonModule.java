/**
 * Copyright (c) 2015 by Studio Classics. All Rights Reserved.
 * Author: Martin Guillon
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.triton;

import java.util.HashMap;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

@Kroll.module(name = "AkylasTriton", id = "akylas.triton")
public class AkylasTritonModule extends KrollModule {
    // Standard Debugging variables
    private static final String TAG = "AkylasTritonModule";

    public AkylasTritonModule() {
        super();
        Log.d(TAG, "AkylasTritonModule module instantiated");
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("AkylasTriton.Player", akylas.triton.PlayerProxy.class.getName());
        
        APIMap.addMapping(map);
    }

}
