package akylas.map;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapMarker extends AkylasMarker{
    private Marker marker;
    
    public GoogleMapMarker(final Marker m, final AnnotationProxy p) {
        super(p);
        marker = m;
    }
    
    public void setMarker(Marker m) {
        marker = m;
    }
    public Marker getMarker() {
        return marker;
    }
    
    public AnnotationProxy getProxy() {
        return proxy;
    }
    
    public void removeFromMap() {
        if (marker != null) {
            marker.remove();
        }
    }

    @Override
    double getLatitude() {
        if (marker != null) {
            return marker.getPosition().latitude;
        }
        return 0;
    }

    @Override
    double getLongitude() {
        if (marker != null) {
            return marker.getPosition().longitude;
        }
        return 0;
    }

    @Override
    double getAltitude() {
        return 0;
    }

    public void showInfoWindow() {
        if (marker != null) {
            marker.showInfoWindow();
        }
    }

    public void hideInfoWindow() {
        if (marker != null) {
            marker.hideInfoWindow();
        }
    }

    @Override
    void setPosition(double latitude, double longitude) {
        if (marker != null) {
            LatLng position = new LatLng(latitude, longitude);
            marker.setPosition(position);
        }
    }
}
