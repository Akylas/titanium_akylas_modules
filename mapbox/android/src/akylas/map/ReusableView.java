package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

public class ReusableView extends TiUIView {
    
    public ReusableView(TiReusableProxy proxy) {
        super(proxy);
        TiCompositeLayout view = new TiCompositeLayout(proxy.getActivity());
        view.setView(this);
        setNativeView(view);
        view.setFocusable(false);
    }
    
    public void processProperties(KrollDict d) {
        super.processProperties(d);
    }
    
    public String getReusableIdentifier() {
        return ((TiReusableProxy)proxy).getReusableIdentifier();
    }
}
