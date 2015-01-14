package akylas.shapes;

import java.util.HashMap;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.APIMap;


@Kroll.module(name="AkylasShapes", id="akylas.shapes")
public class AkylasShapesModule extends KrollModule
{
	private static final String TAG = "ShapesModule";
	
	static final String PROPERTY_RADIUS = "radius";
	static final String PROPERTY_ANCHOR = "anchor";
	static final String PROPERTY_SWEEPANGLE = "sweepAngle";
	static final String PROPERTY_STARTANGLE = "startAngle";
	static final String PROPERTY_LINE_COLOR = "lineColor";
	static final String PROPERTY_LINE_GRADIENT = "lineGradient";
	static final String PROPERTY_LINE_IMAGE = "lineImage";
	static final String PROPERTY_LINE_DASH = "lineDash";
	static final String PROPERTY_LINE_WIDTH = "lineWidth";
	static final String PROPERTY_LINE_OPACITY = "lineOpacity";
	static final String PROPERTY_LINE_JOIN = "lineJoin";
	static final String PROPERTY_LINE_CAP = "lineCap";
	static final String PROPERTY_LINE_SHADOW = "lineShadow";
	static final String PROPERTY_LINE_EMBOSS = "lineEmboss";
	static final String PROPERTY_LINE_INVERSED = "lineInversed";
	static final String PROPERTY_FILL_COLOR = "fillColor";
	static final String PROPERTY_FILL_GRADIENT = "fillGradient";
	static final String PROPERTY_FILL_IMAGE = "fillImage";
	static final String PROPERTY_FILL_WIDTH = "fillWidth";
	static final String PROPERTY_FILL_OPACITY = "fillOpacity";
	static final String PROPERTY_FILL_SHADOW = "fillShadow";
	static final String PROPERTY_FILL_EMBOSS = "fillEmboss";
	static final String PROPERTY_FILL_INVERSED = "fillInversed";
	static final String PROPERTY_OPERATIONS = "operations";
	static final String PROPERTY_CORNERRADIUS = "cornerRadius";
	static final String PROPERTY_LINE_CLIPPED = "lineClipped";
	static final String PROPERTY_INNERRADIUS = "innerRadius";
	static final String PROPERTY_SHAPES = "shapes";
	static final String PROPERTY_POINTS = "points";

	
	
	@Kroll.constant public static final int ALIGNMENT_LEFT = 0;
	@Kroll.constant public static final int ALIGNMENT_CENTER = 1;
	@Kroll.constant public static final int ALIGNMENT_RIGHT = 2;
	@Kroll.constant public static final int ALIGNMENT_TOP = 3;
	@Kroll.constant public static final int ALIGNMENT_MIDDLE = 4;
	@Kroll.constant public static final int ALIGNMENT_BOTTOM = 5;
	@Kroll.constant public static final int LOCATION_TOP = 0;
	@Kroll.constant public static final int LOCATION_BOTTOM = 7;
	@Kroll.constant public static final int LOCATION_LEFT = 2;
	@Kroll.constant public static final int LOCATION_RIGHT = 5;


	
	@Kroll.constant public static final int CAP_BUTT = 0;
	@Kroll.constant public static final int CAP_ROUND = 1;
	@Kroll.constant public static final int CAP_SQUARE = 2;
	@Kroll.constant public static final int JOIN_MITER = 0;
	@Kroll.constant public static final int JOIN_ROUND = 1;
	@Kroll.constant public static final int JOIN_BEVEL = 2;

	@Kroll.constant public static final int HORIZONTAL = 0;
	@Kroll.constant public static final int VERTICAL = 1;
	
	@Kroll.constant public static final int CW = 0;
	@Kroll.constant public static final int CCW = 1;
	
	@Kroll.constant public static final int TOP_MIDDLE = 0;
	@Kroll.constant public static final int LEFT_TOP = 1;
	@Kroll.constant public static final int LEFT_MIDDLE = 2;
	@Kroll.constant public static final int LEFT_BOTTOM = 3;
	@Kroll.constant public static final int RIGHT_TOP = 4;
	@Kroll.constant public static final int RIGHT_MIDDLE = 5;
	@Kroll.constant public static final int RIGHT_BOTTOM = 6;
	@Kroll.constant public static final int BOTTOM_MIDDLE = 7;
	@Kroll.constant public static final int CENTER = 8;
	
	public AkylasShapesModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
	       HashMap<String, String> map = new HashMap();
	        map.put("AkylasShapes.Arc", akylas.shapes.ArcProxy.class.getName());
	        map.put("AkylasShapes.Circle", akylas.shapes.CircleProxy.class.getName());
	        map.put("AkylasShapes.Line", akylas.shapes.LineProxy.class.getName());
	        map.put("AkylasShapes.PieSlice", akylas.shapes.PieSliceProxy.class.getName());
	        map.put("AkylasShapes.Rect", akylas.shapes.RectProxy.class.getName());
	        map.put("AkylasShapes.RoundedRect", akylas.shapes.RoundedRectProxy.class.getName());
	        map.put("AkylasShapes.View", akylas.shapes.ShapeViewProxy.class.getName());
	        APIMap.addMapping(map);
	}
}

