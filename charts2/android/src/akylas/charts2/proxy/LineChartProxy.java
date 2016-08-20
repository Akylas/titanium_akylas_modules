package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.LineChartDataProxy;
import akylas.charts2.view.LineChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="LineChart")
public class LineChartProxy extends BarLineChartViewBaseProxy {

    @Override
    protected Class dataClass() {
        return LineChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new LineChartView(this);
    }

}
