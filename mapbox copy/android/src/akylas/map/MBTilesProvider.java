package akylas.map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;

public class MBTilesProvider extends  MBTilesLayer implements TileProvider {

    private static final String TAG = "MBTilesProvider";

    private static final int BUFFER_SIZE = 16 * 1024;
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    
    public MBTilesProvider(Context context, String url) {
        super(context, url);
    }

    public MBTilesProvider(final String url) {
        super(url);
    }

    public MBTilesProvider(final File file) {
        super(file);
    }

    public MBTilesProvider(final SQLiteDatabase db) {
        super(db);
    }

    
    @Override
    public Tile getTile(int x, int y, int zoom) {
        if (mbTilesFileArchive != null) {
            InputStream stream = mbTilesFileArchive.getInputStream(x, y, zoom);
            if (stream != null) {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[BUFFER_SIZE];

                    while ((nRead = stream.read(data, 0, BUFFER_SIZE)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();

                    return new Tile(TILE_WIDTH, TILE_HEIGHT,
                            buffer.toByteArray());
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
