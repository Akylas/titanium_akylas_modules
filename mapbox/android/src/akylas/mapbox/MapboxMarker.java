package akylas.mapbox;

import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.util.TiUIHelper;

import akylas.map.common.AkylasMarker;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;



public class MapboxMarker extends AkylasMarker<LatLng>{
    
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
        protected MapboxInfoWindow createInfoWindow(){
            return ((AnnotationProxy) getProxy()).createInfoWindow();
        }
        
        public boolean hasContent() {
            return getProxy().hasContent();
        }
        
        public MapboxMarker getAkylasMarker() {
            return MapboxMarker.this;
        }
        @Override
        public void showInfoWindow(InfoWindow infoWindow, MapView aMapView, boolean panIntoView) {
            
            PointF anchor = proxy.getCalloutAnchor();
            if (anchor != null) {
                infoWindow.setAnchor(anchor);
            }
            super.showInfoWindow(infoWindow, aMapView, panIntoView);
        }
        
        public AnnotationProxy getProxy() {
            return (AnnotationProxy) getAkylasMarker().getProxy();
        }
    }
    
    private Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {

        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Config.ARGB_8888);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(sourceBitmap, 0, 0, p);
        return resultBitmap;
    }
    
    
    protected BitmapDrawable getColorImage() {
        BitmapDrawable drawable = (BitmapDrawable) mapView.getDefaultPinDrawable();
        String value = TiConvert.toString(proxy.getProperty(TiC.PROPERTY_COLOR));
        if (value != null) {
            int color = TiConvert.toColor(value);
            if (color != Color.TRANSPARENT) {
                BitmapDrawable bitmapDrawable;
                try {
                    bitmapDrawable = (BitmapDrawable) TiUIHelper.getResourceDrawable(TiRHelper.getResource("drawable.graypin"));
                    if (bitmapDrawable != null) {
                        return new BitmapDrawable(mapView.getResources(), changeBitmapColor(bitmapDrawable.getBitmap(), color));
                    }
                } catch (ResourceNotFoundException e) {
                }
                
            }
        }
        return drawable;
    }
    
    protected void handleSetImage(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (marker != null) {
            marker.setMarker(new BitmapDrawable(mapView.getResources(), bitmap));
            marker.setUsingMakiIcon(proxy.getImageWithShadow());
        }
        
    };

    Marker getMarker(MapView mv, AnnotationProxy proxy) {
        this.mapView = mv;
        if (marker == null) {
            marker = new MapboxRealMarker(mv, proxy.getTitle(), proxy.getSubtitle(), proxy.getPosition());
        }
        else {
            marker.setTitle(proxy.getTitle());
            marker.setDescription(proxy.getSubtitle());
            marker.setPoint(proxy.getPosition());
        }
        marker.setVisible(proxy.visible);

        BitmapDrawable drawable = getColorImage();
        if (drawable != null) {
            marker.setMarker(drawable);
            marker.setUsingMakiIcon(mapView.getDefaultPinIsMaki());
        }
        getImage();
        PointF anchor = proxy.getAnchor();
        if (anchor != null) {
            marker.setAnchor(anchor);
        }
        marker.setDraggable(proxy.getDraggable());
        float minZoom = proxy.getMinZoom();
        if (minZoom >= 0) {
            marker.setMinZoom(minZoom);
        }
        float maxZoom = proxy.getMaxZoom();
        if (maxZoom >= 0) {
            marker.setMaxZoom(maxZoom);
        }
        marker.setSortkey(proxy.getSortKey());
        return marker;
    }
    
    Marker getMarker(MapView mv) {
       return getMarker(mv, (AnnotationProxy) proxy);
    }
    
    public Marker getMarker() {
        return marker;
    }

    @Override
    public double getLatitude() {
       if (marker!= null) {
           return marker.getPoint().getLatitude();
       }
        return 0;
    }

    @Override
    public double getLongitude() {
        if (marker!= null) {
            return marker.getPoint().getLongitude();
        }
        return 0;
    }

    @Override
    public double getAltitude() {
        if (marker!= null) {
            return marker.getPoint().getAltitude();
        }
        return 0;
    }

    @Override
    public void showInfoWindow() {
        if (marker!= null) {
            mapView.selectMarker(marker);
        }
    }

    @Override
    public void hideInfoWindow() {
        if (marker!= null) {
            marker.closeInfoWindow();
        }
    }

    @Override
    public void setPosition(final LatLng point) {
        if (marker!= null) {
            marker.setPoint(point);
        }
    }
    
    @Override
    public void setDraggable(final boolean draggable) {
        if (marker != null) {
            marker.setDraggable(draggable);
        }
    }
    
    @Override
    public void invalidate() {
        if (marker!= null) {
            marker.invalidate();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (marker!= null) {
            marker.setVisible(visible);
        }
    }
    
    @Override
    public void setImageWithShadow(boolean imageWithShadow) {
        if (marker!= null) {
            marker.setUsingMakiIcon(imageWithShadow);
        }
    }


}
