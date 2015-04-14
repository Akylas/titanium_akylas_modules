package akylas.googlemap;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiDatabaseHelper;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import akylas.map.common.BaseTileSourceProxy;
import android.database.sqlite.SQLiteDatabase;

@Kroll.proxy(creatableInModule = AkylasGoogleMapModule.class)
public class TileSourceProxy extends BaseTileSourceProxy {
    private static final String TAG = "TileSourceProxy";
    private TileOverlay mOverlay;
    private TileOverlayOptions mOverlayOptions;
    private TileProvider mTileProvider;
    public static final String MAPBOX_BASE_URL_V4 = "https://a.tiles.mapbox.com/v4/";
    public static final String MAPBOX_BRANDED_JSON_URL_V4 = MAPBOX_BASE_URL_V4 + "%s.json?access_token=%s&secure=1";
      
    public static class MapBoxOnlineTileProvider extends TileJsonProvider {
        private String mToken;
        
        public MapBoxOnlineTileProvider(final String pId, final String token) {
            super(pId, pId, false, false);
            mToken = token;
            initialize(pId, pId, false);
        }

        @Override
        public void setURL(final String aUrl) {
            if (aUrl != null && !aUrl.contains("http://") && !aUrl.contains("https://")) {
                super.setURL(MAPBOX_BASE_URL_V4 + aUrl + "/{z}/{x}/{y}{2x}.png?access_token=" + mToken);
            } else {
                super.setURL(aUrl);
            }
        }

        @Override
        protected String getBrandedJSONURL() {
            String url = String.format(MAPBOX_BRANDED_JSON_URL_V4, mId, mToken);
            if (!mEnableSSL) {
                url = url.replace("https://", "http://");
                url = url.replace("&secure=1", "");
            }

            return url;
        }
        
    }

    @Override
    protected void releaseSource() {
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

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_VISIBLE:
            if (mOverlay != null) {
                mOverlay.setVisible(TiConvert.toBoolean(newValue));
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
    
    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    private static Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    
    public TileProvider getTileProvider() {
        if (mTileProvider == null && mSource != null) {
            String sSource = null;
            if (!(mSource instanceof String)) {
                Object db = getMethod(mSource, "getDatabase");
                if (db instanceof SQLiteDatabase) {
                    mDb = (SQLiteDatabase) db;
                    mTileProvider = new MBTilesProvider((SQLiteDatabase) db);
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
            float minZoom = (mMinZoom >= 0) ? mMinZoom : 1.0f;
            float maxZoom = (mMaxZoom >= 0) ? mMaxZoom : 18.0f;
            if (sSource.toLowerCase().endsWith("mbtiles")) {
                try {
                    mTileProvider = new MBTilesProvider(TiDatabaseHelper.getDatabase(this, sSource, false));
                } catch (Exception e) {
                    release();
                    Log.e(TAG, "Could not load file " + mSource);
                }
            } else if (URL_PATTERN.matcher(sSource).matches()) {
                mTileProvider = new WebTileProvider(sSource, sSource);
            } else {
                switch (sSource.toLowerCase()) {
                case "openstreetmap":
                    mTileProvider = new WebTileProvider("openstreetmap",
                            "http://tile.openstreetmap.org/{z}/{x}/{y}.png")
                            .setName("OpenStreetMap").setAttribution(
                                    "© OpenStreetMap Contributors");
                    break;
                case "openseamap":
                    mTileProvider = new WebTileProvider("openseamap",
                            "http://tiles.openseamap.org/seamark/{z}/{x}/{y}.png")
                            .setName("OpenSeaMap").setAttribution(
                                    "© Map data © OpenStreetMap, licensed under Creative Commons Share Alike By Attribution.");
                    break;
                case "mapquest":
                    mTileProvider = new WebTileProvider("mapquest",
                            "http://otile1.mqcdn.com/tiles/1.0.0/map/{z}/{x}/{y}.png")
                            .setName("MapQuest Open Aerial")
                            .setAttribution(
                                    "Tiles courtesy of MapQuest and OpenStreetMap contributors.");
                    break;
                case "mapquest-sat":
                    mTileProvider = new WebTileProvider("mapquest-sat",
                            "http://otile1.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png")
                            .setName("MapQuest Open Aerial")
                            .setAttribution(
                                    "Tiles courtesy of MapQuest and OpenStreetMap contributors.");
                    break;
                case "ign":
                {
                    final String key = TiConvert.toString(getProperty("key"));
                    String realLayer = "GEOGRAPHICALGRIDSYSTEMS.MAPS";
                    final String layer = TiConvert.toString(getProperty("layer"), realLayer);
                    final String format = TiConvert.toString(getProperty("format"), "image/jpeg");
                    if (layer.equals("express")) {
                        realLayer = "GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.CLASSIQUE";
                    } else if (layer.equals("expressStandard")) {
                        realLayer = "GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD";
                    } else if (layer.equals("plan")) {
                        realLayer = "GEOGRAPHICALGRIDSYSTEMS.PLANIGN";
                    } else if (layer.equals("buildings")) {
                        realLayer = "BUILDINGS.BUILDINGS";
                    } else if (layer.equals("parcels")) {
                        realLayer = "CADASTRALPARCELS.PARCELS";
                    } else if (layer.equals("slopes")) {
                        realLayer = "ELEVATION.SLOPES.HIGHRES";
                    }
                    final String url  = "http://gpp3-wxs.ign.fr/" + key + "/geoportail/wmts?LAYER=" + realLayer + "&EXCEPTIONS=text/xml&FORMAT=" + format + "&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";
                    mTileProvider = new WebTileProvider("ign",
                            url)
                            .setName("IGN")
                            .setAttribution(
                                    "Copyright (c) 2008-2014, Institut National de l'Information Géographique et Forestière France");
                    ((WebTileProvider) mTileProvider).setUserAgent(TiConvert.toString(getProperty("userAgent")));
                    break;
                }
                case "mapbox":
                {
                    final String mapId = TiConvert.toString(getProperty("mapId"));
//                    final String imageQuality = TiConvert.toString(getProperty("imageQuality"), "png");
                    final String token = TiConvert.toString(getProperty("accessToken"));
                    mTileProvider = new MapBoxOnlineTileProvider(mapId, token);
                    break;
                }
                default:
                    break;
                }
            }
        }
        if (mTileProvider != null && hasListeners(TiC.EVENT_LOAD, false)) {
            fireEvent(TiC.EVENT_LOAD, null, false, false);
        }
        return mTileProvider;
    }

    public TileOverlayOptions getTileOverlayOptions() {
        if (mOverlayOptions == null) {
            getTileProvider();
            if (mTileProvider != null) {
                mOverlayOptions = new TileOverlayOptions()
                .tileProvider(mTileProvider);
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
            return AkylasGoogleMapModule.getFactory().regionToDict(bounds);
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

}
