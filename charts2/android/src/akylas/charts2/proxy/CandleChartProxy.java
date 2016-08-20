package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.CandleChartDataProxy;
import akylas.charts2.view.CandleStickChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="CandleChart")
public class CandleChartProxy extends ChartBaseViewProxy {

    @Override
    protected Class dataClass() {
        return CandleChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new CandleStickChartView(this);
    }

}
