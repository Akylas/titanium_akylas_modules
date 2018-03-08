package akylas.charts2.datasets;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "stackLabels",
        "barShadowColor",
        "barBorderColor",
        "barBorderWidth",
        "highlightAlpha",
        })
public class BarChartDataSetProxy
        extends BarLineScatterCandleBubbleChartDataSetProxy {
    public BarDataSet getSet() {
        if (_set == null) {
            _set = new BarDataSet(null, null);
        }
        return (BarDataSet)_set;
    }
    
    @Override
    protected Class dataEntryClass() {
        return BarEntry.class;
    }
    @Override
    public KrollDict chartDataEntryDict(Entry entry) {
        KrollDict result = super.chartDataEntryDict(entry);
        if (result != null && entry instanceof BarEntry) {
//            result.put("high", ((PieEntry) entry).getHigh());
//            result.put("low", ((PieEntry) entry).getLow());
//            result.put("close", ((PieEntry) entry).getClose());
        }
        return result;
    }
    
    @Override
    public BarEntry dictToChartDataEntry(HashMap value) {
        BarEntry entry = (BarEntry) super.dictToChartDataEntry(value);
       if (entry != null) {
//            HashMap dict = (HashMap) value;
//            entry.setHigh(TiConvert.toFloat(value, "high", 0));
//            entry.setLow(TiConvert.toFloat(value, "low", 0));
//            entry.setClose(TiConvert.toFloat(value, "close", 0));
        }
        return entry;
    }
    
    @Override
    protected BarEntry dataEntryFromNumber(Number number, float index) {
        BarEntry entry = (BarEntry) super.dataEntryFromNumber(number, index);
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
        case "stackLabels":
            getSet().setStackLabels(TiConvert.toStringArray(newValue));
            break;
//        case "barSpace":
//            getSet().setBarSpace(TiConvert.toFloat(newValue));
//            break;
        case "barShadowColor":
            getSet().setBarShadowColor(TiConvert.toColor(newValue));
            break;
        case "barBorderColor":
            getSet().setBarBorderColor(TiConvert.toColor(newValue));
            break;
        case "barBorderWidth":
            getSet().setBarBorderWidth(Charts2Module.getInDp(newValue));
            break;
        case "highlightAlpha":
            getSet().setHighLightAlpha((int)(TiConvert.toFloat(newValue) * 255));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
