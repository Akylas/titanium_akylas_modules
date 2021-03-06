package akylas.shapes;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.Gravity;

public class Utils {
	private static final String TAG = "ShapeUtils";


	public static float getRawSize(Object value,
			String defaultValue, Context context) {
		return TiUIHelper.getRawSize(TiConvert.toString(value, defaultValue),
				context);
	}
	
	public static float getRawSize(Object value,
            String defaultValue) {
        return TiUIHelper.getRawSize(TiConvert.toString(value, defaultValue),
                null);
    }
	public static float getRawSize(HashMap dict, String property,
            String defaultValue, Context context) {
        return TiUIHelper.getRawSize(TiConvert.toString(dict, property, defaultValue),
                context);
    }
	public static float getRawSize(HashMap dict, String property, String defaultValue) {
		return getRawSize(dict, property, defaultValue, null);
	}

	public static float getRawSize(HashMap dict, String property,
			Context context) {
		return getRawSize(dict, property, null, context);
	}
	
	public static float getRawSize(Object value,
            Context context) {
        return getRawSize(TiConvert.toString(value), null, context);
    }
	
	public static float getRawSize(Object value) {
        return getRawSize(value, (String)null);
    }
	
	public static float getRawSize(HashMap dict, String property) {
		return getRawSize(dict, property, null, null);
	}

	public static float getRawSizeOrZero(HashMap dict, String property,
			Context context) {
		if (dict.containsKey(property)) {
			return TiUIHelper.getRawSize(TiConvert.toString(dict, property), context);
		}
		return 0;
	}
	
	public static float getRawSizeOrZero(KrollDict dict, String property) {
		return getRawSizeOrZero(dict, property, null);
	}
	
	public static float[] getRawSizeArray(KrollDict dict, String property,
			float[] defaultValue, Context context) {
		if (dict.containsKey(property)) {
			Object[] array = (Object[])dict.get(property);
			float[] result = new float[array.length];
			for (int i = 0; i < array.length; i++) {
				result[i] = TiUIHelper.getRawSize(TiConvert.toString(array[i]), context);
			}
			return result;
		}
		else  {
			return defaultValue;
		}
	}
	public static float[] getRawSizeArray(KrollDict dict, String property, float[] defaultValue) {
		return getRawSizeArray(dict, property, defaultValue, null);
	}

	public static float[] getRawSizeArray(KrollDict dict, String property,
			Context context) {
		return getRawSizeArray(dict, property, null, context);
	}
	public static float[] getRawSizeArray(KrollDict dict, String property) {
		return getRawSizeArray(dict, property, null, null);
	}

