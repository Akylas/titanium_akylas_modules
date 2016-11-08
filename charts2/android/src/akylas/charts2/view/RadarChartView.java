package akylas.charts2.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.YAxis;

import akylas.charts2.Charts2Module;
import akylas.charts2.proxy.RadarChartProxy;
import android.app.Activity;
import android.view.View;

public class RadarChartView extends PieRadarChartViewBase {
    public RadarChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public RadarChart getChart() {
        return (RadarChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new RadarChart(activity);
    }
    
    public YAxis getYAxis() {
        return getChart().getYAxis();
    }
    
    protected static final ArrayList<String> KEY_SEQUENCE;

    static {
        ArrayList<String> tmp = BaseChart.KEY_SEQUENCE;
        tmp.add("yAxis");
        KEY_SEQUENCE = tmp;
    }

    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "webColor":
            getChart().setWebColor(TiConvert.toColor(newValue));
            break;
        case "innerWebColor":
            getChart().setWebColorInner(TiConvert.toColor(newValue));
            break;
        case "drawWeb":
            getChart().setDrawWeb(TiConvert.toBoolean(newValue));
            break;        
        case "webLineWidth":
            getChart().setWebLineWidth(Charts2Module.getInDp(newValue));
            break;        
        case "innerWebLineWidth":
            getChart().setWebLineWidthInner(Charts2Module.getInDp(newValue));
            break;        
        case "webAlpha":
            getChart().setWebAlpha((int)(TiConvert.toFloat(newValue) * 255));
            break;        
        case "skipWebLineCount":
            getChart().setSkipWebLineCount(TiConvert.toInt(newValue));
            break;  
        case "yAxis":
            if (newValue instanceof HashMap) {
                ((RadarChartProxy) proxy).setYAxis((HashMap) newValue);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
