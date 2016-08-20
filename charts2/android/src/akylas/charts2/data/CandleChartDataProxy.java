package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.CandleData;

import akylas.charts2.datasets.CandleChartDataSetProxy;

@Kroll.proxy
public class CandleChartDataProxy
        extends BarLineScatterCandleBubbleChartDataProxy {
    public CandleData getData() {
        if (_data == null) {
            _data = new CandleData();
        }
        return (CandleData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return CandleChartDataSetProxy.class;
    }
}
