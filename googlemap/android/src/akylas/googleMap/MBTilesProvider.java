package akylas.googlemap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Interceptor.Chain;
import com.squareup.picasso.Cache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

public class MBTilesProvider extends WebTileProvider {

    private static final String TAG = "MBTilesProvider";

    private static final int BUFFER_SIZE = 16 * 1024;
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    protected MBTilesFileArchive mbTilesFileArchive;
    protected String mUrl;
    protected String mCacheKey;
    protected String mName;
    protected String mDescription;
    protected String mAttribution;
    protected String mLegend;

    protected float mMinimumZoomLevel = 0;
    protected float mMaximumZoomLevel = 22;
    protected LatLngBounds mBoundingBox = AkylasGooglemapModule.WORLD_BOUNDING_BOX;
    protected LatLng mCenter = new LatLng(0, 0);
    
//    public MBTilesProvider(Context context, String url) {
//        initialize(url, context);
//   }

//    public MBTilesProvider(final String url) {
//        this(null, url);
//    }

//    public MBTilesProvider(final File file) {
//        initialize(file);
//    }
//
//    public MBTilesProvider(final SQLiteDatabase db) {
//        initialize(db);
//    }
    
    public MBTilesProvider(final String pId, final SQLiteDatabase db,
            final int tileSize) {
        super(pId, null, tileSize);
        shouldMergeIfNull = true;
        initialize(db);
    }
    @Override
    protected void initialize(String pId, String aUrl, boolean enableSSL) {
    }
    
    @Override
    public Bitmap getTileImage(int x, int y, int z) {
        String cacheKey = (mId != null) ? (mId+x+y+z) : null;
        Cache cache = TiApplication.getImageMemoryCache();
        Bitmap result = (cacheKey != null) ? cache.get(cacheKey) : null;
        if (result == null && mbTilesFileArchive != null) {
            result = mbTilesFileArchive.getBitmap(x, y, z);
            if (result != null && cacheKey != null) {
                cache.set(cacheKey, result);
            }
        }
        return result;
//        if (mbTilesFileArchive != null) {
//            if (stream != null) {
////                try {
//////                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
////                    return BitmapFactory.decodeStream(stream);
////                } catch (Exception e) {
////                    return null;
////                }
//                try {
//                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                    int nRead;
//                    byte[] data = new byte[BUFFER_SIZE];
//
//                    while ((nRead = stream.read(data, 0, BUFFER_SIZE)) != -1) {
//                        buffer.write(data, 0, nRead);
//                    }
//                    buffer.flush();
//                    data = buffer.toByteArray();
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0, data.length);
//                    return bitmap;
////                    return new Tile(TILE_WIDTH, TILE_HEIGHT,
////                            buffer.toByteArray());
//                } catch (IOException e) {
//                    return null;
//                }
//            }
//        }
//        return null;
    }
    
//    private static final String getFileName(final String path) {
//        return path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
//    }

    /**
     * Creates a file from an input stream by reading it byte by byte.
     * todo: same as MapViewFactory's createFileFromInputStream
     */
//    private static File createFileFromInputStream(InputStream inputStream, String URL) {
//        try {
//            File f = new File(URL);
//            OutputStream outputStream = new FileOutputStream(f);
//            byte[] buffer = new byte[1024];
//            int length = 0;
//
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//
//            outputStream.close();
//            inputStream.close();
//
//            return f;
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to create file from input stream.", e);
//        }
//        return null;
//    }

    /**
     * Reads and opens a MBTiles file and loads its tiles into this layer.
     *
     * @param file
     */
//    private void initialize(File file) {
//        if (file != null) {
//            mbTilesFileArchive = MBTilesFileArchive.getDatabaseFileArchive(file);
//        }
//
//        if (mbTilesFileArchive != null) {
//            mMaximumZoomLevel = mbTilesFileArchive.getMaxZoomLevel();
//            mMinimumZoomLevel = mbTilesFileArchive.getMinZoomLevel();
//            mName = mbTilesFileArchive.getName();
//            mDescription = mbTilesFileArchive.getDescription();
//            mAttribution = mbTilesFileArchive.getAttribution();
//            mBoundingBox = mbTilesFileArchive.getBounds();
//            mCenter = mbTilesFileArchive.getCenter();
//        }
//    }

    /**
     * Reads and opens a MBTiles file given by url and loads its tiles into this layer.
     */
    private void initialize(final SQLiteDatabase db) {
        if (db != null) {
            mbTilesFileArchive = new MBTilesFileArchive(db);
        }

        if (mbTilesFileArchive != null) {
            mMaximumZoomLevel = mbTilesFileArchive.getMaxZoomLevel();
            mMinimumZoomLevel = mbTilesFileArchive.getMinZoomLevel();
            mName = mbTilesFileArchive.getName();
            mDescription = mbTilesFileArchive.getDescription();
            mAttribution = mbTilesFileArchive.getAttribution();
            mBoundingBox = mbTilesFileArchive.getBounds();
            mCenter = mbTilesFileArchive.getCenter();
        }
    }

    /**
     * Reads and opens a MBTiles file given by url and loads its tiles into this layer.
     */
//    private void initialize(String url, final Context context) {
//        initialize(getFile(url, context));
//    }

//    private File getFile(String url, final Context context) {
//        if (context != null) {
//            //we assume asset here
//            AssetManager am = context.getAssets();
//            InputStream inputStream;
//            try {
//                inputStream = am.open(url);
//                final File mbTilesDir;
//                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
//                        || (!Environment.isExternalStorageRemovable())) {
//                    mbTilesDir = new File(context.getExternalFilesDir(null), url);
//                } else {
//                    mbTilesDir = new File(context.getFilesDir(), url);
//                }
//                return createFileFromInputStream(inputStream, mbTilesDir.getPath());
//            } catch (IOException e) {
//                Log.e(TAG, "MBTiles file not found in assets: " + e.toString());
//                return null;
//            }
//        }
//        try {
//            return new File(url);
//        } catch (Exception e) {
//            Log.e(TAG, "can't load MBTiles: " + e.toString());
//            return null;
//        }
//    }

    public void detach() {
        if (mbTilesFileArchive != null) {
            mbTilesFileArchive.close();
            mbTilesFileArchive = null;
        }
    }

//    public float getMaximumZoomLevel() {
//        return mMaximumZoomLevel;
//    }
//    
//    public float getMinimumZoomLevel() {
//        return mMinimumZoomLevel;
//    }
//
//    public LatLngBounds getBoundingBox() {
//        return mBoundingBox;
//    }
}
