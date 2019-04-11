package akylas.carto;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.carto.core.BinaryData;
import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.core.MapPosVector;
import com.carto.packagemanager.CartoPackageManager;
import com.carto.packagemanager.PackageErrorType;
import com.carto.packagemanager.PackageInfo;
import com.carto.packagemanager.PackageInfoVector;
import com.carto.packagemanager.PackageManagerListener;
import com.carto.packagemanager.PackageStatus;
import com.carto.projections.EPSG3857;
import com.carto.projections.Projection;
import com.carto.routing.PackageManagerValhallaRoutingService;
import com.carto.routing.RoutingRequest;
import com.carto.routing.RoutingResult;
import com.carto.routing.ValhallaOnlineRoutingService;
import com.carto.styles.CompiledStyleSet;
import com.carto.ui.MapView;
import com.carto.utils.AssetUtils;
import com.carto.utils.ZippedAssetPackage;
import com.carto.vectortiles.MBVectorTileDecoder;
import com.carto.vectortiles.VectorTileDecoder;

import akylas.map.common.AkylasMapBaseModule;
import android.location.Location;
import android.os.AsyncTask;

@Kroll.module(name = "AkylasCarto", id = "akylas.carto", parentModule = AkylasMapBaseModule.class)
public class AkylasCartoModule extends AkylasMapBaseModule<MapPos, MapBounds> {

    static {
        System.loadLibrary("carto_mobile_sdk");
    }
    private static final String TAG = "AkylasCartoModule";

    public static MapBounds WORLD_BOUNDING_BOX = (MapBounds) getFactory()
            .createRegion(90, 180, -90, -180);
    public static MapBounds MIN_BOUNDING_BOX = (MapBounds) getFactory()
            .createRegion(-90, -180, -90, -180);

    private class OurPackageManagerListener extends PackageManagerListener {
        private boolean isRouting = false;
        public OurPackageManagerListener(boolean isRouting) {
            super();
            this.isRouting = isRouting;
        }
        @Override
        public void onPackageCancelled(String id, int version) {
            // TODO Auto-generated method stub
            super.onPackageCancelled(id, version);
            Log.d(TAG, "onPackageCancelled " + id + " " + version);
            KrollDict result = new KrollDict();
            result.put("routing", this.isRouting);
            result.put(TiC.PROPERTY_ID, id);
            result.put(TiC.PROPERTY_VERSION, version);
            AkylasCartoModule.this.fireEvent("package_cancelled", result);

        }

        @Override
        public void onPackageFailed(String id, int version,
                PackageErrorType errorType) {

            super.onPackageFailed(id, version, errorType);
            Log.d(TAG,
                    "onPackageFailed " + id + " " + version + " " + errorType);
            KrollDict result = new KrollDict();
            result.put("routing", this.isRouting);
            result.put(TiC.PROPERTY_ID, id);
            result.put(TiC.PROPERTY_VERSION, version);
            result.put(TiC.PROPERTY_ERROR, errorType);
            AkylasCartoModule.this.fireEvent("package_failed", result);
        }

        @Override
        public void onPackageListFailed() {
            super.onPackageListFailed();
            Log.d(TAG, "onPackageListFailed ");
            KrollDict result = new KrollDict();
            result.put("routing", this.isRouting);
            AkylasCartoModule.this.fireEvent("package_list_failed", result);
        }

        @Override
        public void onPackageListUpdated() {
            // TODO Auto-generated method stub
            super.onPackageListUpdated();
            Log.d(TAG, "onPackageListUpdated ");
            KrollDict result = new KrollDict();
            result.put("routing", this.isRouting);
            AkylasCartoModule.this.fireEvent("package_list_updated", result);
        }

        @Override
        public void onPackageStatusChanged(String id, int version,
                PackageStatus status) {
            super.onPackageStatusChanged(id, version, status);
            Log.d(TAG, "onPackageStatusChanged " + id + " " + version + " "
                    + status);
            KrollDict result = new KrollDict();
            result.put(TiC.PROPERTY_ID, id);
            result.put("routing", this.isRouting);
            result.put(TiC.PROPERTY_VERSION, version);
            result.put(TiC.PROPERTY_STATUS, status.getCurrentAction());
            result.put(TiC.PROPERTY_PROGRESS, status.getProgress());
            result.put("paused", status.isPaused());
            AkylasCartoModule.this.fireEvent("package_status", result);
        }

        @Override
        public void onPackageUpdated(String id, int version) {
            super.onPackageUpdated(id, version);
            Log.d(TAG, "onPackageUpdated " + id + " " + version);
            KrollDict result = new KrollDict();
            result.put(TiC.PROPERTY_ID, id);
            result.put(TiC.PROPERTY_VERSION, version);
            AkylasCartoModule.this.fireEvent("package_updated", result);
        }
    };

