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
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.ParentingProxy;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import akylas.map.AkylasMarker;
import android.graphics.RectF;
import android.os.Message;

@SuppressWarnings("rawtypes")
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
        TiC.PROPERTY_RIGHT_VIEW,
//        AkylasMapModule.PROPERTY_MINZOOM,
//        AkylasMapModule.PROPERTY_MAXZOOM
})
public class AnnotationProxy extends ParentingProxy {
    private static final String TAG = "AnnotationProxy";

    private AkylasMarker marker;
    private AkylasMapInfoView infoView = null;

    private String annoTitle;
    private String annoSubtitle;
    private boolean draggable = false;
    private boolean flat = false;
    private float pinColor;
    private com.mapbox.mapboxsdk.geometry.LatLng latLong;
    private float mMinZoom = -1;
    private float mMaxZoom = -1;
    

    private Comparable sortKey;
    private TiViewProxy _leftViewProxy;
    private TiViewProxy _rightViewProxy;
    private TiViewProxy _customViewProxy;

    AkylasMapDefaultView mapView;

    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    private static final int MSG_SET_LON = MSG_FIRST_ID + 300;
    private static final int MSG_SET_LAT = MSG_FIRST_ID + 301;
    private static final int MSG_SET_DRAGGABLE = MSG_FIRST_ID + 302;
    private static final int MSG_SET_FLAT = MSG_FIRST_ID + 303;
    private static final int MSG_UPDATE_INFO_WINDOW = MSG_FIRST_ID + 304;

    public AnnotationProxy() {
        super();
        annoTitle = "";
        // defaultValues.put(AkylasMapboxModule.PROPERTY_SHOW_INFO_WINDOW,
        // true);
    }

    public AnnotationProxy(TiContext tiContext) {
        this();
    }

    // public MarkerOptions getMarkerOptions() {
    // return markerOptions;
    // }
    
    @Override
    public KrollProxy getParentForBubbling()
    {
        if (mapView != null) {
            return mapView.getProxy();
        }
        return null;
    }
    
    @Override
    public String getApiName()
    {
        return "AkylasMap.Annotation";
    }

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
    
    public float getMinZoom() {
        return mMinZoom;
    }

