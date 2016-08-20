package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.ScatterChartDataProxy;
import akylas.charts2.view.ScatterChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="ScatterChart")
public class ScatterChartProxy extends BarLineChartViewBaseProxy {

    @Override
    protected Class dataClass() {
        return ScatterChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new ScatterChartView(this);
    }

}
