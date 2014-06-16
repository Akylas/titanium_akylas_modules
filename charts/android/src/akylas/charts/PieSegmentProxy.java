package akylas.charts;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.animation.TiAnimatorSet;
import org.appcelerator.titanium.proxy.AnimatableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.KrollProxyReusableListener;

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
	TiC.PROPERTY_NAME
})
public class PieSegmentProxy extends AnimatableProxy implements KrollProxyReusableListener {
	// Standard Debugging variables
	@SuppressWarnings("unused")
	private static final String TAG = "PieSegmentProxy";
	private Segment segment;
	private SegmentFormatter formatter;
	private String mTitle;
	private Number mValue;
	private Context context;
	private PieChartProxy pieChartProxy;
    private KrollDict additionalEventData;

	public PieSegmentProxy() {
		super();
		setModelListener(this);
	}
	
	public PieSegmentProxy(TiContext context) {
		super();
		setModelListener(this);
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
		if (options.containsKey("fillGradient")) {
			Paint paint = formatter.getFillPaint();
			KrollDict bgOptions = options.getKrollDict("fillGradient");
			paint.setShader(
					Utils.styleGradient(bgOptions, context, rect));
			Utils.styleOpacity(options, "fillOpacity", paint);
		}
		if (options.containsKey("lineGradient")) {
			Paint[] paints = {formatter.getInnerEdgePaint(), formatter.getOuterEdgePaint(), formatter.getRadialEdgePaint()};
			KrollDict bgOptions = options.getKrollDict("lineGradient");
			Shader shader = Utils.styleGradient(bgOptions, context, rect);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setShader(shader);
				Utils.styleOpacity(options, "lineOpacity", paint);
			}
		}

	}

	public Segment getSegment() {
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
			Utils.styleStrokeWidth(options, "lineWidth", "1", paints, context);
			Utils.styleOpacity(options, "lineOpacity", paints);
			Utils.styleColor(options, "lineColor", paints);
			Utils.styleDash(options, "lineDash", paints, context);

			Paint paint = formatter.getFillPaint();
			paint.setAntiAlias(true);
			Utils.styleOpacity(options, "fillOpacity", paint);
			
			Random rnd = new Random(); 
			int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));  
			Utils.styleColor(options, "fillColor", color, paint);

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
	
	@Kroll.getProperty @Kroll.method
	public int getValue() {
	        return TiConvert.toInt(getProperty(TiC.PROPERTY_VALUE));
	}
	@Kroll.setProperty @Kroll.method
	public void setValue(int value) {
		if (value == segment.getValue().intValue()) return;
		segment.setValue(value);
		if (pieChartProxy != null)
			pieChartProxy.refresh();
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {
		if (key.equals(TiC.PROPERTY_VALUE)){
			
		} else if (key.equals(TiC.PROPERTY_NAME)){
			segment.setTitle(TiConvert.toString(newValue));
		}
		if (pieChartProxy != null)
			pieChartProxy.refresh();
	}

	@Override
	public void processProperties(KrollDict properties) {
		mTitle = properties.optString(TiC.PROPERTY_NAME, "");
		mValue = (Number) properties.get(TiC.PROPERTY_VALUE);
		segment = new Segment(mTitle, mValue);
	}

    @Override
    public void setAdditionalEventData(KrollDict dict) {
        additionalEventData = dict;
    }
    
    @Override
    public KrollDict getAdditionalEventData() {
        return additionalEventData;
    }

    @Override
    public void setReusing(boolean reusing) {
//        if (reusing == false) {
//            update();
//        }
        
    }

    @Override
    public void listenerAdded(String arg0, int arg1, KrollProxy arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void listenerRemoved(String arg0, int arg1, KrollProxy arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void propertiesChanged(List<KrollPropertyChange> arg0,
            KrollProxy arg1) {
        // TODO Auto-generated method stub
        
    }
}