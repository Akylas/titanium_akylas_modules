package akylas.itinerarium.sas;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ViewGroup;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.ui.SASAdView;

public class BannerView extends SASView {

	private static final String TAG = "BannerView";
	public BannerView(TiViewProxy proxy) {
		super(proxy);
	}

	@Override
	public SASAdView createView(Context context) {
		SASAdView view = new SASBannerView(context) {
			@Override
			protected void onAttachedToWindow() {
				Log.d(TAG, "onAttachedToWindow");
				super.onAttachedToWindow();
			}
			
			@Override
			protected void onDetachedFromWindow() {
				Log.d(TAG, "onDetachedFromWindow");
				super.onDetachedFromWindow();
			}
			
			@Override
			protected void onConfigurationChanged(Configuration newConfig) {
				Log.d(TAG, "onConfigurationChanged");
				super.onConfigurationChanged(newConfig);
			}
			
			@Override
			protected void onLayout(boolean changed, int l, int t, int r, int b) {
				Log.d(TAG, "onLayout");
				super.onLayout(changed, l, t, r, b);
			}
		};
		return view;
	}

}
