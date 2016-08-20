
package akylas.shapes;

import org.appcelerator.kroll.annotations.Kroll;

@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
})
public class CircleProxy extends ShapeProxy{
	// Standard Debugging variables
	private static final String TAG = "CircleProxy";	
	
	public CircleProxy() {
		super();
		pathable = new Circle();
	}
}