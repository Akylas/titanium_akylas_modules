package akylas.charts;

import java.text.DecimalFormat;
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
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
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
			
			xyPlotView.getGraphWidget().setGridPadding(0,0,0,0);
            xyPlotView.getGraphWidget().setMargins(0,0,0,0);
            xyPlotView.getGraphWidget().setClippingEnabled(false);
            xyPlotView.getGraphWidget().setRangeLabelWidth(0);
            xyPlotView.getGraphWidget().setDomainLabelWidth(0);
            xyPlotView.getGraphWidget().setDomainOriginLabelOffset(0);
            xyPlotView.getGraphWidget().setRangeOriginLabelOffset(0);

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

			if (d.containsKey("gridArea")) {
				KrollDict gridOptions = d.getKrollDict("gridArea");
				if (gridOptions.containsKey("backgroundGradient")) {
					KrollDict bgOptions = gridOptions
							.getKrollDict("backgroundGradient");
					xyPlotView.getGraphWidget().getOrCreateGridBackgroundPaint()
							.setShader(Utils.styleGradient(bgOptions, context, rect));
				}
			}
//			if (d.containsKey("plotArea")) {
//                KrollDict options = d.getKrollDict("plotArea");
//                if (options.containsKey("backgroundGradient")) {
//                    KrollDict bgOptions = options
//                            .getKrollDict("backgroundGradient");
//                    xyPlotView.getGraphWidget().getOrCreateBackgroundPaint()
//                            .setShader(Utils.styleGradient(bgOptions, context, rect));
//                }
//            }
		}

		@Override
		public void processProperties(KrollDict d) {
			super.processProperties(d);

			Context context = plotView.getContext();

			if (d.containsKey("plotSpace")) {
				KrollDict plotOptions = d.getKrollDict("plotSpace");
				boolean scaleToFit = plotOptions
						.optBoolean("scaleToFit", false);
				if (scaleToFit) {
					xyPlotView.setRangeBoundaries(0, 0, BoundaryMode.AUTO);
					xyPlotView.setDomainBoundaries(0, 0, BoundaryMode.AUTO);
				} else {
					needsBoundarySet = true;
					if (plotOptions.containsKey("xRange")) {
						KrollDict xOptions = plotOptions.getKrollDict("xRange");
						if (xOptions.containsKey("min")
								&& xOptions.containsKey("max")) {
							int min = xOptions.getInt("min");
							int max = xOptions.getInt("max");
							xyPlotView.setDomainBoundaries(min, max,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						} else if (xOptions.containsKey("min")) {
							int min = xOptions.getInt("min");
							xyPlotView.setDomainLowerBoundary(min,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						} else if (xOptions.containsKey("max")) {
							int max = xOptions.getInt("max");
							xyPlotView.setDomainUpperBoundary(max,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						}
					}
					if (plotOptions.containsKey("yRange")) {
						KrollDict yOptions = plotOptions.getKrollDict("yRange");
						if (yOptions.containsKey("min")
								&& yOptions.containsKey("max")) {
							int min = yOptions.getInt("min");
							int max = yOptions.getInt("max");
							xyPlotView.setRangeBoundaries(min, max,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						} else if (yOptions.containsKey("min")) {
							int min = yOptions.getInt("min");
							xyPlotView.setRangeLowerBoundary(min,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						} else if (yOptions.containsKey("max")) {
							int max = yOptions.getInt("max");
							xyPlotView.setRangeUpperBoundary(max,
									BoundaryMode.FIXED);
							needsBoundarySet = false;
						}
					}
				}
			} else {
				xyPlotView.setRangeBoundaries(0, 0, BoundaryMode.AUTO);
				xyPlotView.setDomainBoundaries(0, 0, BoundaryMode.AUTO);
			}

			if (d.containsKey("gridArea")) {
				KrollDict gridOptions = d.getKrollDict("gridArea");
				Paint paint1 = xyPlotView.getGraphWidget()
						.getOrCreateGridBackgroundPaint();
				Utils.styleColor(gridOptions, "backgroundColor",
						Color.TRANSPARENT, paint1);
				Utils.styleOpacity(gridOptions, "backgroundOpacity", paint1);
				Utils.styleMargins(gridOptions, xyPlotView.getGraphWidget(),
						"setGridPadding", context);
			}

//			if (d.containsKey("plotArea")) {
//				KrollDict plotOptions = d.getKrollDict("plotArea");
//				Utils.styleMargins(plotOptions, xyPlotView.getGraphWidget(),
//						"setMargins", context);
//                Paint paint1 = xyPlotView.getGraphWidget()
//                        .getOrCreateBackgroundPaint();
//                Utils.styleColor(plotOptions, "backgroundColor",
//                        Color.TRANSPARENT, paint1);
//                Utils.styleOpacity(plotOptions, "backgroundOpacity", paint1);
//			}
//			Utils.styleMargins(d, plotView, "setPlotMargins", context);

			if (d.containsKey("legend")) {
				KrollDict legend = d.getKrollDict("legend");
				if (legend.containsKey("visible"))
					xyPlotView.getLegendWidget().setVisible(
							legend.getBoolean("visible"));
			}
			if (d.containsKey("xAxis")) {
			    HashMap currentOptions = (HashMap) getProperty("xAxis");
				KrollDict axisOptions = d.getKrollDict("xAxis");
				if (currentOptions != null && d.get("xAxis") != currentOptions) {
				    axisOptions = KrollDict.merge(currentOptions, axisOptions);
				}
				
				if (axisOptions.containsKey("origin")) {
					xyPlotView.setUserDomainOrigin((Number) axisOptions
							.get("origin"));
				}
				
				if (axisOptions.containsKey("align")) {
					xyPlotView.getGraphWidget().setRangeAxisAlignment(Utils.gravityFromAlignment(axisOptions
							.getInt("align")));
				}

				Paint paint = xyPlotView.getGraphWidget()
						.getOrCreateRangeOriginLinePaint();
				Utils.styleColor(axisOptions, "lineColor", Color.TRANSPARENT, paint);
				Utils.styleStrokeWidth(axisOptions, "lineWidth", paint, context);

				if (axisOptions.containsKey("title")) {
					KrollDict titleOptions = axisOptions.getKrollDict("title");
					if (titleOptions.containsKey("text")) {
						xyPlotView.setDomainLabel(titleOptions
								.getString("text"));
					}

					Utils.styleTextWidget(titleOptions, xyPlotView
							.getGraphWidget().getOrCreateDomainOriginLabelPaint(), context);
					if (titleOptions.containsKey("offset")) {
						xyPlotView.getGraphWidget().setDomainOriginLabelOffset(Utils.getRawSizeOrZero(titleOptions, "offset", context));
					}
					if (titleOptions.containsKey("angle")) {
						xyPlotView.getGraphWidget().setDomainOriginLabelAngle(Utils.getRawSizeOrZero(titleOptions, "angle", context));
					}
				}

				int ticksPerDomain = 1;
				if (axisOptions.containsKey("minorTicks")) {
					KrollDict minorOptions = axisOptions
							.getKrollDict("minorTicks");
					if (minorOptions.containsKey("count")) {
						ticksPerDomain = minorOptions.getInt("count");
					}
					if (minorOptions.containsKey("gridLines")) {
						KrollDict gridOptions = minorOptions
								.getKrollDict("gridLines");
						Paint paint1 = xyPlotView.getGraphWidget()
								.getOrCreateDomainSubGridLinePaint();
						
						Utils.styleCap(gridOptions, "cap", paint1);
						Utils.styleJoin(gridOptions, "join", paint1);
						Utils.styleEmboss(gridOptions, "emboss", paint1);
						Utils.styleDash(gridOptions, "dash", paint1, context);
						Utils.styleShadow(gridOptions, "shadow", paint1, context);
						Utils.styleColor(gridOptions, "color", Color.TRANSPARENT, paint1);
						Utils.styleStrokeWidth(gridOptions, paint1, context);
						Utils.styleOpacity(gridOptions, paint1);
					}
				}
				// xyPlotView.setDomainStep(XYStepMode.SUBDIVIDE, 9);
				// xyPlotView.setRangeStep(XYStepMode.SUBDIVIDE, 9);

				if (axisOptions.containsKey("interval_px")) {
					double interval = axisOptions.getDouble("interval_px");
					xyPlotView.setTicksPerDomainLabel((int) interval);
					xyPlotView.setDomainStep(XYStepMode.INCREMENT_BY_PIXELS,
							interval / ticksPerDomain);
				}

				if (axisOptions.containsKey("majorTicks")) {
					KrollDict majorOptions = axisOptions
							.getKrollDict("majorTicks");

					if (majorOptions.containsKey("interval")) {
						double interval = majorOptions.getDouble("interval");
						xyPlotView.setTicksPerDomainLabel((int) interval);
						xyPlotView.setDomainStep(XYStepMode.INCREMENT_BY_VAL,
								interval / ticksPerDomain);
					}

					if (majorOptions.containsKey("labels")) {
					    Paint labelPaint = xyPlotView.getGraphWidget()
                                .getOrCreateDomainLabelPaint();
						KrollDict labelOptions = majorOptions
								.getKrollDict("labels");
						Utils.styleTextWidget(labelOptions, labelPaint, context);

						if (labelOptions.containsKey("formatCallback")) {
							formatDomainCallback = (KrollFunction) labelOptions
									.get("formatCallback");
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
//							xyPlotView.getGraphWidget().setDomainValueFormat(
//									new DecimalFormat("0.0"));
							if (labelOptions.containsKey("locations")) {
								Object[] locations = (Object[]) labelOptions
										.get("locations");
								if (locations != null) {
									for (int i = 0; i < locations.length; i++) {
										HashMap location = (HashMap) locations[i];
										String text = (String) location
												.get("text");
										Number value = (Number) location
												.get("value");
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
						if (labelOptions.containsKey("offset")) {
							xyPlotView.getGraphWidget()
									.setDomainLabelVerticalOffset(
											Utils.getRawSize(labelOptions,
													"offset", context));
						}
					}

					if (majorOptions.containsKey("gridLines")) {
						KrollDict gridOptions = majorOptions
								.getKrollDict("gridLines");
						Paint paint1 = xyPlotView.getGraphWidget()
								.getOrCreateDomainGridLinePaint();
						Utils.styleCap(gridOptions, "cap", paint1);
						Utils.styleJoin(gridOptions, "join", paint1);
						Utils.styleEmboss(gridOptions, "emboss", paint1);
						Utils.styleDash(gridOptions, "dash", paint1, context);
						Utils.styleShadow(gridOptions, "shadow", paint1, context);
						Utils.styleColor(gridOptions, "color", Color.TRANSPARENT, paint1);
						Utils.styleStrokeWidth(gridOptions, paint1, context);
						Utils.styleOpacity(gridOptions, paint1);
					}
				}
				if (axisOptions.containsKey("visibleRange")) {
					KrollDict visibleRangeOptions = axisOptions
							.getKrollDict("visibleRange");
					if (visibleRangeOptions.containsKey("min")) {
						xyPlotView.setDomainLeftMin(visibleRangeOptions
								.getFloat("min"));
					}
					if (visibleRangeOptions.containsKey("max")) {
						xyPlotView.setDomainRightMax(visibleRangeOptions
								.getFloat("max"));
					}
				}

				xyPlotView.setTicksPerDomainLabel(ticksPerDomain);
			}
			if (d.containsKey("yAxis")) {
			    HashMap currentOptions = (HashMap) getProperty("yAxis");
                KrollDict axisOptions = d.getKrollDict("yAxis");
                if (currentOptions != null && d.get("yAxis") != currentOptions) {
                    axisOptions = KrollDict.merge(currentOptions, axisOptions);
                }
				if (axisOptions.containsKey("origin")) {
					xyPlotView.setUserRangeOrigin((Number) axisOptions
							.get("origin"));
				}
				
				if (axisOptions.containsKey("align")) {
					xyPlotView.getGraphWidget().setDomainAxisAlignment(Utils.gravityFromAlignment(axisOptions
															.getInt("align")));
				}
				Paint paint = xyPlotView.getGraphWidget()
						.getOrCreateDomainOriginLinePaint();
				Utils.styleColor(axisOptions, "lineColor", Color.TRANSPARENT, paint);
				Utils.styleStrokeWidth(axisOptions, "lineWidth", paint, context);

				if (axisOptions.containsKey("title")) {
					KrollDict titleOptions = axisOptions.getKrollDict("title");
					if (titleOptions.containsKey("text")) {
						xyPlotView.setRangeLabel(titleOptions
								.getString("text"));
					}

					Utils.styleTextWidget(titleOptions, xyPlotView
							.getGraphWidget().getOrCreateRangeOriginLabelPaint(), context);
					if (titleOptions.containsKey("offset")) {
						xyPlotView.getGraphWidget().setRangeOriginLabelOffset(Utils.getRawSizeOrZero(titleOptions, "offset", context));
					}
					if (titleOptions.containsKey("angle")) {
						xyPlotView.getGraphWidget().setRangeOriginLabelAngle(Utils.getRawSizeOrZero(titleOptions, "angle", context));
					}
				}

				int ticksPerRange = 1;
				if (axisOptions.containsKey("minorTicks")) {
					KrollDict minorOptions = axisOptions
							.getKrollDict("minorTicks");
					if (minorOptions.containsKey("count")) {
						ticksPerRange = minorOptions.getInt("count");
					}
					if (minorOptions.containsKey("gridLines")) {
						KrollDict gridOptions = minorOptions
								.getKrollDict("gridLines");
						Paint paint1 = xyPlotView.getGraphWidget()
								.getOrCreateRangeSubGridLinePaint();
						Utils.styleCap(gridOptions, "cap", paint1);
						Utils.styleJoin(gridOptions, "join", paint1);
						Utils.styleEmboss(gridOptions, "emboss", paint1);
						Utils.styleDash(gridOptions, "dash", paint1, context);
						Utils.styleShadow(gridOptions, "shadow", paint1, context);
						Utils.styleColor(gridOptions, "color", Color.TRANSPARENT, paint1);
						Utils.styleStrokeWidth(gridOptions, paint1, context);
						Utils.styleOpacity(gridOptions, paint1);
					}
				}

				if (axisOptions.containsKey("interval_px")) {
					xyPlotView.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS,
							axisOptions.getDouble("interval_px"));
				}

				if (axisOptions.containsKey("majorTicks")) {
					KrollDict majorOptions = axisOptions
							.getKrollDict("majorTicks");

					if (majorOptions.containsKey("interval")) {
						double interval = majorOptions.getDouble("interval");
						xyPlotView.setTicksPerRangeLabel((int) interval);
						xyPlotView.setRangeStep(XYStepMode.INCREMENT_BY_VAL,
								interval / ticksPerRange);
					}

					if (majorOptions.containsKey("labels")) {
						Paint labelPaint = xyPlotView.getGraphWidget()
	                                .getOrCreateRangeLabelPaint();
                        KrollDict labelOptions = majorOptions
                                .getKrollDict("labels");
                        Utils.styleTextWidget(labelOptions, labelPaint, context);

						if (labelOptions.containsKey("formatCallback")) {
							formatRangeCallback = (KrollFunction) labelOptions
									.get("formatCallback");
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
//							xyPlotView.getGraphWidget().setRangeValueFormat(
//									new DecimalFormat("0.0"));
							if (labelOptions.containsKey("locations")) {
								Object[] locations = (Object[]) labelOptions
										.get("locations");
								if (locations != null) {
									for (int i = 0; i < locations.length; i++) {
										HashMap location = (HashMap) locations[i];
										String text = (String) location
												.get("text");
										Number value = (Number) location
												.get("value");
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

						if (labelOptions.containsKey("offset")) {
							xyPlotView.getGraphWidget()
									.setRangeLabelHorizontalOffset(
											Utils.getRawSize(labelOptions,
													"offset", context));
						}
					}

					if (majorOptions.containsKey("gridLines")) {
						KrollDict gridOptions = majorOptions
								.getKrollDict("gridLines");
						Paint paint1 = xyPlotView.getGraphWidget()
								.getOrCreateRangeGridLinePaint();
						Utils.styleCap(gridOptions, "cap", paint1);
						Utils.styleJoin(gridOptions, "join", paint1);
						Utils.styleEmboss(gridOptions, "emboss", paint1);
						Utils.styleDash(gridOptions, "dash", paint1, context);
						Utils.styleShadow(gridOptions, "shadow", paint1, context);
						Utils.styleColor(gridOptions, "color", Color.TRANSPARENT, paint1);
						Utils.styleStrokeWidth(gridOptions, paint1, context);
						Utils.styleOpacity(gridOptions, paint1);
					}
				}
				if (axisOptions.containsKey("visibleRange")) {
					KrollDict visibleRangeOptions = axisOptions
							.getKrollDict("visibleRange");
					if (visibleRangeOptions.containsKey("min")) {
						xyPlotView.setRangeBottomMin(visibleRangeOptions
								.getFloat("min"));
					}
					if (visibleRangeOptions.containsKey("max")) {
						xyPlotView.setRangeTopMax(visibleRangeOptions
								.getFloat("max"));
					}
				}

				xyPlotView.setTicksPerRangeLabel(ticksPerRange);
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
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		userInteractionEnabled = options.optBoolean("userInteraction",
				userInteractionEnabled);
		if (userInteractionEnabled) {
			panEnabled = zoomEnabled = true;
		}
		panEnabled = options.optBoolean("panEnabled", panEnabled);
		zoomEnabled = options.optBoolean("zoomEnabled", zoomEnabled);
		clampInteraction = options.optBoolean("clampInteraction",
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
    protected void handleChildRemoved(KrollProxy child) {
        if (!(child instanceof XYSerieProxy)) {
            super.handleChildRemoved(child);
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