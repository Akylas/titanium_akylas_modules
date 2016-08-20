package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.components.YAxis;

import akylas.charts2.Charts2Module;
import akylas.charts2.view.BarLineChartViewBase;

@Kroll.proxy(parentModule = Charts2Module.class, propertyAccessors = { "drag",
        "scale", "scaleX", "scaleY", "doubleTapToZoom", "autoScaleMinMax",
        "highlightPerDrag", "drawGridBackground", "drawBorders", "minOffset",
        "gridBackgroundColor", "borderColor", "borderLineWidth",
        "maxVisibleValueCount", })
public abstract class BarLineChartViewBaseProxy extends ChartBaseViewProxy {
    YAxisProxy _leftAxisProxy;
    YAxisProxy _rightAxisProxy;

    @Override
    public void release() {
        super.release();
        if (_leftAxisProxy != null) {
            _leftAxisProxy.release();
            _leftAxisProxy = null;
        }
        if (_rightAxisProxy != null) {
            _rightAxisProxy.release();
            _rightAxisProxy = null;
        }
    }

    @Override
    protected void viewDidRealize(final boolean enableModelListener,
            final boolean processProperties) {
        super.viewDidRealize(enableModelListener, processProperties);
        if (_leftAxisProxy != null) {
            _leftAxisProxy.setAxis(getChartLeftAxis());
        }
        if (_rightAxisProxy != null) {
            _rightAxisProxy.setAxis(getChartRightAxis());
        }
    }

    public BarLineChartViewBase getOrCreateChartView() {
        return (BarLineChartViewBase) getOrCreateView(true);
    }

    protected void unarchivedWithRootProxy(KrollProxy rootProxy) {
        super.unarchivedWithRootProxy(rootProxy);
        if (_leftAxisProxy != null) {
            _leftAxisProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_rightAxisProxy != null) {
            _rightAxisProxy.unarchivedWithRootProxy(rootProxy);
        }
    }

    protected YAxis getChartLeftAxis() {
        if (peekView() != null) {
            return getOrCreateChartView().getLeftAxis();
        }
        return null;
    }

    @Kroll.method
    @Kroll.getProperty
    public YAxisProxy getLeftAxis() {
        if (_leftAxisProxy == null) {
            _leftAxisProxy = (YAxisProxy) KrollProxy
                    .createProxy(YAxisProxy.class, null);
            _leftAxisProxy.setActivity(getActivity());

            _leftAxisProxy.setAxis(getChartLeftAxis());
            _leftAxisProxy.updateKrollObjectProperties();
            _leftAxisProxy.setParentChartProxy(this);
           ;
        }
        return _leftAxisProxy;
    }

    @Kroll.method
    @Kroll.setProperty
    public void setLeftAxis(HashMap value) {
        getLeftAxis().applyProperties(value);
    }

    protected YAxis getChartRightAxis() {
        if (peekView() != null) {
            return getOrCreateChartView().getRightAxis();
        }
        return null;
    }

    @Kroll.method
    @Kroll.getProperty
    public YAxisProxy getRightAxis() {
        if (_rightAxisProxy == null) {
            _rightAxisProxy = (YAxisProxy) KrollProxy
                    .createProxy(YAxisProxy.class, null);
            _rightAxisProxy.setActivity(getActivity());
            _rightAxisProxy.updateKrollObjectProperties();
            _rightAxisProxy.setAxis(getChartRightAxis());
            _rightAxisProxy.setParentChartProxy(this);
        }
        return _rightAxisProxy;
    }

    @Kroll.method
    @Kroll.setProperty
    public void setRightAxis(HashMap value) {
        getRightAxis().applyProperties(value);
    }
}
