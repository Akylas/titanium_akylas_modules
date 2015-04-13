package akylas.map;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

@Kroll.proxy
public class CalloutReusableProxy extends TiReusableProxy {
    
    KrollProxy mParentProxy;
    
    public void setParentProxy(KrollProxy proxy)
    {
        mParentProxy = proxy;
    }
    
    @Override
    public KrollProxy getParentForBubbling()
    {
        return mParentProxy;
    }

}
