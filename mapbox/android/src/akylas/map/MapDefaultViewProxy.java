/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.geometry.LatLng;

import akylas.map.AnnotationProxy;
import akylas.map.RouteProxy;
import akylas.map.TileSourceProxy;
import android.app.Activity;

@Kroll.proxy()
public abstract class MapDefaultViewProxy extends TiViewProxy {
	private static final String TAG = "MapDefaultViewProxy";
	protected static final HashMap<String, Class> PRELOAD_CLASSES;
    static{
        PRELOAD_CLASSES = new HashMap<>();
        PRELOAD_CLASSES.put(AkylasMapModule.PROPERTY_ROUTES, RouteProxy.class);
        PRELOAD_CLASSES.put(AkylasMapModule.PROPERTY_ANNOTATIONS, AnnotationProxy.class);
        PRELOAD_CLASSES.put(AkylasMapModule.PROPERTY_TILE_SOURCE, TileSourceProxy.class);
    }

	private HashMap<String, ArrayList<Object>> preloadedProps;
//    private ArrayList<RouteProxy> preloadRoutes = null;
//    private ArrayList<Object> preloadSources = null;
    private List<TileSourceProxy> handledTileSources = new ArrayList<TileSourceProxy>();
    
    
    protected long maxAnnotations = 0;
//    protected ArrayList<AnnotationProxy> annotations = new ArrayList<AnnotationProxy>();
    
    

	public MapDefaultViewProxy() {
		super();
	}
	
    @Override
    public void releaseViews(boolean activityFinishing)
    {
        for (TileSourceProxy sourceProxy : handledTileSources) {
            sourceProxy.release();
        }
        handledTileSources.clear();
//        for (AnnotationProxy annotation : annotations) {
//            annotation.setParentForBubbling(null);
//            annotation.setMarker(null);
//            annotation.setMapView(null);
//        }
        super.releaseViews(activityFinishing);
    }
	
	public void processPreloaded() {
	    if (preloadedProps == null) {
	        return;
	    }
	    for (Map.Entry<String, ArrayList<Object>> entry : preloadedProps.entrySet()) {
            String name = entry.getKey();
            ArrayList<Object> value = entry.getValue();
            setProperty(name, value.toArray());
	    //we set the properties and clear because if the activity is killed by android
	    //we still want to be able to reload everything again
//	    if (preloadRoutes != null && preloadRoutes.size() > 0) {
//	        setProperty(AkylasMapModule.PROPERTY_ROUTES, preloadRoutes.toArray());
//            preloadRoutes.clear();
//	    }
//	    if (preloadSources != null && preloadSources.size() > 0) {
//            setProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, preloadSources.toArray());
//            preloadSources.clear();
        }
	    preloadedProps.clear();
	}

	public abstract TiUIView createView(Activity activity);
	
