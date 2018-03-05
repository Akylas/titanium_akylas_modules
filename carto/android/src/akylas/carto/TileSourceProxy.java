package akylas.carto;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import com.carto.core.BinaryData;
import com.carto.core.MapRange;
import com.carto.core.StringMap;
import com.carto.core.StringVector;
import com.carto.datasources.CartoOnlineTileDataSource;
import com.carto.datasources.HTTPTileDataSource;
import com.carto.datasources.MBTilesTileDataSource;
import com.carto.datasources.PersistentCacheTileDataSource;
import com.carto.datasources.TileDataSource;
import com.carto.layers.RasterTileLayer;
import com.carto.layers.TileLayer;
import com.carto.layers.VectorTileLayer;
import com.carto.layers.VectorTileRenderOrder;
import com.carto.styles.CompiledStyleSet;
import com.carto.utils.AssetUtils;
import com.carto.utils.ZippedAssetPackage;
import com.carto.vectortiles.MBVectorTileDecoder;
import com.carto.vectortiles.VectorTileDecoder;

import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.BaseTileSourceProxy;

@Kroll.proxy(creatableInModule = AkylasCartoModule.class, propertyAccessors = {
        TiC.PROPERTY_VISIBLE, TiC.PROPERTY_OPACITY, "tileSize",
        "showTileAfterMaxZoom", "autoHd", "cacheable", "style" })
public class TileSourceProxy extends BaseTileSourceProxy {
    private static final String TAG = "TileSourceProxy";
    // private TileOverlay mOverlay;
    // private TileOverlayOptions mOverlayOptions;
    // protected TileProvider mTileProvider;
    // public static final String MAPBOX_BASE_URL_V4 =
    // "https://a.tiles.mapbox.com/v4/";
    // public static final String MAPBOX_BRANDED_JSON_URL_V4 =
    // MAPBOX_BASE_URL_V4 + "%s.json?access_token=%s&secure=1";
    private boolean fadeIn = true;
    private float opacity = 1.0f;
    private int tileSize = 256;
    private boolean autoHd = false;
    private boolean cacheable = true;
    private boolean showTileAfterMaxZoom = true;
    private String mSRS = "4326";

    // public static class MapBoxOnlineTileProvider extends TileJsonProvider {
    // private String mToken;
    //
    // public MapBoxOnlineTileProvider(final String pId, final String token) {
    // super(pId, pId, false, false);
    // mToken = token;
    // initialize(pId, pId, false);
    // }
    //
    // @Override
    // public void setURL(final String aUrl) {
    // if (aUrl != null && !aUrl.contains("http://") &&
    // !aUrl.contains("https://")) {
    // super.setURL(MAPBOX_BASE_URL_V4 + aUrl +
    // "/{z}/{x}/{y}{2x}.png?access_token=" + mToken);
    // } else {
    // super.setURL(aUrl);
    // }
    // }
    //
    // @Override
    // protected String getBrandedJSONURL() {
    // String url = String.format(MAPBOX_BRANDED_JSON_URL_V4, mId, mToken);
    // if (!mEnableSSL) {
    // url = url.replace("https://", "http://");
    // url = url.replace("&secure=1", "");
    // }
    //
    // return url;
    // }
    //
    // }

    @Override
    public void releaseSource() {
        // if (mOverlay != null) {
        // mOverlay.remove();
        // mOverlay = null;
        // }
        // mOverlayOptions = null;
        // if (mTileProvider != null) {
        // if (mTileProvider instanceof MBTilesProvider) {
        // ((MBTilesProvider) mTileProvider).detach();
        // }
        // mTileProvider = null;
        // }
        super.releaseSource();
    }

