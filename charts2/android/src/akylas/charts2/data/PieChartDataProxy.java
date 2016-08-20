package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.PieData;

import akylas.charts2.datasets.PieChartDataSetProxy;

@Kroll.proxy
public class PieChartDataProxy extends DataProxy {
    public PieData getData() {
        if (_data == null) {
            _data = new PieData();
        }
        return (PieData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return PieChartDataSetProxy.class;
    }
}
