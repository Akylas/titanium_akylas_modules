package akylas.carto;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiColorHelper;
import org.appcelerator.titanium.util.TiConvert;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapRange;
import com.carto.core.MapTile;
import com.carto.core.StringMap;
import com.carto.core.StringVector;
import com.carto.core.Variant;
import com.carto.datasources.CartoOnlineTileDataSource;
import com.carto.datasources.PersistentCacheTileDataSource;
import com.carto.datasources.TileDataSource;
import com.carto.datasources.TileDownloadListener;
import com.carto.datasources.components.TileData;
import com.carto.geometry.Feature;
import com.carto.geometry.FeatureCollection;
import com.carto.geometry.GeoJSONGeometryReader;
import com.carto.geometry.Geometry;
import com.carto.geometry.PointGeometry;
import com.carto.geometry.VectorTileFeature;
import com.carto.geometry.VectorTileFeatureCollection;
import com.carto.layers.RasterTileEventListener;
import com.carto.layers.RasterTileLayer;
import com.carto.layers.TileLayer;
import com.carto.layers.UTFGridEventListener;
import com.carto.layers.VectorTileLayer;
import com.carto.layers.VectorTileRenderOrder;
import com.carto.projections.Projection;
import com.carto.search.FeatureCollectionSearchService;
import com.carto.search.SearchRequest;
import com.carto.search.VectorTileSearchService;
import com.carto.ui.RasterTileClickInfo;
import com.carto.ui.UTFGridClickInfo;
import com.carto.vectortiles.VectorTileDecoder;