    public static Factory getFactory() {
        if (mFactory == null) {
            mFactory = new Factory<MapPos, MapBounds>() {
                @Override
                public MapPos createPoint(final double latitude,
                        final double longitude, final double altitude) {
                    return new MapPos(longitude, latitude, altitude);
                }

                @Override
                public MapBounds createRegion(final double north,
                        final double east, final double south,
                        final double west) {
                    return new MapBounds(new MapPos(west, south),
                            new MapPos(east, north));
                }

                @Override
                public MapBounds createRegion(final MapPos center,
                        final double latitudeDelta_2,
                        final double longitudeDelta_2) {
                    return new MapBounds(
                            new MapPos(center.getX() - latitudeDelta_2,
                                    center.getY() - longitudeDelta_2),
                            new MapPos(center.getX() + latitudeDelta_2,
                                    center.getY() + longitudeDelta_2));
                }

                @Override
                public MapBounds createRegion(final MapPos ne,
                        final MapPos sw) {
                    try {
                        return new MapBounds(sw, ne);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public boolean isRegionValid(MapBounds region) {
                    return region != null;
                }

                @Override
                public MapBounds unionRegions(MapBounds region1,
                        MapBounds region2) {
                    if (region1 == null) {
                        return region2;
                    }
                    if (region2 == null) {
                        return region1;
                    }

                    return createRegion(
                            Math.max(region1.getMax().getY(),
                                    region2.getMax().getY()),
                            Math.max(region1.getMax().getX(),
                                    region2.getMax().getX()),
                            Math.min(region1.getMin().getY(),
                                    region2.getMin().getY()),
                            Math.min(region1.getMin().getX(),
                                    region2.getMin().getX()));
                }

                @Override
                public KrollDict regionToDict(MapBounds region) {
                    if (region == null)
                        return null;
                    KrollDict result = new KrollDict();
                    KrollDict ne = new KrollDict();
                    KrollDict sw = new KrollDict();
                    ne.put(TiC.PROPERTY_LATITUDE, region.getMax().getY());
                    ne.put(TiC.PROPERTY_LONGITUDE, region.getMax().getX());
                    sw.put(TiC.PROPERTY_LATITUDE, region.getMin().getY());
                    sw.put(TiC.PROPERTY_LONGITUDE, region.getMin().getX());
                    result.put(PROPERTY_NE, ne);
                    result.put(PROPERTY_SW, sw);
                    return result;
                }

                @Override
                public double getLatitude(MapPos point) {
                    return point.getY();
                }

                @Override
                public double getLongitude(MapPos point) {
                    return point.getX();
                }

                @Override
                public double getAltitude(MapPos point) {
                    return point.getZ();
                }

                @Override
                public MapBounds unionRegion(MapBounds region1, MapPos point) {
                    if (region1 != null && point != null) {
                        return createRegion(
                                Math.max(region1.getMax().getY(), point.getY()),
                                Math.max(region1.getMax().getX(), point.getX()),
                                Math.min(region1.getMin().getX(), point.getX()),
                                Math.min(region1.getMin().getX(),
                                        point.getX()));
                    }
                    return region1;
                }
                
                @Override
                public KrollDict latlongToDict(MapPos point) {
                    if (point == null)
                        return null;
                    KrollDict result = new KrollDict();
                    result.put(TiC.PROPERTY_LATITUDE, point.getY());
                    result.put(TiC.PROPERTY_LONGITUDE, point.getX());
                    return result;
                }

                @Override
                public double[] latlongToArray(MapPos point) {
                    if (point == null)
                        return null;
                    double[] result = new double[2];
                    result[0] = point.getY();
                    result[1] = point.getX();
                    return result;
                }

                @Override
                public double getDistance(MapPos p1, MapPos p2) {
                    float[] results = new float[1];
                    Location.distanceBetween(p1.getY(), p1.getX(), p2.getY(),
                            p2.getX(), results);
                    return results[0];
                }

                @Override
                public boolean isPoint(Object p) {
                    return p instanceof MapPos;
                }
            };
        }
        return mFactory;
    }

    public AkylasCartoModule() {
        super();
        // getOfflineManager().setPackageManagerListener(packageListener);
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("Akylas.Carto.View", ViewProxy.class.getName());
        map.put("Akylas.Carto.Annotation", AnnotationProxy.class.getName());
        map.put("Akylas.Carto.Route", RouteProxy.class.getName());
        map.put("Akylas.Carto.TileSource", TileSourceProxy.class.getName());
        map.put("Akylas.Carto.Cluster", ClusterProxy.class.getName());
        APIMap.addMapping(map);
    }

    @Kroll.onVerifyModule
    public static void onVerifyModule(TiApplication app) {
        verifyPassword(app, "akylas.modules.key",
                AeSimpleSHA1.hexToString("7265745b496b2466553b486f736b7b4f"));
    }
    // @Override
    // protected void initActivity(Activity activity) {
    // super.initActivity(activity);
    // }

    @Kroll.method
    @Kroll.setProperty
    public void setLicense(String license) {
        MapView.registerLicense(license, TiApplication.getAppContext());
        try {
            getOfflineManager().setPackageManagerListener(new OurPackageManagerListener(false));
            getOfflineManager().startPackageListDownload();
            getOfflineRoutingManager().setPackageManagerListener(new OurPackageManagerListener(true));
            getOfflineRoutingManager().startPackageListDownload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int MAP_TYPE_VOYAGER = 0;
    public static final int MAP_TYPE_POSITRON = 1;
    public static final int MAP_TYPE_DARKMATTER = 2;
    static KrollDict mapTypes = null;

    @Kroll.method
    @Kroll.getProperty
    public KrollDict MapType() {
        if (mapTypes == null) {
            mapTypes = new KrollDict();
            mapTypes.put("none", MAP_TYPE_NONE);
            mapTypes.put("voyager", MAP_TYPE_VOYAGER);
            mapTypes.put("positron", MAP_TYPE_POSITRON);
            mapTypes.put("darkmatter", MAP_TYPE_DARKMATTER);
        }
        return mapTypes;
    }

    private static String cacheFolder = null;
    private static String packagesFolder = null;
    private static String routingFolder = null;
    private static String mbtilesFolder = null;

    public static String getMapCacheFolder() {
        if (cacheFolder == null) {
            File baseDirectory = TiApplication.getAppContext()
                    .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "cache");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create cache folder!");
            } else {
                cacheFolder = folder.getAbsolutePath();
            }
        }
        return cacheFolder;
    }
    
    public static String getStaticMapPackagesFolder() {
        if (packagesFolder == null) {
            File baseDirectory = TiApplication.getAppContext()
                    .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "packages");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create cache folder!");
            } else {
                packagesFolder = folder.getAbsolutePath();
            }
        }
        return packagesFolder;
    }