    @Override
    public void realizeViews(TiUIView view, boolean enableModelListener, boolean processProperties)
    {
        processPreloaded();
        super.realizeViews(view, enableModelListener, processProperties);
    }
//	@Override
//	public boolean handleMessage(Message msg) {
//		AsyncResult result = null;
//		switch (msg.what) {
//
//		case MSG_ADD_ANNOTATION: {
//			result = (AsyncResult) msg.obj;
//			KrollDict dict = (KrollDict)result.getArg();
//            handleInsertAnnotationAt(dict.optInt("index", -1), dict.get("object"));
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_REMOVE_ANNOTATION: {
//			result = (AsyncResult) msg.obj;
//			handleRemoveAnnotation(result.getArg());
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_REMOVE_ALL_ANNOTATIONS: {
//			result = (AsyncResult) msg.obj;
//			handleRemoveAllAnnotations();
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_SELECT_ANNOTATION: {
//			result = (AsyncResult) msg.obj;
//			handleSelectAnnotation(result.getArg());
//			result.setResult(null);
//			return true;
//		}
//		
//		case MSG_SELECT_USER_ANNOTATION: {
//            result = (AsyncResult) msg.obj;
//            handleSelectUserAnnotation();
//            result.setResult(null);
//            return true;
//        }
//		
//		case MSG_DESELECT_ANNOTATION: {
//			result = (AsyncResult) msg.obj;
//			handleDeselectAnnotation(result.getArg());
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_ADD_ROUTE: {
//			result = (AsyncResult) msg.obj;
//			KrollDict dict = (KrollDict)result.getArg();
//            handleInsertRouteAt(dict.optInt("index", -1), dict.get("object"));
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_REMOVE_ROUTE: {
//			result = (AsyncResult) msg.obj;
//			handleRemoveRoute((RouteProxy) result.getArg());
//			result.setResult(null);
//			return true;
//		}
//
//		case MSG_MAX_ZOOM: {
//			result = (AsyncResult) msg.obj;
//			result.setResult(getMaxZoomInternal());
//			return true;
//		}
//
//		case MSG_MIN_ZOOM: {
//			result = (AsyncResult) msg.obj;
//			result.setResult(getMinZoomInternal());
//			return true;
//		}
//		
//		case MSG_GET_USER_LOCATION_ENABLED: {
//            result = (AsyncResult) msg.obj;
//            result.setResult(getUserLocationEnabledInternal());
//            return true;
//        }
//
//		case MSG_CHANGE_ZOOM: {
//			handleZoom(msg.arg1);
//			return true;
//		}
//
////        case MSG_ADD_TILE_SOURCE: {
////            result = (AsyncResult) msg.obj;
////            KrollDict dict = (KrollDict)result.getArg();
////            handleInsertTileSourceAt(dict.optInt("index", -1), dict.get("object"));
////            result.setResult(null);
////            return true;
////        }
////        case MSG_REMOVE_TILE_SOURCE: {
////            result = (AsyncResult) msg.obj;
////            handleRemoveTileSource(result.getArg());
////            result.setResult(null);
////            return true;
////        }
//
//		default: {
//			return super.handleMessage(msg);
//		}
//		}
//	}

//	public void handleRemoveAllAnnotations() {
//	    AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//	    if (mapView != null) {
//            mapView.removeAllAnnotations();
//        }
//	    for (AnnotationProxy annotation : annotations) {
//	        annotation.setMarker(null);
//        }
//	    annotations.clear();
//        
//	}

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

//		if (annotation instanceof String) {
//			AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//			if (mapView != null) {
//				if (mapView.findMarkerByTitle((String) annotation) == null) {
//					return false;
//				}
//			}
//		}

		return true;
	}

//	public void handleSelectAnnotation(Object annotation) {
//		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//		if (mapView != null) {
//			mapView.selectAnnotation(annotation);
//		}
//	}
	   
//    private void handleSelectUserAnnotation() {
//        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//        if (mapView != null) {
//            mapView.selectUserAnnotation();
//        }
//    }

//	public void handleDeselectAnnotation(Object annotation) {
//		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//		if (mapView != null) {
//			mapView.deselectAnnotation(annotation);
//		}
//	}
	
//	private void handleInsertRouteAt(int index, Object route) {
//	    AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//        if (mapView != null) {
//            Object result = mapView.addRoute(route);
//            if (result != null) {
//                addToProperty(AkylasMapModule.PROPERTY_ROUTES, index, result);
//            }
//        } else {
//            addPreloadRoute(route, index, false);
//        }
//    }
	
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
        if (array == null) {
            if (object instanceof Object[]) {
                array = (Object[]) object;
            } else if (object instanceof List) {
                array = ((List) object).toArray();

            } else {
                array = new Object[] {object};
            }
            setProperty(property, array);
        }
        else {
            ArrayList<Object> newObj = new ArrayList<Object>(Arrays.asList(array));
            if (object instanceof Object[]) {
                if (index >= 0 && index < newObj.size()) {
                    newObj.addAll(index, Arrays.asList(object));
                }
                else {
                    newObj.addAll(Arrays.asList(object));
                }
            } else if (object instanceof List) {
                if (index >= 0 && index < newObj.size()) {
                    newObj.addAll(index, (Collection<? extends Object>) object);
                }
                else {
                    newObj.addAll((Collection<? extends Object>) object);
                }
            } else {
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
//        boolean isArray = object instanceof Object[];
        boolean needsUpdate = false;
        if (object instanceof Number) {
            needsUpdate = newObj.remove(((Number)object).intValue()) != null;
        }
        else if (object instanceof Object[]) {
            needsUpdate = newObj.removeAll(Arrays.asList(object));
        } else {
            needsUpdate = newObj.remove(object);
        }
        if (needsUpdate) {
            setProperty(property, newObj.toArray());
        }
	}
	
	private AnnotationProxy annotationFromObject(Object object) {
        if (object instanceof HashMap) {
            AnnotationProxy result =(AnnotationProxy)  KrollProxy.createProxy(AnnotationProxy.class, null,
                    new Object[] { object }, null);
            result.updateKrollObjectProperties();
            return result;
        }
        if (object instanceof AnnotationProxy) {
            return (AnnotationProxy) object;
        }
        return null;
	}
	
    public void handleMaxAnnotations(int aboutToAdd, List<Object> annotations) {
        if (maxAnnotations <= 0) return;
        int total = aboutToAdd + annotations.size();
        if (total > maxAnnotations) {
            int length = (int) Math.min(total - maxAnnotations, annotations.size());
            List<Object> toRemove = annotations.subList(0, length);
            handleRemoveAnnotation(toRemove.toArray());
        }
    }
	
    
//    private void handleRemoveRoute(Object route) {
//        if (route == null) return;
//        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//        if (mapView != null) {
//            mapView.removeRoute(route);
//            removeFromProperty(AkylasMapModule.PROPERTY_ROUTES, route);
//        } else {
//            deletePreloadObject(preloadRoutes, route);
//        }
//    }
    
    private void handleRemoveAnnotation(Object annotation) {
        
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    private void addPreloadObject(Class objectClass, ArrayList preloadArray, Object object, int index) {
        if(object instanceof HashMap) {
            object =  KrollProxy.createProxy(objectClass, null, new Object[]{object}, null);
        } else if(object instanceof String && objectClass.equals(TileSourceProxy.class)) {
            KrollDict props = new KrollDict();
            props.put(TiC.PROPERTY_SOURCE, object);
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
	
	public void addPreloadObject(String key, Object value, int index, boolean arrayOnly) {
        addPreloadObject(PRELOAD_CLASSES.get(key), getOrCreatePreload(key), value, index, arrayOnly);
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
	
	public void deletePreloadObject(String key, Object value) {
	    deletePreloadObject(getOrCreatePreload(key), value);
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
//    
//	public void addPreloadRoute(Object value, int index, boolean arrayOnly) {
//        addPreloadObject(AkylasMapModule.PROPERTY_ROUTES, value, index, arrayOnly);
//	}

	public ArrayList getOrCreatePreload(final String key) {
	    if (preloadedProps == null) {
	        preloadedProps = new HashMap<String, ArrayList<Object>>();
	    }
	    if (!preloadedProps.containsKey(key)) {
	        ArrayList<Object> array = new ArrayList<Object>();
	        if (hasProperty(key)) {
                addPreloadObject(PRELOAD_CLASSES.get(key), array, getProperty(key), -1, false);
            }
	        preloadedProps.put(key, array);
	    }
	    return preloadedProps.get(key);
	}
	
	public ArrayList deletePreload(final String key) {
        if (preloadedProps == null) {
            return null;
        }
        return preloadedProps.remove(key);
    }
	
	public Object getCurrentObjects(final String key) {
	    if (preloadedProps != null && preloadedProps.containsKey(key)) {
            return preloadedProps.get(key);
        }
	    return getProperty(key);
	}

//	
//	public void removePreloadRoute(Object value) {
//	    deletePreloadObject(preloadRoutes, value);
//    }
//
//    public void removePreloadAnnotation(Object value) {
//        deletePreloadObject(preloadAnnotations, value);
//    }

//	public float getMaxZoomInternal() {
//		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//		if (mapView != null) {
//			return mapView.getMaxZoomLevel();
//		} else {
//			return 0;
//		}
//	}

//	public float getMinZoomInternal() {
//		AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//		if (mapView != null) {
//			return mapView.getMinZoomLevel();
//		} else {
//			return 0;
//		}
//	}
//	
//	public boolean getUserLocationEnabledInternal() {
//        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
//        if (mapView != null) {
//            return mapView.getUserLocationEnabled();
//        } else {
//            return false;
//        }
//    }

    public void handleZoom(int delta) {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.changeZoomLevel(delta, true);
        }
    }
    

//    public ArrayList<RouteProxy> getPreloadRoutes() {
//        return preloadRoutes;
//    }
//    
    
    //KROLL ACCESSORS

//    @Kroll.method
//    @Kroll.getProperty
//    public Object[] getAnnotations() {
//        return annotations.toArray();
//    }
//    
    @Kroll.method
    public void selectUserAnnotation() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.selectUserAnnotation();
        }
//        if (TiApplication.isUIThread()) {
//            handleSelectUserAnnotation();
//        } else {
//            TiMessenger.sendBlockingMainMessage(
//                    getMainHandler().obtainMessage(MSG_SELECT_USER_ANNOTATION));
//        }
    }

    @Kroll.method
    public void deselectAnnotation(final Object annotation) {
        if (!isAnnotationValid(annotation)) {
            return;
        }
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.deselectAnnotation(annotation);
        }
//        if (TiApplication.isUIThread()) {
//            handleDeselectAnnotation(annotation);
//        } else {
//            getMainHandler().post(new Runnable() {
//                @Override
//                public void run() {
//                    handleDeselectAnnotation(annotation);
//                }
//            });
//        }
    }


    
    @Kroll.method
    @Kroll.getProperty
	public float getMaxZoom() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getMaxZoomLevel();
        } else {
            return 0;
        }
//		if (TiApplication.isUIThread()) {
//			return getMaxZoomInternal();
//		} else {
//			return (Float) TiMessenger.sendBlockingMainMessage(getMainHandler()
//					.obtainMessage(MSG_MAX_ZOOM));
//		}
	}
	
	@Kroll.method
	@Kroll.getProperty
	public float getMinZoom() {
	    AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getMinZoomLevel();
        } else {
            return 0;
        }
//		if (TiApplication.isUIThread()) {
//			return getMinZoomInternal();
//		} else {
//			return (Float) TiMessenger.sendBlockingMainMessage(getMainHandler()
//					.obtainMessage(MSG_MIN_ZOOM));
//		}
	}
	
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getRegion() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
			return mapView.getRegionDict();
		}
		return null;
		
//      if (TiApplication.isUIThread()) {
//      return getUserLocationEnabledInternal();
//  } else {
//      return (Boolean) TiMessenger.sendBlockingMainMessage(getMainHandler()
//              .obtainMessage(MSG_GET_USER_LOCATION_ENABLED));
//  }
	}
	
	
	@Kroll.method
	@Kroll.getProperty
    public float getZoom() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getZoomLevel();
        }
        return TiConvert.toFloat(getProperty(AkylasMapModule.PROPERTY_ZOOM), 0);
    }
	
    @Kroll.method
    @Kroll.getProperty
	public boolean getUserLocationEnabled() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getUserLocationEnabled();
        } else {
            return false;
        }
