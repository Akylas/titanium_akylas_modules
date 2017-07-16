package akylas.charts;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.animation.TiAnimatorSet;
import org.appcelerator.titanium.proxy.AnimatableReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import android.content.Context;
import android.graphics.Paint;

import com.androidplot.xy.ValueMarker;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.YValueMarker;
import android.animation.Animator;
import android.animation.ObjectAnimator;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class, propertyAccessors = { TiC.PROPERTY_VALUE })
@SuppressWarnings("rawtypes")
public class MarkerProxy extends AnimatableReusableProxy {
    // Standard Debugging variables
    @SuppressWarnings("unused")
    private static final String TAG = "MarkerProxy";
    private LineChartProxy lineChartProxy;

    ValueMarker mMarker;
    Paint mPaint;

    public MarkerProxy() {
        super();
    }

    public ValueMarker getMarker() {
        return mMarker;
    }

    public void setLineChartProxy(LineChartProxy proxy) {
        lineChartProxy = proxy;
    }

    @Override
    protected void prepareAnimatorSet(TiAnimatorSet tiSet, List<Animator> list,
            List<Animator> listReverse) {
        super.prepareAnimatorSet(tiSet, list, listReverse);
        HashMap options = tiSet.getToOptions();
        if (options.containsKey(TiC.PROPERTY_VALUE)) {
            ObjectAnimator anim = ObjectAnimator.ofInt(this,
                    TiC.PROPERTY_VALUE,
                    TiConvert.toInt(options.get(TiC.PROPERTY_VALUE)));
            list.add(anim);
        }
    }

    @Override
    public void handleCreationDict(HashMap properties, KrollProxy rootProxy) {
        int type = TiConvert.toInt(properties, TiC.PROPERTY_TYPE, 0);
        mMarker = (type == 1) ? new XValueMarker(0, TiConvert.toString(properties,
                TiC.PROPERTY_TITLE, null)) : new YValueMarker(0,
                        TiConvert.toString(properties, TiC.PROPERTY_TITLE, null));
        mPaint = mMarker.getLinePaint();
        super.handleCreationDict(properties, rootProxy);
    }

    @Override
    public void didProcessProperties() {
        super.didProcessProperties();
        if (lineChartProxy != null)
            lineChartProxy.refresh();
    }

    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_VALUE:
            mMarker.setValue(TiConvert.toFloat(newValue, 0.0f));
            break;
        case AkylasChartsModule.PROPERTY_LINE_EMBOSS:
            Utils.styleEmboss(TiConvert.toKrollDict(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_WIDTH:
            Utils.styleStrokeWidth(newValue, "1", mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_OPACITY:
            Utils.styleOpacity(TiConvert.toFloat(newValue, 1.0f), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_JOIN:
            Utils.styleJoin(TiConvert.toInt(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_COLOR:
            Utils.styleColor(TiConvert.toColor(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_CAP:
            Utils.styleCap(TiConvert.toInt(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_SHADOW:
            Utils.styleShadow(TiConvert.toKrollDict(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LINE_DASH:
            Utils.styleDash(TiConvert.toKrollDict(newValue), mPaint);
            break;
        case AkylasChartsModule.PROPERTY_LABEL:
            Paint paint2 = mMarker.getTextPaint();
            KrollDict labelOptions = TiConvert.toKrollDict(newValue);
            Utils.styleShadow(labelOptions, AkylasChartsModule.PROPERTY_SHADOW,
                    paint2);

            Context context = TiApplication.getAppContext();
            Utils.styleTextWidget(labelOptions, paint2, context);

            if (labelOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                KrollDict offset = labelOptions
                        .getKrollDict(TiC.PROPERTY_OFFSET);
                if (mMarker instanceof YValueMarker) {
                    ((YValueMarker) mMarker).vOffset += TiUIHelper.getInPixels(offset, TiC.PROPERTY_Y);
                } else {
                    ((XValueMarker) mMarker).hOffset += TiUIHelper.getInPixels(offset, TiC.PROPERTY_X);
                }
            }
            break;
        default:
            break;
        }
    }

}