    public float getMaxZoom() {
        return mMaxZoom;
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
        case MSG_SET_FLAT: {
            result = (AsyncResult) msg.obj;
            if (marker instanceof GoogleMapMarker) {
                ((GoogleMapMarker) marker).getMarker().setFlat(
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

    public void prepareInfoView(AkylasMapInfoView infoView, KrollDict dict) {
        this.infoView = infoView;
        if (infoView == null)
            return;

        infoView.setTitle(annoTitle);
        infoView.setSubtitle(annoSubtitle);
        
        if (dict.containsKey(AkylasMapModule.PROPERTY_CALLOUT_PADDING)) {
            RectF paddingRect = TiConvert.toPaddingRect(dict,
                    AkylasMapModule.PROPERTY_CALLOUT_PADDING);
            infoView.setPadding((int) paddingRect.left, (int) paddingRect.top,
                    (int) paddingRect.right, (int) paddingRect.bottom);
        }
        
        if (mapView.calloutUseTemplates()) {
            KrollDict extraData = new KrollDict();
            extraData.put(AkylasMapModule.PROPERTY_ANNOTATION, (KrollProxy)this);
            extraData.put("inCallout", true);
            if (dict.containsKey(TiC.PROPERTY_LEFT_VIEW)) {
                CalloutReusableProxy proxy = mapView.reusableViewFromDict(dict.getKrollDict(TiC.PROPERTY_LEFT_VIEW), extraData);
                if (proxy != null) {
                    proxy.setParentProxy(this);
                    infoView.setLeftOrRightPane(proxy,
                            AkylasMapInfoView.LEFT_PANE);
                }
            }
            if (dict.containsKey(TiC.PROPERTY_RIGHT_VIEW)) {
                CalloutReusableProxy proxy = mapView.reusableViewFromDict(dict.getKrollDict(TiC.PROPERTY_RIGHT_VIEW), extraData);
                if (proxy != null) {
                    proxy.setParentProxy(this);
                    infoView.setLeftOrRightPane(proxy,
                            AkylasMapInfoView.RIGHT_PANE);
                }
            }
            if (dict.containsKey(TiC.PROPERTY_CUSTOM_VIEW)) {
                CalloutReusableProxy proxy = mapView.reusableViewFromDict(dict.getKrollDict(TiC.PROPERTY_CUSTOM_VIEW), extraData);
                if (proxy != null) {
                    proxy.setParentProxy(this);
                    infoView.setCustomView(proxy);
                }
            }
            return;
        }

        if (_leftViewProxy == null) {
            if (dict.containsKey(TiC.PROPERTY_LEFT_VIEW)) {
                Object value = dict.get(TiC.PROPERTY_LEFT_VIEW);
                if (value instanceof HashMap) {
                    _leftViewProxy = (TiViewProxy) createProxyFromTemplate(
                            (HashMap) value, this, true);
                    if (_leftViewProxy != null) {
                        _leftViewProxy.updateKrollObjectProperties();
                    }
                } else if (value instanceof TiViewProxy) {
                    _leftViewProxy = (TiViewProxy) value;
                }
            }
        }
        if (_leftViewProxy != null) {
            infoView.setLeftOrRightPane(_leftViewProxy,
                    AkylasMapInfoView.LEFT_PANE);
        } else {
            infoView.setLeftOrRightPane(dict.get(TiC.PROPERTY_LEFT_BUTTON),
                    AkylasMapInfoView.LEFT_PANE);
        }

        if (_rightViewProxy == null) {
            if (dict.containsKey(TiC.PROPERTY_RIGHT_VIEW)) {
                Object value = dict.get(TiC.PROPERTY_RIGHT_VIEW);
                if (value instanceof HashMap) {
                    _rightViewProxy = (TiViewProxy) createProxyFromTemplate(
                            (HashMap) value, this, true);
                    if (_rightViewProxy != null) {
                        _rightViewProxy.updateKrollObjectProperties();
                    }
                } else if (value instanceof TiViewProxy) {
                    _rightViewProxy = (TiViewProxy) value;
                }
            }
        }
        if (_rightViewProxy != null) {
            infoView.setLeftOrRightPane(_rightViewProxy,
                    AkylasMapInfoView.RIGHT_PANE);
        } else {
            infoView.setLeftOrRightPane(dict.get(TiC.PROPERTY_RIGHT_BUTTON),
                    AkylasMapInfoView.RIGHT_PANE);
        }

        // has to done last
        if (_customViewProxy == null) {
            if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
                Object value = dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW);
                if (value instanceof HashMap) {
                    _customViewProxy = (TiViewProxy) createProxyFromTemplate(
                            (HashMap) value, this, true);
                    if (_customViewProxy != null) {
                        _customViewProxy.updateKrollObjectProperties();
                    }
                } else if (value instanceof TiViewProxy) {
                    _customViewProxy = (TiViewProxy) value;
                }
            }
        }
        if (_customViewProxy != null) {
            infoView.setCustomView(_customViewProxy);
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
            draggable = dict.optBoolean(AkylasMapModule.PROPERTY_DRAGGABLE,
                    false);
        }
        if (dict.containsKey(AkylasMapModule.PROPERTY_FLAT)) {
            flat = dict.optBoolean(AkylasMapModule.PROPERTY_FLAT,
                    false);
        }
        
        if (dict.containsKey(AkylasMapModule.PROPERTY_MINZOOM)) {
            mMinZoom = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_MINZOOM);
        }
        if (dict.containsKey(AkylasMapModule.PROPERTY_MAXZOOM)) {
            mMaxZoom = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_MAXZOOM);
        }

        if (dict.containsKey(AkylasMapModule.PROPERTY_SORT_KEY)) {
            Object value = dict.get(AkylasMapModule.PROPERTY_SORT_KEY);
            if (value instanceof Comparable) {
                sortKey = (Comparable) value;
            }
        }
        if (dict.containsKey(TiC.PROPERTY_PINCOLOR)) {
            pinColor = dict.optInt(TiC.PROPERTY_PINCOLOR, -1);
        }
        
    }

    public void setMarker(AkylasMarker m) {
        if (m == null && marker != null) {
            marker.prepareRemoval();
        }
        marker = m;
    }

    public void setMapView(AkylasMapDefaultView mbView) {
        mapView = mbView;
    }

    public AkylasMarker getMarker() {
        return marker;
    }

    public AkylasMapDefaultView getMapView() {
        return mapView;
    }

    public Comparable getSortKey() {
        return sortKey;
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
        return infoView;
    }
    
    @Kroll.method @Kroll.setProperty
    public void setMinZoom(Object value)
    {
        mMinZoom = TiConvert.toFloat(value, -1);
        if (marker != null) {
            ((MapboxMarker)marker).getMarker().setMinZoom(mMinZoom);
        }
    }
    
    @Kroll.method @Kroll.setProperty
    public void setMaxZoom(Object value)
    {
        mMaxZoom = TiConvert.toFloat(value, -1);
        if (marker != null) {
            ((MapboxMarker)marker).getMarker().setMaxZoom(mMaxZoom);
        }
    }

