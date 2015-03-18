package akylas.itinerarium.sas;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.content.Context;
import android.view.ViewGroup;

import com.smartadserver.android.library.SASInterstitialView;
import com.smartadserver.android.library.ui.SASAdView;

public class InterstitialView  extends SASView {

	public InterstitialView(TiViewProxy proxy) {
		super(proxy);
	}

	@Override
	public SASAdView createView(Context context) {
		SASAdView view = new SASInterstitialView(context);
		return view;
	}

}

