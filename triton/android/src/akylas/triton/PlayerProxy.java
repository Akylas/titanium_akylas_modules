/**
 * Akylas
 * Copyright (c) 2014-2015 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.triton;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;

import ti.modules.titanium.audio.BasePlayerProxy;

@Kroll.proxy(creatableInModule = AkylasTritonModule.class, propertyAccessors = {
        TiC.PROPERTY_VOLUME, 
        TiC.PROPERTY_METADATA, 
        TiC.PROPERTY_REPEAT_MODE,
        TiC.PROPERTY_SHUFFLE_MODE,
        })
public class PlayerProxy extends BasePlayerProxy {
    private static final String TAG = "StreamerProxy";

    @Override
    public String getApiName() {
        return "Akylas.Triton.Player";
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    protected Class serviceClass() {
        return TritonAudioService.class;
    }
}
