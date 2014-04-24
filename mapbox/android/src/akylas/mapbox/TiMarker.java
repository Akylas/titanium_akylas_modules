package akylas.mapbox;

import org.appcelerator.titanium.TiApplication;

import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

public class TiMarker extends Marker {
	private AnnotationProxy proxy;
	private AkylasTooltipView tooltip;	
    private MapView mapView;

    public TiMarker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(mv, aTitle, aDescription, aLatLng);
        this.mapView = mv;
    }

    public TiMarker(String aTitle, String aDescription, LatLng aLatLng) {
        this(null, aTitle, aDescription, aLatLng);
    }
	
	private AkylasTooltipView getOrCreateTooltip()
	{
		if (tooltip == null) {
			tooltip = new AkylasTooltipView(TiApplication.getInstance().getApplicationContext());
		}
		return tooltip;
	}
	
	private void setProxy(AnnotationProxy proxy) {
		this.proxy = proxy;
	}
	
	public AnnotationProxy getProxy() {
		return proxy;
	}
	
	private void attachTooltip() {
		getOrCreateTooltip();
		if (mapView != null) {
//			mapView.getOverlays().add(tooltip);
	        mapView.invalidate();
		}
        
    }
	
	@Override
	protected InfoWindow createTooltip(MapView mv){
        return super.createTooltip(mv);
    }

    public void setTooltipVisible() {
        tooltip.setVisibility(View.VISIBLE);
    }

    public void setTooltipInvisible() {
        tooltip.setVisibility(View.GONE);
    }
}
