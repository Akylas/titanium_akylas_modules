package akylas.map;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiDatabaseHelper;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.util.MapboxUtils;

@Kroll.proxy(creatableInModule = AkylasMapModule.class, propertyAccessors = { 
    TiC.PROPERTY_SOURCE
    })
public class TileSourceProxy extends ReusableProxy {
    private static final String TAG = "TileSourceProxy";
    private TileLayer mLayer;
    private TileOverlay mOverlay;
    private TileOverlayOptions mOverlayOptions;
    private TileProvider mTileProvider;
    private float mMinZoom = -1;
    private float mMaxZoom = -1;
    private Object mSource;
    private SQLiteDatabase mDb;
    
    public static class MapBoxOnlineTileProvider extends TileJsonProvider {
        private String mToken;
        public MapBoxOnlineTileProvider(final String pId) {
            super(pId, pId, true);
            mToken = MapboxUtils.getAccessToken();
        }
        
        public MapBoxOnlineTileProvider(final String pId, final String token) {
            super(pId, pId, true);
            mToken = token;
        }

        @Override
        public void setURL(final String aUrl) {
            if (!TextUtils.isEmpty(aUrl) && !aUrl.toLowerCase(Locale.US).contains("http://") && !aUrl.toLowerCase(Locale.US).contains("https://")) {
                super.setURL(MapboxConstants.MAPBOX_BASE_URL_V4 + aUrl + "/{z}/{x}/{y}{2x}.png?access_token=" + mToken);
            } else {
                super.setURL(aUrl);
            }
        }

        @Override
        protected String getBrandedJSONURL() {
            String url = String.format(MapboxConstants.MAPBOX_BRANDED_JSON_URL_V4, mId, MapboxUtils.getAccessToken());
            if (!mEnableSSL) {
                url = url.replace("https://", "http://");
                url = url.replace("&secure=1", "");
            }

            return url;
        }
        
    }

    public TileSourceProxy() {
        super();
    }

    public TileSourceProxy(TiContext tiContext) {
        this();
    }

