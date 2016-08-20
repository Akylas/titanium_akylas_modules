package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;

import android.app.Activity;
import android.view.View;

public class CombinedChartView extends BarLineChartViewBase {
    public CombinedChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public CombinedChart getChart() {
        return (CombinedChart)nativeView;
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
        case "drawOrder":
            if (newValue instanceof Object[]) {
                Object[] inArray = (Object[]) newValue;
                DrawOrder[] outArray = new DrawOrder[inArray.length];
                for (int i = 0; i < inArray.length; i++) {
                    outArray[i] = DrawOrder.values()[((Number) inArray[i]).intValue()];
                }
                getChart().setDrawOrder(outArray);
            }
            break;        
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
