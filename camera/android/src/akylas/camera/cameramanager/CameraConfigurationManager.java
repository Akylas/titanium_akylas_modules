/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akylas.camera.cameramanager;

import akylas.camera.AkylasCameraModule;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
//import android.view.Display;
//import android.view.WindowManager;

//import java.util.ArrayList;
import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
final class CameraConfigurationManager {

	private static final String TAG = "CamConfManager";

	private static final int TEN_DESIRED_ZOOM = 27;
	
	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.

	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

//	private final Context context;
	private Point screenResolution;
	private Size cameraResolution;
//	private int previewFormat;
//	private String previewFormatString;
//	private final ExposureInterface exposure;
	private int quality = AkylasCameraModule.QUALITY_MEDIUM;

	CameraConfigurationManager(Context context) {
//		this.context = context;
//		exposure = new ExposureManager().build();
	}
	
//	@SuppressWarnings("deprecation")
//	private static Point getDisplaySize(final Display display) {
//		final Point point = new Point();
//		try {
//			display.getSize(point);
//		} catch (java.lang.NoSuchMethodError ignore) { // Older device
//			point.x = display.getWidth();
//			point.y = display.getHeight();
//		}
//		return point;
//	}
	
	public void setQuality(int newQual){
		quality = newQual;
	}
	
	public int getQuality()
	{
			return quality;
	}
	
//	private int maxPixels(){
//		switch (quality) {
//		case AkylasCameraModule.QUALITY_HIGH:
//			return 1280 * 720;
//		case AkylasCameraModule.QUALITY_LOW:
//			return 320 * 240;
//		case AkylasCameraModule.QUALITY_MEDIUM:
//		default:
//			return 640 * 480;
//		}
//	}
//	
//	private int minPixels(){
//		switch (quality) {
//		case AkylasCameraModule.QUALITY_HIGH:
//		case AkylasCameraModule.QUALITY_LOW:
//		case AkylasCameraModule.QUALITY_MEDIUM:
//		default:
//			return 320 * 240;
//		}
//	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
//	void initFromCameraParameters(Camera camera) {
//		Camera.Parameters parameters = camera.getParameters();
//		previewFormat = parameters.getPreviewFormat();
//		previewFormatString = parameters.get("preview-format");
////		Log.d(TAG, "Default preview format: " + previewFormat + '/'
////				+ previewFormatString);
//		WindowManager manager = (WindowManager) context
//				.getSystemService(Context.WINDOW_SERVICE);
////		Display display = manager.getDefaultDisplay();
//		screenResolution = getDisplaySize(manager.getDefaultDisplay());
////		Log.d(TAG, "Screen resolution: " + screenResolution);
//		cameraResolution = getCameraResolution(parameters, screenResolution);
////		Log.d(TAG, "Camera resolution: " + screenResolution);
//	}


	
	public void setCameraImageRotation(Camera camera, int angle)
	{
		Camera.Parameters parameters = camera.getParameters();
		parameters.setRotation(CameraManager.get().getCurrentImageRotation());
		camera.setParameters(parameters);
	}
	
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

	public void updatePreviewSize(Camera camera, int width, int height) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters == null) return;
		List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
		cameraResolution = getOptimalPreviewSize(supportedPreviewSizes, width, height);
		
//		setDesiredCameraParameters(camera);
	}

	Camera.Size getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

//	int getPreviewFormat() {
//		return previewFormat;
//	}
//
//	String getPreviewFormatString() {
//		return previewFormatString;
//	}

//	private Point getCameraResolution(Camera.Parameters parameters,
//			Point screenResolution) {
//
//		String previewSizeValueString = parameters.get("preview-size-values");
//		// saw this on Xperia
//		if (previewSizeValueString == null) {
//			previewSizeValueString = parameters.get("preview-size-value");
//		}
//
//		Point cameraResolution = null;
//
//		cameraResolution = findBestPreviewSizeValue(parameters,
//				screenResolution);
//
//		if (cameraResolution == null) {
//			// Ensure that the camera resolution is a multiple of 8, as the
//			// screen may not be.
//			cameraResolution = new Point((screenResolution.x >> 3) << 3,
//					(screenResolution.y >> 3) << 3);
//		}
//
//		return cameraResolution;
//	}

