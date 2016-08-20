package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.ScatterData;

import akylas.charts2.datasets.ScatterChartDataSetProxy;

@Kroll.proxy
public class ScatterChartDataProxy
        extends BarLineScatterCandleBubbleChartDataProxy {
    public ScatterData getData() {
        if (_data == null) {
            _data = new ScatterData();
        }
        return (ScatterData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return ScatterChartDataSetProxy.class;
    }
}
