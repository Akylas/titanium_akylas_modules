package akylas.admob;

import java.util.Date;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

@Kroll.proxy(creatableInModule = AkylasAdmobModule.class, name="Interstitial")
public class InterstitialViewProxy extends ReusableProxy {
    private static final String TAG = "InterstitialViewProxy";
   
    String prop_color_bg;
    String prop_color_bg_top;
    String prop_color_border;
    String prop_color_text;
    String prop_color_link;
    String prop_color_url;
    protected KrollDict adProps = new KrollDict();
    protected Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    InterstitialAd adView =  null;
    private boolean showOnLoad = false;
    protected AdListener adListener = new AdListener() {
        
        @Override
        public void onAdLoaded() {
            fireEvent(AkylasAdmobModule.AD_RECEIVED);
            if (showOnLoad) {
                show();
            }
        }
        
        @Override
        public void onAdFailedToLoad(int errorCode) {
            fireEvent(AkylasAdmobModule.AD_NOT_RECEIVED);
        }

        @Override
        public void onAdLeftApplication() {
            fireEvent(AkylasAdmobModule.AD_LEFT_APP);
        }

        @Override
        public void onAdOpened() {
            fireEvent(AkylasAdmobModule.AD_OPENED);
        }
        
        @Override
        public void onAdClosed() {
            fireEvent(AkylasAdmobModule.AD_CLOSED);
            adView = null;
        }
    };
    
    public InterstitialViewProxy() {
        super();
    }
    
    private void handleLoadAd() {
        final AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        if (adProps.containsKey(AkylasAdmobModule.PROPERTY_TEST_DEVICES)) {
            for (String device : adProps.getStringArray(AkylasAdmobModule.PROPERTY_TEST_DEVICES)) {
                adRequestBuilder .addTestDevice(device);
            }
        }
        if (adProps.containsKey(AkylasAdmobModule.PROPERTY_KEYWORDS)) {
            for (String device : adProps.getStringArray(AkylasAdmobModule.PROPERTY_KEYWORDS)) {
                adRequestBuilder .addKeyword(device);
            }
        }
        if (adProps.containsKey(TiC.PROPERTY_BIRTHDAY))
        {
            adRequestBuilder.setBirthday((Date) adProps.get(TiC.PROPERTY_BIRTHDAY));
        }
        if (adProps.containsKey(AkylasAdmobModule.PROPERTY_GENDER))
        {
            adRequestBuilder.setGender(adProps.getInt(AkylasAdmobModule.PROPERTY_GENDER));
        }
        if (adProps.containsKey(TiC.PROPERTY_LOCATION))
        {
            adRequestBuilder.setLocation((Location) adProps.get(TiC.PROPERTY_LOCATION));
        }
        
        Bundle bundle = new Bundle();
        if (prop_color_bg != null) {
            Log.d(TAG, "color_bg: " + prop_color_bg);
            bundle.putString("color_bg", prop_color_bg);
        }
        if (prop_color_bg_top != null)
            bundle.putString("color_bg_top", prop_color_bg_top);
        if (prop_color_border != null)
            bundle.putString("color_border", prop_color_border);
        if (prop_color_text != null)
            bundle.putString("color_text", prop_color_text);
        if (prop_color_link != null)
            bundle.putString("color_link", prop_color_link);
        if (prop_color_url != null)
            bundle.putString("color_url", prop_color_url);
        if (bundle.size() > 0) {
            adRequestBuilder.addNetworkExtras(new AdMobExtras(bundle));
        }
        loadAdWithRequest(adRequestBuilder.build());
    }
    
    private String convertColorProp(Object value) {
        int color = TiConvert.toColor(value);
        return String.format("%06X", (0xFFFFFF & color));
    }



    // load the adMob ad
    public void loadAd() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                handleLoadAd();
            }
        });
    }

    @Kroll.method
    public void load() {
        if (adView != null) {
            return;
        }
    
        adView = new InterstitialAd(getActivity());
        adView.setAdUnitId(adProps.getString(AkylasAdmobModule.PROPERTY_ADUNITID));
        // set the listener
        adView.setAdListener(adListener);
        loadAd();
    }
    
    @Kroll.method
    public void show() {
        if (adView != null) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (adView.isLoaded()) {
                        adView.show();
                    }
                }
            });
        }
    }
    
    @Kroll.method
    @Kroll.getProperty
    public boolean getLoaded() {
        if (adView != null) {
            return adView.isLoaded();
        }
        return false;
    }
    

    protected void loadAdWithRequest(AdRequest request) {
        adView.loadAd(request);
    }

    public boolean isLoaded() {
        if (adView != null) {
            return adView.isLoaded();
        }
        return false;
    }

    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasAdmobModule.PROPERTY_ADUNITID:
            adProps.put(AkylasAdmobModule.PROPERTY_ADUNITID, TiConvert.toString(newValue));
            break;
        case AkylasAdmobModule.PROPERTY_TEST_DEVICES:
            adProps.put(AkylasAdmobModule.PROPERTY_TEST_DEVICES, TiConvert.toStringArray(newValue));
            break;
        case AkylasAdmobModule.PROPERTY_KEYWORDS:
            adProps.put(AkylasAdmobModule.PROPERTY_KEYWORDS, TiConvert.toStringArray(newValue));
            break;
        case TiC.PROPERTY_BIRTHDAY:
            adProps.put(TiC.PROPERTY_BIRTHDAY, TiConvert.toDate(newValue));
            break;
        case TiC.PROPERTY_LOCATION:
            adProps.put(TiC.PROPERTY_LOCATION, AkylasAdmobModule.toLocation(newValue));
            break;
        case AkylasAdmobModule.PROPERTY_GENDER:
            String gender = TiConvert.toString(newValue);
            int adGender =  AdRequest.GENDER_UNKNOWN;
            if (gender.equals("male")) {
                adGender =  AdRequest.GENDER_MALE;
            } else if (gender.equals("female")) {
                adGender =  AdRequest.GENDER_FEMALE;
            }
            adProps.put(AkylasAdmobModule.PROPERTY_GENDER, adGender);
            break;
        case AkylasAdmobModule.PROPERTY_COLOR_BG: 
        {
            prop_color_bg = convertColorProp(newValue);
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_BG_TOP: {
            prop_color_bg_top = convertColorProp(newValue);
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_BORDER: {
            prop_color_border = convertColorProp(newValue);
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_TEXT: {
            prop_color_text = convertColorProp(newValue);
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_LINK: {
            prop_color_link = convertColorProp(newValue);
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_URL: {
            prop_color_url = convertColorProp(newValue);
            break;
        }
        case "showOnLoad":
            showOnLoad = TiConvert.toBoolean(newValue, showOnLoad);
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
