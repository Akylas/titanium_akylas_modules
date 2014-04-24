package akylas.mapbox;

/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */


import java.lang.reflect.Array;
import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.view.TiTouchDelegate;
import org.appcelerator.titanium.view.TiUINonViewGroupView;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.TilesLoadedListener;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

public class AkylasMapboxView extends TiUINonViewGroupView
{
	private static final String TAG = "AkylasMapboxView";
	private MapController mapController;
    private MapView map;
    private UserLocationOverlay myLocationOverlay;
	protected boolean animate = false;
	protected boolean preLayout = true;
	protected ArrayList<TiMarker> timarkers;
	protected AnnotationProxy selectedAnnotation;

	public AkylasMapboxView(final TiViewProxy proxy)
	{
		super(proxy);
		timarkers = new ArrayList<TiMarker>();
		map = new MapView(proxy.getActivity(), null);
		map.setBackgroundColor(Color.GRAY);
		try {
			map.setDefaultPinRes(TiRHelper.getResource("drawable.def_pin", false));
		} catch (ResourceNotFoundException e) {
		}
		mapController = map.getController();
		map.setOnTilesLoadedListener(new TilesLoadedListener() {
            @Override
            public boolean onTilesLoaded() {
                return false;
            }

			@Override
			public boolean onTilesLoadStarted() {
				return false;
			}
        });
		map.setMapViewListener(new MapViewListener() {
			
			@Override
			public void onTapMarker(MapView pMapView, Marker pMarker) {
				fireEventOnMarker(TiC.EVENT_CLICK, pMarker, "pin");
			}
			
			@Override
			public void onTapMap(MapView pMapView, ILatLng pPosition) {
				fireEventOnMap(TiC.EVENT_CLICK, pPosition);
			}
			
			@Override
			public void onShowMarker(MapView pMapView, Marker pMarker) {
				fireEventOnMarker(TiC.EVENT_FOCUS, pMarker, "pin");				
			}
			
			@Override
			public void onLongPressMarker(MapView pMapView, Marker pMarker) {
				fireEventOnMarker(TiC.EVENT_LONGPRESS, pMarker, "pin");
			}
			
			@Override
			public void onLongPressMap(MapView pMapView, ILatLng pPosition) {
				fireEventOnMap(TiC.EVENT_LONGPRESS, pPosition);
			}
			
			@Override
			public void onHidemarker(MapView pMapView, Marker pMarker) {
				fireEventOnMarker(TiC.EVENT_BLUR, pMarker, "pin");
				
			}
		});
		
		setNativeView(map);
	}
	@Override
	public boolean customInterceptTouchEvent(MotionEvent event) {
		//to prevent double events
		return map.onTouchEvent(event) || super.customInterceptTouchEvent(event);
	}
	
	private UserLocationOverlay getOrCreateLocationOverlay() {
		if (myLocationOverlay == null) {
			// Adds an icon that shows location
	        try {
				myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(getContext()), 
						map,
						TiRHelper.getResource("drawable.direction_arrow", false), 
						TiRHelper.getResource("drawable.person", false));
		        myLocationOverlay.enableMyLocation();
			} catch (ResourceNotFoundException e) {
			}
		}
		return myLocationOverlay;
	}
	
	private void addLocationOverlay() {
		map.getOverlays().add(getOrCreateLocationOverlay());
    }
	
	private void removeLocationOverlay() {
		if (myLocationOverlay != null) {
			if (map.getOverlays().contains(myLocationOverlay)) {
				map.getOverlays().remove(myLocationOverlay);
				map.invalidate();
			}
		}
		
    }

	/**
	 * Traverses through the view hierarchy to locate the SurfaceView and set the background to transparent.
	 * @param v the root view
	 */
//	private void setBackgroundTransparent(View v) {
//	    if (v instanceof SurfaceView) {
//	        SurfaceView sv = (SurfaceView) v;
//	        sv.setBackgroundColor(Color.TRANSPARENT);
//	    }
//
//	    if (v instanceof ViewGroup) {
//	        ViewGroup viewGroup = (ViewGroup) v;
//	        for (int i = 0; i < viewGroup.getChildCount(); i++) {
//	            setBackgroundTransparent(viewGroup.getChildAt(i));
//	        }
//	    }
//	}

	protected void processPreloadRoutes()
	{
		ArrayList<RouteProxy> routes = ((ViewProxy) proxy).getPreloadRoutes();
		for (int i = 0; i < routes.size(); i++) {
			addRoute(routes.get(i));
		}
		((ViewProxy) proxy).clearPreloadObjects();
	}

