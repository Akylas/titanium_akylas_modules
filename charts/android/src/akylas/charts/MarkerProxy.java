package akylas.charts;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.animation.TiAnimatorSet;
import org.appcelerator.titanium.proxy.AnimatableProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.androidplot.xy.ValueMarker;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.YValueMarker;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class, propertyAccessors={
    TiC.PROPERTY_VALUE
})
@SuppressWarnings("rawtypes")
public class MarkerProxy extends AnimatableProxy  {
    // Standard Debugging variables
    @SuppressWarnings("unused")
    private static final String TAG = "MarkerProxy";
    private LineChartProxy lineChartProxy;
    
    ValueMarker mMarker;

    public MarkerProxy() {
        super();
    }
    
    public MarkerProxy(TiContext context) {
        this();
    }

    public ValueMarker getMarker() {
        return mMarker;
    }
    
    
    public void setLineChartProxy(LineChartProxy proxy) {
        lineChartProxy = proxy;
    }
    
    @Override
    protected void prepareAnimatorSet(TiAnimatorSet tiSet, List<Animator> list, List<Animator> listReverse,
            HashMap options) {
        super.prepareAnimatorSet(tiSet, list, listReverse, options);
        if (options.containsKey(TiC.PROPERTY_VALUE)) {
            ObjectAnimator anim = ObjectAnimator.ofInt(this, TiC.PROPERTY_VALUE, TiConvert.toInt(options.get(TiC.PROPERTY_VALUE)));
            list.add(anim);
        }
    }
    
    @Override
    public void onPropertyChanged(String name, Object value) {
        super.onPropertyChanged(name, value);
        if (name.equals(TiC.PROPERTY_VALUE)){
            mMarker.setValue(TiConvert.toFloat(value));
        }
        if (lineChartProxy != null)
            lineChartProxy.refresh();
    }

    @Override
    public void handleCreationDict(KrollDict properties) {
        super.handleCreationDict(properties);
        Log.d(TAG, properties.toString());
        int type = properties.optInt(TiC.PROPERTY_TYPE, 0);
        float value = properties.optFloat(TiC.PROPERTY_VALUE, 0);
        Log.d(TAG, "value test " + properties.get(TiC.PROPERTY_VALUE).toString());
        Log.d(TAG, "value " + value);
        mMarker = (type == 1) ? new XValueMarker(value, properties.optString(
                        TiC.PROPERTY_TITLE, null)) : 
                            new YValueMarker(value, properties.optString(
                        TiC.PROPERTY_TITLE, null));

        Context context = TiApplication.getInstance()
                .getApplicationContext();
        Paint paint = mMarker.getLinePaint();
        Utils.styleStrokeWidth(properties, "lineWidth", "1", paint, context);
        Utils.styleOpacity(properties, "lineOpacity", paint);
        Utils.styleColor(properties, "lineColor", Color.TRANSPARENT, paint);
        Utils.styleCap(properties, "lineCap", paint);
        Utils.styleJoin(properties, "lineJoin", paint);
        Utils.styleEmboss(properties, "lineEmboss", paint);
        Utils.styleDash(properties, "lineDash", paint, context);
        Utils.styleShadow(properties, "shadow", paint, context);

        if (properties.containsKey("label")) {
            Paint paint2 = mMarker.getTextPaint();
            KrollDict labelOptions = properties.getKrollDict("label");
            Utils.styleShadow(labelOptions, "shadow", paint2, context);

            Utils.styleTextWidget(labelOptions, paint2, context);

            if (labelOptions.containsKey("offset")) {
                KrollDict offset = labelOptions.getKrollDict("offset");
                if (mMarker instanceof YValueMarker) {
                    ((YValueMarker) mMarker).vOffset += Utils
                            .getRawSizeOrZero(offset, "y", context);
                } else {
                    ((XValueMarker) mMarker).hOffset += Utils
                            .getRawSizeOrZero(offset, "x", context);
                }
            }
        }
    }

}