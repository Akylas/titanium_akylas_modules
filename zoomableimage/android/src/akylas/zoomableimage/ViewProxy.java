package akylas.zoomableimage;

import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.UIModule;
import android.app.Activity;

@Kroll.proxy(creatableInModule=AkylasZoomableimageModule.class, propertyAccessors = {
	TiC.PROPERTY_DECODE_RETRIES,
	TiC.PROPERTY_AUTOROTATE,
	TiC.PROPERTY_DEFAULT_IMAGE,
	TiC.PROPERTY_IMAGE,
	TiC.PROPERTY_LOCAL_LOAD_SYNC,
    TiC.PROPERTY_TRANSITION,
    TiC.PROPERTY_SCALE_TYPE,
	TiC.PROPERTY_ONLY_TRANSITION_IF_REMOTE,
	TiC.PROPERTY_ZOOM_ENABLED,
	TiC.PROPERTY_OVER_SCROLL_MODE,
	"zoomScale"
})
public class ViewProxy extends TiViewProxy
{
	public ViewProxy()
	{
		super();
		defaultValues.put(TiC.PROPERTY_SCALE_TYPE, UIModule.SCALE_TYPE_ASPECT_FIT);
	}

	@Override
	public TiUIView createView(Activity activity) {
		return new ZoomableImageView(this);
	}

	private ZoomableImageView getImageView() {
		return (ZoomableImageView)view;
	}

	
	@Kroll.method
    public TiBlob toBlob() {
        return getImageView().toBlob();
    }

	@Override
	public String getApiName()
	{
		return "Akylas.ZoomableImageView";
	}
	
	@Kroll.getProperty(enumerable=false) 
	@Kroll.method
    public Object getZoomScale()
    {
        if (peekView() != null) {
            return getImageView().getZoomScale();
        }
        return getProperty("zoomScale");
    }
	
	@Kroll.getProperty(enumerable=false)
	@Kroll.method
	public Object getMinZoomScale() {
	    if (peekView() != null) {
	        return getImageView().getMinZoomScale();
	    }
	    return getProperty("minZoomScale");
	}
	
	
    @Kroll.getProperty(enumerable=false)
    @Kroll.method
    public Object getMaxZoomScale() {
        if (peekView() != null) {
            return getImageView().getMaxZoomScale();
        }
        return getProperty("maxZoomScale");
    }
	
	@Kroll.method
    public void setZoomScale(final Object scale, final @Kroll.argument(optional = true) Object obj)
    {
        setPropertyJava("zoomScale", scale);
        runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                Boolean animated = true;
                TiPoint point = null;
                
                if (obj instanceof HashMap) {
                    animated = TiConvert.toBoolean((HashMap<String, Object>) obj, "animated", animated);
                    point = TiConvert.toPoint(((HashMap) obj).get("point"));
                }
                getImageView().setZoomScale(TiConvert.toFloat(scale, 1.0f), point, animated);
            }
        }, true);
       
    }
}
