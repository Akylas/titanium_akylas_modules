package akylas.map;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.kroll.common.APIMap;

import akylas.map.TileSourceProxy;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.MapboxUtils;

@Kroll.module(name = "AkylasMap", id = "akylas.map")
public class AkylasMapModule extends KrollModule {

    public enum TrackingMode {
        NONE, FOLLOW, FOLLOW_BEARING
    }

    public static final String PROPERTY_SW = "sw";
    public static final String PROPERTY_NE = "ne";
    public static final String PROPERTY_DRAGGABLE = "draggable";
    public static final String PROPERTY_POINTS = "points";
    public static final String PROPERTY_TILE_SOURCE = "tileSource";
    public static final String PROPERTY_MAP = "map";
    public static final String PROPERTY_CENTER_COORDINATE = "centerCoordinate";
    public static final String PROPERTY_NEWSTATE = "newState";
    public static final String PROPERTY_PIN_VIEW = "pinView";
    public static final String PROPERTY_CUSTOM_VIEW = "customView";
    public static final String PROPERTY_PIN = "pin";
    public static final String PROPERTY_INFO_WINDOW = "infoWindow";
    public static final String PROPERTY_LEFT_PANE = "leftPane";
    public static final String PROPERTY_RIGHT_PANE = "rightPane";
    public static final String PROPERTY_TILT = "tilt";
    public static final String PROPERTY_ZOOM = "zoom";
    public static final String PROPERTY_MINZOOM = "minZoom";
    public static final String PROPERTY_MAXZOOM = "maxZoom";
    public static final String PROPERTY_MAX_ANNOTATIONS = "maxAnnotations";
    public static final String PROPERTY_SCROLLABLE_AREA_LIMIT = "scrollableAreaLimit";
    public static final String PROPERTY_ZORDER_ON_TOP = "zOrderOnTop";
    public static final String PROPERTY_USER_TRACKING_MODE = "userTrackingMode";
    public static final String PROPERTY_LINE_CAP = "lineCap";
    public static final String PROPERTY_LINE_JOIN = "lineJoin";
    public static final String PROPERTY_REGION_FIT = "regionFit";
    public static final String PROPERTY_USER_LOCATION_ENABLED = "userLocationEnabled";
    public static final String PROPERTY_USER_LOCATION_REQUIRED_ZOOM = "userLocationRequiredZoom";
    public static final String EVENT_PIN_CHANGE_DRAG_STATE = "pinchangedragstate";
    public static final String EVENT_ON_SNAPSHOT_READY = "onsnapshotready";
    public static final String EVENT_USER_LOCATION = "userlocation";
    public static final String EVENT_LOCATION_BUTTON = "locationButton";
    public static final String EVENT_FOLLOW_LOCATION = "followUserLocation";

    public static final String PROPERTY_DEFAULT_PIN_IMAGE = "defaultPinImage";
    public static final String PROPERTY_DEFAULT_PIN_ANCHOR = "defaultPinAnchor";
    public static final String PROPERTY_DEFAULT_CALLOUT_ANCHOR = "defaultCalloutAnchor";
    public static final String PROPERTY_SPEED = "speed";
    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final String PROPERTY_ROUTES = "routes";
    public static final String PROPERTY_ANNOTATIONS = "annotations";
    public static final String PROPERTY_ANNOTATION = "annotation";
    public static final String PROPERTY_DEBUG = "debug";
    public static final String PROPERTY_DISK_CACHE = "diskCache";
    public static final String PROPERTY_SHOW_INFO_WINDOW = "showInfoWindow";
    public static final String PROPERTY_ANIMATE_CHANGES = "animateChanges";
    public static final String PROPERTY_ANCHOR = "anchorPoint";
    public static final String PROPERTY_SORT_KEY = "sortKey";

    public static final String PROPERTY_CAN_SHOW_CALLOUT = "canShowCallout";
    public static final String PROPERTY_CALLOUT_BACKGROUND_COLOR = "calloutBackgroundColor";
    public static final String PROPERTY_CALLOUT_BORDER_RADIUS = "calloutBorderRadius";
    public static final String PROPERTY_CALLOUT_PADDING = "calloutPadding";
    public static final String PROPERTY_CALLOUT_ANCHOR = "calloutAnchorPoint";
    public static final String PROPERTY_CALLOUT_ARROW_HEIGHT = "calloutArrowHeight";
    public static final String PROPERTY_CALLOUT_TEMPLATES = "calloutTemplates";
    public static final String PROPERTY_CALLOUT_USE_TEMPLATES = "calloutUseTemplates";
    public static final String PROPERTY_DEFAULT_CALLOUT_TEMPLATE = "defaultCalloutTemplate";
    public static final String PROPERTY_FLAT = "flat";
    public static final String PROPERTY_IMAGE_WITH_SHADOW = "imageWithShadow";

