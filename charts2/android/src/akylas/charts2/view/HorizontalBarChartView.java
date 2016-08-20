package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;

import com.github.mikephil.charting.charts.HorizontalBarChart;

import android.app.Activity;
import android.view.View;

public class HorizontalBarChartView extends BarChartView {
    
    public HorizontalBarChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public HorizontalBarChart getChart() {
        return (HorizontalBarChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new HorizontalBarChart(activity);
    }
}
