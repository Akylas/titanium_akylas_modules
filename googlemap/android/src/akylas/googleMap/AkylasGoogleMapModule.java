package akylas.googlemap;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import akylas.map.common.AkylasMapBaseModule;
import android.app.Activity;
import android.location.Location;

@Kroll.module(name = "AkylasGooglemap", id = "akylas.googlemap", parentModule = AkylasMapBaseModule.class)
public class AkylasGooglemapModule extends
        AkylasMapBaseModule<LatLng, LatLngBounds> {

    private static final String TAG = "AkylasGooglemapModule";
    @Kroll.constant
    public static final int SUCCESS = 0;
    @Kroll.constant
    public static final int SERVICE_MISSING = 1;
    @Kroll.constant
    public static final int SERVICE_VERSION_UPDATE_REQUIRED = 2;
    @Kroll.constant
    public static final int SERVICE_DISABLED = 3;
    @Kroll.constant
    public static final int SERVICE_INVALID = 9;

    @Kroll.constant
    public static final float ANNOTATION_AZURE = BitmapDescriptorFactory.HUE_AZURE;
    @Kroll.constant
    public static final float ANNOTATION_BLUE = BitmapDescriptorFactory.HUE_BLUE;
    @Kroll.constant
    public static final float ANNOTATION_CYAN = BitmapDescriptorFactory.HUE_CYAN;
    @Kroll.constant
    public static final float ANNOTATION_GREEN = BitmapDescriptorFactory.HUE_GREEN;
    @Kroll.constant
    public static final float ANNOTATION_MAGENTA = BitmapDescriptorFactory.HUE_MAGENTA;
    @Kroll.constant
    public static final float ANNOTATION_ORANGE = BitmapDescriptorFactory.HUE_ORANGE;
    @Kroll.constant
    public static final float ANNOTATION_RED = BitmapDescriptorFactory.HUE_RED;
    @Kroll.constant
    public static final float ANNOTATION_ROSE = BitmapDescriptorFactory.HUE_ROSE;
    @Kroll.constant
    public static final float ANNOTATION_VIOLET = BitmapDescriptorFactory.HUE_VIOLET;
    @Kroll.constant
    public static final float ANNOTATION_YELLOW = BitmapDescriptorFactory.HUE_YELLOW;

    public static LatLngBounds WORLD_BOUNDING_BOX = (LatLngBounds) getFactory().createRegion(90, 180, -90, -180);
    public static LatLngBounds MIN_BOUNDING_BOX = (LatLngBounds) getFactory().createRegion(-90, -180, -90, -180);
    
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
                        final double east, final double south, final double west) {
                    return new LatLngBounds(new LatLng(south, west),
                            new LatLng(north, east));
                }

                @Override
                public LatLngBounds createRegion(final LatLng center,
                        final double latitudeDelta_2,
                        final double longitudeDelta_2) {
                    return new LatLngBounds(new LatLng(center.latitude
                            - latitudeDelta_2, center.longitude
                            - longitudeDelta_2), new LatLng(center.latitude
                            + latitudeDelta_2, center.longitude
                            + longitudeDelta_2));
                }

                @Override
                public LatLngBounds createRegion(final LatLng ne,
                        final LatLng sw) {
                    try {
                        return new LatLngBounds(sw, ne);
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

                    return createRegion(Math.max(region1.northeast.latitude,
                            region2.northeast.latitude), Math.max(
                            region1.northeast.longitude,
                            region2.northeast.longitude), Math.min(
                            region1.southwest.latitude,
                            region2.southwest.latitude), Math.min(
                            region1.southwest.longitude,
                            region2.southwest.longitude));
                }

                @Override
                public KrollDict regionToDict(LatLngBounds region) {
                    if (region == null)
                        return null;
                    KrollDict result = new KrollDict();
                    KrollDict ne = new KrollDict();
                    KrollDict sw = new KrollDict();
                    ne.put(TiC.PROPERTY_LATITUDE, region.northeast.latitude);
                    ne.put(TiC.PROPERTY_LONGITUDE, region.northeast.longitude);
                    sw.put(TiC.PROPERTY_LATITUDE, region.southwest.latitude);
                    sw.put(TiC.PROPERTY_LONGITUDE, region.southwest.longitude);
                    result.put(PROPERTY_NE, ne);
                    result.put(PROPERTY_SW, sw);
                    return result;
                }

                @Override
                public double getLatitude(LatLng point) {
                    return point.latitude;
                }

                @Override
                public double getLongitude(LatLng point) {
                    return point.longitude;
                }

                @Override
                public double getAltitude(LatLng point) {
                    return 0;
                }

                @Override
                public LatLngBounds unionRegion(LatLngBounds region1,
                        LatLng point) {
                    if (region1 != null && point != null) {
                        return createRegion(Math.max(
                                region1.northeast.latitude, point.latitude),
                                Math.max(region1.northeast.longitude,
                                        point.longitude), Math.min(
                                        region1.southwest.latitude,
                                        point.latitude), Math.min(
                                        region1.southwest.longitude,
                                        point.longitude));
                    }
                    return region1;
                }

                @Override
                public KrollDict latlongToDict(LatLng point) {
                    if (point == null)
                        return null;
                    KrollDict result = new KrollDict();
                    result.put(TiC.PROPERTY_LATITUDE, point.latitude);
                    result.put(TiC.PROPERTY_LONGITUDE, point.longitude);
                    return result;
                }

                @Override
                public double getDistance(LatLng p1, LatLng p2) {
                    float[] results = new float[1];
                    Location.distanceBetween(p1.latitude, p1.longitude,
                            p2.latitude, p2.longitude,
                                             results);
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

    
    public AkylasGooglemapModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("Akylas.Googlemap.View", ViewProxy.class.getName());
        map.put("Akylas.Googlemap.Annotation", AnnotationProxy.class.getName());
        map.put("Akylas.Googlemap.Route", RouteProxy.class.getName());
        map.put("Akylas.Googlemap.TileSource", TileSourceProxy.class.getName());
        map.put("Akylas.Googlemap.Cluster", ClusterProxy.class.getName());
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
        if (!TiApplication.isGooglePlayServicesAvailable()) {
            if (hasListeners(TiC.EVENT_ERROR)) {
                KrollDict data = new KrollDict();
                data.putCodeAndMessage(TiApplication.getGooglePlayServicesState(), TiApplication.getGooglePlayServicesErrorString());
                fireEvent(TiC.EVENT_ERROR, data);
            }
        }
    }
    
    public static String getGooglePlayServicesErrorString(int code) {
        try {
            Class<?> c = Class.forName("com.google.android.gms.common.GooglePlayServicesUtil");
            Method  method = c.getDeclaredMethod ("getErrorString", int.class);
            return (String) method.invoke(null, new Object[] {code});
            
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
