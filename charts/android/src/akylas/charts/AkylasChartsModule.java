package akylas.charts;

import java.util.HashMap;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.APIMap;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import com.androidplot.util.PixelUtils;

import android.graphics.Matrix;

@Kroll.module(name="AkylasCharts", id="akylas.charts")
public class AkylasChartsModule extends KrollModule
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
}

