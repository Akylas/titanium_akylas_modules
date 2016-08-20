package akylas.charts2.datasets;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.Entry;

@Kroll.proxy(propertyAccessors={
        "highlightColor",
        })
public class BubbleChartDataSetProxy
        extends BarLineScatterCandleBubbleChartDataSetProxy {
    public BubbleDataSet getSet() {
        if (_set == null) {
            _set = new BubbleDataSet(null, null);
        }
        return (BubbleDataSet)_set;
    }
    
    protected Class dataEntryClass() {
        return BubbleEntry.class;
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
    
    public KrollDict chartDataEntryDict(Entry entry) {
        KrollDict result = super.chartDataEntryDict(entry);
        if (result != null && entry instanceof BubbleEntry) {
            result.put("size", ((BubbleEntry) entry).getSize());
        }
        return result;
    }
    
    @Override
    public BubbleEntry dictToChartDataEntry(Object value) {
       BubbleEntry entry = (BubbleEntry) super.dictToChartDataEntry(value);
       if (entry != null) {
            HashMap dict = (HashMap) value;
            entry.setSize(TiConvert.toFloat(dict, "size", 0));
        }
        return entry;
    }
    
    @Override
    protected BubbleEntry dataEntryFromNumber(Number number, int index) {
        BubbleEntry entry = (BubbleEntry) super.dataEntryFromNumber(number, index);
        if (entry != null) {
            entry.setSize(number.floatValue());
        }
        return entry;
    }
}
