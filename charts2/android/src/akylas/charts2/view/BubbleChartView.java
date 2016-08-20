package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;

import com.github.mikephil.charting.charts.BubbleChart;

import android.app.Activity;
import android.view.View;

public class BubbleChartView extends BarLineChartViewBase {
    public BubbleChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public BubbleChart getChart() {
        return (BubbleChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new BubbleChart(activity);
    }
}
