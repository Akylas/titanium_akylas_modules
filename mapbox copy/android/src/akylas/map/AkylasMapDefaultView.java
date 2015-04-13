package akylas.map;

/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUINonViewGroupView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;

import akylas.map.AnnotationProxy;
import akylas.map.RouteProxy;
import akylas.map.AkylasMarker;
import android.view.MotionEvent;
import android.view.View;

abstract class AkylasMapDefaultView extends TiUINonViewGroupView {
    private static final String TAG = "AkylasMapDefaultView";
    
    protected class InfoWindowCache extends SoftCache{

        public InfoWindowCache() {
            super();
        }

        @Override
        public Object runWhenCacheEmpty(String key) {
            if (key.equals("window")) {
                return new MabpoxInfoWindow(getProxy().getActivity()); //new view;
            } else if (key.equals("infoView")) {
                return new AkylasMapInfoView(getProxy().getActivity()); //new view;
            }
            return null;
        }   
    }

    protected boolean animate = true;
    protected boolean preLayout = true;
//    protected ArrayList<AkylasMarker> timarkers;
    protected AnnotationProxy selectedAnnotation;
    protected boolean regionFit = false;
    protected boolean shouldFollowUserLocation = true;
    
    protected boolean _calloutUsesTemplates = false;
    private String defaultTemplateBinding;
    private HashMap<String, TiViewTemplate> templatesByBinding;
    protected InfoWindowCache mInfoWindowCache = new InfoWindowCache();
    

    public AkylasMapDefaultView(final TiViewProxy proxy) {
        super(proxy);
//        timarkers = new ArrayList<AkylasMarker>();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // to prevent double events
        return false;
    }

//    @Override
//    public void processProperties(KrollDict d) {
//        super.processProperties(d);
//        processMapProperties(d);
//    }
    
    @Override
    public void release() {
        mInfoWindowCache.clear();
        super.release();
    }
    
