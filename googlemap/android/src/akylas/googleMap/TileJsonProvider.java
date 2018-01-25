package akylas.googlemap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.appcelerator.titanium.TiApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TileJsonProvider extends WebTileProvider {
    private static final String TAG = "TileJsonProvider";

    private JSONObject tileJSON;
    private String jsonURL;
    private boolean isFetchingtileJSON = false;

    public TileJsonProvider(final String pId, final String url, final boolean enableSSL) {
        this(pId, url, enableSSL, true);
    }
    public TileJsonProvider(final String pId, final String url, final boolean enableSSL, final boolean shouldInit) {
        super(pId, url, enableSSL, shouldInit);
    }

    protected void initialize(String pId, String aUrl, boolean enableSSL) {
        super.initialize(pId, aUrl, enableSSL);
        jsonURL = this.getBrandedJSONURL();
        fetchBrandedJSONAndInit(jsonURL);
    }

    @Override
    public String getTileUrl(int x, int y, int zoom) {
        if (this.tileJSON == null && jsonURL != null && !isFetchingtileJSON) {
            // try to get the JSON again
            ConnectivityManager cm = (ConnectivityManager) TiApplication
                    .getAppSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isAvailable() && ni.isConnected()) {
                    fetchBrandedJSONAndInit(jsonURL);
                }
            }
        }
        return super.getTileUrl(x, y, zoom);
    }

    private void initWithTileJSON(JSONObject aTileJSON) {
        this.setTileJSON((aTileJSON != null) ? aTileJSON : new JSONObject());
        if (aTileJSON != null) {
            if (this.tileJSON.has("tiles")) {
                try {
                    setURL(this.tileJSON.getJSONArray("tiles").getString(0)
                            .replace(".png", "{2x}.png"));
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't set tile url", e);
                }
            }
            mMinimumZoomLevel = getJSONFloat(this.tileJSON, "minzoom");
            mMaximumZoomLevel = getJSONFloat(this.tileJSON, "maxzoom");
            mName = this.tileJSON.optString("name");
            mDescription = this.tileJSON.optString("description");
            mAttribution = this.tileJSON.optString("attribution");
            mLegend = this.tileJSON.optString("legend");

            double[] center = getJSONDoubleArray(this.tileJSON, "center", 3);
            if (center != null) {
                mCenter = (LatLng) AkylasGooglemapModule.getFactory()
                        .createPoint(center[0], center[1], center[2]);
            }
            double[] bounds = getJSONDoubleArray(this.tileJSON, "bounds", 4);
            if (bounds != null) {
                mBoundingBox = (LatLngBounds) AkylasGooglemapModule
                        .getFactory().createRegion(bounds[3], bounds[2],
                                bounds[1], bounds[0]);
            }
        }
    }

    public JSONObject getTileJSON() {
        return tileJSON;
    }

    public void setTileJSON(JSONObject aTileJSON) {
        this.tileJSON = aTileJSON;
    }

    private float getJSONFloat(JSONObject JSON, String key) {
        float defaultValue = 0;
        if (JSON.has(key)) {
            try {
                return (float) JSON.getDouble(key);
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double[] getJSONDoubleArray(JSONObject JSON, String key, int length) {
        double[] defaultValue = null;
        if (JSON.has(key)) {
            try {
                boolean valid = false;
                double[] result = new double[length];
                Object value = JSON.get(key);
                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    if (array.length() == length) {
                        for (int i = 0; i < array.length(); i++) {
                            result[i] = array.getDouble(i);
                        }
                        valid = true;
                    }
                } else {
                    String[] array = JSON.getString(key).split(",");
                    if (array.length == length) {
                        for (int i = 0; i < array.length; i++) {
                            result[i] = Double.parseDouble(array[i]);
                        }
                        valid = true;
                    }
                }
                if (valid) {
                    return result;
                }
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1;) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    private void fetchBrandedJSONAndInit(String url) {
        if (isFetchingtileJSON) {
            return;
        }
        isFetchingtileJSON = true;
        if (jsonURL != null) {
            TiApplication
                    .getOkHttpClientInstance()
                    .newCall(
                            new okhttp3.Request.Builder().url(
                                    jsonURL).build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call request, IOException e) {
                            e.printStackTrace();
                            isFetchingtileJSON = false;
                        }

                        @Override
                        public void onResponse(Call arg0, Response response)
                                throws IOException {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code "
                                        + response);
                            try {
                                initWithTileJSON(new JSONObject(response.body()
                                        .string()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            isFetchingtileJSON = false;
                        }
                    });
        }
    }

    protected String getBrandedJSONURL() {
        return null;
    }

}
