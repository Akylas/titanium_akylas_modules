package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;

import com.github.mikephil.charting.charts.CandleStickChart;

import android.app.Activity;
import android.view.View;

public class CandleStickChartView extends BarLineChartViewBase {
    public CandleStickChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public CandleStickChart getChart() {
        return (CandleStickChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new CandleStickChart(activity);
    }
}
