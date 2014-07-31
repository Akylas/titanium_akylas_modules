package akylas.map;

import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiImageLruCache;
import org.appcelerator.titanium.view.TiDrawableReference;

import akylas.map.AnnotationProxy;
import android.graphics.Bitmap;
import android.graphics.Color;

abstract class AkylasMarker  {
    protected AnnotationProxy proxy;
    private TiImageLruCache mMemoryCache = TiImageLruCache.getInstance();
	
	public AkylasMarker(final AnnotationProxy p) {
        proxy = p;
    }
		
//	private void setProxy(AnnotationProxy proxy) {
//		this.proxy = proxy;
//	}
	
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
	        int hash = imageref.hashCode();
	        Bitmap bitmap = mMemoryCache.get(hash);
            if (bitmap != null) {
                if (!bitmap.isRecycled()) {
                    return bitmap;
                } else { // If the cached image has been recycled, remove it
                         // from the cache.
                    mMemoryCache.remove(hash);
                }
            }
            bitmap = imageref.getBitmap();
            if (bitmap != null) {
                mMemoryCache.put(hash, bitmap);
            }
            return bitmap;
	    }
	    return null;
	}
	
	public AnnotationProxy getProxy() {
		return proxy;
	}
    
    abstract double getLatitude();
    abstract double getLongitude();
    abstract double getAltitude();
    
    abstract void showInfoWindow();

    abstract void hideInfoWindow();
    abstract void setPosition(double latitude, double longitude);
}
