package akylas.mapboxgl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import akylas.map.common.AkylasMapBaseModule;
import akylas.map.common.AkylasMapBaseModule.Factory;
import android.app.Activity;
import android.location.Location;

@Kroll.module(name = "AkylasMGL", id = "akylas.mapboxgl", parentModule = AkylasMapBaseModule.class)
public class AkylasMapboxGLModule extends
        AkylasMapBaseModule<LatLng, LatLngBounds> {

    
    
    static {
        System.loadLibrary("carto_mobile_sdk");
    }
    private static final String TAG = "AkylasCartoModule";

    public static LatLngBounds WORLD_BOUNDING_BOX = (LatLngBounds) getFactory()
            .createRegion(90, 180, -90, -180);
    public static LatLngBounds MIN_BOUNDING_BOX = (LatLngBounds) getFactory()
            .createRegion(-90, -180, -90, -180);

    public static Factory getFactory() {
        if (mFactory == null) {
            mFactory = new Factory<LatLng, LatLngBounds>() {
                @Override
                public LatLng createPoint(final double latitude,
                        final double longitude, final double altitude) {
                    return new LatLng(latitude, longitude);
                }

                @Override
                public LatLngBounds createRegion(final double north,
                        final double east, final double south,
                        final double west) {
                    return LatLngBounds.from(north, east, south, west);
                }

                @Override
                public LatLngBounds createRegion(final LatLng center,
                        final double latitudeDelta_2,
                        final double longitudeDelta_2) {
                    return LatLngBounds.from(center.getLatitude() + latitudeDelta_2,
                            center.getLongitude() + longitudeDelta_2, center.getLatitude() - latitudeDelta_2,
                            center.getLongitude() - longitudeDelta_2);
                }

                @Override
                public LatLngBounds createRegion(final LatLng ne,
                        final LatLng sw) {
                    try {
                        return LatLngBounds.from(ne.getLatitude(), ne.getLongitude(), sw.getLatitude(), sw.getLongitude());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public boolean isRegionValid(LatLngBounds region) {
                    return region != null;
                }

                @Override
                public LatLngBounds unionRegions(LatLngBounds region1,
                        LatLngBounds region2) {
                    if (region1 == null) {
                        return region2;
                    }
                    if (region2 == null) {
                        return region1;
                    }

                    return createRegion(
                            Math.max(region1.getLatNorth(),
                                    region2.getLatNorth()),
                            Math.max(region1.getLonEast(),
                                    region2.getLonEast()),
                            Math.min(region1.getLatSouth(),
                                    region2.getLatSouth()),
                            Math.min(region1.getLonWest(),
                                    region2.getLonWest()));
                }

                @Override
                public KrollDict regionToDict(LatLngBounds region) {
                    if (region == null)
                        return null;
                    KrollDict result = new KrollDict();
                    KrollDict ne = new KrollDict();
                    KrollDict sw = new KrollDict();
                    ne.put(TiC.PROPERTY_LATITUDE, region.getLatNorth());
                    ne.put(TiC.PROPERTY_LONGITUDE, region.getLonEast());
                    sw.put(TiC.PROPERTY_LATITUDE, region.getLatSouth());
                    sw.put(TiC.PROPERTY_LONGITUDE, region.getLonWest());
                    result.put(PROPERTY_NE, ne);
                    result.put(PROPERTY_SW, sw);
                    return result;
                }

                @Override
                public double getLatitude(LatLng point) {
                    return point.getLatitude();
                }

                @Override
                public double getLongitude(LatLng point) {
                    return point.getLongitude();
                }

                @Override
                public double getAltitude(LatLng point) {
                    return 0;
                }

                @Override
                public LatLngBounds unionRegion(LatLngBounds region1,
                        LatLng point) {
                    if (region1 != null && point != null) {
                        return createRegion(
                                Math.max(region1.getLatNorth(),
                                        point.getLatitude()),
                                Math.max(region1.getLonEast(),
                                        point.getLongitude()),
                                Math.min(region1.getLatSouth(),
                                        point.getLatitude()),
                                Math.min(region1.getLonWest(),
                                        point.getLongitude()));
                    }
                    return region1;
                }

                @Override
                public KrollDict latlongToDict(LatLng point) {
                    if (point == null)
                        return null;
                    KrollDict result = new KrollDict();
                    result.put(TiC.PROPERTY_LATITUDE, point.getLatitude());
                    result.put(TiC.PROPERTY_LONGITUDE, point.getLongitude());
                    return result;
                }

                @Override
                public double getDistance(LatLng p1, LatLng p2) {
                    float[] results = new float[1];
                    Location.distanceBetween(p1.getLatitude(), p1.getLongitude(),
                            p2.getLatitude(), p2.getLongitude(), results);
                    return results[0];
                }

                @Override
                public boolean isPoint(Object p) {
                    return p instanceof LatLng;
                }
            };
        }
        return mFactory;
    }
    
    public AkylasMapboxGLModule() {
        super();
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
    public static void onVerifyModule(TiApplication app)
    {
        verifyPassword(app, "akylas.modules.key", AeSimpleSHA1.hexToString("7265745b496b2466553b486f736b7b4f"));
    }

    
    @Kroll.method
    @Kroll.setProperty
    public void setLicense(String license) {
        Mapbox.getInstance(TiApplication.getAppContext(), license);
    }

    
    public static final int MAP_TYPE_DARK = 5;
    public static final int MAP_TYPE_LIGHT = 6;
    static KrollDict mapTypes = null;
    @Kroll.method
    @Kroll.getProperty
    public KrollDict MapType()
    {
        if (mapTypes == null) {
            mapTypes = new KrollDict();
            mapTypes.put("none", MAP_TYPE_NONE);
            mapTypes.put("normal", MAP_TYPE_NORMAL);
            mapTypes.put("hybrid", MAP_TYPE_HYBRID);
            mapTypes.put("satellite", MAP_TYPE_SATELLITE);
            mapTypes.put("terrain", MAP_TYPE_TERRAIN);
            mapTypes.put("dark", MAP_TYPE_DARK);
            mapTypes.put("light", MAP_TYPE_LIGHT);
        }
        return mapTypes;
    }
    
    private static String  cacheFolder = null;
    private static String  packagesFolder = null;
    public static String getMapCacheFolder() {
        if (cacheFolder == null)
        {        
            File baseDirectory = TiApplication.getAppContext() .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "cache");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create cache folder!");
            } else {
                cacheFolder = folder.getAbsolutePath();
            }
        }
        return cacheFolder;
    }
    
    public static String getMapPackagesFolder() {
        if (packagesFolder == null)
        {        
            File baseDirectory = TiApplication.getAppContext() .getExternalFilesDir(null);
            File folder = new File(baseDirectory, "packages");
            if (!(folder.mkdirs() || folder.isDirectory())) {
                Log.e(TAG, "Could not create cache folder!");
            } else {
                packagesFolder = folder.getAbsolutePath();
            }
        }
        return packagesFolder;
    }
    
//    private static CartoPackageManager offlineManager = null;
//    public static CartoPackageManager getOfflineManager() {
//        if (offlineManager == null) {
//         // Create package manager
//            try {
//                offlineManager = new CartoPackageManager("carto.streets", AkylasMapboxGLModule.getMapPackagesFolder());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return offlineManager;
//    }
//    
//    @Kroll.method
//    public Object[] getOfflineServerPackages() {
//        final CartoPackageManager manager = getOfflineManager();
//        PackageInfoVector vector = manager.getServerPackages();
//        int count = (int)vector.size();
//        Object[] result = new Object[count];
//        PackageInfo info;
//        for(int i = 0; i < count; i++) {
//            info = vector.get(i);
//            HashMap data = new HashMap();
//            data.put("id",info.getPackageId());
//            data.put("type",info.getPackageType());
//            data.put("mask",info.getTileMask());
//            data.put("version",info.getVersion());
//            data.put("metainfo",info.getMetaInfo());
//            data.put("name",info.getName());
//            data.put("size",info.getSize());
//            result[i] = data;
//        }
//        return result;        
//    }
    
//    @Kroll.method
//    public Object[] getOfflineLocalPackages() {
//        final CartoPackageManager manager = getOfflineManager();
//        PackageInfoVector vector = manager.getLocalPackages();
//        int count = (int)vector.size();
//        Object[] result = new Object[count];
//        PackageInfo info;
//        for(int i = 0; i < count; i++) {
//            info = vector.get(i);
//            HashMap data = new HashMap();
//            data.put("id",info.getPackageId());
//            data.put("type",info.getPackageType());
//            data.put("mask",info.getTileMask());
//            data.put("version",info.getVersion());
//            data.put("metainfo",info.getMetaInfo());
//            data.put("name",info.getName());
//            data.put("size",info.getSize());
//            result[i] = data;
//        }
//        return result;        
//    }

}
