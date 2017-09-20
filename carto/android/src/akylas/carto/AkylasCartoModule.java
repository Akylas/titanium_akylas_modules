package akylas.carto;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.carto.core.MapBounds;
import com.carto.core.MapPos;
import com.carto.layers.CartoBaseMapStyle;
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
                    return new MapPos(latitude, longitude, altitude);
                }

                @Override
                public MapBounds createRegion(final double north,
                        final double east, final double south, final double west) {
                    return new MapBounds(new MapPos(south, west),
                            new MapPos(north, east));
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

                    return createRegion(Math.max(region1.getMax().getX(),
                            region2.getMax().getX()), Math.max(
                            region1.getMax().getY(),
                            region2.getMax().getY()), Math.min(
                            region1.getMin().getX(),
                            region2.getMin().getX()), Math.min(
                            region1.getMin().getY(),
                            region2.getMin().getY()));
                }

                @Override
                public KrollDict regionToDict(MapBounds region) {
                    if (region == null)
                        return null;
                    KrollDict result = new KrollDict();
                    KrollDict ne = new KrollDict();
                    KrollDict sw = new KrollDict();
                    ne.put(TiC.PROPERTY_LATITUDE, region.getMax().getX());
                    ne.put(TiC.PROPERTY_LONGITUDE, region.getMax().getY());
                    sw.put(TiC.PROPERTY_LATITUDE, region.getMin().getX());
                    sw.put(TiC.PROPERTY_LONGITUDE, region.getMin().getY());
                    result.put(PROPERTY_NE, ne);
                    result.put(PROPERTY_SW, sw);
                    return result;
                }

                @Override
                public double getLatitude(MapPos point) {
                    return point.getX();
                }

                @Override
                public double getLongitude(MapPos point) {
                    return point.getY();
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
                                region1.getMax().getX(), point.getX()),
                                Math.max(region1.getMax().getY(),
                                        point.getY()), Math.min(
                                        region1.getMin().getX(),
                                        point.getX()), Math.min(
                                        region1.getMin().getY(),
                                        point.getY()));
                    }
                    return region1;
                }

                @Override
                public KrollDict latlongToDict(MapPos point) {
                    if (point == null)
                        return null;
                    KrollDict result = new KrollDict();
                    result.put(TiC.PROPERTY_LATITUDE, point.getX());
                    result.put(TiC.PROPERTY_LONGITUDE, point.getY());
                    return result;
                }

                @Override
                public double getDistance(MapPos p1, MapPos p2) {
                    float[] results = new float[1];
                    Location.distanceBetween(p1.getX(), p1.getY(),
                            p2.getX(), p2.getY(),
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

}
