
package akylas.udp;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;

@Kroll.module(name = "AkylasUdp", id = "akylas.udp")
public class AkylasUdpModule extends KrollModule {
	public AkylasUdpModule() {
		super();
	}
	
	static public Activity getCurrentOrRootActivity() {
        Activity activity = TiApplication.getAppCurrentActivity();
        if (activity == null) {
            return TiApplication.getAppRootOrCurrentActivity();
        }
        return activity;
    }
}
