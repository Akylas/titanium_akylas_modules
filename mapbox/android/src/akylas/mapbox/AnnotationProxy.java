package akylas.mapbox;

import org.appcelerator.kroll.annotations.Kroll;

import com.mapbox.mapboxsdk.geometry.LatLng;

import akylas.map.common.BaseAnnotationProxy;

@Kroll.proxy(creatableInModule = AkylasMapboxModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<LatLng> {
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Annotation";
    }
    
    @Override
    public void setMinZoom(Object value) {
        super.setMinZoom(value);
        if (marker != null) {
            ((MapboxMarker) marker).getMarker().setMinZoom(mMinZoom);
        }
    }
    
    @Override
    public void setMaxZoom(Object value) {
        super.setMaxZoom(value);
        if (marker != null) {
            ((MapboxMarker) marker).getMarker().setMaxZoom(mMaxZoom);
        }
    }
    
    public MapboxInfoWindow createInfoWindow() {
        return ((MapboxView)mapView).createInfoWindow(this);
    }

    public void mapbBoxInfoWindowDidClose(MapboxInfoWindow mabpoxInfoWindow) {
        infoView = null;
        ((MapboxView)mapView).infoWindowDidClose(mabpoxInfoWindow);
    }
}
