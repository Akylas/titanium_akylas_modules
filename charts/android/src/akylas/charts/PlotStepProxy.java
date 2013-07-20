package akylas.charts;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYSeries;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class PlotStepProxy extends XYSerieProxy {
	// Standard Debugging variables
	private static final String TAG = "PlotStepProxy";
	private StepFormatter formatter;
	private KrollFunction labelFormatCallback;

	// Constructor
	public PlotStepProxy(TiContext tiContext) {
		super(tiContext);
	}

	public PlotStepProxy() {
		super();
	}

	@Override
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
			Paint paint = getFormatter().getLinePaint();
			KrollDict bgOptions = options.getKrollDict("lineGradient");
			paint.setShader(Utils.styleGradient(bgOptions, context, rect));
			Utils.styleCap(options, "lineCap", paint);
			Utils.styleOpacity(options, "lineOpacity", paint);
		}
	}

	@Override
	public StepFormatter getFormatter() {
		if (formatter == null) {
			KrollDict options = getProperties();

			formatter = new StepFormatter(Color.BLACK, Color.TRANSPARENT);
			formatter.setPointLabelFormatter(null);
			Paint paint = formatter.getLinePaint();
			paint.setAntiAlias(false);
			Utils.styleStrokeWidth(options, "lineWidth", "1", paint, context);
			Utils.styleOpacity(options, "lineOpacity", paint);
			Utils.styleColor(options, "lineColor", paint);
			Utils.styleCap(options, "lineCap", paint);
			Utils.styleJoin(options, "lineJoin", paint);
			Utils.styleEmboss(options, "lineEmboss", paint);
			Utils.styleDash(options, "lineDash", paint, context);

			paint = formatter.getFillPaint();
			Utils.styleColor(options, "fillColor", paint);
			Utils.styleOpacity(options, "fillOpacity", paint);
			
			if (options.containsKey("labels")) {
				PointLabelFormatter labelformatter = new PointLabelFormatter(Color.BLACK);
				labelformatter.getTextPaint().clearShadowLayer();
				KrollDict labelOptions = options.getKrollDict("labels");

				Utils.styleTextWidget(labelOptions,
						labelformatter.getTextPaint(), context);

				if (labelOptions.containsKey("offset")) {
					KrollDict offset = labelOptions.getKrollDict("offset");
					labelformatter.vOffset += Utils.getRawSizeOrZero(offset,
							"y", context);
					labelformatter.hOffset += Utils.getRawSizeOrZero(offset,
							"x", context);
				}
				formatter.setPointLabelFormatter(labelformatter);
				
				if (labelOptions.containsKey("formatCallback")) {
					labelFormatCallback = (KrollFunction) labelOptions.get("formatCallback");
				}
				else {
					labelFormatCallback = null;
				}
				if (labelFormatCallback != null) {
					formatter.setPointLabeler(new PointLabeler() {
				        @Override
				        public String getLabel(XYSeries series, int index) {
				        	Object result = labelFormatCallback.call(krollObject, new Object[]{(Number) index, (Number) series.getX(index), (Number) series.getY(index)});
				            return TiConvert.toString(result);
				        }
				    });
				}
			}

			if (options.containsKey("symbol")) {
				formatter.getVertexPaint().setColor(Color.BLACK);
				KrollDict symbolsOptions = options.getKrollDict("symbol");
				Paint paint1 = formatter.getVertexPaint();
				Utils.styleStrokeWidth(symbolsOptions, "width", "1", paint1,
						context);
				Utils.styleColor(symbolsOptions, "fillColor", paint1);
				Utils.styleOpacity(symbolsOptions, "fillOpacity", paint);
			}
			else {
				formatter.setVertexPaint(null);
			}
		}
		return formatter;
	}


	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options) {
		Log.d(TAG, "handleCreationDict ");
		super.handleCreationDict(options);
	}
}