package akylas.mapbox;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiRHelper.ResourceNotFoundException;
import org.appcelerator.kroll.common.Log;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.tileprovider.MapTile;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;

@Kroll.module(name="AkylasMapbox", id="akylas.mapbox")
public class AkylasMapboxModule extends KrollModule
{

	public static final String PROPERTY_ALTITUDE = "altitude";
	public static final String PROPERTY_LATITUDE = "latitude";
	public static final String PROPERTY_LONGITUDE = "longitude";
	public static final String PROPERTY_SW = "sw";
	public static final String PROPERTY_NE = "ne";
	public static final String PROPERTY_LATITUDE_DELTA = "latitudeDelta";
	public static final String PROPERTY_LONGITUDE_DELTA = "longitudeDelta";
	public static final String PROPERTY_DRAGGABLE = "draggable";
	public static final String PROPERTY_POINTS = "points";
	public static final String PROPERTY_TILE_SOURCE = "tileSource";
	public static final String PROPERTY_MAP = "map";
	public static final String PROPERTY_CENTER_COORDINATE = "centerCoordinate";
	public static final String PROPERTY_NEWSTATE = "newState";
	public static final String PROPERTY_CUSTOM_VIEW = "customView";
	public static final String PROPERTY_PIN = "pin";
	public static final String PROPERTY_LEFT_PANE = "leftPane";
	public static final String PROPERTY_RIGHT_PANE = "rightPane";
	public static final String PROPERTY_TILT = "tilt";
	public static final String PROPERTY_BEARING = "bearing";
	public static final String PROPERTY_ZOOM = "zoom";
	public static final String PROPERTY_MINZOOM = "minZoom";
	public static final String PROPERTY_MAXZOOM = "maxZoom";
	public static final String PROPERTY_SCROLLABLE_AREA_LIMIT = "scrollableAreaLimit";
	public static final String PROPERTY_ZORDER_ON_TOP = "zOrderOnTop";
	public static final String PROPERTY_USER_TRACKING_MODE = "userTrackingMode";
	public static final String EVENT_PIN_CHANGE_DRAG_STATE = "pinchangedragstate";
	public static final String EVENT_ON_SNAPSHOT_READY = "onsnapshotready";
	public static final String EVENT_USER_LOCATION = "userlocation";
	public static final String PROPERTY_DEFAULT_PIN_IMAGE = "defaultPinImage";
	public static final String PROPERTY_DEFAULT_PIN_ANCHOR = "defaultPinAnchor";
	
	@Kroll.constant public static final int ANNOTATION_DRAG_STATE_START = 0;
	@Kroll.constant public static final int ANNOTATION_DRAG_STATE_END = 1;

	@Kroll.constant public static final int SUCCESS = 0;
	@Kroll.constant public static final int SERVICE_MISSING = 1;
	@Kroll.constant public static final int SERVICE_VERSION_UPDATE_REQUIRED = 2;
	@Kroll.constant public static final int SERVICE_DISABLED = 3;
	@Kroll.constant public static final int SERVICE_INVALID = 9;

