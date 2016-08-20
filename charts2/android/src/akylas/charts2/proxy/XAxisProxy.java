package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import akylas.charts2.Charts2Module;

@Kroll.proxy(parentModule = Charts2Module.class)
public class XAxisProxy extends AxisProxy {
    public XAxis getXAxis() {
        return (XAxis) getAxis();
    }
    
    
    public void setValueFormatter(IAxisValueFormatter f) {
        getXAxis().setValueFormatter(f);
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {

        case "labelPosition":
            getXAxis().setPosition(Charts2Module.toXAxisLabelPosition(newValue));
            break;
        case "spaceBetweenLabels":
            getXAxis().mSpaceBeetweenLabels = TiConvert.toInt(newValue);
            break;
        case "labelWidth":
            getXAxis().mLabelWidth = TiConvert.toInt(newValue);
            break;
        case "labelHeight":
            getXAxis().mLabelHeight = TiConvert.toInt(newValue);
            break;
        case "avoidFirstLastClippingEnabled":
            getXAxis().setAvoidFirstLastClipping(TiConvert.toBoolean(newValue));
            break;
        case "valueFormatter":
            getXAxis().setValueFormatter(Charts2Module.axisFormatterValue(newValue, this));
            break;
        case "labelsToSkip":
            int result = TiConvert.toInt(newValue);
            if (result >= 0) {
                getXAxis().resetLabelsToSkip();
            } else {
                getXAxis().setLabelsToSkip(result);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
