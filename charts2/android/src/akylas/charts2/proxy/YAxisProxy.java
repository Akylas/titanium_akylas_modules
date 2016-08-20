package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.components.YAxis;

import akylas.charts2.Charts2Module;

@Kroll.proxy(parentModule = Charts2Module.class)
public class YAxisProxy extends AxisProxy {
    public YAxis getYAxis() {
        return (YAxis) getAxis();
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {

        case "drawTopYLabelEntry":
            getYAxis().setDrawTopYLabelEntry(TiConvert.toBoolean(newValue));
            break;
        case "showOnlyMinMax":
            getYAxis().setShowOnlyMinMax(TiConvert.toBoolean(newValue));
            break;
        case "inverted":
            getYAxis().setInverted(TiConvert.toBoolean(newValue));
            break;
        case "startAtZero":
            boolean startAtZero = TiConvert.toBoolean(newValue);
            if (startAtZero)
                getYAxis().setAxisMinimum(0f);
            else
                getYAxis().resetAxisMinimum();
            break;
        case "labelCount":
            int count = TiConvert.toInt(newValue);
            getYAxis().setLabelCount(count, count >= 0);
            break;
        case "drawZeroLine":
            getYAxis().setDrawZeroLine(TiConvert.toBoolean(newValue));
            break;
        case "zeroLineColor":
            getYAxis().setZeroLineColor(TiConvert.toColor(newValue));
            break;
        case "zeroLineWidth":
            getYAxis().setZeroLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "zeroLineDash":
            getYAxis().setZeroLineWidth(TiConvert.toFloat(newValue));
            break;
        case "valueFormatter":
            getYAxis().setValueFormatter(Charts2Module.axisFormatterValue(newValue, this));
            break;
        case "spaceTop":
            getYAxis().setSpaceTop(TiConvert.toFloat(newValue));
            break;
        case "spaceBottom":
            getYAxis().setSpaceBottom(TiConvert.toFloat(newValue));
            break;
        case "labelPosition":
            getYAxis().setPosition(Charts2Module.toYAxisLabelPosition(newValue));
            break;
        case "minWidth":
            getYAxis().setMinWidth(TiConvert.toFloat(newValue));
            break;
        case "maxWidth":
            getYAxis().setMaxWidth(TiConvert.toFloat(newValue));
            break;
        case "granuralityEnabled":
            getYAxis().setGranularityEnabled(TiConvert.toBoolean(newValue));
            break;
        case "granurality":
            getYAxis().setGranularity(TiConvert.toFloat(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
