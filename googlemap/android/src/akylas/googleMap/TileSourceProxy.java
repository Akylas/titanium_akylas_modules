package akylas.googlemap;

import java.lang.reflect.Method;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger.CommandNoReturn;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiDatabaseHelper;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import akylas.map.common.BaseTileSourceProxy;
import android.database.sqlite.SQLiteDatabase;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class, propertyAccessors = {
    TiC.PROPERTY_VISIBLE,
    TiC.PROPERTY_OPACITY,
    "tileSize",
    "showTileAfterMaxZoom",
    "autoHd",
    "cacheable"
})
public class TileSourceProxy extends BaseTileSourceProxy {
    private static final String TAG = "TileSourceProxy";
    private TileOverlay mOverlay;
    private TileOverlayOptions mOverlayOptions;
    protected TileProvider mTileProvider;
//    public static final String MAPBOX_BASE_URL_V4 = "https://a.tiles.mapbox.com/v4/";
//    public static final String MAPBOX_BRANDED_JSON_URL_V4 = MAPBOX_BASE_URL_V4 + "%s.json?access_token=%s&secure=1";
    private boolean fadeIn = true;
    private boolean visible = true;
    private float opacity = 1.0f;
    private float zIndex = -1;
    private int tileSize = 256;
    private boolean autoHd = false;
    private boolean cacheable = true;
    private boolean showTileAfterMaxZoom = true;
    private String mSRS = "4326";
    
//    public static class MapBoxOnlineTileProvider extends TileJsonProvider {
//        private String mToken;
//        
//        public MapBoxOnlineTileProvider(final String pId, final String token) {
//            super(pId, pId, false, false);
//            mToken = token;
//            initialize(pId, pId, false);
//        }
//
//        @Override
//        public void setURL(final String aUrl) {
//            if (aUrl != null && !aUrl.contains("http://") && !aUrl.contains("https://")) {
//                super.setURL(MAPBOX_BASE_URL_V4 + aUrl + "/{z}/{x}/{y}{2x}.png?access_token=" + mToken);
//            } else {
//                super.setURL(aUrl);
//            }
//        }
//
//        @Override
//        protected String getBrandedJSONURL() {
//            String url = String.format(MAPBOX_BRANDED_JSON_URL_V4, mId, mToken);
//            if (!mEnableSSL) {
//                url = url.replace("https://", "http://");
//                url = url.replace("&secure=1", "");
//            }
//
//            return url;
//        }
//        
//    }

    @Override
    public void releaseSource() {
        if (mOverlay != null) {
            mOverlay.remove();
            mOverlay = null;
        }
        mOverlayOptions = null;
        if (mTileProvider != null) {
            if (mTileProvider instanceof MBTilesProvider) {
                ((MBTilesProvider) mTileProvider).detach();
            }
            mTileProvider = null;
        }
        super.releaseSource();
    }
    @Override
    public String getApiName() {
        return "Akylas.GoogleMap.TileSource";
    }
    
    private void updateTileLayerVisibility() {
       runInUiThread(new CommandNoReturn() {
            @Override
            public void execute() {
                if (mOverlay != null) {
                    mOverlay.setVisible(visible && opacity > 0.0f);
                }
            }
        }, false);
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_VISIBLE:
            visible = TiConvert.toBoolean(newValue);
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setVisible(visible);
            }
            updateTileLayerVisibility();
            break;
        case TiC.PROPERTY_OPACITY:
           opacity = TiConvert.toFloat(newValue, 1.0f);
           if (mOverlay != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOverlay != null) {
                            mOverlay.setTransparency(1-opacity);
                        }
                    }
                });
            }
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setOpacity(opacity);
            }
            updateTileLayerVisibility();
            break;
        case TiC.PROPERTY_ZINDEX:
            zIndex = TiConvert.toFloat(newValue, 1.0f);
            if (mOverlay != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOverlay != null) {
                            mOverlay.setZIndex(zIndex);
                        }
                    }
                });
            }

            break;
        case "tileSize": 
            tileSize = TiConvert.toInt(newValue, 256);
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setTileSize(tileSize);
            }
            break;
        case "fadeIn":
            fadeIn = TiConvert.toBoolean(newValue);
            if (mOverlay != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOverlay != null) {
                            mOverlay.setFadeIn(fadeIn);
                        }
                    }
                });
            }
            updateTileLayerVisibility();
            break;
        case "autoHd":
            autoHd  = TiConvert.toBoolean(newValue);
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setAutoHD(autoHd);
            }
            break;
        case "cacheable":
            cacheable  = TiConvert.toBoolean(newValue);
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setCacheable(cacheable);
            }
            if (!cacheable && changedProperty) {
                clearCache();
            }
            break;
        case "showTileAfterMaxZoom":
            showTileAfterMaxZoom  = TiConvert.toBoolean(newValue);
            if (mTileProvider instanceof WebTileProvider) {
                ((WebTileProvider) mTileProvider).setShowTileAfterMaxZoom(showTileAfterMaxZoom);
            }
            break;
        default:
            break;
        }
    }

    public static Object getMethod(final Object sourceObj, final String methodName) {
        try {
            Method m = sourceObj.getClass().getMethod(methodName, new Class[] {});
            return m.invoke(sourceObj, new Object[] {});
        } catch (Exception e) {
            return null;
        }
    }
    
