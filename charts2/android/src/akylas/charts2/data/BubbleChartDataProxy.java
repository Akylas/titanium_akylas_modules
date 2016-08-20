package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.BubbleData;

import akylas.charts2.datasets.BubbleChartDataSetProxy;

@Kroll.proxy
public class BubbleChartDataProxy
        extends BarLineScatterCandleBubbleChartDataProxy {
    public BubbleData getData() {
        if (_data == null) {
            _data = new BubbleData();
        }
        return (BubbleData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return BubbleChartDataSetProxy.class;
    }
}
