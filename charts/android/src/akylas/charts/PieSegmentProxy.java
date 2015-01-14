package akylas.charts;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.animation.TiAnimatorSet;
import org.appcelerator.titanium.proxy.AnimatableReusableProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;

import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class, propertyAccessors={
    TiC.PROPERTY_NAME,
    TiC.PROPERTY_VALUE
})
public class PieSegmentProxy extends AnimatableReusableProxy{
	// Standard Debugging variables
	@SuppressWarnings("unused")
	private static final String TAG = "PieSegmentProxy";
	private Segment segment;
	private SegmentFormatter formatter;
	private String mTitle;
	private Number mValue;
	private Context context;
	private PieChartProxy pieChartProxy;

	public PieSegmentProxy() {
		super();
	}
	
	public PieSegmentProxy(TiContext context) {
		super();
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setFilter(MaskFilter filter) {
		getFormatter().getFillPaint().setMaskFilter(filter);
	}
	
	public void setPieChartProxy(PieChartProxy proxy) {
		pieChartProxy = proxy;
	}
	

	public void updateGradients(Context context, Rect rect) {
		KrollDict options = getProperties();
		if (options.containsKey(AkylasChartsModule.PROPERTY_FILL_GRADIENT)) {
			Paint paint = formatter.getFillPaint();
			KrollDict bgOptions = options.getKrollDict(AkylasChartsModule.PROPERTY_FILL_GRADIENT);
			paint.setShader(
					Utils.styleGradient(bgOptions, context, rect));
			Utils.styleOpacity(options, AkylasChartsModule.PROPERTY_FILL_OPACITY, paint);
		}
		if (options.containsKey(AkylasChartsModule.PROPERTY_LINE_GRADIENT)) {
			Paint[] paints = {formatter.getInnerEdgePaint(), formatter.getOuterEdgePaint(), formatter.getRadialEdgePaint()};
			KrollDict bgOptions = options.getKrollDict(AkylasChartsModule.PROPERTY_LINE_GRADIENT);
			Shader shader = Utils.styleGradient(bgOptions, context, rect);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setShader(shader);
				Utils.styleOpacity(options, AkylasChartsModule.PROPERTY_LINE_OPACITY, paint);
			}
		}

	}

	public Segment getSegment() {
	    if (segment == null) {
	        segment = new Segment(mTitle, mValue);
	    }
		return segment;
	}

	public SegmentFormatter getFormatter() {
		if (formatter == null) {
			KrollDict options = getProperties();

			formatter = new SegmentFormatter(Color.BLACK,Color.TRANSPARENT);
			Paint[] paints = {formatter.getInnerEdgePaint(), formatter.getOuterEdgePaint(), formatter.getRadialEdgePaint()};
			for (int i = 0; i < paints.length; i++) {
				paints[i].setAntiAlias(true);
			}
			Utils.styleStrokeWidth(options, AkylasChartsModule.PROPERTY_LINE_WIDTH, "1", paints, context);
			Utils.styleOpacity(options, AkylasChartsModule.PROPERTY_LINE_OPACITY, paints);
			Utils.styleColor(options, AkylasChartsModule.PROPERTY_LINE_COLOR, paints);
			Utils.styleDash(options, AkylasChartsModule.PROPERTY_LINE_DASH, paints);

			Paint paint = formatter.getFillPaint();
			paint.setAntiAlias(true);
			Utils.styleOpacity(options, AkylasChartsModule.PROPERTY_FILL_OPACITY, paint);
			
			Random rnd = new Random(); 
			int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));  
			Utils.styleColor(options, AkylasChartsModule.PROPERTY_FILL_COLOR, color, paint);

			if (options.containsKey("label")) {
				KrollDict labelOptions = options.getKrollDict("label");

				Utils.styleTextWidget(labelOptions,
						formatter.getLabelPaint(), context);
			}
		}
		return formatter;
	}
	
	@SuppressWarnings("rawtypes")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void prepareAnimatorSet(TiAnimatorSet tiSet, List<Animator> list, List<Animator> listReverse,
            HashMap options) {
	    super.prepareAnimatorSet(tiSet, list, listReverse, options);
		if (options.containsKey(TiC.PROPERTY_VALUE)) {
			ObjectAnimator anim = ObjectAnimator.ofInt(this, TiC.PROPERTY_VALUE, TiConvert.toInt(options.get(TiC.PROPERTY_VALUE)));
			list.add(anim);
		}
	}

	// Start Utility Methods

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
	
	@Override
	public void didProcessProperties() {
	    super.didProcessProperties();
		if (pieChartProxy != null)
			pieChartProxy.refresh();
	}
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case TiC.PROPERTY_NAME:
            mTitle = TiConvert.toString(newValue, "");
            if (segment != null) {
                segment.setTitle(mTitle);
            }
            break;
        case TiC.PROPERTY_VALUE:
            mValue = (Number)newValue;
            if (segment != null) {
                segment.setValue(mValue);
            }
            break;
        default:
            break;
        }
    }
}