/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

package akylas.map;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;

import akylas.map.AnnotationProxy;
import akylas.map.RouteProxy;
import akylas.map.AkylasMarker;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class AkylasMapView extends AkylasMapDefaultView implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
	GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter,
	GoogleMap.OnMapLongClickListener, GoogleMap.OnMapLoadedCallback
{
    private static int viewId = 1000;
	private static final String TAG = "AkylasMapView";
    private GoogleMap map;
    private View mapView;
	protected boolean animate = false;
	protected boolean preLayout = true;
	protected LatLngBounds preLayoutUpdateBounds;
	protected ArrayList<AkylasMarker> timarkers;
	protected AnnotationProxy selectedAnnotation;
    private Fragment fragment;
    private Handler handler;
    
    private boolean googlePlayServicesAvailable = false;

	public AkylasMapView(final TiViewProxy proxy, Activity activity)
	{
		super(proxy);
		googlePlayServicesAvailable = ((MapViewProxy)proxy).googlePlayServicesAvailable();
		TiCompositeLayout container = new TiCompositeLayout(activity, this)
        {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev)
            {
                return interceptTouchEvent(ev) || super.dispatchTouchEvent(ev);
            }
        };
        container.setId(viewId++);
        setNativeView(container);
        if (googlePlayServicesAvailable) {
            FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
            Fragment tabFragment = manager.findFragmentById(android.R.id.tabcontent);
            FragmentTransaction transaction = null;
            //check if this map is opened inside an actionbar tab, which is another fragment
            if (tabFragment != null) {
                FragmentManager childManager = tabFragment.getChildFragmentManager();
                transaction = childManager.beginTransaction();
            } else {
                transaction = manager.beginTransaction();
            }
            fragment = createFragment();
            transaction.add(container.getId(), fragment);
            transaction.commit();
            // initialize handler
            handler = new Handler(TiMessenger.getMainMessenger().getLooper(), this);
            // send a msg to skip a cycle to make sure the fragment's view is created and initialized
            handler.obtainMessage().sendToTarget();
        }

        
		timarkers = new ArrayList<AkylasMarker>();
	}

	/**
	 * Traverses through the view hierarchy to locate the SurfaceView and set the background to transparent.
	 * @param v the root view
	 */
	private void setBackgroundTransparent(View v) {
	    if (v instanceof SurfaceView) {
	        SurfaceView sv = (SurfaceView) v;
	        sv.setBackgroundColor(Color.TRANSPARENT);
	    }

	    if (v instanceof ViewGroup) {
	        ViewGroup viewGroup = (ViewGroup) v;
	        for (int i = 0; i < viewGroup.getChildCount(); i++) {
	            setBackgroundTransparent(viewGroup.getChildAt(i));
	        }
	    }
	}
	
	public Fragment getFragment()
    {
        return fragment;
    }

    public boolean handleMessage(Message msg)
    {
        // we know here that the view is available, so we can process properties
        onViewCreated();
        return true;
    }
    
    @Override
    public void release()
    {
        if (fragment != null) {
            FragmentManager fragmentManager = fragment.getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction transaction = null;
                Fragment tabFragment = fragmentManager.findFragmentById(android.R.id.tabcontent);
                if (tabFragment != null) {
                    FragmentManager childManager = tabFragment.getChildFragmentManager();
                    transaction = childManager.beginTransaction();
                } else {
                    transaction = fragmentManager.beginTransaction();
                }
                transaction.remove(fragment);
                transaction.commit();
            }
        }
        selectedAnnotation = null;
        if (map != null) {
            map.clear();
            map = null;
        }
        
        timarkers.clear();
        super.release();
    }

	protected Fragment createFragment()
	{
		if (proxy == null) {
			return SupportMapFragment.newInstance();
		} else {
			boolean zOrderOnTop = TiConvert.toBoolean(proxy.getProperty(AkylasMapModule.PROPERTY_ZORDER_ON_TOP), false);
			GoogleMapOptions gOptions = new GoogleMapOptions();
			gOptions.zOrderOnTop(zOrderOnTop);
			return SupportMapFragment.newInstance(gOptions);
		}
	}

	protected void onViewCreated()
	{
		acquireMap();
		//A workaround for https://code.google.com/p/android/issues/detail?id=11676 pre Jelly Bean.
		//This problem doesn't exist on 4.1+ since the map base view changes to TextureView from SurfaceView. 
		if (Build.VERSION.SDK_INT < 16) {
			View rootView = proxy.getActivity().findViewById(android.R.id.content);
			setBackgroundTransparent(rootView);
		}
		map.setOnMarkerClickListener(this);
		map.setOnMapClickListener(this);
		map.setOnCameraChangeListener(this);
		map.setOnMarkerDragListener(this);
		map.setOnInfoWindowClickListener(this);
		map.setInfoWindowAdapter(this);
		map.setOnMapLongClickListener(this);
		map.setOnMapLoadedCallback(this);
		processMapProperties(proxy.getProperties());
	}

	@Override
	public void processProperties(KrollDict d)
	{
		super.processProperties(d);
	}

	@Override
	public void processPreMapProperties(final KrollDict d)
	{
	    super.processPreMapProperties(d);
		if (d.containsKey(AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON)) {
			setUserLocationButtonEnabled(TiConvert.toBoolean(d, AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON, true));
		}
		if (d.containsKey(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(d.getInt(TiC.PROPERTY_MAP_TYPE));
		}
		if (d.containsKey(AkylasMapModule.PROPERTY_TRAFFIC)) {
			setTrafficEnabled(d.getBoolean(AkylasMapModule.PROPERTY_TRAFFIC));
		}
		if (d.containsKey(TiC.PROPERTY_ENABLE_ZOOM_CONTROLS)) {
			setZoomControlsEnabled(TiConvert.toBoolean(d, TiC.PROPERTY_ENABLE_ZOOM_CONTROLS, true));
		}
		if (d.containsKey(AkylasMapModule.PROPERTY_COMPASS_ENABLED)) {
			setCompassEnabled(TiConvert.toBoolean(d, AkylasMapModule.PROPERTY_COMPASS_ENABLED, true));
		}
	}
	
	@Override
    public void processMapProperties(final KrollDict d)
    {
        if (acquireMap() == null) return;
        super.processMapProperties(d);
    }
	
	@Override
    public void processPostMapProperties(final KrollDict d, final boolean animated)
    {
	    super.processPostMapProperties(d, animated);
    }

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy)
	{
		if (key.equals(AkylasMapModule.PROPERTY_USER_LOCATION_BUTTON)) {
			setUserLocationButtonEnabled(TiConvert.toBoolean(newValue));
		} else if (key.equals(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(TiConvert.toInt(newValue));
		} else if (key.equals(AkylasMapModule.PROPERTY_TRAFFIC)) {
			setTrafficEnabled(TiConvert.toBoolean(newValue));
		} else if (key.equals(AkylasMapModule.PROPERTY_COMPASS_ENABLED)) {
			setCompassEnabled(TiConvert.toBoolean(newValue, true));
		} else if (key.equals(TiC.PROPERTY_ENABLE_ZOOM_CONTROLS)) {
			setZoomControlsEnabled(TiConvert.toBoolean(newValue, true));
		} else {
			super.propertyChanged(key, oldValue, newValue, proxy);
		}
	}
    @Override
    public void processMapPositioningProperties(final KrollDict d, final boolean animated) {
        updateCamera(d, animated);
    }

	public GoogleMap acquireMap()
	{
	    
	    if (googlePlayServicesAvailable) {
	        if (map == null) {
	            mapView = ((SupportMapFragment) getFragment()).getView();
	            map = ((SupportMapFragment) getFragment()).getMap();
	        }
	        return ((SupportMapFragment) getFragment()).getMap();
	    }
	    return null;
	}

	public GoogleMap getMap()
	{
		return map;
	}

	protected void setUserLocationEnabled(boolean enabled)
	{
		map.setMyLocationEnabled(enabled);
	}
    
	protected void setCompassEnabled(boolean enabled) 
	{
		map.getUiSettings().setCompassEnabled(enabled);
	}

	protected void setUserLocationButtonEnabled(boolean enabled)
	{
		map.getUiSettings().setMyLocationButtonEnabled(enabled);
	}

	public float getMaxZoomLevel() 
	{
		return map.getMaxZoomLevel();
	}
		
	public float getMinZoomLevel() 
	{
		return map.getMinZoomLevel();
	}

	protected void setMapType(int type)
	{
		map.setMapType(type);
	}

	protected void setTrafficEnabled(boolean enabled)
	{
		map.setTrafficEnabled(enabled);
	}

	protected void setZoomControlsEnabled(boolean enabled)
	{
		map.getUiSettings().setZoomControlsEnabled(enabled);
	}

	public void updateCamera(HashMap<String, Object> dict, final boolean animated)
	{
	    if (preLayout) return;
		float bearing = 0;
		float tilt = 0;
		float zoom = 0;

		// In the setLocation() method, the old map module allows the user to provide two more properties - "animate" and "regionFit".
		// In this map module, no matter "regionFit" is set to true or false, we will always make sure the specified 
		// latitudeDelta / longitudeDelta bounds are centered on screen at the greatest possible zoom level.
		boolean anim = animated;
		if (dict.containsKey(TiC.PROPERTY_ANIMATE)) {
			anim &= TiConvert.toBoolean(dict, TiC.PROPERTY_ANIMATE);
		}
		boolean regFit = regionFit;
        if (dict.containsKey(TiC.PROPERTY_REGION_FIT)) {
            regFit = TiConvert.toBoolean(dict, TiC.PROPERTY_REGION_FIT);
        }
		if (dict.containsKey(AkylasMapModule.PROPERTY_BEARING)) {
			bearing = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_BEARING);
		}
		if (dict.containsKey(AkylasMapModule.PROPERTY_TILT)) {
			tilt = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_TILT);
		}
		if (dict.containsKey(AkylasMapModule.PROPERTY_ZOOM)) {
            zoom = TiConvert.toFloat(dict, AkylasMapModule.PROPERTY_ZOOM);
        }
		
		boolean regionUpdate = false;
        LatLngBounds region = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng center = region.getCenter();
		if (dict.containsKey(TiC.PROPERTY_REGION)) {
            regionUpdate = true;
		    region = AkylasMapModule.mapBoxToGoogle(AkylasMapModule.regionFromDict(dict.get(TiC.PROPERTY_REGION)));
		}
		
		if (dict.containsKey(AkylasMapModule.PROPERTY_CENTER_COORDINATE)) {
		    center = AkylasMapModule.mapBoxToGoogle(AkylasMapModule.latlongFromObject(dict.get(AkylasMapModule.PROPERTY_CENTER_COORDINATE)));
        }

		CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
		cameraBuilder.target(center);
		cameraBuilder.bearing(bearing);
		cameraBuilder.tilt(tilt);
		cameraBuilder.zoom(zoom);

		if (regionUpdate) {
    		moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0), anim);
		}
		else {
		    CameraPosition position = cameraBuilder.build();
	        CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
	        moveCamera(camUpdate, anim);
		}
	}

	protected void moveCamera(CameraUpdate camUpdate, boolean anim)
	{
		if (anim) {
			map.animateCamera(camUpdate);
		} else {
			map.moveCamera(camUpdate);
		}
	}
	

	private AnnotationProxy getProxyByMarker(Marker m)
	{
		if (m != null) {
			for (int i = 0; i < timarkers.size(); i++) {
				GoogleMapMarker timarker = (GoogleMapMarker) timarkers.get(i);
				if (m.equals(timarker.getMarker())) {
					return timarker.getProxy();
				}
			}
		}
		return null;
	}
	
	protected void changeZoomLevel(final float level)
    {
	    changeZoomLevel(level, animate);
    }
    @Override
	protected void changeZoomLevel(final float level, final boolean animated)
	{
        if (preLayout) return;
        CameraUpdate camUpdate = CameraUpdateFactory.zoomBy(level);
        moveCamera(camUpdate, animated);
	}
	
	protected void fireEventOnMap(String type, LatLng point) {
        if (!hasListeners(type))
            return;
        KrollDict d = new KrollDict();
        d.put(TiC.PROPERTY_LATITUDE, point.latitude);
        d.put(TiC.PROPERTY_LONGITUDE, point.longitude);
        d.put(TiC.PROPERTY_REGION, getRegionDict());
        d.put(AkylasMapModule.PROPERTY_MAP, proxy);
        fireEvent(type, d, true, false);
    }

	public void fireLongClickEvent(LatLng point)
	{
	    fireEventOnMap(TiC.EVENT_LONGCLICK, point);
	}
	
	public void fireClickEvent(final Marker marker, final String source)
    {
	    AnnotationProxy proxy = getProxyByMarker(marker);
	    fireEventOnMarker(TiC.EVENT_CLICK, proxy.getMarker(), source);
    }

	public void firePinChangeDragStateEvent(final Marker marker, final AnnotationProxy annoProxy, int dragState)
	{
	    if (proxy.hasListeners(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE, false)) {
	        KrollDict d = new KrollDict();

	        d.put(TiC.PROPERTY_TITLE, annoProxy.getTitle());
	        d.put(TiC.PROPERTY_SUBTITLE, annoProxy.getSubtitle());
	        d.put(TiC.PROPERTY_ANNOTATION, annoProxy);
	        d.put(TiC.PROPERTY_SOURCE, proxy);
	        d.put(AkylasMapModule.PROPERTY_NEWSTATE, dragState);
	        proxy.fireEvent(AkylasMapModule.EVENT_PIN_CHANGE_DRAG_STATE, d, false, false);
	    }
		
	}

	@Override
	public boolean onMarkerClick(Marker marker)
	{
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (annoProxy == null) {
			Log.e(TAG, "Marker can not be found, click event won't fired.", Log.DEBUG_MODE);
			return false;
		}
		if (selectedAnnotation != null) {
            selectedAnnotation.hideInfo();
		}
		if (!annoProxy.equals(selectedAnnotation)) {
            selectedAnnotation = annoProxy;
        }
        else {
            selectedAnnotation = null;
        }
		fireClickEvent(marker, AkylasMapModule.PROPERTY_PIN);
		selectedAnnotation = annoProxy;
		boolean showInfoWindow = TiConvert.toBoolean(annoProxy.getProperty(AkylasMapModule.PROPERTY_SHOW_INFO_WINDOW), true);
		//Returning false here will enable native behavior, which shows the info window.
		if (showInfoWindow) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onMapClick(LatLng point)
	{
		if (selectedAnnotation != null) {
		    deselectAnnotation(selectedAnnotation);
		}
        fireEventOnMap(TiC.EVENT_CLICK, point);
	}
	
	@Override
	public void onMapLongClick(LatLng point)
	{
		fireLongClickEvent(point);
	}

	@Override
	public void onMarkerDrag(Marker marker)
	{
		Log.d(TAG, "The annotation is dragged.", Log.DEBUG_MODE);
	}

	@Override
	public void onMarkerDragEnd(Marker marker)
	{
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (annoProxy != null) {
			LatLng position = marker.getPosition();
			annoProxy.setProperty(TiC.PROPERTY_LONGITUDE, position.longitude);
			annoProxy.setProperty(TiC.PROPERTY_LATITUDE, position.latitude);
			firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_END);
		}
	}

	@Override
	public void onMarkerDragStart(Marker marker)
	{
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (annoProxy != null) {
			firePinChangeDragStateEvent(marker, annoProxy, AkylasMapModule.ANNOTATION_DRAG_STATE_START);
		}
	}

	@Override
	public void onInfoWindowClick(Marker marker)
	{
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (annoProxy != null) {
			String clicksource = annoProxy.getMapInfoWindow().getClicksource();
			// The clicksource is null means the click event is not inside "leftPane", "title", "subtible"
			// or "rightPane". In this case, use "infoWindow" as the clicksource.
			if (clicksource == null) {
				clicksource = AkylasMapModule.PROPERTY_INFO_WINDOW;
			}
			fireClickEvent(marker, clicksource);
		}
	}

	@Override
	public View getInfoContents(Marker marker)
	{
		AnnotationProxy annoProxy = getProxyByMarker(marker);
		if (annoProxy != null) {
			return annoProxy.getOrCreateMapInfoWindow();
		}
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker)
	{
		return null;
	}

	@Override
	public void onCameraChange(CameraPosition position)
	{
		if (preLayout) {
			
				// moveCamera will trigger another callback, so we do this to make sure
				// we don't fire event when region is set initially
				preLayout = false;
			updateCamera(getProxy().getProperties(), false);
		} else if (proxy != null) {
			LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
			proxy.fireEvent(TiC.EVENT_REGION_CHANGED, AkylasMapModule.regionToDict(bounds));
		}

	}

	// Intercept the touch event to find out the correct clicksource if clicking on the info window.
	protected boolean interceptTouchEvent(MotionEvent ev)
	{
		if (ev.getAction() == MotionEvent.ACTION_UP && selectedAnnotation != null) {
			AkylasMapInfoWindow infoWindow = selectedAnnotation.getMapInfoWindow();
			AkylasMarker timarker = selectedAnnotation.getMarker();
			if (infoWindow != null && timarker != null) {
				Marker marker = ((GoogleMapMarker) timarker).getMarker();
				if (marker != null && marker.isInfoWindowShown()) {
					Point markerPoint = map.getProjection().toScreenLocation(marker.getPosition());
					infoWindow.analyzeTouchEvent( ev, markerPoint, selectedAnnotation.getIconImageHeight());
				}
			}
		}
		return false;
	}
	
	public void snapshot() 
	{
		map.snapshot(new GoogleMap.SnapshotReadyCallback()
		{
			
			@Override
			public void onSnapshotReady(Bitmap snapshot)
			{
				TiBlob sblob = TiBlob.blobFromImage(snapshot);
				KrollDict data = new KrollDict();
				data.put("snapshot", sblob);
				data.put("source", proxy);
				proxy.fireEvent(AkylasMapModule.EVENT_ON_SNAPSHOT_READY, data);
			}
		});
	}

	@Override
	public void onMapLoaded()
	{
		proxy.fireEvent(TiC.EVENT_COMPLETE, null);
	}

    @Override
    public KrollDict getUserLocation() {
        return AkylasMapModule.locationToDict(map.getMyLocation());
    }

    @Override
    boolean getUserLocationEnabled() {
        return map.isMyLocationEnabled();
    }

    @Override
    int getUserTrackingMode() {
        return 0;
    }

    @Override
    void handleMinZoomLevel(float level) {
        
    }

    @Override
    void handleMaxZoomLevel(float level) {
        
    }

    @Override
    void setUserTrackingMode(int mode) {
    }
    
    @Override
    void updateCenter(Object dict, boolean animated) {
        LatLng center = AkylasMapModule.mapBoxToGoogle(AkylasMapModule.latlongFromObject(dict)) ;
        if (center != null) {
            if (preLayout) return;
                CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
                cameraBuilder.target(center);
                CameraPosition position = cameraBuilder.build();
                CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
                moveCamera(camUpdate, animated);
            
        }
    }

    @Override
    void updateRegion(Object dict, boolean animated) {
        LatLngBounds region = AkylasMapModule.mapBoxToGoogle(AkylasMapModule.regionFromDict(dict)) ;
        if (region != null) {
            if (preLayout) return;
                moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0), animated);
        }
    }

    @Override
    void updateScrollableAreaLimit(Object dict) {
        // TODO Auto-generated method stub
        
    }

    @Override
    void selectUserAnnotation() {
        updateCenter(getUserLocation(), animate);
    }

    @Override
    void zoomIn() {
        float currentZoom  = map.getCameraPosition().zoom;
        float targetZoom = (float) (Math.ceil(currentZoom) + 1);
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 2.25) {
            targetZoom = (float) Math.ceil(currentZoom);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    void zoomIn(com.mapbox.mapboxsdk.geometry.LatLng about, boolean userAction) {
        // TODO Auto-generated method stub
        zoomIn();
    }

    @Override
    void zoomOut() {
        float currentZoom  = map.getCameraPosition().zoom;
        float targetZoom = (float) (Math.floor(currentZoom));
        float factor = (float) Math.pow(2, targetZoom - currentZoom);

        if (factor > 0.75) {
            targetZoom = (float) (Math.floor(currentZoom) - 1);
        }
        changeZoomLevel(targetZoom, animate);
    }

    @Override
    void zoomOut(com.mapbox.mapboxsdk.geometry.LatLng about, boolean userAction) {
        // TODO Auto-generated method stub
        zoomOut();
    }

    @Override
    KrollDict getRegionDict() {
        LatLngBounds region = map.getProjection().getVisibleRegion().latLngBounds;
        return AkylasMapModule.regionToDict(region);
    }

    @Override
    void handleDeselectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).hideInfoWindow();
    }

    @Override
    void handleSelectMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).showInfoWindow();
    }

    @Override
    void handleAddRoute(RouteProxy route) {
        route.setPolyline(map.addPolyline(route.getAndSetOptions()));
    }

    @Override
    void handleRemoveRoute(RouteProxy route) {
        route.removePolyline();
    }

    @Override
    void handleAddAnnotation(AnnotationProxy annotation) {
        AkylasMarker marker = annotation.getMarker();
        if (marker != null) {
            //already in
            removeAnnotation(marker);
        }
        else {
            Marker googlemarker = map.addMarker(annotation.getMarkerOptions());
            annotation.setMarker(new GoogleMapMarker(googlemarker, annotation));
            timarkers.add(annotation.getMarker());
        }
    }

    @Override
    void handleRemoveMarker(AkylasMarker marker) {
        ((GoogleMapMarker) marker).removeFromMap();
        AnnotationProxy proxy = marker.getProxy();
        if (proxy != null) {
            proxy.setMarker(null);
        }
    }
}
