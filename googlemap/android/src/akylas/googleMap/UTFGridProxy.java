package akylas.googlemap;

import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileProvider;

@Kroll.proxy(creatableInModule = AkylasGooglemapModule.class, propertyAccessors = {
        TiC.PROPERTY_VISIBLE, "tileSize", "showTileAfterMaxZoom" })
public class UTFGridProxy extends TileSourceProxy {

    private static final String TAG = "UTFGridProxy";

    @Override
    public TileProvider getTileProvider() {
        if (mTileProvider == null && mSource != null) {
            String sSource = TiConvert.toString(mSource);
            if (sSource == null) {
                return null;
            }

            final int tileSize = TiConvert.toInt(getProperty("tileSize"), 256);
            mTileProvider = new UTFGridProvider(
                    TiConvert.toString(getProperty("id")),
                    TiConvert.toString(getProperty("url")), tileSize)
                            .setName(TiConvert.toString(getProperty("name")))
                            .setAttribution(TiConvert
                                    .toString(getProperty("attribution")));

            initTileProvider();
        }

        return mTileProvider;
    }
    
    @Kroll.method
    public Object getData(Object pos, float zoom) {
        if (mTileProvider instanceof UTFGridProvider) {
            LatLng latlng = (LatLng) AkylasGooglemapModule.latlongFromObject(pos);
            if (latlng != null) {
                return ((UTFGridProvider) mTileProvider).getData(latlng,  (int) Math.floor(zoom));
            }
        }
        return null;
    }
}
