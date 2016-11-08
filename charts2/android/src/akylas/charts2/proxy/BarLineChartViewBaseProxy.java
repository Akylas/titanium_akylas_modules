package akylas.charts2.proxy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

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
    public void handleCreationDict(HashMap dict) {
        super.handleCreationDict(dict);
        if (dict.containsKey("leftAxis")) {
            setLeftAxis(TiConvert.toHashMap(dict .get("leftAxis")));
        }
        if (dict.containsKey("rightAxis")) {
            setRightAxis(TiConvert.toHashMap(dict .get("rightAxis")));
        }
    }

    @Override
    public void realizeViews(TiUIView view, final boolean enableModelListener,
            final boolean processProperties) {
        if (_leftAxisProxy != null) {
            _leftAxisProxy.setAxis(getChartLeftAxis());
        }
        if (_rightAxisProxy != null) {
            _rightAxisProxy.setAxis(getChartRightAxis());
        }
        super.realizeViews(view, enableModelListener, processProperties);
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
    
    public YAxisProxy getOrCreateLeftAxis(HashMap value) {
        if (_leftAxisProxy == null) {
            _leftAxisProxy = (YAxisProxy) KrollProxy.createProxy(YAxisProxy.class,
                    value);
            _leftAxisProxy.setActivity(getActivity());
            _leftAxisProxy.updateKrollObjectProperties();
            _leftAxisProxy.setAxis(getChartLeftAxis());
            _leftAxisProxy.setParentChartProxy(this);
            if (_leftAxisProxy != null) {
                _leftAxisProxy.unarchivedWithRootProxy(_rootProxy);
            }
        }
        return _leftAxisProxy;
    }
    
    public YAxisProxy getOrCreateRightAxis(HashMap value) {
        if (_rightAxisProxy == null) {
            _rightAxisProxy = (YAxisProxy) KrollProxy.createProxy(YAxisProxy.class,
                    value);
            _rightAxisProxy.setActivity(getActivity());
            _rightAxisProxy.updateKrollObjectProperties();
            _rightAxisProxy.setAxis(getChartLeftAxis());
            _rightAxisProxy.setParentChartProxy(this);
            if (_rightAxisProxy != null) {
                _rightAxisProxy.unarchivedWithRootProxy(_rootProxy);
            }
        }
        return _rightAxisProxy;
    }

    @Kroll.method
    @Kroll.getProperty
    public YAxisProxy getLeftAxis() {
        return getOrCreateLeftAxis(null);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setLeftAxis(HashMap value) {
        getOrCreateLeftAxis(value);
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
        return getOrCreateRightAxis(null);
    }

    @Kroll.method
    @Kroll.setProperty
    public void setRightAxis(HashMap value) {
        getOrCreateRightAxis(value);
    }
}
