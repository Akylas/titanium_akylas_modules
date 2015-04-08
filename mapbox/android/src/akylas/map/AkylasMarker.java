package akylas.map;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiActivityHelper;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiDrawableReference;

import com.squareup.picasso.Cache;

import akylas.map.AnnotationProxy;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

abstract class AkylasMarker  {
    protected AnnotationProxy proxy;
	
	public AkylasMarker(final AnnotationProxy p) {
        proxy = p;
    }
		
	public void prepareRemoval() {
		this.proxy = null;
	}
	
	protected int getColor() {
	    Object value = proxy.getProperty(TiC.PROPERTY_PINCOLOR);
        if (value != null) {
            return TiConvert.toColor(value);
        }
        return Color.TRANSPARENT;
	}

	
	protected Bitmap getImage() {
	    Object value = proxy.getProperty(TiC.PROPERTY_IMAGE);
	    if (value != null) {
	        TiDrawableReference imageref = TiDrawableReference.fromUrl(proxy,
                    TiConvert.toString(proxy.getProperty(TiC.PROPERTY_IMAGE)));
	        Cache cache = TiApplication.getImageMemoryCache();
            Bitmap bitmap = cache.get(imageref.getUrl());
            Drawable drawable = null;
            if (bitmap == null) {
                drawable = imageref.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable)drawable).getBitmap();
                    cache.set(imageref.getUrl(), bitmap);
                }
            }
            return bitmap;
	    }
	    return null;
	}
	
	public AnnotationProxy getProxy() {
		return proxy;
	}
	
	
	public void runInUiThread(final TiActivityHelper.CommandNoReturn command) {
	    proxy.runInUiThread(command);
    }
    abstract void invalidate();
    
    abstract double getLatitude();
    abstract double getLongitude();
    abstract double getAltitude();
    
    abstract void showInfoWindow();

    abstract void hideInfoWindow();
    abstract void setPosition(double latitude, double longitude);
    abstract void setPosition(double latitude, double longitude, double altitude);
    abstract void setDraggable(final boolean draggable);
    abstract void setAnchor(final PointF anchor);
    abstract void setWindowAnchor(final PointF anchor);
    abstract void setFlat(final boolean flat);
    abstract void setVisible(final boolean visible);
    abstract void setHeading(final float heading);
}
