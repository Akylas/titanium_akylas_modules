package akylas.charts;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.UIModule;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BarRenderer.BarRenderStyle;
import com.androidplot.xy.BarRenderer.BarWidthStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.ValueMarker;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YValueMarker;

// This proxy can be created by calling Android.createExample({message: "hello world"})
@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
@SuppressLint("UseSparseArrays")
@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class LineChartProxy extends ChartProxy {
	// Standard Debugging variables
	private static final String TAG = "LineChartProxy";
	private final List<XYSerieProxy> mPlots;

	private XYPlot xyPlotView;
	private boolean userInteractionEnabled = false;
	private boolean panEnabled = false;
	private boolean zoomEnabled = false;
	private boolean clampInteraction = false;
	private boolean needsBoundarySet = false;
	private boolean barRenderInitiated = false;
	private KrollFunction formatDomainCallback;
	private KrollFunction formatRangeCallback;
    private ArrayList<MarkerProxy> preloadMarkers = null;

	private final ArrayList<MarkerProxy> mMarkers;

	private class LineChartView extends ChartView implements OnTouchListener {
		// private PointF minXY;
		// private PointF maxXY;
	    private boolean needsUpdateGradients = true;

		protected Class viewClass() {
			return XYPlot.class;
		}
		
		public void updateGradient() {
		    updateGradient(false);
		}
		public void updateGradient(final boolean force) {
		    if (force || needsUpdateGradients == true) {
		        needsUpdateGradients = false;
		        Rect rect = new Rect();
	            ((XYPlot) plotView).getGraphWidget().getWidgetDimensions().paddedRect.round(rect);
	            updateGradients(layout.getContext(), rect);
	            for (int i = 0; i < mPlots.size(); i++) {
	                XYSerieProxy lProxy = mPlots.get(i);
	                lProxy.updateGradients(layout.getContext(), rect);
	            }
		    }
		}

		protected void onLayoutChanged() {
		    needsUpdateGradients = true;
		    updateGradient();
		}

		protected void beforeLayoutNativeView() {
			xyPlotView = (XYPlot) plotView;
			xyPlotView.setRangeBoundaries(0, 0, BoundaryMode.AUTO);
            xyPlotView.setDomainBoundaries(0, 0, BoundaryMode.AUTO);
			xyPlotView.getGraphWidget().setGridPadding(0,0,0,0);
            xyPlotView.getGraphWidget().setMargins(0,0,0,0);
            xyPlotView.getGraphWidget().setClippingEnabled(false);
            xyPlotView.getGraphWidget().setRangeLabelWidth(0);
            xyPlotView.getGraphWidget().setDomainLabelWidth(0);
            xyPlotView.getGraphWidget().setDomainOriginLabelOffset(0);
            xyPlotView.getGraphWidget().setRangeOriginLabelOffset(0);
            xyPlotView.getLegendWidget().setVisible(false);
            for (int i = 0; i < mPlots.size(); i++) {
				addSerie(mPlots.get(i), false);
			}
            for (int i = 0; i < mMarkers.size(); i++) {
                ValueMarker marker = mMarkers.get(i).getMarker();
                if (marker instanceof YValueMarker)
                    xyPlotView.addMarker((YValueMarker) marker);
                else
                    xyPlotView.addMarker((XValueMarker) marker);
            }
			updateMinMax();
		}

		public void initBarRenderer() {
			if (barRenderInitiated == true)
				return;
			barRenderInitiated = true;
			BarRenderer renderer = ((BarRenderer) xyPlotView
					.getRenderer(BarRenderer.class));
			if (renderer == null) {
				barRenderInitiated = false;
				return;
			}
			renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.OVERLAID);
			renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.FIXED_WIDTH);
			renderer.setBarWidth(20);
			renderer.setBarGap(10);
			KrollDict d = proxy.getProperties();
			if (d.containsKey("barPlot")) {
				KrollDict barPlotOptions = d.getKrollDict("barPlot");
				if (barPlotOptions.containsKey("barStyle")) {
					renderer.setBarRenderStyle(BarRenderStyle.values()[barPlotOptions
							.getInt("barStyle")]);
				}
				if (barPlotOptions.containsKey("barWidthStyle")) {
					renderer.setBarWidthStyle(BarWidthStyle.values()[barPlotOptions
							.getInt("barWidthStyle")]);
				}
				if (barPlotOptions.containsKey("barWidth")) {
					float width = Utils.getRawSize(barPlotOptions, "barWidth",
							plotView.getContext());
					renderer.setBarWidth(width);
				}

				if (barPlotOptions.containsKey("barGap")) {
					float width = Utils.getRawSize(barPlotOptions, "barGap",
							plotView.getContext());
					renderer.setBarGap(width);
				}
			}
		}

		public void handleInteraction() {
			if (userInteractionEnabled || zoomEnabled || panEnabled) {
				plotView.setOnTouchListener(this);
			} else {
				plotView.setOnTouchListener(null);
			}
		}

		protected void postCreateNativeView() {
			super.postCreateNativeView();
			handleInteraction();

			if (needsBoundarySet && (panEnabled || zoomEnabled)) {
				xyPlotView.setDomainBoundaries(newMinX, newMaxX,
						BoundaryMode.FIXED);
			}
		}

		public LineChartView(final TiViewProxy proxy, Activity activity) {
			super(proxy, activity);
		}

		public void updateMinMax() {
			if (xyPlotView == null)
				return;

			// next we go through each series to update our min/max values:
			for (XYSeries series : xyPlotView.getSeriesSet()) {
				// step through each point in each series:
				synchronized (series) {
					for (int i = 0; i < series.size(); i++) {
						Number thisX = series.getX(i);
						Number thisY = series.getY(i);
						if (thisX != null
								&& (minXSeriesValue == null || thisX
										.doubleValue() < minXSeriesValue
										.doubleValue())) {
							minXSeriesValue = thisX;
						}

						// Number thisMaxX = series.getMaxX();
						if (thisX != null
								&& (maxXSeriesValue == null || thisX
										.doubleValue() > maxXSeriesValue
										.doubleValue())) {
							maxXSeriesValue = thisX;
						}

						// Number thisMinY = series.getMinY();
						if (thisY != null
								&& (minYSeriesValue == null || thisY
										.doubleValue() < minYSeriesValue
										.doubleValue())) {
							minYSeriesValue = thisY;
						}

						// Number thisMaxY = series.getMaxY();
						if (thisY != null
								&& (maxYSeriesValue == null || thisY
										.doubleValue() > maxYSeriesValue
										.doubleValue())) {
							maxYSeriesValue = thisY;
						}
					}
				}
			}
		}

		protected void updateGradients(Context context, Rect rect) {
			super.updateGradients(context, rect);
			KrollDict d = proxy.getProperties();

			if (d.containsKey(AkylasChartsModule.PROPERTY_GRID_AREA)) {
				KrollDict gridOptions = d.getKrollDict(AkylasChartsModule.PROPERTY_GRID_AREA);
				if (gridOptions.containsKey(TiC.PROPERTY_BACKGROUND_GRADIENT)) {
					KrollDict bgOptions = gridOptions
							.getKrollDict(TiC.PROPERTY_BACKGROUND_GRADIENT);
					xyPlotView.getGraphWidget().getOrCreateGridBackgroundPaint()
							.setShader(Utils.styleGradient(bgOptions, context, rect));
				}
			}
//			if (d.containsKey("plotArea")) {
//                KrollDict options = d.getKrollDict("plotArea");
//                if (options.containsKey(TiC.PROPERTY_BACKGROUND_GRADIENT)) {
//                    KrollDict bgOptions = options
//                            .getKrollDict(TiC.PROPERTY_BACKGROUND_GRADIENT);
//                    xyPlotView.getGraphWidget().getOrCreateBackgroundPaint()
//                            .setShader(Utils.styleGradient(bgOptions, context, rect));
//                }
//            }
		}
		
		@Override
	    public void propertySet(String key, Object newValue, Object oldValue,
	            boolean changedProperty) {
	        switch (key) {
	        case AkylasChartsModule.PROPERTY_PLOT_SPACE:
	            KrollDict plotOptions = TiConvert.toKrollDict(newValue);
                boolean scaleToFit = plotOptions
                        .optBoolean(AkylasChartsModule.PROPERTY_SCALE_TO_FIT, false);
                if (scaleToFit) {
                    xyPlotView.setRangeBoundaries(0, 0, BoundaryMode.AUTO);
                    xyPlotView.setDomainBoundaries(0, 0, BoundaryMode.AUTO);
                } else {
                    needsBoundarySet = true;
                    if (plotOptions.containsKey(AkylasChartsModule.PROPERTY_RANGE_X)) {
                        KrollDict xOptions = plotOptions.getKrollDict(AkylasChartsModule.PROPERTY_RANGE_X);
                        if (xOptions.containsKey(TiC.PROPERTY_MIN)
                                && xOptions.containsKey(TiC.PROPERTY_MAX)) {
                            int min = xOptions.getInt(TiC.PROPERTY_MIN);
                            int max = xOptions.getInt(TiC.PROPERTY_MAX);
                            xyPlotView.setDomainBoundaries(min, max,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        } else if (xOptions.containsKey(TiC.PROPERTY_MIN)) {
                            int min = xOptions.getInt(TiC.PROPERTY_MIN);
                            xyPlotView.setDomainLowerBoundary(min,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        } else if (xOptions.containsKey(TiC.PROPERTY_MAX)) {
                            int max = xOptions.getInt(TiC.PROPERTY_MAX);
                            xyPlotView.setDomainUpperBoundary(max,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        }
                    }
                    if (plotOptions.containsKey(AkylasChartsModule.PROPERTY_RANGE_Y)) {
                        KrollDict yOptions = plotOptions.getKrollDict(AkylasChartsModule.PROPERTY_RANGE_Y);
                        if (yOptions.containsKey(TiC.PROPERTY_MIN)
                                && yOptions.containsKey(TiC.PROPERTY_MAX)) {
                            int min = yOptions.getInt(TiC.PROPERTY_MIN);
                            int max = yOptions.getInt(TiC.PROPERTY_MAX);
                            xyPlotView.setRangeBoundaries(min, max,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        } else if (yOptions.containsKey(TiC.PROPERTY_MIN)) {
                            int min = yOptions.getInt(TiC.PROPERTY_MIN);
                            xyPlotView.setRangeLowerBoundary(min,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        } else if (yOptions.containsKey(TiC.PROPERTY_MAX)) {
                            int max = yOptions.getInt(TiC.PROPERTY_MAX);
                            xyPlotView.setRangeUpperBoundary(max,
                                    BoundaryMode.FIXED);
                            needsBoundarySet = false;
                        }
                    }
                }
	            break;
	        case AkylasChartsModule.PROPERTY_GRID_AREA:
	        {
	            KrollDict gridOptions = TiConvert.toKrollDict(newValue);
                Paint paint1 = xyPlotView.getGraphWidget()
                        .getOrCreateGridBackgroundPaint();
                Utils.styleColor(gridOptions, TiC.PROPERTY_BACKGROUND_COLOR,
                        Color.TRANSPARENT, paint1);
                Utils.styleOpacity(gridOptions, TiC.PROPERTY_BACKGROUND_OPACITY, paint1);
                Utils.styleMargins(gridOptions, xyPlotView.getGraphWidget(),
                        "setGridPadding", plotView.getContext());
                break;
	        }
	        case AkylasChartsModule.PROPERTY_LEGEND:
	            KrollDict legend = TiConvert.toKrollDict(newValue);
                if (legend.containsKey(TiC.PROPERTY_VISIBLE))
                    xyPlotView.getLegendWidget().setVisible(
                            legend.getBoolean(TiC.PROPERTY_VISIBLE));
                break;
	        case AkylasChartsModule.PROPERTY_AXIS_X:
	        {
	            HashMap currentOptions = TiConvert.toHashMap(oldValue);
                KrollDict axisOptions = TiConvert.toKrollDict(newValue);
                if (currentOptions != null) {
//                    axisOptions = KrollDict.merge(currentOptions, axisOptions);
                    KrollDict.merge(currentOptions, axisOptions, false);
                }
                Context context = plotView.getContext();
                
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_ORIGIN)) {
                    xyPlotView.setUserDomainOrigin((Number) axisOptions
                            .get(AkylasChartsModule.PROPERTY_ORIGIN));
                }
                
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_ALIGN)) {
                    xyPlotView.getGraphWidget().setRangeAxisAlignment(Utils.gravityFromAlignment(axisOptions
                            .getInt(AkylasChartsModule.PROPERTY_ALIGN)));
                }

                Paint paint = xyPlotView.getGraphWidget()
                        .getOrCreateRangeOriginLinePaint();
                Utils.styleColor(axisOptions, AkylasChartsModule.PROPERTY_LINE_COLOR, Color.TRANSPARENT, paint);
                Utils.styleStrokeWidth(axisOptions, AkylasChartsModule.PROPERTY_LINE_WIDTH, paint, context);

                if (axisOptions.containsKey(TiC.PROPERTY_TITLE)) {
                    KrollDict titleOptions = axisOptions.getKrollDict(TiC.PROPERTY_TITLE);
                    if (titleOptions.containsKey(TiC.PROPERTY_TEXT)) {
                        xyPlotView.setDomainLabel(titleOptions
                                .getString(TiC.PROPERTY_TEXT));
                    }

                    Utils.styleTextWidget(titleOptions, xyPlotView
                            .getGraphWidget().getOrCreateDomainOriginLabelPaint(), context);
                    if (titleOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                        xyPlotView.getGraphWidget().setDomainOriginLabelOffset(Utils.getRawSizeOrZero(titleOptions, TiC.PROPERTY_OFFSET, context));
                    }
                    if (titleOptions.containsKey(AkylasChartsModule.PROPERTY_ANGLE)) {
                        xyPlotView.getGraphWidget().setDomainOriginLabelAngle(Utils.getRawSizeOrZero(titleOptions, AkylasChartsModule.PROPERTY_ANGLE, context));
                    }
                }

                int ticksPerDomain = 1;
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_MINOR_TICKS)) {
                    KrollDict minorOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_MINOR_TICKS);
                    if (minorOptions.containsKey(TiC.PROPERTY_COUNT)) {
                        ticksPerDomain = minorOptions.getInt(TiC.PROPERTY_COUNT);
                    }
                    if (minorOptions.containsKey(AkylasChartsModule.PROPERTY_GRID_LINES)) {
                        KrollDict gridOptions = minorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_GRID_LINES);
                        Paint paint1 = xyPlotView.getGraphWidget()
                                .getOrCreateDomainSubGridLinePaint();
                        
                        Utils.styleCap(gridOptions, AkylasChartsModule.PROPERTY_CAP, paint1);
                        Utils.styleJoin(gridOptions, AkylasChartsModule.PROPERTY_JOIN, paint1);
                        Utils.styleEmboss(gridOptions, AkylasChartsModule.PROPERTY_EMBOSS, paint1);
                        Utils.styleDash(gridOptions, AkylasChartsModule.PROPERTY_DASH, paint1);
                        Utils.styleShadow(gridOptions, AkylasChartsModule.PROPERTY_SHADOW, paint1);
                        Utils.styleColor(gridOptions, TiC.PROPERTY_COLOR, Color.TRANSPARENT, paint1);
                        Utils.styleStrokeWidth(gridOptions, paint1, context);
                        Utils.styleOpacity(gridOptions, paint1);
                    }
                }
                // xyPlotView.setDomainStep(XYStepMode.SUBDIVIDE, 9);
                // xyPlotView.setRangeStep(XYStepMode.SUBDIVIDE, 9);

                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_INTERVAL_PX)) {
                    double interval = axisOptions.getDouble(AkylasChartsModule.PROPERTY_INTERVAL_PX);
                    xyPlotView.setTicksPerDomainLabel((int) interval);
                    xyPlotView.setDomainStep(XYStepMode.INCREMENT_BY_PIXELS,
                            interval / ticksPerDomain);
                }

                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_MAJOR_TICKS)) {
                    KrollDict majorOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_MAJOR_TICKS);

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_INTERVAL)) {
                        double interval = majorOptions.getDouble(AkylasChartsModule.PROPERTY_INTERVAL);
                        xyPlotView.setTicksPerDomainLabel((int) interval);
                        xyPlotView.setDomainStep(XYStepMode.INCREMENT_BY_VAL,
                                interval / ticksPerDomain);
                    }

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_LABELS)) {
                        Paint labelPaint = xyPlotView.getGraphWidget()
                                .getOrCreateDomainLabelPaint();
                        KrollDict labelOptions = majorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_LABELS);
                        Utils.styleTextWidget(labelOptions, labelPaint, context);

                        if (labelOptions.containsKey(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK)) {
                            formatDomainCallback = (KrollFunction) labelOptions
                                    .get(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK);
                        } else {
                            formatDomainCallback = null;
                        }
                        if (formatDomainCallback != null) {
                            xyPlotView.getGraphWidget().setDomainValueFormat(
                                    new Format() {
                                        @Override
                                        public StringBuffer format(
                                                Object object,
                                                StringBuffer buffer,
                                                FieldPosition field) {
                                            Object result = formatDomainCallback
                                                    .call(krollObject,
                                                            new Object[] { (Number) object });
                                            buffer.append(TiConvert
                                                    .toString(result));
                                            return buffer;
                                        }

                                        @Override
                                        public Object parseObject(
                                                String string,
                                                ParsePosition position) {
                                            return null;
                                        }
                                    });
                        } else {
//                          xyPlotView.getGraphWidget().setDomainValueFormat(
//                                  new DecimalFormat("0.0"));
                            if (labelOptions.containsKey(AkylasChartsModule.PROPERTY_LOCATIONS)) {
                                Object[] locations = (Object[]) labelOptions
                                        .get(AkylasChartsModule.PROPERTY_LOCATIONS);
                                if (locations != null) {
                                    for (int i = 0; i < locations.length; i++) {
                                        HashMap location = (HashMap) locations[i];
                                        String text = (String) location
                                                .get(TiC.PROPERTY_TEXT);
                                        Number value = (Number) location
                                                .get(TiC.PROPERTY_VALUE);
                                        if (value != null && text != null) {
                                            xyPlotView.getGraphWidget()
                                                    .addDomainValueFormat(
                                                            value, text);
                                        }
                                    }
                                }
                            }
                            Utils.styleValueFormat(labelOptions, xyPlotView.getGraphWidget(),
                                    "setDomainValueFormat");
                        }
                        if (labelOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                            xyPlotView.getGraphWidget()
                                    .setDomainLabelVerticalOffset(
                                            Utils.getRawSize(labelOptions,
                                                    TiC.PROPERTY_OFFSET, context));
                        }
                    }

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_GRID_LINES)) {
                        KrollDict gridOptions = majorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_GRID_LINES);
                        Paint paint1 = xyPlotView.getGraphWidget()
                                .getOrCreateDomainGridLinePaint();
                        Utils.styleCap(gridOptions, AkylasChartsModule.PROPERTY_CAP, paint1);
                        Utils.styleJoin(gridOptions, AkylasChartsModule.PROPERTY_JOIN, paint1);
                        Utils.styleEmboss(gridOptions, AkylasChartsModule.PROPERTY_EMBOSS, paint1);
                        Utils.styleDash(gridOptions, AkylasChartsModule.PROPERTY_DASH, paint1);
                        Utils.styleShadow(gridOptions, AkylasChartsModule.PROPERTY_SHADOW, paint1);
                        Utils.styleColor(gridOptions, TiC.PROPERTY_COLOR, Color.TRANSPARENT, paint1);
                        Utils.styleStrokeWidth(gridOptions, paint1, context);
                        Utils.styleOpacity(gridOptions, paint1);
                    }
                }
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_VISIBLE_RANGE)) {
                    KrollDict visibleRangeOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_VISIBLE_RANGE);
                    if (visibleRangeOptions.containsKey(TiC.PROPERTY_MIN)) {
                        xyPlotView.setDomainLeftMin(visibleRangeOptions
                                .getFloat(TiC.PROPERTY_MIN));
                    }
                    if (visibleRangeOptions.containsKey(TiC.PROPERTY_MAX)) {
                        xyPlotView.setDomainRightMax(visibleRangeOptions
                                .getFloat(TiC.PROPERTY_MAX));
                    }
                }

                xyPlotView.setTicksPerDomainLabel(ticksPerDomain);
                break;
	        }
	        case AkylasChartsModule.PROPERTY_AXIS_Y:
	        {
	            HashMap currentOptions = TiConvert.toHashMap(oldValue);
                KrollDict axisOptions = TiConvert.toKrollDict(newValue);
                if (currentOptions != null) {
//                    axisOptions = KrollDict.merge(currentOptions, axisOptions);
                    KrollDict.merge(currentOptions, axisOptions, false);
                }
                Context context = plotView.getContext();
                
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_ORIGIN)) {
                    xyPlotView.setUserRangeOrigin((Number) axisOptions
                            .get(AkylasChartsModule.PROPERTY_ORIGIN));
                }
                
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_ALIGN)) {
                    xyPlotView.getGraphWidget().setDomainAxisAlignment(Utils.gravityFromAlignment(axisOptions
                                                            .getInt(AkylasChartsModule.PROPERTY_ALIGN)));
                }
                Paint paint = xyPlotView.getGraphWidget()
                        .getOrCreateDomainOriginLinePaint();
                Utils.styleColor(axisOptions, AkylasChartsModule.PROPERTY_LINE_COLOR, Color.TRANSPARENT, paint);
                Utils.styleStrokeWidth(axisOptions, AkylasChartsModule.PROPERTY_LINE_WIDTH, paint, context);

                if (axisOptions.containsKey(TiC.PROPERTY_TITLE)) {
                    KrollDict titleOptions = axisOptions.getKrollDict(TiC.PROPERTY_TITLE);
                    if (titleOptions.containsKey(TiC.PROPERTY_TEXT)) {
                        xyPlotView.setRangeLabel(titleOptions
                                .getString(TiC.PROPERTY_TEXT));
                    }

                    Utils.styleTextWidget(titleOptions, xyPlotView
                            .getGraphWidget().getOrCreateRangeOriginLabelPaint(), context);
                    if (titleOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                        xyPlotView.getGraphWidget().setRangeOriginLabelOffset(Utils.getRawSizeOrZero(titleOptions, TiC.PROPERTY_OFFSET, context));
                    }
                    if (titleOptions.containsKey(AkylasChartsModule.PROPERTY_ANGLE)) {
                        xyPlotView.getGraphWidget().setRangeOriginLabelAngle(Utils.getRawSizeOrZero(titleOptions, AkylasChartsModule.PROPERTY_ANGLE, context));
                    }
                }

                int ticksPerRange = 1;
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_MINOR_TICKS)) {
                    KrollDict minorOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_MINOR_TICKS);
                    if (minorOptions.containsKey(TiC.PROPERTY_COUNT)) {
                        ticksPerRange = minorOptions.getInt(TiC.PROPERTY_COUNT);
                    }
                    if (minorOptions.containsKey(AkylasChartsModule.PROPERTY_GRID_LINES)) {
                        KrollDict gridOptions = minorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_GRID_LINES);
                        Paint paint1 = xyPlotView.getGraphWidget()
                                .getOrCreateRangeSubGridLinePaint();
                        Utils.styleCap(gridOptions, AkylasChartsModule.PROPERTY_CAP, paint1);
                        Utils.styleJoin(gridOptions, AkylasChartsModule.PROPERTY_JOIN, paint1);
                        Utils.styleEmboss(gridOptions, AkylasChartsModule.PROPERTY_EMBOSS, paint1);
                        Utils.styleDash(gridOptions, AkylasChartsModule.PROPERTY_DASH, paint1);
                        Utils.styleShadow(gridOptions, AkylasChartsModule.PROPERTY_SHADOW, paint1);
                        Utils.styleColor(gridOptions, TiC.PROPERTY_COLOR, Color.TRANSPARENT, paint1);
                        Utils.styleStrokeWidth(gridOptions, paint1, context);
                        Utils.styleOpacity(gridOptions, paint1);
                    }
                }

                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_INTERVAL_PX)) {
                    xyPlotView.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS,
                            axisOptions.getDouble(AkylasChartsModule.PROPERTY_INTERVAL_PX));
                }

                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_MAJOR_TICKS)) {
                    KrollDict majorOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_MAJOR_TICKS);

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_INTERVAL)) {
                        double interval = majorOptions.getDouble(AkylasChartsModule.PROPERTY_INTERVAL);
                        xyPlotView.setTicksPerRangeLabel((int) interval);
                        xyPlotView.setRangeStep(XYStepMode.INCREMENT_BY_VAL,
                                interval / ticksPerRange);
                    }

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_LABELS)) {
                        Paint labelPaint = xyPlotView.getGraphWidget()
                                    .getOrCreateRangeLabelPaint();
                        KrollDict labelOptions = majorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_LABELS);
                        Utils.styleTextWidget(labelOptions, labelPaint, context);

                        if (labelOptions.containsKey(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK)) {
                            formatRangeCallback = (KrollFunction) labelOptions
                                    .get(AkylasChartsModule.PROPERTY_FORMAT_CALLBACK);
                        } else {
                            formatRangeCallback = null;
                        }
                        if (formatRangeCallback != null) {
                            xyPlotView.getGraphWidget().setRangeValueFormat(
                                    new Format() {
                                        @Override
                                        public StringBuffer format(
                                                Object object,
                                                StringBuffer buffer,
                                                FieldPosition field) {
                                            Object result = formatRangeCallback
                                                    .call(krollObject,
                                                            new Object[] { (Number) object });
                                            buffer.append(TiConvert
                                                    .toString(result));
                                            return buffer;
                                        }

                                        @Override
                                        public Object parseObject(
                                                String string,
                                                ParsePosition position) {
                                            return null;
                                        }
                                    });
                        } else {
//                          xyPlotView.getGraphWidget().setRangeValueFormat(
//                                  new DecimalFormat("0.0"));
                            if (labelOptions.containsKey(AkylasChartsModule.PROPERTY_LOCATIONS)) {
                                Object[] locations = (Object[]) labelOptions
                                        .get(AkylasChartsModule.PROPERTY_LOCATIONS);
                                if (locations != null) {
                                    for (int i = 0; i < locations.length; i++) {
                                        HashMap location = (HashMap) locations[i];
                                        String text = (String) location
                                                .get(TiC.PROPERTY_TEXT);
                                        Number value = (Number) location
                                                .get(TiC.PROPERTY_VALUE);
                                        if (value != null && text != null) {
                                            xyPlotView.getGraphWidget()
                                                    .addRangeValueFormat(value,
                                                            text);
                                        }
                                    }
                                }
                            }
                            Utils.styleValueFormat(labelOptions, xyPlotView.getGraphWidget(),
                                    "setRangeValueFormat");
                        }

                        if (labelOptions.containsKey(TiC.PROPERTY_OFFSET)) {
                            xyPlotView.getGraphWidget()
                                    .setRangeLabelHorizontalOffset(
                                            Utils.getRawSize(labelOptions,
                                                    TiC.PROPERTY_OFFSET, context));
                        }
                    }

                    if (majorOptions.containsKey(AkylasChartsModule.PROPERTY_GRID_LINES)) {
                        KrollDict gridOptions = majorOptions
                                .getKrollDict(AkylasChartsModule.PROPERTY_GRID_LINES);
                        Paint paint1 = xyPlotView.getGraphWidget()
                                .getOrCreateRangeGridLinePaint();
                        Utils.styleCap(gridOptions, AkylasChartsModule.PROPERTY_CAP, paint1);
                        Utils.styleJoin(gridOptions, AkylasChartsModule.PROPERTY_JOIN, paint1);
                        Utils.styleEmboss(gridOptions, AkylasChartsModule.PROPERTY_EMBOSS, paint1);
                        Utils.styleDash(gridOptions, AkylasChartsModule.PROPERTY_DASH, paint1);
                        Utils.styleShadow(gridOptions, AkylasChartsModule.PROPERTY_SHADOW, paint1);
                        Utils.styleColor(gridOptions, TiC.PROPERTY_COLOR, Color.TRANSPARENT, paint1);
                        Utils.styleStrokeWidth(gridOptions, paint1, context);
                        Utils.styleOpacity(gridOptions, paint1);
                    }
                }
                if (axisOptions.containsKey(AkylasChartsModule.PROPERTY_VISIBLE_RANGE)) {
                    KrollDict visibleRangeOptions = axisOptions
                            .getKrollDict(AkylasChartsModule.PROPERTY_VISIBLE_RANGE);
                    if (visibleRangeOptions.containsKey(TiC.PROPERTY_MIN)) {
                        xyPlotView.setRangeBottomMin(visibleRangeOptions
                                .getFloat(TiC.PROPERTY_MIN));
                    }
                    if (visibleRangeOptions.containsKey(TiC.PROPERTY_MAX)) {
                        xyPlotView.setRangeTopMax(visibleRangeOptions
                                .getFloat(TiC.PROPERTY_MAX));
                    }
                }

                xyPlotView.setTicksPerRangeLabel(ticksPerRange);
                break;
	        }
            default:
	            super.propertySet(key, newValue, oldValue, changedProperty);
	            break;
	        }
	    }

		@Override
		public void release() {
			super.release();
			xyPlotView = null;
		}

		// Definition of the touch states
		static final private int NONE = 0;
		static final private int ONE_FINGER_DRAG = 1;
		static final private int TWO_FINGERS_DRAG = 2;
		private int mode = NONE;

		private Number minXSeriesValue;
		private Number maxXSeriesValue;
		private Number minYSeriesValue;
		private Number maxYSeriesValue;

		private PointF firstFinger;
		private float lastScrolling;
		private float distBetweenFingers;

		private Number newMinX;
		private Number newMaxX;

		public boolean onTouch(View view, MotionEvent motionEvent) {

			switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: // start gesture
				firstFinger = new PointF(motionEvent.getX(), motionEvent.getY());
				mode = ONE_FINGER_DRAG;
				break;

			case MotionEvent.ACTION_POINTER_DOWN: // second finger
			{
				distBetweenFingers = distance(motionEvent);
				// the distance check is done to avoid false alarms
				if (distBetweenFingers > 5f || distBetweenFingers < -5f)
					mode = TWO_FINGERS_DRAG;
				break;
			}

			case MotionEvent.ACTION_POINTER_UP: // end zoom
				// should I count pointers and change mode after only one is
				// left?

				mode = NONE;

				break;

			case MotionEvent.ACTION_MOVE:
				if (mode == ONE_FINGER_DRAG && panEnabled) {

					final PointF oldFirstFinger = firstFinger;
					firstFinger = new PointF(motionEvent.getX(),
							motionEvent.getY());
					lastScrolling = oldFirstFinger.x - firstFinger.x;
					if (scroll(lastScrolling)) {
						xyPlotView.setDomainBoundaries(newMinX, newMaxX,
								BoundaryMode.FIXED);
						xyPlotView.redraw();
					}

				} else if (mode == TWO_FINGERS_DRAG && zoomEnabled) {

					final float oldDist = distBetweenFingers;
					final float newDist = distance(motionEvent);
					if (oldDist > 0 && newDist < 0 || oldDist < 0
							&& newDist > 0) // sign change! Fingers have crossed
											// ;-)
						break;

					distBetweenFingers = newDist;

					if (zoom(oldDist / distBetweenFingers)) {
						xyPlotView.setDomainBoundaries(newMinX, newMaxX,
								BoundaryMode.FIXED);
						xyPlotView.redraw();
					}
				}
				break;
			}

			return true;
		}

		private boolean scroll(float pan) {
			xyPlotView.calculateMinMaxVals();
			if (minXSeriesValue == null || maxXSeriesValue == null) return false;
			float calculatedMinX = xyPlotView.getCalculatedMinX().floatValue();
			float calculatedMaxX = xyPlotView.getCalculatedMaxX().floatValue();
			final float domainSpan = calculatedMaxX - calculatedMinX;
			final float step = domainSpan / xyPlotView.getWidth();
			final float offset = pan * step;

			newMinX = calculatedMinX + offset;
			newMaxX = calculatedMaxX + offset;
			if (clampInteraction) {
				if ((newMinX.floatValue() < minXSeriesValue.floatValue() && newMaxX
						.floatValue() <= maxXSeriesValue.floatValue())) {
					newMinX = minXSeriesValue.floatValue();
					newMaxX = newMinX.floatValue() + domainSpan;
					return true;
				} else if (newMinX.floatValue() >= minXSeriesValue.floatValue()
						&& newMaxX.floatValue() > maxXSeriesValue.floatValue()) {
					newMaxX = maxXSeriesValue.floatValue();
					newMinX = newMaxX.floatValue() - domainSpan;
					return true;
				}
			}
			return true;
		}

		private float distance(MotionEvent event) {
			final float x = event.getX(0) - event.getX(1);
			return x;
		}

		private boolean clamp(float testNewMnX, float testNewMaxX) {
			if (clampInteraction) {
				if ((testNewMnX < minXSeriesValue.floatValue() && testNewMaxX <= maxXSeriesValue
						.floatValue())
						|| (testNewMnX >= minXSeriesValue.floatValue() && testNewMaxX > maxXSeriesValue
								.floatValue()))
					return true;
			}
			return false;
		}

		private boolean zoom(float scale) {
			if (Float.isInfinite(scale) || Float.isNaN(scale)
					|| (scale > -0.001 && scale < 0.001)) // sanity check
				return false;

			xyPlotView.calculateMinMaxVals();
			float calculatedMinX = xyPlotView.getCalculatedMinX().floatValue();
			float calculatedMaxX = xyPlotView.getCalculatedMaxX().floatValue();
			final float domainSpan = calculatedMaxX - calculatedMinX;
			final float domainMidPoint = calculatedMaxX - domainSpan / 2.0f;
			final float offset = domainSpan * scale / 2.0f;
			final float testNewMnX = domainMidPoint - offset;
			final float testNewMaxX = domainMidPoint + offset;
			if (clamp(testNewMnX, testNewMaxX))
				return false;
			newMinX = testNewMnX;
			newMaxX = testNewMaxX;
			return true;
		}

		public void addSerie(XYSerieProxy proxy) {
			addSerie(proxy, true);
		}

		public void addSerie(XYSerieProxy proxy, boolean updateMinMax) {
			proxy.setContext(plotView.getContext());
			proxy.setPlot(LineChartProxy.this);
			xyPlotView.addSeries(proxy.getSeries(), proxy.getFormatter());
			if (proxy instanceof PlotBarProxy && barRenderInitiated == false) {
				initBarRenderer();
			}
			if (updateMinMax)
				updateMinMax();
		}

		public void removeSerie(XYSerieProxy proxy) {
			proxy.setPlot(null);
			xyPlotView.removeSeries(proxy.getSeries());
			updateMinMax();
		}
	}

	// Constructor
	public LineChartProxy() {
		super();
		mPlots = new ArrayList<XYSerieProxy>();
		mMarkers = new ArrayList<MarkerProxy>();
	}
	
	protected void clearPlots()
    {
        if (view != null) {
            for (int i = 0; i < mPlots.size(); i++) {
                ((LineChartView) view).removeSerie(mPlots.get(i));
            }
        }
        mPlots.clear();
    }

	public void addSerie(XYSerieProxy proxy) {
		if (!mPlots.contains(proxy)) {
			mPlots.add(proxy);
			if (view != null) {
				((LineChartView) view).addSerie(proxy);
			}
			refresh();
		}
	}

	public void removeSerie(XYSerieProxy proxy) {
		if (!mPlots.contains(proxy))
			return;

		mPlots.remove(proxy);
		if (view != null) {
			((LineChartView) view).removeSerie(proxy);
			refresh();
		}
	}

	@Override
	public TiUIView createView(Activity activity) {
		TiUIView view = new LineChartView(this, (TiBaseActivity) activity);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(HashMap options) {
		super.handleCreationDict(options);

		userInteractionEnabled = TiConvert.toBoolean(options, "userInteraction",
				userInteractionEnabled);
		if (userInteractionEnabled) {
			panEnabled = zoomEnabled = true;
		}
		panEnabled = TiConvert.toBoolean(options, "panEnabled", panEnabled);
		zoomEnabled = TiConvert.toBoolean(options, "zoomEnabled", zoomEnabled);
		clampInteraction = TiConvert.toBoolean(options, "clampInteraction",
				clampInteraction);
	}

	@Kroll.method
	public void addDataLast(Object args) {
		Object[] datas = (Object[]) args;
		if (datas == null || datas.length != mPlots.size())
			return;
		for (int i = 0; i < mPlots.size(); i++) {
			Object data = datas[i];
			if (data.getClass().isArray()) {
				Object[] array = (Object[]) data;
				mPlots.get(i).addLast(array[0], array[1]);
			} else {
				mPlots.get(i).addLast(null, data);
			}
		}
	}

	@Kroll.method
	public void addDataFirst(Object args) {
		Object[] datas = (Object[]) args;
		if (datas == null || datas.length != mPlots.size())
			return;
		for (int i = 0; i < mPlots.size(); i++) {
			Object data = datas[i];
			if (data.getClass().isArray()) {
				Object[] array = (Object[]) data;
				mPlots.get(i).addLast(array[0], array[1]);
			} else {
				mPlots.get(i).addFirst(null, data);
			}
		}
		if (view != null) {
			((LineChartView) view).updateMinMax();
		}
	}

	@Kroll.method
	public void removeDataFirst() {
		for (int i = 0; i < mPlots.size(); i++) {
			mPlots.get(i).removeFirst();
		}
		if (view != null) {
			((LineChartView) view).updateMinMax();
		}
	}

	@Kroll.method
	public void removeDataLast() {
		for (int i = 0; i < mPlots.size(); i++) {
			mPlots.get(i).removeLast();
		}
		if (view != null) {
			((LineChartView) view).updateMinMax();
		}
	}

	
	@Kroll.method
    public MarkerProxy addMarker(Object object) {
	    MarkerProxy marker = null;
	    if(object instanceof HashMap) {
	        Log.d(TAG, "addMarker " + object.toString());
	        marker =  (MarkerProxy) KrollProxy.createProxy(MarkerProxy.class, null, new Object[]{object}, null);
        } else if(object instanceof MarkerProxy) {
            marker = (MarkerProxy)object;
        }
        if (marker != null) {
            mMarkers.add(marker);
            marker.setLineChartProxy(this);
            
            if (xyPlotView != null) {
                ValueMarker vMarker = marker.getMarker();
                if (vMarker instanceof YValueMarker)
                    xyPlotView.addMarker((YValueMarker) vMarker);
                else
                    xyPlotView.addMarker((XValueMarker) vMarker);
                xyPlotView.redraw();
            }
    	}
        return marker;
    }

	@Kroll.method
	public void removeMarker(MarkerProxy marker) {
		if (mMarkers.contains(marker)) {
	        marker.setLineChartProxy(null);
			ValueMarker vMarker = marker.getMarker();
			mMarkers.remove(marker);
			if (xyPlotView != null) {
				if (vMarker instanceof YValueMarker)
					xyPlotView.removeMarker((YValueMarker) vMarker);
				else
					xyPlotView.removeMarker((XValueMarker) vMarker);
			}
		}
	}
	
	@Kroll.method
	public void update() {
		if (view != null) {
			((LineChartView) view).updateMinMax();
		}
		if (plotView != null) {
			plotView.redraw();
		}
	}
	
	@Override
	protected void handleChildAdded(KrollProxy child, int index) {
	    if (!(child instanceof XYSerieProxy)) {
            super.handleChildAdded(child, index);
            return;
        }
        XYSerieProxy proxy = (XYSerieProxy) child;
        addSerie(proxy);
    }
    
    @Override
    protected void handleChildRemoved(KrollProxy child, final boolean shouldDetach) {
        if (!(child instanceof XYSerieProxy)) {
            super.handleChildRemoved(child, shouldDetach);
            return;
        }
        XYSerieProxy proxy = (XYSerieProxy) child;
        removeSerie(proxy);
    }
    
    public final Rect getGraphRect() {
        Rect rect = new Rect();
        if (xyPlotView != null) {
            xyPlotView.getGraphWidget().getWidgetDimensions().paddedRect.round(rect);
        }
        return rect;
    }
}