    public static final String PROPERTY_TILT_ENABLED = "tiltEnabled";
    public static final String PROPERTY_ROTATE_ENABLED = "rotateEnabled";
    public static final String PROPERTY_SCROLL_ENABLED = "scrollEnabled";
    // public static final String PROPERTY_ZOOM_ENABLED = "zoomEnabled";

    public static final String PROPERTY_USER_LOCATION_BUTTON = "userLocationButton";
    public static final String PROPERTY_TRAFFIC = "traffic";
    public static final String PROPERTY_COMPASS_ENABLED = "compass";
    public static final String PROPERTY_TOOLBAR_ENABLED = "toolbar";
    public static final String PROPERTY_ZOOM_CONTROLS_ENABLED = "zoomControls";
    public static final String PROPERTY_BUILDINGS_ENABLED = "buildings";
    public static final String PROPERTY_INDOOR_ENABLED = "indoor";
    public static final String PROPERTY_INDOOR_CONTROLS_ENABLED = "buildingsControls";

    @Kroll.constant
    public static final int NORMAL_TYPE = GoogleMap.MAP_TYPE_NORMAL;
    @Kroll.constant
    public static final int TERRAIN_TYPE = GoogleMap.MAP_TYPE_TERRAIN;
    @Kroll.constant
    public static final int SATELLITE_TYPE = GoogleMap.MAP_TYPE_SATELLITE;
    @Kroll.constant
    public static final int HYBRID_TYPE = GoogleMap.MAP_TYPE_HYBRID;

    @Kroll.constant
    public static final int ANNOTATION_DRAG_STATE_START = 1;
    @Kroll.constant
    public static final int ANNOTATION_DRAG_STATE_DRAGGING = 2;
    @Kroll.constant
    public static final int ANNOTATION_DRAG_STATE_CANCEL = 3;
    @Kroll.constant
    public static final int ANNOTATION_DRAG_STATE_END = 4;

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

    // Standard Debugging variables
    private static final String TAG = "AkylasMapModule";

    // You can define constants with @Kroll.constant, for example:
    // @Kroll.constant public static final String EXTERNAL_NAME = value;

