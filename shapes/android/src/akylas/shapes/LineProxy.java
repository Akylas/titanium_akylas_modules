
package akylas.shapes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.animation.TiAnimatorSet;

import android.animation.PropertyValuesHolder;
import android.animation.TypeEvaluator;


@Kroll.proxy(creatableInModule = AkylasShapesModule.class, propertyAccessors={
	AkylasShapesModule.PROPERTY_POINTS
})
public class LineProxy extends ArcProxy{
	// Standard Debugging variables
	private static final String TAG = "LineProxy";
	protected ArrayList<TiBezierPoint> points;
	
	private class TiBezierPoint {
		public TiPoint point = null;
		public TiPoint curvePoint1 = null;
		public TiPoint curvePoint2 = null;
		public TiBezierPoint() {
		}
	}
	
	public LineProxy() {
		super();
		pathable = new Line();
		points = new ArrayList<TiBezierPoint>();
		anchor = AnchorPosition.LEFT_MIDDLE;
	}
	
	@Override
	protected void updatePath() {
		int width = currentBounds.width();
		int height = currentBounds.height();
		ArrayList<BezierPoint> realPoints = new ArrayList<BezierPoint>(this.points.size());
		for (int i = 0; i < this.points.size(); i++) {
			TiBezierPoint tiBPoint = this.points.get(i);
			BezierPoint bPoint = new BezierPoint();
			bPoint.point = computePoint(tiBPoint.point, anchor, width, height);
			if (tiBPoint.curvePoint1 != null) bPoint.curvePoint1 = computePoint(tiBPoint.curvePoint1, anchor, width, height);
			if (tiBPoint.curvePoint2 != null) bPoint.curvePoint2 = computePoint(tiBPoint.curvePoint2, anchor, width, height);
			realPoints.add(bPoint);
		}
		((Line) pathable).setPoints(realPoints);
		super.updatePath();
	}
	
	private void setPointsFromObject(Object[] obj) {
		this.points.clear();
		if (obj == null) return;
		for (int i = 0; i < obj.length; i++) {
			Object[] pointArray = (Object[]) obj[i];
			if (pointArray == null || pointArray.length < 2) continue;
			TiBezierPoint tiBPoint = new TiBezierPoint();
			tiBPoint.point = new TiPoint(pointArray[0], pointArray[1]);
			if (pointArray.length >= 4) {
				tiBPoint.curvePoint1 = new TiPoint(pointArray[2], pointArray[3]);
				if (pointArray.length >= 6) {
					tiBPoint.curvePoint2 = new TiPoint(pointArray[4], pointArray[5]);
				}
			}
			this.points.add(tiBPoint);
		}
	}
	
	private ArrayList<BezierPoint> getRealPointsFromObject(Object[] obj, int width, int height) {
		if (obj == null) return null;
		ArrayList<BezierPoint> result = new ArrayList<BezierPoint>();
		for (int i = 0; i < obj.length; i++) {
			Object[] pointArray = (Object[]) obj[i];
			if (pointArray == null || pointArray.length < 2) continue;
			BezierPoint bPoint = new BezierPoint();
			bPoint.point = computePoint(new TiPoint(pointArray[0], pointArray[1]), anchor, width, height);
			if (pointArray.length >= 4) {
				bPoint.curvePoint1 = computePoint(new TiPoint(pointArray[2], pointArray[3]), anchor, width, height);
				if (pointArray.length >= 6) {
					bPoint.curvePoint2 = computePoint(new TiPoint(pointArray[4], pointArray[5]), anchor, width, height);
				}
			}
			result.add(bPoint);
		}
		return result;
	}
	
	@Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
            case AkylasShapesModule.PROPERTY_POINTS:
                setPointsFromObject((Object[]) newValue);
        break;

        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
        break;
        }
    }
	
	public class BezierPointsEvaluator implements TypeEvaluator<ArrayList<BezierPoint>> {
		
		public BezierPointsEvaluator() {
		}
		
		public ArrayList<BezierPoint> evaluate(float fraction, ArrayList<BezierPoint> startValue,
				ArrayList<BezierPoint> endValue) {
			ArrayList<BezierPoint> result = new ArrayList<BezierPoint>();
			if (startValue == null) {
				for (int i = 0; i < endValue.size(); i++) {
					BezierPoint point = endValue.get(i);
					result.add(point.fraction(fraction));
				}
			}
			else if (endValue.size() != startValue.size()) return endValue;
			else {
				for (int i = 0; i < endValue.size(); i++) {
					BezierPoint startPoint = startValue.get(i);
					BezierPoint endPoint = endValue.get(i);
					result.add(startPoint.fraction(fraction, endPoint));
				}
			}
			return result;
		}

	}
	
	@Override
	protected void preparePropertiesSet(TiAnimatorSet tiSet,
			List<PropertyValuesHolder> propertiesList,
			List<PropertyValuesHolder> propertiesListReverse,
			HashMap animOptions) {
		super.preparePropertiesSet(tiSet, propertiesList, propertiesListReverse, animOptions);
		
		if (animOptions.containsKey(AkylasShapesModule.PROPERTY_POINTS)) {
			
			int width = currentBounds.width();
			int height = currentBounds.height();
			
			ArrayList<BezierPoint> realPoints = getRealPointsFromObject((Object[]) animOptions.get(AkylasShapesModule.PROPERTY_POINTS), width, height);
			BezierPointsEvaluator evaluator =  new BezierPointsEvaluator();
			propertiesList.add(PropertyValuesHolder.ofObject("points", evaluator, realPoints));
			if (propertiesListReverse != null) {
				propertiesListReverse.add(PropertyValuesHolder.ofObject("points", evaluator, getPoints()));
			}
		}
	}
	
	//ANIMATION getter/setter
	public void setPoints(ArrayList<BezierPoint> points) {
		((Line) pathable).setPoints(points);
	}
	public ArrayList<BezierPoint> getPoints() {
		return ((Line) pathable).getPoints();		
	}
}