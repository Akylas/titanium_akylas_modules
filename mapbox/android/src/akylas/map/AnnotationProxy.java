/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import akylas.map.AkylasMarker;
import android.graphics.RectF;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
        TiC.PROPERTY_SUBTITLE, 
        TiC.PROPERTY_SUBTITLEID, 
        TiC.PROPERTY_TITLE,
        TiC.PROPERTY_TITLEID, 
        TiC.PROPERTY_LATITUDE, 
        TiC.PROPERTY_LONGITUDE,
        AkylasMapModule.PROPERTY_DRAGGABLE, 
        TiC.PROPERTY_IMAGE,
        TiC.PROPERTY_PINCOLOR, 
        AkylasMapModule.PROPERTY_CUSTOM_VIEW,
        TiC.PROPERTY_LEFT_BUTTON, 
        TiC.PROPERTY_LEFT_VIEW,
        TiC.PROPERTY_RIGHT_BUTTON, 
        TiC.PROPERTY_RIGHT_VIEW })
public class AnnotationProxy extends KrollProxy {
    private static final String TAG = "AnnotationProxy";

    private AkylasMarker marker;
    private AkylasMapInfoView infoWindow = null;
    
    private String annoTitle;
    private String annoSubtitle;
    private boolean draggable;
    private float pinColor;
    private com.mapbox.mapboxsdk.geometry.LatLng latLong;
    
    private TiViewProxy _leftViewProxy;
    private TiViewProxy _rightViewProxy;
    private TiViewProxy _customViewProxy;

    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    private static final int MSG_SET_LON = MSG_FIRST_ID + 300;
    private static final int MSG_SET_LAT = MSG_FIRST_ID + 301;
    private static final int MSG_SET_DRAGGABLE = MSG_FIRST_ID + 302;
    private static final int MSG_UPDATE_INFO_WINDOW = MSG_FIRST_ID + 303;

    public AnnotationProxy() {
        super();
        annoTitle = "";
        // defaultValues.put(AkylasMapboxModule.PROPERTY_SHOW_INFO_WINDOW,
        // true);
    }

    public AnnotationProxy(TiContext tiContext) {
        this();
    }

//    public MarkerOptions getMarkerOptions() {
//        return markerOptions;
//    }

    @Override
    protected KrollDict getLangConversionTable() {
        KrollDict table = new KrollDict();
        table.put(TiC.PROPERTY_SUBTITLE, TiC.PROPERTY_SUBTITLEID);
        table.put(TiC.PROPERTY_TITLE, TiC.PROPERTY_TITLEID);
        return table;
    }

    public String getTitle() {
        return annoTitle;
    }

    public String getSubtitle() {
        return annoSubtitle;
    }
    
    public boolean getDraggable() {
        return draggable;
    }
    
    public float getPinColor() {
        return pinColor;
    }
    
    public com.mapbox.mapboxsdk.geometry.LatLng getPosition() {
        return latLong;
    }

    @Override
    public boolean handleMessage(Message msg) {
        AsyncResult result = null;
        switch (msg.what) {

        case MSG_SET_LON: {
            result = (AsyncResult) msg.obj;
            setPosition(TiConvert.toDouble(getProperty(TiC.PROPERTY_LATITUDE)),
                    (Double) result.getArg());
            result.setResult(null);
            return true;
        }

        case MSG_SET_LAT: {
            result = (AsyncResult) msg.obj;
            setPosition((Double) result.getArg(),
                    TiConvert.toDouble(getProperty(TiC.PROPERTY_LONGITUDE)));
            result.setResult(null);
            return true;
        }

        case MSG_SET_DRAGGABLE: {
            result = (AsyncResult) msg.obj;
            if (marker instanceof GoogleMapMarker) {
                ((GoogleMapMarker) marker).getMarker().setDraggable(
                        (Boolean) result.getArg());
            }
            result.setResult(null);
            return true;
        }

        case MSG_UPDATE_INFO_WINDOW: {
            updateInfoWindow();
            return true;
        }

        default: {
            return super.handleMessage(msg);
        }
        }
    }

    public void setPosition(double latitude, double longitude) {
        latLong = new com.mapbox.mapboxsdk.geometry.LatLng(latitude, longitude);
        if (marker != null) {
            marker.setPosition(latitude, longitude);
        }
    }
    
