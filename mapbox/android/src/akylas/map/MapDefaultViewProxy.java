/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.geometry.LatLng;

import akylas.map.AnnotationProxy;
import akylas.map.RouteProxy;
import akylas.map.TileSourceProxy;
import android.app.Activity;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
    AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED,
    TiC.PROPERTY_USER_LOCATION,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE, 
    AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
    AkylasMapModule.PROPERTY_REGION_FIT,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE,
    AkylasMapModule.PROPERTY_ZOOM,
    AkylasMapModule.PROPERTY_MINZOOM,
    AkylasMapModule.PROPERTY_MAXZOOM,
    AkylasMapModule.PROPERTY_ROUTES,
    TiC.PROPERTY_REGION,
    TiC.PROPERTY_ANNOTATIONS, 
    AkylasMapModule.PROPERTY_ANIMATE_CHANGES,
    TiC.PROPERTY_ENABLE_ZOOM_CONTROLS })
public abstract class MapDefaultViewProxy extends TiViewProxy {
	private static final String TAG = "MapDefaultViewProxy";

	private static final int MSG_FIRST_ID = TiViewProxy.MSG_LAST_ID + 1;

	private static final int MSG_ADD_ANNOTATION = MSG_FIRST_ID + 500;
	private static final int MSG_REMOVE_ANNOTATION = MSG_FIRST_ID + 502;
	private static final int MSG_REMOVE_ALL_ANNOTATIONS = MSG_FIRST_ID + 504;
	private static final int MSG_SELECT_ANNOTATION = MSG_FIRST_ID + 505;
	private static final int MSG_DESELECT_ANNOTATION = MSG_FIRST_ID + 506;
	private static final int MSG_ADD_ROUTE = MSG_FIRST_ID + 507;
	private static final int MSG_REMOVE_ROUTE = MSG_FIRST_ID + 508;
	private static final int MSG_CHANGE_ZOOM = MSG_FIRST_ID + 509;
	private static final int MSG_MAX_ZOOM = MSG_FIRST_ID + 511;
	private static final int MSG_MIN_ZOOM = MSG_FIRST_ID + 512;
    private static final int MSG_SELECT_USER_ANNOTATION = MSG_FIRST_ID + 513;
    private static final int MSG_GET_USER_LOCATION_ENABLED = MSG_FIRST_ID + 514;
    private static final int MSG_GET_USER_LOCATION = MSG_FIRST_ID + 515;
    public static final int MSG_LAST_ID = MSG_GET_USER_LOCATION;

    private ArrayList<RouteProxy> preloadRoutes = null;
    private ArrayList<AnnotationProxy> preloadAnnotations = null;

	public MapDefaultViewProxy() {
		super();
		
        preloadAnnotations = new ArrayList<AnnotationProxy>();
	}
	
