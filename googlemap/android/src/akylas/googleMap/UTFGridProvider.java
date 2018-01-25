package akylas.googlemap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.maps.android.projection.SphericalMercatorProjection;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.CacheControl;
import okhttp3.Interceptor;

import android.graphics.Bitmap;

public class UTFGridProvider extends WebTileProvider {
    OkHttpClient client; 
    SphericalMercatorProjection projection;

    private static byte[] sBlankBitmapData;
    
    float resolution = 4;
    
    
    
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
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
        }
    };
    
    public UTFGridProvider(final String pId, final String url,
            final int tileSize) {
        super(pId, url, false, true, tileSize);
    }
    
    
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        setURL(aUrl);
        diskCache = TiApplication.getDiskCache("akylas.utfgrid.data" + pId);
        client = new OkHttpClient().newBuilder().cache(diskCache).addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR).addInterceptor(new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                okhttp3.Request.Builder builder = chain.request()
                        .newBuilder();
                if (mUserAgent != null) {
                    builder.addHeader("User-Agent", mUserAgent);
                }
                return chain.proceed(builder.build());
            }
        }).build();
    }
    
    @Override
    public Tile getTile(int x, int y, int z) {
        if (mMinimumZoomLevel >= 0 && z < mMinimumZoomLevel) {
            return NO_TILE;
        }
        if (mMaximumZoomLevel >= 0 && z > mMaximumZoomLevel) {
            return NO_TILE;
        }
        String data = getTileData(x, y, z, false);
        if (data != null) {
//            final String key = z + "_" + x + "_" + y;
//            dataCache.put(key, data);
            if (sBlankBitmapData == null) {
                Bitmap bitmap = Bitmap.createBitmap(mTileSizePixels, mTileSizePixels, Bitmap.Config.ARGB_8888);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                sBlankBitmapData = stream.toByteArray();
                bitmap.recycle();
            }
//            return null;
            return new Tile(mTileSizePixels, mTileSizePixels, sBlankBitmapData);
        }
        return null;
    }
    
    protected String getTileData(final int x, final int y, final int z, final boolean onlyCache) {
        try {
            final String url = getTileUrl(x, y, z);
            Request.Builder builder = new Request.Builder().url(url);
            if (onlyCache) {
                Log.d(TAG, url);
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }

            Response response = client.newCall(builder.build()).execute();

            return response.body().string();
        } catch (Exception e) {
        }
        return null;
    }
    
    public Object getData(LatLng latlng, int zoom) {
        boolean passedMax = mMaximumZoomLevel >= 0
                && zoom > mMaximumZoomLevel;
        if (passedMax && !mShowTileAfterMaxZoom) {
            return null;
        }
        double lat_rad = Math.toRadians(latlng.latitude);
        int n = (int) Math.pow(2, zoom);
        double xfloat = (latlng.longitude + 180.0) / 360.0 * n;
        double yfloat = (1.0 - Math.log(Math.tan(lat_rad) + (1 / Math.cos(lat_rad))) / Math.PI) / 2.0 * n;
        if (passedMax) {
            float currentTileDepth = zoom - mMaximumZoomLevel;
            xfloat = xfloat / Math.pow(2.0, currentTileDepth);
            yfloat = yfloat / Math.pow(2.0, currentTileDepth);
            zoom = (int) mMaximumZoomLevel;
        }
        int x = (int) Math.floor(xfloat);
        int y = (int) Math.floor(yfloat);
        
        
        int gridX = (int) ((xfloat - x) * mTileSizePixels / resolution);
        int gridY = (int) ((yfloat - y) * mTileSizePixels / resolution);
        String gridData = getTileData(x, y, zoom, true);
        Log.d(TAG, x + "," + y + "," + zoom + "," + gridX + "," + gridY);
        if (gridData != null) {
            KrollDict json;
            try {
                json = new KrollDict(gridData);
                final char theChar = TiConvert
                        .toString(((Object[]) json.get("grid"))[gridY]).charAt(gridX);
                Log.d(TAG, "theChar " + theChar + " " + (int)theChar);
                final int idx = utfDecode(theChar);
               final String key = TiConvert
                        .toString(((Object[]) json.get("keys"))[idx]);
               Log.d(TAG, "idx " + idx);
               Log.d(TAG, "key " + key);
             return (TiConvert.toHashMap(json.get("data"))).get(key);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    private int utfDecode(char c) {
        if (c >= 93) {
            c--;
        }
        if (c >= 35) {
            c--;
        }
        return c - 32;
    }

}
