package akylas.charts2.datasets;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.BarLineScatterCandleBubbleDataSet;


@Kroll.proxy
public class BarLineScatterCandleBubbleChartDataSetProxy extends DataSetProxy {
    
    public BarLineScatterCandleBubbleDataSet getSet() {
        return (BarLineScatterCandleBubbleDataSet)_set;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "highlightColor":
            getSet().setHighLightColor(TiConvert.toColor(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
