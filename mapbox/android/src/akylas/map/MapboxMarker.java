package akylas.map;

import com.google.android.gms.maps.model.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;



public class MapboxMarker extends AkylasMarker{
    
    public MapboxMarker(AnnotationProxy p) {
        super(p);
    }

    private Marker marker;
    private MapView mapView;
    
    public class MapboxRealMarker extends Marker{

        public MapboxRealMarker(MapView mv, String aTitle, String aDescription,
                LatLng aLatLng) {
            super(mv, aTitle, aDescription, aLatLng);
        }
        
        public MapboxRealMarker(String aTitle, String aDescription,
                LatLng aLatLng) {
            this(null, aTitle, aDescription, aLatLng);
        }
        
        @Override
        protected InfoWindow createTooltip(MapView mv){
            return super.createTooltip(mv);
        }
        
        public AnnotationProxy getProxy() {
            return proxy;
        }
        
        public MapboxMarker getAkylasMarker() {
            return MapboxMarker.this;
        }
    }

    Marker getMarker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        this.mapView = mv;
        if (marker == null) {
            marker = new MapboxRealMarker(mv, aTitle, aDescription, aLatLng);
        }
        else {
            marker.setTitle(aTitle);
            marker.setDescription(aDescription);
            marker.setPoint(aLatLng);
        }
        
        return marker;
    }
    
    Marker getMarker(MapView mv, MarkerOptions options) {
       return getMarker(mv, options.getTitle(), 
               options.getSnippet(), 
              AkylasMapModule.googleToMapbox(options.getPosition()));
    }
    
    public Marker getMarker() {
        return marker;
    }

    @Override
    double getLatitude() {
       if (marker!= null) {
           return marker.getPoint().getLatitude();
       }
        return 0;
    }

    @Override
    double getLongitude() {
        if (marker!= null) {
            return marker.getPoint().getLongitude();
        }
        return 0;
    }

    @Override
    double getAltitude() {
        if (marker!= null) {
            return marker.getPoint().getAltitude();
        }
        return 0;
    }

    @Override
    void showInfoWindow() {
        if (marker!= null) {
            mapView.selectMarker(marker);
        }
    }

    @Override
    void hideInfoWindow() {
        if (marker!= null) {
            marker.closeToolTip();
        }
    }

    @Override
    void setPosition(double latitude, double longitude) {
        if (marker!= null) {
            marker.setPoint(new LatLng(latitude, longitude));
        }
    }
}