    public static String getStaticRoutingPackagesFolder() {
        if (routingFolder == null) {
            File baseDirectory = TiApplication.getAppContext()
                    .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "routingpackages");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create cache folder!");
            } else {
                routingFolder = folder.getAbsolutePath();
            }
        }
        return routingFolder;
    }

    public static String getStaticMbTilesFolder() {
        if (mbtilesFolder == null) {
            File baseDirectory = TiApplication.getAppContext()
                    .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "mbtiles");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create mbTiles folder!");
            } else {
                mbtilesFolder = folder.getAbsolutePath();
            }
        }
        return mbtilesFolder;
    }
    
    private static CartoPackageManager offlineManager = null;
    
    public static CartoPackageManager getOfflineManager() {
        if (offlineManager == null) {
            // Create package manager
            try {
                offlineManager = new CartoPackageManager("carto.streets",
                        AkylasCartoModule.getStaticMapPackagesFolder());
                offlineManager.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return offlineManager;
    }

    private static CartoPackageManager offlineRoutingManager = null;

    public static CartoPackageManager getOfflineRoutingManager() {
        if (offlineRoutingManager == null) {
            // Create package manager
            try {
                offlineRoutingManager = new CartoPackageManager("routing:carto.streets",
                        AkylasCartoModule.getStaticRoutingPackagesFolder());
                offlineRoutingManager.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return offlineRoutingManager;
    }

    @Kroll.method
    @Kroll.getProperty
    public String getMbTilesFolder() {
        return getStaticMbTilesFolder();
    }

    @Kroll.method
    @Kroll.getProperty
    public String getMapPackagesFolder() {
        return getStaticMapPackagesFolder();
    }
    
    @Kroll.method
    public Object[] getServerPackages() {
        final CartoPackageManager manager = getOfflineManager();
        PackageInfoVector vector = manager.getServerPackages();
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
            // data.put("mask",info.getTileMask());
            data.put("version", info.getVersion());
            // data.put("metainfo",info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }
    

    @Kroll.method
    public Object[] getServerRoutingPackages() {
        final CartoPackageManager manager = getOfflineRoutingManager();
        PackageInfoVector vector = manager.getServerPackages();
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
            // data.put("mask",info.getTileMask());
            data.put("version", info.getVersion());
            // data.put("metainfo",info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }
    
    @Kroll.method
    public Object[] getLocalPackages() {
        final CartoPackageManager manager = getOfflineManager();
        PackageInfoVector vector = manager.getLocalPackages();
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
            // data.put("mask",info.getTileMask());
            data.put("version", info.getVersion());
            // data.put("metainfo",info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }

    @Kroll.method
    public Object[] getLocalRoutingPackages() {
        final CartoPackageManager manager = getOfflineRoutingManager();
        PackageInfoVector vector = manager.getLocalPackages();
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
//            PackageTileMask  mask = info.getTileMask();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
            // data.put("mask",info.getTileMask());
            data.put("version", info.getVersion());
//            Variant metainfo = info.getMetaInfo().getVariant();
            // data.put("metainfo",info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }
    
    @Kroll.method
    public void startPackageDownload(String packageId) {
        final CartoPackageManager manager = getOfflineManager();
        manager.startPackageDownload(packageId);
    }

    @Kroll.method
    public void startRoutingPackageDownload(String packageId) {
        final CartoPackageManager manager = getOfflineRoutingManager();
        manager.startPackageDownload(packageId);
    }
    
    @Kroll.method
    public void startPackageRemove(String packageId) {
        final CartoPackageManager manager = getOfflineManager();
        manager.startPackageRemove(packageId);
    }

    @Kroll.method
    public void startRoutingPackageRemove(String packageId) {
        final CartoPackageManager manager = getOfflineRoutingManager();
        manager.startPackageRemove(packageId);
    }
    
    @Kroll.method
    public void startPackageListDownload() {
        final CartoPackageManager manager = getOfflineManager();
        manager.startPackageListDownload();
    }

    @Kroll.method
    public void startRoutingPackageListDownload() {
        final CartoPackageManager manager = getOfflineRoutingManager();
        manager.startPackageListDownload();
    }
    
    @Kroll.method
    public Object[] suggestOfflinePackage(Object mapPos) {
        final CartoPackageManager manager = getOfflineManager();
        PackageInfoVector vector = manager.suggestPackages(
                (MapPos) AkylasCartoModule.latlongFromObject(mapPos),
                new EPSG3857());
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
//            data.put("mask", info.getTileMask());
            data.put("version", info.getVersion());
//            data.put("metainfo", info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }

    @Kroll.method
    public Object[] suggestOfflineRoutingPackage(Object mapPos) {
        final CartoPackageManager manager = getOfflineRoutingManager();
        PackageInfoVector vector = manager.suggestPackages(
                (MapPos) AkylasCartoModule.latlongFromObject(mapPos),
                new EPSG3857());
        int count = (int) vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for (int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id", info.getPackageId());
            data.put("type", info.getPackageType());
//            data.put("mask", info.getTileMask());
            data.put("version", info.getVersion());
//            data.put("metainfo", info.getMetaInfo());
            data.put("name", info.getName());
            data.put("size", info.getSize());
            result[i] = data;
        }
        return result;
    }

    @Kroll.method
    public void calculateRoute(final String mapZenKey, final String profile,
            final Object[] locs, final KrollFunction callback) {
       final Projection proj = new EPSG3857();
       final AsyncTask<Void, Void, RoutingResult> task = new AsyncTask<Void, Void, RoutingResult>() {
            protected RoutingResult doInBackground(Void... v) {
                ValhallaOnlineRoutingService onlineRoutingService = new ValhallaOnlineRoutingService(
                        mapZenKey);
                onlineRoutingService.setProfile(profile);
                MapPosVector poses = new MapPosVector();
                for (int i = 0; i < locs.length; i++) {
                    Object loc = locs[i];
                    poses.add(
                            proj.fromWgs84((MapPos) AkylasCartoModule.latlongFromObject(loc)));
                }
                RoutingRequest request = new RoutingRequest(proj,
                        poses);
                RoutingResult result;
                try {
                    result = onlineRoutingService.calculateRoute(request);
                    return result;
                } catch (IOException e) {
                    KrollDict data = new KrollDict();
                    data.putCodeAndMessage(-1, e.getLocalizedMessage());
                    callback.callAsync(getKrollObject(), data);
                }
                return null;

            }

            protected void onPostExecute(RoutingResult rresult) {

                KrollDict result = null;
                if (rresult != null) {
                    result = new KrollDict();
                    result.put("distance", rresult.getTotalDistance());
                    result.put("duration", rresult.getTotalTime() * 1000);
                    MapPosVector pts = rresult.getPoints();
                    int pointsCount = (int) pts.size();
                    Object[] points = new Object[pointsCount];
                    // MapPos point;
                    for (int i = 0; i < pointsCount; i++) {
                        points[i] = AkylasCartoModule
                                .latLongToArray(proj.toWgs84(pts.get(i)));
                    }
                    result.put("points", points);
                    callback.callAsync(getKrollObject(), result);
                }
            }
        };

        task.execute();
    }
    
    @Kroll.method
    public void calculateOfflineRoute(final String profile,
            final Object[] locs, final KrollFunction callback) {
        final Projection proj = new EPSG3857();
        final AsyncTask<Void, Void, RoutingResult> task = new AsyncTask<Void, Void, RoutingResult>() {
            protected RoutingResult doInBackground(Void... v) {
                PackageManagerValhallaRoutingService routingService = new PackageManagerValhallaRoutingService(
                        getOfflineRoutingManager());
                routingService.setProfile(profile);
                MapPosVector poses = new MapPosVector();
                for (int i = 0; i < locs.length; i++) {
                    Object loc = locs[i];
                    poses.add(
                            proj.fromWgs84((MapPos) AkylasCartoModule.latlongFromObject(loc)));
                }
                RoutingRequest request = new RoutingRequest(proj,
                        poses);
                RoutingResult result;
                try {
                    result = routingService.calculateRoute(request);
                    return result;
                } catch (IOException e) {
                    KrollDict data = new KrollDict();
                    data.putCodeAndMessage(-1, e.getLocalizedMessage());
                    callback.callAsync(getKrollObject(), data);
                }
                return null;

            }

            protected void onPostExecute(RoutingResult rresult) {

                KrollDict result = null;
                if (rresult != null) {
                    result = new KrollDict();
                    result.put("distance", rresult.getTotalDistance());
                    result.put("duration", rresult.getTotalTime() * 1000);
                    MapPosVector pts = rresult.getPoints();
                    int pointsCount = (int) pts.size();
                    Object[] points = new Object[pointsCount];
                    // MapPos point;
                    for (int i = 0; i < pointsCount; i++) {
                        points[i] = AkylasCartoModule
                                .latLongToArray(proj.toWgs84(pts.get(i)));
                    }
                    result.put("points", points);
                    callback.callAsync(getKrollObject(), result);
                }
            }
        };

        task.execute();
    }
    
    public static String defaultStyle = "voyager";
    public static String defaultVectorStyleFile = "Resources/styles/bright.zip";

    static HashMap<String, ZippedAssetPackage> styleAssets = new HashMap<>();
    static HashMap<String, VectorTileDecoder> vectorTileDecoders = new HashMap<>();

    public static ZippedAssetPackage getStyleZippedAsset(final String vectorStyleFile) {
        if (!styleAssets.containsKey(vectorStyleFile)) {
            BinaryData data = AssetUtils.loadAsset(vectorStyleFile);
            styleAssets.put(vectorStyleFile, new ZippedAssetPackage(data));
        }
        return styleAssets.get(vectorStyleFile);
    }


    public static VectorTileDecoder getVectorTileDecoder(final String vectorStyleFile, final String style) {
        String key = vectorStyleFile + "_"  + style;
        if (!vectorTileDecoders.containsKey(key)) {
            try  {
                vectorTileDecoders.put(key, new MBVectorTileDecoder(
                        new CompiledStyleSet(getStyleZippedAsset(vectorStyleFile), style)));
            } catch(Exception e) {
                 Log.e(TAG, e.getLocalizedMessage());
                 return getVectorTileDecoder(vectorStyleFile, defaultStyle);
            }
        }
        return vectorTileDecoders.get(key);
    }
    

    @Kroll.method
    public void findJSONFeatures(final String jsonURL,
            final Object mapPos, final KrollFunction callback) {
        final Projection proj = new EPSG3857();
        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... v) {
                return null;

            }

            protected void onPostExecute(RoutingResult rresult) {

                KrollDict result = null;
                if (rresult != null) {
                    result = new KrollDict();
                    result.put("distance", rresult.getTotalDistance());
                    result.put("duration", rresult.getTotalTime() * 1000);
                    MapPosVector pts = rresult.getPoints();
                    int pointsCount = (int) pts.size();
                    Object[] points = new Object[pointsCount];
                    // MapPos point;
                    for (int i = 0; i < pointsCount; i++) {
                        points[i] = AkylasCartoModule
                                .latLongToArray(proj.toWgs84(pts.get(i)));
                    }
                    result.put("points", points);
                    callback.callAsync(getKrollObject(), result);
                }
            }
        };

        task.execute();
    }
    
}
