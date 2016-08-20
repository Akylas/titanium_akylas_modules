package akylas.charts2.datasets;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import akylas.charts2.Charts2Module;

@Kroll.proxy(propertyAccessors={
        "selectionShift",
        "sliceSpace",
        "xValuePosition",
        "yValuePosition",
        "valueLineColor",
        "valueLineWidth",
        "valueLinePart1OffsetPercentage",
        "valueLinePart1Length",
        "valueLinePart2Length",
        "valueLineVariableLength"
        })
public class PieChartDataSetProxy extends DataSetProxy {
    public PieDataSet getSet() {
        if (_set == null) {
            _set = new PieDataSet(null, null);
        }
        return (PieDataSet)_set;
    }
    
    
    protected Class dataEntryClass() {
        return PieEntry.class;
    }
    @Override
    public KrollDict chartDataEntryDict(Entry entry) {
        KrollDict result = super.chartDataEntryDict(entry);
        if (result != null && entry instanceof PieEntry) {
//            result.put("high", ((PieEntry) entry).getHigh());
//            result.put("low", ((PieEntry) entry).getLow());
//            result.put("close", ((PieEntry) entry).getClose());
        }
        return result;
    }
    
    @Override
    public PieEntry dictToChartDataEntry(Object value) {
        PieEntry entry = (PieEntry) super.dictToChartDataEntry(value);
       if (entry != null) {
            HashMap dict = (HashMap) value;
//            entry.setHigh(TiConvert.toFloat(dict, "high", 0));
//            entry.setLow(TiConvert.toFloat(dict, "low", 0));
//            entry.setClose(TiConvert.toFloat(dict, "close", 0));
        }
        return entry;
    }
    
    @Override
    protected PieEntry dataEntryFromNumber(Number number, int index) {
        PieEntry entry = (PieEntry) super.dataEntryFromNumber(number, index);
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
        case "selectionShift":
            getSet().setSelectionShift(Charts2Module.getInDp(newValue));
            break;
        case "sliceSpace":
            getSet().setSliceSpace(Charts2Module.getInDp(newValue));
            break;
        case "xValuePosition":
            getSet().setXValuePosition(Charts2Module.toValuePosition(newValue));
            break;
        case "yValuePosition":
            getSet().setYValuePosition(Charts2Module.toValuePosition(newValue));
            break;
        case "valueLineColor":
            getSet().setValueLineColor(TiConvert.toColor(newValue));
            break;
        case "valueLineWidth":
            getSet().setValueLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "valueLinePart1OffsetPercentage":
            getSet().setValueLinePart1OffsetPercentage(TiConvert.toColor(newValue));
            break;
        case "valueLinePart1Length":
            getSet().setValueLinePart1Length(TiConvert.toColor(newValue));
            break;
        case "valueLinePart2Length":
            getSet().setValueLinePart2Length(TiConvert.toColor(newValue));
            break;
        case "valueLineVariableLength":
            getSet().setValueLineVariableLength(TiConvert.toBoolean(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
