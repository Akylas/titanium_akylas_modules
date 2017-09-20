package akylas.carto;

import org.appcelerator.kroll.annotations.Kroll;

import com.carto.core.MapPos;
import com.carto.styles.MarkerStyle;
import com.carto.vectorelements.Marker;

//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.maps.android.clustering.ClusterItem;


import akylas.map.common.BaseAnnotationProxy;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class)
public class AnnotationProxy extends BaseAnnotationProxy<MapPos>  {
    
    class AkMarker extends Marker {
        AnnotationProxy proxy;
        public AkMarker(AnnotationProxy proxy, MapPos pos, MarkerStyle style) {
            super(pos, style);
            this.proxy = proxy;
        }
        public AnnotationProxy getProxy() {
            // TODO Auto-generated method stub
            return this.proxy;
        }
        
    }
    @Override
    public String getApiName() {
        return "Akylas.Carto.Annotation";
    }

    @Override
    public void infoWindowDidClose() {
        if (mapView != null) {
            ((CartoView) mapView).infoWindowDidClose(infoView);
        }
        infoView = null;
    }

//    @Override
//    public boolean isVisible() {
//        return visible;
//    }

    @Override
    public void removeFromMap() {
//        if (marker != null) {
//            ((VectorElementMarker) marker).removeFromMap();
//        }
    }

    public MarkerStyle getMarkerBuildStyle() {
        if (marker != null) {
            return ((CartoMarker) getMarker()).getMarkerBuildStyle();
        }
        return null;
    }

    public AkMarker createMarker() {
       MapPos pos = this.getPosition();
        return new AkMarker(this, pos, getMarkerBuildStyle());
//        return map.addMarker(getMarkerOptions());
    }

//    @Override
//    public boolean canBeClustered() {
//        return !selected && canBeClustered;
//    }
}