    @Override
    public String getApiName() {
        return "Akylas.Carto.TileSource";
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        super.propertySet(key, newValue, oldValue, changedProperty);
        switch (key) {
        case TiC.PROPERTY_OPACITY:
            opacity = TiConvert.toFloat(newValue, 1.0f);
            break;
        case "tileSize":
            tileSize = TiConvert.toInt(newValue, 256);
            break;
        case "fadeIn":
            fadeIn = TiConvert.toBoolean(newValue);
            break;
        case "style":
            style = TiConvert.toString(newValue);
            break;
        case "styleFile":
            vectorStyleFile = TiConvert.toString(newValue);
            break;
        case "autoHd":
            autoHd = TiConvert.toBoolean(newValue);
            break;
        case "cacheable":
            cacheable = TiConvert.toBoolean(newValue);
            if (!cacheable && changedProperty) {
                clearCache();
            }
            break;
        case "showTileAfterMaxZoom":
            showTileAfterMaxZoom = TiConvert.toBoolean(newValue);
            break;
        default:
            break;
        }
    }

    public static Object getMethod(final Object sourceObj,
            final String methodName) {
        try {
            Method m = sourceObj.getClass().getMethod(methodName,
                    new Class[] {});
            return m.invoke(sourceObj, new Object[] {});
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setMinZoom(Object value) {
        super.setMinZoom(value);
        if (layer != null) {
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
    }

    @Override
    public void setMaxZoom(Object value) {
        super.setMaxZoom(value);
        if (layer != null) {
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
    }
    
    @Override
    public void setVisible(Object value) {
        super.setVisible(value);
        if (layer != null) {
            layer.setVisible(visible);
        }
    }

    @Override
    public void setZIndex(float value) {
        super.setZIndex(value);
        if (layer != null && mapView != null) {
            ((CartoView) mapView).resortLayers();
        }
    }

    @Override
    public void clearCache() {
        // if (mOverlay != null) {
        // getActivity().runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // if (mOverlay != null) {
        // mOverlay.clearTileCache();
        // }
        // }
        // });
        // }
        if (cachedDataSource != null) {
            cachedDataSource.clear();
        }
    }

    protected AkylasMapBaseView mapView;

    public void setMapView(AkylasMapBaseView mbView) {
        mapView = mbView;
    }

    TileLayer layer;
    TileDataSource source;
    PersistentCacheTileDataSource cachedDataSource;

    @Override
    public void removeFromMap() {
        if (layer != null && mapView != null) {
            ((CartoView) mapView).getMapView().getLayers().remove(layer);

            // ((CartoView)
            // mapView).getClusterManager().removeClusterAlgorithm(_algorithm);
        }
        source = null;
        cachedDataSource = null;
        layer = null;
        mapView = null;
    }
    
    private TileDataSource createSource(String sSource) {
        TileDataSource result = null;
        if (sSource == null) {
            return null;
        }
        if (sSource.contains("carto.")) {
            result = new CartoOnlineTileDataSource(sSource);
        } else if (sSource.toLowerCase().endsWith("mbtiles")) {
            try {
                // mTileProvider = new
                // MBTilesProvider(TiDatabaseHelper.getDatabase(this,
                // sSource, false));
                result = new MBTilesTileDataSource(sSource);
            } catch (Exception e) {
                release();
                Log.e(TAG, "Could not load file " + mSource);
            }
        } else {
            // final int tileSize = TiConvert.toInt(getProperty("tileSize"),
            // 256);
            result = new HTTPTileDataSource((int) mMinZoom, (int) mMaxZoom,
                    sSource);
            String subs = TiConvert.toString(getProperty("subdomains"),
                    "abc");
            StringVector vector = new StringVector(subs.length());
            for (int i = 0; i < subs.length(); i++) {
                vector.add(subs.substring(i, i + 1));
            }
            ((HTTPTileDataSource) result).setSubdomains(vector);
            String userAgent = TiConvert.toString(getProperty("userAgent"));
            if (userAgent != null) {
                StringMap map = new StringMap();
                map.set("User-Agent", userAgent);
                ((HTTPTileDataSource) result).setHTTPHeaders(map);
            }
        }
        return result;
    }

    private TileDataSource getSource() {
        if (source == null && this.mapView != null) {
            Object[] sSource = null;
            if (mSource instanceof Object[]) {
                sSource = (Object[]) mSource;
            } else if (!(mSource instanceof String)) {
                // Object db = getMethod(mSource, "getDatabase");
                // if (db instanceof SQLiteDatabase) {
                // mDb = (SQLiteDatabase) db;
                //
                // source = new MBTilesTileDataSource(
                // TiConvert.toString(getProperty("id")),
                // (SQLiteDatabase) db, tileSize);
                // return mTileProvider;
                // } else {
                Object path = getMethod(mSource, "getNativePath");
                if (path instanceof String) {
                    sSource = new String[] {(String) path};
                }
                // }
            } else {
                sSource = new String[] {(String) mSource};
            }
            if (sSource != null) {
                final int length = sSource.length;
                if (length  == 1) {
                    source = createSource(TiConvert.toString(sSource[0]));
                } else if (length  > 1) {
                    List<TileDataSource> results = new ArrayList<TileDataSource>();
                    for(int i =0; i < length; i++) {
                        TileDataSource res = createSource(TiConvert.toString(sSource[i]));
                        if (res != null ) {
                            results.add(res);
                        }
                    }
                    if (results.size() == 1) {
                        source = results.get(0);
                    } else if (results.size() > 1) {
                        source = new MergeTileDataSource(results.toArray(new TileDataSource[results.size()]));
                    }
                }
            }
            
            if (source != null) {
                if (cacheable) {
                    String id = TiConvert.toString(getProperty("id"), "");
                    cachedDataSource = new PersistentCacheTileDataSource(source,
                            AkylasCartoModule.getMapCacheFolder() + "/" + id + ".db");
                }
            }
        }
        return (cachedDataSource != null) ? cachedDataSource : source;
    }

    private String style = "voyager";
    private String vectorStyleFile = "carto.zip";
    
    ZippedAssetPackage styleAsset;
    public ZippedAssetPackage getStyleZippedAsset() {
        if (styleAsset == null) {
            BinaryData data = AssetUtils.loadAsset(vectorStyleFile);
            styleAsset = new ZippedAssetPackage(data);
        }
        return styleAsset;
    }

    public TileLayer getLayer() {
        return layer;
    }
    public TileLayer getOrCreateLayer() {
        if (layer == null) {
            TileDataSource theSource = getSource();
            if (source instanceof MBTilesTileDataSource) {
                // VectorTileDecoder decoder = ((CartoView) mapView)
                // .getTileDecoder();
                layer = new RasterTileLayer(theSource);

            } else {
                String sourceUrl = null;
                if (mSource instanceof Object[]) {
                    sourceUrl = ((Object[])mSource)[0].toString().toLowerCase();
                } else {
                    sourceUrl = mSource.toString().toLowerCase();
                }
                if (sourceUrl.contains("mvt") || sourceUrl.contains("pbf") || sourceUrl.startsWith("carto.")) {
                 // load style from zip file in assets
                    VectorTileDecoder vectorTileDecoder = new MBVectorTileDecoder(new CompiledStyleSet(getStyleZippedAsset(), style));

                    // 4. Create vector tile layer, using previously created
                    // data source and decoder
                    layer = new VectorTileLayer(getSource(), vectorTileDecoder);
                    ((VectorTileLayer) layer).setLabelRenderOrder(VectorTileRenderOrder.VECTOR_TILE_RENDER_ORDER_LAST);
//                    ((VectorTileLayer)layer).setVectorTileEventListener(((CartoView) mapView).getVectorTileListener());
                } else {
                    layer = new RasterTileLayer(theSource);                    
                }
            }
            if (layer != null) {
                // layer.setMaxOverzoomLevel(24);
                layer.setVisible(visible);
                layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
//                if (layer instanceof VectorTileLayer) {
//                }
            }
            // layer = new ClusteredVectorLayer(getSource(),
            // new MyClusterElementBuilder());
            // layer.setVisible(visible);
            // layer.setMinimumClusterDistance(minDistance);
            // layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
        return layer;
    }

}
