package akylas.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollPropertyChange;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.KrollProxyReusableListener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.androidplot.Plot;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

@Kroll.proxy
public class XYSerieProxy extends KrollProxy implements KrollProxyReusableListener {
	// Standard Debugging variables
	private static final String TAG = "PlotStepProxy";
	protected SimpleXYSeries series;
	protected String mTitle;
	protected Context context;
	protected LineChartProxy plot;
	protected LineAndPointFormatter formatter;
	protected PointLabelFormatter labelformatter;
	protected KrollFunction labelFormatCallback;
	protected boolean mFillSpacePath = true;
    private KrollDict additionalEventData;
    
    private KrollDict fillGradientProps;
    private KrollDict lineGradientProps;
    
	// Constructor
	public XYSerieProxy(TiContext tiContext) {
		super(tiContext);
	}

	public XYSerieProxy() {
		super();
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setPlot(LineChartProxy plot) {
		this.plot = plot;
	}


	public SimpleXYSeries getSeries() {
		return series;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options) {
		Log.d(TAG, "handleCreationDict ");
		mTitle = options.optString("name", "");
        series = new SimpleXYSeries(mTitle);
        series.useImplicitXVals();
        if (options.containsKey("implicitXVals")) {
            series.useImplicitXVals();
        }
		super.handleCreationDict(options);
        setModelListener(this);
	}

	@Kroll.method
	public void setData(Object args) {
		Object[] data = (Object[]) args;
		if (data == null || data.length == 0)
			series.setModel(new ArrayList<Number>(),
					SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
		else {
			if (data[0].getClass().isArray()) {
				List<Number> model = new ArrayList<Number>();
				Object[] x = (Object[]) (data[0]);
				Object[] y = (Object[]) (data[1]);
				int length = x.length;
				for (int i = 0; i < length; i++) {
					model.add((Number) x[i]);
					model.add((Number) y[i]);
				}
				series.setModel(model,
						SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED);
			} else {
				series.setModel(Arrays.asList(TiConvert.toNumberArray(data)),
						SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
			}
		}
		if (plot != null) {
			plot.update();
		}
	}
	
	

	// Methods
	@Kroll.method
	public void useImplicitXVals() {
		series.useImplicitXVals();
	}
	@Kroll.method
	public void removeFirst() {
		series.removeFirst();
	}
	@Kroll.method
	public void removeLast() {
		series.removeLast();
	}
	@Kroll.method
	public void addFirst(Object x, Object y) {
		series.addFirst((Number)x, (Number)y);
	}
	@Kroll.method
	public void addLast(Object x, Object y) {
		series.addLast((Number)x, (Number)y);
	}
	
	@Kroll.method
	public int size() {
		return series.size();
	}
	
    protected void internalUpdateFillGradient(Context context, Rect rect) {
        if (fillGradientProps != null) {
            Paint paint = formatter.getFillPaint();
            paint.setColor(Color.WHITE);
            paint.setShader(
                    Utils.styleGradient(fillGradientProps, context, rect));
        }
    }
    
    protected void internalUpdateLineGradient(Context context, Rect rect) {
        if (lineGradientProps != null) {
            Paint paint = getFormatter().getLinePaint();
            paint.setColor(Color.WHITE);
            paint.setShader(Utils.styleGradient(lineGradientProps, context, rect));
//            Utils.styleCap(options, "lineCap", paint);
        }
    }
    
    protected void updateGradients() {
        if (mFillSpacePath|| plot == null) return;
        Plot thePlot = plot.getPlot();
        if (thePlot != null) {
            updateGradients(thePlot.getContext(), plot.getGraphRect());
        }
    }


    protected void updateGradients(Context context, Rect rect) {
        if (mFillSpacePath) return;
        internalUpdateFillGradient(context, rect);
        internalUpdateLineGradient(context, rect);
    }
    
    protected LineAndPointFormatter createFormatter() {
        return null;
    }
    
    protected void styleFormatter(KrollDict options) {
        Paint paint = formatter.getLinePaint();
        Utils.styleStrokeWidth(options, "lineWidth", "1", paint, context);
        Utils.styleOpacity(options, "lineOpacity", paint);
        Utils.styleColor(options, "lineColor", paint);
        Utils.styleCap(options, "lineCap", paint);
        Utils.styleJoin(options, "lineJoin", paint);
        Utils.styleEmboss(options, "lineEmboss", paint);
        Utils.styleDash(options, "lineDash", paint, context);
        Utils.styleShadow(options, "shadow", paint, context);

        paint = formatter.getFillPaint();
        Utils.styleColor(options, "fillColor", paint);
        Utils.styleOpacity(options, "fillOpacity", paint);
        if (options.containsKey("fillDirection")) {
            String value = TiConvert.toString(options, "fillDirection",
                    TiC.PROPERTY_BOTTOM);
            FillDirection direction = FillDirection.BOTTOM;
            if (value == TiC.PROPERTY_TOP) {
                direction = FillDirection.TOP;
            } else if (value == "origin") {
                direction = FillDirection.RANGE_ORIGIN;
            }
            formatter.setFillDirection(direction);
        }
        mFillSpacePath = TiConvert.toBoolean(options, "fillSpacePath",  false);

        if (options.containsKey("labels")) {
            labelformatter = new PointLabelFormatter(Color.BLACK);
            labelformatter.getTextPaint().clearShadowLayer();
            KrollDict labelOptions = options.getKrollDict("labels");

            Utils.styleTextWidget(labelOptions, labelformatter.getTextPaint(),
                    context);

            if (labelOptions.containsKey("offset")) {
                KrollDict offset = labelOptions.getKrollDict("offset");
                labelformatter.vOffset += Utils.getRawSizeOrZero(offset, "y",
                        context);
                labelformatter.hOffset += Utils.getRawSizeOrZero(offset, "x",
                        context);
            }
            formatter.setPointLabelFormatter(labelformatter);

            if (labelOptions.containsKey("formatCallback")) {
                labelFormatCallback = (KrollFunction) labelOptions
                        .get("formatCallback");
            } else {
                labelFormatCallback = null;
            }
            if (labelFormatCallback != null) {
                formatter.setPointLabeler(new PointLabeler() {
                    @Override
                    public String getLabel(XYSeries series, int index) {
                        Object result = labelFormatCallback.call(
                                krollObject,
                                new Object[] { (Number) index,
                                        (Number) series.getX(index),
                                        (Number) series.getY(index) });
                        return TiConvert.toString(result);
                    }
                });
            }
        }
        if (options.containsKey("symbol")) {
            KrollDict symbolsOptions = options.getKrollDict("symbol");
            Paint paint1 = formatter.getVertexPaint();
            Utils.styleStrokeWidth(symbolsOptions, "width", "1", paint1,
                    context);
            Utils.styleColor(symbolsOptions, "fillColor", paint1);
            Utils.styleOpacity(symbolsOptions, "fillOpacity", paint);
        } else {
            formatter.setVertexPaint(null);
        }
        
        if (options.containsKey("lineGradient")) {
            lineGradientProps = options.getKrollDict("lineGradient");
        }
        if (options.containsKey("fillGradient")) {
            fillGradientProps = options.getKrollDict("fillGradient");
        }
        if (!mFillSpacePath) updateGradients();
    }

    public LineAndPointFormatter getFormatter() {
        if (formatter == null) {
//            KrollDict options = getProperties();

            labelformatter = null;
            formatter = createFormatter();
//            styleFormatter(options);
        }
        return formatter;
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
    public void processProperties(KrollDict d) {
        getFormatter();
        styleFormatter(d);
        if (d.containsKey("data")) {
            setData((Object[]) d.get("data"));
        }
    }

    @Override
    public void propertiesChanged(List<KrollPropertyChange> arg0,
            KrollProxy arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void propertyChanged(String key, Object oldValue, Object newValue,
            KrollProxy arg3) {
        if (key.equals("data")) {
            setData(newValue);
        }
    }
}
