package akylas.googleMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiImageHelper;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.squareup.okhttp.OkHttpClient;
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

    protected float mMinimumZoomLevel = 0;
    protected float mMaximumZoomLevel = 22;
    protected LatLngBounds mBoundingBox = AkylasGoogleMapModule.WORLD_BOUNDING_BOX;
    protected LatLng mCenter = new LatLng(0, 0);
    private final static int mTileSizePixels = 256;

    protected boolean mEnableSSL = false;
    protected boolean mHdpi = false;
    
    private float mScale = 1.0f;
    Picasso picasso;
    
    public WebTileProvider(final String pId, final String url, final boolean enableSSL) {
        this(pId, url, enableSSL, true);
    }
    public WebTileProvider(final String pId, final String url, final boolean enableSSL, final boolean shouldInit) {
        mEnableSSL = enableSSL;
        mHdpi = TiApplication.getAppDensity() >= 2;
        
//        mScale = Math.round(TiApplication.getAppDensity() + .3f);
        mId = pId;
        if (shouldInit) {
            initialize(pId, url, enableSSL);
        }
    }
    
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        setURL(aUrl);
        final Context context = TiApplication.getAppContext();
        OkHttpClient client = TiApplication.getPicassoHttpClientInstance().clone();;
        client.interceptors().add(new com.squareup.okhttp.Interceptor() {
            @Override public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                com.squareup.okhttp.Request.Builder builder = chain.request().newBuilder();
                builder.addHeader("User-Agent", mUserAgent);
                return chain.proceed(builder.build());
            }
          });
        picasso = new Picasso.Builder(context).downloader(new OkHttpDownloader(client)).build();
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
    
    
    private static OkHttpClient _httpClient = null;
    public static OkHttpClient getOkHttpClient() {
        if (_httpClient == null) {
            _httpClient = new OkHttpClient();
        }
        return _httpClient;
    }

    public static com.squareup.okhttp.Request getHttpRequest(final String url) {
        return new com.squareup.okhttp.Request.Builder()
            .url(url)
            .build();
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
        Bitmap bitmap = null;
        try {
            final String url = getTileUrl(x, y, z);
            bitmap = picasso.load(url).get();
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

    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    public LatLng getCenterCoordinate() {
        return mCenter;
    }

    public float getCenterZoom() {
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