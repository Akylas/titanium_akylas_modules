package akylas.charts2.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import akylas.charts2.Charts2Module;
import akylas.charts2.datasets.DataSetProxy;
import akylas.charts2.proxy.ChartBaseViewProxy;
import android.content.Context;

@Kroll.proxy(propertyAccessors = { "highlight", "drawValues", "valueFont",
        "valueTextColor", "valueFormatter", })
public abstract class DataProxy extends ReusableProxy {
    ChartData _data;
    List<DataSetProxy> _dataSets = new ArrayList();
    WeakReference<ChartBaseViewProxy> _viewProxy;

    protected int mProcessUpdateFlags = 0;
    public static final int TIFLAG_NEEDS_UPDATE = 0x01000000;

    abstract protected Class dataSetClass();

    public void setParentChartProxy(ChartBaseViewProxy viewProxy) {
        if (viewProxy != null) {
            _viewProxy = new WeakReference<ChartBaseViewProxy>(viewProxy);
        } else {
            _viewProxy = null;
        }
    }

    public ChartData getData() {
        return _data;
    }

    public void setData(ChartData data) {
        _data = data;
    }

    public DataSetProxy getDataSet(int index) {
        if (index >= 0 && index < _dataSets.size()) {
            return _dataSets.get(index);
        }
        return null;
    }

    public void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (getBindId() != null) {
            rootProxy.addBinding(getBindId(), this);
        }
        for (DataSetProxy setProxy : _dataSets) {
            setProxy.unarchivedWithRootProxy(rootProxy);
        }
    }

    private boolean highlight = true;
    private boolean drawValues = false;
    private FontDesc fontDesc = null;
    private float fontSize = 0.0f;
    private int valueTextColor = 0;
    private IValueFormatter valueFormatter = null;

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "highlight":
            highlight = TiConvert.toBoolean(newValue);
            getData().setHighlightEnabled(highlight);
            break;
        case "drawValues":
            drawValues = TiConvert.toBoolean(newValue);
            getData().setDrawValues(drawValues);
            break;
        case "valueFont":
            final Context context = getActivity();
            fontDesc = TiUIHelper.getFontStyle(context,
                    TiConvert.toHashMap(newValue));
            fontSize = TiUIHelper.getRawSize(fontDesc.sizeUnit, fontDesc.size,
                    context);
            getData().setValueTypeface(fontDesc.typeface);
            getData().setValueTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        case "valueTextColor":
            valueTextColor = TiConvert.toColor(newValue);
            getData().setValueTextColor(valueTextColor);
            break;

        case "valueFormatter":
            valueFormatter = Charts2Module.formatterValue(newValue, this);
            getData().setValueFormatter(valueFormatter);
            break;
        case "labels":
            final String[] xValues = TiConvert.toStringArray(newValue);
            if (xValues != null && xValues.length > 0) {
                if (_viewProxy != null) {
                    _viewProxy.get().getXAxis().setValueFormatter(
                            new Charts2Module.AxisValueFormatter() {
                                @Override
                                public String getFormattedValue(int index,
                                        float value, AxisBase axis) {
                                    return xValues[(int) value
                                            % xValues.length];
                                }

                                @Override
                                public int getDecimalDigits() {
                                    return 0;
                                }
                            });
                }
            }
            getData().setValueFormatter(
                    Charts2Module.formatterValue(newValue, this));
            break;
        case "datasets":
            if (newValue instanceof Object[]) {
                setDatasets((Object[]) newValue);
                mProcessUpdateFlags |= TIFLAG_NEEDS_UPDATE;
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    private boolean processingProperties = false;

    @Override
    protected void aboutToProcessProperties(HashMap d) {
        super.aboutToProcessProperties(d);
        processingProperties = true;
    }

    @Override
    protected void didProcessProperties() {
        processingProperties = false;
        super.didProcessProperties();
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_UPDATE) != 0) {
            notifyDataChanged();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_UPDATE;
        } else {
            redraw();
        }
    }

    @Kroll.method
    @Kroll.getProperty
    public Object[] getDatasets() {
        return (Object[]) _dataSets.toArray();
    }

    @Kroll.method
    @Kroll.setProperty
    public void setDatasets(Object[] value) {
        mProcessUpdateFlags &= ~TIFLAG_NEEDS_UPDATE;
        if (_dataSets.size() > 0) {
            for (DataSetProxy setProxy : _dataSets) {
                setProxy.setParentDataProxy(null);
            }
            _dataSets.clear();
        }
        List sets = new ArrayList<>();
        DataSetProxy setProxy = null;
        Object current = null;
        Class dataSetClass = dataSetClass();
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                current = value[i];
                if (current instanceof DataSetProxy) {
                    setProxy = (DataSetProxy) current;
                } else if (current instanceof HashMap) {
                    setProxy = (DataSetProxy) KrollProxy
                            .createProxy(dataSetClass, current);
                    setProxy.setActivity(getActivity());
                    setProxy.updateKrollObjectProperties();
                    ;
                } else {
                    setProxy = null;
                }
                if (setProxy != null) {
                    DataSet set = setProxy.getSet();
                    set.setDrawValues(drawValues);
                    set.setHighlightEnabled(highlight);
                    if (fontDesc != null) {
                        set.setValueTypeface(fontDesc.typeface);
                        set.setValueTextSize(Utils.convertPixelsToDp(fontSize));
                    }
                    set.setValueTextColor(valueTextColor);
                    if (valueFormatter != null) {
                        set.setValueFormatter(valueFormatter);
                    }
                    setProxy.setParentDataProxy(this);
                    _dataSets.add(setProxy);
                    sets.add(set);
                }
            }

        }
        getData().setDataSets(sets);
        if (!processingProperties) {
            notifyDataChanged();
        }
    }

    // private void updateChartData() {
    // notifyDataChanged();
    // }

    @Kroll.method
    public void notifyDataChanged() {

        if (_data != null) {
            _data.notifyDataChanged();
        }
        if (_viewProxy != null) {
            _viewProxy.get().notifyDataSetChanged();
        } else {
          
        }
    }

    @Kroll.method
    public void redraw() {
        if (_viewProxy != null) {
            _viewProxy.get().redraw();
        }
    }

    @Kroll.method
    public void addDataSet(Object value) {
        DataSetProxy setProxy = null;
        if (value instanceof DataSetProxy) {
            setProxy = (DataSetProxy) value;
        } else if (value instanceof HashMap) {
            setProxy = (DataSetProxy) KrollProxy.createProxy(dataSetClass(),
                    value);
            setProxy.setActivity(getActivity());
            setProxy.updateKrollObjectProperties();
            ;
        }
        if (setProxy != null) {
            setProxy.setParentDataProxy(this);
            _dataSets.add(setProxy);
            getData().addDataSet(setProxy.getSet());
        }
    }

    @Kroll.method
    public void removeDataSet(Object value) {
        if (value instanceof DataSetProxy) {
            getData().removeDataSet(((DataSetProxy) value).getSet());
            ((DataSetProxy) value).setParentDataProxy(null);
            _dataSets.remove(value);
        } else if (value instanceof Number) {
            int index = ((Number) value).intValue();
            if (index >= 0 && index < _dataSets.size()) {
                getData().removeDataSet(index);
                _dataSets.remove(index).setParentDataProxy(null);
            }
        }
    }
}
