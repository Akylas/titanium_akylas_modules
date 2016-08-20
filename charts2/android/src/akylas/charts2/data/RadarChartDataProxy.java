package akylas.charts2.data;

import org.appcelerator.kroll.annotations.Kroll;

import com.github.mikephil.charting.data.RadarData;

import akylas.charts2.datasets.RadarChartDataSetProxy;

@Kroll.proxy
public class RadarChartDataProxy extends DataProxy {

    public RadarData getData() {
        if (_data == null) {
            _data = new RadarData();
        }
        return (RadarData)_data;
    }
    @Override
    protected Class dataSetClass() {
        return RadarChartDataSetProxy.class;
    }

}
