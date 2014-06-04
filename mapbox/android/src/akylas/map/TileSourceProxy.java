package akylas.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;

import com.mapbox.mapboxsdk.tileprovider.tilesource.TileLayer;

@Kroll.proxy(creatableInModule=AkylasMapModule.class, propertyAccessors = {
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
			mLayer = AkylasMapModule.tileSourceFromObject(this, dict.get(TiC.PROPERTY_SOURCE));
		}
	}
	
	public TileLayer getLayer() {
		return mLayer;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public float getMinZoom() {
		if (mLayer != null) {
			return mLayer.getMinimumZoomLevel();
		}
		return 0;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public float getMaxZoom() {
		if (mLayer != null) {
			return mLayer.getMaximumZoomLevel();
		}
		return 0;
	}
	
	@Kroll.method
	@Kroll.getProperty
	public KrollDict getRegion() {
		if (mLayer != null) {
			return AkylasMapModule.regionToDict(mLayer.getBoundingBox());
		}
		return null;
	}
}
