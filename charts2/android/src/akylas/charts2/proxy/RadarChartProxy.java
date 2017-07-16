package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.github.mikephil.charting.components.YAxis;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.RadarChartDataProxy;
import akylas.charts2.view.RadarChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name = "RadarChart", propertyAccessors = {
        "webColor", "innerWebColor", "drawWeb", "webLineWidth",
        "innerWebLineWidth", "webAlpha", "skipWebLineCount" })
public class RadarChartProxy extends PieRadarChartViewBaseProxy {
    YAxisProxy _yAxisProxy;

    @Override
    protected Class dataClass() {
        return RadarChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new RadarChartView(this);
    }
    
    @Override
    public void handleCreationDict(HashMap dict, KrollProxy rootProxy) {
        super.handleCreationDict(dict, rootProxy);
        if (dict.containsKey("yAxis")) {
            setYAxis(TiConvert.toHashMap(dict .get("yAxis")));
        }
    }

    @Override
    public void realizeViews(TiUIView view, final boolean enableModelListener,
            final boolean processProperties) {
        if (_yAxisProxy != null) {
            _yAxisProxy.setAxis(getChartYAxis());
        }
        super.realizeViews(view, enableModelListener, processProperties);
    }

    @Override
    public void release() {
        super.release();
        if (_yAxisProxy != null) {
            _yAxisProxy.release();
            _yAxisProxy = null;
        }
    }

    protected void unarchivedWithRootProxy(KrollProxy rootProxy) {
        super.unarchivedWithRootProxy(rootProxy);
        if (_yAxisProxy != null) {
            _yAxisProxy.unarchivedWithRootProxy(rootProxy);
        }
    }

    protected YAxis getChartYAxis() {
        if (peekView() != null) {
            return ((RadarChartView) getOrCreateChartView()).getYAxis();
        }
        return null;
    }
    
    public YAxisProxy getOrCreateYAxis(HashMap value) {
        if (_yAxisProxy == null) {
            _yAxisProxy = (YAxisProxy) KrollProxy.createProxy(YAxisProxy.class,
                    value);
            _yAxisProxy.setActivity(getActivity());
            _yAxisProxy.updateKrollObjectProperties();
            _yAxisProxy.setAxis(getChartYAxis());
            _yAxisProxy.setParentChartProxy(this);
            if (_yAxisProxy != null) {
                _yAxisProxy.unarchivedWithRootProxy(_rootProxy);
            }
        }
        return _yAxisProxy;
    }

    @Kroll.method
    @Kroll.getProperty
    public YAxisProxy getYAxis() {
        return getOrCreateYAxis(null);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setYAxis(HashMap value) {
        getOrCreateYAxis(value);
    }

}
