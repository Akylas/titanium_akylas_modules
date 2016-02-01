package akylas.googlemap;

import org.appcelerator.kroll.annotations.Kroll;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

import akylas.map.common.BaseAnnotationProxy;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<LatLng>
        implements ClusterItem {
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Annotation";
    }

    @Override
    public void infoWindowDidClose() {
        if (mapView != null) {
            ((GoogleMapView) mapView).infoWindowDidClose(infoView);
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
            ((GoogleMapMarker) marker).removeFromMap();
        }
    }

    public MarkerOptions getMarkerOptions() {
        if (marker != null) {
            return ((GoogleMapMarker) getMarker()).getMarkerOptions();
        }
        return null;
    }

    @Override
    public Marker getMarker(GoogleMap map) {
        return map.addMarker(getMarkerOptions());
    }

    @Override
    public boolean canBeClustered() {
        return !selected && canBeClustered;
    }
}
