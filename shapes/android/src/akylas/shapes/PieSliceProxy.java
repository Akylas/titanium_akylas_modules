
package akylas.shapes;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.animation.TiAnimatorSet;

import android.animation.PropertyValuesHolder;

import android.graphics.Point;

@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
	AkylasShapesModule.PROPERTY_INNERRADIUS
})
public class PieSliceProxy extends ArcProxy{
	// Standard Debugging variables
	private static final String TAG = "PieSliceProxy";
	protected Object innerRadius = new TiPoint(0, 0);;
	
	public PieSliceProxy() {
		super();
		pathable = new PieSlice();
	}

	@Override
	protected void updatePath() {
		int width = currentBounds.width();
		int height = currentBounds.height();
		((PieSlice) pathable).innerRadius = computeRadius(this.innerRadius, width, height);
		super.updatePath();
	}
	
	@Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
            case AkylasShapesModule.PROPERTY_INNERRADIUS:
                this.innerRadius = properties.get(newValue);
        break;

        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
        break;
        }
    }
	
	@Override
	protected void preparePropertiesSet(TiAnimatorSet tiSet,
			List<PropertyValuesHolder> propertiesList,
			List<PropertyValuesHolder> propertiesListReverse,
			HashMap animOptions) {
		super.preparePropertiesSet(tiSet, propertiesList, propertiesListReverse, animOptions);
		
		if (animOptions.containsKey(AkylasShapesModule.PROPERTY_INNERRADIUS)) {
			int width = parentBounds.width();
			int height = parentBounds.height();
			Point currentRadius = computeRadius(this.innerRadius, width, height);
			Point animRadius = computeRadius(animOptions.get(AkylasShapesModule.PROPERTY_INNERRADIUS), width, height);
			PointEvaluator evaluator = new PointEvaluator();
			propertiesList.add(PropertyValuesHolder.ofObject("innereRadius", evaluator, animRadius));
			if (propertiesListReverse != null) {
				propertiesListReverse.add(PropertyValuesHolder.ofObject("innereRadius", evaluator, currentRadius));
			}
		}
	}
	
	//ANIMATION getter/setter
	public void setInnerRadius(Point point) {
		((PieSlice) pathable).innerRadius = point;
	}
	public Point getInnereRadius() {
		return ((PieSlice) pathable).innerRadius;		
	}
}