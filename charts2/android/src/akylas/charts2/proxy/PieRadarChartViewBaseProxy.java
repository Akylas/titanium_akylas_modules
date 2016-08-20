package akylas.charts2.proxy;

import org.appcelerator.kroll.annotations.Kroll;

import akylas.charts2.Charts2Module;

@Kroll.proxy(parentModule = Charts2Module.class, propertyAccessors = { "rotationAngle",
        "rotation", "minOffset" })
public abstract class PieRadarChartViewBaseProxy extends ChartBaseViewProxy {

}
