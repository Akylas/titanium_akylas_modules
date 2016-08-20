package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;

import com.github.mikephil.charting.charts.ScatterChart;

import android.app.Activity;
import android.view.View;

public class ScatterChartView extends BarLineChartViewBase {
    public ScatterChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public ScatterChart getChart() {
        return (ScatterChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new ScatterChart(activity);
    }
}
