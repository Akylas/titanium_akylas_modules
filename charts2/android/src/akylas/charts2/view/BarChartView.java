package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.BarChart;

import android.app.Activity;
import android.view.View;

public class BarChartView extends BarLineChartViewBase {

    public BarChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public BarChart getChart() {
        return (BarChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new BarChart(activity);
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "drawBarShadow":
            getChart().setDrawBarShadow(TiConvert.toBoolean(newValue));
            break;
        case "drawValueAboveBar":
            getChart().setDrawValueAboveBar(TiConvert.toBoolean(newValue));
            break;        
        case "highlightFullBar":
            getChart().setHighlightFullBarEnabled(TiConvert.toBoolean(newValue));
            break;  
        case "fitBars":
            getChart().setFitBars(TiConvert.toBoolean(newValue));
            break;  
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
