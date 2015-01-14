package akylas.admob;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBaseActivity.ConfigurationChangedListener;
import org.appcelerator.titanium.view.TiUIView;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

@Kroll.proxy(creatableInModule = AkylasAdmobModule.class, name="View")
public class AdmobViewProxy extends AdmobDefaultViewProxy implements ConfigurationChangedListener {

    @Override
    public TiUIView createView(Activity activity) {
        return new AdmobView(this);
    }
    
    @Override
    public void setActivity(Activity activity) {
        if (this.activity != null) {
            TiBaseActivity tiActivity = (TiBaseActivity) this.activity.get();
            if (tiActivity != null) {
                tiActivity.removeOnLifecycleEventListener(this);
                tiActivity.removeConfigurationChangedListener(this);
            }
        }
        super.setActivity(activity);
        if (this.activity != null) {
            TiBaseActivity tiActivity = (TiBaseActivity) this.activity.get();
            if (tiActivity != null) {
                tiActivity.addOnLifecycleEventListener(this);
                tiActivity.addConfigurationChangedListener(this);
            }
        }
    }
    
    @Override
    public void releaseViews(boolean activityFinishing)
    {
        if (this.activity != null) {
            TiBaseActivity tiActivity = (TiBaseActivity) this.activity.get();
            if (tiActivity != null) {
                tiActivity.removeOnLifecycleEventListener(this);
                tiActivity.removeConfigurationChangedListener(this);
            }
        }
        super.releaseViews(activityFinishing);
    }
    
    
    private AdView getAdView() {
        if (view != null) {
            return ((AdmobView)view).adView;
        }
        return null;
    }

    @Override
    public void onDestroy(Activity activity) {
        AdView adView = getAdView();
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onPause(Activity activity) {
        AdView adView = getAdView();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    public void onResume(Activity activity) {
        AdView adView = getAdView();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onStart(Activity activity) {
    }

    @Override
    public void onStop(Activity activity) {
    }

    @Override
    public void onCreate(Activity arg0, Bundle arg1) {

    }
    

    @Override
    public void onConfigurationChanged(TiBaseActivity activity, Configuration configuration) {
        AdView adView = getAdView();
        if (adView != null && getProperty(AkylasAdmobModule.PROPERTY_ADSIZE).equals("smartBanner")) {
            ((AdmobView)view).createAdView();
        }
    }

}
