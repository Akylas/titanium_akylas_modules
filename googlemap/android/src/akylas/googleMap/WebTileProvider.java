package akylas.googlemap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class WebTileProvider implements TileProvider {

    public static final String TAG = "WebTileProvider";
    
    private final static double WMS_MAP_SIZE_2 = 20037508.342789244;
    private final static double EARTH_RADIUS = 6378137.0;
    private static final Pattern sSRSPattern = Pattern.compile("(?:SRS=)(.*?)(?=&|$)");

    protected String mUrl = null;
    protected String mId = null;
    protected String mName;
    protected String mDescription;
    protected String mAttribution;
    protected String mLegend;
    protected String mUserAgent = null;
    protected String mSubdomains = "abc";

    protected float mMinimumZoomLevel = -1.0f;
    protected float mMaximumZoomLevel = -1.0f;
    protected LatLngBounds mBoundingBox = AkylasGooglemapModule.WORLD_BOUNDING_BOX;
    protected LatLng mCenter = new LatLng(0, 0);
    protected int mTileSizePixels;
    
    private String mSRS;
    double[] mBboxReuse = null;

    protected boolean mEnableSSL = false;
    protected float mDpi = 1.0f;

    private boolean mVisible = true;
    private float mOpacity = 1.0f;
    protected boolean mShowTileAfterMaxZoom = true;
    protected boolean mShowTileBeforeMaxZoom = false;
    
    protected boolean wmsFormat = false;
    protected boolean mCacheable = true;
    
    Picasso picasso;
    Cache diskCache;
    // private Paint tilePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private boolean mAutoHD = false;
    
    protected static boolean isNetworkAvailable() {
        ConnectivityManager cm =
            (ConnectivityManager) TiApplication.getAppSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // private Paint mMergePaint;

    public WebTileProvider(final String pId, final String url,
            final boolean enableSSL) {
        this(pId, url, enableSSL, true);
    }

    public WebTileProvider(final String pId, final String url,
            final boolean enableSSL, final boolean shouldInit) {
        this(pId, url, enableSSL, shouldInit, 256);
    }

    public WebTileProvider(final String pId, final String url,
            final boolean enableSSL, final boolean shouldInit,
            final int tileSize) {
        mEnableSSL = enableSSL;
        mDpi = TiApplication.getAppDensity();
        mTileSizePixels = tileSize;
        mId = pId;
        if (shouldInit) {
            initialize(pId, url, enableSSL);
        }
    }

    public WebTileProvider(final String pId, final String url) {
        this(pId, url, false);
    }

    public WebTileProvider(final String pId, final String url,
            final int tileSize) {
        this(pId, url, false, true, tileSize);
    }

    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        setURL(aUrl);
        final Context context = TiApplication.getAppContext();
//        OkHttpClient client = TiApplication.getPicassoHttpClientInstance()
//                .clone();
        OkHttpClient client = new OkHttpClient();
        diskCache = TiApplication.getDiskCache("akylas.gmap.tiles");
        client.setCache(diskCache);
        client.networkInterceptors().add(new Interceptor() {
            @Override public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                if (mCacheable) {
                    if (isNetworkAvailable()) {
                        int maxAge = 60 * 3600; // read from cache for 1 day
                        return originalResponse.newBuilder()
                                .header("Cache-Control", "private, max-age=" + maxAge)
                                .build();
                    } else {
                        int maxStale = 60 * 60 * 24; // tolerate 4-weeks stale
                        return originalResponse.newBuilder()
                                .header("Cache-Control", "private, only-if-cached, max-stale=" + maxStale)
                                .build();
                    }
                } else {
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "no-cache")
                            .build();
                }
            }
        });
        client.interceptors().add(new com.squareup.okhttp.Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain)
                    throws IOException {
                com.squareup.okhttp.Request.Builder builder = chain.request()
                        .newBuilder();
                if (mUserAgent != null) {
                    builder.addHeader("User-Agent", mUserAgent);
                }
                return chain.proceed(builder.build());
            }
        });
        picasso = new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(client)).build();
    }

    public Bitmap getTileFromNextZoomLevel(final int x, final int y,
            final int z) {
        final Bitmap[] tiles = new Bitmap[4];

        Thread t1 = new Thread() {

            @Override
            public void run() {
                tiles[0] = getTileImage(x * 2, y * 2, z + 1);
            }
        };
        t1.start();

        Thread t2 = new Thread() {

            @Override
            public void run() {
                tiles[1] = getTileImage(x * 2 + 1, y * 2, z + 1);
            }
        };
        t2.start();

        Thread t3 = new Thread() {

            @Override
            public void run() {
                tiles[2] = getTileImage(x * 2, y * 2 + 1, z + 1);
            }
        };
        t3.start();

        Thread t4 = new Thread() {

            @Override
            public void run() {
                tiles[3] = getTileImage(x * 2 + 1, y * 2 + 1, z + 1);
            }
        };
        t4.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mergeBitmaps(tiles);

    }

    @Override
    public Tile getTile(int x, int y, int z) {
        boolean needsHd = mTileSizePixels / mDpi <= 128 && mAutoHD;
        int hdZ = (needsHd ? z + 1 : z);
        boolean passedMin =mMinimumZoomLevel >= 0 && hdZ < mMinimumZoomLevel;
                
        if (passedMin && !mShowTileBeforeMaxZoom) {
            return NO_TILE;
        }

        boolean passedMax = mMaximumZoomLevel >= 0
                && hdZ > mMaximumZoomLevel;
        if (passedMax && !mShowTileAfterMaxZoom) {
            return NO_TILE;
        }

        if (mVisible == false || mOpacity == 0.0f) {
            return null;
        }
        Bitmap tileImage = null;
        boolean needsCrop = false;
        double deltaCropX = 0.0f;
        double deltaCropY = 0.0f;
        double cropSize = 0.0f;
        if (passedMin) {
//            needsCrop = true;
//            int minZoom = (int) (needsHd ? (mMinimumZoomLevel - 1)
//                    : mMinimumZoomLevel);
//            float currentTileDepth = mMinimumZoomLevel - z;
//            double nextx = x / Math.pow(2.0, currentTileDepth);
//            double nexty = y / Math.pow(2.0, currentTileDepth);
//            x = (int) Math.floor(nextx);
//            y = (int) Math.floor(nexty);
//            z = maxZoom;
//            cropSize = 1.0f / Math.pow(2.0, currentTileDepth);
//            deltaCropX = nextx - x;
//            deltaCropY = nexty - y;
        } else if (passedMax) {
            needsCrop = true;
            int maxZoom = (int) (needsHd ? (mMaximumZoomLevel - 1)
                    : mMaximumZoomLevel);
            float currentTileDepth = z - maxZoom;
            double nextx = x / Math.pow(2.0, currentTileDepth);
            double nexty = y / Math.pow(2.0, currentTileDepth);
            x = (int) Math.floor(nextx);
            y = (int) Math.floor(nexty);
            z = maxZoom;
            cropSize = 1.0f / Math.pow(2.0, currentTileDepth);
            deltaCropX = nextx - x;
            deltaCropY = nexty - y;
        }
        if (needsHd) {
            tileImage = getTileFromNextZoomLevel(x, y, z);
        } else {
            tileImage = getTileImage(x, y, z);
        }
        if (tileImage != null) {
            if (needsCrop) {
                Bitmap oldBitmap = tileImage;
                final int width = tileImage.getWidth();
                final int height = tileImage.getHeight();
                tileImage = Bitmap.createBitmap(tileImage,
                        (int) (width * deltaCropX), (int) (height * deltaCropY),
                        (int) (width * cropSize), (int) (height * cropSize));
                if (oldBitmap != tileImage) {
                    oldBitmap.recycle();
                }
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            tileImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return new Tile(mTileSizePixels, mTileSizePixels,
                    stream.toByteArray());
        }
        return getTileForNoImage();
    }
    
    protected Tile getTileForNoImage () {
        return null;
    }

    private static OkHttpClient _httpClient = null;

    public static OkHttpClient getOkHttpClient() {
        if (_httpClient == null) {
            _httpClient = new OkHttpClient();
        }
        return _httpClient;
    }

    public static com.squareup.okhttp.Request getHttpRequest(final String url) {
        return new com.squareup.okhttp.Request.Builder().url(url).build();
    }

    /**
     * Synchronously loads the requested Tile image either from cache or from
     * the web.
     * </p>
     * Background threading/pooling is done by the google maps api so we can do
     * it all synchronously.
     *
     * @param x
     *            x coordinate of the tile
     * @param y
     *            y coordinate of the tile
     * @param z
     *            the zoom level
     * @return byte data of the image or <i>null</i> if the image could not be
     *         loaded.
     */
    protected Bitmap getTileImage(int x, int y, int z) {
        Bitmap bitmap = null;
        try {
            final String url = getTileUrl(x, y, z);
            bitmap = picasso.load(url).get();
        } catch (Exception e) {
            bitmap = null;
        }
        if (bitmap == null) {
            return null;
        }
        return bitmap;
    }

    // private Paint getMergePaint() {
    // if (mMergePaint == null) {
    // mMergePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //// mMergePaint.setXfermode(new PorterDuffXfermode(
    //// PorterDuff.Mode.DST_IN));
    // }
    // return mMergePaint;
    // }

    protected boolean shouldMergeIfNull = false;;  
    private Rect drawingRect = new Rect();
    public Bitmap mergeBitmaps(Bitmap[] parts) {

        // Check if any of the bitmap is null (if so return null) :
        boolean anyNull = false;
        boolean allNull = false;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                anyNull = true;
            } else {
                allNull = false;
            }
        }
        if (allNull || (!shouldMergeIfNull && anyNull)) {
            return null;
        }
        Bitmap tileBitmap = null;
        try {
            tileBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
        // tileBitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(tileBitmap);
        // Paint paint = getMergePaint();
        for (int i = 0; i < parts.length; i++) {

             if(parts[i] != null) {
            //
            // parts[i] = Bitmap.createBitmap(256, 256,
            // Bitmap.Config.ARGB_8888);
            // }
                final int left = parts[i].getWidth() * (i % 2);
                final int top = parts[i].getHeight() * (i / 2);
                drawingRect.set(left, top, left + 256, top + 256);
                canvas.drawBitmap(parts[i], null, drawingRect, null);
             }
//            canvas.drawBitmap(parts[i], parts[i].getWidth() * (i % 2),
//                    parts[i].getHeight() * (i / 2), null);
        }

        return tileBitmap;
    }

    public String getSubdomain(int x, int y) {
        int index = (x + y) % mSubdomains.length();
        return mSubdomains.substring(index, index + 1);
    }

    static double born(double value, double max) {
        return Math.max(-max, Math.min(max, value));
    }

    public double[] convertSRS(double[] bbox, final boolean to900913) {
        if (to900913) {
            bbox[0] = born(EARTH_RADIUS * Math.toRadians(bbox[0]), WMS_MAP_SIZE_2);
            bbox[1] = born(EARTH_RADIUS * Math.log(Math.tan((Math.PI*0.25) + (0.5 * Math.toRadians(bbox[1])))), WMS_MAP_SIZE_2);
            
            bbox[2] = born(EARTH_RADIUS * Math.toRadians(bbox[2]), WMS_MAP_SIZE_2);
            bbox[3] = born(EARTH_RADIUS * Math.log(Math.tan((Math.PI*0.25) + (0.5 * Math.toRadians(bbox[3])))), WMS_MAP_SIZE_2);
            
        } else {
            bbox[0] = Math.toDegrees(bbox[0]) / EARTH_RADIUS;
            bbox[1] = Math.toDegrees((Math.PI*0.5) - 2.0 * Math.atan(Math.exp(-bbox[1] / EARTH_RADIUS)));
            
            bbox[2] = Math.toDegrees(bbox[2]) / EARTH_RADIUS;
            bbox[3] = Math.toDegrees((Math.PI*0.5) - 2.0 * Math.atan(Math.exp(-bbox[3] / EARTH_RADIUS)));
        }
        return bbox;
    }
    
    public double[] bbox(int x, int y, int zoom, final String SRS, double[] reuse) {
        double[] result = (reuse != null)?reuse:new double[4];
        double tileSize = WMS_MAP_SIZE_2 * 2 / Math.pow(2, zoom);
        result[0] = - WMS_MAP_SIZE_2 + x * tileSize;
        result[1] = WMS_MAP_SIZE_2 - (y+1) * tileSize;
        result[2] = - WMS_MAP_SIZE_2 + (x+1) * tileSize;
        result[3] = WMS_MAP_SIZE_2 - y * tileSize;
        if (SRS.indexOf("900913") == -1) {
            return convertSRS(result, false);
        }
        return result;
    }

    public String getTileUrl(int x, int y, int zoom) {
        if (mUrl == null) {
            return null;
        }
        String result = mUrl;
        if (wmsFormat) {
            double[] bbox = bbox(x, y, zoom, mSRS, null);
            result = result.replace("{bbox}", bbox[0] + "," +  bbox[1] + "," +  bbox[2] + "," +  bbox[3]);
        } else {
            result = result.replace("{z}", Integer.toString(zoom))
                    .replace("{x}", Integer.toString(x))
                    .replace("{y}", Integer.toString(y));
        }
        return result.replace("{2x}", (mTileSizePixels >= 512) ? "@2x" : "") // for
                                                                        // mapbox
                .replace("{s}", getSubdomain(x, y));
    }

    public void setURL(final String aUrl) {
        if (!mEnableSSL) {
            mUrl = aUrl.replace("https://", "http://");
        } else {
            mUrl = aUrl;
        }
        if (mUrl.contains("{bbox}")) {
            wmsFormat = true;
            Matcher matcher = sSRSPattern.matcher(mUrl);
            if (matcher.find()) {
                mSRS = matcher.group();
            }
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

    public WebTileProvider setVisible(final boolean visible) {
        mVisible = visible;
        return this;
    }

    public WebTileProvider setAutoHD(final boolean hd) {
        mAutoHD = hd;
        return this;
    }

    public WebTileProvider setShowTileAfterMaxZoom(
            boolean showTileAfterMaxZoom) {
        mShowTileAfterMaxZoom = showTileAfterMaxZoom;
        return this;

    }

    public WebTileProvider setOpacity(final float opacity) {
        mOpacity = opacity;
        // tilePaint.setAlpha((int) (mOpacity * 255));
        return this;
    }

    public WebTileProvider setTileSize(final int size) {
        mTileSizePixels = size;
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

    public void setSubdomains(String string) {
        // TODO Auto-generated method stub

    }

    public void setCacheable(boolean cacheable) {
        mCacheable = cacheable;
    }

}
