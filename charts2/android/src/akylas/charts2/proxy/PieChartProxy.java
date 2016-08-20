package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.view.TiUIView;

import akylas.charts2.Charts2Module;
import akylas.charts2.data.PieChartDataProxy;
import akylas.charts2.view.PieChartView;
import android.app.Activity;

@Kroll.proxy(creatableInModule = Charts2Module.class, name = "PieChart", propertyAccessors = {
        "transparentCircleColor", "holeColor", "drawSlicesUnderHole",
        "drawHole", "drawCenterText", "drawSliceText", "drawEntryLabels",
        "centerText", "holeRadiusPercent", "transparentCircleRadiusPercent",
        "centerTextRadiusPercent", "usePercentValues", "maxAngle" })
public class PieChartProxy extends PieRadarChartViewBaseProxy {

    @Override
    protected Class dataClass() {
        return PieChartDataProxy.class;
    }

    @Override
    public TiUIView createView(Activity activity) {
        return new PieChartView(this);
    }

}
