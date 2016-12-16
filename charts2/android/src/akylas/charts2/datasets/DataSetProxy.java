package akylas.charts2.datasets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.DataProxy;
import android.content.Context;

@Kroll.proxy(propertyAccessors={
        "highlight",
        "drawValues",
        "axisDependency",
        "valueFont",
        "label",
        TiC.PROPERTY_VISIBLE,
        "color",
        "colors",
        "valueTextColor",
        "valueTextColors",
        "valueFormatter",
        "values",
        })
public abstract class DataSetProxy extends ReusableProxy {
    DataSet _set;
    
    WeakReference<DataProxy> _dataProxy;
    
    protected int mProcessUpdateFlags = 0;
    public static final int TIFLAG_NEEDS_UPDATE = 0x01000000;
    
    public void setParentDataProxy(DataProxy dataProxy) {
        if (dataProxy != null) {
            _dataProxy = new WeakReference<DataProxy>(dataProxy);
        } else {
            _dataProxy = null;
        }
    }
    
    protected Class dataEntryClass() {
        return Entry.class;
    }
    
    public DataSet getSet() {
        return _set;
    }
    
    public void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (getBindId() != null) {
            rootProxy.addBinding(getBindId(), this);
        }
    }
    
    public KrollDict chartDataEntryDict(Entry entry) {
        if (entry != null) {
            KrollDict result = new KrollDict();
            result.put("y", entry.getY());
            result.put("x", entry.getX());
            if (entry.getData() != null)
            {
                result.put("data", entry.getData());
            }
            return result;
        }
        return null;
    }
    
    public Entry dictToChartDataEntry(Object value) {
        if (value instanceof HashMap) {
            HashMap dict = (HashMap) value;
            Entry entry = null;;
            try {
                entry = (Entry) dataEntryClass().newInstance();
            } catch (Exception e) {
                return null;
            }
            entry.setX(TiConvert.toFloat(dict, "x", 0));
            entry.setY(TiConvert.toFloat(dict, "y"));
            if (dict.containsKey("data"))
            {
                entry.setData(dict.get("data"));
            }
            return entry;
        }
        return null;
    }
    
    
    protected Entry dataEntryFromNumber(Number number, int index) {
        Entry entry = null;;
        try {
            entry = (Entry) dataEntryClass().newInstance();
        } catch (Exception e) {
            return null;
        }
        entry.setX(index);
        entry.setY(number.floatValue());
        return entry;
    }
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "highlight":
            getSet().setHighlightEnabled(TiConvert.toBoolean(newValue));
            break;
        case "drawValues":
            getSet().setDrawValues(TiConvert.toBoolean(newValue));
            break;
        case "axisDependency":
            getSet().setAxisDependency(Charts2Module.toAxisDependency(newValue));
            break;
        case "valueFont":
            final Context context = getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
            getSet().setValueTypeface(desc.typeface);
            getSet().setValueTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        case "label":
            getSet().setLabel(TiConvert.toString(newValue));
            break;
        case TiC.PROPERTY_VISIBLE:
            getSet().setVisible(TiConvert.toBoolean(newValue));
            break;
        case "color":
            getSet().setColor(TiConvert.toColor(newValue));
            break;
        case "colors":
            getSet().setColors(Charts2Module.colorsArrayValue(newValue));
            break;
        case "valueTextColor":
            getSet().setValueTextColor(TiConvert.toColor(newValue));
            break;
        case "valueTextColors":
            getSet().setValueTextColors(Arrays.asList(Charts2Module.colorsArrayValue(newValue)));
            break;   
        case "valueFormatter":
            getSet().setValueFormatter(Charts2Module.formatterValue(newValue, this));
            break;   
        case "values":
            List values = new ArrayList<>();
            if (newValue instanceof Object[]) {
                Object[] array = (Object[]) newValue;
                Object current = null;
                for (int i = 0; i < array.length; i++) {
                    current = array[i];
                    Entry entry = null;
                    if (current instanceof Number) {
                        entry = dataEntryFromNumber((Number) current, i);
                    } else if (current instanceof HashMap) {
                        entry = dictToChartDataEntry(current);
                    }
                    if (entry != null) {
                        entry.setX(i);
                        values.add(entry);
                    }
                }
            }
            getSet().setValues(values);
            mProcessUpdateFlags |= TIFLAG_NEEDS_UPDATE;
            break;   
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    protected void didProcessProperties() {
        super.didProcessProperties();
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_UPDATE) != 0) {
            notifyDataSetChanged();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_UPDATE;
        } else {
            
        }
    }
    
    @Kroll.getProperty
    @Kroll.method
    public float getYMin() {
        return _set.getYMin();
    }
    @Kroll.getProperty
    @Kroll.method
    public float getYMax() {
        return _set.getYMax();
    }
    
    @Kroll.getProperty
    @Kroll.method
    public float getEntryCount() {
        return _set.getEntryCount();
    }
    
    
    @Kroll.method
    public KrollDict entryForIndex(int index) {
        Entry entry = _set.getEntryForIndex(index);
        return chartDataEntryDict(entry);
    }
    
    @Kroll.method
    public float yValForX(int index) {
        Entry entry = _set.getEntryForIndex(index);
        if (entry != null) {
            return entry.getY();
        }
        return Float.NaN;
    }
    
    @Kroll.method
    public float[] yValsForX(int index) {
        List<Entry> entries = _set.getEntriesForXValue(index);
        float[] results = new float[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            results[i] = entries.get(i).getY();
        }
        return results;
    }

    @Kroll.method
    public KrollDict entryForX(HashMap args) {
        Entry entry = _set.getEntryForXValue(
                TiConvert.toInt(args, "x"), Float.NaN, Charts2Module.toRounding(args.get("round")));
        return chartDataEntryDict(entry);
    }
    
    @Kroll.method
    public KrollDict[] entriesForXIndex(int xVal) {
        List<Entry> entries = _set.getEntriesForXValue(xVal);
        KrollDict[] results = new KrollDict[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            results[i] = chartDataEntryDict(entries.get(i));
        }
        return results;
    }
    
    @Kroll.method
    public float entryIndexWithX(HashMap args) {
        Entry entry = _set.getEntryForXValue(TiConvert.toInt(args, "x"), Float.NaN, Charts2Module.toRounding(args.get("round")));
        if (entry != null) {
            return entry.getX();
        }
        return Float.NaN;
    }

    @Kroll.method
    public float entryIndexWithEntry(HashMap args) {
        return _set.getEntryIndex(dictToChartDataEntry(args));
    }
    
    @Kroll.method
    public boolean addEntry(HashMap args) {
        return _set.addEntry(dictToChartDataEntry(args));
    }
    
    @Kroll.method
    public void addEntryOrdered(HashMap args) {
        _set.addEntryOrdered(dictToChartDataEntry(args));
    }
    
    @Kroll.method
    public boolean removeEntry(HashMap args) {
        return _set.removeEntry(dictToChartDataEntry(args));
    }
    
    @Kroll.method
    public boolean removeEntryWithXIndex(int index) {
        return _set.removeEntry(index);
    }
    
    @Kroll.method
    public boolean removeFirst() {
        return _set.removeFirst();
    }
    
    @Kroll.method
    public boolean removeLast() {
        return _set.removeLast();
    }
    
    @Kroll.method
    public boolean contains(HashMap args) {
        return _set.contains(dictToChartDataEntry(args));
    }
    
    @Kroll.method
    public void clear() {
        _set.clear();
    }
    
    @Kroll.method
    public void notifyDataSetChanged() {
        if (_set != null) {
            _set.notifyDataSetChanged();
        }
        if (_dataProxy != null) {
            _dataProxy.get().notifyDataChanged();
        }
    }
    
    @Kroll.method
    public void redraw() {
        if (_dataProxy != null) {
            _dataProxy.get().redraw();
        }
    }
}