    public void release() {
        mLayer = null;
        if (mOverlay != null) {
            mOverlay.remove();
            mOverlay = null;
        }
        mOverlayOptions = null;
        mTileProvider = null;
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_SOURCE:
//            if (newValue instanceof TiDatabaseProxy)
            mSource = newValue;
            release();
            break;
        case TiC.PROPERTY_VISIBLE:
            if (mOverlay != null) {
                mOverlay.setVisible(TiConvert.toBoolean(newValue));
            }
            break;
        case AkylasMapModule.PROPERTY_MINZOOM:
            mMinZoom = TiConvert.toFloat(newValue);
            if (mLayer != null) {
                mLayer.setMinimumZoomLevel(mMinZoom);
            }
            break;
        case AkylasMapModule.PROPERTY_MAXZOOM:
            mMaxZoom = TiConvert.toFloat(newValue);
            if (mLayer != null) {
                mLayer.setMaximumZoomLevel(mMaxZoom);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    // @Override
    // public void handleCreationDict(KrollDict dict) {
    // super.handleCreationDict(dict);
    // if (dict.containsKey(TiC.PROPERTY_SOURCE)) {
    // mLayer = AkylasMapModule.tileSourceFromObject(this,
    // dict.get(TiC.PROPERTY_SOURCE));
    // }
    // }

    public TileLayer getLayer() {
        if (mLayer == null && mSource != null) {
            if (!(mSource instanceof String)) {
                return null;
            }
            final String sSource = (String) mSource;
            float minZoom = (mMinZoom >= 0) ? mMinZoom : 1.0f;
            float maxZoom = (mMaxZoom >= 0) ? mMaxZoom : 18.0f;
            if (sSource.toLowerCase().endsWith("mbtiles")) {
                try {
                    mDb = TiDatabaseHelper.getDatabase(this, sSource, false);
                    mLayer = new MBTilesLayer(mDb);
                } catch (Exception e) {
                    release();
                    Log.e(TAG, "Could not load file " + mSource);
                }
            } else {
                switch (sSource.toLowerCase()) {
                case "openstreetmap":
                    mLayer = new WebSourceTileLayer("openstreetmap",
                            "http://tile.openstreetmap.org/{z}/{x}/{y}.png")
                            .setName("OpenStreetMap").setAttribution(
                                    "© OpenStreetMap Contributors");
                    break;
                case "openseamap":
                    mLayer = new WebSourceTileLayer("openseamap", "") {
                        private static final String BASE_URL = "http://tile.openstreetmap.org/{z}/{x}/{y}.png";
                        private static final String BASE_URL_SEA = "http://tiles.openseamap.org/seamark/{z}/{x}/{y}.png";

                        @Override
                        public String[] getTileURLs(final MapTile aTile,
                                boolean hdpi) {
                            return new String[] {
                                    this.parseUrlForTile(BASE_URL, aTile, hdpi),
                                    this.parseUrlForTile(BASE_URL_SEA, aTile,
                                            hdpi) };
                        }
                    }.setName("Open Sea Map").setAttribution(
                            "© OpenStreetMap CC-BY-SA");
                    break;
                case "mapquest":
                    mLayer = new WebSourceTileLayer("mapquest",
                            "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png")
                            .setName("MapQuest Open Aerial")
                            .setAttribution(
                                    "Tiles courtesy of MapQuest and OpenStreetMap contributors.");
                    break;
                case "tilemill":
                    String host = TiConvert.toString(getProperty("host"));
                    String mapName = TiConvert.toString(getProperty("mapId"));
                    if (host != null && mapName != null) {
                        String cacheKey = TiConvert.toString(
                                getProperty("cacheKey"), mapName);
                        String url = String.format("http://%s:20008/tile/%s",
                                host, mapName) + "/{z}/{x}/{y}.png?updated=%d";
                        mLayer = new WebSourceTileLayer(cacheKey, url) {
                            @Override
                            public String getTileURL(final MapTile aTile,
                                    boolean hdpi) {
                                return String.format(getTileURL(aTile, hdpi),
                                        System.currentTimeMillis() / 1000L);
                            }
                        }.setName(mapName);
                    }
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
                    
                    mLayer = new WebSourceTileLayer("ign",
                            url)
                            .setName("IGN")
                            .setAttribution(
                                    "Copyright (c) 2008-2014, Institut National de l'Information Géographique et Forestière France");
                    
                    ((WebSourceTileLayer) mLayer).setUserAgent(TiConvert.toString(getProperty("userAgent")));
                    break;
                }
                case "mapbox":
                {
                    final String mapId = TiConvert.toString(getProperty("mapId"));
//                    final String imageQuality = TiConvert.toString(getProperty("imageQuality"), "png");
                    final String token = TiConvert.toString(getProperty("accessToken"), MapboxUtils.getAccessToken());
                    mLayer = new MapboxTileLayer(mapId, token, true);
                    break;
                }
                default:
                    mLayer = new MapboxTileLayer(sSource);
                    break;
                }
            }

            if (mLayer != null) {
                mLayer.setMinimumZoomLevel(minZoom);
                mLayer.setMaximumZoomLevel(maxZoom);
            }

        }
        return mLayer;
    }
    private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    private static Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    
    public TileProvider getTileProvider() {
        if (mTileProvider == null && mSource != null) {
            String sSource = null;
            if (mSource instanceof String) {
                sSource = (String) mSource;
            } else if (mSource instanceof HashMap) {
                sSource = TiConvert.toString((HashMap) mSource, TiC.PROPERTY_SOURCE);
            }
            if (sSource == null) {
                return null;
            }
            float minZoom = (mMinZoom >= 0) ? mMinZoom : 1.0f;
            float maxZoom = (mMaxZoom >= 0) ? mMaxZoom : 18.0f;
            if (sSource.toLowerCase().endsWith("mbtiles")) {
                try {
                    mDb = TiDatabaseHelper.getDatabase(this, sSource, false);
                    mTileProvider = new MBTilesProvider(mDb);
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
                    final String token = TiConvert.toString(getProperty("accessToken"), MapboxUtils.getAccessToken());
                    mTileProvider = new MapBoxOnlineTileProvider(mapId, token);
                    break;
                }
                default:
                    mTileProvider = new MapBoxOnlineTileProvider(sSource);
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

    @Kroll.method
    @Kroll.getProperty
    public float getMinZoom() {
        if (mLayer != null) {
            return mLayer.getMinimumZoomLevel();
        } 
        if (mTileProvider != null ) {
            if (mTileProvider instanceof WebTileProvider) {
                return ((WebTileProvider)mTileProvider).getMinimumZoomLevel();
            } else if (mTileProvider instanceof TileLayer) {
                return ((TileLayer)mTileProvider).getMinimumZoomLevel();
            }
        }
        return TileLayerConstants.MINIMUM_ZOOMLEVEL;
    }

    @Kroll.method
    @Kroll.getProperty
    public float getMaxZoom() {
        if (mLayer != null) {
            return mLayer.getMaximumZoomLevel();
        } 
        if (mTileProvider != null ) {
            if (mTileProvider instanceof WebTileProvider) {
                return ((WebTileProvider)mTileProvider).getMaximumZoomLevel();
            } else if (mTileProvider instanceof TileLayer) {
                return ((TileLayer)mTileProvider).getMaximumZoomLevel();
            }
        }
        return TileLayerConstants.MAXIMUM_ZOOMLEVEL;
    }

    @Kroll.method
    @Kroll.getProperty
    public KrollDict getRegion() {
        if (mLayer != null) {
            return AkylasMapModule.regionToDict(mLayer.getBoundingBox());
        } 
        if (mTileProvider != null ) {
            if (mTileProvider instanceof WebTileProvider) {
                return AkylasMapModule.regionToDict(((WebTileProvider)mTileProvider).getBoundingBox());
            } else if (mTileProvider instanceof TileLayer) {
                return AkylasMapModule.regionToDict(((TileLayer)mTileProvider).getBoundingBox());
            }
        }
        return AkylasMapModule.regionToDict(TileLayerConstants.WORLD_BOUNDING_BOX);
    }

    @Kroll.method
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
