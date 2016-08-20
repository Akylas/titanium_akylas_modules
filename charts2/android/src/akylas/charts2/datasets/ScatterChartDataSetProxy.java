package akylas.charts2.datasets;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.ScatterDataSet;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "scatterShapeSize",
        "scatterShape",
        "scatterShapeHoleRadius",
        "scatterShapeHoleColor",
        })
public class ScatterChartDataSetProxy
        extends LineScatterCandleRadarChartDataSetProxy {
    public ScatterDataSet getSet() {
        if (_set == null) {
            _set = new ScatterDataSet(null, null);
        }
        return (ScatterDataSet)_set;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "scatterShapeSize":
            getSet().setScatterShapeSize(Charts2Module.getInDp(newValue));
            break;
        case "scatterShape":
            getSet().setScatterShape(Charts2Module.toScatterShape(newValue));
            break;
        case "scatterShapeHoleRadius":
            getSet().setScatterShapeHoleRadius(Charts2Module.getInDp(newValue));
            break;
        case "scatterShapeHoleColor":
            getSet().setScatterShapeHoleColor(TiConvert.toColor(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