//	    if (TiApplication.isUIThread()) {
//            return getUserLocationEnabledInternal();
//        } else {
//            return (Boolean) TiMessenger.sendBlockingMainMessage(getMainHandler()
//                    .obtainMessage(MSG_GET_USER_LOCATION_ENABLED));
//        }
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
	@Kroll.getProperty
    public KrollDict getUserLocation() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            return mapView.getUserLocation();
        }
        return null;
    }

    @Kroll.method
	public void zoom(int delta) {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.changeZoomLevel(delta, true);
        }
//		if (TiApplication.isUIThread()) {
//			handleZoom(delta);
//		} else {
//			getMainHandler().obtainMessage(MSG_CHANGE_ZOOM, delta, 0)
//					.sendToTarget();
//		}
	}

    @Kroll.method
    public void zoomIn(@Kroll.argument(optional = true) final Object about) {
		final AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
//			getActivity().runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
					LatLng position = AkylasMapModule.latlongFromObject(about);
					if (position != null) {
						mapView.zoomIn(position, true);
					}
					else {
						mapView.zoomIn();
					}
//				}
//			});
		}
	}
    
    @Kroll.method
    public void zoomOut(@Kroll.argument(optional = true) final Object about) {
		final AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
		if (mapView != null) {
//			getActivity().runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
					LatLng position = AkylasMapModule.latlongFromObject(about);
					if (position != null) {
						mapView.zoomOut(position, true);
					}
					else {
						mapView.zoomOut();
					}
//				}
//			});
		}
	}


