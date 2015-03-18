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
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.AnimatableReusableProxy;
import org.appcelerator.titanium.util.TiConvert;

import akylas.map.AkylasMarker;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Message;

@SuppressWarnings("rawtypes")
@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
        TiC.PROPERTY_SUBTITLE, TiC.PROPERTY_SUBTITLEID, TiC.PROPERTY_TITLE,
        TiC.PROPERTY_TITLEID, TiC.PROPERTY_LATITUDE, TiC.PROPERTY_LONGITUDE,
        AkylasMapModule.PROPERTY_DRAGGABLE, AkylasMapModule.PROPERTY_FLAT,
        TiC.PROPERTY_IMAGE, TiC.PROPERTY_PINCOLOR,
        AkylasMapModule.PROPERTY_CUSTOM_VIEW, TiC.PROPERTY_LEFT_BUTTON,
        TiC.PROPERTY_LEFT_VIEW, TiC.PROPERTY_RIGHT_BUTTON,
        TiC.PROPERTY_RIGHT_VIEW })
public class AnnotationProxy extends AnimatableReusableProxy {
    private static final String TAG = "AnnotationProxy";

    private AkylasMarker marker;
    private AkylasMapInfoView infoView = null;

    private String annoTitle;
    private String annoSubtitle;
    private RectF calloutPadding = null;
    private PointF anchor = null;
    private PointF calloutAnchor = null;
    private boolean draggable = false;
    private boolean flat = false;
    private boolean showInfoWindow = true;
    private float pinColor;
    private com.mapbox.mapboxsdk.geometry.LatLng latLong;
    private float mMinZoom = -1;
    private float mMaxZoom = -1;
    private double longitude = 0;
    private double latitude = 0;
    private double altitude = 0;
    public float heading = 0;
    public boolean visible = true;
    private boolean imageWithShadow = false;

    private Comparable sortKey;
    
    protected int mProcessUpdateFlags = 0;
    protected static final int TIFLAG_NEEDS_LOCATION   = 0x00000001;

