package akylas.charts2.datasets;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import com.github.mikephil.charting.data.LineRadarDataSet;

import akylas.charts2.Charts2Module;
import android.graphics.drawable.Drawable;

@Kroll.proxy(propertyAccessors={
        "drawFilled",
        "lineWidth",
        "fillColor",
        "fillImage",
        "fillImageTiled",
        "fillGradient",
        "fillAlpha",
        })
public class LineRadarChartDataSetProxy
        extends LineScatterCandleRadarChartDataSetProxy {
    public LineRadarDataSet getSet() {
        return (LineRadarDataSet)_set;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "drawFilled":
            getSet().setDrawFilled(TiConvert.toBoolean(newValue));
            break;
        case "lineWidth":
            getSet().setLineWidth(Charts2Module.getInDp(newValue));
            break;
        case "fillColor":
            getSet().setFillColor(TiConvert.toColor(newValue));
            break;
        case "fillImage":
        {
            Drawable drawable = TiUIHelper.buildImageDrawable(
                    getActivity(), newValue, false, this);
            getSet().setFillDrawable(drawable);
            break;
        }
        case "fillImageTiled":
        {
            Drawable drawable = TiUIHelper.buildImageDrawable(
                    getActivity(), newValue, true, this);
            getSet().setFillDrawable(drawable);
            break;
        }
        case "fillAlpha":
            getSet().setFillAlpha((int)(TiConvert.toFloat(newValue) * 255));
            break;
        case "fillGradient":
            Drawable drawable = TiUIHelper
            .buildGradientDrawable(TiConvert.toKrollDict(newValue));
            getSet().setFillDrawable(drawable);
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
}