	// Standard Debugging variables
	private static final String TAG = "AkylasMapboxModule";

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public AkylasMapboxModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}

	public static LatLng latlongFromDict(Object value)
	{
		KrollDict dict = null;
		if (value instanceof KrollDict) {
			dict = (KrollDict)value;
		}
		else if (value instanceof HashMap) {
			dict = new KrollDict((HashMap)value);
		}
		if (dict == null) return null;
		if (dict.containsKey(PROPERTY_LATITUDE) && dict.containsKey(PROPERTY_LONGITUDE))
		{
			return new LatLng(dict.optDouble(PROPERTY_LATITUDE, 0.0), dict.optDouble(PROPERTY_LONGITUDE, 0.0), dict.optDouble(PROPERTY_ALTITUDE, 0.0));
		}
		return null;
	}
	
	public static BoundingBox regionFromDict(Object value)
	{
		KrollDict dict = null;
		if (value instanceof KrollDict) {
			dict = (KrollDict)value;
		}
		else if (value instanceof HashMap) {
			dict = new KrollDict((HashMap)value);
		}
		if (dict == null) return null;
		if (dict.containsKey(PROPERTY_NE) && dict.containsKey(PROPERTY_SW))
		{
			LatLng ne = latlongFromDict(dict.getKrollDict(PROPERTY_NE));
			LatLng sw = latlongFromDict(dict.getKrollDict(PROPERTY_SW));
			return new BoundingBox(ne.getLatitude(), ne.getLongitude(), sw.getLatitude(), sw.getLongitude());
		}
		else
		{
			LatLng center = latlongFromDict(dict);
			if (center != null) {
				float latitudeDelta_2 = dict.optFloat(PROPERTY_LATITUDE_DELTA, 0)/2;
				float longitudeDelta_2 = dict.optFloat(PROPERTY_LONGITUDE_DELTA, 0)/2;
				return new BoundingBox(center.getLatitude() + latitudeDelta_2, 
						center.getLongitude() + longitudeDelta_2, 
						center.getLatitude() - latitudeDelta_2, 
						center.getLongitude() - longitudeDelta_2);
			}
			
		}
		return null;
	}
	
	public static KrollDict regionToDict(BoundingBox box)
	{
		KrollDict result = new KrollDict();
		KrollDict ne = new KrollDict();
		KrollDict sw = new KrollDict();
		ne.put(PROPERTY_LATITUDE, box.getLatNorth());
		ne.put(PROPERTY_LONGITUDE, box.getLonEast());
		sw.put(PROPERTY_LATITUDE, box.getLatSouth());
		sw.put(PROPERTY_LONGITUDE, box.getLonWest());
		result.put(PROPERTY_NE, ne);
		result.put(PROPERTY_SW, sw);
		return result;
	}
	
	public static TileLayer tileSourceFromObject(Object obj) {
		if (obj instanceof TileSourceProxy) {
			return ((TileSourceProxy)obj).getLayer();
		}
		String source = null;
		if (obj instanceof String) {
			source = TiConvert.toString(obj);
		}
		else if (obj instanceof HashMap) {
			source = TiConvert.toString((HashMap)obj, "type");
		}
		if (source == null) return null;
		Log.d(TAG, "tileSourceFromString " + source);
		if (source.toLowerCase().endsWith("mbtiles")) {
			if (source.startsWith("file://")) {
				source = source.substring("file://".length()); // the service doesn't like file://
			}
			return new MBTilesLayer(source);
		}
		else if (source.equalsIgnoreCase("openstreetmap")) {
			return new WebSourceTileLayer("openstreetmap", "http://tile.openstreetmap.org/{z}/{x}/{y}.png")
            .setName("OpenStreetMap")
            .setAttribution("© OpenStreetMap Contributors")
            .setMinimumZoomLevel(1)
            .setMaximumZoomLevel(18);
		} else if (source.equalsIgnoreCase("openseamap")) {
			return new WebSourceTileLayer("openseamap", "") {
				private static final String BASE_URL = "http://tile.openstreetmap.org/{z}/{x}/{y}.png";
			    private static final String BASE_URL_SEA = "http://tiles.openseamap.org/seamark/{z}/{x}/{y}.png";
			    @Override
			    public String[] getTileURLs(final MapTile aTile, boolean hdpi) {
			        return new String[]{this.parseUrlForTile(BASE_URL, aTile, hdpi), this.parseUrlForTile(BASE_URL_SEA, aTile, hdpi)};
			    }
			}
            .setName("Open Sea Map")
            .setAttribution("© OpenStreetMap CC-BY-SA")
            .setMinimumZoomLevel(1)
            .setMaximumZoomLevel(18);
		} else if (source.equalsIgnoreCase("mapquest")) {
    			return new WebSourceTileLayer("mapquest", "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png")
                .setName("MapQuest Open Aerial")
                .setAttribution("Tiles courtesy of MapQuest and OpenStreetMap contributors.")
                .setMinimumZoomLevel(1)
                .setMaximumZoomLevel(18);
		} else if (source.equalsIgnoreCase("tilemill") && obj instanceof HashMap) {
			String host = TiConvert.toString((HashMap)obj, "host");
			String mapName = TiConvert.toString((HashMap)obj, "mapName");
			if (host != null && mapName != null) {
				String cacheKey = TiConvert.toString((HashMap)obj, "cacheKey", mapName);
				String url = String.format("http://%s:20008/tile/%s",host, mapName) + "/{z}/{x}/{y}.png?updated=%d";
				return new WebSourceTileLayer(cacheKey, url) {
				    @Override
				    public String getTileURL(final MapTile aTile, boolean hdpi) {
				        return String.format(getTileURL(aTile, hdpi),  System.currentTimeMillis() / 1000L);
				    }
				}
	            .setName(mapName)
	            .setMinimumZoomLevel(TiConvert.toFloat((HashMap)obj, "minZoom", 1))
	            .setMaximumZoomLevel(TiConvert.toFloat((HashMap)obj, "maxZoom", 18));
			}
		} else {
			return new MapboxTileLayer(source);
		}
		return null;
	}
}