//	private Point findBestPreviewSizeValue(Camera.Parameters parameters,
//			Point screenResolution) {
//
//		// Sort by size, descending
//		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
//				parameters.getSupportedPreviewSizes());
//		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
//			@Override
//			public int compare(Camera.Size a, Camera.Size b) {
//				int aPixels = a.height * a.width;
//				int bPixels = b.height * b.width;
//				if (bPixels < aPixels) {
//					return -1;
//				}
//				if (bPixels > aPixels) {
//					return 1;
//				}
//				return 0;
//			}
//		});
//
////		if (Log.isLoggable(TAG, Log.INFO)) {
//			StringBuilder previewSizesString = new StringBuilder();
//			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
//				previewSizesString.append(supportedPreviewSize.width)
//						.append('x').append(supportedPreviewSize.height)
//						.append(' ');
//			}
//			Log.i(TAG, "Supported preview sizes: " + previewSizesString);
////		}
//
//		Point bestSize = null;
//		float screenAspectRatio = (float) screenResolution.x
//				/ (float) screenResolution.y;
//
//		float diff = (float) 0.2;
//		int MIN_PREVIEW_PIXELS = minPixels();
//		int MAX_PREVIEW_PIXELS = maxPixels();
//		for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
//			int realWidth = supportedPreviewSize.width;
//			int realHeight = supportedPreviewSize.height;
////			Log.i(TAG, "testing sizes: " + realWidth + ", " + realHeight);
//			int pixels = realWidth * realHeight;
////			Log.i(TAG, "pixels: " + pixels);
//			if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
//				continue;
//			}
//			boolean isCandidatePortrait = realHeight < realWidth;
////			Log.i(TAG, "isCandidatePortrait: " + isCandidatePortrait);
//			int maybeFlippedWidth = isCandidatePortrait ? realHeight
//					: realWidth;
//			int maybeFlippedHeight = isCandidatePortrait ? realWidth
//					: realHeight;
//			if (maybeFlippedWidth == screenResolution.x
//					&& maybeFlippedHeight == screenResolution.y) {
//				Point exactPoint = new Point(realWidth, realHeight);
//				Log.i(TAG, "Found preview size exactly matching screen size: "
//						+ exactPoint);
//				return exactPoint;
//			}
//			float aspectRatio = (float) maybeFlippedWidth
//					/ (float) maybeFlippedHeight;
////			Log.i(TAG, "aspectRatio: " + aspectRatio);
////			Log.i(TAG, "screenAspectRatio: " + screenAspectRatio);
//			float newDiff = Math.abs(aspectRatio - screenAspectRatio);
////			Log.i(TAG, "newDiff: " + newDiff);
//			if (newDiff < diff) {
//				bestSize = new Point(realWidth, realHeight);
//				diff = newDiff;
//				break;
//			}
//		}
//
//		if (bestSize == null) {
//			Camera.Size defaultSize = parameters.getPreviewSize();
//			bestSize = new Point(defaultSize.width, defaultSize.height);
//			Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
//		}
//		else
//			Log.i(TAG, "Found best approximate preview size: " + bestSize);
//		return bestSize;
//	}
//
//	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
//		final double ASPECT_TOLERANCE = 0.2;
//		double targetRatio = (double) w / h;
//		if (sizes == null)
//			return null;
//
//		Size optimalSize = null;
//		double minDiff = Double.MAX_VALUE;
//
//		int targetHeight = h;
//
//		// Try to find an size match aspect ratio and size
//		for (Size size : sizes) {
////			Log.d(TAG, "Checking size " + size.width + "w " + size.height + "h");
//			double ratio = (double) size.width / size.height;
//			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
//				continue;
//			if (Math.abs(size.height - targetHeight) < minDiff) {
//				optimalSize = size;
//				minDiff = Math.abs(size.height - targetHeight);
//			}
//		}
//
//		// Cannot find the one match the aspect ratio, ignore the
//		// requirement
//		if (optimalSize == null) {
//			minDiff = Double.MAX_VALUE;
//			for (Size size : sizes) {
//				if (Math.abs(size.height - targetHeight) < minDiff) {
//					optimalSize = size;
//					minDiff = Math.abs(size.height - targetHeight);
//				}
//			}
//		}
//		return optimalSize;
//	}

	private static int findBestMotZoomValue(CharSequence stringValues,
			int tenDesiredZoom) {
		int tenBestValue = 0;
		for (String stringValue : COMMA_PATTERN.split(stringValues)) {
			stringValue = stringValue.trim();
			double value;
			try {
				value = Double.parseDouble(stringValue);
			} catch (NumberFormatException nfe) {
				return tenDesiredZoom;
			}
			int tenValue = (int) (10.0 * value);
			if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom
					- tenBestValue)) {
				tenBestValue = tenValue;
			}
		}
		return tenBestValue;
	}
	
	



	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();
		doSetTorch(parameters, newSetting);
		camera.setParameters(parameters);
	}
	
	public void setFlashMode(Camera.Parameters parameters, String newSetting) {
	    List<String> supported = parameters.getSupportedFlashModes();
	    if (supported != null && supported.contains(newSetting)) {
            parameters.setFlashMode(newSetting);
        }
	}
	
	void setFlashMode(Camera camera, String newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        setFlashMode(parameters, newSetting);
        camera.setParameters(parameters);
    }
	
	public void setFocusMode(Camera.Parameters parameters, String newSetting) {
        List<String> supported = parameters.getSupportedFocusModes();
        if (supported != null && supported.contains(newSetting)) {
            parameters.setFocusMode(newSetting);
        }
    }
	void setFocusMode(Camera camera, String newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        setFocusMode(parameters, newSetting);
        camera.setParameters(parameters);
    }

	private void setZoom(Camera.Parameters parameters) {

		String zoomSupportedString = parameters.get("zoom-supported");
		if (zoomSupportedString != null
				&& !Boolean.parseBoolean(zoomSupportedString)) {
			return;
		}

		int tenDesiredZoom = TEN_DESIRED_ZOOM;

		String maxZoomString = parameters.get("max-zoom");
		if (maxZoomString != null) {
			try {
				int tenMaxZoom = (int) (10.0 * Double
						.parseDouble(maxZoomString));
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad max-zoom: " + maxZoomString);
			}
		}

		String takingPictureZoomMaxString = parameters
				.get("taking-picture-zoom-max");
		if (takingPictureZoomMaxString != null) {
			try {
				int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
				if (tenDesiredZoom > tenMaxZoom) {
					tenDesiredZoom = tenMaxZoom;
				}
			} catch (NumberFormatException nfe) {
				Log.w(TAG, "Bad taking-picture-zoom-max: "
						+ takingPictureZoomMaxString);
			}
		}

		String motZoomValuesString = parameters.get("mot-zoom-values");
		if (motZoomValuesString != null) {
			tenDesiredZoom = findBestMotZoomValue(motZoomValuesString,
					tenDesiredZoom);
		}

		String motZoomStepString = parameters.get("mot-zoom-step");
		if (motZoomStepString != null) {
			try {
				double motZoomStep = Double.parseDouble(motZoomStepString
						.trim());
				int tenZoomStep = (int) (10.0 * motZoomStep);
				if (tenZoomStep > 1) {
					tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
				}
			} catch (NumberFormatException nfe) {
				// continue
			}
		}

		// Set zoom. This helps encourage the user to pull back.
		// Some devices like the Behold have a zoom parameter
		if (maxZoomString != null || motZoomValuesString != null) {
			parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
		}

		// Most devices, like the Hero, appear to expose this zoom parameter.
		// It takes on values like "27" which appears to mean 2.7x zoom
		if (takingPictureZoomMaxString != null) {
			parameters.set("taking-picture-zoom", tenDesiredZoom);
		}
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {
//		Log.i(TAG, "Supported values: " + supportedValues);
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
//		Log.i(TAG, "Settable value: " + result);
		return result;
	}
	
	public boolean isTorchOn(Camera.Parameters parameters)
	{
		String flashmode = parameters.getFlashMode();
		Boolean result = !flashmode.equals(Parameters.FLASH_MODE_OFF);
//		Log.i(TAG, "current FlashMode is: " + flashmode + "," + Parameters.FLASH_MODE_OFF + "," + result);
		return (result);
	}

	public void doSetTorch(Camera.Parameters parameters, boolean newSetting) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_AUTO,
					Camera.Parameters.FLASH_MODE_ON,
					Camera.Parameters.FLASH_MODE_RED_EYE);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}

	}
}
