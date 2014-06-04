package akylas.map;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutParams;
import org.appcelerator.titanium.view.TiUIView;

import com.mapbox.mapboxsdk.views.MapView;

import android.app.Activity;
import android.os.Message;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = {
    AkylasMapModule.PROPERTY_USER_LOCATION_ENABLED,
    AkylasMapModule.PROPERTY_USER_LOCATION_REQUIRED_ZOOM,
    TiC.PROPERTY_USER_LOCATION,
    AkylasMapModule.PROPERTY_TILE_SOURCE,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE, 
    AkylasMapModule.PROPERTY_SCROLLABLE_AREA_LIMIT,
    AkylasMapModule.PROPERTY_REGION_FIT,
    AkylasMapModule.PROPERTY_CENTER_COORDINATE,
    AkylasMapModule.PROPERTY_ZOOM,
    AkylasMapModule.PROPERTY_MINZOOM,
    AkylasMapModule.PROPERTY_MAXZOOM,
    AkylasMapModule.PROPERTY_ROUTES,
    AkylasMapModule.PROPERTY_DISK_CACHE,
    TiC.PROPERTY_REGION,
    AkylasMapModule.PROPERTY_DEBUG,
    TiC.PROPERTY_ANNOTATIONS, 
    AkylasMapModule.PROPERTY_ANIMATE_CHANGES,
    TiC.PROPERTY_ENABLE_ZOOM_CONTROLS })
public class MapboxViewProxy extends MapDefaultViewProxy {
    private static final String TAG = "MapboxViewProxy";
    private ArrayList<Object> preloadSources = null;
    
    private static final int MSG_FIRST_ID = MapDefaultViewProxy.MSG_LAST_ID + 1;
    private static final int MSG_ADD_TILE_SOURCE = MSG_FIRST_ID;
    private static final int MSG_REMOVE_TILE_SOURCE = MSG_FIRST_ID + 1;
    
    public MapboxViewProxy() {
        super();
        
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        AsyncResult result = null;
        switch (msg.what) {
        
        case MSG_ADD_TILE_SOURCE: {
            result = (AsyncResult) msg.obj;
            KrollDict dict = (KrollDict)result.getArg();
            handleInsertTileSourceAt(dict.optInt("index", -1), dict.get("object"));
            result.setResult(null);
            return true;
        }
        case MSG_REMOVE_TILE_SOURCE: {
            result = (AsyncResult) msg.obj;
            handleRemoveTileSource(result.getArg());
            result.setResult(null);
            return true;
        }
        default: {
            return super.handleMessage(msg);
        }
        }
    }
    
    @Override
    public void processPreloaded() {
        super.processPreloaded();

        if (preloadSources != null && preloadSources.size() > 0) {
            setProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, preloadSources.toArray());
            preloadSources.clear();
        }
    }
    
    public TiUIView createView(Activity activity) {
        if (hasProperty(AkylasMapModule.PROPERTY_DEBUG)) {
            boolean debug = TiConvert.toBoolean(getProperty(AkylasMapModule.PROPERTY_DEBUG));
            MapView.setDebugMode(debug);
        }
        TiUIView view = new AkylasMapboxView(this);
        LayoutParams params = view.getLayoutParams();
        params.sizeOrFillWidthEnabled = true;
        params.sizeOrFillHeightEnabled = true;
        params.autoFillsHeight = true;
        params.autoFillsHeight = true;
        params.autoFillsWidth = true;
        processPreloaded();
        return view;
    }
    
//    private MapView getMap() {
//        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
//        if (mapView != null) {
//            return mapView.getMap();
//        }
//        return null;
//    }

    private void handleInsertTileSourceAt(int index, Object tilesource) {
        if (tilesource == null) return;
        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
        if (mapView != null) {
            Object result = mapView.addTileSource(tilesource, index);
            if (result != null) {
                addToProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, index, result);

            }
        } else {
            addPreloadTileSource(tilesource, index, false);
        }
    }
    
    private void handleRemoveTileSource(Object tilesource) {
        if (tilesource == null) return;
        AkylasMapboxView mapView = (AkylasMapboxView) peekView();
        if (mapView != null) {
            mapView.removeTileSource(tilesource);
            removeFromProperty(AkylasMapModule.PROPERTY_TILE_SOURCE, tilesource);
        } else {
            deletePreloadObject(preloadSources, tilesource);
        }
    }
    
    
    public void addPreloadTileSource(Object value, int index, boolean arrayOnly) {
        addPreloadObject(TileSourceProxy.class, getOrCreatePreloadSources(), value, index, arrayOnly);
    }
    
    public ArrayList getOrCreatePreloadSources() {
        if (preloadSources == null) {
            preloadSources = new ArrayList<Object>();
            if (hasProperty(AkylasMapModule.PROPERTY_TILE_SOURCE)) {
                addPreloadObject(Object.class, preloadSources, getProperty(AkylasMapModule.PROPERTY_TILE_SOURCE), -1, false);
            }
        }
        return preloadSources;
    }

//    public void removePreloadTileSource(Object value) {
//        deletePreloadObject(preloadSources, value);
//    }
    
    @Override
    public void addTileSource(Object value, @Kroll.argument(optional = true) final Object indexObj) {
        if (value == null) return;
        int index = -1;
        if (indexObj != null) {
            index = ((Number)indexObj).intValue();
        }
        if (TiApplication.isUIThread()) {
            handleInsertTileSourceAt(index, value);
        } else {
            sendIndexMessage(MSG_ADD_TILE_SOURCE, index, value);
        }
    }

    @Override
    public void removeTileSource(Object value) {
        if (value == null) return;
        if (TiApplication.isUIThread()) {
            handleRemoveTileSource(value);
        } else {
            TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_REMOVE_TILE_SOURCE), value);

        }
    }
}