//    public void addPreloadTileSource(Object value, int index, boolean arrayOnly) {
//        addPreloadObject(TileSourceProxy.class, getOrCreatePreloadSources(), value, index, arrayOnly);
//    }
    
//    public ArrayList getOrCreatePreloadSources() {
//        if (preloadSources == null) {
//            preloadSources = new ArrayList<Object>();
//            if (hasProperty(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
//                addPreloadObject(Object.class, preloadSources, getProperty(AkylasMapModule.PROPERTY_TILE_SOURCE), -1, false);
//            }
//        }
//        return preloadSources;
//    }

//    public void removePreloadTileSource(Object value) {
//        deletePreloadObject(preloadSources, value);
//    }
    //KROLL ACCESSORS
    
    @Kroll.method
    @Kroll.getProperty
    public Object getRoutes() {
        return getCurrentObjects(AkylasMapModule.PROPERTY_ROUTES);
    }

    
    @Kroll.method
    public void addRoute(Object route) {
        if (route == null) return;
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        int index = -1;
        if (mapView != null) {
            Object result = mapView.addRoute(route);
            if (result != null) {
                addToProperty(AkylasMapModule.PROPERTY_ROUTES, index, result);
            }
        } else {
            addPreloadObject(AkylasMapModule.PROPERTY_ROUTES, route, index, false);
        }
//        if (TiApplication.isUIThread()) {
//            handleInsertRouteAt(-1, route);
//        } else {
//            sendIndexMessage(MSG_ADD_ROUTE, -1, route);
//        }
    }
    
    @Kroll.method
    public void removeRoute(RouteProxy route) {
        if (route == null) return;
        
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            mapView.removeRoute(route);
            removeFromProperty(AkylasMapModule.PROPERTY_ROUTES, route);
        } else {
            deletePreloadObject(AkylasMapModule.PROPERTY_ROUTES, route);
        }
