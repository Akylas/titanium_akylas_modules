package akylas.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
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
import com.androidplot.xy.XYSeries;

@Kroll.proxy
public class XYSerieProxy extends ReusableProxy {
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
    
    private KrollDict fillGradientProps;
    private KrollDict lineGradientProps;
    
	// Constructor
	public XYSerieProxy(TiContext tiContext) {
		super();
	}

	public XYSerieProxy() {
		this(null);
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
	public void handleCreationDict(HashMap options) {
		mTitle = TiConvert.toString(options, "name", "");
        series = new SimpleXYSeries(mTitle);
        series.useImplicitXVals();
        if (options.containsKey("implicitXVals")) {
            series.useImplicitXVals();
        }
		super.handleCreationDict(options);
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
}
