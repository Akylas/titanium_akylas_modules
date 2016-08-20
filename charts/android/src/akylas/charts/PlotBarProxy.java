package akylas.charts;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;

import android.graphics.Color;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.LineAndPointFormatter;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class PlotBarProxy extends XYSerieProxy {
	// Standard Debugging variables
	private static final String TAG = "PlotStepProxy";
	public PlotBarProxy() {
		super();
	}
	
    @Override
    protected LineAndPointFormatter createFormatter() {
        return new BarFormatter(Color.BLACK, Color.TRANSPARENT);
    }
}