package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;

import android.graphics.Bitmap;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapMarker extends AkylasMarker {
    private static final String TAG = "GoogleMapMarker";
    private MarkerOptions markerOptions;
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
            markerOptions = new MarkerOptions();
            markerOptions.position(AkylasMapModule.mapBoxToGoogle(proxy
                    .getPosition()));
            markerOptions.title(proxy.getTitle());
            markerOptions.snippet(proxy.getSubtitle());
            markerOptions.draggable(proxy.getDraggable());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(proxy
                    .getPinColor()));
        }
        KrollDict dict = proxy.getProperties();
        // customView, image and pincolor must be defined before adding to
        // mapview. Once added, their values are final.
        if (dict.containsKey(AkylasMapModule.PROPERTY_CUSTOM_VIEW)) {
            handleCustomView(dict.get(AkylasMapModule.PROPERTY_CUSTOM_VIEW));
        } else if (dict.containsKey(TiC.PROPERTY_IMAGE)) {
            handleImage(dict.get(TiC.PROPERTY_IMAGE));
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
        }
    }

    @Override
    void setPosition(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);
        if (marker != null) {
            marker.setPosition(position);
        }
        if (markerOptions != null) {
            markerOptions.position(position);
        }
    }
}
