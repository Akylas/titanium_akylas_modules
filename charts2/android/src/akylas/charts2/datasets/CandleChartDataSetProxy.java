package akylas.charts2.datasets;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;

import akylas.charts2.Charts2Module;
import android.graphics.Paint;

@Kroll.proxy(propertyAccessors={
        "showCandleBar",
        "shadowColorSameAsCandle",
        "increasingFilled",
        "decreasingFilled",
        "barSpace",
        "shadowWidth",
        "shadowColor",
        "neutralColor",
        "increasingColor",
        "decreasingColor",
        })
public class CandleChartDataSetProxy
        extends LineScatterCandleRadarChartDataSetProxy {
    public CandleDataSet getSet() {
        if (_set == null) {
            _set = new CandleDataSet(null, null);
        }
        return (CandleDataSet)_set;
    }
    
    protected Class dataEntryClass() {
        return CandleEntry.class;
    }
    @Override
    public KrollDict chartDataEntryDict(Entry entry) {
        KrollDict result = super.chartDataEntryDict(entry);
        if (result != null && entry instanceof CandleEntry) {
            result.put("high", ((CandleEntry) entry).getHigh());
            result.put("low", ((CandleEntry) entry).getLow());
            result.put("close", ((CandleEntry) entry).getClose());
        }
        return result;
    }
    
    @Override
    public CandleEntry dictToChartDataEntry(Object value) {
        CandleEntry entry = (CandleEntry) super.dictToChartDataEntry(value);
       if (entry != null) {
            HashMap dict = (HashMap) value;
            entry.setHigh(TiConvert.toFloat(dict, "high", 0));
            entry.setLow(TiConvert.toFloat(dict, "low", 0));
            entry.setClose(TiConvert.toFloat(dict, "close", 0));
        }
        return entry;
    }
    
    @Override
    protected CandleEntry dataEntryFromNumber(Number number, int index) {
        CandleEntry entry = (CandleEntry) super.dataEntryFromNumber(number, index);
        if (entry != null) {
            entry.setHigh(number.floatValue());
            entry.setLow(number.floatValue());
            entry.setClose(number.floatValue());
        }
        return entry;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "showCandleBar":
            getSet().setShowCandleBar(TiConvert.toBoolean(newValue));
            break;
        case "shadowColorSameAsCandle":
            getSet().setShadowColorSameAsCandle(TiConvert.toBoolean(newValue));
            break;
        case "increasingFilled":
        {
            boolean filled = TiConvert.toBoolean(newValue);
            getSet().setIncreasingPaintStyle(filled?Paint.Style.FILL_AND_STROKE:Paint.Style.STROKE);
            break;
        }
        case "decreasingFilled":
        {
            boolean filled = TiConvert.toBoolean(newValue);
            getSet().setDecreasingPaintStyle(filled?Paint.Style.FILL_AND_STROKE:Paint.Style.FILL);
            break;
        }
        case "barSpace":
            getSet().setBarSpace(TiConvert.toFloat(newValue));
            break;
        case "shadowWidth":
            getSet().setShadowWidth(Charts2Module.getInDp(newValue));
            break;
        case "shadowColor":
            getSet().setShadowColor(TiConvert.toColor(newValue));
            break;
        case "neutralColor":
            getSet().setNeutralColor(TiConvert.toColor(newValue));
            break;
        case "increasingColor":
            getSet().setIncreasingColor(TiConvert.toColor(newValue));
            break;
        case "decreasingColor":
            getSet().setDecreasingColor(TiConvert.toColor(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
