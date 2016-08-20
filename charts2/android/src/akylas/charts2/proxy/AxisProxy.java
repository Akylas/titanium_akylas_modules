package akylas.charts2.proxy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.utils.Utils;

import akylas.charts2.Charts2Module;
import android.content.Context;

@Kroll.proxy(parentModule = Charts2Module.class, propertyAccessors = {
        TiC.PROPERTY_ENABLED, "drawAxisLine", "drawGridLines", "drawLabels",
        "drawLimitLinesBehindData", "axisLineWidth", "axisLineDash",
        "gridLineWidth", "axisLineColor", "gridColor", "labelColor",
        "labelFont", "gridLineDash", "gridLineCap", "minValue", "maxValue", })
public class AxisProxy extends ReusableProxy {
    AxisBase _axis;
    List<LimitLineProxy> _limitLines;
    WeakReference<ChartBaseViewProxy> _viewProxy;

    public void setAxis(AxisBase axis) {
        if (_axis != null) {
            _axis.removeAllLimitLines();
        }
        _axis = axis;
        if (_axis != null) {
            processProperties(getShallowProperties());
            if (_limitLines != null) {
                for (LimitLineProxy limitLine : _limitLines) {
                    _axis.addLimitLine(limitLine.getOrCreateLimitLine());
                }
            }
        }
    }
    
    public void setParentChartProxy(ChartBaseViewProxy viewProxy) {
        if (_viewProxy != null) {
            _viewProxy = new WeakReference<ChartBaseViewProxy>(viewProxy);
        } else {
            _viewProxy = null;
        }
    }

    public AxisBase getAxis() {
        return _axis;
    }

    public void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (getBindId() != null) {
            rootProxy.addBinding(getBindId(), this);
        }
    }

    @Override
    public void release() {
        super.release();
        if (_axis != null) {
            _axis.removeAllLimitLines();
            _axis = null;
        }
        if (_limitLines != null) {
            _limitLines.clear();
            _limitLines = null;
        }
    }

    @Override
    public void processProperties(HashMap d) {
        if (_axis == null) {
            return;
        }
        super.processProperties(d);
    }

    @Override
    public void processApplyProperties(HashMap d) {
        if (_axis == null) {
            return;
        }
        super.processApplyProperties(d);
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_ENABLED:
            getAxis().setEnabled(TiConvert.toBoolean(newValue));
            break;
        case "drawAxisLine":
            getAxis().setDrawAxisLine(TiConvert.toBoolean(newValue));
            break;
        case "drawGridLines":
            getAxis().setDrawGridLines(TiConvert.toBoolean(newValue));
            break;
        case "drawLabels":
            getAxis().setDrawLabels(TiConvert.toBoolean(newValue));
            break;
        case "drawLimitLinesBehindData":
            getAxis()
                    .setDrawLimitLinesBehindData(TiConvert.toBoolean(newValue));
            break;
        case "axisLineWidth":
            getAxis().setAxisLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "axisLineDash":
            getAxis().setAxisLineDashedLine(
                    Charts2Module.toDashPathEffect(newValue));
            break;
        case "gridLineWidth":
            getAxis().setGridLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "axisLineColor":
            _axis.setAxisLineColor(TiConvert.toColor(newValue));
            break;
        case "gridColor":
            getAxis().setGridColor(TiConvert.toColor(newValue));
            break;
        case "labelColor":
            getAxis().setTextColor(TiConvert.toColor(newValue));
            break;
        case "labelFont":
            final Context context = getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context,
                    TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size,
                    context);
            getAxis().setTypeface(desc.typeface);
            getAxis().setTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        case "gridLineDash":
            getAxis().setGridDashedLine(
                    Charts2Module.toDashPathEffect(newValue));
            break;
        case "gridLineCap":
            getAxis().setGridStrokeCap(Charts2Module.toCap(newValue));
            break;
        case "minValue":
            getAxis().setAxisMinimum(TiConvert.toFloat(newValue));
            break;
        case "maxValue":
            getAxis().setAxisMaximum(TiConvert.toFloat(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    protected void didProcessProperties() {
        super.didProcessProperties();
        if (_viewProxy != null) {
            _viewProxy.get().redraw();
        }
    }

    @Kroll.method
    public void addLimitLine(Object value) {
        LimitLineProxy theProxy = null;
        if (value instanceof LimitLineProxy) {
            theProxy = (LimitLineProxy) value;
        } else if (value instanceof HashMap) {
            theProxy = (LimitLineProxy) KrollProxy.createProxy(
                    LimitLineProxy.class, null, new Object[] { value }, null);
            theProxy.updateKrollObjectProperties();
        }

        if (theProxy != null) {
            if (_limitLines == null) {
                _limitLines = new ArrayList<>();
            }
            _limitLines.add(theProxy);
            if (_axis != null) {
                _axis.addLimitLine(theProxy.getOrCreateLimitLine());
            }
        }

    }

    @Kroll.method
    public void removeLimitLine(Object value) {
        if (value instanceof LimitLineProxy) {
            if (_axis != null) {
                _axis.removeLimitLine(
                        ((LimitLineProxy) value).getOrCreateLimitLine());
            }
            _limitLines.remove(value);
        }
    }

    @Kroll.method
    public void removeAllLimitLines() {
        if (_axis != null) {
            _axis.removeAllLimitLines();
        }
        _limitLines.clear();
    }
}
