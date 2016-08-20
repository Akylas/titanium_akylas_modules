package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.view.HorizontalBarChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="HorizontalBarChart")
public class HorizontalBarChartProxy extends BarChartProxy {
    @Override
    public TiUIView createView(Activity activity) {
        return new HorizontalBarChartView(this);
    }
}