    public AkylasMapModule() {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        HashMap<String, String> map = new HashMap();
        map.put("AkylasMap.MapboxView",
                akylas.map.MapboxViewProxy.class.getName());
        map.put("AkylasMap.MapView", akylas.map.MapViewProxy.class.getName());
        map.put("AkylasMap.Annotation",
                akylas.map.AnnotationProxy.class.getName());
        map.put("AkylasMap.Route", akylas.map.RouteProxy.class.getName());
        map.put("AkylasMap.TileSource",
                akylas.map.TileSourceProxy.class.getName());
        APIMap.addMapping(map);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static LatLng latlongFromObject(Object value) {
        if (value instanceof KrollDict) {
            return latlongFromDict((KrollDict) value);
        } else if (value instanceof HashMap) {
            return latlongFromDict(new KrollDict((HashMap) value));
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            int count = array.length;
            if (count < 2)
                return null;
            double altitude = (count > 2) ? TiConvert.toDouble(array[2]) : 0;
            return new LatLng(TiConvert.toDouble(array[0]),
                    TiConvert.toDouble(array[1]), altitude);
        }
        return null;
    }

    public static LatLng latlongFromDict(KrollDict dict) {
        if (dict == null)
            return null;
        if (dict.containsKey(TiC.PROPERTY_LATITUDE)
                && dict.containsKey(TiC.PROPERTY_LONGITUDE)) {
            return new LatLng(dict.optDouble(TiC.PROPERTY_LATITUDE, 0.0),
                    dict.optDouble(TiC.PROPERTY_LONGITUDE, 0.0),
                    dict.optDouble(TiC.PROPERTY_ALTITUDE, 0.0));
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static BoundingBox regionFromDict(Object value) {
        KrollDict dict = null;
        if (value instanceof KrollDict) {
            dict = (KrollDict) value;
        } else if (value instanceof HashMap) {
            dict = new KrollDict((HashMap) value);
        }
        if (dict == null)
            return null;
        if (dict.containsKey(PROPERTY_NE) && dict.containsKey(PROPERTY_SW)) {
            LatLng ne = latlongFromObject(dict.get(PROPERTY_NE));
            LatLng sw = latlongFromObject(dict.get(PROPERTY_SW));
            return new BoundingBox(ne.getLatitude(), ne.getLongitude(),
                    sw.getLatitude(), sw.getLongitude());
        } else {
            LatLng center = latlongFromDict(dict);
            if (center != null) {
                float latitudeDelta_2 = dict.optFloat(
                        TiC.PROPERTY_LATITUDE_DELTA, 0) / 2;
                float longitudeDelta_2 = dict.optFloat(
                        TiC.PROPERTY_LONGITUDE_DELTA, 0) / 2;
                return new BoundingBox(center.getLatitude() + latitudeDelta_2,
                        center.getLongitude() + longitudeDelta_2,
                        center.getLatitude() - latitudeDelta_2,
                        center.getLongitude() - longitudeDelta_2);
            }

        }
        return null;
    }

    public static KrollDict latlongToDict(LatLng point) {
        if (point == null)
            return null;
        KrollDict result = new KrollDict();
        result.put(TiC.PROPERTY_LATITUDE, point.getLatitude());
        result.put(TiC.PROPERTY_LONGITUDE, point.getLongitude());
        result.put(TiC.PROPERTY_ALTITUDE, point.getAltitude());
        return result;
    }

    public static KrollDict locationToDict(Location location) {
        if (location == null)
            return null;
        KrollDict result = new KrollDict();
        result.put(TiC.PROPERTY_LATITUDE, location.getLatitude());
        result.put(TiC.PROPERTY_LONGITUDE, location.getLongitude());
        result.put(TiC.PROPERTY_ALTITUDE, location.getAltitude());
        result.put(TiC.PROPERTY_BEARING, location.getBearing());
        result.put(TiC.PROPERTY_SPEED, location.getSpeed());
        result.put(TiC.PROPERTY_TIMESTAMP, location.getTime());
        return result;
    }

    public static KrollDict regionToDict(BoundingBox box) {
        if (box == null)
            return null;
        KrollDict result = new KrollDict();
        KrollDict ne = new KrollDict();
        KrollDict sw = new KrollDict();
        ne.put(TiC.PROPERTY_LATITUDE, box.getLatNorth());
        ne.put(TiC.PROPERTY_LONGITUDE, box.getLonEast());
        sw.put(TiC.PROPERTY_LATITUDE, box.getLatSouth());
        sw.put(TiC.PROPERTY_LONGITUDE, box.getLonWest());
        result.put(PROPERTY_NE, ne);
        result.put(PROPERTY_SW, sw);
        return result;
    }

    public static KrollDict regionToDict(LatLngBounds box) {
        if (box == null)
            return null;
        KrollDict result = new KrollDict();
        KrollDict ne = new KrollDict();
        KrollDict sw = new KrollDict();
        ne.put(TiC.PROPERTY_LATITUDE, box.northeast.latitude);
        ne.put(TiC.PROPERTY_LONGITUDE, box.northeast.longitude);
        sw.put(TiC.PROPERTY_LATITUDE, box.southwest.latitude);
        sw.put(TiC.PROPERTY_LONGITUDE, box.southwest.longitude);
        result.put(PROPERTY_NE, ne);
        result.put(PROPERTY_SW, sw);
        return result;
    }

    static public com.mapbox.mapboxsdk.geometry.LatLng googleToMapbox(
            final com.google.android.gms.maps.model.LatLng point) {
        return new com.mapbox.mapboxsdk.geometry.LatLng(point.latitude,
                point.longitude);
    }

    static public com.google.android.gms.maps.model.LatLng mapBoxToGoogle(
            final com.mapbox.mapboxsdk.geometry.LatLng point) {
        return new com.google.android.gms.maps.model.LatLng(
                point.getLatitude(), point.getLongitude());
    }

    static public LatLngBounds mapBoxToGoogle(final BoundingBox region) {
        if (region == null)
            return null;
        return new LatLngBounds(new com.google.android.gms.maps.model.LatLng(
                region.getLatSouth(), region.getLonWest()),
                new com.google.android.gms.maps.model.LatLng(region
                        .getLatNorth(), region.getLonEast()));
    }

    static public LatLngBounds moveRegion(final LatLngBounds region,
            com.google.android.gms.maps.model.LatLng center) {
        double latitudeDelta_2 = (region.northeast.latitude - region.southwest.latitude) / 2;
        double longitudeDelta_2 = (region.northeast.longitude - region.southwest.longitude) / 2;

        return new LatLngBounds(new com.google.android.gms.maps.model.LatLng(
                center.latitude - latitudeDelta_2, center.longitude
                        - longitudeDelta_2),
                new com.google.android.gms.maps.model.LatLng(center.latitude
                        + latitudeDelta_2, center.longitude + longitudeDelta_2));
    }

    public static TileSourceProxy tileSourceProxyFromObject(Object obj) {
        if (obj instanceof TileSourceProxy) {
            return ((TileSourceProxy) obj);
        }
        if (obj instanceof HashMap) {
            return (TileSourceProxy) KrollProxy.createProxy(
                    TileSourceProxy.class, null, new Object[] { obj }, null);
        } else if (obj instanceof String) {
            KrollDict dict = new KrollDict();
            dict.put(TiC.PROPERTY_SOURCE, TiConvert.toString(obj));
            return (TileSourceProxy) KrollProxy.createProxy(
                    TileSourceProxy.class, null, new Object[] { dict }, null);
        }
        return null;
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
    
    @Kroll.method
    @Kroll.setProperty
    public void setMapboxAccessToken(String token)
    {
        MapboxUtils.setAccessToken(token);
    }
}
