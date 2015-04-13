package akylas.googleMap;

import org.appcelerator.kroll.annotations.Kroll;

import com.google.android.gms.maps.model.LatLng;

import akylas.map.common.BaseAnnotationProxy;

@Kroll.proxy(creatableInModule = AkylasGoogleMapModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<LatLng> {
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.Annotation";
    }
    
    @Override
    public void infoWindowDidClose() {
        ((GoogleMapView)mapView).infoWindowDidClose(infoView);
        infoView = null;
    }
}
