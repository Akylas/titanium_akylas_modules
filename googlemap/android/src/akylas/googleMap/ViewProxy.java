package akylas.googleMap;


import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import akylas.map.common.MapDefaultViewProxy;
import android.app.Activity;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasGoogleMapModule.class, propertyAccessors = {
})
public class ViewProxy extends MapDefaultViewProxy {
    private static final String TAG = "GoogleMapViewProxy";
    
    
    private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    private static final int MSG_SNAP_SHOT = MSG_FIRST_ID;
    
    public ViewProxy() {
        super();
    }
    
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.View";
    }
    
    @Override
    protected Class annotationClass() {
        return AnnotationProxy.class;
    }
    @Override
    protected Class routeClass() {
        return RouteProxy.class;
    }
    @Override
    protected Class tileSourceClass() {
        return TileSourceProxy.class;
    }
    
    @Override
    public void realizeViews(TiUIView view, boolean enableModelListener, boolean processProperties)
    {
        if (((GoogleMapView)view).getMap() != null) {
            super.realizeViews(view, enableModelListener, processProperties);
        }
    }
    
    public TiUIView createView(Activity activity) {
        return new GoogleMapView(this, activity);
    }

    @Override
    public boolean handleMessage(Message msg) 
    {
//      AsyncResult result = null;
        switch (msg.what) {

        case MSG_SNAP_SHOT: {
            handleSnapshot();
            return true;
        }

        default : {
            return super.handleMessage(msg);
        }
        }
    }
    
    private void handleSnapshot() 
    {
        TiUIView view = peekView();
        if (view instanceof GoogleMapView) {
            ((GoogleMapView) view).snapshot();
        }
    }
    
    //KROLL ACCESSORS
    
    @Kroll.method
    public void snapshot()
    {
        if (TiApplication.isUIThread()) {
            handleSnapshot();
        } else {
            getMainHandler().obtainMessage(MSG_SNAP_SHOT).sendToTarget();
        }
    }
    
    @Kroll.method
    public void updateCamera(HashMap value)
    {
        if (value == null) {
            return;
        }
        TiUIView view = peekView();
        if (view instanceof GoogleMapView) {
            ((GoogleMapView) view).updateCamera(TiConvert.toKrollDict(value));
        }
    }
}
