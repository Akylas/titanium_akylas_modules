package akylas.itinerarium.sas;

import org.appcelerator.kroll.annotations.Kroll;

import android.app.Activity;

@Kroll.proxy(name = "InterstitialView", creatableInModule=SASModule.class)
public class InterstitialViewProxy extends SASProxy {
	@Override
	public SASView createSASView(Activity activity)
	{
		return new InterstitialView(this);
	}
}