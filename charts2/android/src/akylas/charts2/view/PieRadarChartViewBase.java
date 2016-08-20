package akylas.charts2.view;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.charts.PieRadarChartBase;

public class PieRadarChartViewBase extends BaseChart {

    public PieRadarChartViewBase(TiViewProxy proxy) {
        super(proxy);
    }
    
    public PieRadarChartBase getChart() {
        return (PieRadarChartBase)nativeView;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "rotation":
            getChart().setRotationEnabled(TiConvert.toBoolean(newValue));
            break;
        case "minOffset":
            getChart().setMinOffset(TiConvert.toFloat(newValue));
            break;
        case "rotationAngle":
            getChart().setRotationAngle(TiConvert.toFloat(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
