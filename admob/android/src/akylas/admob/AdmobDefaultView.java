package akylas.admob;

import java.util.Date;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

public  abstract class AdmobDefaultView extends TiUIView {
    private static final String TAG = "AdMobDefaultView";
    private static final int MSG_LOAD = 10001;
    
    String prop_color_bg;
    String prop_color_bg_top;
    String prop_color_border;
    String prop_color_text;
    String prop_color_link;
    String prop_color_url;
    protected KrollDict adProps = new KrollDict();
    protected Handler mainHandler = new Handler(Looper.getMainLooper(), this);
    protected AdListener adListener = new AdListener() {
        
        @Override
        public void onAdLoaded() {
            AdmobDefaultView.this.onAdLoaded();
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            AdmobDefaultView.this.onAdFailedToLoad(errorCode);
        }

        @Override
        public void onAdLeftApplication() {
            AdmobDefaultView.this.onAdLeftApplication();
        }

        @Override
        public void onAdOpened() {
            AdmobDefaultView.this.onAdOpened();
        }
        
        @Override
        public void onAdClosed() {
            AdmobDefaultView.this.onAdClosed();
        }
    };
   
    protected static final int TIFLAG_NEEDS_LOAD               = 0x00000001;


    public AdmobDefaultView(final TiViewProxy proxy) {
        super(proxy);
    }
    
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MSG_LOAD:
            handleLoadAd();
            return true;
        default:
            return false;
        }
    }

    protected abstract void createAdView();
    protected abstract void loadAdWithRequest(final AdRequest request);
    
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
        
        Bundle bundle = createAdRequestProperties();
        if (bundle.size() > 0) {
            adRequestBuilder.addNetworkExtras(new AdMobExtras(bundle));
        }
        loadAdWithRequest(adRequestBuilder.build());
    }

    // load the adMob ad
    public void loadAd() {
        
        if (!TiApplication.isUIThread()) {
            TiMessenger.sendBlockingMainMessage(
                    mainHandler.obtainMessage(MSG_LOAD));
        } else {
            handleLoadAd();
        }
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasAdmobModule.PROPERTY_ADUNITID:
            adProps.put(AkylasAdmobModule.PROPERTY_ADUNITID, TiConvert.toString(newValue));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case AkylasAdmobModule.PROPERTY_ADSIZE:
            adProps.put(AkylasAdmobModule.PROPERTY_ADSIZE, AkylasAdmobModule.sizeFromString(TiConvert.toString(newValue)));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case AkylasAdmobModule.PROPERTY_TEST_DEVICES:
            adProps.put(AkylasAdmobModule.PROPERTY_TEST_DEVICES, TiConvert.toStringArray(newValue));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case AkylasAdmobModule.PROPERTY_KEYWORDS:
            adProps.put(AkylasAdmobModule.PROPERTY_KEYWORDS, TiConvert.toStringArray(newValue));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case TiC.PROPERTY_BIRTHDAY:
            adProps.put(TiC.PROPERTY_BIRTHDAY, TiConvert.toDate(newValue));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case TiC.PROPERTY_LOCATION:
            adProps.put(TiC.PROPERTY_LOCATION, AkylasAdmobModule.toLocation(newValue));
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
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
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        case AkylasAdmobModule.PROPERTY_COLOR_BG: 
        {
            prop_color_bg = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_BG_TOP: {
            prop_color_bg_top = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_BORDER: {
            prop_color_border = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_TEXT: {
            prop_color_text = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_LINK: {
            prop_color_link = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        case AkylasAdmobModule.PROPERTY_COLOR_URL: {
            prop_color_url = convertColorProp(newValue);
            mProcessUpdateFlags |= TIFLAG_NEEDS_LOAD;
            break;
        }
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
    
    @Override
    protected void didProcessProperties() {
        super.didProcessProperties();
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_LOAD) != 0) {
            createAdView();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_LOAD;
        }
    }

    // create the adRequest extra props
    // http://code.google.com/mobile/ads/docs/bestpractices.html#adcolors
    private Bundle createAdRequestProperties() {
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
        return bundle;
    }

    // modifies the color prop -- removes # and changes constants into hex
    // values
    private String convertColorProp(Object value) {
        int color = TiConvert.toColor(value);
        return String.format("%06X", (0xFFFFFF & color));
    }

    protected void onAdLoaded() {
        proxy.fireEvent(AkylasAdmobModule.AD_RECEIVED);
    }

    protected void onAdFailedToLoad(int errorCode) {
        proxy.fireEvent(AkylasAdmobModule.AD_NOT_RECEIVED);
    }

    protected void onAdLeftApplication() {
        proxy.fireEvent(AkylasAdmobModule.AD_LEFT_APP);
    }

    protected void onAdOpened() {
        proxy.fireEvent(AkylasAdmobModule.AD_OPENED);
    }
    
    protected void onAdClosed() {
        proxy.fireEvent(AkylasAdmobModule.AD_CLOSED);
    }
}