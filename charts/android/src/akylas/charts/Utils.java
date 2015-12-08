package akylas.charts;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.util.TiUIHelper.FontDesc;
import org.appcelerator.titanium.view.TiGradientDrawable.GradientType;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;

public class Utils {
	private static final String TAG = "ChartsUtils";

	private static final TiPoint DEFAULT_START_POINT = new TiPoint(0, 0);
	private static final TiPoint DEFAULT_END_POINT = new TiPoint("0", "100%");
	private static final TiDimension DEFAULT_RADIUS = new TiDimension(1.0,
			TiDimension.TYPE_UNDEFINED);

	public static float getRawSize(HashMap dict, String property,
			String defaultValue, Context context) {
		return TiUIHelper.getRawSize(TiConvert.toString(dict, property, defaultValue),
				context);
	}

	public static float getRawSize(HashMap dict, String property,
			Context context) {
		return getRawSize(dict, property, null, context);
	}
	
	public static float getRawSize(Object value,
            String defaultValue) {
        return TiUIHelper.getRawSize(TiConvert.toString(value, defaultValue),
                null);
    }

	public static float getRawSizeOrZero(HashMap dict, String property,
			Context context) {
		if (dict.containsKey(property)) {
			return TiUIHelper.getRawSize(TiConvert.toString(dict, property), context);
		}
		return 0;
	}
	
	public static float[] getRawSizeArray(HashMap dict, String property,
			float[] defaultValue) {
		if (dict.containsKey(property)) {
			Object[] array = (Object[])dict.get(property);
			float[] result = new float[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = TiUIHelper.getRawSize(TiConvert.toString(array[i]), null);
			}
			return result;
		}
		else  {
			return defaultValue;
		}
	}

	public static float[] getRawSizeArray(HashMap dict, String property) {
		return getRawSizeArray(dict, property, null);
	}

