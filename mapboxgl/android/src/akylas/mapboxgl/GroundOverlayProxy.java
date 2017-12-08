package akylas.mapboxgl;

import java.util.List;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiImageHelper;
import org.appcelerator.titanium.util.TiImageHelper.TiDrawableTarget;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.squareup.picasso.Picasso.LoadedFrom;

import akylas.map.common.BaseGroundOverlayProxy;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

@Kroll.proxy(creatableInModule = AkylasMapboxGLModule.class)
public class GroundOverlayProxy extends BaseGroundOverlayProxy<LatLng, LatLngBounds , List<LatLng>> implements TiDrawableTarget {
    
//    private GroundOverlayOptions options = null;
//    private GroundOverlay overlay;
    private Bitmap bitmap = null;
    public GroundOverlayProxy() {
        super();
        mBoundingBox = AkylasMapboxGLModule.MIN_BOUNDING_BOX;
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
//            if (overlay != null) {
//                overlay.setZIndex(zIndex);
//            }
            break;
        case TiC.PROPERTY_REGION:
            mBoundingBox = AkylasMapboxGLModule.regionFromObject(newValue);
//            if (overlay != null) {
//                overlay.setPositionFromBounds((LatLngBounds) mBoundingBox);
//            }
            break;
        case TiC.PROPERTY_VISIBLE:
//            if (overlay != null) {
//                overlay.setVisible(TiConvert.toBoolean(newValue));
//            }
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
//        if (overlay != null) {
//            overlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap));
//        }
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
//        if (overlay != null) {
//            overlay.setPositionFromBounds((LatLngBounds) mBoundingBox);
//        }
    }
    
//    public GroundOverlayOptions getAndSetOptions(final CameraPosition position) {
//        options = new GroundOverlayOptions();
//        if (bitmap != null) {
//            options.image(BitmapDescriptorFactory.fromBitmap(bitmap));
//        }
//        return options.positionFromBounds((LatLngBounds) mBoundingBox).zIndex(zIndex);
//    }
    
//    public void setGroundOverlay(GroundOverlay r) {
//        overlay = r;
//        overlay.setTag(this);
//    }

//    public GroundOverlay getGroundOverlay() {
//        return overlay;
//    }

    public void removeFromMap() {
//        if (overlay != null) {
//            overlay.remove();
//            overlay.setTag(null);
//            overlay = null;
//        }
    }


    @Override
    protected long getPointsSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected Object getPoint(int i) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void addPos(Object point) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void clearPoints() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected Object[] getPointsArray() {
        // TODO Auto-generated method stub
        return null;
    }
}
