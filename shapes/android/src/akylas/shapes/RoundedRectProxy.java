
package akylas.shapes;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;


@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
})
public class RoundedRectProxy extends ShapeProxy{
	// Standard Debugging variables
	private static final String TAG = "RoundedRectProxy";	
	
	public RoundedRectProxy() {
		super();
		pathable = new PRoundRect();
	}
	
	public RoundedRectProxy(TiContext context) {
		this();
	}

	@Override
	public void processProperties(KrollDict properties) {
		super.processProperties(properties);
		if (properties.containsKey(AkylasShapesModule.PROPERTY_CORNERRADIUS)) {
			((PRoundRect) pathable).setCornerRadius(properties.get(AkylasShapesModule.PROPERTY_CORNERRADIUS));
		}
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) {
		if (key.equals(AkylasShapesModule.PROPERTY_CORNERRADIUS)) {
			((PRoundRect) pathable).setCornerRadius(newValue);
		}
		else super.propertyChanged(key, oldValue, newValue, proxy);
		redraw();
	}

}