	public static void styleOpacity(HashMap dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
		    styleOpacity(TiConvert.toFloat(dict, property), paints);
		}
	}
	
	public static void styleOpacity(float value,
            Paint[] paints) {
        int alpha  = (int) (value * 255);
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            paint.setAlpha(alpha);
        }
    }

	public static void styleOpacity(HashMap dict, String property, Paint paint) {
		styleOpacity(dict, property, new Paint[] { paint });

	}

	public static void styleOpacity(HashMap dict, Paint paint) {
		styleOpacity(dict, "opacity", new Paint[] { paint });
	}
	
	public static void styleOpacity(float value, Paint paint) {
        styleOpacity(value, new Paint[] { paint });
    }
	
	public static void styleOpacity(HashMap dict, Paint[] paints) {
		styleOpacity(dict, "opacity", paints);
	}

	// StrokeWidth
	public static void styleStrokeWidth(HashMap dict, String property,
			String defaultValue, Paint[] paints, Context context) {
		float width = getRawSize(dict, property, defaultValue, context);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setStrokeWidth(width);
		}
	}
	
	public static void styleStrokeWidth(HashMap dict, String property,
            String defaultValue, Paint paint) {
        styleStrokeWidth(dict, property, defaultValue, paint, null);
    }
	public static void styleStrokeWidth(HashMap dict, String property,
			String defaultValue, Paint paint, Context context) {
		styleStrokeWidth(dict, property, defaultValue, new Paint[] { paint },
				context);
	}

	public static void styleStrokeWidth(HashMap dict, String property,
			Paint[] paints, Context context) {
		if (dict.containsKey(property)) {
			float width = getRawSize(dict, property, context);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setStrokeWidth(width);
			}
		}
	}

	public static void styleStrokeWidth(HashMap dict, String property,
			Paint paint, Context context) {
		styleStrokeWidth(dict, property, new Paint[] { paint }, context);
	}
	
	public static void styleStrokeWidth(HashMap dict, String property,
            Paint paint) {
        styleStrokeWidth(dict, property, paint, null);
    }


	public static void styleStrokeWidth(HashMap dict, Paint paint,
			Context context) {
		styleStrokeWidth(dict, "width", paint, context);
	}
	
	public static void styleStrokeWidth(Object value,
            String defaultValue, Paint paint) {
        styleStrokeWidth(value, defaultValue, new Paint[] { paint });
    }
	
	public static void styleStrokeWidth(Object value,
            String defaultValue, Paint[] paints) {
        float width = getRawSize(value, defaultValue);
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            paint.setStrokeWidth(width);
        }
    }

	// Cap
	public static void styleCap(HashMap dict, String property,
			int defaultValue, Paint paint) {
		Cap cap = Cap.values()[TiConvert.toInt(dict, property, defaultValue)];
		paint.setStrokeCap(cap);
	}
	
	public static void styleCap(int value, Paint paint) {
        Cap cap = Cap.values()[value];
        paint.setStrokeCap(cap);
    }
	public static void styleCap(HashMap dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
		    styleCap(TiConvert.toInt(dict, property), paint);
		}
	}

	public static void styleJoin(HashMap dict, Paint paint) {
		styleCap(dict, "join", paint);
	}

	// Cap
	public static void styleJoin(HashMap dict, String property,
			int defaultValue, Paint paint) {
		Join join = Join.values()[TiConvert.toInt(dict, property, defaultValue)];
		paint.setStrokeJoin(join);
	}
	
	public static void styleJoin(int value,  Paint paint) {
        Join join = Join.values()[value];
        paint.setStrokeJoin(join);
    }
	public static void styleJoin(HashMap dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
		    styleJoin(TiConvert.toInt(dict, property), paint);
		}
	}

	public static void styleCap(HashMap dict, Paint paint) {
		styleCap(dict, "cap", paint);
	}

	// Color
	public static void styleColor(HashMap dict, String property,
			int defaultValue, Paint[] paints) {
		int color = TiConvert.toColor(dict, property, defaultValue);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setColor(color);
		}
	}

	public static void styleColor(HashMap dict, String property,
			int defaultValue, Paint paint) {
		paint.setColor(TiConvert.toColor(dict, property, defaultValue));
	}

	public static void styleColor(HashMap dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
			paint.setColor(TiConvert.toColor(dict, property));
		}
	}

	public static void styleColor(HashMap dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
			int color = TiConvert.toColor(dict, property);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setColor(color);
			}
		}
	}
	
    public static void styleColor(int color, Paint paint) {
        paint.setColor(color);
    }

	public static void styleColor(HashMap dict, Paint paint) {
		styleColor(dict, "color", paint);
	}
	
	public static void styleColor(HashMap dict, Paint[] paints) {
		styleColor(dict, "color", paints);
	}

	// Margins
	public static void styleMargins(HashMap dict, String property,
			Object object, String method, Context context) {
		if (dict.containsKey(property)) {
		    HashMap padding = TiConvert.toHashMap(dict.get(property));
			float top = getRawSizeOrZero(padding, "top", context);
			float bottom = getRawSizeOrZero(padding, "bottom", context);
			float left = getRawSizeOrZero(padding, "left", context);
			float right = getRawSizeOrZero(padding, "right", context);
			try {
				Method m = object.getClass().getMethod(method, float.class,
						float.class, float.class, float.class);
				m.invoke(object, left, top, right, bottom);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void styleMargins(HashMap dict, Object object, String m,
			Context context) {
		styleMargins(dict, "padding", object, m, context);
	}

	// Text Widget
	public static void styleTextWidget(HashMap dict, Paint[] paints,
			Context context) {
		if (dict.containsKey(TiC.PROPERTY_COLOR)) {
			int color = TiConvert.toColor(dict, TiC.PROPERTY_COLOR);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setColor(color);
			}
		}
		styleShadow(dict, AkylasChartsModule.PROPERTY_SHADOW, paints);
		Align align = Align.values()[TiConvert.toInt(dict, TiC.PROPERTY_TEXT_ALIGN, 1)];
		
		if (dict.containsKey(TiC.PROPERTY_FONT)) {
		    FontDesc desc = TiUIHelper.getFontStyle(context, TiConvert.toHashMap(dict.get(TiC.PROPERTY_FONT)));
            float fontSize = TiUIHelper.getRawSize(desc.sizeUnit, desc.size, context);
			for (int i = 0; i < paints.length; i++) {
			    
				Paint paint = paints[i];
				paint.setTextSize(fontSize);
                paint.setTypeface(desc.typeface);
                paint.setTextAlign(align);
			}
		}
		else {
		    float defaultSize = TiUIHelper.getRawSize(null, context);
		    for (int i = 0; i < paints.length; i++) {
                Paint paint = paints[i];
                paint.setTextSize(defaultSize);
                paint.setTextAlign(align);
            }
		}
		
		styleOpacity(dict, paints);
	}

	public static void styleTextWidget(HashMap dict, Paint paint,
			Context context) {
		styleTextWidget(dict, new Paint[] { paint }, context);
	}

	// Value Format
	public static void styleValueFormat(HashMap dict, final HashMap<Integer, String> locations, Object object,
			String method) {
		String pattern = TiConvert.toString(dict, "numberFormat", null);
		String patternPos = TiConvert.toString(dict, "numberFormatPositive", null);
		String patternNeg = TiConvert.toString(dict, "numberFormatNegative", null);

		String realpattern = pattern;
		if (patternPos != null && patternNeg != null)
			realpattern = patternPos + ";" + patternNeg;
		else if (patternPos != null)
			realpattern = patternPos + ";";
		else if (patternNeg != null)
			realpattern = ";" + patternNeg;

		boolean hasSuffixOrPrefix = (dict.containsKey("numberSuffix")
				|| dict.containsKey("numberPrefix")
				|| dict.containsKey("numberPrefixNegative")
				|| dict.containsKey("numberPrefixPositive")
				|| dict.containsKey("numberSuffixPositive") || dict
				.containsKey("numberSuffixNegative"));
	
		if (realpattern != null || hasSuffixOrPrefix) {
			DecimalFormat format = new DecimalFormat((realpattern != null)?realpattern:"0.0");
			String prefix = TiConvert.toString(dict, "numberPrefix", "");
			format.setNegativePrefix(TiConvert.toString(dict, "numberPrefixNegative",
					prefix));
			format.setPositivePrefix(TiConvert.toString(dict, "numberPrefixPositive",
					prefix));
			String suffix = TiConvert.toString(dict, "numberSuffix", "");
			format.setNegativeSuffix(TiConvert.toString(dict, "numberSuffixNegative",
					suffix));
			format.setPositiveSuffix(TiConvert.toString(dict, "numberSuffixPositive",
					suffix));
			try {
				Method m = object.getClass().getMethod(method, Format.class);
				m.invoke(object, format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void styleValueFormat(HashMap dict, Object object,
			String method) {
		styleValueFormat(dict, null, object, method);
	}

	// Gradient
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static float[] loadColors(Object[] colors, int[] result) {
		float[] offsets = null;
		int offsetCount = 0;
		for (int i = 0; i < colors.length; i++) {
			Object color = colors[i];
			if (color instanceof HashMap) {
				HashMap<String, Object> colorRefObject = (HashMap) color;
				result[i] = TiConvert.toColor(colorRefObject, "color");

				if (offsets == null) {
					offsets = new float[colors.length];
				}

				float offset = TiConvert.toFloat(colorRefObject, "offset", -1);
				if (offset >= 0.0f && offset <= 1.0f) {
					offsets[offsetCount++] = offset;
				}

			} else {
				result[i] = TiConvert.toColor(TiConvert.toString(color));
			}
		}

		// If the number of offsets doesn't match the number of colors,
		// just distribute the colors evenly along the gradient line.
		if (offsetCount != result.length) {
			offsets = null;
		}
		return offsets;
	}

	@SuppressWarnings("rawtypes")
	public static Shader styleGradient(HashMap properties, Context context, Rect rect) {
		String type = TiConvert.toString(properties, "type", "linear");
		GradientType gradientType = GradientType.LINEAR_GRADIENT;
		TiDimension startRadius = DEFAULT_RADIUS;
		TiPoint startPoint = DEFAULT_START_POINT;
		TiPoint endPoint = DEFAULT_END_POINT;
		int radiusType = TiDimension.TYPE_WIDTH;
		int width = rect.width();
		int height = rect.height();
		if (width > height)
			radiusType = TiDimension.TYPE_HEIGHT;
		
		if (type.equals("radial")) {
			startRadius = new TiDimension("50%", radiusType);
			gradientType = GradientType.RADIAL_GRADIENT;
			startPoint = endPoint = new TiPoint("50%", "50%");
		}
		
		if (properties.containsKey("startRadius")) {
			startRadius = TiConvert.toTiDimension(properties, "startRadius", radiusType);
		}
		
		if (properties.containsKey("startPoint")) {
			startPoint = new TiPoint((HashMap) properties.get("startPoint"), 0, 0);
		}
		if (properties.containsKey("endPoint")) {
			endPoint = new TiPoint((HashMap) properties.get("endPoint"), 0, 1);
		}

		Object colors = properties.get("colors");
		if (!(colors instanceof Object[])) {
			Log.w(TAG, "Android does not support gradients without colors.");
			return null;
		}

		Object[] array = (Object[]) colors;

		int[] resultColors = new int[array.length];
		float[] offsets = loadColors((Object[]) colors, resultColors);

		if (gradientType == GradientType.LINEAR_GRADIENT) {
			float x0 = rect.left + startPoint.getX().getAsPixels(width, height);
			float y0 = rect.top + startPoint.getY().getAsPixels(width, height);
			float x1 = rect.left + endPoint.getX().getAsPixels(width, height);
			float y1 = rect.top + endPoint.getY().getAsPixels(width, height);
			return new LinearGradient(x0, y0, x1, y1, resultColors, offsets,
					TileMode.CLAMP);
		} else {
			float x0 = rect.left + startPoint.getX().getAsPixels(width, height);
			float y0 = rect.top + startPoint.getY().getAsPixels(width, height);
			float radius0 = startRadius.getAsPixels(width, height);
			if (radius0 <= 0) return null; 
			return new RadialGradient(x0, y0, radius0, resultColors, offsets, TileMode.CLAMP);
		}
	}
	public static Shader styleGradient(HashMap properties, View view) {
		Rect rect = new Rect();
		view.getDrawingRect(rect);
		return styleGradient(properties, view.getContext(), rect);
	}

	
	// Emboss
	public static EmbossMaskFilter styleEmboss(HashMap dict, String property) {
		if (dict.containsKey(property)) {
		    HashMap emboss = TiConvert.toHashMap(dict.get(property));
			float[] direction = TiConvert.toFloatArray(emboss, 
			        "direction", new float[]{1, 1, 1});
			float ambient = TiConvert.toFloat(emboss, "ambient", 0.4f);
			float specular = TiConvert.toFloat(emboss, "specular", 10);
			float blurRadius = TiConvert.toFloat(emboss, "blurRadius", 8.2f);
	        EmbossMaskFilter emf = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
	        return emf;
		}
		return null;
	}
	
	public static EmbossMaskFilter styleEmboss(KrollDict dict) {
		return styleEmboss(dict, "emboss");
	}
	
	public static void styleEmboss(HashMap dict, String property, Paint paint) {
        EmbossMaskFilter emf = styleEmboss(dict, property);
		if (emf != null) {
	        paint.setMaskFilter(emf);
		}
	}
	
	public static void styleEmboss(HashMap dict, Paint paint) {
		styleEmboss(dict, "emboss", paint);
	}
	
	// Dash
	public static DashPathEffect styleDash(HashMap dict, String property) {
		if (dict.containsKey(property)) {
		    HashMap dash = TiConvert.toHashMap(dict.get(property));
			float[] pattern = getRawSizeArray(dash, "pattern", new float[]{10,20});
			float phase = TiConvert.toFloat(dash, "phase", 0.0f);
			DashPathEffect effect = new DashPathEffect(pattern, phase);
	        return effect;
		}
		return null;
	}
	
	public static void styleDash(HashMap dict, String property, Paint[] paints) {
		DashPathEffect effect = styleDash(dict, property);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setPathEffect(effect);
		}
	}
	
	public static void styleDash(HashMap dict, String property, Paint paint) {
		paint.setPathEffect(styleDash(dict, property));
	}
	
	public static void styleDash(HashMap dict, Paint paint) {
		styleDash(dict, "dash", paint);
	}
	
	public static void styleShadow(HashMap dict, String property, Paint[] paints) {
		if (dict.containsKey(property)) {
			styleShadow(TiConvert.toHashMap(dict.get(property)), paints);
		}
	}
	
	public static void styleShadow(HashMap shadowOptions, Paint[] paints) {
        float offsetx = 0.0f;
        float offsety = 0.0f;
        HashMap offset = (shadowOptions != null)?TiConvert.toHashMap(shadowOptions.get(TiC.PROPERTY_OFFSET)):null;
        
        if (offset != null) {
            offsetx = TiUIHelper.getInPixels(offset, TiC.PROPERTY_X);
            offsety = TiUIHelper.getInPixels(offset, TiC.PROPERTY_Y);
        }
        float blurRadius = TiUIHelper.getInPixels(shadowOptions, "radius");
        int color = TiConvert.toColor(shadowOptions, TiC.PROPERTY_COLOR, Color.BLACK);
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            paint.setShadowLayer(blurRadius, offsetx, offsety, color);
        }
    }
	
	public static void styleShadow(HashMap dict, String property, Paint paint) {
		styleShadow(dict, property, new Paint[]{paint});
	}
	
	public static void styleShadow(HashMap shadowOptions, Paint paint) {
        styleShadow(shadowOptions, new Paint[]{paint});
    }
	
	public static int gravityFromAlignment(int alignment) {
		switch (alignment) {
		case 0:
			return Gravity.LEFT;
		case 2:
			return Gravity.RIGHT;
		case 3:
			return Gravity.TOP;
		case 5:
			return Gravity.BOTTOM;
		default:
		case 1:
		case 4:
			return Gravity.CENTER;
		}
	}
}
