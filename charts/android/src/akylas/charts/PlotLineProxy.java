package akylas.charts;

import org.appcelerator.kroll.annotations.Kroll;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.androidplot.xy.FillDirection;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class PlotLineProxy extends XYSerieProxy {
    // Standard Debugging variables
    private static final String TAG = "PlotLineProxy";

    private class PlotLineFormatter extends LineAndPointFormatter {

        public PlotLineFormatter(int lineColor, int vertexColor, int fillColor,
                PointLabelFormatter plf) {
            super(lineColor, vertexColor, fillColor, plf);
        }

        public PlotLineFormatter(int lineColor, int vertexColor, int fillColor,
                PointLabelFormatter plf, FillDirection direction) {
            super(lineColor, vertexColor, fillColor, plf, direction);
        }

        @Override
        public void drawFillPath(final Canvas canvas, final Path path) {
            if (mFillSpacePath) {
                RectF rect = new RectF();
                path.computeBounds(rect, true);
                Rect bounds = new Rect();
                rect.round(bounds);
                PlotLineProxy.this.internalUpdateFillGradient(context, bounds);
            }
            super.drawFillPath(canvas, path);
        }

        @Override
        public void drawLinePath(final Canvas canvas, final Path path) {
            if (mFillSpacePath) {
                RectF rect = new RectF();
                path.computeBounds(rect, true);
                Rect bounds = new Rect();
                rect.round(bounds);
                PlotLineProxy.this.internalUpdateLineGradient(context, bounds);
            }
            super.drawLinePath(canvas, path);
        }
    }

    public PlotLineProxy() {
        super();
    }

    @Override
    protected LineAndPointFormatter createFormatter() {
        return new PlotLineFormatter(Color.BLACK, Color.TRANSPARENT,
                Color.TRANSPARENT, labelformatter, FillDirection.BOTTOM);
    }
}