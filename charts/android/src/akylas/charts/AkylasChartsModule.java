package akylas.charts;

import java.util.HashMap;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.ProtectedModule;
import org.appcelerator.titanium.TiApplication;

import com.androidplot.util.PixelUtils;

import android.graphics.Matrix;

@Kroll.module(name="AkylasCharts", id="akylas.charts")
public class AkylasChartsModule extends ProtectedModule
{

	public static float[] gradientCoordsFromAngle(int width, int height, int angle) {
		float[] result = {0, height/2, width, height/2};
		Matrix matrix = new Matrix();
		matrix.setRotate(-angle, width/2, height/2);
		matrix.mapPoints(result);
	    Log.d(TAG, "gradientCoordsFromAngle " + result[0] + ", " + result[1] + ", " + result[2] + ", " + result[3], Log.DEBUG_MODE);
		return result;
	}
	// Standard Debugging variables
	private static final String TAG = "AkylasChartsModule";
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
	@Kroll.constant public static final int BAR_WIDTH_FIXED = 0;
	@Kroll.constant public static final int BAR_WIDTH_VARIABLE = 1;
	@Kroll.constant public static final int BAR_STYLE_OVERLAID = 0;
	@Kroll.constant public static final int BAR_STYLE_STACKED = 1;
	@Kroll.constant public static final int BAR_STYLE_SIDE_BY_SIDE = 2;
	@Kroll.constant public static final int HORIZONTAL = 0;
	@Kroll.constant public static final int VERTICAL = 1;
//	@Kroll.constant public static final int STYLE_DIAMOND = PointStyle.DIAMOND;
//	@Kroll.constant public static final int STYLE_CIRCLE = PointStyle.CIRCLE;
//	@Kroll.constant public static final int STYLE_POINT = PointStyle.POINT;
//	@Kroll.constant public static final int STYLE_SQUARE = PointStyle.SQUARE;
//	@Kroll.constant public static final int STYLE_TRIANGLE = PointStyle.TRIANGLE;
    public static final String PROPERTY_DASH = "dash";
    public static final String PROPERTY_JOIN = "join";
    public static final String PROPERTY_CAP = "xap";
    public static final String PROPERTY_EMBOSS = "emboss";
    public static final String PROPERTY_SHADOW = "shadow";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_INTERVAL = "interval";
    
    public static final String PROPERTY_LINE_COLOR = "lineColor";
    public static final String PROPERTY_LINE_GRADIENT = "lineGradient";
    public static final String PROPERTY_LINE_IMAGE = "lineImage";
    public static final String PROPERTY_LINE_DASH = "lineDash";
    public static final String PROPERTY_LINE_WIDTH = "lineWidth";
    public static final String PROPERTY_LINE_OPACITY = "lineOpacity";
    public static final String PROPERTY_LINE_JOIN = "lineJoin";
    public static final String PROPERTY_LINE_CAP = "lineCap";
    public static final String PROPERTY_LINE_SHADOW = "lineShadow";
    public static final String PROPERTY_LINE_EMBOSS = "lineEmboss";
    public static final String PROPERTY_LINE_INVERSED = "lineInversed";
    public static final String PROPERTY_FILL_COLOR = "fillColor";
    public static final String PROPERTY_FILL_GRADIENT = "fillGradient";
    public static final String PROPERTY_FILL_IMAGE = "fillImage";
    public static final String PROPERTY_FILL_WIDTH = "fillWidth";
    public static final String PROPERTY_FILL_OPACITY = "fillOpacity";
    public static final String PROPERTY_FILL_SHADOW = "fillShadow";
    public static final String PROPERTY_FILL_EMBOSS = "fillEmboss";
    public static final String PROPERTY_FILL_INVERSED = "fillInversed";
    public static final String PROPERTY_GRID_LINES = "gridLines";
    public static final String PROPERTY_MINOR_TICKS = "minorTicks";
    public static final String PROPERTY_ANGLE = "angle";
    public static final String PROPERTY_ORIGIN = "origin";
    public static final String PROPERTY_ALIGN = "align";
    public static final String PROPERTY_AXIS_Y = "yAxis";
    public static final String PROPERTY_VISIBLE_RANGE = "visibleRane";
    public static final String PROPERTY_INTERVAL_PX = "interval_px";
    public static final String PROPERTY_MAJOR_TICKS = "majorTicks";
    public static final String PROPERTY_GRID_AREA = "gridArea";
    public static final String PROPERTY_FORMAT_CALLBACK = "formatCallback";
    public static final String PROPERTY_LABELS = "labels";
    public static final String PROPERTY_AXIS_X = "xAxis";
    public static final String PROPERTY_LEGEND = "legend";
    public static final String PROPERTY_RANGE_Y = "yRange";
    public static final String PROPERTY_RANGE_X = "xRange";
    public static final String PROPERTY_PLOT_SPACE = "plotSpace";
    public static final String PROPERTY_SCALE_TO_FIT = "scaleToFit";
    public static final String PROPERTY_LOCATIONS = "locations";
    public static final String PROPERTY_PLOT_AREA = "plotArea";
    public static final String PROPERTY_BORDER_OPACITY = "borderOpacity";
    public static final String PROPERTY_SYMBOL = "symbol";
    public static final String PROPERTY_FILL_SPACE_PATH = "fillSpacePath";
    public static final String PROPERTY_FILL_DIRECTION = "fillDirection";


	public AkylasChartsModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		PixelUtils.init(app.getBaseContext());
		HashMap<String, String> map = new HashMap();
        map.put("AkylasCharts.LineChart", akylas.charts.LineChartProxy.class.getName());
        map.put("AkylasCharts.Marker", akylas.charts.MarkerProxy.class.getName());
        map.put("AkylasCharts.PieChart", akylas.charts.PieChartProxy.class.getName());
        map.put("AkylasCharts.PieSegment", akylas.charts.PieSegmentProxy.class.getName());
        map.put("AkylasCharts.PlotBar", akylas.charts.PlotBarProxy.class.getName());
        map.put("AkylasCharts.PlotLine", akylas.charts.PlotLineProxy.class.getName());
        map.put("AkylasCharts.PlotStep", akylas.charts.PlotStepProxy.class.getName());
		APIMap.addMapping(map);
		// put module init code that needs to run when the application is created
	}
    
	@Kroll.onVerifyModule
    public static void onVerifyModule(TiApplication app)
    {
        verifyPassword(app, "akylas.modules.key", AeSimpleSHA1.hexToString("7265745b496b2466553b486f736b7b4f"));
    }
}

