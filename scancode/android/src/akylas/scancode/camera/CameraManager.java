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

package akylas.scancode.camera;

import java.io.IOException;
import java.lang.reflect.Method;

//import akylas.scancode.constants.Id;
import android.app.Activity;
import android.content.Context;
//import android.graphics.PixelFormat;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
//import android.hardware.SensorManager;
//import android.hardware.Camera.CameraInfo;
//import android.hardware.Camera.Parameters;
//import android.os.Build;
import android.os.Handler;
import android.util.Log;
//import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;

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

//	static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
//	static {
//		int sdkInt;
//		try {
//			sdkInt = Integer.parseInt(Build.VERSION.SDK);
//		} catch (NumberFormatException nfe) {
//			// Just to be safe
//			sdkInt = 10000;
//		}
//		SDK_INT = sdkInt;
//	}

	static final int ONE_D_BAND_HEIGHT = 10;

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private int cameraId;
	// private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
	private Rect framingRect;
	public Rect cropRect = null;
	public Boolean centeredCropRect = true;

	public Boolean onlyOneDimension = false;
	public Boolean needsFlip = false;
	
	public int currentFlippedRotation;

	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean wasFlipped;
	private boolean ninetydegreesFromCamera;
	
	private boolean mirrored;
	// private SurfaceHolder previewDisplay;
	public boolean previewing;
	private Activity activity = null;
//	private final boolean useOneShotPreviewCallback;
	private int currentOrientation = 0;
	private int currentImageRotation = 0;
//	private OrientationEventListener myOrientationEventListener;

	private AutoFocusManager autoFocusManager;

	/**
	 * Preview frames are delivered here, which we pass on to the registered
	 * handler. Make sure to clear the handler so it will only receive one
	 * message.
	 */
	private final PreviewCallback previewCallback;

	private int currentPreviewWidth;

	private int currentPreviewHeight;

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

		// Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
		// so we use the older
		// Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
		// we need to use
		// the more efficient one shot callback, as the older one can swamp the
		// system and cause it
		// to run out of memory. We can't use SDK_INT because it was introduced
		// in the Donut SDK.
//		useOneShotPreviewCallback = SDK_INT > Build.VERSION_CODES.CUPCAKE;
//		Log.d(TAG, "useOneShotPreviewCallback " + useOneShotPreviewCallback);
		previewCallback = new PreviewCallback(configManager);
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
			Log.d(TAG, "opening camera");
			camera = openCamera(cameraPosition);
			if (camera == null) {
				Log.d(TAG, "cant open camera");
				return;
			}
			Log.d(TAG, "camera opened");
			try {
				Log.d(TAG, "camera setting rpeview display");
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				Log.d(TAG, "cannot set preview display");
			}
			Log.d(TAG, "preview display done");
			Log.d(TAG, "setting cameraDisplay orientation");
			updateCameraDisplayOrientation();
			Log.d(TAG, "cameraDisplay orientation done");
			if (!initialized) {
				initialized = true;
				Log.d(TAG, "cameraDisplay orientation done");
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
			framingRect = null;
			framingRectInPreview = null;
			
			
		}
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

	    int resultInterface;
		int resultCamera;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			resultInterface = (info.orientation + degrees) % 360;
			resultCamera = resultInterface = (360 - resultInterface) % 360; // compensate the mirror
			mirrored = true;
		} else { // back-facing
			resultInterface = resultCamera = (info.orientation - degrees + 360) % 360;
			mirrored = false;
		}
		ninetydegreesFromCamera = ((resultCamera % 180) != 0);

		Log.d(TAG, "setCameraDisplayOrientation: " + info.orientation + "," + degrees + "," + resultInterface + "," + resultCamera);
		currentImageRotation = resultCamera + 180;
		Log.d(TAG, "setDisplayOrientation:" + resultInterface);
	     setDisplayOrientation(camera, resultInterface);
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
//			Log.d(TAG, "startPreview");
			camera.startPreview();
			previewing = true;
			autoFocusManager = new AutoFocusManager(context, camera);
		}
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
//			Log.d(TAG, "stopPreview");
			camera.stopPreview();
