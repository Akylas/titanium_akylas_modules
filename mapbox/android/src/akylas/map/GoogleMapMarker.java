package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiActivityHelper;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiActivityHelper.CommandNoReturn;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapMarker extends AkylasMarker {
    private static final String TAG = "GoogleMapMarker";
    private MarkerOptions markerOptions = null;
    private Marker marker;

    private static final String defaultIconImageHeight = "40dip"; // The height
    // of the
    // default
    // marker icon
    // The height of the marker icon in the unit of "px". Will use it to analyze
    // the touch event to find out
    // the correct clicksource for the click event.
    private int iconImageHeight = 0;

    public GoogleMapMarker(final AnnotationProxy p) {
        super(p);
    }

    private void handleCustomView(Object obj) {
        if (obj instanceof TiViewProxy) {
            TiBlob imageBlob = ((TiViewProxy) obj).toImage(null, 1);
            if (imageBlob != null) {
                Bitmap image = ((TiBlob) imageBlob).getImage();
                if (image != null) {
                    markerOptions.icon(BitmapDescriptorFactory
                            .fromBitmap(image));
                    setIconImageHeight(image.getHeight());
                    return;
                }
            }
        }
        Log.w(TAG, "Unable to get the image from the custom view: " + obj);
        setIconImageHeight(-1);
    }

    private void handleImage(Object image) {
        Bitmap bitmap = getImage();
        if (bitmap != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            setIconImageHeight(bitmap.getHeight());
            return;
        }
        Log.w(TAG, "Unable to get the image from the path: " + image);
        setIconImageHeight(-1);
    }

    private void setIconImageHeight(int h) {
        if (h >= 0) {
            iconImageHeight = h;
        } else { // default maker icon
            TiDimension dimension = new TiDimension(defaultIconImageHeight,
                    TiDimension.TYPE_UNDEFINED);
            // TiDimension needs a view to grab the window manager, so we'll
            // just use the decorview of the current window
            View view = TiApplication.getAppCurrentActivity().getWindow()
                    .getDecorView();
            iconImageHeight = dimension.getAsPixels(view);
        }
    }

    public int getIconImageHeight() {
        return iconImageHeight;
    }

    public MarkerOptions getMarkerOptions() {
        if (markerOptions == null) {
            
            markerOptions = new MarkerOptions()
                .position(AkylasMapModule.mapBoxToGoogle(proxy
                    .getPosition()))
                .rotation(proxy.heading)
                .title(proxy.getTitle())
                .snippet(proxy.getSubtitle())
                .draggable(proxy.getDraggable())
                .visible(proxy.visible)
                .flat(proxy.getFlat());
            
            Bitmap bitmap = getImage();
            if (bitmap != null) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else {
                int color = proxy.getPinColor();
                if (color != -1) {
                    float[] hsv = new float[3];
                    Color.colorToHSV(color, hsv);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
                }
            }
            
            if (proxy.getAnchor() != null) {
                PointF anchor = proxy.getAnchor();
                markerOptions.anchor(anchor.x, anchor.y);
            }
            if (proxy.getCalloutAnchor() != null) {
                PointF anchor = proxy.getCalloutAnchor();
                markerOptions.infoWindowAnchor(anchor.x, anchor.y);
            }
        }
        
        
        KrollDict dict = proxy.getProperties();
        // customView, image and pincolor must be defined before adding to
        // mapview. Once added, their values are final.
//        if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
//            handleCustomView(dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW));
//        } else 
        if (dict.containsKey(TiC.PROPERTY_IMAGE)) {
            handleImage(dict.get(TiC.PROPERTY_IMAGE));
        } else {
            setIconImageHeight(-1);
        }
        return markerOptions;
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
            if (proxy != null) {
                proxy.googleInfoWindowDidClose();
            }
        }
    }
    
    public void runInUiThread(final TiActivityHelper.CommandNoReturn command) {
        if (marker != null) {
            super.runInUiThread(command);
        }
    }
    
    

    @Override
    void setPosition(double latitude, double longitude, double altitude) {
        
        
        final LatLng position = new LatLng(latitude, longitude);
        if (marker == null) {
            if (markerOptions != null) {
                markerOptions.position(position);
            }
            return;
        }
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                AkylasMapView mapView = (AkylasMapView) proxy.mapView;
                if (mapView != null) {
                    mapView.updateMarkerPosition(marker, position);
                }
            }
        });
        
    }
    

    @Override
    void setPosition(double latitude, double longitude) {
        setPosition(latitude, longitude, 0);
    }
    
    @Override
    void setVisible(final boolean visible) {
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setVisible(visible);
            }
        });
        if (markerOptions != null) {
            markerOptions.visible(visible);
        }
    }
    
    @Override
    void setHeading(final float heading) {
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setRotation(heading);
            }
        });
        
        if (markerOptions != null) {
            markerOptions.rotation(heading);
        }
    }
    
    void setAlpha(final float alpha) {
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setAlpha(alpha);
            }
        });
        
        if (markerOptions != null) {
            markerOptions.alpha(alpha);
        }
    }
    
    @Override
    public void setDraggable(final boolean draggable) {
        if (marker != null) {
//            if (TiApplication.isUIThread()) {
                marker.setDraggable(draggable);
//            } else {
//                getProxy().getActivity().runOnUiThread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        marker.setDraggable(draggable);
//                    }
//                });
//            }
        }
        if (markerOptions != null) {
            markerOptions.draggable(draggable);
        }
    }
    
    @Override
    public void setFlat(final boolean flat) {
        if (marker != null) {
//            if (TiApplication.isUIThread()) {
                marker.setFlat(flat);
//            } else {
//                getProxy().getActivity().runOnUiThread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        marker.setFlat(flat);
//                    }
//                });
//            }
        }
        if (markerOptions != null) {
            markerOptions.flat(flat);
        }
    }

    @Override
    void invalidate() {
        final boolean oldVisible = marker.isVisible();
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setVisible(!oldVisible);
                marker.setVisible(oldVisible);
            }
        });
    }

    @Override
    void setAnchor(PointF anchor) {
        final float anchorX = (anchor != null)?anchor.x:0.5f;
        final float anchorY = (anchor != null)?anchor.y:1.0f;
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setAnchor(anchorX, anchorY);
            }
        });
        if (markerOptions != null) {
            markerOptions.anchor(anchorX, anchorY);
        }
    }

    @Override
    void setWindowAnchor(PointF anchor) {
        final float anchorX = (anchor != null)?anchor.x:0.5f;
        final float anchorY = (anchor != null)?anchor.y:1.0f;
        runInUiThread(new CommandNoReturn() {
            public void execute() {
                marker.setInfoWindowAnchor(anchorX, anchorY);
            }
        });
        if (markerOptions != null) {
            markerOptions.infoWindowAnchor(anchorX, anchorY);
        }
    }

}
