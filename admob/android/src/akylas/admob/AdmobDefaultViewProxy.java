package akylas.admob;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;


@Kroll.proxy()
public abstract class AdmobDefaultViewProxy extends TiViewProxy {
	private static final String TAG = "AdMobViewProxy";

	public AdmobDefaultViewProxy() {
		super();
		defaultValues.put(AkylasAdmobModule.PROPERTY_ADUNITID, AkylasAdmobModule.PUBLISHER_ID);
		defaultValues.put(AkylasAdmobModule.PROPERTY_ADSIZE, "smartBanner");
	}
	

	@Override
	protected KrollDict getLangConversionTable() {
		KrollDict table = new KrollDict();
		table.put(TiC.PROPERTY_TITLE, "titleid");
		return table;
	}
}
