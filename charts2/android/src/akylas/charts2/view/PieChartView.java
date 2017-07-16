package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;

import android.app.Activity;
import android.view.View;

public class PieChartView extends PieRadarChartViewBase {
    public PieChartView(TiViewProxy proxy) {
        super(proxy);
    }
    
    public PieChart getChart() {
        return (PieChart)nativeView;
    }
    
    @Override
    protected View newChartView(Activity activity) {
        return new PieChart(activity);
    }
    
    @Override
   public XAxis getXAxis() {
        return null;
    }    
    
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "transparentCircleColor":
            getChart().setTransparentCircleColor(TiConvert.toColor(newValue));
            break;
        case "holeColor":
            getChart().setHoleColor(TiConvert.toColor(newValue));
            break;
        case "drawSlicesUnderHole":
            getChart().setDrawSlicesUnderHole(TiConvert.toBoolean(newValue));
            break;        
        case "drawHole":
            getChart().setDrawHoleEnabled(TiConvert.toBoolean(newValue));
            break;        
        case "drawCenterText":
            getChart().setDrawCenterText(TiConvert.toBoolean(newValue));
            break;        
        case "drawSliceText":
        case "drawEntryLabels":
            getChart().setDrawEntryLabels(TiConvert.toBoolean(newValue));
            break;        
        case "centerText":
            getChart().setCenterText(TiConvert.toString(newValue));
            break;        
        case "holeRadiusPercent":
            getChart().setHoleRadius(TiConvert.toFloat(newValue));
            break;        
        case "transparentCircleRadiusPercent":
            getChart().setTransparentCircleRadius(TiConvert.toFloat(newValue));
            break;        
        case "centerTextRadiusPercent":
            getChart().setCenterTextRadiusPercent(TiConvert.toFloat(newValue));
            break;        
        case "usePercentValues":
            getChart().setUsePercentValues(TiConvert.toBoolean(newValue));
            break;        
        case "maxAngle":
            getChart().setMaxAngle(TiConvert.toFloat(newValue));
            break;              
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
