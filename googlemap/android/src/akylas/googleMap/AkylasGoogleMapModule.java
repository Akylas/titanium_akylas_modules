package akylas.googleMap;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import akylas.map.common.AkylasMapBaseModule;

@Kroll.module(name = "AkylasGoogleMap", id = "akylas.googleMap", parentModule = AkylasMapBaseModule.class)
public class AkylasGoogleMapModule extends
        AkylasMapBaseModule<LatLng, LatLngBounds> {

    private static final String TAG = "AkylasGoogleMapModule";
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
    private static int googlePlayServicesState;
    private static boolean googlePlayServicesAvailable = false;
    
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
                    return new LatLngBounds(ne, sw);
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
            };
        }
        return mFactory;
    }


    public AkylasGoogleMapModule() {
        super();
        
        googlePlayServicesState = TiApplication.getGooglePlayServicesState();

        googlePlayServicesAvailable = googlePlayServicesState == 0;
        if (!googlePlayServicesAvailable) {
            if (hasListeners(TiC.EVENT_ERROR)) {
                KrollDict data = new KrollDict();

                data.putCodeAndMessage(
                        googlePlayServicesState,
                        AkylasGoogleMapModule
                                .getGoogleServiceStateMessage(googlePlayServicesState));
                fireEvent(TiC.EVENT_ERROR, data);
            }
            Log.e(TAG,
                    "Google Play Services not available: Error "
                            + AkylasGoogleMapModule
                                    .getGoogleServiceStateMessage(googlePlayServicesState));
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("Akylas.GoogleMap.View", ViewProxy.class.getName());
        map.put("Akylas.GoogleMap.Annotation", AnnotationProxy.class.getName());
        map.put("Akylas.GoogleMap.Route", RouteProxy.class.getName());
        map.put("Akylas.GoogleMap.TileSource", TileSourceProxy.class.getName());
        APIMap.addMapping(map);
    }

    static public final String getGoogleServiceStateMessage(final int state) {
        switch (state) {

        case 1:
            return "SERVICE_MISSING";
        case 2:
            return "SERVICE_VERSION_UPDATE_REQUIRED";
        case 3:
            return "SERVICE_DISABLED";
        case 9:
            return "SERVICE_INVALID";
        default:
        case 0:
            return "SUCCESS";
        }
    }

    public static final boolean googlePlayServicesAvailable() {
        return googlePlayServicesAvailable;
    }

}
