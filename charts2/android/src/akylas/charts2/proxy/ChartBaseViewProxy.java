package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUrl;
import org.appcelerator.titanium.view.TiUIView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.DataProxy;
import akylas.charts2.datasets.DataSetProxy;
import akylas.charts2.view.BaseChart;
import android.app.Activity;

@Kroll.proxy(parentModule = Charts2Module.class)
public abstract class ChartBaseViewProxy extends TiViewProxy {
    DataProxy _dataProxy;
    LegendProxy _legendProxy;
    XAxisProxy _xAxisProxy;
    KrollProxy _rootProxy = null;

    abstract protected Class dataClass();

    @Override
    abstract public TiUIView createView(Activity activity);

    @Override
    public void release() {
        super.release();
        if (_legendProxy != null) {
            _legendProxy.release();
            _legendProxy = null;
        }
        if (_xAxisProxy != null) {
            _xAxisProxy.release();
            _xAxisProxy = null;
        }
        if (_dataProxy != null) {
            _dataProxy.release();
            _dataProxy = null;
        }
    }

    @Override
    public void handleCreationDict(HashMap dict, KrollProxy rootProxy) {
        super.handleCreationDict(dict, rootProxy);
        if (dict.containsKey(TiC.PROPERTY_DATA)) {
            setData(TiConvert.toHashMap(dict.get(TiC.PROPERTY_DATA)));
        }
        if (dict.containsKey("xAxis")) {
            setXAxis(TiConvert.toHashMap(dict.get("xAxis")));
        }
        if (dict.containsKey("legend")) {
            setLegend(TiConvert.toHashMap(dict.get("legend")));
        }
    }

