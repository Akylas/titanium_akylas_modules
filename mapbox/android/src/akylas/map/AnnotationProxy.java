/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import akylas.map.AkylasMarker;
import android.graphics.Bitmap;
import android.os.Message;
import android.view.View;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
        TiC.PROPERTY_SUBTITLE, TiC.PROPERTY_SUBTITLEID, TiC.PROPERTY_TITLE,
        TiC.PROPERTY_TITLEID, TiC.PROPERTY_LATITUDE, TiC.PROPERTY_LONGITUDE,
        AkylasMapModule.PROPERTY_DRAGGABLE, TiC.PROPERTY_IMAGE,
        TiC.PROPERTY_PINCOLOR, AkylasMapModule.PROPERTY_CUSTOM_VIEW,
        TiC.PROPERTY_LEFT_BUTTON, TiC.PROPERTY_LEFT_VIEW,
        TiC.PROPERTY_RIGHT_BUTTON, TiC.PROPERTY_RIGHT_VIEW })
public class AnnotationProxy extends KrollProxy {
    private static final String TAG = "AnnotationProxy";

    private MarkerOptions markerOptions;
    private AkylasMarker marker;
    private AkylasMapInfoWindow infoWindow = null;
    private static final String defaultIconImageHeight = "40dip"; // The height
                                                                  // of the
                                                                  // default
                                                                  // marker icon
    // The height of the marker icon in the unit of "px". Will use it to analyze
    // the touch event to find out
    // the correct clicksource for the click event.
    private int iconImageHeight = 0;
    private String annoTitle;
    private String annoSubtitle;

    private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

    private static final int MSG_SET_LON = MSG_FIRST_ID + 300;
    private static final int MSG_SET_LAT = MSG_FIRST_ID + 301;
    private static final int MSG_SET_DRAGGABLE = MSG_FIRST_ID + 302;
    private static final int MSG_UPDATE_INFO_WINDOW = MSG_FIRST_ID + 303;

    public AnnotationProxy() {
        super();
        annoTitle = "";
        markerOptions = new MarkerOptions();
        // defaultValues.put(AkylasMapboxModule.PROPERTY_SHOW_INFO_WINDOW,
        // true);
    }

    public AnnotationProxy(TiContext tiContext) {
        this();
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
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
        markerOptions.position(new LatLng(latitude, longitude));
        if (marker != null) {
            marker.setPosition(latitude, longitude);
        }
    }
    
