package akylas.googlemap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.SSLSocketFactory;

import org.appcelerator.titanium.TiApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

public class TileJsonProvider extends WebTileProvider {
    private static final String TAG = "TileJsonProvider";

    private JSONObject tileJSON;
    private String jsonURL;
    private Cache cache;
    private boolean isFetchingtileJSON = false;

    public TileJsonProvider(final String pId, final String url,
            final boolean enableSSL) {
        super(pId, url, enableSSL);
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), UUID
                .randomUUID().toString());
        try {
            cache = new Cache(cacheDir, 1024);
        } catch (Exception e) {
            Log.e(TAG, "Cache creation failed.", e);
        }

        jsonURL = this.getBrandedJSONURL();
        if (jsonURL != null) {
            fetchBrandedJSONAndInit(jsonURL);
        }
    }
    
    @Override
    public String getTileUrl(int x, int y, int zoom) {
        if (jsonURL != null && this.tileJSON == null && !isFetchingtileJSON) {
            //try to get the JSON again
            ConnectivityManager cm = (ConnectivityManager) TiApplication.getAppSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if(ni != null && ni.isAvailable() && ni.isConnected()) {
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
                mCenter = (LatLng) AkylasGoogleMapModule.getFactory().createPoint(center[0], center[1], center[2]);
            }
            double[] bounds = getJSONDoubleArray(this.tileJSON, "bounds", 4);
            if (bounds != null) {
                mBoundingBox = (LatLngBounds) AkylasGoogleMapModule.getFactory().createRegion(bounds[3], bounds[2], bounds[1],
                        bounds[0]);
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
        new RetrieveJSONTask() {
            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                initWithTileJSON(jsonObject);
                isFetchingtileJSON = false;
            }
        }.execute(url);
    }

    protected String getBrandedJSONURL() {
        return null;
    }
    
    public static HttpURLConnection getHttpURLConnection(final URL url, final Cache cache, final SSLSocketFactory sslSocketFactory) {
        OkHttpClient client = new OkHttpClient();
        if (cache != null) {
            client.setCache(cache);
        }
        if (sslSocketFactory != null) {
            client.setSslSocketFactory(sslSocketFactory);
        }
        HttpURLConnection connection = new OkUrlFactory(client).open(url);
        return connection;
    }

    class RetrieveJSONTask extends AsyncTask<String, Void, JSONObject> {
        protected JSONObject doInBackground(String... urls) {
            InputStream in = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = getHttpURLConnection(url, cache, null);
                in = connection.getInputStream();
                byte[] response = readFully(in);
                String result = new String(response, "UTF-8");
                return new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error closing InputStream: " + e.toString());
                }
            }
        }
    }
}
