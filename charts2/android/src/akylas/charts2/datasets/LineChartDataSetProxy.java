package akylas.charts2.datasets;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.LineDataSet;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "mode",
        "cubicIntensity",
        "axisDependency",
        "drawCircles",
        "drawCircleHole",
        "circleRadius",
        "circleHoleRadius",
        "circleColors",
        "circleColor",
        "circleHoleColor",
        "lineDash",
        "lineCap",
        })
public class LineChartDataSetProxy extends LineRadarChartDataSetProxy {
    public LineDataSet getSet() {
        if (_set == null) {
            _set = new LineDataSet(null, null);
        }
        return (LineDataSet)_set;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "mode":
            getSet().setMode(Charts2Module.toMode(newValue));
            break;
        case "cubicIntensity":
            getSet().setCubicIntensity(TiConvert.toFloat(newValue));
            break;
        case "drawCircles":
            getSet().setDrawCircles(TiConvert.toBoolean(newValue));
            break;
        case "drawCircleHole":
            getSet().setDrawCircleHole(TiConvert.toBoolean(newValue));
            break;
        case "circleRadius":
            getSet().setCircleRadius(Charts2Module.getInDp(newValue));
            break;
        case "circleHoleRadius":
            getSet().setCircleHoleRadius(Charts2Module.getInDp(newValue));
            break;
        case "circleColors":
            getSet().setCircleColors(Charts2Module.colorsArrayValue(newValue));
            break;
        case "circleColor":
            getSet().setCircleColor(TiConvert.toColor(newValue));
            break;
        case "circleHoleColor":
            getSet().setCircleColorHole(TiConvert.toColor(newValue));
            break;
        case "lineDash":
            getSet().setLineDashEffect(Charts2Module.toDashPathEffect(newValue));
            break;
        case "lineCap":
            getSet().setLineStrokeCap(Charts2Module.toCap(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
