
package akylas.shapes;

import org.appcelerator.kroll.annotations.Kroll;

@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
})
public class RectProxy extends ShapeProxy{
	// Standard Debugging variables
	private static final String TAG = "RectProxy";	
	
	public RectProxy() {
		super();
		pathable = new PRect();
	}
}