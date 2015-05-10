package akylas.googlemap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiImageHelper;
import org.appcelerator.titanium.util.TiImageHelper.TiDrawableTarget;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso.LoadedFrom;

import akylas.map.common.BaseGroundOverlayProxy;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class)
public class GroundOverlayProxy extends BaseGroundOverlayProxy<LatLng, LatLngBounds> implements TiDrawableTarget {
    
    private GroundOverlayOptions options = null;
    private GroundOverlay overlay;
    private Bitmap bitmap = null;
    public GroundOverlayProxy() {
        super();
        mBoundingBox = AkylasGooglemapModule.MIN_BOUNDING_BOX;
    }

    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.GroundOverlay";
    }
    
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_ZINDEX:
            zIndex = TiConvert.toInt(newValue);
            if (overlay != null) {
                overlay.setZIndex(zIndex);
            }
            break;
        case TiC.PROPERTY_REGION:
            mBoundingBox = AkylasGooglemapModule.regionFromObject(newValue);
            if (overlay != null) {
                overlay.setPositionFromBounds((LatLngBounds) mBoundingBox);
            }
            break;
        case TiC.PROPERTY_VISIBLE:
            if (overlay != null) {
                overlay.setVisible(TiConvert.toBoolean(newValue));
            }
            break;
        case TiC.PROPERTY_IMAGE:
            TiDrawableReference imageref = TiDrawableReference.fromObject(this, newValue);
            if (imageref.isTypeUrl()) {
                TiImageHelper.downloadDrawable(this, imageref, true, this);
            } else {
                handleSetImage(imageref.getBitmap()); 
            }
            break;
        default:
            break;
        }
    }
    
    protected void handleSetImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (overlay != null) {
            overlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    };

    @Override
    public void onDrawableLoaded(Drawable drawable, LoadedFrom from) {
        if (drawable instanceof BitmapDrawable) {
            onBitmapLoaded(((BitmapDrawable) drawable).getBitmap(), from);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        handleSetImage(null);
    }

    

    @Override
    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        handleSetImage(bitmap);
    }

    @Override
    public void onPrepareLoad(Drawable arg0) {        
    }
    
    @Override
    protected void replacePoints(Object points) {

        super.replacePoints(points);
        if (overlay != null) {
            overlay.setPositionFromBounds((LatLngBounds) mBoundingBox);
        }
    }
    
    public GroundOverlayOptions getAndSetOptions(final CameraPosition position) {
        options = new GroundOverlayOptions();
        if (bitmap != null) {
            options.image(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
        return options.positionFromBounds((LatLngBounds) mBoundingBox).zIndex(zIndex);
    }
    
    public void setGroundOverlay(GroundOverlay r) {
        overlay = r;
    }

    public GroundOverlay getGroundOverlay() {
        return overlay;
    }

    public void removeGroundOverlay() {
        if (overlay != null) {
            overlay.remove();
            overlay = null;
        }
    }
}
