package akylas.map;

import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.titanium.util.TiUIHelper;

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
        protected MabpoxInfoWindow createInfoWindow(){
            return getProxy().createInfoWindow();
        }
        
        public AnnotationProxy getProxy() {
            return proxy;
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
        Bitmap bitmap = getImage();
        if (bitmap != null) {
            marker.setMarker(new BitmapDrawable(mapView.getResources(), bitmap));
            marker.setUsingMakiIcon(proxy.getImageWithShadow());
        }
        else {
            BitmapDrawable drawable = getColorImage();
            if (drawable != null) {
                marker.setMarker(drawable);
                marker.setUsingMakiIcon(mapView.getDefaultPinIsMaki());
            }
        }
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
       return getMarker(mv, proxy);
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
            marker.closeInfoWindow();
        }
    }

    @Override
    void setPosition(double latitude, double longitude) {
        if (marker!= null) {
            marker.setPoint(new LatLng(latitude, longitude));
        }
    }
    
    @Override
    public void setDraggable(final boolean draggable) {
        if (marker != null) {
            marker.setDraggable(draggable);
        }
    }
    
    @Override
    public void setFlat(final boolean flat) {
    }


    @Override
    void invalidate() {
        if (marker!= null) {
            marker.invalidate();
        }
    }


    @Override
    void setAnchor(PointF anchor) {
        // TODO Auto-generated method stub
        
    }


    @Override
    void setWindowAnchor(PointF anchor) {
    }


    @Override
    void setVisible(boolean visible) {
        if (marker!= null) {
            marker.setVisible(visible);
        }
    }


    @Override
    void setHeading(float heading) {
        // TODO Auto-generated method stub
        
    }
    
    void setImageWithShadow(boolean imageWithShadow) {
        if (marker!= null) {
            marker.setUsingMakiIcon(imageWithShadow);
        }
    }


    @Override
    void setPosition(double latitude, double longitude, double altitude) {
        if (marker!= null) {
            marker.setPoint(new LatLng(latitude, longitude, altitude));
        }
    }
}
