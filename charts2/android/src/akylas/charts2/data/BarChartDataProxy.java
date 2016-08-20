package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.BarData;

import akylas.charts2.datasets.BarChartDataSetProxy;

@Kroll.proxy(propertyAccessors={
        "barWidth",
        })
public class BarChartDataProxy
        extends BarLineScatterCandleBubbleChartDataProxy {
    public BarData getData() {
        if (_data == null) {
            _data = new BarData();
        }
        return (BarData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return BarChartDataSetProxy.class;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "barWidth":
            getData().setBarWidth(TiConvert.toFloat(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