	public void processPreloaded() {
	    //we set the properties and clear because if the activity is killed by android
	    //we still want to be able to reload everything again
	    if (preloadRoutes != null && preloadRoutes.size() > 0) {
	        setProperty(AkylasMapModule.PROPERTY_ROUTES, preloadRoutes.toArray());
            preloadRoutes.clear();
	    }

        if (preloadAnnotations != null && preloadAnnotations.size() > 0) {
            setProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, preloadAnnotations.toArray());
            preloadAnnotations.clear();
        }
	}

	public abstract TiUIView createView(Activity activity);
	
	@Override
	public boolean handleMessage(Message msg) {
		AsyncResult result = null;
		switch (msg.what) {

		case MSG_ADD_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			KrollDict dict = (KrollDict)result.getArg();
            handleInsertAnnotationAt(dict.optInt("index", -1), dict.get("object"));
			result.setResult(null);
			return true;
		}

		case MSG_REMOVE_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			handleRemoveAnnotation(result.getArg());
			result.setResult(null);
			return true;
		}

		case MSG_REMOVE_ALL_ANNOTATIONS: {
			result = (AsyncResult) msg.obj;
			handleRemoveAllAnnotations();
			result.setResult(null);
			return true;
		}

		case MSG_SELECT_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			handleSelectAnnotation(result.getArg());
			result.setResult(null);
			return true;
		}
		
		case MSG_SELECT_USER_ANNOTATION: {
            result = (AsyncResult) msg.obj;
            handleSelectUserAnnotation();
            result.setResult(null);
            return true;
        }
		
		case MSG_DESELECT_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			handleDeselectAnnotation(result.getArg());
			result.setResult(null);
			return true;
		}

		case MSG_ADD_ROUTE: {
			result = (AsyncResult) msg.obj;
			KrollDict dict = (KrollDict)result.getArg();
            handleInsertRouteAt(dict.optInt("index", -1), dict.get("object"));
			result.setResult(null);
			return true;
		}

		case MSG_REMOVE_ROUTE: {
			result = (AsyncResult) msg.obj;
			handleRemoveRoute((RouteProxy) result.getArg());
			result.setResult(null);
			return true;
		}

		case MSG_MAX_ZOOM: {
			result = (AsyncResult) msg.obj;
			result.setResult(getMaxZoomInternal());
			return true;
		}

		case MSG_MIN_ZOOM: {
			result = (AsyncResult) msg.obj;
			result.setResult(getMinZoomInternal());
			return true;
		}
		
		case MSG_GET_USER_LOCATION_ENABLED: {
            result = (AsyncResult) msg.obj;
            result.setResult(getUserLocationEnabledInternal());
            return true;
        }

		case MSG_CHANGE_ZOOM: {
			handleZoom(msg.arg1);
			return true;
		}

		default: {
			return super.handleMessage(msg);
		}
		}
	}

    @Kroll.method
    public void addAnnotation(Object annotation) {
        if (annotation == null) return;
        if (TiApplication.isUIThread()) {
            handleInsertAnnotationAt(-1, annotation);
        } else {
            sendIndexMessage(MSG_ADD_ANNOTATION, -1, annotation);
        }
    }

	@Kroll.method
	public void addAnnotations(Object[] annos) {
		addAnnotation(annos);
	}

	@Kroll.method
	public void removeAllAnnotations() {
		// Update the JS object
		setProperty(TiC.PROPERTY_ANNOTATIONS, new Object[0]);

		if (TiApplication.isUIThread()) {
			handleRemoveAllAnnotations();
		} else {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(
					MSG_REMOVE_ALL_ANNOTATIONS));
		}
	}
	

	public void handleRemoveAllAnnotations() {
	    AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.removeAllAnnotations();
        }
        preloadAnnotations.clear();
        setProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, preloadAnnotations);
	}

	public boolean isAnnotationValid(Object annotation) {
		// Incorrect argument types
		if (!(annotation instanceof AnnotationProxy || annotation instanceof String)) {
			Log.e(TAG, "Unsupported argument type for removeAnnotation");
			return false;
		}
		// Marker isn't on the map
		if (annotation instanceof AnnotationProxy
				&& ((AnnotationProxy) annotation).getMarker() == null) {
			return false;
		}

		if (annotation instanceof String) {
			AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
			if (mapView != null) {
				if (mapView.findMarkerByTitle((String) annotation) == null) {
					return false;
				}
			}
		}

		return true;
	}

	@Kroll.method
	public void removeAnnotation(Object annotation) {

		if (TiApplication.isUIThread()) {
			handleRemoveAnnotation(annotation);
		} else {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_REMOVE_ANNOTATION),
					annotation);
		}
	}

	@Kroll.method
	public void removeAnnotations(Object annos) {

		removeAnnotation(annos);
	}

	@Kroll.method
	public void selectAnnotation(Object annotation) {
		if (!isAnnotationValid(annotation)) {
			return;
		}

		if (TiApplication.isUIThread()) {
			handleSelectAnnotation(annotation);
		} else {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_SELECT_ANNOTATION),
					annotation);
		}
	}
	
	@Kroll.method
    public void selectUserAnnotation() {
        if (TiApplication.isUIThread()) {
            handleSelectUserAnnotation();
        } else {
            TiMessenger.sendBlockingMainMessage(
                    getMainHandler().obtainMessage(MSG_SELECT_USER_ANNOTATION));
        }
    }

    @Kroll.method
	@Kroll.getProperty
	public KrollDict getUserLocation() {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getUserLocation();
		}
		return null;
	}

	public void handleSelectAnnotation(Object annotation) {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			mapView.selectAnnotation(annotation);
		}
	}
	   
    private void handleSelectUserAnnotation() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.selectUserAnnotation();
        }
    }

	@Kroll.method
	public void deselectAnnotation(Object annotation) {
		if (!isAnnotationValid(annotation)) {
			return;
		}

		if (TiApplication.isUIThread()) {
			handleDeselectAnnotation(annotation);
		} else {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_DESELECT_ANNOTATION),
					annotation);
		}
	}

	public void handleDeselectAnnotation(Object annotation) {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			mapView.deselectAnnotation(annotation);
		}
	}

	@Kroll.method
	public void addRoute(Object route) {
        if (route == null) return;
		if (TiApplication.isUIThread()) {
		    handleInsertRouteAt(-1, route);
		} else {
		    sendIndexMessage(MSG_ADD_ROUTE, -1, route);
		}
	}
	
	
	private void handleInsertRouteAt(int index, Object route) {
	    AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            Object result = mapView.addRoute(route);
            if (result != null) {
                addToProperty(AkylasMapModule.PROPERTY_ROUTES, index, result);
            }
        } else {
            addPreloadRoute(route, index, false);
        }
    }
	
	protected void addToProperty(final String property, int index, final Object object) {
	    Object propertyValue = getProperty(property);
        Object[] array = null;
        if (propertyValue != null) {
    	    if (!(propertyValue instanceof Object[])) {
    	        array = new Object[] {propertyValue};
    	    }
    	    else {
    	        array = (Object[]) propertyValue;
    	    }
        }
	    boolean isArray = object instanceof Object[];
        if (array == null) {
            if (isArray) {
                array = (Object[]) object;
            }
            else {
                array = new Object[] {object};
            }
            setProperty(property, array);
        }
        else {
            ArrayList<Object> newObj = new ArrayList<Object>(Arrays.asList(array));
            if (isArray) {
                if (index >= 0 && index < newObj.size()) {
                    newObj.addAll(index, Arrays.asList(object));
                }
                else {
                    newObj.addAll(Arrays.asList(object));
                }
            }
            else {
                if (index >= 0 && index < newObj.size()) {
                    newObj.add(index, object);
                }
                else {
                    newObj.add(object);
                }
            }
            setProperty(property, newObj.toArray());
        }
	}
	
	protected void removeFromProperty(final String property, final Object object) {
	    Object[] array = (Object[]) getProperty(property);

        if (array == null) {
            return;
        }
        ArrayList<Object> newObj = new ArrayList<Object>(Arrays.asList(array));
        boolean needsUpdate = false;
        if (object instanceof Number) {
            needsUpdate = newObj.remove(((Number)object).intValue()) != null;
        }
        else {
            needsUpdate = newObj.remove(object);
        }
        if (needsUpdate) {
            setProperty(property, newObj.toArray());
        }
	}
	
	private void handleInsertAnnotationAt(int index, Object annotation) {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            Object result = null;
            if (annotation instanceof Object[]) {
                result = mapView.addAnnotations((Object[]) annotation);
            }
            else {
                result = mapView.addAnnotation(annotation);
            }
            if (result != null) {
                addToProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, index, result);
            }
        } else {
            addPreloadAnnotation(annotation, index, false);
        }
    }
	
    
    private void handleRemoveRoute(Object route) {
        if (route == null) return;
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.removeRoute(route);
            removeFromProperty(AkylasMapModule.PROPERTY_ROUTES, route);
        } else {
            deletePreloadObject(preloadRoutes, route);
        }
    }
    
    private void handleRemoveAnnotation(Object annotation) {
        if (annotation == null) return;
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.removeAnnotation(annotation);
            removeFromProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, annotation);
        } else {
            deletePreloadObject(preloadAnnotations, annotation);
        }
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private void addPreloadObject(Class objectClass, ArrayList preloadArray, Object object, int index) {
        if(object instanceof HashMap) {
            object =  KrollProxy.createProxy(objectClass, null, new Object[]{object}, null);
        } else if(object instanceof String && objectClass.equals(TileSourceProxy.class)) {
            KrollDict props = new KrollDict();
            props.put(TiC.PROPERTY_TYPE, object);
            object =  KrollProxy.createProxy(objectClass, null, new Object[]{props}, null);
        }
        if (objectClass.isInstance(object)) {
            if (index == -1) {
                preloadArray.add(object);
            } else {
                preloadArray.add(index, object);
            }
        }
    }
	
	@SuppressWarnings("rawtypes")
    public void addPreloadObject(Class objectClass, ArrayList preloadArray, Object value, int index, boolean arrayOnly) {
        if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            for (int i = 0; i < array.length; i++) {
                Object object = array[i];
                addPreloadObject(objectClass, preloadArray, object, -1);
            }
        } else if (!arrayOnly) {
            addPreloadObject(objectClass, preloadArray, value, -1);
        }
    }
	
	@SuppressWarnings("rawtypes")
    private void deletePreloadObjectAt(ArrayList preloadArray, int index) {
        if (index < 0 || index >= preloadArray.size()) {
            return;
        }
        preloadArray.remove(index);
    }
	
	@SuppressWarnings("rawtypes")
    protected void deletePreloadObject(ArrayList preloadArray, Object object) {
	    if (preloadArray == null) return;
	    if (object instanceof Object[]) {
            Object[] objectArray = (Object[]) object;
            for (int i = 0; i < objectArray.length; i++) {
                deletePreloadObject(preloadArray, objectArray[i]);
            }
        }
	    else if (object instanceof Number) {
            int index =((Number)object).intValue();
            deletePreloadObjectAt(preloadArray, index);
            return;
        }
        
	    int index = preloadArray.indexOf(object);
        if (index != -1) {
            preloadArray.remove(index);
        }
    }
	
	protected void sendIndexMessage(int msg, int index, Object object) {
        KrollDict data = new KrollDict();
        data.put("index", index);
        data.put("object", object);
        TiMessenger.sendBlockingMainMessage(
                getMainHandler().obtainMessage(msg), data);
    }
    
	public void addPreloadRoute(Object value, int index, boolean arrayOnly) {
        addPreloadObject(RouteProxy.class, getOrCreatePreloadRoutes(), value, index, arrayOnly);
	}
	public void addPreloadAnnotation(Object value, int index, boolean arrayOnly) {
        addPreloadObject(AnnotationProxy.class, getOrCreatePreloadAnnotations(), value, index, arrayOnly);
    }
	
	public ArrayList getOrCreatePreloadRoutes() {
	    if (preloadRoutes == null) {
	        preloadRoutes = new ArrayList<RouteProxy>();
	        if (hasProperty(AkylasMapModule.PROPERTY_ROUTES)) {
	            addPreloadObject(RouteProxy.class, preloadRoutes, getProperty(AkylasMapModule.PROPERTY_ROUTES), -1, false);
	        }
	    }
	    return preloadRoutes;
	}
	
	public ArrayList getOrCreatePreloadAnnotations() {
        if (preloadAnnotations == null) {
            preloadAnnotations = new ArrayList<AnnotationProxy>();
            if (hasProperty(AkylasMapModule.PROPERTY_ANNOTATIONS)) {
                addPreloadObject(RouteProxy.class, preloadAnnotations, getProperty(AkylasMapModule.PROPERTY_ANNOTATIONS), -1, false);
            }
        }
        return preloadAnnotations;
    }
