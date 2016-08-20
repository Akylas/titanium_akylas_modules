package akylas.charts2.proxy;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.Utils;

import akylas.charts2.Charts2Module;
import android.content.Context;

@Kroll.proxy(parentModule = Charts2Module.class)
public class LegendProxy extends ReusableProxy {
    Legend _chartLegend;
    WeakReference<ChartBaseViewProxy> _viewProxy;
    
    public void setParentChartProxy(ChartBaseViewProxy viewProxy) {
        if (_viewProxy != null) {
            _viewProxy = new WeakReference<ChartBaseViewProxy>(viewProxy);
        } else {
            _viewProxy = null;
        }
    }
    
    public void setLegend(Legend chartLegend) {
        _chartLegend = chartLegend;
        if (_chartLegend != null) {
            processProperties(getShallowProperties());
        }
    }
    public Legend getLegend() {
        return _chartLegend;
    }
    
    public void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (getBindId() != null) {
            rootProxy.addBinding(getBindId(), this);
        }
    }
    
    @Override
    public void processProperties(HashMap d)
    {
        if (_chartLegend == null) {
            return;
        }
        super.processProperties(d);
    }
    
    @Override
    public void processApplyProperties(HashMap d)
    {
        if (_chartLegend == null) {
            return;
        }
        super.processApplyProperties(d);
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_ENABLED:
            getLegend().setEnabled(TiConvert.toBoolean(newValue));
            break;
        case "color":
            getLegend().setTextColor(TiConvert.toColor(newValue));
            break;
        case "font":
            final Context context = getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
            getLegend().setTypeface(desc.typeface);
            getLegend().setTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        case "direction":
            getLegend().setDirection(Charts2Module.toLegendDirection(newValue));
            break;
        case "position":
            getLegend().setPosition(Charts2Module.toLegendPosition(newValue));
            break;
        case "form":
            getLegend().setForm(Charts2Module.toLegendForm(newValue));
            break;
        case "formSize":
            getLegend().setFormSize(Charts2Module.getInDp(newValue));
            break;
        case "formLineWidth":
            getLegend().setFormLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "stackSpace":
            getLegend().setStackSpace(TiConvert.toFloat(newValue));
            break;
        case "formToTextSpace":
            getLegend().setFormToTextSpace(Charts2Module.getInDp(newValue));
            break;
        case "xEntrySpace":
            getLegend().setXEntrySpace(Charts2Module.getInDp(newValue));
            break;
        case "yEntrySpace":
            getLegend().setYEntrySpace(Charts2Module.getInDp(newValue));
            break;
        case "xOffset":
            getLegend().setXOffset(Charts2Module.getInDp(newValue));
            break;
        case "yOffset":
            getLegend().setYOffset(Charts2Module.getInDp(newValue));
            break;
        case "maxSizePercent":
            getLegend().setMaxSizePercent(TiConvert.toFloat(newValue));
            break;
        case "wordWrap":
            getLegend().setWordWrapEnabled(TiConvert.toBoolean(newValue));
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
}
