package akylas.charts;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiUIView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import ti.modules.titanium.ui.widget.TiUIImageView;

import com.androidplot.Plot;
import com.androidplot.Plot.RenderMode;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.XYPlot;

// This proxy can be created by calling Android.createExample({message: "hello world"})
@SuppressLint("NewApi")
@SuppressWarnings({"unchecked", "rawtypes"})

@Kroll.proxy
public class ChartProxy extends TiViewProxy {
	// Standard Debugging variables
	private static final String TAG = "ChartProxy";
	protected Plot plotView;
	protected String mTitle;
	

	protected class ChartView extends TiUIView {
		protected LinearLayout layout;
		
		protected void onLayoutChanged() {
			Rect rect = new Rect();
			plotView.getDisplayDimensions().canvasRect.round(rect);
			updateGradients(layout.getContext(), rect);
		}
		
		protected Class viewClass() {
			return Plot.class;
		}
		
		protected void postCreateNativeView() {
			
		}
		
		protected void createNativeView(Activity activity) {
			layout = new LinearLayout(activity) {
				@Override
				protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
					super.onLayout(changed, left, top, right, bottom);
					if (changed) {
	                    onLayoutChanged();
	                    TiUIHelper.firePostLayoutEvent(ChartView.this);
	                }
				}
			};
			try {
				plotView = (Plot) viewClass().getConstructor(Context.class, String.class, RenderMode.class).newInstance(proxy.getActivity(), mTitle, RenderMode.USE_MAIN_THREAD);
			} catch (Exception e) {
				plotView = null;
				return;
			}
//			plotView.setShouldSupportHwAcceleration(false);
			if (TiC.HONEYCOMB_OR_GREATER) {
				plotView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
			float defaultSize = TiUIHelper.getRawSize(null, plotView.getContext());
			plotView.getTitleWidget().getLabelPaint().setTextSize(defaultSize);
			plotView.getTitleWidget().pack();
			
			plotView.getBorderPaint().setAntiAlias(true);
			
			plotView.getBorderPaint().setColor(Color.TRANSPARENT);
			plotView.getBackgroundPaint().setColor(Color.TRANSPARENT);
	
			plotView.setPlotMargins(0, 0, 0, 0);
			plotView.setPlotPadding(0, 0, 0, 0);
			plotView.setPadding(0, 0, 0, 0);

			// reposition the grid so that it rests above the bottom-left
			// edge of the graph widget:
			plotView.getTitleWidget().position(0, XLayoutStyle.RELATIVE_TO_CENTER, 0,
					YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.TOP_MIDDLE);

			beforeLayoutNativeView();
				
			LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
					1.0f);
			layout.addView(plotView, params);
//			plotView.setMarkupEnabled(true);
			
			setNativeView(layout);
		}
		
		protected void beforeLayoutNativeView() {
			
		}



		public ChartView(final TiViewProxy proxy, Activity activity) {
			super(proxy);

			createNativeView(activity);
			
			postCreateNativeView();

			plotView.redraw();
		}

		protected void updateGradients(Context context, Rect rect) {
			KrollDict d = proxy.getProperties();
			if (d.containsKey(AkylasChartsModule.PROPERTY_PLOT_AREA)) {
				KrollDict plotOptions = d.getKrollDict(AkylasChartsModule.PROPERTY_PLOT_AREA);
				if (plotOptions.containsKey("backgroundGradient")) {
					KrollDict bgOptions = plotOptions.getKrollDict("backgroundGradient");
					plotView.getBackgroundPaint().setShader(Utils.styleGradient(bgOptions, context, rect));
				}
			}
		}