    private void prepareInfoWindow(KrollDict dict) {
        if (infoWindow == null) return;
        if (dict == null) dict = getProperties();
        infoWindow.setTitle(annoTitle);
        infoWindow.setSubtitle(annoSubtitle);
        
        Object leftButton = dict.get(TiC.PROPERTY_LEFT_BUTTON);
        Object leftView = dict.get(TiC.PROPERTY_LEFT_VIEW);
        Object rightButton = dict.get(TiC.PROPERTY_RIGHT_BUTTON);
        Object rightView = dict.get(TiC.PROPERTY_RIGHT_VIEW);
        if (leftButton != null) {
            infoWindow.setLeftOrRightPane(leftButton,
                    AkylasMapInfoWindow.LEFT_PANE);
        } else {
            infoWindow.setLeftOrRightPane(leftView,
                    AkylasMapInfoWindow.LEFT_PANE);
        }
        if (rightButton != null) {
            infoWindow.setLeftOrRightPane(rightButton,
                    AkylasMapInfoWindow.RIGHT_PANE);
        } else {
            infoWindow.setLeftOrRightPane(rightView,
                    AkylasMapInfoWindow.RIGHT_PANE);
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
        prepareInfoWindow(dict);

        if (dict.containsKey(AkylasMapModule.PROPERTY_DRAGGABLE)) {
            markerOptions
                    .draggable(TiConvert
                            .toBoolean(getProperty(AkylasMapModule.PROPERTY_DRAGGABLE)));
        }

        // customView, image and pincolor must be defined before adding to
        // mapview. Once added, their values are final.
        if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
            handleCustomView(dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW));
        } else if (dict.containsKey(TiC.PROPERTY_IMAGE)) {
            handleImage(dict.get(TiC.PROPERTY_IMAGE));
        } else if (dict.containsKey(TiC.PROPERTY_PINCOLOR)) {
            Object value = dict.get(TiC.PROPERTY_PINCOLOR);
            if (value instanceof Number) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(TiConvert
                        .toFloat(getProperty(TiC.PROPERTY_PINCOLOR))));
            }
           
            setIconImageHeight(-1);
        } else {
            setIconImageHeight(-1);
        }
    }

    private void handleCustomView(Object obj) {
        if (obj instanceof TiViewProxy) {
            TiBlob imageBlob = ((TiViewProxy) obj).toImage(null, 1);
            if (imageBlob != null) {
                Bitmap image = ((TiBlob) imageBlob).getImage();
                if (image != null) {
                    markerOptions.icon(BitmapDescriptorFactory
                            .fromBitmap(image));
                    setIconImageHeight(image.getHeight());
                    return;
                }
            }
        }
        Log.w(TAG, "Unable to get the image from the custom view: " + obj);
        setIconImageHeight(-1);
    }

    private void handleImage(Object image) {
        // image path
        if (image instanceof String) {
            TiDrawableReference imageref = TiDrawableReference.fromUrl(this,
                    (String) image);
            Bitmap bitmap;
            bitmap = imageref.getBitmap();
            if (bitmap != null) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                setIconImageHeight(bitmap.getHeight());
                return;
            }
        }
        Log.w(TAG, "Unable to get the image from the path: " + image);
        setIconImageHeight(-1);
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
        marker.showInfoWindow();
    }

    public void hideInfo() {
        if (marker == null) {
            return;
        }
        marker.hideInfoWindow();

    }

    public AkylasMapInfoWindow getMapInfoWindow() {
        return infoWindow;
    }

    private void setIconImageHeight(int h) {
        if (h >= 0) {
            iconImageHeight = h;
        } else { // default maker icon
            TiDimension dimension = new TiDimension(defaultIconImageHeight,
                    TiDimension.TYPE_UNDEFINED);
            // TiDimension needs a view to grab the window manager, so we'll
            // just use the decorview of the current window
            View view = TiApplication.getAppCurrentActivity().getWindow()
                    .getDecorView();
            iconImageHeight = dimension.getAsPixels(view);
        }
    }

    public int getIconImageHeight() {
        return iconImageHeight;
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
                        AkylasMapInfoWindow.LEFT_PANE);
                if (value == null) {
                    Object leftView = getProperty(TiC.PROPERTY_LEFT_VIEW);
                    if (leftView != null) {
                        infoWindow.setLeftOrRightPane(leftView,
                                AkylasMapInfoWindow.LEFT_PANE);
                    }
                }
            } else if (name.equals(TiC.PROPERTY_LEFT_VIEW)
                    && getProperty(TiC.PROPERTY_LEFT_BUTTON) == null) {
                infoWindow.setLeftOrRightPane(value,
                        AkylasMapInfoWindow.LEFT_PANE);
            } else if (name.equals(TiC.PROPERTY_RIGHT_BUTTON)) {
                getOrCreateMapInfoWindow().setLeftOrRightPane(value,
                        AkylasMapInfoWindow.RIGHT_PANE);
                if (value == null) {
                    Object rightView = getProperty(TiC.PROPERTY_RIGHT_VIEW);
                    if (rightView != null) {
                        getOrCreateMapInfoWindow().setLeftOrRightPane(
                                rightView, AkylasMapInfoWindow.LEFT_PANE);
                    }
                }
            } else if (name.equals(TiC.PROPERTY_RIGHT_VIEW)
                    && getProperty(TiC.PROPERTY_RIGHT_BUTTON) == null) {
                infoWindow.setLeftOrRightPane(value,
                        AkylasMapInfoWindow.RIGHT_PANE);
            } else if (name.equals(AkylasMapModule.PROPERTY_DRAGGABLE)) {
                TiMessenger.sendBlockingMainMessage(getMainHandler()
                        .obtainMessage(MSG_SET_DRAGGABLE), TiConvert
                        .toBoolean(value));
            }
            updateInfoWindow();
        }

    }

    AkylasMapInfoWindow getOrCreateMapInfoWindow() {
        if (infoWindow == null) {
            infoWindow = new AkylasMapInfoWindow(TiApplication.getInstance()
                    .getApplicationContext());
            prepareInfoWindow(null);
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