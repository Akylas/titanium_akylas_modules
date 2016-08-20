
package akylas.shapes;

import org.appcelerator.kroll.annotations.Kroll;


@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
})
public class RoundedRectProxy extends ShapeProxy{
	// Standard Debugging variables
	private static final String TAG = "RoundedRectProxy";	
	
	public RoundedRectProxy() {
		super();
		pathable = new PRoundRect();
	}

	@Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
            case AkylasShapesModule.PROPERTY_CORNERRADIUS:
                ((PRoundRect) pathable).setCornerRadius(newValue);
        break;

        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
        break;
        }
    }

}