import akylas.map.common.AkylasMapBaseView;
import akylas.map.common.BaseTileSourceProxy;
import android.os.AsyncTask;

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
    // private boolean fadeIn = true;
    private float opacity = 1.0f;
    private int tileSize = 256;
    private boolean autoHd = false;
    private boolean cacheable = true;
    private boolean showTileAfterMaxZoom = true;
    private String utfGridSource = null;
    // private String mSRS = "4326";

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
            float fValue = TiConvert.toFloat(newValue, 1.0f);
            if (fValue != opacity) {
                opacity = fValue;
                if (layer != null) {
                    layer.setOpacity(opacity);
                }
            }

            break;
        case "tileSize":
            tileSize = TiConvert.toInt(newValue, 256);
            if (source instanceof HTTPTileDataSource) {
                ((HTTPTileDataSource) source).setTileSize(tileSize);
            } else if (source instanceof MBTilesTileDataSource) {
                ((MBTilesTileDataSource) source).setTileSize(tileSize);
            }
            break;
        // case "fadeIn":
        // fadeIn = TiConvert.toBoolean(newValue);
        // break;
        case "style":
            style = TiConvert.toString(newValue);
            break;
        case "styleFile":
            vectorStyleFile = TiConvert.toString(newValue);
            break;
        case "autoHd":
            autoHd = TiConvert.toBoolean(newValue);
            if (source instanceof HTTPTileDataSource) {
                ((HTTPTileDataSource) source).setAutoHD(autoHd);
            } else if (source instanceof MBTilesTileDataSource) {
                ((MBTilesTileDataSource) source).setAutoHD(autoHd);
            }
            break;
        case "cacheable":
            cacheable = TiConvert.toBoolean(newValue);
            if (!cacheable && changedProperty) {
                clearCache();
            }
            break;
        case "showTileAfterMaxZoom":
            showTileAfterMaxZoom = TiConvert.toBoolean(newValue);
            if (source instanceof HTTPTileDataSource) {
//                ((HTTPTileDataSource) source)
//                        .setShowTileAfterMaxZoom(showTileAfterMaxZoom);
            } else if (source instanceof MBTilesTileDataSource) {
//                ((MBTilesTileDataSource) source)
//                        .setShowTileAfterMaxZoom(showTileAfterMaxZoom);
            }
            break;
        case "gridSource":
            utfGridSource = TiConvert.toString(newValue);
            handleUtfGrid();
            break;
        default:
            break;
        }
    }

    private void handleUtfGrid() {
        if (layer != null) {
            if (utfGridSource == null) {
                layer.setUTFGridDataSource(null);
                layer.setUTFGridEventListener(null);
            } else {
                layer.setUTFGridDataSource(new HTTPTileDataSource(
                        TiConvert.toInt(getProperty("gridMinZoom"),
                                (int) mMinZoom),
                        TiConvert.toInt(getProperty("gridMaxZoom"),
                                (int) mMaxZoom),
                        utfGridSource));
                layer.setUTFGridEventListener(new UTFGridEventListener() {
                    @Override
                    public boolean onUTFGridClicked(
                            UTFGridClickInfo clickInfo) {
                        if (TileSourceProxy.this.hasListeners(
                                AkylasCartoModule.EVENT_GRID, false)) {
                            Projection projection = source.getProjection();
                            MapPos pos = projection.toWgs84(clickInfo.getClickPos());
                            KrollDict d = new KrollDict();
                            HashMap poi = new HashMap();
                            poi.put(TiC.PROPERTY_LATITUDE, pos.getY());
                            poi.put(TiC.PROPERTY_LONGITUDE, pos.getX());
                            poi.put("sourceid", getProperty("id"));
                            Variant variant = clickInfo.getElementInfo();
                            StringVector keys = variant.getObjectKeys();
                            for (int i = 0; i < keys.size(); i++) {
                                poi.put(keys.get(i),
                                        variant.getObjectElement(keys.get(i))
                                                .toString());
                            }
                            d.put("grid", poi);
                            TileSourceProxy.this.fireEvent(
                                    AkylasCartoModule.EVENT_GRID, d, false,
                                    false);
                        }
                        return false;
                    }
                });
            }
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
        if (source instanceof HTTPTileDataSource) {
            ((HTTPTileDataSource) source).setMinimumZoomLevel(mMinZoom);
        }
    }

    @Override
    public void setMaxZoom(Object value) {
        super.setMaxZoom(value);
        if (layer != null) {
            layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
        if (source instanceof HTTPTileDataSource) {
            ((HTTPTileDataSource) source).setMaximumZoomLevel(mMinZoom);
        }
    }

    @Override
    public void setVisible(Object value) {
        super.setVisible(value);
        if (source instanceof HTTPTileDataSource) {
            ((HTTPTileDataSource) source).setVisible(visible);
        }
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
        if (layer != null) {
            layer.clearTileCaches(true);
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
                result = new MBTilesTileDataSource(
                        sSource.replace("file://", ""));
                ((MBTilesTileDataSource) result).setAutoHD(autoHd);
//                ((MBTilesTileDataSource) result)
//                        .setShowTileAfterMaxZoom(showTileAfterMaxZoom);
                ((MBTilesTileDataSource) result).setTileSize(tileSize);
                ((MBTilesTileDataSource) result).setMinimumZoomLevel(mMinZoom);
                ((MBTilesTileDataSource) result).setMaximumZoomLevel(mMaxZoom);
            } catch (Exception e) {
                release();
                Log.e(TAG, "Could not load file " + mSource);
            }
            // } else if (sSource.toLowerCase().endsWith("pdb")) {
            // try {
            // result = new PersistentCacheTileDataSource(null, sSource);
            // ((PersistentCacheTileDataSource)result).setCacheOnlyMode(true);
            // } catch (Exception e) {
            // release();
            // Log.e(TAG, "Could not load file " + mSource);
            // }
        } else {
            HTTPTileDataSource httpSource = (HTTPTileDataSource) (result = new HTTPTileDataSource(
                    (int) mMinZoom, (int) mMaxZoom, sSource));

            httpSource.setMinimumZoomLevel(mMinZoom);
            httpSource.setMaximumZoomLevel(mMaxZoom);
            httpSource.setVisible(visible);
            httpSource.setOpacity(opacity);
            httpSource.setAutoHD(autoHd);
//            httpSource.setShowTileAfterMaxZoom(showTileAfterMaxZoom);
            httpSource.setTileSize(tileSize);

            String subs = TiConvert.toString(getProperty("subdomains"), "abc");
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
        if (source == null) {
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
                    sSource = new String[] { (String) path };
                }
                // }
            } else {
                sSource = new String[] { (String) mSource };
            }
            if (sSource != null) {
                final int length = sSource.length;
                if (length == 1) {
                    source = createSource(TiConvert.toString(sSource[0]));
                } else if (length > 1) {
                    List<TileDataSource> results = new ArrayList<TileDataSource>();
                    for (int i = 0; i < length; i++) {
                        TileDataSource res = createSource(
                                TiConvert.toString(sSource[i]));
                        if (res != null) {
                            results.add(res);
                        }
                    }
                    if (results.size() == 1) {
                        source = results.get(0);
                    } else if (results.size() > 1) {
                        source = new MergeTileDataSource(results
                                .toArray(new TileDataSource[results.size()]));
                    }
                }
            }

            if (source != null) {
                if (cacheable) {
                    String id = TiConvert.toString(getProperty("id"), null);
                    if (id != null) {
                        cachedDataSource = new PersistentCacheTileDataSource(
                                source, AkylasCartoModule.getMapCacheFolder()
                                        + "/" + id + ".db");
                    }

                }
            }
        }
        return (cachedDataSource != null) ? cachedDataSource : source;
    }

    public TileLayer getLayer() {
        return layer;
    }

    private String style = AkylasCartoModule.defaultStyle;
    private String vectorStyleFile = AkylasCartoModule.defaultVectorStyleFile;
    private VectorTileDecoder vectorTileDecoder = null;

    VectorTileDecoder getVectorTileDecoder() {
        if (vectorTileDecoder == null) {
            vectorTileDecoder = AkylasCartoModule
                    .getVectorTileDecoder(vectorStyleFile, style);
        }
        return vectorTileDecoder;
    }

    public TileLayer getOrCreateLayer(CartoView mapView) {
        if (layer == null) {
            TileDataSource theSource = getSource();
            // if (source instanceof MBTilesTileDataSource) {
            // // VectorTileDecoder decoder = ((CartoView) mapView)
            // // .getTileDecoder();
            // layer = new RasterTileLayer(theSource);
            //
            // } else {
            String sourceUrl = null;
            if (mSource instanceof Object[]) {
                sourceUrl = ((Object[]) mSource)[0].toString().toLowerCase();
            } else {
                sourceUrl = mSource.toString().toLowerCase();
            }
            if (sourceUrl.contains("mvt") || sourceUrl.contains("pbf")
                    || sourceUrl.startsWith("carto.")) {
                // 4. Create vector tile layer, using previously created
                // data source and decoder
                layer = new VectorTileLayer(getSource(),
                        getVectorTileDecoder());
                ((VectorTileLayer) layer).setLabelRenderOrder(
                        VectorTileRenderOrder.VECTOR_TILE_RENDER_ORDER_LAST);
                // if (mapView != null) {
                ((VectorTileLayer) layer).setVectorTileEventListener(
                        mapView.getVectorTileListener());
                // }
            } else if (theSource != null) {
                layer = new RasterTileLayer(theSource);
                final String fSourceUrl = sourceUrl;
                ((RasterTileLayer) layer).setRasterTileEventListener(
                        new RasterTileEventListener() {
                            @Override
                            public boolean onRasterTileClicked(
                                    RasterTileClickInfo clickInfo) {
                                if (TileSourceProxy.this.hasListeners(
                                        AkylasCartoModule.EVENT_TILE, false)) {
                                    Projection projection = source.getProjection();
                                    MapPos pos = projection.toWgs84(clickInfo.getClickPos());
                                    MapTile tile = clickInfo.getMapTile();
                                    KrollDict d = new KrollDict();
                                    HashMap poi = new HashMap();
                                    // poi.put("layer", layer);
                                    poi.put("nearestColor",
                                            TiColorHelper.toHexString(
                                                    clickInfo.getNearestColor()
                                                            .getARGB()));
                                    poi.put("interpolatedColor",
                                            TiColorHelper.toHexString(clickInfo
                                                    .getInterpolatedColor()
                                                    .getARGB()));
                                    poi.put(TiC.PROPERTY_LATITUDE, pos.getY());
                                    poi.put(TiC.PROPERTY_LONGITUDE, pos.getX());
                                    poi.put("x", tile.getX());
                                    poi.put("y", tile.getY());
                                    poi.put("z", tile.getZoom());
                                    poi.put("sourceid", getProperty("id"));
                                    poi.put("url", fSourceUrl);
                                    d.put("tile", poi);
                                    TileSourceProxy.this.fireEvent(
                                            AkylasCartoModule.EVENT_TILE, d,
                                            false, false);
                                }
                                return false;
                            }
                        });
            }
            // }
            if (layer != null) {
                // layer.setMaxOverzoomLevel(24);
//                layer.setVisibleZoomRange(new MapRange(mMinZoom, 24mMaxZoom));
                layer.setVisible(visible);
                if (opacity != 1.0f) {
                    layer.setOpacity(opacity);
                }
                // if (layer instanceof VectorTileLayer) {
                // }
                handleUtfGrid();
            }
            // layer = new ClusteredVectorLayer(getSource(),
            // new MyClusterElementBuilder());

            // layer.setMinimumClusterDistance(minDistance);
            // layer.setVisibleZoomRange(new MapRange(mMinZoom, mMaxZoom));
        }
        return layer;
    }

    PersistentCacheTileDataSource downloadDataSource = null;

    @Kroll.method
    public void downloadRegion(Object region, final String filePath,
            int minZoom, int maxZoom, final KrollProxy listener) {
        if (source == null || cachedDataSource != null) {
            return;
        }
        // downloadDataSource = new PersistentCacheTileDataSource(source,
        // filePath);
        // downloadDataSource.setCapacity(200 * 1024 * 1024);
        Projection projection = source.getProjection();
        MapBounds bounds = (MapBounds) AkylasCartoModule
                .regionFromObject(region);
        bounds = new MapBounds(projection.fromWgs84(bounds.getMin()),
                projection.fromWgs84(bounds.getMax()));
        cachedDataSource.startDownloadArea(bounds, minZoom, maxZoom,
                new TileDownloadListener() {
                    public void onDownloadCompleted() {
                        KrollDict result = new KrollDict();
                        result.put(TiC.PROPERTY_ID, filePath);
                        listener.fireEvent("download_completed", result);
                        // cachedDataSource.close();
                        // cachedDataSource = null;
                    };

                    public void onDownloadFailed(com.carto.core.MapTile tile) {
                        KrollDict result = new KrollDict();
                        result.put(TiC.PROPERTY_ID, filePath);
                        result.put(TiC.PROPERTY_X, tile.getX());
                        result.put(TiC.PROPERTY_Y, tile.getY());
                        result.put(TiC.PROPERTY_Z, tile.getZoom());
                        listener.fireEvent("download_failed", result);
                    };

                    public void onDownloadProgress(float progress) {
                        KrollDict result = new KrollDict();
                        result.put(TiC.PROPERTY_ID, filePath);
                        result.put(TiC.PROPERTY_PROGRESS, progress);
                        listener.fireEvent("download_progress", result);
                    };

                    public void onDownloadStarting(int tileCount) {

                        KrollDict result = new KrollDict();
                        result.put(TiC.PROPERTY_ID, filePath);
                        result.put(TiC.PROPERTY_COUNT, tileCount);
                        listener.fireEvent("download_started", result);
                    };
                });
    }

    // @Kroll.method
    // public boolean isDownloadingRegion() {
    // return cachedDataSource != null && cachedDataSource.i();
    // }

    @Kroll.method
    public void pauseDownloadingRegion() {
        if (cachedDataSource != null) {
            cachedDataSource.stopAllDownloads();
        }
    }

    @Kroll.method
    public void stopDownloadingRegion() {
        if (cachedDataSource != null) {
            cachedDataSource.stopAllDownloads();
            // downloadDataSource.close();
            // downloadDataSource = null;
        }
    }

    @Kroll.method
    public void getTileData(final int x, final int y, final int zoom,
            final KrollFunction callback) {
        final AsyncTask<Void, Void, Void> sendTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                MapTile tile = new MapTile(x, y, zoom, 0);
                TileData data = getSource().loadTile(tile);
                if (data != null) {
                    KrollDict response = new KrollDict();
                    byte[] bytes = data.getData().getData().clone();
                    response.put("data", TiBlob.blobFromObject(bytes));
                    callback.callAsync(getKrollObject(), response);
                    // final TiBlob result = TiBlob.blobFromObject(bytes);
                    // data.delete();
                    // return TiConvert.bytesToIntArray(bytes);
                } else {
                    callback.callAsync(getKrollObject(), (KrollDict) null);
                }
                return null;
            }
        };
        sendTask.execute();

    }

    private SearchRequest createSearchRequest(HashMap options) {
        SearchRequest request = new SearchRequest();
//        request.setFilterExpression(null);
//        request.setRegexFilter(null);
        Projection proj = getSource().getProjection();
        request.setProjection(proj);
        if (options.containsKey("position")) {
            request.setGeometry(new PointGeometry(proj.fromWgs84((MapPos) AkylasCartoModule
                    .latlongFromObject(options.get("position")))));
        } else {

        }
        if (options.containsKey("radius")) {
            request.setSearchRadius(TiConvert.toFloat(options, "radius", 0.0f));
        }
        if (options.containsKey("filter")) {
            request.setFilterExpression(TiConvert.toString(options, "filter"));
        }
        if (options.containsKey("regexFilter")) {
            request.setRegexFilter(TiConvert.toString(options, "regexFilter"));
        }
        Log.d(TAG, "searchRequest: " + request.toString());
        return request;
    }

    private HashMap featureToDict(Feature feature) {
        HashMap data = new HashMap();
        Variant variant = feature.getProperties();
        StringVector keys = variant.getObjectKeys();
        for (int i = 0; i < keys.size(); i++) {
            data.put(keys.get(i),
                    variant.getObjectElement(keys.get(i)).toString());
        }
        Geometry geo = feature.getGeometry();
        MapPos pos = geo.getCenterPos();
        data.put("latitude", pos.getY());
        data.put("longitude", pos.getX());
        if (feature instanceof VectorTileFeature) {
            data.put("layer", ((VectorTileFeature) feature).getLayerName());
            data.put("id", ((VectorTileFeature) feature).getId());

        }
        return data;
    }

    @Kroll.method
    public void searchFeatures(final HashMap options,
            final KrollFunction callback) {
        final AsyncTask<Void, Void, Void> sendTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                SearchRequest request = createSearchRequest(options);
                if (options.containsKey("geojson")) {
                    GeoJSONGeometryReader reader = new GeoJSONGeometryReader();
                    final String geojson = TiConvert.toString(options,"geojson");
                    FeatureCollection features = reader.readFeatureCollection(geojson);
//                    final int featuresCount = features.getFeatureCount();
                    FeatureCollectionSearchService searchService = new FeatureCollectionSearchService(getSource().getProjection(), features);
                    FeatureCollection result = searchService
                            .findFeatures(request);
                    final int count = result.getFeatureCount();
                    Object[] tResult = new Object[count];
                    for (int i = 0; i < count; i++) {
                        tResult[i] = featureToDict(result.getFeature(i));
                    }
                    callback.callAsync(getKrollObject(),
                            new Object[] { tResult });
                }
                 else {
                     String sourceUrl = null;
                     if (mSource instanceof Object[]) {
                         sourceUrl = ((Object[]) mSource)[0].toString()
                                 .toLowerCase();
                     } else {
                         sourceUrl = mSource.toString().toLowerCase();
                     }

                     if (sourceUrl.contains("mvt") || sourceUrl.contains("pbf")
                             || sourceUrl.startsWith("carto.")) {
                         VectorTileSearchService searchService = new VectorTileSearchService(
                                 getSource(), getVectorTileDecoder());
                         VectorTileFeatureCollection result = searchService
                                 .findFeatures(request);
                         final int count = result.getFeatureCount();
                         Object[] tResult = new Object[] { count };
                         for (int i = 0; i < count; i++) {
                             tResult[i] = featureToDict(result.getFeature(i));
                         }
                         callback.callAsync(getKrollObject(),
                                 new Object[] { tResult });
                     }
                }
                return null;
            }
        };
        sendTask.execute();

    }

}
