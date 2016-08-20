package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;

import com.github.mikephil.charting.charts.LineChart;

import android.app.Activity;
import android.view.View;

public class LineChartView extends BarLineChartViewBase {

    public LineChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public LineChart getChart() {
        return (LineChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new LineChart(activity);
    }
}
