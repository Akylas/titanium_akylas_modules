package akylas.mapbox;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.MapboxUtils;

import akylas.map.common.AkylasMapBaseModule;

@Kroll.module(name = "AkylasMapbox", id = "akylas.mapbox", parentModule=AkylasMapBaseModule.class)
public class AkylasMapboxModule extends AkylasMapBaseModule<LatLng, BoundingBox> {
    public static final String PROPERTY_DEBUG = "debug";
    public static final String PROPERTY_DISK_CACHE = "diskCache";
    
    public AkylasMapboxModule() {
        super();
        if (mFactory == null) {
            mFactory = new Factory<LatLng, BoundingBox>() {
                @Override
                public LatLng createPoint(final double latitude, final double longitude, final double altitude) {
                    return new LatLng(latitude, longitude, altitude);
                }
                @Override
                public BoundingBox createRegion(final double north, final double east, final double south, final double west) {
                    return new BoundingBox(north, east, south, west);
                }
                @Override
                public BoundingBox createRegion(final LatLng center, final double latitudeDelta_2, final double longitudeDelta_2) {
                    return new BoundingBox(center.getLatitude() + latitudeDelta_2,
                            center.getLongitude() + longitudeDelta_2,
                            center.getLatitude() - latitudeDelta_2,
                            center.getLongitude() - longitudeDelta_2);
                }
                @Override
                public BoundingBox createRegion(final LatLng ne, final LatLng sw) {
                    return new BoundingBox(ne, sw);
                }
                @Override
                public boolean isRegionValid(BoundingBox region) {
                    return region != null && region.isValid();
                }
                @Override
                public BoundingBox unionRegions(BoundingBox region1,
                        BoundingBox region2) {
                    if (region1 == null) {
                        return region2;
                    }
                    if (region2 == null) {
                        return region1;
                    }
                    return region1.union(region2);
                }
                @Override
                public KrollDict regionToDict(BoundingBox region) {
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
                    return point.getAltitude();
                }
                @Override
                public BoundingBox unionRegion(BoundingBox region1, LatLng point) {
                    if (region1 != null && point != null) {
                        return region1.union(point);
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
                    result.put(TiC.PROPERTY_ALTITUDE, point.getAltitude());
                    return result;
                }
            };
        }
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("Akylas.Mapbox.View", ViewProxy.class.getName());
        map.put("Akylas.Mapbox.Annotation", AnnotationProxy.class.getName());
        map.put("Akylas.Mapbox.Route", RouteProxy.class.getName());
        map.put("Akylas.Mapbox.TileSource", TileSourceProxy.class.getName());
        APIMap.addMapping(map);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setMapboxAccessToken(String token) {
        MapboxUtils.setAccessToken(token);
    }
}