		@Override
	    protected void handleProperties(HashMap d, final boolean changed) {
		    super.handleProperties(d, changed);
			if (d.containsKey(AkylasChartsModule.PROPERTY_FILL_COLOR)) {
				// a trick to have fillColor work as a background (for
				// compatibiity with the ios part)
				d.put(TiC.PROPERTY_BACKGROUND_COLOR, d.get(AkylasChartsModule.PROPERTY_FILL_COLOR));
			}
			if (d.containsKey(AkylasChartsModule.PROPERTY_FILL_GRADIENT)) {
				// a trick to have fillGradient work as a background (for
				// compatibiity with the ios part)
				d.put(TiC.PROPERTY_BACKGROUND_GRADIENT, d.get(AkylasChartsModule.PROPERTY_FILL_GRADIENT));
			}
			if (d.containsKey(AkylasChartsModule.PROPERTY_FILL_OPACITY)) {
				// a trick to have fillOpacity work as a background (for
				// compatibiity with the ios part)
				d.put(TiC.PROPERTY_BACKGROUND_OPACITY, d.get(AkylasChartsModule.PROPERTY_FILL_OPACITY));
			}
						
			Context context = plotView.getContext();

			if (d.containsKey(TiC.PROPERTY_TITLE)) {
			    HashMap titleOptions =TiConvert.toHashMap(d.get(TiC.PROPERTY_TITLE));
				
				Utils.styleTextWidget(titleOptions, plotView.getTitleWidget().getLabelPaint(), context);

				float top = 0;
				float left = 0;
				AnchorPosition anchor = AnchorPosition.TOP_MIDDLE;
				XLayoutStyle xlayout = XLayoutStyle.ABSOLUTE_FROM_CENTER;
				YLayoutStyle ylayout = YLayoutStyle.ABSOLUTE_FROM_TOP;
				if (titleOptions.containsKey(TiC.PROPERTY_LOCATION)) {
					anchor = AnchorPosition.values()[TiConvert.toInt(titleOptions, TiC.PROPERTY_LOCATION)];
				}
				if (anchor == AnchorPosition.BOTTOM_MIDDLE) {
					xlayout = XLayoutStyle.ABSOLUTE_FROM_CENTER;
					ylayout = YLayoutStyle.ABSOLUTE_FROM_BOTTOM;
				} else if (anchor == AnchorPosition.LEFT_MIDDLE) {
					xlayout = XLayoutStyle.ABSOLUTE_FROM_LEFT;
					ylayout = YLayoutStyle.ABSOLUTE_FROM_CENTER;
				} else if (anchor == AnchorPosition.RIGHT_MIDDLE) {
					xlayout = XLayoutStyle.ABSOLUTE_FROM_RIGHT;
					ylayout = YLayoutStyle.ABSOLUTE_FROM_CENTER;
				}
				if (titleOptions.containsKey(TiC.PROPERTY_OFFSET)) {
				    HashMap offset = TiConvert.toHashMap(titleOptions.get(TiC.PROPERTY_OFFSET));
					top = Utils.getRawSizeOrZero(offset, TiC.PROPERTY_Y, context);
					left = Utils.getRawSizeOrZero(offset, TiC.PROPERTY_X, context);
				}

				plotView.getTitleWidget().position(left, xlayout, top, ylayout, anchor);
				plotView.getTitleWidget().pack();
			}


			if (d.containsKey(AkylasChartsModule.PROPERTY_PLOT_AREA)) {
			    HashMap plotOptions = TiConvert.toHashMap(d.get(AkylasChartsModule.PROPERTY_PLOT_AREA));
				if (plotOptions.containsKey(TiC.PROPERTY_BORDER_RADIUS)) {
					float radius = Utils.getRawSize(plotOptions, TiC.PROPERTY_BORDER_RADIUS, context);
					plotView.setBorderStyle(XYPlot.BorderStyle.ROUNDED, radius, radius);
				}
				if (plotOptions.containsKey(TiC.PROPERTY_BORDER_COLOR)) {
					plotView.getBorderPaint().setColor(TiConvert.toColor(plotOptions, TiC.PROPERTY_BORDER_COLOR, Color.BLACK));
				}

				Paint paint1 = plotView.getBorderPaint();
				Utils.styleOpacity(plotOptions, AkylasChartsModule.PROPERTY_BORDER_OPACITY, paint1);
				Utils.styleStrokeWidth(plotOptions, TiC.PROPERTY_BORDER_WIDTH, "0", paint1, context);
				paint1 = plotView.getBackgroundPaint();
				Utils.styleOpacity(plotOptions, TiC.PROPERTY_BACKGROUND_OPACITY, paint1);
				Utils.styleColor(plotOptions, TiC.PROPERTY_BACKGROUND_COLOR, Color.TRANSPARENT, paint1);
				
			}
			Utils.styleMargins(d, plotView, "setPlotMargins", context);

		}

		@Override
		public void release() {
			super.release();
			plotView = null;
		}
	}
	

	

	// Constructor
	public ChartProxy() {
		super();
		mTitle = "";
	}

	@Override
	public TiUIView createView(Activity activity) {
		TiUIView view = new ChartView(this, (TiBaseActivity) activity);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}
	

	
	@Override
	public TiUIView getOrCreateView()
	{
		TiUIView view =  super.getOrCreateView(true);
		((ChartView)view).postCreateNativeView();
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(HashMap options) {
		super.handleCreationDict(options);

		if (options.containsKey(TiC.PROPERTY_TITLE)) {
			HashMap titleOptions = TiConvert.toHashMap(options.get(TiC.PROPERTY_TITLE));
			if (titleOptions.containsKey(TiC.PROPERTY_TEXT)) {
				mTitle = TiConvert.toString(titleOptions, TiC.PROPERTY_TEXT);
			}
		}
	}

	@Kroll.method
	public void refresh()
	{
		if (plotView != null) {
			plotView.redraw();
		}
	}
	
	public Plot getPlot() {
	    return plotView;
	}
	
}