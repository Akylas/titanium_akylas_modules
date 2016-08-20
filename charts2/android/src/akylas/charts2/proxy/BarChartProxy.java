package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.BarChartDataProxy;
import akylas.charts2.view.BarChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name = "BarChart")
public class BarChartProxy extends ChartBaseViewProxy {

    @Override
    protected Class dataClass() {
        return BarChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new BarChartView(this);
    }

}
