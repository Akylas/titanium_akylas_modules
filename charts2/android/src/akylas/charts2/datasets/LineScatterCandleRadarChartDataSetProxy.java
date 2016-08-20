package akylas.charts2.datasets;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.LineScatterCandleRadarDataSet;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "drawHorizontalHighlightIndicator",
        "drawVerticalHighlightIndicator",
        "drawHighlightIndicators",
        "highlightLineWidth",
        })
public class LineScatterCandleRadarChartDataSetProxy
        extends BarLineScatterCandleBubbleChartDataSetProxy {
    public LineScatterCandleRadarDataSet getSet() {
        return (LineScatterCandleRadarDataSet)_set;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "drawHorizontalHighlightIndicator":
            getSet().setDrawHorizontalHighlightIndicator(TiConvert.toBoolean(newValue));
            break;
        case "drawVerticalHighlightIndicator":
            getSet().setDrawVerticalHighlightIndicator(TiConvert.toBoolean(newValue));
            break;
        case "drawHighlightIndicators":
            getSet().setDrawHighlightIndicators(TiConvert.toBoolean(newValue));
            break;
        case "highlightLineWidth":
            getSet().setHighlightLineWidth(Charts2Module.getInDp(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
