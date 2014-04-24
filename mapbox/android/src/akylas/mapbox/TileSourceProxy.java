package akylas.mapbox;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;

import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;

@Kroll.proxy(creatableInModule=AkylasMapboxModule.class, propertyAccessors = {
	TiC.PROPERTY_SOURCE
})
public class TileSourceProxy extends KrollProxy {
	private TileLayer mLayer;
	
	public TileSourceProxy()
	{
		super();
	}

	public TileSourceProxy(TiContext tiContext)
	{
		this();
	}
	
	@Override
	public void handleCreationDict(KrollDict dict) {
		super.handleCreationDict(dict);
		if (dict.containsKey(TiC.PROPERTY_SOURCE)) {
			mLayer = AkylasMapboxModule.tileSourceFromObject (dict.get(TiC.PROPERTY_SOURCE));
		}
	}
	
	public TileLayer getLayer() {
		return mLayer;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public float getMinZoomLevel() {
		if (mLayer != null) {
			return mLayer.getMinimumZoomLevel();
		}
		return 0;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public float getMaxZoomLevel() {
		if (mLayer != null) {
			return mLayer.getMaximumZoomLevel();
		}
		return 0;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getRegion() {
		if (mLayer != null) {
			return AkylasMapboxModule.regionToDict(mLayer.getBoundingBox());
		}
		return null;
	}
}