//	
//	public void removePreloadRoute(Object value) {
//	    deletePreloadObject(preloadRoutes, value);
//    }
//
//    public void removePreloadAnnotation(Object value) {
//        deletePreloadObject(preloadAnnotations, value);
//    }

	public float getMaxZoomInternal() {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getMaxZoomLevel();
		} else {
			return 0;
		}
	}

	public float getMinZoomInternal() {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getMinZoomLevel();
		} else {
			return 0;
		}
	}
	
	public boolean getUserLocationEnabledInternal() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getUserLocationEnabled();
        } else {
            return false;
        }
    }

	@Kroll.method
	@Kroll.getProperty
	public float getMaxZoom() {
		if (TiApplication.isUIThread()) {
			return getMaxZoomInternal();
		} else {
			return (Float) TiMessenger.sendBlockingMainMessage(getMainHandler()
					.obtainMessage(MSG_MAX_ZOOM));
		}
	}

	@Kroll.method
	@Kroll.getProperty
	public float getMinZoom() {
		if (TiApplication.isUIThread()) {
			return getMinZoomInternal();
		} else {
			return (Float) TiMessenger.sendBlockingMainMessage(getMainHandler()
					.obtainMessage(MSG_MIN_ZOOM));
		}
	}
	
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getRegion() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getRegionDict();
		}
		return null;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public boolean getUserLocationEnabled() {
	    if (TiApplication.isUIThread()) {
            return getUserLocationEnabledInternal();
        } else {
            return (Boolean) TiMessenger.sendBlockingMainMessage(getMainHandler()
                    .obtainMessage(MSG_GET_USER_LOCATION_ENABLED));
        }
	}
	
	@Kroll.method
	@Kroll.getProperty
	public int getUserTrackingMode() {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getUserTrackingMode();
		}
		return TiConvert.toInt(getProperty(AkylasMapModule.PROPERTY_USER_TRACKING_MODE), 0);
	}

	@Kroll.method
	public void removeRoute(RouteProxy route) {
        if (route == null) return;
		if (TiApplication.isUIThread()) {
			handleRemoveRoute(route);
		} else {
			TiMessenger.sendBlockingMainMessage(
					getMainHandler().obtainMessage(MSG_REMOVE_ROUTE), route);

		}
	}
	

	public ArrayList<RouteProxy> getPreloadRoutes() {
		return preloadRoutes;
	}

	@Kroll.method
	public void zoom(int delta) {
		if (TiApplication.isUIThread()) {
			handleZoom(delta);
		} else {
			getMainHandler().obtainMessage(MSG_CHANGE_ZOOM, delta, 0)
					.sendToTarget();
		}
	}

	@Kroll.method
	public void zoomIn(@Kroll.argument(optional = true) final Object about) {
		final AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LatLng position = AkylasMapModule.latlongFromObject(about);
					if (position != null) {
						mapView.zoomIn(position, true);
					}
					else {
						mapView.zoomIn();
					}
				}
			});
		}
	}

	@Kroll.method
	public void zoomOut(@Kroll.argument(optional = true) final Object about) {
		final AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LatLng position = AkylasMapModule.latlongFromObject(about);
					if (position != null) {
						mapView.zoomOut(position, true);
					}
					else {
						mapView.zoomOut();
					}
				}
			});
		}
	}

	public void handleZoom(int delta) {
		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			mapView.changeZoomLevel(delta, true);
		}
	}
	
	@Kroll.method
    public void addTileSource(Object value, @Kroll.argument(optional = true) final Object indexObj) {
    }

    @Kroll.method
    public void removeTileSource(Object value) {
    }
}
