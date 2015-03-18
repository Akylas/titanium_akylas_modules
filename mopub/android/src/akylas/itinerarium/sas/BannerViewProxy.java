package akylas.itinerarium.sas;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

@Kroll.proxy(name = "BannerView", creatableInModule=SASModule.class)
public class BannerViewProxy extends SASProxy {
	@Override
	public SASView createSASView(Activity activity)
	{
		return new BannerView(this);
	}
}