//	protected void onViewCreated()
//	{
//		map = acquireMap();
//		//A workaround for https://code.google.com/p/android/issues/detail?id=11676 pre Jelly Bean.
//		//This problem doesn't exist on 4.1+ since the map base view changes to TextureView from SurfaceView. 
//		if (Build.VERSION.SDK_INT < 16) {
//			View rootView = proxy.getActivity().findViewById(android.R.id.content);
//			setBackgroundTransparent(rootView);
//		}
//		processMapProperties(proxy.getProperties());
//		processPreloadRoutes();
//		map.setOnMarkerClickListener(this);
//		map.setOnMapClickListener(this);
//		map.setOnCameraChangeListener(this);
//		map.setOnMarkerDragListener(this);
//		map.setOnInfoWindowClickListener(this);
//		map.setInfoWindowAdapter(this);
//		map.setOnMapLongClickListener(this);
//		map.setOnMapLoadedCallback(this);
//	}

	@Override
	public void processProperties(KrollDict d)
	{
		super.processProperties(d);
		processPreloadRoutes();
		processMapProperties(d);
	}
	
	private void setTileSources(Object sources) {
		Object source = null;
		if (sources instanceof Object[]) {
			int length = Array.getLength(sources);
			source = new TileLayer[length];
			for (int i = 0; i < length; i++) {
				((Object[])source)[i] = AkylasMapboxModule.tileSourceFromObject(((Object[])sources)[i]);
			}
			map.setTileSource((TileLayer[])source);
		}
		else {
			source = AkylasMapboxModule.tileSourceFromObject (sources);
			map.setTileSource((TileLayer)source);
		}
//		if (source != null) {
//			map.setScrollableAreaLimit(map.getTileProvider().getBoundingBox());
//			map.setMinZoomLevel(map.getTileProvider().getMinimumZoomLevel());
//			map.setMaxZoomLevel(map.getTileProvider().getMaximumZoomLevel());
//			map.setCenter(map.getTileProvider().getCenterCoordinate());
//	        map.setZoom(map.getTileProvider().getCenterZoom());
//	        map.zoomToBoundingBox(map.getTileProvider().getBoundingBox());
//		}
	}

	public void processMapProperties(KrollDict d)
	{
		if (d.containsKey(AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
			updateScrollableAreaLimit(d.getKrollDict(AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT));
		}
		if (d.containsKey(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocationEnabled(TiConvert.toBoolean(d, TiC.PROPERTY_USER_LOCATION, false));
		}
		
		if (d.containsKey(AkylasMapboxModule.PROPERTY_USER_TRACKING_MODE)) {
			setUserTrackingMode(TiConvert.toInt(d, AkylasMapboxModule.PROPERTY_USER_TRACKING_MODE, 0));
		}
		if (d.containsKey(TiC.PROPERTY_ANIMATE)) {
			animate = d.getBoolean(TiC.PROPERTY_ANIMATE);
		}
		if (d.containsKey(AkylasMapboxModule.PROPERTY_TILE_SOURCE)) {
			setTileSources(d.get(AkylasMapboxModule.PROPERTY_TILE_SOURCE));
		}
		if (d.containsKey(TiC.PROPERTY_REGION)) {
			updateRegion(d.getKrollDict(TiC.PROPERTY_REGION));
		}
		if (d.containsKey(AkylasMapboxModule.PROPERTY_CENTER_COORDINATE)) {
			updateCenter(d.getKrollDict(AkylasMapboxModule.PROPERTY_CENTER_COORDINATE));
		}
		if (d.containsKey(TiC.PROPERTY_ANNOTATIONS)) {
			Object[] annotations = (Object[]) d.get(TiC.PROPERTY_ANNOTATIONS);
			addAnnotations(annotations);
		}
		if (d.containsKey(AkylasMapboxModule.PROPERTY_ZOOM)) {
			changeZoomLevel(TiConvert.toFloat(d, AkylasMapboxModule.PROPERTY_ZOOM));
		}
		if (d.containsKey(AkylasMapboxModule.PROPERTY_DEFAULT_PIN_IMAGE)) {
			map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(d.get(AkylasMapboxModule.PROPERTY_DEFAULT_PIN_IMAGE)));
		}
		if (d.containsKey(AkylasMapboxModule.PROPERTY_DEFAULT_PIN_ANCHOR)) {
			TiPoint point = TiConvert.toPoint(d.get(AkylasMapboxModule.PROPERTY_DEFAULT_PIN_IMAGE));
			map.setDefaultPinAnchor(point.computeFloat(null, 0, 0));
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{

		if (key.equals(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocationEnabled(TiConvert.toBoolean(newValue));
		} else if(key.equals(AkylasMapboxModule.PROPERTY_USER_TRACKING_MODE)) {
			setUserTrackingMode(TiConvert.toInt(newValue, 0));
		} else if (key.equals(TiC.PROPERTY_REGION)) {
			updateRegion(newValue);
		} else if (key.equals(AkylasMapboxModule.PROPERTY_CENTER_COORDINATE)) {
			updateCenter(newValue);
		} else if (key.equals(TiC.PROPERTY_ANIMATE)) {
			animate = TiConvert.toBoolean(newValue);
		} else if (key.equals(TiC.PROPERTY_ANNOTATIONS)) {
			updateAnnotations((Object[]) newValue);
		} else if (key.equals(AkylasMapboxModule.PROPERTY_TILE_SOURCE)) {
			setTileSources(newValue);
		} else if (key.equals(AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
			updateScrollableAreaLimit((KrollDict) newValue);
		} else if (key.equals(AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
				map.setDefaultPinDrawable(TiUIHelper.getResourceDrawable(newValue));
		} else if (key.equals(AkylasMapboxModule.PROPERTY_SCROLLABLE_AREA_LIMIT)) {
				TiPoint point = TiConvert.toPoint(newValue);
				map.setDefaultPinAnchor(point.computeFloat(null, 0, 0));
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}

	public MapView getMap()
	{
		return map;
	}

	protected void setUserLocationEnabled(boolean enabled)
	{
		if (enabled) {
			addLocationOverlay();
		}
		else {
			removeLocationOverlay();
		}
	}
	protected void setUserTrackingMode(int mode)
	{
		if (mode == 0) {
			getOrCreateLocationOverlay().disableFollowLocation();
		}
		else  {
			getOrCreateLocationOverlay().enableFollowLocation();
		}
	}
	

	public float getMaxZoomLevel() 
	{
		return map.getMaxZoomLevel();
	}
		
	public float getMinZoomLevel() 
	{
		return map.getMinZoomLevel();
	}
	public void updateCenter(Object dict)
	{
		LatLng center = AkylasMapboxModule.latlongFromDict(dict);
		if (center != null) {
			map.setCenter(center);
		}
	}
	
	public void updateRegion(Object dict)
	{
		BoundingBox box = AkylasMapboxModule.regionFromDict(dict);
		if (box != null) {
			map.zoomToBoundingBox(box);
		}
	}
	
	public void updateScrollableAreaLimit(Object dict)
	{
		BoundingBox box = AkylasMapboxModule.regionFromDict(dict);
		if (box != null) {
			map.setScrollableAreaLimit(box);
		}
	}


	protected void addAnnotation(AnnotationProxy annotation)
	{
		// if annotation already on map, remove it first then re-add it
		TiMarker tiMarker = annotation.getTiMarker();
		if (tiMarker != null) {
			removeAnnotation(tiMarker);
		}
//		ann otation.processOptions();
		// add annotation to map view
//		Marker marker = map.addMarker(annotation.getMarkerOptions());
//		TiMarker tiMarker = new TiMarker(marker, annotation);
//		annotation.setTiMarker(tiMarker);
//		timarkers.add(timarker);
		map.addMarker(tiMarker);
	}

	protected void addAnnotations(Object[] annotations)
	{
		for (int i = 0; i < annotations.length; i++) {
			Object obj = annotations[i];
			if (obj instanceof AnnotationProxy) {
				AnnotationProxy annotation = (AnnotationProxy) obj;
				addAnnotation(annotation);
			}
		}
	}

	protected void updateAnnotations(Object[] annotations)
	{
		// First, remove old annotations from map
		removeAllAnnotations();
		// Then we add new annotations to the map
		addAnnotations(annotations);
	}

	protected void removeAllAnnotations()
	{
		for (int i = 0; i < timarkers.size(); i++) {
			TiMarker timarker = timarkers.get(i);
			map.getOverlays().remove(timarker);
			AnnotationProxy proxy = timarker.getProxy();
			if (proxy != null) {
				proxy.setTiMarker(null);
			}
		}
		timarkers.clear();
	}

	public TiMarker findMarkerByTitle(String title)
	{
		for (int i = 0; i < timarkers.size(); i++) {
			TiMarker timarker = timarkers.get(i);
			AnnotationProxy annoProxy = timarker.getProxy();
			if (annoProxy != null && annoProxy.getTitle().equals(title)) {
				return timarker;
			}
		}
		return null;
	}

	protected void removeAnnotation(Object annotation)
	{
		TiMarker timarker = null;
		if (annotation instanceof TiMarker) {
			timarker = (TiMarker) annotation;
		} else if (annotation instanceof AnnotationProxy) {
			timarker = ((AnnotationProxy) annotation).getTiMarker();
		} else if (annotation instanceof String) {
			timarker = findMarkerByTitle((String) annotation);
		}

		if (timarker != null && timarkers.remove(timarker)) {
			map.getOverlays().remove(timarker);
			AnnotationProxy proxy = timarker.getProxy();
			if (proxy != null) {
				proxy.setTiMarker(null);
			}
		}
	}

	protected void selectAnnotation(Object annotation)
	{
		TiMarker marker = null;
		if (annotation instanceof AnnotationProxy) {
			AnnotationProxy proxy = (AnnotationProxy) annotation;
			marker = ((AnnotationProxy) annotation).getTiMarker();
		} else if (annotation instanceof TiMarker) {
			marker = (TiMarker) annotation;
		} else if (annotation instanceof String) {
			String title = (String) annotation;
			marker = findMarkerByTitle(title);
		}

		if (marker != null) {
			selectedAnnotation = marker.getProxy();
			map.selectMarker(marker);
		}
	}

	protected void deselectAnnotation(Object annotation)
	{
		map.selectMarker(null);
		selectedAnnotation = null;
	}

	private AnnotationProxy getProxyByMarker(TiMarker m)
	{
		if (m != null) {
			for (int i = 0; i < timarkers.size(); i++) {
				TiMarker timarker = timarkers.get(i);
				if (m.equals(timarker)) {
					return timarker.getProxy();
				}
			}
		}
		return null;
	}

	public void addRoute(RouteProxy r)
	{
		// check if route already added.
//		if (r.getRoute() != null) {
//			return;
//		}
//
//		r.processOptions();
//		r.setRoute(map.addPolyline(r.getOptions()));
		map.getOverlayManager().add(r.getPath());
	}

	public void removeRoute(RouteProxy r)
	{
//		if (r.getRoute() == null) {
//			return;
//		}
//
//		r.getRoute().remove();
//		r.setRoute(null);
		map.getOverlayManager().remove(r.getPath());
	}

	public void changeZoomLevel(float delta)
	{
		mapController.setZoom(delta);

	}
	
	public void zoomIn()
	{
		mapController.zoomIn();
	}
	
	public void zoomOut()
	{
		mapController.zoomOut();
	}
	
	private void fireEventOnMap(String type, ILatLng point) {
		if (!hasListeners(type)) return;
		KrollDict d = new KrollDict();
		d.put(TiC.PROPERTY_ALTITUDE, point.getAltitude());
		d.put(TiC.PROPERTY_LATITUDE, point.getLatitude());
		d.put(TiC.PROPERTY_LONGITUDE, point.getLongitude());
		d.put(AkylasMapboxModule.PROPERTY_MAP, proxy);
		fireEvent(type, d, true, false);
	}

	public void fireEventOnMarker(String type, Marker marker, String clickSource)
	{
		if (!hasListeners(type)) return;
		KrollDict d = new KrollDict();
		String title = null;
		String subtitle = null;
//			TiMapInfoWindow infoWindow = annoProxy.getMapInfoWindow();
//			if (infoWindow != null) {
//				title = infoWindow.getTitle();
//				subtitle = infoWindow.getSubtitle();
//			}
		d.put(TiC.PROPERTY_TITLE, title);
		d.put(TiC.PROPERTY_SUBTITLE, subtitle);
		d.put(TiC.PROPERTY_ALTITUDE, marker.getPoint().getAltitude());
		d.put(TiC.PROPERTY_LATITUDE, marker.getPoint().getLatitude());
		d.put(TiC.PROPERTY_LONGITUDE, marker.getPoint().getLongitude());
		if (marker instanceof TiMarker) {
			d.put(TiC.PROPERTY_ANNOTATION, ((TiMarker)marker).getProxy());
		}
		d.put(AkylasMapboxModule.PROPERTY_MAP, proxy);
		d.put(TiC.EVENT_PROPERTY_CLICKSOURCE, clickSource);
		fireEvent(type, d, true, false);
		
	}

	public void firePinChangeDragStateEvent(Marker marker, AnnotationProxy annoProxy, int dragState)
	{
		KrollDict d = new KrollDict();
		String title = null;
//		TiMapInfoWindow infoWindow = annoProxy.getMapInfoWindow();
//		if (infoWindow != null) {
//			title = infoWindow.getTitle();
//		}
		d.put(TiC.PROPERTY_TITLE, title);
		d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
		d.put(AkylasMapboxModule.PROPERTY_MAP, proxy);
		d.put(TiC.PROPERTY_SOURCE, proxy);
		d.put(AkylasMapboxModule.PROPERTY_NEWSTATE, dragState);
		d.put(TiC.PROPERTY_TYPE, AkylasMapboxModule.EVENT_PIN_CHANGE_DRAG_STATE);
		proxy.fireEvent(AkylasMapboxModule.EVENT_PIN_CHANGE_DRAG_STATE, d);
	}

//	@Override
//	public boolean onMarkerClick(Marker marker)
//	{
//		AnnotationProxy annoProxy = getProxyByMarker(marker);
//		if (annoProxy == null) {
//			Log.e(TAG, "Marker can not be found, click event won't fired.", Log.DEBUG_MODE);
//			return false;
//		} else if (selectedAnnotation != null && selectedAnnotation.equals(annoProxy)) {
//			selectedAnnotation.hideInfo();
//			selectedAnnotation = null;
//			fireClickEvent(marker, annoProxy, AkylasMapboxModule.PROPERTY_PIN);
//			return true;
//		}
//		fireClickEvent(marker, annoProxy, AkylasMapboxModule.PROPERTY_PIN);
//		selectedAnnotation = annoProxy;
//		boolean showInfoWindow = TiConvert.toBoolean(annoProxy.getProperty(AkylasMapboxModule.PROPERTY_SHOW_INFO_WINDOW), true);
//		//Returning false here will enable native behavior, which shows the info window.
//		if (showInfoWindow) {
//			return false;
//		} else {
//			return true;
//		}
//	}
//
//	@Override
//	public void onMapClick(LatLng point)
//	{
//		if (selectedAnnotation != null) {
//			TiMarker tiMarker = selectedAnnotation.getTiMarker();
//			if (tiMarker != null) {
//				fireClickEvent(tiMarker.getMarker(), selectedAnnotation, null);
//			}
//			selectedAnnotation = null;
//		}
//
//	}
//	
//	@Override
//	public void onMapLongClick(LatLng point)
//	{
//		fireLongClickEvent(point);
//	}
//
//	@Override
//	public void onMarkerDrag(Marker marker)
//	{
//		Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
//	}
//
//	@Override
//	public void onMarkerDragEnd(Marker marker)
//	{
//		AnnotationProxy annoProxy = getProxyByMarker(marker);
//		if (annoProxy != null) {
//			LatLng position = marker.getPosition();
//			annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.longitude);
//			annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
//			firePinChangeDragStateEvent(marker, annoProxy, AkylasMapboxModule.ANNOTATION_DRAG_STATE_END);
//		}
//	}
//
//	@Override
//	public void onMarkerDragStart(Marker marker)
//	{
//		AnnotationProxy annoProxy = getProxyByMarker(marker);
//		if (annoProxy != null) {
//			firePinChangeDragStateEvent(marker, annoProxy, AkylasMapboxModule.ANNOTATION_DRAG_STATE_START);
//		}
//	}
//
//	@Override
//	public void onInfoWindowClick(Marker marker)
//	{
//		AnnotationProxy annoProxy = getProxyByMarker(marker);
//		if (annoProxy != null) {
//			String clicksource = annoProxy.getMapInfoWindow().getClicksource();
//			// The clicksource is null means the click event is not inside "leftPane", "title", "subtible"
//			// or "rightPane". In this case, use "infoWindow" as the clicksource.
//			if (clicksource == null) {
//				clicksource = AkylasMapboxModule.PROPERTY_INFO_WINDOW;
//			}
//			fireClickEvent(marker, annoProxy, clicksource);
//		}
//	}
//
//	@Override
//	public View getInfoContents(Marker marker)
//	{
//		AnnotationProxy annoProxy = getProxyByMarker(marker);
//		if (annoProxy != null) {
//			return annoProxy.getMapInfoWindow();
//		}
//		return null;
//	}
//
//	@Override
//	public View getInfoWindow(Marker marker)
//	{
//		return null;
//	}
//
//	@Override
//	public void release()
//	{
//		selectedAnnotation = null;
//		map.clear();
//		map = null;
//		timarkers.clear();
//		super.release();
//	}
//
//	@Override
//	public void onCameraChange(CameraPosition position)
//	{
//		if (preLayout) {
//			if (preLayoutUpdateBounds != null) {
//				moveCamera(CameraUpdateFactory.newLatLngBounds(preLayoutUpdateBounds, 0), animate);
//				preLayoutUpdateBounds = null;
//			} else {
//				// moveCamera will trigger another callback, so we do this to make sure
//				// we don't fire event when region is set initially
//				preLayout = false;
//			}
//		} else if (proxy != null) {
//			KrollDict d = new KrollDict();
//			d.put(TiC.PROPERTY_LATITUDE, position.target.latitude);
//			d.put(TiC.PROPERTY_LONGITUDE, position.target.longitude);
//			d.put(TiC.PROPERTY_SOURCE, proxy);
//			LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
//			d.put(TiC.PROPERTY_LATITUDE_DELTA, (bounds.northeast.latitude - bounds.southwest.latitude));
//			d.put(TiC.PROPERTY_LONGITUDE_DELTA, (bounds.northeast.longitude - bounds.southwest.longitude));
//			proxy.fireEvent(TiC.EVENT_REGION_CHANGED, d);
//		}
//
//	}
//
//	// Intercept the touch event to find out the correct clicksource if clicking on the info window.
//	@Override
//	protected boolean interceptTouchEvent(MotionEvent ev)
//	{
//		if (ev.getAction() == MotionEvent.ACTION_UP && selectedAnnotation != null) {
//			TiMapInfoWindow infoWindow = selectedAnnotation.getMapInfoWindow();
//			TiMarker timarker = selectedAnnotation.getTiMarker();
//			if (infoWindow != null && timarker != null) {
//				Marker marker = timarker.getMarker();
//				if (marker != null && marker.isInfoWindowShown()) {
//					Point markerPoint = map.getProjection().toScreenLocation(marker.getPosition());
//					infoWindow.analyzeTouchEvent( ev, markerPoint, selectedAnnotation.getIconImageHeight());
//				}
//			}
//		}
//		return false;
//	}
//	
//	public void snapshot() 
//	{
//		map.snapshot(new GoogleMap.SnapshotReadyCallback()
//		{
//			
//			@Override
//			public void onSnapshotReady(Bitmap snapshot)
//			{
//				TiBlob sblob = TiBlob.blobFromImage(snapshot);
//				KrollDict data = new KrollDict();
//				data.put("snapshot", sblob);
//				data.put("source", proxy);
//				proxy.fireEvent(AkylasMapboxModule.EVENT_ON_SNAPSHOT_READY, data);
//			}
//		});
//	}
//
//	@Override
//	public void onMapLoaded()
//	{
//		proxy.fireEvent(TiC.EVENT_COMPLETE, null);
//	}
}
