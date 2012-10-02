
package akylas.udp;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;

@Kroll.module(name = "AkylasUdp", id = "akylas.udp")
public class AkylasUdpModule extends KrollModule {
	public AkylasUdpModule(TiContext tiContext) {
		super(tiContext);
	}
	public AkylasUdpModule() {
		super();
	}
}