    @Override
    public void onPropertyChanged(String name, Object value, Object oldValue) {
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
            if (infoView != null) {
                infoView.setTitle(annoTitle);
                updateInfoWindow();
            }
        } else if (name.equals(TiC.PROPERTY_SUBTITLE)) {
            annoSubtitle = TiConvert.toString(value);
            if (infoView != null) {
                infoView.setSubtitle(annoSubtitle);
                updateInfoWindow();
            }
        } else if (infoView != null) {
            if (name.equals(TiC.PROPERTY_LEFT_BUTTON)) {
                if (infoView != null) {
                    infoView.setLeftOrRightPane(value, AkylasMapInfoView.LEFT_PANE);
                }
                if (value == null) {
                    Object leftView = getProperty(TiC.PROPERTY_LEFT_VIEW);
                    if (leftView != null) {
                        infoView.setLeftOrRightPane(leftView,
                                AkylasMapInfoView.LEFT_PANE);
                    }
                }
            } else if (name.equals(TiC.PROPERTY_LEFT_VIEW)) {
                if (_leftViewProxy != null) {
                    _leftViewProxy.releaseViews(true);
                    _leftViewProxy.setParent(null);
                    _leftViewProxy = null;
                }
                if (infoView != null) {
                    infoView.setLeftOrRightPane(value, AkylasMapInfoView.LEFT_PANE);
                }
            } else if (name.equals(TiC.PROPERTY_RIGHT_BUTTON)) {
                if (infoView != null) {
                    infoView.setLeftOrRightPane(value,
                            AkylasMapInfoView.RIGHT_PANE);
                }
                if (value == null) {
                    Object rightView = getProperty(TiC.PROPERTY_RIGHT_VIEW);
                    if (rightView != null) {
                        if (infoView != null) {
                            infoView.setLeftOrRightPane(rightView,
                                AkylasMapInfoView.LEFT_PANE);
                        }
                    }
                }
            } else if (name.equals(TiC.PROPERTY_RIGHT_VIEW)) {
                if (_rightViewProxy != null) {
                    _rightViewProxy.releaseViews(true);
                    _rightViewProxy.setParent(null);
                    _rightViewProxy = null;
                }
                if (infoView != null) {
                    infoView.setLeftOrRightPane(value, AkylasMapInfoView.RIGHT_PANE);
                }
            } else if (name.equals(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
                if (_customViewProxy != null) {
                    _customViewProxy.releaseViews(true);
                    _customViewProxy.setParent(null);
                    _customViewProxy = null;
                }
                if (infoView != null) {
                    infoView.setCustomView(value);
                }
            } else if (name.equals(AkylasMapModule.PROPERTY_DRAGGABLE)) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_DRAGGABLE), TiConvert
                        .toBoolean(value));
            } else if (name.equals(AkylasMapModule.PROPERTY_FLAT)) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_FLAT), TiConvert
                        .toBoolean(value));
            } else if (name.equals(AkylasMapModule.PROPERTY_SORT_KEY)) {
                if (value instanceof Comparable) {
                    sortKey = (Comparable) value;
                    invalidate();
                }
            }
        }
        super.onPropertyChanged(name, value, oldValue);

    }
    
    private void invalidate() {
        if (marker != null) {
            marker.invalidate();
        }
    }

//    public AkylasMapInfoView createAndPrepareInfoWindow() {
//        AkylasMapInfoView infoView = new AkylasMapInfoView(TiApplication.getInstance()
//                .getApplicationContext());
//        prepareInfoView(null);
//        return infoView;
//    }
//
//    AkylasMapInfoView getOrCreateMapInfoView() {
//        if (infoView == null) {
//            createAndPrepareInfoWindow();
//        }
//        return infoView;
//    }

    private void updateInfoWindow() {
        if (marker == null) {
            return;
        }
        // if (TiApplication.isUIThread()) {
        // // Marker m = marker.getMarker();
        // // if (marker != null && marker.()) {
        // // marker.showInfoWindow();
        // // }
        // } else {
        // getMainHandler().sendEmptyMessage(MSG_UPDATE_INFO_WINDOW);
        // }
    }

    public MabpoxInfoWindow createInfoWindow() {
        return mapView.createInfoWindow(this);
    }

    public void infoWindowDidClose(MabpoxInfoWindow mabpoxInfoWindow) {
        infoView = null;
        mapView.infoWindowDidClose(mabpoxInfoWindow);
    }

    public void prepareInfoView(AkylasMapInfoView infoView) {
        prepareInfoView(infoView, getProperties());
    }

    public boolean hasContent() {
        return  annoTitle != null || 
                annoSubtitle != null || 
                hasProperty(AkylasMapModule.PROPERTY_CUSTOM_VIEW) ||
                hasProperty(TiC.PROPERTY_LEFT_VIEW) || 
                hasProperty(TiC.PROPERTY_RIGHT_VIEW);
    }
}