//    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
//    private static Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    
    public TileProvider getTileProvider() {
        if (mTileProvider == null && mSource != null) {
            String sSource = null;
            if (!(mSource instanceof String)) {
                Object db = getMethod(mSource, "getDatabase");
                if (db instanceof SQLiteDatabase) {
                    mDb = (SQLiteDatabase) db;
                    
                    mTileProvider = new MBTilesProvider(TiConvert.toString(getProperty("id")),
                            (SQLiteDatabase) db,tileSize);
                    return mTileProvider;
                } else {
                    Object path = getMethod(mSource, "getNativePath");
                    if (path instanceof String) {
                        sSource = (String) path;
                    }
                }
            } else {
                sSource = (String) mSource;
            }
            if (sSource == null) {
                return null;
            }
            if (sSource.toLowerCase().endsWith("mbtiles")) {
                try {
//                    mTileProvider = new MBTilesProvider(TiDatabaseHelper.getDatabase(this, sSource, false));
                    mTileProvider = new MBTilesProvider(TiConvert.toString(getProperty("id")),
                            TiDatabaseHelper.getDatabase(this, sSource, false),tileSize);
                } catch (Exception e) {
                    release();
                    Log.e(TAG, "Could not load file " + mSource);
                }
            } else {
                final int tileSize = TiConvert.toInt(getProperty("tileSize"), 256);
                    mTileProvider = new WebTileProvider(TiConvert.toString(getProperty("id")),
                            TiConvert.toString(getProperty("url")),tileSize)
                            .setName(TiConvert.toString(getProperty("name"))).setAttribution(
                                    TiConvert.toString(getProperty("attribution")));
            }
            
            initTileProvider();
        }
        
        return mTileProvider;
    }
    
    protected void initTileProvider() {
        if (mTileProvider == null) {
            return;
        }
        if (mTileProvider instanceof WebTileProvider) {
            ((WebTileProvider) mTileProvider).setSubdomains(TiConvert.toString(getProperty("subdomains"), "abc"));
            ((WebTileProvider) mTileProvider).setUserAgent(TiConvert.toString(getProperty("userAgent")));
            ((WebTileProvider) mTileProvider).setMinimumZoomLevel(mMinZoom);
            ((WebTileProvider) mTileProvider).setMaximumZoomLevel(mMaxZoom);
            ((WebTileProvider) mTileProvider).setVisible(visible);
            ((WebTileProvider) mTileProvider).setOpacity(opacity);
            ((WebTileProvider) mTileProvider).setAutoHD(autoHd);
            ((WebTileProvider) mTileProvider).setShowTileAfterMaxZoom(showTileAfterMaxZoom);
        }
        if (hasListeners(TiC.EVENT_LOAD, false)) {
            fireEvent(TiC.EVENT_LOAD, null, false, false);
        }
    }

    public TileOverlayOptions getTileOverlayOptions() {
        if (mOverlayOptions == null) {
            getTileProvider();
            if (mTileProvider != null) {
                mOverlayOptions = new TileOverlayOptions()
                .fadeIn(fadeIn)
                .transparency(1-opacity)
                .tileProvider(mTileProvider);
                if (zIndex != -1) {
                    mOverlayOptions.zIndex(zIndex);
                }
            }
        }
        return mOverlayOptions;
    }


    @Override
    public float getMinZoom() {
        if (mTileProvider instanceof WebTileProvider) {
            return ((WebTileProvider)mTileProvider).getMinimumZoomLevel();
        }
        if (mTileProvider instanceof MBTilesProvider) {
            return ((MBTilesProvider)mTileProvider).getMinimumZoomLevel();
        }
        return super.getMinZoom();
    }

    @Override
    public float getMaxZoom() {
        if (mTileProvider instanceof WebTileProvider) {
            return ((WebTileProvider)mTileProvider).getMaximumZoomLevel();
        }
        if (mTileProvider instanceof MBTilesProvider) {
            return ((MBTilesProvider)mTileProvider).getMaximumZoomLevel();
        }
        return super.getMaxZoom();
    }

    @Override
    public KrollDict getRegion() {
        LatLngBounds bounds = null;
        if (mTileProvider instanceof WebTileProvider) {
            bounds = ((WebTileProvider)mTileProvider).getBoundingBox();
        }
        if (mTileProvider instanceof MBTilesProvider) {
            bounds = ((MBTilesProvider)mTileProvider).getBoundingBox();
        }
        if (bounds != null) {
            return AkylasGooglemapModule.getFactory().regionToDict(bounds);
        }
        return super.getRegion();
    }
    
    @Override
    public void removeFromMap() {
        if (mOverlay != null) {
            mOverlay.remove();
            mOverlay = null;
        }
    }

    public void setTileOverlay(TileOverlay overlay) {
        mOverlay = overlay;
    }
    
    @Override
    public void clearCache() {
        if (mOverlay != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mOverlay != null) {
                        mOverlay.clearTileCache();
                    }
                }
            });
        }
    }

}