    private void prepareInfoView(KrollDict dict) {
        if (infoWindow == null) return;
        if (dict == null) dict = getProperties();
        infoWindow.setTitle(annoTitle);
        infoWindow.setSubtitle(annoSubtitle);
        if (dict.containsKey(AkylasMapModule.PROPERTY_CALLOUT_PADDING)) {
            RectF paddingRect = TiConvert.toPaddingRect(dict, AkylasMapModule.PROPERTY_CALLOUT_PADDING);
            infoWindow.setPadding((int)paddingRect.left, (int)paddingRect.top, (int)paddingRect.right, (int)paddingRect.bottom);
        }
        
        if (_leftViewProxy == null) {
            if (dict.containsKey(TiC.PROPERTY_LEFT_VIEW)) {
                Object value = dict.get(TiC.PROPERTY_LEFT_VIEW);
                if (value instanceof HashMap) {
                    _leftViewProxy = (TiViewProxy)createProxyFromTemplate((HashMap) value,
                           this, true);
                    if (_leftViewProxy != null) {
                        _leftViewProxy.updateKrollObjectProperties();
                    }
                }
                else if (value instanceof TiViewProxy) {
                    _leftViewProxy = (TiViewProxy)value;
                }
            }
        }
        if (_leftViewProxy != null) {
            infoWindow.setLeftOrRightPane(_leftViewProxy,
                    AkylasMapInfoView.LEFT_PANE);
        }
        else {
            infoWindow.setLeftOrRightPane(dict.get(TiC.PROPERTY_LEFT_BUTTON),
                    AkylasMapInfoView.LEFT_PANE);
        }
        
        if (_rightViewProxy == null) {
            if (dict.containsKey(TiC.PROPERTY_RIGHT_VIEW)) {
                Object value = dict.get(TiC.PROPERTY_RIGHT_VIEW);
                if (value instanceof HashMap) {
                    _rightViewProxy = (TiViewProxy)createProxyFromTemplate((HashMap) value,
                           this, true);
                    if (_rightViewProxy != null) {
                        _rightViewProxy.updateKrollObjectProperties();
                    }
                }
                else if (value instanceof TiViewProxy) {
                    _rightViewProxy = (TiViewProxy)value;
                }
            }
        }
        if (_rightViewProxy != null) {
            infoWindow.setLeftOrRightPane(_rightViewProxy,
                    AkylasMapInfoView.RIGHT_PANE);
        }
        else {
            infoWindow.setLeftOrRightPane(dict.get(TiC.PROPERTY_RIGHT_BUTTON),
                    AkylasMapInfoView.RIGHT_PANE);
        }
        
        //has to done last
        if (_customViewProxy == null) {
            if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
                Object value = dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW);
                if (value instanceof HashMap) {
                    _customViewProxy = (TiViewProxy)createProxyFromTemplate((HashMap) value,
                           this, true);
                    if (_customViewProxy != null) {
                        _customViewProxy.updateKrollObjectProperties();
                    }
                }
                else if (value instanceof TiViewProxy) {
                    _customViewProxy = (TiViewProxy)value;
                }
            }
        }
        if (_customViewProxy != null) {
            infoWindow.setCustomView(_customViewProxy);
        }

    }

    @Override
    public void handleCreationDict(KrollDict dict) {
        
        super.handleCreationDict(dict);
        double longitude = 0;
        double latitude = 0;

        if (dict.containsKey(TiC.PROPERTY_LONGITUDE)) {
            longitude = TiConvert.toDouble(dict, TiC.PROPERTY_LONGITUDE);
        }
        if (dict.containsKey(TiC.PROPERTY_LATITUDE)) {
            latitude = TiConvert.toDouble(dict, TiC.PROPERTY_LATITUDE);
        }
        setPosition(latitude, longitude);

        if (dict.containsKey(TiC.PROPERTY_TITLE)) {
            String title = TiConvert.toString(dict, TiC.PROPERTY_TITLE);
            annoTitle = title;
        }
        if (dict.containsKey(TiC.PROPERTY_SUBTITLE)) {
            String subtitle = TiConvert.toString(dict, TiC.PROPERTY_SUBTITLE);
            annoSubtitle = subtitle;
        }

        if (dict.containsKey(AkylasMapModule.PROPERTY_DRAGGABLE)) {
            draggable = dict.optBoolean(AkylasMapModule.PROPERTY_DRAGGABLE, false);
        }
        prepareInfoView(dict);
    }


    public void setMarker(AkylasMarker m) {
        marker = m;
    }

    public AkylasMarker getMarker() {
        return marker;
    }

    public void showInfo() {
        if (marker == null) {
            return;
        }
        marker.hideInfoWindow();
    }

    public void hideInfo() {
        if (marker == null) {
            return;
        }
        marker.hideInfoWindow();

    }

    public AkylasMapInfoView getMapInfoWindow() {
        return infoWindow;
    }

    

    @Override
    public void onPropertyChanged(String name, Object value) {
        super.onPropertyChanged(name, value);

        if (name.equals(TiC.PROPERTY_LONGITUDE)) {
            TiMessenger.sendBlockingMainMessage(
                    getMainHandler().obtainMessage(MSG_SET_LON),
                    TiConvert.toDouble(value));
        } else if (name.equals(TiC.PROPERTY_LATITUDE)) {
            TiMessenger.sendBlockingMainMessage(
                    getMainHandler().obtainMessage(MSG_SET_LAT),
                    TiConvert.toDouble(value));
        } else if (name.equals(TiC.PROPERTY_TITLE)) {
            annoTitle = TiConvert.toString(value);
            if (infoWindow != null) {
                infoWindow.setTitle(annoTitle);
                updateInfoWindow();
            }
        } else if (name.equals(TiC.PROPERTY_SUBTITLE)) {
            annoSubtitle = TiConvert.toString(value);
            if (infoWindow != null) {
                infoWindow.setSubtitle(annoSubtitle);
                updateInfoWindow();
            }
        } else if (infoWindow != null) {
            if (name.equals(TiC.PROPERTY_LEFT_BUTTON)) {
                infoWindow.setLeftOrRightPane(value,
                        AkylasMapInfoView.LEFT_PANE);
                if (value == null) {
                    Object leftView = getProperty(TiC.PROPERTY_LEFT_VIEW);
                    if (leftView != null) {
                        infoWindow.setLeftOrRightPane(leftView,
                                AkylasMapInfoView.LEFT_PANE);
                    }
                }
            } else if (name.equals(TiC.PROPERTY_LEFT_VIEW)) {
                if (_leftViewProxy != null) {
                    _leftViewProxy.releaseViews(true);
                    _leftViewProxy.setParent(null);
                    _leftViewProxy = null;
                }
                infoWindow.setLeftOrRightPane(value,
                        AkylasMapInfoView.LEFT_PANE);
            } else if (name.equals(TiC.PROPERTY_RIGHT_BUTTON)) {
                getOrCreateMapInfoView().setLeftOrRightPane(value,
                        AkylasMapInfoView.RIGHT_PANE);
                if (value == null) {
                    Object rightView = getProperty(TiC.PROPERTY_RIGHT_VIEW);
                    if (rightView != null) {
                        getOrCreateMapInfoView().setLeftOrRightPane(
                                rightView, AkylasMapInfoView.LEFT_PANE);
                    }
                }
            } else if (name.equals(TiC.PROPERTY_RIGHT_VIEW)) {
                if (_rightViewProxy != null) {
                    _rightViewProxy.releaseViews(true);
                    _rightViewProxy.setParent(null);
                    _rightViewProxy = null;
                }
                infoWindow.setLeftOrRightPane(value,
                        AkylasMapInfoView.RIGHT_PANE);
            } else if (name.equals(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
                if (_customViewProxy != null) {
                    _customViewProxy.releaseViews(true);
                    _customViewProxy.setParent(null);
                    _customViewProxy = null;
                }
                infoWindow.setCustomView(value);
            } else if (name.equals(AkylasMapModule.PROPERTY_DRAGGABLE)) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_DRAGGABLE), TiConvert
                        .toBoolean(value));
            }
            updateInfoWindow();
        }

    }

    AkylasMapInfoView getOrCreateMapInfoView() {
        if (infoWindow == null) {
            infoWindow = new AkylasMapInfoView(TiApplication.getInstance()
                    .getApplicationContext());
            prepareInfoView(null);
        }
        return infoWindow;
    }

    private void updateInfoWindow() {
        if (marker == null) {
            return;
        }
//        if (TiApplication.isUIThread()) {
//            // Marker m = marker.getMarker();
//            // if (marker != null && marker.()) {
//            // marker.showInfoWindow();
//            // }
//        } else {
//            getMainHandler().sendEmptyMessage(MSG_UPDATE_INFO_WINDOW);
//        }
    }
}