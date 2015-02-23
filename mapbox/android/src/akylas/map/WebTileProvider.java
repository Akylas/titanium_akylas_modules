package akylas.map;

import java.net.MalformedURLException;
import java.net.URL;

import org.appcelerator.titanium.TiApplication;

import com.google.android.gms.maps.model.UrlTileProvider;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.util.AppUtils;

public class WebTileProvider extends UrlTileProvider {

    private static final String TAG = "WebTileProvider";

    protected String mUrl = null;
    protected String mId = null;
    protected String mName;
    protected String mDescription;
    protected String mAttribution;
    protected String mLegend;

    protected float mMinimumZoomLevel = TileLayerConstants.MINIMUM_ZOOMLEVEL;
    protected float mMaximumZoomLevel = TileLayerConstants.MAXIMUM_ZOOMLEVEL;
    protected BoundingBox mBoundingBox = TileLayerConstants.WORLD_BOUNDING_BOX;
    protected LatLng mCenter = new LatLng(0, 0);
    private final static int mTileSizePixels = 256;

    protected boolean mEnableSSL = false;
    protected boolean mHdpi = false;

    public WebTileProvider(final String pId, final String url,
            final boolean enableSSL) {
        super(mTileSizePixels, mTileSizePixels);
        mEnableSSL = enableSSL;
        mHdpi = AppUtils.isRunningOn2xOrGreaterScreen(TiApplication.getAppContext());
        mId = pId;
        setURL(url);
    }
    public WebTileProvider(final String pId, final String url) {
        this(pId, url, false);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        if (mUrl == null) {
            return null;
        }
        try {
            return new URL(mUrl.replace("{z}", Integer.toString(zoom))
                    .replace("{x}", Integer.toString(x))
                    .replace("{y}", Integer.toString(y))
                    .replace("{2x}", mHdpi ? "@2x" : ""));
        } catch (MalformedURLException e) {
            return null;
        }
    }
    
    public void setURL(final String aUrl) {
        if (!mEnableSSL) {
            mUrl = aUrl.replace("https://", "http://");
        } else {
            mUrl = aUrl;
        }
    }
    
    /**
     * Sets the layer's attribution string.
     */
    public WebTileProvider setAttribution(final String aAttribution) {
        this.mAttribution = aAttribution;
        return this;
    }

    /**
     * Sets the layer's description string.
     */
    public WebTileProvider setDescription(final String aDescription) {
        this.mDescription = aDescription;
        return this;
    }

    /**
     * Sets the layer's name.
     */
    public WebTileProvider setName(final String aName) {
        this.mName = aName;
        return this;
    }

    /**
     * Sets the layer's minimum zoom level.
     */
    public WebTileProvider setMinimumZoomLevel(final float aMinimumZoomLevel) {
        this.mMinimumZoomLevel = aMinimumZoomLevel;
        return this;
    }

    /**
     * Sets the layer's minimum zoom level.
     */
    public WebTileProvider setMaximumZoomLevel(final float aMaximumZoomLevel) {
        this.mMaximumZoomLevel = aMaximumZoomLevel;
        return this;
    }
    
    public float getMinimumZoomLevel() {
        return mMinimumZoomLevel;
    }

    public float getMaximumZoomLevel() {
        return mMaximumZoomLevel;
    }

    public int getTileSizePixels() {
        return mTileSizePixels;
    }

    public BoundingBox getBoundingBox() {
        return mBoundingBox;
    }

    public LatLng getCenterCoordinate() {
        return mCenter;
    }

    public float getCenterZoom() {
        if (mCenter != null) {
            return (float) mCenter.getAltitude();
        }
        return Math.round(mMaximumZoomLevel + mMinimumZoomLevel) / 2;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getAttribution() {
        return mAttribution;
    }

    public String getLegend() {
        return mLegend;
    }
}
