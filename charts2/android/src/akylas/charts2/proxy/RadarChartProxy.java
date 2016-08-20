package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
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
    protected void viewDidRealize(final boolean enableModelListener,
            final boolean processProperties) {
        super.viewDidRealize(enableModelListener, processProperties);
        if (_yAxisProxy != null) {
            _yAxisProxy.setAxis(getChartYAxis());
        }
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

    @Kroll.method
    @Kroll.getProperty
    public YAxisProxy getYAxis() {
        if (_yAxisProxy == null) {
            _yAxisProxy = (YAxisProxy) KrollProxy.createProxy(YAxisProxy.class,
                    null);
            _yAxisProxy.setActivity(getActivity());
            _yAxisProxy.updateKrollObjectProperties();
            _yAxisProxy.setAxis(getChartYAxis());
            _yAxisProxy.setParentChartProxy(this);
        }
        return _yAxisProxy;
    }

    @Kroll.method
    @Kroll.setProperty
    public void setYAxis(HashMap value) {
        getYAxis().applyProperties(value);
    }

}