//      if (TiApplication.isUIThread()) {
//          handleRemoveRoute(route);
//      } else {
//          TiMessenger.sendBlockingMainMessage(
//                  getMainHandler().obtainMessage(MSG_REMOVE_ROUTE), route);
//
//      }
    }
    
    @Kroll.method
    @Kroll.getProperty
    public Object getTileSource() {
        return getCurrentObjects(AkylasMapModule.PROPERTY_TILE_SOURCE);
    }

    @Kroll.method
    public void addTileSource(Object value, @Kroll.argument(optional = true) final Object indexObj) {
        if (value == null) return;
        int index = -1;
        if (indexObj != null) {
            index = ((Number)indexObj).intValue();
        }
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            TileSourceProxy result = mapView.addTileSource(value, index);
            if (result != null) {
                handledTileSources.add(result);
                addToProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, index, value);

            }
        } else {
            addPreloadObject(AkylasMapModule.PROPERTY_TILE_SOURCE, value, index, false);
        }
    }

    @Kroll.method
    public void removeTileSource(Object value) {
        if (value == null) return;
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView != null) {
            if (value instanceof Number) {
                int index = ((Number) value).intValue();
                if (index >= 0  && index < handledTileSources.size()) {
                    value = handledTileSources.get(index);
                }
            }
            mapView.removeTileSource(value);
            removeFromProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, value);
        } else {
            deletePreloadObject(AkylasMapModule.PROPERTY_TILE_SOURCE, value);
        }
    }
    
    public List<TileSourceProxy> getHandledTileSources() {
        return handledTileSources;
    }
    
    
    @Kroll.method
    @Kroll.getProperty
    public Object getAnnotations() {
        return getCurrentObjects(AkylasMapModule.PROPERTY_ANNOTATIONS);
    }

    @Kroll.method
    public void addAnnotation(Object value) {
        if (value == null) return;
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        int index = -1;
        Object[] array;
        if (value instanceof Object[]) {
            array  = (Object[]) value;
        }else {
            array  = new Object[]{value};
        }
        List<Object> current = Arrays.asList(getCurrentObjects(AkylasMapModule.PROPERTY_ANNOTATIONS));
        List<AnnotationProxy> toAdd = new ArrayList<AnnotationProxy>();
        for (int i = 0; i < array.length; i++) {
            AnnotationProxy annProxy  = annotationFromObject(array[i]);
            if (annProxy != null && !current.contains(annProxy)) {
                toAdd.add(annProxy);
            }
        }
        int addCount = toAdd.size();
        if (addCount > 0) {
            handleMaxAnnotations(addCount, current);
            if (maxAnnotations > 0 && addCount > maxAnnotations) {
                toAdd = toAdd.subList((int) (addCount - maxAnnotations), toAdd.size());
            }
            if (mapView != null) {
                mapView.addAnnotations(toAdd.toArray());
                addToProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, index, toAdd);
            } else {
                addPreloadObject(AkylasMapModule.PROPERTY_ANNOTATIONS, value, index, false);
            }
        }
