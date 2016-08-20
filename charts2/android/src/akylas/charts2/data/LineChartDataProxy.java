package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.LineData;

import akylas.charts2.datasets.LineChartDataSetProxy;

@Kroll.proxy
public class LineChartDataProxy extends DataProxy {
    
    public LineData getData() {
        if (_data == null) {
            _data = new LineData();
        }
        return (LineData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return LineChartDataSetProxy.class;
    }

}
