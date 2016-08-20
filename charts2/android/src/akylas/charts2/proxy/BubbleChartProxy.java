package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.BubbleChartDataProxy;
import akylas.charts2.view.BubbleChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="BubbleChart", propertyAccessors = {
        "drawBarShadow", "drawValueAboveBar", "highlightFullBar", "fitBars" })
public class BubbleChartProxy extends ChartBaseViewProxy {

    @Override
    protected Class dataClass() {
        return BubbleChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new BubbleChartView(this);
    }

}
