package akylas.mapboxgl;

import org.appcelerator.kroll.annotations.Kroll;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterItem;

import akylas.map.common.BaseAnnotationProxy;

@Kroll.proxy(creatableInModule = AkylasMapboxGLModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<LatLng>
        implements ClusterItem {
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Annotation";
    }

    @Override
    public void infoWindowDidClose() {
        if (mapView != null) {
            ((AkMapView) mapView).infoWindowDidClose(infoView);
        }
        infoView = null;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void removeFromMap() {
        if (marker != null) {
            ((MGLMarker) marker).removeFromMap();
        }
    }

    public MarkerOptions getMarkerOptions() {
        if (marker != null) {
            return ((MGLMarker) getMarker()).getMarkerOptions();
        }
        return null;
    }

    @Override
    public Marker getMarker(MapboxMap map) {
        return map.addMarker(getMarkerOptions());
    }

    @Override
    public boolean canBeClustered() {
        return !selected && canBeClustered;
    }
}
