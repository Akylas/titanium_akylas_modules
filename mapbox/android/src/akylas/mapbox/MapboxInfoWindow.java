package akylas.mapbox;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.util.TiUIHelper;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapInfoView;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;

public class MapboxInfoWindow extends InfoWindow {
    protected AnnotationProxy proxy;

    public MapboxInfoWindow(Context context) {
        super(context);
    }
    public MapboxInfoWindow(Context context, AnnotationProxy proxy) {
        super(context);
        setProxy(proxy);
    }
    
    public MapboxInfoWindow(AnnotationProxy proxy) {
        super(proxy.getActivity());
        setProxy(proxy);
    }
    
    public void setProxy(AnnotationProxy proxy) {
        this.proxy = proxy;
//        if (proxy != null) {
//            prepare();
//        }
    }
    
    public void prepare() {
        InfoWindowContainerView view = (InfoWindowContainerView) getContainerView();
        if (view == null) return;
        KrollDict dict = proxy.getProperties();
        if (dict.containsKey(AkylasMapBaseModule.PROPERTY_CALLOUT_ARROW_HEIGHT)) {
            view.setArrowHeight(TiUIHelper.getInPixels(dict, AkylasMapBaseModule.PROPERTY_CALLOUT_ARROW_HEIGHT));
        }
        view.setBorderRadius(TiUIHelper.getInPixels(dict, AkylasMapBaseModule.PROPERTY_CALLOUT_BORDER_RADIUS, 4));
        view.setBackgroundColor(dict.optColor(AkylasMapBaseModule.PROPERTY_CALLOUT_BACKGROUND_COLOR, Color.WHITE));
        proxy.prepareInfoView((AkylasMapInfoView)getInfoView(), dict);
    }
    
    @Override
    protected ViewGroup createContainerView(final Context context) {
        InfoWindowContainerView view = (InfoWindowContainerView) super.createContainerView(context);
        return view;
    }
    
    @Override
    protected View createInfoView(final Context context) {
        return new AkylasMapInfoView(context);
    }
    
    @Override
    public void willOpen(Marker marker) {
        if (marker instanceof MapboxMarker.MapboxRealMarker) {
            setProxy(((MapboxMarker.MapboxRealMarker)marker).getProxy());
        }
        prepare();
        //this is where we should update the template
    }
    
    @Override
    public void didClose() {
        //do that before super so that we still have access to the marker
        if (proxy != null) {
            proxy.mapbBoxInfoWindowDidClose(this);
        }
        super.didClose();
    }
}
