package akylas.charts;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiGradientDrawable.GradientType;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
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

	public static float getRawSize(KrollDict dict, String property,
			String defaultValue, Context context) {
		return TiUIHelper.getRawSize(dict.optString(property, defaultValue),
				context);
	}

	public static float getRawSize(KrollDict dict, String property,
			Context context) {
		return getRawSize(dict, property, null, context);
	}

	public static float getRawSizeOrZero(KrollDict dict, String property,
			Context context) {
		if (dict.containsKey(property)) {
			return TiUIHelper.getRawSize(dict.getString(property), context);
		}
		return 0;
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

	public static float[] getRawSizeArray(KrollDict dict, String property,
			Context context) {
		return getRawSizeArray(dict, property, null, context);
	}

	public static void styleOpacity(KrollDict dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
			int alpha  = (int) (dict.getFloat(property) * 255);
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
			String defaultValue, Paint paint, Context context) {
		styleStrokeWidth(dict, property, defaultValue, new Paint[] { paint },
				context);
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

	public static void styleStrokeWidth(KrollDict dict, String property,
			Paint paint, Context context) {
		styleStrokeWidth(dict, property, new Paint[] { paint }, context);
	}

	public static void styleStrokeWidth(KrollDict dict, Paint paint,
			Context context) {
		styleStrokeWidth(dict, "width", paint, context);
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

	public static void styleJoin(KrollDict dict, Paint paint) {
		styleCap(dict, "join", paint);
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

	public static void styleCap(KrollDict dict, Paint paint) {
		styleCap(dict, "cap", paint);
	}

	// Color
	public static void styleColor(KrollDict dict, String property,
			int defaultValue, Paint[] paints) {
		int color = dict.optColor(property, defaultValue);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setColor(color);
		}
	}

	public static void styleColor(KrollDict dict, String property,
			int defaultValue, Paint paint) {
		paint.setColor(dict.optColor(property, defaultValue));
	}

	public static void styleColor(KrollDict dict, String property, Paint paint) {
		if (dict.containsKey(property)) {
			paint.setColor(dict.getColor(property));
		}
	}

	public static void styleColor(KrollDict dict, String property,
			Paint[] paints) {
		if (dict.containsKey(property)) {
			int color = dict.getColor(property);
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setColor(color);
			}
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
	public static Shader styleGradient(KrollDict properties, Context context, Rect rect) {
		String type = properties.optString("type", "linear");
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
			float x0 = rect.left + startPoint.getX().getAsPixels(context, width, height);
			float y0 = rect.top + startPoint.getY().getAsPixels(context, width, height);
			float x1 = rect.left + endPoint.getX().getAsPixels(context, width, height);
			float y1 = rect.top + endPoint.getY().getAsPixels(context, width, height);
			return new LinearGradient(x0, y0, x1, y1, resultColors, offsets,
					TileMode.CLAMP);
		} else {
			float x0 = rect.left + startPoint.getX().getAsPixels(context, width, height);
			float y0 = rect.top + startPoint.getY().getAsPixels(context, width, height);
			float radius0 = startRadius.getAsPixels(context, width, height);
			if (radius0 <= 0) return null; 
			return new RadialGradient(x0, y0, radius0, resultColors, offsets, TileMode.CLAMP);
		}
	}
	public static Shader styleGradient(KrollDict properties, View view) {
		Rect rect = new Rect();
		view.getDrawingRect(rect);
		return styleGradient(properties, view.getContext(), rect);
	}

	
	// Emboss
	public static EmbossMaskFilter styleEmboss(KrollDict dict, String property) {
		if (dict.containsKey(property)) {
			KrollDict emboss = dict.getKrollDict(property);
			float[] direction = emboss.optFloatArray("direction", new float[]{1, 1, 1});
			float ambient = emboss.optFloat("ambient", 0.4f);
			float specular = emboss.optFloat("specular", 10);
			float blurRadius = emboss.optFloat("blurRadius", 8.2f);
	        EmbossMaskFilter emf = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
	        return emf;
		}
		return null;
	}
	
	public static EmbossMaskFilter styleEmboss(KrollDict dict) {
		return styleEmboss(dict, "emboss");
	}
	
	public static void styleEmboss(KrollDict dict, String property, Paint paint) {
        EmbossMaskFilter emf = styleEmboss(dict, property);
		if (emf != null) {
	        paint.setMaskFilter(emf);
		}
	}
	
	public static void styleEmboss(KrollDict dict, Paint paint) {
		styleEmboss(dict, "emboss", paint);
	}
	
	// Dash
	public static DashPathEffect styleDash(KrollDict dict, String property, Context context) {
		if (dict.containsKey(property)) {
			KrollDict dash = dict.getKrollDict(property);
			float[] pattern = getRawSizeArray(dash, "pattern", new float[]{10,20}, context);
			float phase = dash.optFloat("phase", 0.0f);
			DashPathEffect effect = new DashPathEffect(pattern, phase);
	        return effect;
		}
		return null;
	}
	
	public static void styleDash(KrollDict dict, String property, Paint[] paints, Context context) {
		DashPathEffect effect = styleDash(dict, property, context);
		for (int i = 0; i < paints.length; i++) {
			Paint paint = paints[i];
			paint.setPathEffect(effect);
		}
	}
	
	public static void styleDash(KrollDict dict, String property, Paint paint, Context context) {
		paint.setPathEffect(styleDash(dict, property, context));
	}
	
	public static void styleDash(KrollDict dict, Paint paint, Context context) {
		styleDash(dict, "dash", paint, context);
	}
	
	public static void styleShadow(KrollDict dict, String property, Paint[] paints, Context context) {
		if (dict.containsKey(property)) {
			KrollDict shadowOptions = dict.getKrollDict(property);
			float offsetx = 0.0f;
			float offsety = 0.0f;
			KrollDict offset = shadowOptions.getKrollDict("offset");
			
			if (offset != null) {
				offsetx = Utils.getRawSizeOrZero(offset, "y", context);
				offsety = Utils.getRawSizeOrZero(offset, "x", context);
			}
			float blurRadius = Utils.getRawSizeOrZero(shadowOptions, "radius", context);
			int color = shadowOptions.optColor("color", Color.BLACK);	
			for (int i = 0; i < paints.length; i++) {
				Paint paint = paints[i];
				paint.setShadowLayer(blurRadius, offsetx, offsety, color);
			}
		}
	}
	
	public static void styleShadow(KrollDict dict, String property, Paint[] paints) {
		styleShadow(dict, property, paints, null);
	}
	
	public static void styleShadow(KrollDict dict, String property, Paint paint, Context context) {
		styleShadow(dict, property, new Paint[]{paint}, context);
	}
	
	public static void styleShadow(KrollDict dict, String property, Paint paint) {
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