//        if (TiApplication.isUIThread()) {
//        } else {
//            sendIndexMessage(MSG_ADD_ANNOTATION, -1, annotation);
//        }
    }

    @Kroll.method
    public void removeAllAnnotations() {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        List toClear = null;
        if (mapView != null) {
            mapView.removeAllAnnotations();
            Object[] annots = (Object[]) getProperty(AkylasMapModule.PROPERTY_ANNOTATIONS);
            if (annots != null) {
                toClear = Arrays.asList(annots);
                setProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, null);
            }
        } else {
            
            toClear = deletePreload(AkylasMapModule.PROPERTY_ANNOTATIONS);
        }

        if (toClear != null) {
            for (int i = 0; i < toClear.size(); i++) {
                ((AnnotationProxy)toClear.get(i)).wasRemoved();
            }
        }

//        annotations.clear();
//        if (TiApplication.isUIThread()) {
//            handleRemoveAllAnnotations();
//        } else {
//            TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(
//                    MSG_REMOVE_ALL_ANNOTATIONS));
//        }
//        annotations.clear();
    }
    

    @Kroll.method
    public void removeAnnotation(Object value) {
            
        if (value == null) return;
        Object[] array;
        if (value instanceof Object[]) {
            array  = (Object[]) value;
        }else {
            array  = new Object[]{value};
        }
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();

        List<AnnotationProxy> toRemove = new ArrayList<AnnotationProxy>();
        for (int i = 0; i < array.length; i++) {
            Object annotation  = array[i] ;
            if (annotation instanceof AnnotationProxy) {
                toRemove.add((AnnotationProxy) annotation);
            }
        }
        if (toRemove.size() > 0) {
            if (mapView != null) {
                mapView.removeAnnotation(toRemove.toArray());
                removeFromProperty(AkylasMapModule.PROPERTY_ANNOTATIONS, toRemove.toArray());
            } else {
                deletePreloadObject(AkylasMapModule.PROPERTY_ANNOTATIONS, toRemove.toArray());
            }
        }
    }

//    @Kroll.method
//    public void removeAnnotations(Object annos) {
//        removeAnnotation(annos);
//    }

    @Kroll.method
    public void selectAnnotation(Object annotation) {
        AkylasMapDefaultView mapView = (AkylasMapDefaultView) peekView();
        if (mapView == null || !(annotation instanceof AnnotationProxy)) {
            return;
        }
//        if (annotation instanceof Number) {
//            int index = ((Number) annotation).intValue();
//            if (index >= 0 && index < annotations.size()) {
//                annotation = annotations.get(index);
//            }
//        }
        if (!isAnnotationValid(annotation)) {
            return;
        }
        
        if (mapView != null) {
            mapView.selectAnnotation(annotation);
        }
//        if (TiApplication.isUIThread()) {
//            handleSelectAnnotation(annotation);
//        } else {
//            TiMessenger.sendBlockingMainMessage(
//                    getMainHandler().obtainMessage(MSG_SELECT_ANNOTATION),
//                    annotation);
//        }
    }
    
       
//    @Kroll.method
//    public void setAnnotations(Object annos) {
//        removeAllAnnotations();
//        addAnnotation(annos);
//    }
    
    
//    private void handleInsertTileSourceAt(int index, Object tilesource) {
//        if (tilesource == null) return;
//        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
//        if (mapView != null) {
//            Object result = mapView.addTileSource(tilesource, index);
//            if (result != null) {
//                addToProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, index, result);
//
//            }
//        } else {
//            addPreloadTileSource(tilesource, index, false);
//        }
//    }
    
//    private void handleRemoveTileSource(Object tilesource) {
//        if (tilesource == null) return;
//        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
//        if (mapView != null) {
//            mapView.removeTileSource(tilesource);
//            removeFromProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, tilesource);
//        } else {
//            deletePreloadObject(preloadSources, tilesource);
//        }
//    }
}
