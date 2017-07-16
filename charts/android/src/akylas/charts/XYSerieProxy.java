package akylas.charts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.ReusableProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

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
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

@Kroll.proxy(propertyDontEnumAccessors= {
        "data"
})
public class XYSerieProxy extends ReusableProxy {
    
    public static class AkXYSeries extends SimpleXYSeries {
        public WeakReference<XYSerieProxy> proxy = null;
        public AkXYSeries(String title) {
            super(title);
        }
        
    }
	// Standard Debugging variables
	private static final String TAG = "PlotStepProxy";
	protected AkXYSeries series;
	protected String mTitle;
	protected Context context;
	protected LineChartProxy plot;
	protected LineAndPointFormatter formatter;
	protected PointLabelFormatter labelformatter;
	protected KrollFunction labelFormatCallback;
	protected boolean mFillSpacePath = true;
    
    private KrollDict fillGradientProps;
    private KrollDict lineGradientProps;

	public XYSerieProxy() {
		super();
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setPlot(LineChartProxy plot) {
		this.plot = plot;
	}


	public AkXYSeries getSeries() {
		return series;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(HashMap options, KrollProxy rootProxy) {
		mTitle = TiConvert.toString(options, "name", "");
        series = new AkXYSeries(mTitle);
        series.proxy = new WeakReference<XYSerieProxy>(this);
        series.useImplicitXVals();
        if (options.containsKey("implicitXVals")) {
            series.useImplicitXVals();
        }
		super.handleCreationDict(options, rootProxy);
	}
	
	@Kroll.method
    @Kroll.getProperty(enumerable=false)
    public Object getData() {
        return getProperty("data");
    }
	
	@Kroll.method
    @Kroll.setProperty
	public void setData(Object args) {
		Object[] data = (Object[]) args;
		setPropertyJava("data", args);
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

    public LineAndPointFormatter getFormatter() {
        if (formatter == null) {
//            KrollDict options = getProperties();

            labelformatter = null;
            formatter = createFormatter();
            if (formatter != null) {
                formatter.setVertexPaint(null);
            }
        }
        return formatter;
    }
    
    @Override
    protected void didProcessProperties() {
        super.didProcessProperties();
        if (!mFillSpacePath) updateGradients();
    }
   
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "data":
            setData(newValue);
            break;
        case AkylasChartsModule.PROPERTY_LINE_WIDTH:
            Utils.styleStrokeWidth(newValue, "1", getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_OPACITY:
            Utils.styleOpacity(TiConvert.toFloat(newValue, 1.0f), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_COLOR:
            Utils.styleColor(TiConvert.toColor(newValue), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_CAP:
            Utils.styleCap(TiConvert.toInt(newValue), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_JOIN:
            Utils.styleJoin(TiConvert.toInt(newValue), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_EMBOSS:
            Utils.styleEmboss(TiConvert.toKrollDict(newValue), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_LINE_DASH:
            Utils.styleDash(TiConvert.toKrollDict(newValue), getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_SHADOW:
            Utils.styleShadow(TiConvert.toKrollDict(newValue), "shadow", getFormatter().getLinePaint());
            break;
        case AkylasChartsModule.PROPERTY_FILL_OPACITY:
            Utils.styleOpacity(TiConvert.toFloat(newValue, 1.0f), getFormatter().getFillPaint());
            break;
        case AkylasChartsModule.PROPERTY_FILL_COLOR:
            Utils.styleColor(TiConvert.toColor(newValue), getFormatter().getFillPaint());
            break;
        case AkylasChartsModule.PROPERTY_SYMBOL:
            KrollDict symbolsOptions = TiConvert.toKrollDict(newValue);
            Paint paint1 = formatter.getVertexPaint();
            Utils.styleStrokeWidth(symbolsOptions, TiC.PROPERTY_WIDTH, "1", paint1,
                    context);
            Utils.styleColor(symbolsOptions, AkylasChartsModule.PROPERTY_FILL_COLOR, paint1);
            Utils.styleOpacity(symbolsOptions, AkylasChartsModule.PROPERTY_FILL_OPACITY, paint1);
            break;
        case AkylasChartsModule.PROPERTY_FILL_SPACE_PATH:
            mFillSpacePath = TiConvert.toBoolean(newValue,  false);
            break;
        case AkylasChartsModule.PROPERTY_LINE_GRADIENT:
            lineGradientProps = TiConvert.toKrollDict(newValue);
            break;
        case AkylasChartsModule.PROPERTY_FILL_GRADIENT:
            fillGradientProps = TiConvert.toKrollDict(newValue);
            break;
        case AkylasChartsModule.PROPERTY_LABELS:
            if (labelformatter == null) {
                labelformatter = new PointLabelFormatter(Color.BLACK);
            }
            labelformatter.getTextPaint().clearShadowLayer();
            KrollDict labelOptions = TiConvert.toKrollDict(newValue);

            Utils.styleTextWidget(labelOptions, labelformatter.getTextPaint(),
                    context);

            if (labelOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                KrollDict offset = labelOptions.getKrollDict(TiC.PROPERTY_OFFSET);
                labelformatter.vOffset += TiUIHelper.getInPixels(offset, TiC.PROPERTY_X);
                labelformatter.hOffset += TiUIHelper.getInPixels(offset, TiC.PROPERTY_X);
            }
            formatter.setPointLabelFormatter(labelformatter);

            if (labelOptions.containsKey(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK)) {
                labelFormatCallback = (KrollFunction) labelOptions
                        .get(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK);
            } else {
                labelFormatCallback = null;
            }
            if (labelFormatCallback != null) {
                getFormatter().setPointLabeler(new PointLabeler() {
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
            } else {
                getFormatter().setPointLabeler(null);
            }
            break;
        case AkylasChartsModule.PROPERTY_FILL_DIRECTION:
            String value = TiConvert.toString(newValue, TiC.PROPERTY_BOTTOM);
            FillDirection direction = FillDirection.BOTTOM;
            if (value == TiC.PROPERTY_TOP) {
                direction = FillDirection.TOP;
            } else if (value == "origin") {
                direction = FillDirection.RANGE_ORIGIN;
            }
            getFormatter().setFillDirection(direction);
            break;
        default:
            break;
        }
    }
    
    public void addTouchEventData(final long targetValX, final XYPlot xyPlotView, KrollDict data) { 
        String bindId = getBindId();
        if (bindId != null) {
            Long realTargetValX = targetValX;
            int targetIndex = -1;
            Long targetValY = null;
            Long prevValX = null;
            Long prevValY = null;
            for (int i = 0; i < series.size(); ++i) {
                long currValX = series.getX(i).longValue();
                long currValY = series.getY(i).longValue();
                // Calculate the range value of the closest
                // domain value (assumes xyData is sorted in
                // ascending X order)
                if (currValX >= targetValX) {
                    long currDiff = currValX - targetValX;
                    if (prevValX != null && (targetValX
                            - prevValX) < currDiff) {
                        realTargetValX = prevValX;
                        targetValY = prevValY;
                    } else {
                        realTargetValX = currValX;
                        targetValY = currValY;
                    }
                    targetIndex = i;
                    break;
                }
                prevValX = currValX;
                prevValY = currValY;
            }
            if (targetValY != null) {
                float pixelPosX = xyPlotView.getXPix(targetValX)
                        .floatValue();
                float pixelPosY = xyPlotView.getYPix(targetValY)
                        .floatValue();
                KrollDict serieData = new KrollDict();
                serieData.put("plot", this);
                serieData.put("index", targetIndex);
                serieData.put("x", new TiDimension(pixelPosX, TiDimension.TYPE_LEFT).getAsDefault());
                serieData.put("y", new TiDimension(pixelPosY, TiDimension.TYPE_LEFT).getAsDefault());
                serieData.put("xValue", realTargetValX);
                serieData.put("yValue", targetValY);
                data.put(bindId, serieData);
            }
        }
    }
    
 // Methods
    @Kroll.method
    public void select(int index) {
        if (index < 0 || index >= series.size()) {
            return;
        }
        String bindId = getBindId();
        if (bindId != null && hierarchyHasListener(TiC.EVENT_CLICK)) {
            XYPlot thePlot = (XYPlot) plot.getPlot();
            XYGraphWidget widget = thePlot.getGraphWidget();
            Number xVal = series.getX(index);
            Number yVal = series.getY(index);
            float xPix = widget.getXPix(xVal.floatValue());
            float yPix = widget.getYPix(yVal.floatValue());
            KrollDict serieData = new KrollDict();
            serieData.put("plot", this);
            serieData.put("index", index);
            serieData.put("x", new TiDimension(xPix, TiDimension.TYPE_LEFT).getAsDefault());
            serieData.put("y", new TiDimension(yPix, TiDimension.TYPE_LEFT).getAsDefault());
            serieData.put("xValue", xVal);
            serieData.put("yValue", yVal);
            
            KrollDict event = new KrollDict();
            event.put(bindId, serieData);
            plot.fireEvent(TiC.EVENT_CLICK, event, true, false);
        }
    }
}
