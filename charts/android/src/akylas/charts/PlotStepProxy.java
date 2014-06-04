package akylas.charts;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.StepFormatter;

@Kroll.proxy(creatableInModule = AkylasChartsModule.class)
public class PlotStepProxy extends XYSerieProxy {
	// Standard Debugging variables
	private static final String TAG = "PlotStepProxy";

    private boolean mFillSpacePath = false;
    
    private class PlotStepFormatter extends StepFormatter {

        public PlotStepFormatter(Integer lineColor, Integer fillColor) {
            super(lineColor, fillColor);
        }

        @Override
        public void drawFillPath(final Canvas canvas, final Path path) {
            if (mFillSpacePath) {
                RectF rect = new RectF();
                path.computeBounds(rect, true);
                Rect bounds = new Rect();
                rect.round(bounds);
                PlotStepProxy.this.internalUpdateFillGradient(context, bounds, getProperties());
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
                PlotStepProxy.this.internalUpdateLineGradient(context, bounds, getProperties());
            }
            super.drawLinePath(canvas, path);
        }
    }
    
	// Constructor
	public PlotStepProxy(TiContext tiContext) {
		super(tiContext);
	}

	public PlotStepProxy() {
		super();
	}
	
    @Override
    protected LineAndPointFormatter createFormatter() {
        return new PlotStepFormatter(Color.BLACK, Color.TRANSPARENT);
    }

}