package akylas.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiImageHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.constants.TileLayerConstants;
import com.mapbox.mapboxsdk.util.AppUtils;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class WebTileProvider implements TileProvider {

    private static final String TAG = "WebTileProvider";

    protected String mUrl = null;
    protected String mId = null;
    protected String mName;
    protected String mDescription;
    protected String mAttribution;
    protected String mLegend;
    protected String mUserAgent = null;

    protected float mMinimumZoomLevel = TileLayerConstants.MINIMUM_ZOOMLEVEL;
    protected float mMaximumZoomLevel = TileLayerConstants.MAXIMUM_ZOOMLEVEL;
    protected BoundingBox mBoundingBox = TileLayerConstants.WORLD_BOUNDING_BOX;
    protected LatLng mCenter = new LatLng(0, 0);
    private final static int mTileSizePixels = 256;

    protected boolean mEnableSSL = false;
    protected boolean mHdpi = false;
    
    private float mScale = 1.0f;
    
    private final Picasso picasso;
    
    
    public WebTileProvider(final String pId, final String url,
            final boolean enableSSL) {
        mEnableSSL = enableSSL;
        mHdpi = AppUtils.isRunningOn2xOrGreaterScreen(TiApplication.getAppContext());
        
//        mScale = Math.round(TiApplication.getAppDensity() + .3f);
        mId = pId;
        setURL(url);
        final Context context = TiApplication.getAppContext();
        picasso = new Picasso.Builder(context).downloader(
            new OkHttpDownloader(context) {
                @Override
                protected HttpURLConnection openConnection(
                        Uri uri) throws IOException {
                    HttpURLConnection connection = super
                            .openConnection(uri);
                    if (mUserAgent != null) {
                        connection.addRequestProperty("User-Agent", mUserAgent);
                    }
                    
                    return connection;
                }
            }).build();
    }
    public WebTileProvider(final String pId, final String url) {
        this(pId, url, false);
    }
 
    @Override
    public Tile getTile(int x, int y, int z) {
        byte[] tileImage = getTileImage(x, y, z);
        if (tileImage != null) {
            return new Tile(mTileSizePixels, mTileSizePixels, tileImage);
        }
        return NO_TILE;
    }
 
    /**
     * Synchronously loads the requested Tile image either from cache or from the web.</p>
     * Background threading/pooling is done by the google maps api so we can do it all synchronously.
     *
     * @param x x coordinate of the tile
     * @param y y coordinate of the tile
     * @param z the zoom level
     * @return byte data of the image or <i>null</i> if the image could not be loaded.
     */
    private byte[] getTileImage(int x, int y, int z) {
        Bitmap bitmap;
        try {
            bitmap = picasso.load(getTileUrl(x, y, z)).get();
            if (mScale != 1) {
                bitmap = TiImageHelper.imageScaled(bitmap, mScale);
            }
        } catch (IOException e) {
            bitmap = null;
        }
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
 
    /**
     * Return the url to your tiles. For example:
     * <pre>
public String getTileUrl(int x, int y, int z) {
     return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png",x,y,z);
}
     </pre>
     * See <a href="http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames</a> for more details
     *
     * @param x x coordinate of the tile
     * @param y y coordinate of the tile
     * @param z the zoom level
     * @return the url to the tile specified by the parameters
     */

    public String getTileUrl(int x, int y, int zoom) {
        if (mUrl == null) {
            return null;
        }
        return mUrl.replace("{z}", Integer.toString(zoom))
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{2x}", mHdpi ? "@2x" : "");
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
    
    public WebTileProvider setHdpi(final boolean hdpi) {
        this.mHdpi = hdpi;
        return this;
    }
    
    public WebTileProvider setUserAgent(final String agent) {
        this.mUserAgent = agent;
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
