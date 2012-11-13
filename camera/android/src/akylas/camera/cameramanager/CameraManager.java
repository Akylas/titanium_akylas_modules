/*
 * Copyright (C) 2008 ZXing authors
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

import java.io.IOException;
import java.lang.reflect.Method;

import org.appcelerator.kroll.annotations.Kroll;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;
	private static final int MAX_FRAME_WIDTH = 600;
	private static final int MAX_FRAME_HEIGHT = 400;

	private static CameraManager cameraManager;
	
	static final int ONE_D_BAND_HEIGHT = 10;

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private int cameraId;
	// private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

	public Boolean needsFlip = false;
	private Boolean ninetydegreesFromCamera = false;
	
	public int currentFlippedRotation;

	private boolean initialized;
	private boolean wasFlipped;
	
	private boolean mirrored;
	// private SurfaceHolder previewDisplay;
	public boolean previewing;
	private Activity activity = null;
//	private final boolean useOneShotPreviewCallback;
	private int currentOrientation = -1000;
	private int currentImageRotation = 0;
//	private OrientationEventListener myOrientationEventListener;

	private AutoFocusManager autoFocusManager;

	private int currentPreviewWidth;

	private int currentPreviewHeight;
	private Boolean torch = false;

	/**
	 * Initializes this static object with the Context of the calling Activity.
	 * 
	 * @param context
	 *            The Activity which wants to use the camera.
	 */
	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	/**
	 * Gets the CameraManager singleton instance.
	 * 
	 * @return A reference to the CameraManager singleton.
	 */
	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
		currentOrientation = this.activity.getWindowManager().getDefaultDisplay()
		.getRotation();
//		myOrientationEventListener
//		   = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL){
//
//		    @Override
//		    public void onOrientationChanged(int orientation) {
//				Log.d(TAG, "onOrientationChanged " + orientation);
//		    	
////			     mParameters.setRotation(rotation);
//		    };
//		};
//		if (myOrientationEventListener.canDetectOrientation()){
//			myOrientationEventListener.enable();
//		}
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 * 
	 * @param holder
	 *            The surface object which the camera will draw preview frames
	 *            into.
	 * @throws IOException
	 *             Indicates the camera driver failed to open.
	 */
	public void openDriver(SurfaceHolder holder, int cameraPosition)
	{
		if (camera == null) {
			camera = openCamera(cameraPosition);
			if (camera == null) {
				return;
			}
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.d(TAG, "cannot set preview display");
			}
			updateCameraDisplayOrientation();
			if (!initialized) {
				initialized = true;
				configManager.initFromCameraParameters(camera);
			}
			configManager.setDesiredCameraParameters(camera);
			
		}
	}

	public void updatePreviewSize(int width, int height) {
		Log.d(TAG, "updatePreviewSize " + width + "," + height);
		currentPreviewWidth = width;
		currentPreviewHeight = height;
		updatePreviewSize();
	}
	
	public void updatePreviewSize() {
		if (camera != null) {
			configManager.updatePreviewSize(camera, currentPreviewWidth, currentPreviewHeight);
		}
	}


	/**
	 * Closes the camera driver if still in use.
	 */
	public void closeDriver() {
		if (camera != null) {
			camera.release();
			camera = null;
			initialized = false;
		}
	}

	public synchronized boolean isOpen() {
		return camera != null;
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public void startPreview() {

		if (camera != null && !previewing) {
			Log.d(TAG, "startPreview");
			camera.startPreview();
			previewing = true;
			if (torch)
			{
				Log.d(TAG, "we need to activate torch " );
				setTorch(torch);
			}
			autoFocusManager = new AutoFocusManager(context, camera);
		}
	}
	
	public Boolean IsPreviewing() {
		return (camera != null && previewing);
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public void stopPreview() {
		if (autoFocusManager != null) {
			autoFocusManager.stop();
			autoFocusManager = null;
		}
		if (camera != null && previewing) {
			Log.d(TAG, "stopPreview");
			camera.stopPreview();
			previewing = false;
		}
	}

	public synchronized void setTorch(boolean newSetting) {
		Log.d(TAG, "set torch " + newSetting);
		torch = newSetting;
		if (camera != null) {
			boolean isActive =(autoFocusManager != null && autoFocusManager.isActive());
			if ( isActive) {
				autoFocusManager.stop();
			}
			configManager.setTorch(camera, newSetting);
			if (isActive) {
				autoFocusManager.start();
			}
		}
	}

	public Boolean getTorch() {
		if (camera != null) {
			return configManager.isTorchOn(camera.getParameters());
		}
		return torch;
	}


	/**
	 * Asks the camera hardware to perform one focus.
	 * 
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestFocus() {
		if (camera != null && previewing) {
			autoFocusManager.stop();
			Log.d(TAG, "Requesting  one focus");
			camera.autoFocus(null);
		}
	}

	/**
	 * Asks the camera hardware to perform an autofocus.
	 * 
	 * @param handler
	 *            The Handler to notify when the autofocus completes.
	 * @param message
	 *            The message to deliver.
	 */
	public void requestAutoFocus(Handler handler, int message) {
		if (camera != null && previewing) {
			autoFocusManager.start();
		}
	}
	
	public Boolean isMirrored() {
		return mirrored;
	}
	
	public Boolean wasFlipped() {
		return wasFlipped;
	}

	public int getCurrentImageRotation() {
		return currentImageRotation;
	}

	private Camera openCamera(int facing) {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == facing) {
				try {
					cam = Camera.open(camIdx);
					cameraId = camIdx;
					return cam;
				} catch (RuntimeException e) {
					Log.e(TAG,
							"Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}
		return null;
	}
	
	public boolean needCameraDisplayChange()
	{
		if (camera == null || this.activity == null) {
			return false;
		}
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int orientation = this.activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		return orientation != currentOrientation;
	}
	
	protected void setDisplayOrientation(Camera camera, int angle){
	    Method downPolymorphic;
	    try
	    {
	        downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
	        if (downPolymorphic != null)
	            downPolymorphic.invoke(camera, new Object[] { angle });
	    }
	    catch (Exception e1)
	    {
	    }
	}
	
	public void setCameraDisplayOrientation(Activity activity,
	         int cameraId, Camera camera) {
	     CameraInfo info = new CameraInfo();
	     Camera.getCameraInfo(cameraId, info);
	     currentOrientation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (currentOrientation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
		Log.d(TAG, "setDisplayOrientation:" + result);
	     setDisplayOrientation(camera, result);
	 }

	public void updateCameraDisplayOrientation() {
		if (camera == null || this.activity == null) {
			return;
		}
		Log.d(TAG, "updateCameraDisplayOrientation:");
		Boolean waspreviewing = previewing;
		if (waspreviewing) 
			stopPreview();
		setCameraDisplayOrientation(this.activity, cameraId, camera);
		if (waspreviewing) 
			startPreview();
	}
	
	public void setQuality(int value)
	{
//	    Log.d(LCAT, "setTorch3 to: " + value);
		configManager.setQuality(value);
		updatePreviewSize();
	}
	
	public int getQuality()
	{
//		if (camera != null) {
			return configManager.getQuality();
//		}
	}
}