//			previewCallback.setHandler(null, 0);
			previewing = false;
		}
	}
	
	public Boolean IsPreviewing() {
		return (camera != null && previewing);
	}

	public synchronized void setTorch(boolean newSetting) {
		if (camera != null) {
			boolean isActive = autoFocusManager.isActive();
			if (autoFocusManager != null && isActive) {
				autoFocusManager.stop();
			}
			configManager.setTorch(camera, newSetting);
			if (autoFocusManager != null && isActive) {
				autoFocusManager.start();
			}
		}
	}

	public Boolean getTorch() {
		if (camera != null) {
			return configManager.isTorchOn(camera.getParameters());
		}
		return false;
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data
	 * will arrive as byte[] in the message.obj field, with width and height
	 * encoded as message.arg1 and message.arg2, respectively.
	 * 
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public void requestPreviewFrame(Handler handler, int message) {
//		Log.d(TAG, "requestPreviewFrame");
		Camera theCamera = camera;
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
		else
		{
			Log.d(TAG, "requestPreviewFrame not ok!");
		}
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

	/**
	 * Calculates the framing rect which the UI should draw to show the user
	 * where to place the barcode. This target helps with alignment as well as
	 * forces the user to hold the device far enough away to ensure the image
	 * will be in focus.
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public Rect computeFramingRect() {
		Point screenResolution = configManager.getScreenResolution();
//		Log.d(TAG, "screenResolution: " + screenResolution);
		if (cropRect == null) {
			if (camera == null) {
				Log.d(TAG, "cant get framingRect as camera is null");
				return null;
			}
			int width = screenResolution.x * 3 / 4;
			if (width < MIN_FRAME_WIDTH) {
				width = MIN_FRAME_WIDTH;
			} else if (width > MAX_FRAME_WIDTH) {
				width = MAX_FRAME_WIDTH;
			}
			int height = screenResolution.y * 3 / 4;
			if (height < MIN_FRAME_HEIGHT) {
				height = MIN_FRAME_HEIGHT;
			} else if (height > MAX_FRAME_HEIGHT) {
				height = MAX_FRAME_HEIGHT;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (screenResolution.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
					topOffset + height);
		} else {
//			Log.d(TAG, "cropRect: " + cropRect);
			framingRect = new Rect (cropRect);
			if (centeredCropRect) {
				int width = framingRect.width();
				int height = framingRect.height();
				framingRect.left = (screenResolution.x - width) / 2;
				framingRect.top = (screenResolution.y - height) / 2;
				framingRect.right = framingRect.left + width;
				framingRect.bottom = framingRect.top + height;
			}
		}
				
		if (onlyOneDimension) {
//			switch (currentOrientation) {
//			case Surface.ROTATION_0:
//			case Surface.ROTATION_180:
				framingRect.top += framingRect.height() / 2 - ONE_D_BAND_HEIGHT
						/ 2 + 1;
				framingRect.bottom = framingRect.top + ONE_D_BAND_HEIGHT;
//				break;
//			case Surface.ROTATION_90:
//			case Surface.ROTATION_270:
//				framingRect.left += framingRect.width() / 2 - ONE_D_BAND_HEIGHT
//						/ 2 + 1;
//				framingRect.right = framingRect.left + ONE_D_BAND_HEIGHT;
//				break;
//			}
		}
		

//		Log.d(TAG, "Calculated framing rect: " + framingRect);
		return framingRect;
	}

	public Rect getFramingRect() {
		return framingRect;
	}
	
	public Boolean isMirrored() {
		return mirrored;
	}
	
	public Boolean wasFlipped() {
		return wasFlipped;
	}

	public Rect getCropRect() {
		return cropRect;
	}

	public int getCurrentImageRotation() {
		return currentImageRotation;
	}

	/**
	 * Like {@link #computeFramingRect} but coordinates are in terms of the preview
	 * frame, not UI / screen.
	 */
	public Rect computeFramingRectInPreview() {
		// if (framingRectInPreview == null) {
		Rect fRect = computeFramingRect();
		if (fRect == null)
			return null;
		Rect rect = new Rect(fRect);
		
		
		
		Point cameraResolution = configManager.getCameraResolution();
		Point screenResolution = configManager.getScreenResolution();
//		Log.d(TAG, "cameraResolution: " + cameraResolution);
//		Log.d(TAG, "screenResolution: " + screenResolution);
		
		float scaleX, scaleY;
//		Log.d(TAG, "framingRectInPreview before rotation: " + rect);
		int rotation = currentImageRotation;
		rect = rotateRect(rect, screenResolution, rotation);
//		Log.d(TAG, "framingRectInPreview after rotation(" + rotation + "):" + rect);
		if (ninetydegreesFromCamera)
		{
			scaleX = (float)cameraResolution.x / (float)screenResolution.y;
			scaleY = (float)cameraResolution.y / (float)screenResolution.x;
		}
		else
		{
			scaleX = (float)cameraResolution.x / (float)screenResolution.x;
			scaleY = (float)cameraResolution.y / (float)screenResolution.y;
		}
		
		rect.left = (int) (rect.left * scaleX);
		rect.right = (int) (rect.right * scaleX);
		rect.top = (int) (rect.top * scaleY);
		rect.bottom = (int) (rect.bottom * scaleY);
//		Log.d(TAG, "framingRectInPreview scale: " + scaleX + "," + scaleY);
		
		rect.intersect(0,0,cameraResolution.x, cameraResolution.y);
		
		if (mirrored)
		{
//			Log.d(TAG, "framingRectInPreview before mirroring " + rect);
			Rect newRect = new Rect(rect);
			newRect.right = cameraResolution.x - rect.left;
			newRect.left = cameraResolution.x - rect.right;
			rect  = newRect;
		}
		
		framingRectInPreview = rect;

//		Log.d(TAG, "Calculated framingRectInPreview: " + framingRectInPreview);
		return framingRectInPreview;
	}

	public Rect getFramingRectInPreview() {
		return framingRectInPreview;
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

	private Rect rotateRect(Rect rect,  Point size, float angle)
	{
	    
	    Rect newRect =  new Rect();
	    int modAngle = ((int)angle % 360);
	    switch(modAngle) {
	        case 270: //-90
	            newRect.left = rect.top;
	            newRect.top = size.x - rect.left - rect.width();
	            newRect.right = newRect.left + rect.height() - 1;
	            newRect.bottom = newRect.top + rect.width();
	            break;
	        case 180: //180
	            newRect.left = size.x - rect.left - rect.width();
	            newRect.top = size.y - rect.top - rect.height();
	            newRect.right = newRect.left + rect.width();
	            newRect.bottom = newRect.top + rect.height();
	            break;
	        case 90: // 90
	            newRect.left = size.y - rect.top - rect.height();
	            newRect.top = rect.left;
	            newRect.right = newRect.left + rect.height();
	            newRect.bottom = newRect.top + rect.width();
	            break;
	        case 0:
	        {
	            newRect = rect;
	            break;
	        }
	    }
	    return newRect;
	}
	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
			int width, int height) throws IllegalArgumentException
	{
		Rect rect = computeFramingRectInPreview();
		if (rect == null) {
			rect = new Rect(0,0,width,height);
		}
		Point cameraResolution = new Point(width, height);
		
//		Log.d(TAG, "test : " + ninetydegreesFromCamera + "," + needsFlip);
    	wasFlipped = false;
	    if (ninetydegreesFromCamera && (needsFlip || onlyOneDimension))
		{
	    	wasFlipped = true;
//			Log.d(TAG, "needs rotation: " + currentImageRotation);
//			int rotation = (360 - currentImageRotation) % 360;
			if (mirrored)
				currentFlippedRotation = (currentImageRotation == 90)?0:180;
			else
				currentFlippedRotation = (currentImageRotation == 90)?180:0;

			rect = rotateRect(rect, cameraResolution, 90);
			rect.intersect(0,0,height, width);
//			Log.d(TAG, "rotation new rect: " + rect);
//			if (currentImageRotation == 270)
//			{
//				currentFlippedRotation = 180;
//				Log.d(TAG, "algo1: ");
				byte[] rotatedData = new byte[data.length];
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++)
						rotatedData[x * height + height - y - 1] = data[x + y * width];
				}
			    int tmp = width; // Here we are swapping, that's the difference to #11
			    width = height;
			    height = tmp;
			    data = rotatedData;
//			}
//			else
//			{
//				currentFlippedRotation = 0;
//				//NOT WORKING!
//				// HACK: the frame sent by the camera is rotated, we need to rotate it before we continue.
//				Log.d(TAG, "algo2: ");
//				byte[] rotatedData = new byte[data.length];
//				for (int y = 0; y < height; y++) {
//					for (int x = 0; x < width; x++)
//						rotatedData[x * height + height - y - 1] = data[x + y * width];
//				}
//			    int tmp = width; // Here we are swapping, that's the difference to #11
//			    width = height;
//			    height = tmp;
//			    data = rotatedData;
//			}
		}
	    else
	    {
	    	currentFlippedRotation = currentImageRotation;
	    }

		// Go ahead and assume it's YUV rather than die.
		return new PlanarYUVLuminanceSource(data, width, height, rect.left,
				rect.top, rect.width(), rect.height(), false);
	}
	/**
	 * Converts the result points from still resolution coordinates to screen
	 * coordinates.
	 * 
	 * @param points
	 *            The points returned by the Reader subclass through
	 *            Result.getResultPoints().
	 * @return An array of Points scaled to the size of the framing rect and
	 *         offset appropriately so they can be drawn in screen coordinates.
	 */
	/*
	 * public Point[] convertResultPoints(ResultPoint[] points) { Rect frame =
	 * getFramingRectInPreview(); int count = points.length; Point[] output =
	 * new Point[count]; for (int x = 0; x < count; x++) { output[x] = new
	 * Point(); output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
	 * output[x].y = frame.top + (int) (points[x].getY() + 0.5f); } return
	 * output; }
	 */
	  public static Bitmap cropBitmap(Bitmap bitmap,Rect rect){
	    int w=rect.right-rect.left;
	    int h=rect.bottom-rect.top;
	    Bitmap ret=Bitmap.createBitmap(w, h, bitmap.getConfig());
	    Canvas canvas=new Canvas(ret);
	    canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
	    return ret;
	  }
	  
	
	public RGBLuminanceSource buildRGBLuminanceSource(Bitmap imageBitmap,
			Rect crop) {
//		Log.d(TAG, "buildLuminanceSource: " + imageBitmap.getWidth() + "x" + imageBitmap.getHeight() + " " + crop );
//		Bitmap bmpCropped = cropBitmap(imageBitmap, crop);
		int width = crop.width();
	    int height = crop.height();
	    int[] pixels = new int[width * height];
	    imageBitmap.getPixels(pixels, 0, imageBitmap.getWidth(), crop.left, crop.top, width, height);

	    return new RGBLuminanceSource(width, height, pixels);
	}

	/**
	 * A factory method to build the appropriate LuminanceSource object based on
	 * the format of the preview buffers, as described by Camera.Parameters.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	// public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
	// int width, int height) {
	// Rect rect = computeFramingRectInPreview();
	// // Log.d(TAG, "buildLuminanceSource: " + width + "x" + height + " " +
	// // rect );
	//
	// int previewFormat = configManager.getPreviewFormat();
	// String previewFormatString = configManager.getPreviewFormatString();
	// switch (previewFormat) {
	// // This is the standard Android format which all devices are REQUIRED to
	// // support.
	// // In theory, it's the only one we should ever care about.
	// case PixelFormat.YCbCr_420_SP:
	// // This format has never been seen in the wild, but is compatible as
	// // we only care
	// // about the Y channel, so allow it.
	// case PixelFormat.YCbCr_422_SP:
	// return new PlanarYUVLuminanceSource(data, width, height, rect.left,
	// rect.top, rect.width(), rect.height());
	// default:
	// // The Samsung Moment incorrectly uses this variant instead of the
	// // 'sp' version.
	// // Fortunately, it too has all the Y data up front, so we can read
	// // it.
	// if ("yuv420p".equals(previewFormatString)) {
	// return new PlanarYUVLuminanceSource(data, width, height,
	// rect.left, rect.top, rect.width(), rect.height());
	// }
	// }
	// throw new IllegalArgumentException("Unsupported picture format: "
	// + previewFormat + '/' + previewFormatString);
	// }

}