    protected static final ArrayList<String> KEY_SEQUENCE;
    static{
      ArrayList<String> tmp = new ArrayList<String>();
      tmp.add(AkylasMapModule.PROPERTY_ANIMATE_CHANGES);
//      tmp.add(AkylasMapModule.PROPERTY_CALLOUT_TEMPLATES);
//      tmp.add(AkylasMapModule.PROPERTY_CALLOUT_USE_TEMPLATES);
//      tmp.add(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_TEMPLATE);
      tmp.add(AkylasMapModule.PROPERTY_MAX_ANNOTATIONS);
      tmp.add(AkylasMapModule.PROPERTY_REGION_FIT);
      tmp.add(AkylasMapModule.PROPERTY_TILE_SOURCE);
//      tmp.add(AkylasMapModule.PROPERTY_ANNOTATIONS);
//      tmp.add(AkylasMapModule.PROPERTY_ROUTES);
      tmp.add(AkylasMapModule.PROPERTY_MINZOOM);
      tmp.add(AkylasMapModule.PROPERTY_MAXZOOM);
      tmp.add(AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT);
      tmp.add(AkylasMapModule.PROPERTY_ZOOM);
      tmp.add(TiC.PROPERTY_REGION);
      tmp.add(AkylasMapModule.PROPERTY_CENTER_COORDINATE);
//      tmp.add(AkylasMapModule.PROPERTY_USER_TRACKING_MODE);
//      tmp.add(AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED);
      KEY_SEQUENCE = tmp;
    }
    
    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasMapModule.PROPERTY_CALLOUT_TEMPLATES:
            processTemplates((HashMap)newValue);
            break;
        case AkylasMapModule.PROPERTY_CALLOUT_USE_TEMPLATES:
            _calloutUsesTemplates = TiConvert.toBoolean(newValue, false);
            break;
        case AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_TEMPLATE:
            defaultTemplateBinding = TiConvert.toString(newValue);
            break;
        case AkylasMapModule.PROPERTY_MAX_ANNOTATIONS:
            ((MapDefaultViewProxy)proxy).maxAnnotations = TiConvert.toInt(newValue, 0);
            break;
        case AkylasMapModule.PROPERTY_REGION_FIT:
            regionFit = TiConvert.toBoolean(newValue, false);
            break;
        case AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT:
            updateScrollableAreaLimit(TiConvert.toKrollDict(newValue));
            break;
        case AkylasMapModule.PROPERTY_ZOOM:
            changeZoomLevel(TiConvert.toFloat(newValue), shouldAnimate());
            break;
        case AkylasMapModule.PROPERTY_ANIMATE_CHANGES:
            animate = TiConvert.toBoolean(newValue, false);
            break;
        case TiC.PROPERTY_REGION:
            updateRegion(newValue, shouldAnimate());
            break;
        case AkylasMapModule.PROPERTY_CENTER_COORDINATE:
            updateCenter(newValue, shouldAnimate());
            break;
        case AkylasMapModule.PROPERTY_TILE_SOURCE:
            if (changedProperty) {
                removeAllTileSources();
            }
            setTileSources(newValue);
            break;
        case AkylasMapModule.PROPERTY_ANNOTATIONS:
            if (changedProperty) {
                removeAllAnnotations();
            }
            addAnnotations((Object[])newValue);
            break;
        case AkylasMapModule.PROPERTY_ROUTES:
            if (changedProperty) {
                removeAllRoutes();
            }
            addRoutes((Object[]) newValue);
            break;
        case AkylasMapModule.PROPERTY_MINZOOM:
            handleMinZoomLevel(TiConvert.toFloat(newValue));
            break;
        case AkylasMapModule.PROPERTY_MAXZOOM:
            handleMaxZoomLevel(TiConvert.toFloat(newValue));
            break;
        case AkylasMapModule.PROPERTY_USER_TRACKING_MODE:
            setUserTrackingMode(TiConvert.toInt(newValue, 0));
            break;
        case AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED:
            setUserLocationEnabled(TiConvert.toBoolean(newValue, false));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
    
//    public void processPreMapProperties(final KrollDict d) {
//        if (d.containsKey(AkylasMapModule.PROPERTY_CALLOUT_TEMPLATES)) {
//        } 
//        
//        if (d.containsKey(AkylasMapModule.PROPERTY_CALLOUT_USE_TEMPLATES)) {
//        } 
//        if (d.containsKey(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_TEMPLATE)) {
//            defaultTemplateBinding = d.getString(AkylasMapModule.PROPERTY_DEFAULT_CALLOUT_TEMPLATE);
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_MAX_ANNOTATIONS)) {
//            ((MapDefaultViewProxy)proxy).maxAnnotations = d.optInt(AkylasMapModule.PROPERTY_MAX_ANNOTATIONS, 0);
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_REGION_FIT)) {
//            regionFit = d.optBoolean(AkylasMapModule.PROPERTY_REGION_FIT,
//                    regionFit);
//        }
//    }
    
//    public void processMapPositioningProperties(final KrollDict d, final boolean animated) {
//        if (d.containsKey(AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
//            updateScrollableAreaLimit(d
//                    .getKrollDict(AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT));
//        }
//        
//        if (d.containsKey(AkylasMapModule.PROPERTY_ZOOM)) {
//            changeZoomLevel(
//                    TiConvert.toFloat(d, AkylasMapModule.PROPERTY_ZOOM),
//                    animated);
//        }
//
//        if (d.containsKey(TiC.PROPERTY_REGION)) {
//            updateRegion(d.get(TiC.PROPERTY_REGION), animated);
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_CENTER_COORDINATE)) {
//            updateCenter(d.get(AkylasMapModule.PROPERTY_CENTER_COORDINATE),
//                    animated);
//        }
//        
//    }

//    public void processPostMapProperties(final KrollDict d, final boolean animated) {
//        
//        if (d.containsKey(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
//            setTileSources(d.get(AkylasMapModule.PROPERTY_TILE_SOURCE));
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_ANNOTATIONS)) {
//            ((MapDefaultViewProxy)proxy).addAnnotations(d.get(AkylasMapModule.PROPERTY_ANNOTATIONS));
//        }
//        if (d.containsKey(AkylasMapModule.PROPERTY_ROUTES)) {
//            Object[] routes = (Object[]) d.get(AkylasMapModule.PROPERTY_ROUTES);
//            addRoutes(routes);
//        }
//        
//
//        if (d.containsKey(AkylasMapModule.PROPERTY_MINZOOM)) {
//            handleMinZoomLevel(TiConvert.toFloat(d,
//                    AkylasMapModule.PROPERTY_MINZOOM));
//        }
//
//        if (d.containsKey(AkylasMapModule.PROPERTY_MAXZOOM)) {
//            handleMaxZoomLevel(TiConvert.toFloat(d,
//                    AkylasMapModule.PROPERTY_MAXZOOM));
//        }
//
//        processMapPositioningProperties(d, animated);
//        
//        // the order is important !
//        if (d.containsKey(AkylasMapModule.PROPERTY_USER_TRACKING_MODE)) {
//            setUserTrackingMode(TiConvert.toInt(d,
//                    AkylasMapModule.PROPERTY_USER_TRACKING_MODE, 0));
//        }
//
//        if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED)) {
//            setUserLocationEnabled(TiConvert.toBoolean(d,
//                    AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED, false));
//        }
//    }
    
    
    protected boolean shouldAnimate() {
        return animate && proxy.viewInitialised();
    }
//    public void processMapProperties(final KrollDict d) {
//        if (d.containsKey(AkylasMapModule.PROPERTY_ANIMATE_CHANGES)) {
//            animate = d.optBoolean(AkylasMapModule.PROPERTY_ANIMATE_CHANGES, animate);
//        }
//        boolean animated = animate && proxy.viewInitialised();
//        processPreMapProperties(d);
//        processPostMapProperties(d, animated);
//    }

//    @Override
//    public void propertyChanged(String key, Object oldValue, Object newValue,
//            KrollProxy proxy) {
//
//        if (key.equals(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
//            setTileSources(newValue);
//        } else if (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED)) {
//            setUserLocationEnabled(TiConvert.toBoolean(newValue));
//        } else if (key.equals(AkylasMapModule.PROPERTY_USER_TRACKING_MODE)) {
//            setUserTrackingMode(TiConvert.toInt(newValue, 0));
//        } else if (key.equals(AkylasMapModule.PROPERTY_MAX_ANNOTATIONS)) {
//            ((MapDefaultViewProxy)proxy).maxAnnotations = TiConvert.toInt(newValue, 0);
//        } else if (key.equals(TiC.PROPERTY_REGION)) {
//            updateRegion(newValue, true);
//        } else if (key.equals(AkylasMapModule.PROPERTY_CENTER_COORDINATE)) {
//            updateCenter(newValue, true);
//        } else if (key.equals(AkylasMapModule.PROPERTY_ZOOM)) {
//            changeZoomLevel(TiConvert.toFloat(newValue), true);
//        } else if (key.equals(AkylasMapModule.PROPERTY_ANIMATE_CHANGES)) {
//            animate = TiConvert.toBoolean(newValue, animate);
//        } else if (key.equals(AkylasMapModule.PROPERTY_REGION_FIT)) {
//            regionFit = TiConvert.toBoolean(newValue, regionFit);
//        } else if (key.equals(TiC.PROPERTY_ANNOTATIONS)) {
//            ((MapDefaultViewProxy)proxy).setAnnotations(newValue);
//        } else if (key.equals(AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
//            updateScrollableAreaLimit(newValue);
//        } else {
//            super.propertyChanged(key, oldValue, newValue, proxy);
//        }
//    }

    abstract public KrollDict getUserLocation();
    abstract boolean getUserLocationEnabled();
    abstract int getUserTrackingMode();
    abstract void handleMinZoomLevel(final float level);
    abstract void handleMaxZoomLevel(final float level);
    abstract void changeZoomLevel(final float level, final boolean animated);
    abstract void setUserLocationEnabled(boolean enabled);
    abstract void setUserTrackingMode(int mode);
    abstract float getMaxZoomLevel();
    abstract float getMinZoomLevel();
    abstract float getZoomLevel();
    abstract void updateCenter(Object dict, final boolean animated);
    abstract void updateRegion(Object dict, final boolean animated);
    abstract void updateScrollableAreaLimit(Object dict);
    abstract void selectUserAnnotation();
    abstract void zoomIn();
    abstract void zoomIn(final LatLng about, final boolean userAction);
    abstract void zoomOut();
    abstract void zoomOut(final LatLng about, final boolean userAction);
    abstract KrollDict getRegionDict();




//    public AkylasMarker findMarkerByTitle(String title) {
//        for (int i = 0; i < timarkers.size(); i++) {
//            AkylasMarker timarker = timarkers.get(i);
//            AnnotationProxy annoProxy = timarker.getProxy();
//            if (annoProxy != null && annoProxy.getTitle().equals(title)) {
//                return timarker;
//            }
//        }
//        return null;
//    }

    abstract void handleDeselectMarker(final AkylasMarker marker); 
    abstract void handleSelectMarker(final AkylasMarker marker); 
    abstract void handleAddRoute(final RouteProxy router); 
    abstract void handleRemoveRoute(final RouteProxy router); 
    abstract void handleAddAnnotation(final AnnotationProxy annotation); 
    abstract void handleRemoveMarker(final AkylasMarker marker); 
    

//    public void addAnnotation(Object object) {
//        if (object instanceof HashMap) {
//            object = KrollProxy.createProxy(AnnotationProxy.class, null,
//                    new Object[] { object }, null);
//        }
//        if (object instanceof AnnotationProxy) {
//            AnnotationProxy annotation = (AnnotationProxy) object;
//            handleAddAnnotation((AnnotationProxy) object);
//            return annotation;
//        }
//        return null;
//    }
    

    protected void selectAnnotation(Object annotation) {
        AkylasMarker marker = null;
        if (annotation instanceof AnnotationProxy) {
            AnnotationProxy proxy = (AnnotationProxy) annotation;
            marker = proxy.getMarker();
        } else if (annotation instanceof AkylasMarker) {
            marker = (AkylasMarker) annotation;
//        } else if (annotation instanceof String) {
//            String title = (String) annotation;
//            marker = findMarkerByTitle(title);
        }

        if (marker != null) {
            selectedAnnotation = marker.getProxy();
            handleSelectMarker(marker);
        }
    }

    protected void deselectAnnotation(Object annotation) {
        AkylasMarker marker = null;
        if (annotation instanceof AnnotationProxy) {
            AnnotationProxy proxy = (AnnotationProxy) annotation;
            marker = proxy.getMarker();
        } else if (annotation instanceof AkylasMarker) {
            marker = (AkylasMarker) annotation;
//        } else if (annotation instanceof String) {
//            String title = (String) annotation;
//            marker = findMarkerByTitle(title);
        }

        if (marker != null) {
            if (selectedAnnotation == marker.getProxy()) {
                handleDeselectMarker(marker);
                selectedAnnotation = null;
            }
        }
        
    }
    
    public RouteProxy addRoute(Object object) {
        if (object instanceof HashMap) {
            object = KrollProxy.createProxy(RouteProxy.class, null,
                    new Object[] { object }, null);
        }
        if (object instanceof RouteProxy) {
            RouteProxy route = (RouteProxy) object;
            handleAddRoute(route);
            return route;
        }
        return null;
    }
    
    public ArrayList<RouteProxy> addRoutes(Object[] routes) {
        ArrayList<RouteProxy> result = new ArrayList<RouteProxy>();
        for (int i = 0; i < routes.length; i++) {
            RouteProxy route = addRoute(routes[i]);
            if (route != null) {
                result.add(route);
            }
        }
        return result;
    }

    public void removeRoute(Object object) {
        if (object instanceof Object[]) {
            for (Object obj : (Object[]) object) {
                removeRoute(obj);
            }
            return;
        }
        if (object instanceof RouteProxy) {
            RouteProxy r = (RouteProxy) object;
            handleRemoveRoute(r);
        }
    }
    
    public AnnotationProxy addAnnotation(Object object) {
        if (object instanceof HashMap) {
            object = KrollProxy.createProxy(AnnotationProxy.class, null,
                    new Object[] { object }, null);
        }
        if (object instanceof AnnotationProxy) {
            AnnotationProxy annotation = (AnnotationProxy) object;
            handleAddAnnotation(annotation);
            return annotation;
        }
        return null;
    }
    
    public ArrayList<AnnotationProxy> addAnnotations(Object[] annotations) {
        ArrayList<AnnotationProxy> result = new ArrayList<AnnotationProxy>();
        for (int i = 0; i < annotations.length; i++) {
            AnnotationProxy annotation = addAnnotation(annotations[i]);
            if (annotation != null) {
                result.add(annotation);
            }
        }
        return result;
    }
    
    

    public void removeAnnotation(Object object) {
        if (object == null) return;
        if (object instanceof Object[]) {
            for (Object obj : (Object[]) object) {
                removeAnnotation(obj);
            }
            return;
        }
        AkylasMarker timarker = null;
        if (object instanceof AkylasMarker) {
            timarker = (AkylasMarker) object;
        } else if (object instanceof AnnotationProxy) {
            timarker = ((AnnotationProxy) object).getMarker();
//        } else if (annotation instanceof String) {
//            timarker = findMarkerByTitle((String) annotation);
        }

        if (timarker != null) {
            handleRemoveMarker(timarker);
        }
    }
    protected void removeAllAnnotations() {}
    protected void removeAllRoutes() {}
    protected void removeAllTileSources() {}


    private AnnotationProxy getProxyByMarker(AkylasMarker m) {
        if (m != null) {
            return m.getProxy();
//            for (int i = 0; i < timarkers.size(); i++) {
//                AkylasMarker timarker = timarkers.get(i);
//                if (m.equals(timarker)) {
//                    return timarker.getProxy();
//                }
//            }
        }
        return null;
    }

    

    protected void fireEventOnMap(String type, ILatLng point) {
        if (!hasListeners(type, false))
            return;
        KrollDict d = new KrollDict();
        d.put(TiC.PROPERTY_ALTITUDE, point.getAltitude());
        d.put(TiC.PROPERTY_LATITUDE, point.getLatitude());
        d.put(TiC.PROPERTY_LONGITUDE, point.getLongitude());
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasMapModule.PROPERTY_MAP, proxy);
        fireEvent(type, d, false, false);
    }

    protected void fireEventOnMarker(String type, AkylasMarker marker, String clickSource) {
        
        AnnotationProxy annoProxy = marker.getProxy();
        if (!annoProxy.hasListeners(type, false))
            return;
        KrollDict d = new KrollDict();
        if (annoProxy != null) {
            d.put(TiC.PROPERTY_TITLE, annoProxy.getTitle());
            d.put(TiC.PROPERTY_SUBTITLE, annoProxy.getSubtitle());
        }
        d.put(TiC.PROPERTY_ALTITUDE, marker.getAltitude());
        d.put(TiC.PROPERTY_LATITUDE, marker.getLatitude());
        d.put(TiC.PROPERTY_LONGITUDE, marker.getLongitude());
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        if (marker instanceof AkylasMarker) {
            d.put(TiC.PROPERTY_ANNOTATION, ((AkylasMarker) marker).getProxy());
        }
        d.put(AkylasMapModule.PROPERTY_MAP, proxy);
        d.put(TiC.EVENT_PROPERTY_CLICKSOURCE, clickSource);
        fireEvent(type, d, false, false);

    }
    

    public void firePinChangeDragStateEvent(Marker marker,
            AnnotationProxy annoProxy, int dragState) {
        if (!annoProxy.hasListeners(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE, false))
            return;
        KrollDict d = new KrollDict();
        String title = null;
        // TiMapInfoWindow infoWindow = annoProxy.getMapInfoWindow();
        // if (infoWindow != null) {
        // title = infoWindow.getTitle();
        // }
        d.put(TiC.PROPERTY_TITLE, title);
        d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
        d.put(AkylasMapModule.PROPERTY_MAP, proxy);
        d.put(TiC.PROPERTY_SOURCE, proxy);
        d.put(AkylasMapModule.PROPERTY_NEWSTATE, dragState);
        d.put(TiC.PROPERTY_TYPE, AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE);
        proxy.fireEvent(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE, d, false, false);
    }
    
    public boolean calloutUseTemplates() {
        return _calloutUsesTemplates;
    }
    
    public TiViewTemplate getTemplate(String template)
    {
        if (template == null) template = defaultTemplateBinding;
        if (templatesByBinding.containsKey(template))
        {
            return templatesByBinding.get(template);
        }
        return null;
    }
    public CalloutReusableProxy reusableViewFromDict(KrollDict dict, final KrollDict extraData) {
        TiViewTemplate template = getTemplate(dict.getString("template"));
        
        if (template != null) {
            Object view  = mInfoWindowCache.get(template.getTemplateID());
            
            if (view != null && view instanceof ReusableView) {
                CalloutReusableProxy proxy = (CalloutReusableProxy) ((ReusableView) view).getProxy();
                proxy.populateViews(dict, (ReusableView) view, template, extraData, true);
                return proxy;
            }
            else {
                CalloutReusableProxy proxy = (CalloutReusableProxy) template.generateProxy(CalloutReusableProxy.class, dict, this.proxy);
                proxy.generateContent(dict, template, extraData);
                return proxy;
            }
        }
        return null;
    }
    

    
    protected void processTemplates(HashMap<String,Object> templates) {
        templatesByBinding = new HashMap<String, TiViewTemplate>();
        if(templates != null) {
            for (String key : templates.keySet()) {
                HashMap templateDict = (HashMap)templates.get(key);
                if (templateDict != null) {
                    //Here we bind each template with a key so we can use it to look up later
                    KrollDict properties = new KrollDict((HashMap)templates.get(key));
                    TiViewTemplate template = new TiViewTemplate(key, properties);
                    templatesByBinding.put(key, template);
                }
                else {
                    Log.e(TAG, "null template definition: " + key);
                }
            }
        }
    }
    

    

    
    
    protected void setTileSources(Object sources) {
        if (sources instanceof Object[]) {
            int length = Array.getLength(sources);
            for (int i = 0; i < length; i++) {
                addTileSource(((Object[]) sources)[i], i);
            }
        } else if (sources instanceof List) {
            for (int i = 0; i < ((List) sources).size(); i++) {
                addTileSource(((List) sources).get(i), i);
            }
        } else {
            addTileSource(sources, 0);
        }
    }
    
    
    public TileSourceProxy addTileSource(Object object, int index) {
        return null;
    }

    public void removeTileSource(Object object) {
    }
    
    protected void setShouldFollowUserLocation(final boolean value) {
        if (shouldFollowUserLocation != value) {
            shouldFollowUserLocation = value;
            if (proxy.hasListeners(AkylasMapModule.EVENT_FOLLOW_LOCATION,
                    false)) {
                KrollDict d = new KrollDict();
                d.put(TiC.PROPERTY_VALUE, shouldFollowUserLocation);
                proxy.fireEvent(AkylasMapModule.EVENT_FOLLOW_LOCATION, d,
                        false, false);
            }
        }
    }
}