	public static void styleOpacity(KrollDict dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
			int alpha  = (int) (dict.optFloat(property, 1.0f) * 255);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setAlpha(alpha);
			}
		}
	}

	public static void styleOpacity(KrollDict dict, String property, Paint paint) {
		styleOpacity(dict, property, new Paint[] { paint });

	}

	public static void styleOpacity(KrollDict dict, Paint paint) {
		styleOpacity(dict, "opacity", new Paint[] { paint });
	}
	
	public static void styleOpacity(KrollDict dict, Paint[] paints) {
		styleOpacity(dict, "opacity", paints);
	}

	// StrokeWidth
	public static void styleStrokeWidth(KrollDict dict, String property,
			String defaultValue, Paint[] paints, Context context) {
		float width = getRawSize(dict, property, defaultValue, context);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setStrokeWidth(width);
		}
	}
	public static void styleStrokeWidth(KrollDict dict, String property,
			String defaultValue, Paint[] paints) {
		float width = getRawSize(dict, property, defaultValue);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setStrokeWidth(width);
		}
	}
	
	public static void styleStrokeWidth(Object value,
            String defaultValue, Paint[] paints) {
        float width = getRawSize(value, defaultValue);
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            paint.setStrokeWidth(width);
        }
    }

	public static void styleStrokeWidth(KrollDict dict, String property,
			String defaultValue, Paint paint, Context context) {
		styleStrokeWidth(dict, property, defaultValue, new Paint[] { paint },
				context);
	}
	
	public static void styleStrokeWidth(KrollDict dict, String property,
			String defaultValue, Paint paint) {
		styleStrokeWidth(dict, property, defaultValue, new Paint[] { paint });
	}
	
	public static void styleStrokeWidth(Object value,
            String defaultValue, Paint paint) {
        styleStrokeWidth(value, defaultValue, new Paint[] { paint });
    }

	public static void styleStrokeWidth(KrollDict dict, String property,
			Paint[] paints, Context context) {
		if (dict.containsKey(property)) {
			float width = getRawSize(dict, property, context);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setStrokeWidth(width);
			}
		}
	}
	
	public static void styleStrokeWidth(Object value,
            Paint[] paints, Context context) {
        float width = getRawSize(value, context);
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            paint.setStrokeWidth(width);
        }
    }

	public static void styleStrokeWidth(KrollDict dict, String property,
			Paint paint, Context context) {
		styleStrokeWidth(dict, property, new Paint[] { paint }, context);
	}
	public static void styleStrokeWidth(KrollDict dict, String property,
			Paint paint) {
		styleStrokeWidth(dict, property, new Paint[] { paint }, null);
	}

	public static void styleStrokeWidth(KrollDict dict, Paint paint,
			Context context) {
		styleStrokeWidth(dict, "width", paint, context);
	}
	
	public static void styleStrokeWidth(KrollDict dict, Paint paint) {
		styleStrokeWidth(dict, "width", paint);
	}

	// Cap
	public static void styleCap(KrollDict dict, String property,
			int defaultValue, Paint paint) {
		Cap cap = Cap.values()[dict.optInt(property, defaultValue)];
		paint.setStrokeCap(cap);
	}

	public static void styleCap(KrollDict dict, String property, Paint paint) {
        if (dict.containsKey(property)) {
            Cap cap = Cap.values()[dict.getInt(property)];
            paint.setStrokeCap(cap);
        }
    }
	public static void styleCap(int value, Paint paint) {
        Cap cap = Cap.values()[value];
        paint.setStrokeCap(cap);
    }

	public static void styleJoin(KrollDict dict, Paint paint) {
		styleJoin(dict, "join", paint);
	}

	// Cap
	public static void styleJoin(KrollDict dict, String property,
			int defaultValue, Paint paint) {
		Join join = Join.values()[dict.optInt(property, defaultValue)];
		paint.setStrokeJoin(join);
	}

	public static void styleJoin(KrollDict dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
			Join join = Join.values()[dict.getInt(property)];
			paint.setStrokeJoin(join);
		}
	}
	
	public static void styleJoin(int value, Paint paint) {
        Join join = Join.values()[value];
        paint.setStrokeJoin(join);
    }

	public static void styleCap(KrollDict dict, Paint paint) {
		styleCap(dict, "cap", paint);
	}

	// Color
	public static void styleColor(KrollDict dict, String property,
			int defaultValue, Paint[] paints) {
		int color = dict.optColor(property, defaultValue);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			setColorForPaint(color, paint);
		}
	}

	public static void styleColor(KrollDict dict, String property,
			int defaultValue, Paint paint) {
		paint.setColor(dict.optColor(property, defaultValue));
	}

	public static void styleColor(KrollDict dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
			styleColor(dict.getColor(property), paint);
		}
	}
	
	public static void styleColor(int color, Paint paint) {
	    setColorForPaint(color, paint);
    }

	public static void setColorForPaint(int color, Paint paint) {
		paint.setColor(color);
	}
	
	public static void setShaderForPaint(Shader shader, Paint paint) {
		paint.setShader(shader);
	}
	
	public static void styleColor(KrollDict dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
			styleColor(dict.getColor(property), paints);
		}
	}
	
	public static void styleColor(int color,
            Paint[] paints) {
        for (int i = 0; i < paints.length; i++) {
            Paint paint = paints[i];
            setColorForPaint(color, paint);
        }
    }

	public static void styleColor(KrollDict dict, Paint paint) {
		styleColor(dict, "color", paint);
	}
	
	public static void styleColor(KrollDict dict, Paint[] paints) {
		styleColor(dict, "color", paints);
	}

	// Margins
	public static void styleMargins(KrollDict dict, String property,
			Object object, String method, Context context) {
		if (dict.containsKey(property)) {
			KrollDict padding = dict.getKrollDict(property);
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

	public static void styleMargins(KrollDict dict, Object object, String m,
			Context context) {
		styleMargins(dict, "padding", object, m, context);
	}

	// Text Widget
	public static void styleTextWidget(KrollDict dict, Paint[] paints,
			Context context) {
		if (dict.containsKey("color")) {
			int color = dict.getColor("color");
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setColor(color);
			}
		}
		styleShadow(dict, "shadow", paints, context);
		
		if (dict.containsKey("font")) {
			KrollDict fontOptions = dict.getKrollDict("font");

			String fontWeight = fontOptions.optString("fontWeight", null);
			String fontFamily = fontOptions.optString("fontFamily", null);
			String fontStyle = fontOptions.optString("fontStyle", null);
			float size = getRawSize(fontOptions, "fontSize", "12", context);

			Typeface typeface = Typeface.create(
					TiUIHelper.toTypeface(context, fontFamily),
					TiUIHelper.toTypefaceStyle(fontWeight, fontStyle));
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setTextSize(size);
				paint.setTypeface(typeface);
			}
		}
		
		styleOpacity(dict, paints);
	}

	public static void styleTextWidget(KrollDict dict, Paint paint,
			Context context) {
		styleTextWidget(dict, new Paint[] { paint }, context);
	}

	// Value Format
	public static void styleValueFormat(KrollDict dict, final HashMap<Integer, String> locations, Object object,
			String method) {
		String pattern = dict.optString("numberFormat", null);
		String patternPos = dict.optString("numberFormatPositive", null);
		String patternNeg = dict.optString("numberFormatNegative", null);

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
			DecimalFormat format = new DecimalFormat("0.0");
			if (realpattern != null)
				format.applyPattern(realpattern);
			String prefix = dict.optString("numberPrefix", "");
			format.setNegativePrefix(dict.optString("numberPrefixNegative",
					prefix));
			format.setPositivePrefix(dict.optString("numberPrefixPositive",
					prefix));
			String suffix = dict.optString("numberSuffix", "");
			format.setNegativeSuffix(dict.optString("numberSuffixNegative",
					suffix));
			format.setPositiveSuffix(dict.optString("numberSuffixPositive",
					suffix));
			try {
				Method m = object.getClass().getMethod(method, Format.class);
				m.invoke(object, format);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void styleValueFormat(KrollDict dict, Object object,
			String method) {
		styleValueFormat(dict, null, object, method);
	}
	
	// Emboss
	public static EmbossMaskFilter styleEmboss(KrollDict dict, String property) {
		if (dict.containsKey(property)) {
	        return styleEmboss(dict.getKrollDict(property));
		}
		return null;
	}
	
	public static EmbossMaskFilter styleEmboss(KrollDict emboss) {
	    if (emboss == null) return null;
        float[] direction = emboss.optFloatArray("direction", new float[]{1, 1, 1});
        float ambient = emboss.optFloat("ambient", 0.4f);
        float specular = emboss.optFloat("specular", 10);
        float blurRadius = emboss.optFloat("radius", 8.2f);
        EmbossMaskFilter emf = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
        return emf;
    }
	
//	public static EmbossMaskFilter styleEmboss(KrollDict dict) {
//		return styleEmboss(dict, "emboss");
//	}
	
	public static void styleEmboss(KrollDict dict, String property, Paint paint) {
        EmbossMaskFilter emf = styleEmboss(dict, property);
	    paint.setMaskFilter(emf);
	}
	
	public static void styleEmboss(KrollDict dict, Paint paint) {
	    EmbossMaskFilter emf = styleEmboss(dict);
        paint.setMaskFilter(emf);
	}
	
	// Dash
	public static DashPathEffect getDashEffect(KrollDict dict, String property, Context context) {
		if (dict.containsKey(property)) {
	        return getDashEffect(dict.getKrollDict(property), context);
		}
		return null;
	}
	public static DashPathEffect getDashEffect(KrollDict dash, Context context) {
        if (dash == null) return null;
        float[] pattern = getRawSizeArray(dash, "pattern", new float[]{10,20}, context);
        float phase = dash.optFloat("phase", 0.0f);
        DashPathEffect effect = new DashPathEffect(pattern, phase);
        return effect;
    }
	public static DashPathEffect styleDash(KrollDict dict, String property) {
		return getDashEffect(dict, property, null);
	}
	
	public static void styleDash(KrollDict dict, String property, Paint[] paints, Context context) {
		DashPathEffect effect = getDashEffect(dict, property, context);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setPathEffect(effect);
		}
	}
	
	public static void styleDash(KrollDict dict, String property, Paint paint, Context context) {
		paint.setPathEffect(getDashEffect(dict, property, context));
	}
	public static void styleDash(KrollDict dict, String property, Paint paint) {
		styleDash(dict, property, paint, null);
	}
	
//	public static void styleDash(KrollDict dict, Paint paint, Context context) {
//		styleDash(dict, "dash", paint, context);
//	}
	
	 public static void styleDash(KrollDict dict, Paint paint, Context context) {
	        DashPathEffect effect = getDashEffect(dict, context);
            paint.setPathEffect(effect);
    }
	 
	 public static void styleDash(KrollDict dict, Paint paint) {
	     styleDash(dict, paint, null);
 }

	
	public static void styleShadow(HashMap shadowOptions, Paint[] paints, Context context) {
		float offsetx = 0.0f;
		float offsety = 0.0f;
		HashMap offset = TiConvert.toHashMap(shadowOptions.get("offset"));
		
		if (offset != null) {
			offsetx = Utils.getRawSizeOrZero(offset, "x", context);
			offsety = Utils.getRawSizeOrZero(offset, "y", context);
		}
		float blurRadius =  Utils.getRawSize(shadowOptions, "radius", "3");
		int color = TiConvert.toColor(shadowOptions, "color", Color.BLACK);	
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setShadowLayer(blurRadius, offsetx, offsety, color);
		}
	}
	
	public static void styleShadow(HashMap shadowOptions, Paint paint, Context context) {
		styleShadow(shadowOptions, new Paint[]{paint}, context);
	}
	public static void styleShadow(HashMap shadowOptions, Paint paint) {
		styleShadow(shadowOptions, new Paint[]{paint}, null);
	}
	public static void styleShadow(HashMap dict, String property, Paint[] paints, Context context) {
		if (dict.containsKey(property)) {
			styleShadow(TiConvert.toHashMap(dict.get(property)), paints, context);
		}
	}
	
	public static void styleShadow(HashMap dict, String property, Paint paint, Context context) {
		styleShadow(dict, property, new Paint[]{paint}, context);
	}

	public static void styleShadow(HashMap dict, String property, Paint[] paints) {
		styleShadow(dict, property, paints, null);
	}
	
	public static void styleShadow(HashMap dict, String property, Paint paint) {
		styleShadow(dict, property, new Paint[]{paint}, null);
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
