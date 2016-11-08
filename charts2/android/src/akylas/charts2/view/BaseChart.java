package akylas.charts2.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import akylas.charts2.Charts2Module;
import akylas.charts2.data.DataProxy;
import akylas.charts2.datasets.DataSetProxy;
import akylas.charts2.proxy.ChartBaseViewProxy;

public class BaseChart extends TiUIView implements OnChartGestureListener, OnChartValueSelectedListener {
    public BaseChart(TiViewProxy proxy) {
        super(proxy, new TiCompositeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setNativeView(newChartView(proxy.getActivity()));
        getChart().setUnbindEnabled(true);
        getChart().setOnChartGestureListener(this);
        getChart().setOnChartValueSelectedListener(this);
    }

    protected View newChartView(Activity activity) {
        return null;
    }
    public Chart getChart() {
        return (Chart)nativeView;
    }
    
    public Legend getLegend() {
        return getChart().getLegend();
    }
    
    public XAxis getXAxis() {
        return getChart().getXAxis();
    }    
    
    public ChartData getData() {
        return getChart().getData();
    }
    
    public void setData(ChartData data) {
        getChart().setData(data);
    }
    
    @Override
    public void release() {
    }
    
    protected void prepareForReuse() {
        getChart().highlightValue(null);
    }
    
    public void notifyDataSetChanged() {
        getChart().postInvalidate();
    }
    
    public void redraw() {
        getChart().postInvalidate();
    }
    
    
    @Override
    public void setReusing(boolean value) {
        super.setReusing(value);
        if (value) {   
            prepareForReuse();
        }
    }

    protected static final ArrayList<String> KEY_SEQUENCE;

    static {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add("xAxis");
        tmp.add("legend");
        KEY_SEQUENCE = tmp;
    }

    @Override
    protected ArrayList<String> keySequence() {
        return KEY_SEQUENCE;
    }
    
    @Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
//        case TiC.PROPERTY_TOUCH_ENABLED:
//            getChart().setTouchEnabled(TiConvert.toBoolean(newValue));
//            break;
        case "drawMarkers":
            getChart().setDrawMarkers(TiConvert.toBoolean(newValue));
            break;
        case "highlightPerTap":
            getChart().setHighlightPerTapEnabled(TiConvert.toBoolean(newValue));
            break;
        case "dragDeceleration":
            getChart().setDragDecelerationEnabled(TiConvert.toBoolean(newValue));
            break;
        case "descriptionFont":
        {
            final Context context = proxy.getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
            getChart().setDescriptionTypeface(desc.typeface);
            getChart().setDescriptionTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        }
        case "descriptionColor":
            getChart().setDescriptionColor(TiConvert.toColor(newValue));
            break;
        case "descriptionTextAlign":
            getChart().setDescriptionTextAlign(Charts2Module.toTextAlign(newValue));
            break;
        case "descriptionPosition":
        {
            PointF pos = TiConvert.toPointF(newValue);
            getChart().setDescriptionPosition(pos.x, pos.y);
            break;
        }
        case "description":
            getChart().setDescription(TiConvert.toString(newValue));
            break;
        case "noDataFont":
        {
            final Context context = proxy.getActivity();
            FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(newValue));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
            getChart().setNoDataTextTypeface(desc.typeface);
            getChart().setNoDataTextSize(Utils.convertPixelsToDp(fontSize));
            break;
        }
        case "noDataColor":
            getChart().setNoDataTextColor(TiConvert.toColor(newValue));
            break;
        case "noData":
            getChart().setNoDataText(TiConvert.toString(newValue));
            break;
        case "noDataDescription":
            getChart().setNoDataTextDescription(TiConvert.toString(newValue));
            break;
        case "maxHighlightDistance":
            getChart().setMaxHighlightDistance(TiConvert.toFloat(newValue));
            break;
        case "extraOffset":
            RectF offset = TiConvert.toPaddingRect(newValue, null);
            getChart().setExtraOffsets(offset.left, offset.top, offset.right, offset.bottom);
            break;
        case "data":
            if (newValue instanceof HashMap) {
                ((ChartBaseViewProxy) proxy).setData((HashMap) newValue);
            }
            break;
        case "legend":
            if (newValue instanceof HashMap) {
                ((ChartBaseViewProxy) proxy).setLegend((HashMap) newValue);
            }
            break;
        case "xAxis":
            if (newValue instanceof HashMap) {
                ((ChartBaseViewProxy) proxy).setXAxis((HashMap) newValue);
            }
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
    
    
    
    private DataProxy getDataProxy() {
        return ((ChartBaseViewProxy) proxy).getData();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        boolean hasHighlight = proxy.hasListeners("highlight", false);
        boolean hasClick = proxy.hasListeners("click");
        if (hasHighlight || hasClick)
        {
            final int dataSetIndex = h.getDataSetIndex();
            KrollDict result = new KrollDict();
            DataSetProxy dataSetProxy = getDataProxy().getDataSet(dataSetIndex);
            if (dataSetProxy != null) {
                result.put("data", dataSetProxy.chartDataEntryDict(e));
            }
            result.put("dataSetIndex", dataSetIndex);
            if (hasHighlight) {
                proxy.fireEvent("highlight", result, false, false);
            }
            if (hasClick) {
                proxy.fireEvent("click", result, true, false);
            }
        }
    }

    @Override
    public void onNothingSelected() {
        proxy.fireEvent("click", null, true, true);
    }

    @Override
    public void onChartGestureStart(MotionEvent me,
            ChartGesture lastPerformedGesture) {        
    }

    @Override
    public void onChartGestureEnd(MotionEvent me,
            ChartGesture lastPerformedGesture) {
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {  
        if (proxy.hasListeners(TiC.EVENT_LONGPRESS, false)) {
            fireEvent(TiC.EVENT_LONGPRESS, dictFromMotionEvent(me), false, false);
        }
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        if (proxy.hasListeners(TiC.EVENT_DOUBLE_TAP, false)) {
            fireEvent(TiC.EVENT_DOUBLE_TAP, dictFromMotionEvent(me), false, false);
        }
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        if (proxy.hasListeners(TiC.EVENT_SINGLE_TAP, false)) {
            fireEvent(TiC.EVENT_SINGLE_TAP, dictFromMotionEvent(me), false, false);
        }
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
            float velocityY) {
        if (proxy.hasListeners(TiC.EVENT_SWIPE, false)) {
            KrollDict data = dictFromMotionEvent(me2);
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                data.put(TiC.EVENT_PROPERTY_DIRECTION,
                        velocityX > 0 ? "right" : "left");
            } else {
                data.put(TiC.EVENT_PROPERTY_DIRECTION,
                        velocityY > 0 ? "down" : "up");
            }

            fireEvent(TiC.EVENT_SWIPE, data, false, false);
        }
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        if (proxy.hasListeners(TiC.EVENT_PROPERTY_SCALE, false)) {
            KrollDict data = dictFromMotionEvent(me);
            data.put("scaleX", scaleX);
            data.put("scaleY", scaleY);
            fireEvent(TiC.EVENT_PROPERTY_SCALE, data, false, false);
        }
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        if (proxy.hasListeners("translate", false)) {
            KrollDict data = dictFromMotionEvent(me);
            data.put("dX", dX);
            data.put("dY", dY);
            fireEvent("translate", data, false, false);
        }
    }
}