    protected void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (_legendProxy != null) {
            _legendProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_xAxisProxy != null) {
            _xAxisProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_dataProxy != null) {
            _dataProxy.unarchivedWithRootProxy(rootProxy);
        }
    }

    private void setRootProxy(KrollProxy rootProxy) {
        if (rootProxy == null) {
            _rootProxy = rootProxy;
            return;
        }
        if (_rootProxy == null) {
            _rootProxy = rootProxy;
            unarchivedWithRootProxy(rootProxy);
            rootProxy.updatePropertiesNativeSide();
        }

        // if (view != null) {
        // unarchivedWithRootProxy(rootProxy);
        // rootProxy.updatePropertiesNativeSide();
        // _rootProxy = null;
        // } else {
        // // store the root proxy until the view is created
        // _rootProxy = rootProxy;
        // }
    }

    @Override
    protected void setupProxy(KrollObject object, Object[] creationArguments,
            TiUrl creationUrl) {
        setRootProxy(this);
        super.setupProxy(object, creationArguments, creationUrl);
    }

    @Override
    public void realizeViews(TiUIView view, final boolean enableModelListener,
            final boolean processProperties) {
        if (_dataProxy != null) {
            getOrCreateChartView().setData(_dataProxy.getData());
        }
        if (_xAxisProxy != null) {
            _xAxisProxy.setAxis(getChartXAxis());
        }
        if (_legendProxy != null) {
            _legendProxy.setLegend(getChartLegend());
        }
        super.realizeViews(view, enableModelListener, processProperties);
    }

    @Override
    protected void initFromTemplate(HashMap template_, KrollProxy rootProxy,
            boolean updateKrollProperties, boolean recursive) {
        super.initFromTemplate(template_, rootProxy, updateKrollProperties,
                recursive);
        setRootProxy(rootProxy);
    }

    public BaseChart getOrCreateChartView() {
        return (BaseChart) getOrCreateView(true);
    }

    protected Legend getChartLegend() {
        if (peekView() != null) {
            return getOrCreateChartView().getLegend();
        }
        return null;
    }

    private LegendProxy getOrCreateLegend(HashMap value) {
        if (_legendProxy == null) {
            _legendProxy = (LegendProxy) KrollProxy
                    .createProxy(LegendProxy.class, value);
            _legendProxy.setActivity(getActivity());
            _legendProxy.updateKrollObjectProperties();
            ;
            _legendProxy.setLegend(getChartLegend());
            _legendProxy.setParentChartProxy(this);
            if (_legendProxy != null) {
                _legendProxy.unarchivedWithRootProxy(_rootProxy);
            }
        } else if (value != null) {
            _legendProxy.applyPropertiesInternal(value, false, false);
        }
        return _legendProxy;
    }

    @Kroll.method
    @Kroll.getProperty
    public LegendProxy getLegend() {
        return getOrCreateLegend(null);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setLegend(HashMap value) {
        getOrCreateLegend(value);
    }

    protected XAxis getChartXAxis() {
        if (peekView() != null) {
            return getOrCreateChartView().getXAxis();
        }
        return null;
    }

    private XAxisProxy getOrCreateXAxis(HashMap value) {
        if (_xAxisProxy == null) {
            _xAxisProxy = (XAxisProxy) KrollProxy.createProxy(XAxisProxy.class,
                    value);
            _xAxisProxy.setActivity(getActivity());
            _xAxisProxy.updateKrollObjectProperties();
            _xAxisProxy.setAxis(getChartXAxis());
            _xAxisProxy.setParentChartProxy(this);
            if (_xAxisProxy != null) {
                _xAxisProxy.unarchivedWithRootProxy(_rootProxy);
            }
        } else if (value != null) {
            _xAxisProxy.applyPropertiesInternal(value, false, false);
        }
        return _xAxisProxy;
    }

    @Kroll.method
    @Kroll.getProperty
    public XAxisProxy getXAxis() {
        return getOrCreateXAxis(null);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setXAxis(HashMap value) {
        getOrCreateXAxis(value);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setData(HashMap value) {
        getOrCreateData(value);
    }

    protected ChartData getChartData() {
        if (peekView() != null) {
            return getOrCreateChartView().getData();
        }
        return null;
    }

    private DataProxy getOrCreateData(HashMap value) {
        if (_dataProxy == null) {
            _dataProxy = (DataProxy) KrollProxy.createProxy(dataClass(), value);
            _dataProxy.setActivity(getActivity());
            _dataProxy.setParentChartProxy(this);
            if (_rootProxy != null) {
                _dataProxy.unarchivedWithRootProxy(_rootProxy);
            }
            _dataProxy.updateKrollObjectProperties();
            // _dataProxy.setData(getChartData());
            if (peekView() != null) {
                getOrCreateChartView().setData(_dataProxy.getData());
            }
        } else if (value != null) {
            _dataProxy.applyPropertiesInternal(value, false, false);
        }
        return _dataProxy;
    }

    @Kroll.method
    @Kroll.getProperty
    public DataProxy getData() {
        return getOrCreateData(null);
    }

    @Kroll.method
    public void highlightValue(final HashMap args) {
        if (!TiApplication.isUIThread()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    highlightValue(args);
                }
            });
            return;
        }
        if (view != null) {
            getOrCreateChartView().getChart().highlightValue(
                    TiConvert.toInt(args, "x", -1),
                    TiConvert.toInt(args, "datasetIndex", -1), 
                    TiConvert.toBoolean(args, "callEvent", true));
        }
    }

    @Kroll.method
    public void notifyDataSetChanged() {
        if (view != null) {
            getOrCreateChartView().notifyDataSetChanged();
//            getOrCreateChartView().redraw();
        }
    }
    
    @Kroll.method
    public void redraw() {
        if (view != null) {
            getOrCreateChartView().redraw();
        }
    }

    @Kroll.method
    public KrollDict getDataForIndex(int dataSetIndex, int dataIndex) {
        DataSetProxy dataSetProxy = getData().getDataSet(dataSetIndex);
        if (dataSetProxy != null) {
            Entry e = dataSetProxy.getSet().getEntryForIndex(dataIndex);
            if (e != null) {
                Highlight h = new Highlight(e.getX(), e.getY(), dataSetIndex);
                h.setDataIndex(dataIndex);
                return getOrCreateChartView().getRealDictForHighlight(dataSetProxy, h, e);
            }
        }
        return null;
    }
    
    @Kroll.method
    public void animate(HashMap props) {
        if (view == null) {
            return;
        }
        getOrCreateChartView().animate(props);
    }
}
