package akylas.charts2.datasets;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "drawHighlightCircle",
        "highlightCircleFillColor",
        "highlightCircleStrokeColor",
        "highlightCircleStrokeAlpha",
        "highlightCircleInnerRadius",
        "highlightCircleOuterRadius",
        "highlightCircleStrokeWidth",
        })
public class RadarChartDataSetProxy extends LineRadarChartDataSetProxy {
    public RadarDataSet getSet() {
        if (_set == null) {
            _set = new RadarDataSet(null, null);
        }
        return (RadarDataSet)_set;
    }
    
    
    @Override
    protected Class dataEntryClass() {
        return RadarEntry.class;
    }
    @Override
    public KrollDict chartDataEntryDict(Entry entry) {
        KrollDict result = super.chartDataEntryDict(entry);
        if (result != null && entry instanceof RadarEntry) {
//            result.put("high", ((PieEntry) entry).getHigh());
//            result.put("low", ((PieEntry) entry).getLow());
//            result.put("close", ((PieEntry) entry).getClose());
        }
        return result;
    }
    
    @Override
    public RadarEntry dictToChartDataEntry(HashMap value) {
        RadarEntry entry = (RadarEntry) super.dictToChartDataEntry(value);
       if (entry != null) {
//            HashMap dict = (HashMap) value;
//            entry.setHigh(TiConvert.toFloat(dict, "high", 0));
//            entry.setLow(TiConvert.toFloat(dict, "low", 0));
//            entry.setClose(TiConvert.toFloat(dict, "close", 0));
        }
        return entry;
    }
    
    @Override
    protected RadarEntry dataEntryFromNumber(Number number, int index) {
        RadarEntry entry = (RadarEntry) super.dataEntryFromNumber(number, index);
        if (entry != null) {
//            entry.setHigh(number.floatValue());
//            entry.setLow(number.floatValue());
//            entry.setClose(number.floatValue());
        }
        return entry;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "drawHighlightCircle":
            getSet().setDrawHighlightCircleEnabled(TiConvert.toBoolean(newValue));
            break;
        case "highlightCircleFillColor":
            getSet().setHighlightCircleFillColor(TiConvert.toColor(newValue));
            break;
        case "highlightCircleStrokeColor":
            getSet().setHighlightCircleStrokeColor(TiConvert.toColor(newValue));
            break;
        case "highlightCircleStrokeAlpha":
            getSet().setHighlightCircleStrokeAlpha((int)(TiConvert.toFloat(newValue) * 255));
            break;
        case "highlightCircleInnerRadius":
            getSet().setHighlightCircleInnerRadius(Charts2Module.getInDp(newValue));
            break;
        case "highlightCircleOuterRadius":
            getSet().setHighlightCircleOuterRadius(Charts2Module.getInDp(newValue));
            break;
        case "highlightCircleStrokeWidth":
            getSet().setHighlightCircleStrokeWidth(Charts2Module.getInDp(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
