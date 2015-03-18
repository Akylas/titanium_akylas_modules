package akylas.itinerarium.sas;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.smartadserver.android.library.ui.SASAdView;

public abstract class SASView extends TiUIView {
	private SASAdView mAdView;
	public SASView(TiViewProxy proxy) {
		super(proxy);
		Activity activity = proxy.getActivity();
		mAdView  = createView(activity);
		FrameLayout layout = new FrameLayout(activity);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		layout.addView(mAdView);
		setNativeView(layout);
	}
	
	public SASAdView getAdView()
	{
		return mAdView;
	}

	@Override
	public void processProperties(KrollDict d)
	{
//		SASAdView adView = getAdView();
		super.processProperties(d);
		
	}
	public abstract SASAdView createView(Context context);
	/**
	 * Overriden to clean up SASAdView instances. This must be done to avoid IntentReceiver leak.
	 */
	@Override
	public void release() {
		mAdView.onDestroy();
		super.release();
	}
}


