
package akylas.shapes;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.animation.TiAnimatorSet;
import org.appcelerator.titanium.util.TiConvert;

import com.nineoldandroids.animation.PropertyValuesHolder;

@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
	AkylasShapesModule.PROPERTY_SWEEPANGLE,
	AkylasShapesModule.PROPERTY_STARTANGLE
})
public class ArcProxy extends ShapeProxy{
	// Standard Debugging variables
	private static final String TAG = "ArcProxy";
	
	public ArcProxy() {
		super();
		pathable = new Arc();
	}
	
	public ArcProxy(TiContext context) {
		this();
	}

	@Override
	public void processProperties(KrollDict properties) {
		if (properties.containsKey(AkylasShapesModule.PROPERTY_SWEEPANGLE)) {
			((Arc) pathable).setSweepAngle(properties.getFloat(AkylasShapesModule.PROPERTY_SWEEPANGLE));
		}
		if (properties.containsKey(AkylasShapesModule.PROPERTY_STARTANGLE)) {
			((Arc) pathable).setStartAngle(properties.getFloat(AkylasShapesModule.PROPERTY_STARTANGLE));
		}
        super.processProperties(properties);
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) {
		if (key.equals(AkylasShapesModule.PROPERTY_SWEEPANGLE)) {
			setSweepAngle(TiConvert.toFloat(newValue));
		}
		else if (key.equals(AkylasShapesModule.PROPERTY_STARTANGLE)) {
			setStartAngle(TiConvert.toFloat(newValue));
		}
		else super.propertyChanged(key, oldValue, newValue, proxy);
		redraw();
	}
	
	@Override
	protected void preparePropertiesSet(TiAnimatorSet tiSet,
			List<PropertyValuesHolder> propertiesList,
			List<PropertyValuesHolder> propertiesListReverse,
			KrollDict animOptions) {
		super.preparePropertiesSet(tiSet, propertiesList, propertiesListReverse, animOptions);
		
		createAnimForFloat(AkylasShapesModule.PROPERTY_SWEEPANGLE, animOptions, properties, propertiesList, propertiesListReverse, 0.0f);
		createAnimForFloat(AkylasShapesModule.PROPERTY_STARTANGLE, animOptions, properties, propertiesList, propertiesListReverse, 0.0f);
	}
	
	public void setStartAngle(float value) {
		((Arc) pathable).setStartAngle(value);
	}
	public float getStartAngle() {
		return((Arc) pathable).startAngle;
	}
	
	public void setSweepAngle(float value) {
		((Arc) pathable).setSweepAngle(value);
	}
	public float getSweepAngle() {
		return((Arc) pathable).sweepAngle;
	}
}