package akylas.admob;

import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.graphics.RectF;
import android.view.ViewGroup;

class AdmobView extends AdmobDefaultView {
    private static final String TAG = "AdMobView";
    AdView adView =  null;
    private RectF padding;

    public AdmobView(TiViewProxy proxy) {
        super(proxy);
//        layout = new TiCompositeLayout(proxy.getActivity(), this);
//        setNativeView(layout);
    }
    
    @Override
    protected void createAdView() {
        if (adView != null) {
            adView.destroy();
            adView.setAdListener(null);
//            TiUIHelper.removeViewFromSuperView(adView);
        }
        
        adView = new AdView(proxy.getActivity());
        AdSize size = (AdSize)adProps.get(AkylasAdmobModule.PROPERTY_ADSIZE);
        adView.setAdSize(size);
        adView.setAdUnitId(adProps.getString(AkylasAdmobModule.PROPERTY_ADUNITID));
        // set the listener
        adView.setFocusable(false);
        adView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        adView.setAdListener(adListener);
        if (padding != null) {
            adView.setPadding((int) padding.left, (int) padding.top, (int) padding.right, (int) padding.bottom);
        }
        // Add the AdView to your view hierarchy.
        // The view will have no size until the ad is loaded.
//        layout.addView(adView);
        setNativeView(adView);
        loadAd();
    }
    
//    @Override
//    protected void onAdLoaded() {
//        super.onAdLoaded();
//        layoutNativeView();
//    }
    
    @Override
    public void release() {
        if (adView != null) {
            adView.destroy();
            adView.setAdListener(null);
//            TiUIHelper.removeViewFromSuperView(adView);
            adView = null;
        }
    }
    
    
    @Override
    protected void onAdClosed() {
        super.onAdClosed();
        adView = null;
    }

    @Override
    protected void loadAdWithRequest(AdRequest request) {
        if (adView != null) {
            adView.loadAd(request);
        }
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_PADDING:
            padding = TiConvert.toPaddingRect(newValue, padding);
            if (adView != null) {
                adView.setPadding((int) padding.left, (int) padding.top, (int) padding.right, (int) padding.bottom);
                setNeedsLayout();
            }
            break;
        
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

}
