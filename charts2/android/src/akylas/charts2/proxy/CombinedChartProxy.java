package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.CombinedChartDataProxy;
import akylas.charts2.view.CombinedChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="CombinedChart")
public class CombinedChartProxy extends ChartBaseViewProxy {

    @Override
    protected Class dataClass() {
        return CombinedChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new CombinedChartView(this);
    }

}
