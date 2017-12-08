package akylas.carto;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.layers.CartoBaseMapStyle;
import com.carto.packagemanager.CartoPackageManager;
import com.carto.packagemanager.PackageInfo;
import com.carto.packagemanager.PackageInfoVector;
import com.carto.ui.MapView;

import akylas.map.common.AkylasMapBaseModule;
import android.app.Activity;
import android.location.Location;

@Kroll.module(name = "AkylasCart", id = "akylas.carto", parentModule = AkylasMapBaseModule.class)
public class AkylasCartoModule extends
        AkylasMapBaseModule<MapPos, MapBounds> {

    
    
    static {
        System.loadLibrary("carto_mobile_sdk");
    }
    private static final String TAG = "AkylasCartoModule";

    public static MapBounds WORLD_BOUNDING_BOX = (MapBounds) getFactory().createRegion(90, 180, -90, -180);
    public static MapBounds MIN_BOUNDING_BOX = (MapBounds) getFactory().createRegion(-90, -180, -90, -180);
    
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
                        final double east, final double south, final double west) {
                    return new MapBounds(new MapPos(west, south),
                            new MapPos(east, north));
                }

                @Override
                public MapBounds createRegion(final MapPos center,
                        final double latitudeDelta_2,
                        final double longitudeDelta_2) {
                    return new MapBounds(new MapPos(center.getX()
                            - latitudeDelta_2, center.getY()
                            - longitudeDelta_2), new MapPos(center.getX()
                            + latitudeDelta_2, center.getY()
                            + longitudeDelta_2));
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

                    return createRegion(Math.max(region1.getMax().getY(),
                            region2.getMax().getY()), Math.max(
                            region1.getMax().getX(),
                            region2.getMax().getX()), Math.min(
                            region1.getMin().getY(),
                            region2.getMin().getY()), Math.min(
                            region1.getMin().getX(),
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
                public MapBounds unionRegion(MapBounds region1,
                        MapPos point) {
                    if (region1 != null && point != null) {
                        return createRegion(Math.max(
                                region1.getMax().getY(), point.getY()),
                                Math.max(region1.getMax().getX(),
                                        point.getX()), Math.min(
                                        region1.getMin().getX(),
                                        point.getX()), Math.min(
                                        region1.getMin().getX(),
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
                public double getDistance(MapPos p1, MapPos p2) {
                    float[] results = new float[1];
                    Location.distanceBetween(p1.getY(), p1.getX(),
                            p2.getY(), p2.getX(),
                                             results);
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
    @Override
    protected void initActivity(Activity activity) {
        super.initActivity(activity);
//        if (!TiApplication.isGooglePlayServicesAvailable()) {
//            if (hasListeners(TiC.EVENT_ERROR)) {
//                KrollDict data = new KrollDict();
//                data.putCodeAndMessage(TiApplication.getGooglePlayServicesState(), TiApplication.getGooglePlayServicesErrorString());
//                fireEvent(TiC.EVENT_ERROR, data);
//            }
//        }
    }
    
    @Kroll.method
    @Kroll.setProperty
    public void setLicense(String license) {
        MapView.registerLicense(license, TiApplication.getAppContext());
    }
    
//    public static String getGooglePlayServicesErrorString(int code) {
//        try {
//            Class<?> c = Class.forName("com.google.android.gms.common.GooglePlayServicesUtil");
//            Method  method = c.getDeclaredMethod ("getErrorString", int.class);
//            return (String) method.invoke(null, new Object[] {code});
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
    
    public static final int MAP_TYPE_VOYAGER = 0;
    public static final int MAP_TYPE_POSITRON = 1;
    public static final int MAP_TYPE_DARKMATTER = 2;
    static KrollDict mapTypes = null;
    @Kroll.method
    @Kroll.getProperty
    public KrollDict MapType()
    {
        if (mapTypes == null) {
            mapTypes = new KrollDict();
            mapTypes.put("none", MAP_TYPE_NONE);
            mapTypes.put("voyager", MAP_TYPE_VOYAGER);
            mapTypes.put("positron", MAP_TYPE_POSITRON);
            mapTypes.put("darkmatter",MAP_TYPE_DARKMATTER);
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
    
    private static CartoPackageManager offlineManager = null;
    public static CartoPackageManager getOfflineManager() {
        if (offlineManager == null) {
         // Create package manager
            try {
                offlineManager = new CartoPackageManager("carto.streets", AkylasCartoModule.getMapPackagesFolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return offlineManager;
    }
    
    @Kroll.method
    public Object[] getOfflineServerPackages() {
        final CartoPackageManager manager = getOfflineManager();
        PackageInfoVector vector = manager.getServerPackages();
        int count = (int)vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for(int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id",info.getPackageId());
            data.put("type",info.getPackageType());
            data.put("mask",info.getTileMask());
            data.put("version",info.getVersion());
            data.put("metainfo",info.getMetaInfo());
            data.put("name",info.getName());
            data.put("size",info.getSize());
            result[i] = data;
        }
        return result;        
    }
    
    @Kroll.method
    public Object[] getOfflineLocalPackages() {
        final CartoPackageManager manager = getOfflineManager();
        PackageInfoVector vector = manager.getLocalPackages();
        int count = (int)vector.size();
        Object[] result = new Object[count];
        PackageInfo info;
        for(int i = 0; i < count; i++) {
            info = vector.get(i);
            HashMap data = new HashMap();
            data.put("id",info.getPackageId());
            data.put("type",info.getPackageType());
            data.put("mask",info.getTileMask());
            data.put("version",info.getVersion());
            data.put("metainfo",info.getMetaInfo());
            data.put("name",info.getName());
            data.put("size",info.getSize());
            result[i] = data;
        }
        return result;        
    }

}
