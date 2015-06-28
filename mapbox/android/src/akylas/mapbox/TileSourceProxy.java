package akylas.mapbox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiDatabaseHelper;

import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.util.MapboxUtils;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.BaseTileSourceProxy;
import android.database.sqlite.SQLiteDatabase;

@Kroll.proxy(creatableInModule = AkylasMapboxModule.class)
public class TileSourceProxy extends BaseTileSourceProxy {
    private static final String TAG = "TileSourceProxy";
    private TileLayer mLayer;

    @Override
    protected void releaseSource() {
        mLayer = null;
        super.releaseSource();
    }
    
    @Override
    public String getApiName() {
        return "Akylas.Mapbox.TileSource";
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case AkylasMapBaseModule.PROPERTY_MINZOOM:
            if (mLayer != null) {
                mLayer.setMinimumZoomLevel(mMinZoom);
            }
            break;
        case AkylasMapBaseModule.PROPERTY_MAXZOOM:
            if (mLayer != null) {
                mLayer.setMaximumZoomLevel(mMaxZoom);
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

    public TileLayer getLayer() {
        if (mLayer == null && mSource != null) {
            String sSource = null;
            if (!(mSource instanceof String)) {
                Object db = getMethod(mSource, "getDatabase");
                if (db instanceof SQLiteDatabase) {
                    mDb = (SQLiteDatabase) db;
                    mLayer = new MBTilesLayer(mDb);
                    return mLayer;
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
            float maxZoom = (mMaxZoom >= 0) ? mMaxZoom : 22.0f;
            if (sSource.toLowerCase().endsWith("mbtiles")) {
                try {
                    mDb = TiDatabaseHelper.getDatabase(this, sSource, false);
                    mLayer = new MBTilesLayer(mDb);
                } catch (Exception e) {
                    releaseSource();
                    Log.e(TAG, "Could not load file " + mSource);
                }
            } else {
                switch (sSource.toLowerCase()) {
                case "websource":
                {
                    mLayer = new WebSourceTileLayer(TiConvert.toString(getProperty("id")),
                            TiConvert.toString(getProperty("url")))
                            .setName(TiConvert.toString(getProperty("name"))).setAttribution(
                                    TiConvert.toString(getProperty("attribution")));
                    break;
                }
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
                case "ign": {
                    final String key = TiConvert.toString(getProperty("key"));
                    String realLayer = "GEOGRAPHICALGRIDSYSTEMS.MAPS";
                    final String layer = TiConvert.toString(
                            getProperty("layer"), realLayer);
                    final String format = TiConvert.toString(
                            getProperty("format"), "image/jpeg");
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
                    final String url = "http://gpp3-wxs.ign.fr/"
                            + key
                            + "/geoportail/wmts?LAYER="
                            + realLayer
                            + "&EXCEPTIONS=text/xml&FORMAT="
                            + format
                            + "&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}";

                    mLayer = new WebSourceTileLayer("ign", url)
                            .setName("IGN")
                            .setAttribution(
                                    "Copyright (c) 2008-2014, Institut National de l'Information Géographique et Forestière France");

                    ((WebSourceTileLayer) mLayer).setUserAgent(TiConvert
                            .toString(getProperty("userAgent")));
                    break;
                }
                case "mapbox": {
                    final String mapId = TiConvert
                            .toString(getProperty("mapId"));
                    // final String imageQuality =
                    // TiConvert.toString(getProperty("imageQuality"), "png");
                    final String token = TiConvert.toString(
                            getProperty("accessToken"),
                            MapboxUtils.getAccessToken());
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

    @Override
    public float getMinZoom() {
        if (mLayer != null) {
            return mLayer.getMinimumZoomLevel();
        }
        return super.getMinZoom();
    }

    @Override
    public float getMaxZoom() {
        if (mLayer != null) {
            return mLayer.getMaximumZoomLevel();
        }
        return super.getMaxZoom();
    }

    @Override
    public KrollDict getRegion() {
        if (mLayer != null) {
            return AkylasMapboxModule.getFactory().regionToDict(
                    mLayer.getBoundingBox());
        }
        return super.getRegion();
    }

}
