package akylas.charts2.data;


import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.CombinedData;

@Kroll.proxy
public class CombinedChartDataProxy
        extends BarLineScatterCandleBubbleChartDataProxy {
    BarChartDataProxy _barDataProxy;
    LineChartDataProxy _lineDataProxy;
    CandleChartDataProxy _candleDataProxy;
    BubbleChartDataProxy _bubbleDataProxy;
    public CombinedData getData() {
        if (_data == null) {
            _data = new CombinedData();
        }
        return (CombinedData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return null;
    }
    
    @Override
    public void release() {
        super.release();
        if (_barDataProxy != null) {
            _barDataProxy.release();
            _barDataProxy = null;
        }
        if (_lineDataProxy != null) {
            _lineDataProxy.release();
            _lineDataProxy = null;
        }
        if (_candleDataProxy != null) {
            _candleDataProxy.release();
            _candleDataProxy = null;
        }
        if (_bubbleDataProxy != null) {
            _bubbleDataProxy.release();
            _bubbleDataProxy = null;
        }
    }
    
    @Override
    public void unarchivedWithRootProxy(KrollProxy rootProxy) {
        if (_barDataProxy != null) {
            _barDataProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_lineDataProxy != null) {
            _lineDataProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_candleDataProxy != null) {
            _candleDataProxy.unarchivedWithRootProxy(rootProxy);
        }
        if (_bubbleDataProxy != null) {
            _bubbleDataProxy.unarchivedWithRootProxy(rootProxy);
        }
    }
    
    @Kroll.method
    @Kroll.getProperty
    public BarChartDataProxy getChartData() {
        if (_barDataProxy == null) {
            _barDataProxy = (BarChartDataProxy) KrollProxy
                    .createProxy(BarChartDataProxy.class,
                            null, null, null);
            _barDataProxy.updateKrollObjectProperties();
            getData().setData(_barDataProxy.getData());
        }
        return _barDataProxy;
    }
    
    @Kroll.method
    @Kroll.setProperty
    public void setChartData(HashMap value) {
        getChartData().applyProperties(value);
    }
    
    @Kroll.method
    @Kroll.getProperty
    public LineChartDataProxy getLineData() {
        if (_lineDataProxy == null) {
            _lineDataProxy = (LineChartDataProxy) KrollProxy
                    .createProxy(LineChartDataProxy.class,
                            null, null, null);
            _lineDataProxy.updateKrollObjectProperties();
            getData().setData(_lineDataProxy.getData());
        }
        return _lineDataProxy;
    }
    
    @Kroll.method
    @Kroll.setProperty
    public void setLineData(HashMap value) {
        getLineData().applyProperties(value);
    }

    @Kroll.method
    @Kroll.getProperty
    public CandleChartDataProxy getCandleData() {
        if (_candleDataProxy == null) {
            _candleDataProxy = (CandleChartDataProxy) KrollProxy
                    .createProxy(CandleChartDataProxy.class,
                            null, null, null);
            _candleDataProxy.updateKrollObjectProperties();
            getData().setData(_candleDataProxy.getData());
        }
        return _candleDataProxy;
    }
    
    @Kroll.method
    @Kroll.setProperty
    public void setCandleData(HashMap value) {
        getCandleData().applyProperties(value);
    }

    @Kroll.method
    @Kroll.getProperty
    public BubbleChartDataProxy getBubbleData() {
        if (_bubbleDataProxy == null) {
            _bubbleDataProxy = (BubbleChartDataProxy) KrollProxy
                    .createProxy(BubbleChartDataProxy.class,
                            null, null, null);
            _bubbleDataProxy.updateKrollObjectProperties();
            getData().setData(_bubbleDataProxy.getData());
        }
        return _bubbleDataProxy;
    }
    
    @Kroll.method
    @Kroll.setProperty
    public void setBubbleData(HashMap value) {
        getBubbleData().applyProperties(value);
    }
}
