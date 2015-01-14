package akylas.charts;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;

// This proxy can be created by calling Android.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class PieChartProxy extends ChartProxy {
	// Standard Debugging variables
	private static final String TAG = "PieChartProxy";
	private final ArrayList<PieSegmentProxy> mSegments;

	private PieChart pieView;

	private class PieChartView extends ChartView {

		@SuppressWarnings("rawtypes")
		protected Class viewClass() {
			return PieChart.class;
		}
		
		protected void onLayoutChanged() {
			Rect rect = new Rect();
			((PieChart) plotView).getPieWidget().getWidgetDimensions().paddedRect.round(rect);
			updateGradients(layout.getContext(), rect);
		}
		
		protected void beforeLayoutNativeView() {
			pieView = (PieChart) plotView;
//			float defaultSize = TiUIHelper.getRawSize(null,
//					plotView.getContext());
			
		}

		protected void postCreateNativeView() {
			super.postCreateNativeView();
			
			for (int i = 0; i < mSegments.size(); i++) {
				PieSegmentProxy lProxy = mSegments.get(i);
				lProxy.setContext(pieView.getContext());
				pieView.addSegment(lProxy.getSegment(), lProxy.getFormatter());
			}

		}

		public PieChartView(final TiViewProxy proxy, Activity activity) {
			super(proxy, activity);
		}

		protected void updateGradients(Context context, Rect rect) {
			super.updateGradients(context, rect);
			for (int i = 0; i < mSegments.size(); i++) {
				PieSegmentProxy lProxy = mSegments.get(i);
				lProxy.updateGradients(context, rect);
			}
		}

		@Override
        protected void handleProperties(KrollDict d, final boolean changed) {
            super.handleProperties(d, changed);

//			Context context = plotView.getContext();
			PieRenderer render = pieView.getRenderer(PieRenderer.class);
						
			if (d.containsKey("donutSize")) {
				float size = Utils.getRawSize(d, "donutSize", plotView.getContext());
				render.setDonutSize(size, PieRenderer.DonutMode.PIXELS);
			}
			if (d.containsKey("startAngle")) {
				render.setStartDeg(-90 + d.getFloat("startAngle"));
			}
			if (d.containsKey("endAngle")) {
				render.setEndDeg(-90 + d.getFloat("endAngle"));
			}
		}

		@Override
		public void release() {
			super.release();
			pieView = null;
		}
	}

	// Constructor
	public PieChartProxy() {
		super();
		mSegments = new ArrayList<PieSegmentProxy>();
	}

	public void addSegment(PieSegmentProxy proxy) {
		if (!mSegments.contains(proxy)) {
			mSegments.add(proxy);
			proxy.setPieChartProxy(this);
		if (pieView != null) {
				proxy.setContext(pieView.getContext());
				pieView.addSegment(proxy.getSegment(), proxy.getFormatter());
				pieView.redraw();
			}
		}
	}

	public void removeSegment(PieSegmentProxy proxy) {
		if (!mSegments.contains(proxy))
			return;
		mSegments.remove(proxy);
		proxy.setPieChartProxy(null);
		if (pieView != null) {
			pieView.removeSegment(proxy.getSegment());
			pieView.redraw();
		}
	}

	@Override
	public TiUIView createView(Activity activity) {
		TiUIView view = new PieChartView(this, (TiBaseActivity) activity);
		view.getLayoutParams().autoFillsHeight = true;
		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// Handle creation options
//	@Override
//	public void handleCreationDict(KrollDict options) {
//		super.handleCreationDict(options);
//	}

	@Kroll.method
	public void add(Object segment) {
		Log.d(TAG, "add", Log.DEBUG_MODE);
		if (!(segment instanceof PieSegmentProxy)) {
			Log.e(TAG, "add: must be a LinePlot");
			return;
		}
		addSegment((PieSegmentProxy)segment);
	}

	@Kroll.method
	public void remove(Object segment) {
		Log.d(TAG, "remove", Log.DEBUG_MODE);
		if (!(segment instanceof PieSegmentProxy)) {
			Log.e(TAG, "remove: must be a LinePlot");
			return;
		}
		removeSegment((PieSegmentProxy)segment);
	}
}