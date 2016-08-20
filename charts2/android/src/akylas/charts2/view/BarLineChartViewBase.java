package akylas.charts2.view;

import java.util.HashMap;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.YAxis;

import akylas.charts2.Charts2Module;
import akylas.charts2.proxy.BarLineChartViewBaseProxy;

public class BarLineChartViewBase extends BaseChart {

    public BarLineChartViewBase(TiViewProxy proxy) {
        super(proxy);
    }
    @Override
    protected void prepareForReuse() {
        getChart().fitScreen();
        super.prepareForReuse(); 
    }
    
    public BarLineChartBase getChart() {
        return (BarLineChartBase)nativeView;
    }
    
    public YAxis getLeftAxis() {
        return getChart().getAxisLeft();
    }
    
    public YAxis getRightAxis() {
        return getChart().getAxisRight();
    }
    
    @Override
    public void notifyDataSetChanged() {
        getChart().notifyDataSetChanged();
        getChart().postInvalidate();
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "drag":
            getChart().setDragEnabled(TiConvert.toBoolean(newValue));
            break;
        case "scale":
            getChart().setScaleEnabled(TiConvert.toBoolean(newValue));
            break;
        case "scaleX":
            getChart().setScaleXEnabled(TiConvert.toBoolean(newValue));
            break;
        case "scaleY":
            getChart().setScaleYEnabled(TiConvert.toBoolean(newValue));
            break;
        case "doubleTapToZoom":
            getChart().setDoubleTapToZoomEnabled(TiConvert.toBoolean(newValue));
            break;
        case "autoScaleMinMax":
            getChart().setAutoScaleMinMaxEnabled(TiConvert.toBoolean(newValue));
            break;
        case "highlightPerDrag":
            getChart().setHighlightPerDragEnabled(TiConvert.toBoolean(newValue));
            break;
        case "drawGridBackground":
            getChart().setDrawGridBackground(TiConvert.toBoolean(newValue));
            break;
        case "drawBorders":
            getChart().setDrawBorders(TiConvert.toBoolean(newValue));
            break;
        case "minOffset":
            getChart().setMinOffset(TiConvert.toFloat(newValue));
            break;
        case "gridBackgroundColor":
            getChart().setGridBackgroundColor(TiConvert.toColor(newValue));
            break;
        case "borderColor":
            getChart().setBorderColor(TiConvert.toColor(newValue));
            break;
        case "borderLineWidth":
            getChart().setBorderWidth(Charts2Module.getInDp(newValue));
            break;
        case "maxVisibleValueCount":
            getChart().setMaxVisibleValueCount(TiConvert.toInt(newValue));
            break;
        case "leftAxis":
            if (newValue instanceof HashMap) {
                ((BarLineChartViewBaseProxy) proxy).setLeftAxis((HashMap) newValue);
            }
            break;
        case "rightAxis":
            if (newValue instanceof HashMap) {
                ((BarLineChartViewBaseProxy) proxy).setRightAxis((HashMap) newValue);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
    
//    @Override
//    public void setReusing(boolean value) {
//        super.setReusing(value);
//        if (!value) {   
//            getData().notifyDataChanged();
//            notifyDataSetChanged();
//        }
//    }
}
