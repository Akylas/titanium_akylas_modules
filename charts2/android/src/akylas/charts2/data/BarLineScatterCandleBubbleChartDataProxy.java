package akylas.charts2.data;


import org.appcelerator.kroll.annotations.Kroll;

import akylas.charts2.datasets.BarLineScatterCandleBubbleChartDataSetProxy;

@Kroll.proxy
public class BarLineScatterCandleBubbleChartDataProxy extends DataProxy {
    
    @Override
    protected Class dataSetClass() {
        return BarLineScatterCandleBubbleChartDataSetProxy.class;
    }

}
