package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.DataProxy;
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
        if (view != null) {
            unarchivedWithRootProxy(rootProxy);
            rootProxy.updatePropertiesNativeSide();
            _rootProxy = null;
        } else {
            // store the root proxy until the view is created
            _rootProxy = rootProxy;
        }
    }

    @Override
    public void handleCreationDict(HashMap dict) {
        super.handleCreationDict(dict);
        setRootProxy(this);
    }

    @Override
    protected void viewDidRealize(final boolean enableModelListener,
            final boolean processProperties) {
        setRootProxy(_rootProxy);
        if (_dataProxy != null) {
            getOrCreateChartView().setData(_dataProxy.getData());
        }
        if (_xAxisProxy != null) {
            _xAxisProxy.setAxis(getChartXAxis());
        }
        if (_legendProxy != null) {
            _legendProxy.setLegend(getChartLegend());
        }
        super.viewDidRealize(enableModelListener, processProperties);
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

    @Kroll.method
    @Kroll.getProperty
    public LegendProxy getLegend() {
        if (_legendProxy == null) {
            _legendProxy = (LegendProxy) KrollProxy
                    .createProxy(LegendProxy.class, null);
            _legendProxy.setActivity(getActivity());
            _legendProxy.updateKrollObjectProperties();
            ;
            _legendProxy.setLegend(getChartLegend());
            _legendProxy.setParentChartProxy(this);
       }
        return _legendProxy;
    }

    @Kroll.method
    @Kroll.setProperty
    public void setLegend(HashMap value) {
        getLegend().applyProperties(value);
    }

    protected XAxis getChartXAxis() {
        if (peekView() != null) {
            return getOrCreateChartView().getXAxis();
        }
        return null;
    }

    @Kroll.method
    @Kroll.getProperty
    public XAxisProxy getXAxis() {
        if (_xAxisProxy == null) {
            _xAxisProxy = (XAxisProxy) KrollProxy.createProxy(XAxisProxy.class,
                    null);
            _xAxisProxy.setActivity(getActivity());
            _xAxisProxy.updateKrollObjectProperties();
            _xAxisProxy.setAxis(getChartXAxis());
            _xAxisProxy.setParentChartProxy(this);
        }
        return _xAxisProxy;
    }

    @Kroll.method
    @Kroll.setProperty
    public void setXAxis(HashMap value) {
        getXAxis().applyProperties(value);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setData(HashMap value) {
        getData().applyProperties(value);
    }

    protected ChartData getChartData() {
        if (peekView() != null) {
            return getOrCreateChartView().getData();
        }
        return null;
    }

    @Kroll.method
    @Kroll.getProperty
    public DataProxy getData() {
        if (_dataProxy == null) {
            _dataProxy = (DataProxy) KrollProxy.createProxy(dataClass(), null);
            _dataProxy.setActivity(getActivity());
            _dataProxy.setParentChartProxy(this);
            _dataProxy.updateKrollObjectProperties();
            ;
            // _dataProxy.setData(getChartData());
            if (peekView() != null) {
                getOrCreateChartView().setData(_dataProxy.getData());
            }
        }
        return _dataProxy;
    }

    @Kroll.method
    public void highlightValue(HashMap args) {
        getOrCreateChartView().getChart().highlightValue(
                TiConvert.toInt(args, "xIndex"),
                TiConvert.toInt(args, "dataSetIndex"), true);
    }

    @Kroll.method
    public void notifyDataSetChanged() {
        getOrCreateChartView().notifyDataSetChanged();
    }
    
    @Kroll.method
    public void redraw() {
        getOrCreateChartView().redraw();
    }
}
