package akylas.map;

import akylas.map.AnnotationProxy;

abstract class AkylasMarker  {
    protected AnnotationProxy proxy;
	
	public AkylasMarker(final AnnotationProxy p) {
        proxy = p;
    }
		
//	private void setProxy(AnnotationProxy proxy) {
//		this.proxy = proxy;
//	}
	
	public AnnotationProxy getProxy() {
		return proxy;
	}
    
    abstract double getLatitude();
    abstract double getLongitude();
    abstract double getAltitude();
    
    
    abstract void showInfoWindow();

    abstract void hideInfoWindow();
    abstract void setPosition(double latitude, double longitude);
}