    AkylasMapDefaultView mapView;

    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

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
    public String getApiName() {
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
    

    public boolean canShowInfoWindow() {
        return showInfoWindow;
    }

    public boolean getFlat() {
        return flat;
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

    public PointF getAnchor() {
        return anchor;
    }
    public PointF getCalloutAnchor() {
        return calloutAnchor;
    }
    

    public boolean getImageWithShadow() {
        return imageWithShadow;
    }

    public com.mapbox.mapboxsdk.geometry.LatLng getPosition() {
        if (latLong == null) {
            latLong = new com.mapbox.mapboxsdk.geometry.LatLng(latitude, longitude);
        }
        return latLong;
    }

    @Override
    public boolean handleMessage(Message msg) {
        // AsyncResult result = null;
        switch (msg.what) {
        case MSG_UPDATE_INFO_WINDOW: {
            updateInfoWindow();
            return true;
        }

        default: {
            return super.handleMessage(msg);
        }
        }
    }

    public void setPosition(double latitude, double longitude, double altitude) {
        latLong = new com.mapbox.mapboxsdk.geometry.LatLng(latitude, longitude, altitude);
        if (marker != null) {
            marker.setPosition(latitude, longitude, altitude);
        }
    }
    
    private void setView(Object value, final String key, final int side) {
        if (mapView == null || infoView == null) {
          //will be handled later
            return;
        }
        KrollProxy proxy = null;
        //infoView is supposed NOT to be null
        if (value instanceof HashMap && mapView.calloutUseTemplates()) {
            proxy = mapView.reusableViewFromDict(TiConvert.toKrollDict(value), extraData());
            addProxyToHold(proxy, key);
        } else {
            proxy = getHoldedProxy(key);
            if (proxy == null) {
                proxy = addProxyToHold(value, key);
            }
        }
        if (side == AkylasMapInfoView.CUSTOM_VIEW) {
            if (proxy != null) {
                infoView.setCustomView(proxy);
            }
        } else {
            infoView.setLeftOrRightPane(proxy, side);
        }
        
    }
    
    private KrollDict mExtraDict = null;
    public KrollDict extraData() {
        if (mExtraDict == null) {
            mExtraDict = new KrollDict();
            mExtraDict.put(AkylasMapModule.PROPERTY_ANNOTATION,
                    (KrollProxy) this);
            mExtraDict.put("inCallout", true);
        }
        return mExtraDict;
    }

    public void prepareInfoView(AkylasMapInfoView infoView, KrollDict dict) {
        this.infoView = infoView;
        if (infoView == null)
            return;

        infoView.setTitle(annoTitle);
        infoView.setSubtitle(annoSubtitle);

        if (calloutPadding != null) {
            infoView.setPadding((int) calloutPadding.left,
                    (int) calloutPadding.top,
                    (int) calloutPadding.right,
                    (int) calloutPadding.bottom);
        }

        setView(dict.get(TiC.PROPERTY_LEFT_VIEW), TiC.PROPERTY_LEFT_VIEW, AkylasMapInfoView.LEFT_PANE);
        KrollProxy proxy = getHoldedProxy(TiC.PROPERTY_LEFT_VIEW);
        if (proxy == null) {
            infoView.setLeftOrRightPane(dict.get(TiC.PROPERTY_LEFT_BUTTON),
                    AkylasMapInfoView.LEFT_PANE);
        }
        setView(dict.get(TiC.PROPERTY_RIGHT_VIEW), TiC.PROPERTY_RIGHT_VIEW, AkylasMapInfoView.RIGHT_PANE);
        proxy = getHoldedProxy(TiC.PROPERTY_RIGHT_VIEW);
        if (proxy == null) {
            infoView.setLeftOrRightPane(dict.get(TiC.PROPERTY_RIGHT_BUTTON),
                    AkylasMapInfoView.RIGHT_PANE);
        }
        setView(dict.get(TiC.PROPERTY_CUSTOM_VIEW), TiC.PROPERTY_CUSTOM_VIEW, AkylasMapInfoView.CUSTOM_VIEW);
    }
    
    public void wasRemoved() {
        setMarker(null);
        setMapView(null);
        setParentForBubbling(null);
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

    @Kroll.method
    @Kroll.setProperty
    public void setMinZoom(Object value) {
        mMinZoom = TiConvert.toFloat(value, -1);
        if (marker != null) {
            ((MapboxMarker) marker).getMarker().setMinZoom(mMinZoom);
        }
    }

    @Kroll.method
    @Kroll.setProperty
    public void setMaxZoom(Object value) {
        mMaxZoom = TiConvert.toFloat(value, -1);
        if (marker != null) {
            ((MapboxMarker) marker).getMarker().setMaxZoom(mMaxZoom);
        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_LONGITUDE:
            longitude = TiConvert.toDouble(newValue, 0.0);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOCATION;
            break;
        case TiC.PROPERTY_LATITUDE:
            latitude = TiConvert.toDouble(newValue, 0.0);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOCATION;
            break;
        case TiC.PROPERTY_ALTITUDE:
            altitude = TiConvert.toDouble(newValue, 0.0);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOCATION;
            break;
        case TiC.PROPERTY_HEADING:
            heading = TiConvert.toFloat(newValue, 0.0f);
            if (marker != null) {
                marker.setHeading(heading);
            }
            break;
        case TiC.PROPERTY_VISIBLE:
            visible = TiConvert.toBoolean(newValue, true);
            if (marker != null) {
                marker.setVisible(visible);
            }
            break;
        case AkylasMapModule.PROPERTY_IMAGE_WITH_SHADOW:
            imageWithShadow = TiConvert.toBoolean(newValue, false);
            if (marker instanceof MapboxMarker) {
                ((MapboxMarker)marker).setImageWithShadow(imageWithShadow);
            }
            break;
        case AkylasMapModule.PROPERTY_MINZOOM:
            mMinZoom = TiConvert.toFloat(newValue);
            break;
        case AkylasMapModule.PROPERTY_MAXZOOM:
            mMaxZoom = TiConvert.toFloat(newValue);
            break;
        case AkylasMapModule.PROPERTY_CALLOUT_PADDING:
            calloutPadding = TiConvert.toPaddingRect(newValue);
            if (infoView != null) {
                if (calloutPadding != null) {
                    infoView.setPadding((int) calloutPadding.left,
                            (int) calloutPadding.top,
                            (int) calloutPadding.right,
                            (int) calloutPadding.bottom);
                } else {
                    infoView.setPadding(0, 0, 0, 0);
                }
            }
            break;
        case AkylasMapModule.PROPERTY_CALLOUT_ANCHOR:
            calloutAnchor = TiConvert.toPointF(newValue);
            if (marker != null) {
                marker.setWindowAnchor(calloutAnchor);
            }
            break;
        case AkylasMapModule.PROPERTY_ANCHOR:
            anchor = TiConvert.toPointF(newValue);
            if (marker != null) {
                marker.setAnchor(anchor);
            }
            break;
        case TiC.PROPERTY_PINCOLOR:
            pinColor = TiConvert.toInt(newValue, -1);
            break;
        case TiC.PROPERTY_TITLE:
            annoTitle = TiConvert.toString(newValue);
            if (infoView != null) {
                infoView.setTitle(annoTitle);
                updateInfoWindow();
            }
            break;
        case TiC.PROPERTY_SUBTITLE:
            annoSubtitle = TiConvert.toString(newValue);
            if (infoView != null) {
                infoView.setSubtitle(annoSubtitle);
                updateInfoWindow();
            }
            break;
        case AkylasMapModule.PROPERTY_DRAGGABLE:
            draggable = TiConvert.toBoolean(newValue);
            if (marker != null) {
                marker.setDraggable(draggable);
            }
            break;
        case AkylasMapModule.PROPERTY_FLAT:
            flat = TiConvert.toBoolean(newValue);
            if (marker != null) {
                marker.setFlat(flat);
            }
            break;
        case AkylasMapModule.PROPERTY_SHOW_INFO_WINDOW:
            showInfoWindow = TiConvert.toBoolean(newValue);
            if (!showInfoWindow) {
                hideInfo();
            }
            break;
        case TiC.PROPERTY_LEFT_BUTTON:
            removeHoldedProxy(TiC.PROPERTY_LEFT_VIEW);
            if (infoView != null) {
                infoView.setLeftOrRightPane(newValue,
                        AkylasMapInfoView.LEFT_PANE);
            }
            break;
        case TiC.PROPERTY_LEFT_VIEW: {
            setView(newValue, TiC.PROPERTY_LEFT_VIEW, AkylasMapInfoView.LEFT_PANE);
        }
        case TiC.PROPERTY_RIGHT_BUTTON:
            removeHoldedProxy(TiC.PROPERTY_RIGHT_VIEW);
            if (infoView != null) {
                infoView.setLeftOrRightPane(newValue,
                        AkylasMapInfoView.RIGHT_PANE);
            }
            break;
        case TiC.PROPERTY_RIGHT_VIEW: {
            setView(newValue, TiC.PROPERTY_RIGHT_VIEW, AkylasMapInfoView.RIGHT_PANE);
            break;
        }
        case TiC.PROPERTY_CUSTOM_VIEW: {
            setView(newValue, TiC.PROPERTY_CUSTOM_VIEW, AkylasMapInfoView.CUSTOM_VIEW);
            break;
        }
        case AkylasMapModule.PROPERTY_SORT_KEY: {
            if (newValue instanceof Comparable) {
                sortKey = (Comparable) newValue;
                invalidate();
            }
            break;
        }
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    protected void didProcessProperties() {
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_LOCATION) != 0) {
            setPosition(latitude, longitude, altitude);
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_LOCATION;
        }
        super.didProcessProperties();
    }

    private void invalidate() {
        if (marker != null) {
            marker.invalidate();
        }
    }

    // public AkylasMapInfoView createAndPrepareInfoWindow() {
    // AkylasMapInfoView infoView = new
    // AkylasMapInfoView(TiApplication.getInstance()
    // .getApplicationContext());
    // prepareInfoView(null);
    // return infoView;
    // }
    //
    // AkylasMapInfoView getOrCreateMapInfoView() {
    // if (infoView == null) {
    // createAndPrepareInfoWindow();
    // }
    // return infoView;
    // }

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
        return ((AkylasMapboxView)mapView).createInfoWindow(this);
    }

    public void mapbBoxInfoWindowDidClose(MabpoxInfoWindow mabpoxInfoWindow) {
        infoView = null;
        ((AkylasMapboxView)mapView).infoWindowDidClose(mabpoxInfoWindow);
    }
    
    public void googleInfoWindowDidClose() {
        ((AkylasMapView)mapView).infoWindowDidClose(infoView);
        infoView = null;
    }

    public void prepareInfoView(AkylasMapInfoView infoView) {
        prepareInfoView(infoView, getProperties());
    }

    public boolean hasContent() {
        return annoTitle != null || annoSubtitle != null
                || getHoldedProxy(TiC.PROPERTY_CUSTOM_VIEW) != null
                || getHoldedProxy(TiC.PROPERTY_LEFT_VIEW) != null
                || getHoldedProxy(TiC.PROPERTY_RIGHT_VIEW) != null;
    }
}