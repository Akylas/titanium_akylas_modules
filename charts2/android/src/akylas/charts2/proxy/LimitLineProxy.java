package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;

import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.utils.Utils;

import akylas.charts2.Charts2Module;
import android.content.Context;

@Kroll.proxy(creatableInModule = Charts2Module.class, name="LimitLine")
public class LimitLineProxy extends ReusableProxy {
    LimitLine _limitLine;
    public LimitLine getOrCreateLimitLine() {
        if (_limitLine == null) {
            _limitLine = new LimitLine(0);
        }
        return _limitLine;
    }
    
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_ENABLED:
            getOrCreateLimitLine().setEnabled(TiConvert.toBoolean(newValue));
            break;
        case "font":
            final Context context = getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
            getOrCreateLimitLine().setTypeface(desc.typeface);
            getOrCreateLimitLine().setTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        case "color":
            getOrCreateLimitLine().setTextColor(TiConvert.toColor(newValue));
            break;
        case "drawLabels":
            getOrCreateLimitLine().setDrawLabel(TiConvert.toBoolean(newValue));
            break;
        case "labelPosition":
            getOrCreateLimitLine().setLabelPosition(Charts2Module.toLabelPosition(newValue));
            break;
        case "lineWidth":
            getOrCreateLimitLine().setLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "lineDash":
            getOrCreateLimitLine().setDashedLine(Charts2Module.toDashPathEffect(newValue));
            break;
        case "limit":
            getOrCreateLimitLine().setLimit(TiConvert.toFloat(newValue));
            break;
        case "lineColor":
            getOrCreateLimitLine().setLineColor(TiConvert.toColor(newValue));
            break;
        case "label":
            getOrCreateLimitLine().setLabel(TiConvert.toString(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
