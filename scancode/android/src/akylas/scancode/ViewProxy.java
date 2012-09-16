/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package akylas.scancode;

import java.io.IOException;
import java.util.Vector;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBaseActivity.ConfigurationChangedListener;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
//import org.appcelerator.titanium.TiPoint;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import akylas.scancode.camera.CameraManager;
//import akylas.scancode.constants.BarcodeColor;
import akylas.scancode.constants.Id;
import akylas.scancode.constants.MessageId;
import akylas.scancode.zxing.CaptureActivityHandler;
//import akylas.scancode.zxing.ViewfinderView;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;


// This proxy can be created by calling AkylasScancodeAndroid.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule=AkylasScancodeModule.class)
public class ViewProxy extends TiViewProxy implements OnLifecycleEvent, SurfaceHolder.Callback, ConfigurationChangedListener
{
	@SuppressWarnings("unused")
	private static final String TAG = "CaptureActivity";



//	private enum Source {
//		NATIVE_APP_INTENT, NONE
//	}
//	
	/**
	 * @return current viewfinderView
	 */

//	public ViewfinderView getViewfinderView() {
//		return viewfinderView;
//	}

	/**
	 * @return current handler
	 */
	public Handler getHandler() {
		return mHandler;
	}

	private CaptureActivityHandler mHandler;

//	private ViewfinderView viewfinderView;
	private int cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
//	private Result lastResult;
	private boolean hasSurface;
//	private Source source;
	private Vector<BarcodeFormat> decodeFormats;
	private Object[] stringReaders;
	private String characterSet;
//	private CaptureView captureView;
	// Standard Debugging variables
	private static final String LCAT = "AkylasScancodeProxy";
//	private static final boolean DBG = TiConfig.LOGD;

	private class CameraView extends TiUIView
	{
		@SuppressWarnings("unused")
		private static final String TAG = "AkylasScanCodeView";
		private SurfaceView preview;
		private SurfaceHolder previewHolder;
//		private TiCompositeLayout overlayLayout;
		private TiCompositeLayout previewLayout;
		
//		private TiCompositeLayout overlayLayout;

		@SuppressWarnings("deprecation")
		public CameraView(TiViewProxy proxy)
		{
			super(proxy);

			preview = new SurfaceView(proxy.getActivity());

			previewHolder = preview.getHolder();

			// this call is deprecated but we still need it for SDK level 7 otherwise kaboom
			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			previewLayout = new TiCompositeLayout(proxy.getActivity(), proxy);
//			TiCompositeLayout.LayoutParams params = getLayoutParams();
//			params.autoFillsHeight = true;
//			params.autoFillsWidth = true;
			previewLayout.addView(preview);

//			overlayLayout = new TiCompositeLayout(proxy.getActivity(), proxy);
//			previewLayout.addView(overlayLayout);

			setNativeView(previewLayout);
		}
		
//		public SurfaceView getPreviewView()
//		{
//			return preview;
//		}
		
		public SurfaceHolder getPreviewSurfaceHolder()
		{
			return previewHolder;
		}

//		@Override
//		public void add(TiUIView overlayItem)
//		{
//			if (overlayItem != null) {
//				View overlayItemView = overlayItem.getNativeView();
//				if (overlayItemView != null) {
//					previewLayout.addView(overlayItemView, overlayItem.getLayoutParams());
//				}
//			}
//		}
//		
//		@Override
//		public void remove(TiUIView overlayItem)
//		{
//			if (overlayItem != null) {
//				View overlayItemView = overlayItem.getNativeView();
//				if (overlayItemView != null) {
//					overlayLayout.removeView(overlayItemView);
//				}
//			}
//		}

		@Override
		public void processProperties(KrollDict d)
		{
			super.processProperties(d);
		}
	}
	
	public ViewProxy()
	{
		super();
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] creating proxy ");
	}

	public ViewProxy(TiContext tiContext)
	{
		this();
		CameraManager.init(tiContext.getActivity().getApplication());
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] creating proxy from context");
	}
	
	@Override
	public void setActivity(Activity activity)
	{
		super.setActivity(activity);
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] set activity");
		
		CameraManager.get().setActivity(activity);
		
//		TiBaseActivity.registerOrientationListener (new TiBaseActivity.OrientationChangedListener()
//		{
//			@Override
//			public void onOrientationChanged (int configOrientationMode)
//			{
//				CameraManager.get().stopPreview();
//				CameraManager.get().setCameraDisplayOrientation();
//				CameraManager.get().startPreview();
//				if (mHandler != null) {
//					mHandler.sendEmptyMessage(Id.RESTART_PREVIEW);
//				}
//			}
//		});
	}

	@Override
	public TiUIView createView(Activity activity)
	{
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT]creating view" );
		TiUIView view = new CameraView(this);
		((TiBaseActivity) activity).addOnLifecycleEventListener(this);
		((TiBaseActivity) activity).addConfigurationChangedListener(this);
//		view.getLayoutParams().autoFillsHeight = true;
//		view.getLayoutParams().autoFillsWidth = true;
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options)
	{
		super.handleCreationDict(options);
		
		if (options.containsKey("message")) {
//			Log.d(LCAT, "scancodeproxy created with message: " + options.get("message"));
		}
		
		if (options.containsKey("readers")) {
			setReaders((Object[]) options.get("readers"));
//			Log.d(LCAT, "scancodeproxy created with readers: " + options.get("readers"));
		}
		
		if (options.containsKey("cropRect")) {
			setCropRect((KrollDict) options.get("cropRect"));
//			Log.d(LCAT, "scancodeproxy created with cropRect: " + options.get("cropRect"));
		}
		
		if (options.containsKey("centeredCropRect")) {
			setCenteredCropRect( (Boolean) options.get("centeredCropRect"));
//			Log.d(LCAT, "scancodeproxy created with centeredCropRect: " + options.get("centeredCropRect"));
		}
		
		if (options.containsKey("cameraPosition")) {
			setCameraPosition( options.get("cameraPosition"));
//			Log.d(LCAT, "scancodeproxy created with setCameraPosition: " + options.get("cameraPosition"));
		}
		
		if (options.containsKey("onlyOneDimension")) {
			setOnlyOneDimension( (Boolean) options.get("onlyOneDimension"));
//			Log.d(LCAT, "scancodeproxy created with torch: " + options.get("onlyOneDimension"));
		}

		if (options.containsKey("torch")) {
			setTorch( (Boolean) options.get("torch"));
//			Log.d(LCAT, "scancodeproxy created with torch: " + options.get("torch"));
		}
	}
	
	@Override
	public void onStop(Activity activity) 
	{
		// This method is called when the root context is stopped 

//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] stop proxy with id " + getProxyId());
	}
	
	private void stopCapture(){
//		Log.d(LCAT, "stopCapture with camera " + cameraPosition);
		if (mHandler != null) {
			mHandler.quitSynchronously();
			mHandler = null;
		}
		CameraManager.get().closeDriver();
        fireEvent("stop", new KrollDict());
	}
	
	private void startCapture(){
//		Log.d(LCAT, "startCapture with camera " + cameraPosition);
//		SurfaceView surfaceView = ;
		SurfaceHolder surfaceHolder = ((CameraView)this.view).getPreviewSurfaceHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder, cameraPosition);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}
		
//		resetStatusView();
	}

	@Override
	public void onPause(Activity activity) 
	{
		stopCapture();
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] pause proxy with id " + getProxyId());
	}
	
	@Override
	public void onStart(Activity arg0) {
//	    Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] activity start");
		
	}

	@Override
	public void onResume(Activity activity) 
	{		
//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] resume proxy with id " + getProxyId());
		// This method is called when the root context is being resumed
		startCapture();
	}

	@Override
	public void onDestroy(Activity activity) 
	{
		// This method is called when the root context is being destroyed

//		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] destroy proxy with id " + getProxyId());
	}
	
	private int cameraPositionValue(Object value)
	{
		int result = CameraInfo.CAMERA_FACING_BACK;
		String sValue = TiConvert.toString(value);
		if (sValue != null)
		{
			if (value == "front")
				result = CameraInfo.CAMERA_FACING_FRONT;
			else if (value == "rear")
				result = CameraInfo.CAMERA_FACING_BACK;
		}
		else
		{
			int iValue = TiConvert.toInt(value);
			if (iValue ==CameraInfo.CAMERA_FACING_FRONT || iValue == CameraInfo.CAMERA_FACING_BACK)
				result = iValue;
		}
		return result;
	}
	
	private KrollDict KrollDictFromRect(Rect rect)
	{
		KrollDict d = new KrollDict();
	    d.put(TiC.PROPERTY_WIDTH, rect.width());
		d.put(TiC.PROPERTY_HEIGHT, rect.height());
		d.put(TiC.PROPERTY_X, rect.left);
		d.put(TiC.PROPERTY_Y, rect.top);
		return d;
	}
	
//	private KrollDict KrollDictFromResultPoint(ResultPoint point)
//	{
//		KrollDict d = new KrollDict();
//		d.put(TiC.PROPERTY_X, point.getX());
//		d.put(TiC.PROPERTY_Y, point.getY());
//		return d;
//	}
	
	private KrollDict KrollDictFromPoint(Point point)
	{
		KrollDict d = new KrollDict();
		d.put(TiC.PROPERTY_X, point.x);
		d.put(TiC.PROPERTY_Y, point.y);
		return d;
	}
	
	public Point rotatePointWithinRect(Point point, Rect rect, int angle) {
		Point center = new Point(rect.centerX(), rect.centerY());
		int x = point.x - center.x;
		int y = point.y - center.y;
		Point newPoint = new Point();
	    int modAngle = ((int)angle % 360);
		switch (modAngle) {
		case 0:
			newPoint.x = x + center.x;
			newPoint.y = y + center.y;
			break;
		case 90:
			newPoint.x = -y + center.y;
			newPoint.y = x + center.x;
			break;
		case 180:
			newPoint.x = -x + center.x;
			newPoint.y = -y + center.y;
			break;
		case 270:
			newPoint.x = y + center.y;
			newPoint.y = -x + center.x;
			break;
		}
		return newPoint;
	}
	
	public static Bitmap rotate(Bitmap b, int degrees) {
	    if (degrees != 0 && b != null) {
	        Matrix m = new Matrix();

	        m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
	        try {
	            Bitmap b2 = Bitmap.createBitmap(
	                    b, 0, 0, b.getWidth(), b.getHeight(), m, true);
	            if (b != b2) {
	                b.recycle();
	                b = b2;
	            }
	        } catch (OutOfMemoryError ex) {
	           throw ex;
	        }
	    }
	    return b;
	}
	// Public APIs (available in javascript)
	// The methods are exposed to javascript because of the @Kroll.method annotation	
	
	// Methods
	@Kroll.method
	public void stop()
	{
		Log.d(LCAT, "stop");
		stopCapture();
	}

	@Kroll.method
	public void start()
	{
		Log.d(LCAT, "start");
		startCapture();
	}
	
	@Kroll.method
	public void swapCamera()
	{
//		Log.d(LCAT, "swapCamera with camera " + cameraPosition);
		stopCapture();
//		String sPos;
		if (cameraPosition == CameraInfo.CAMERA_FACING_BACK)
		{
			cameraPosition = CameraInfo.CAMERA_FACING_FRONT;
//			sPos = "front";
		}
		else
		{
			cameraPosition = CameraInfo.CAMERA_FACING_BACK;
//			sPos = "rear";
		}
		startCapture();
//		KrollDict data = new KrollDict();
//        data.put("position", sPos);
//        fireEvent("camera", data);
	}
	
	@Kroll.method
	public void flush()
	{
		if (mHandler != null) {
			mHandler.sendEmptyMessage(Id.RESTART_PREVIEW);
		}
	}
	
	@Kroll.method
	public void focus()
	{
		if (mHandler != null) {
			mHandler.requestFocus();
		}
	}
	
	@Kroll.method
	public void autoFocus()
	{
		if (mHandler != null) {
			mHandler.requestAutoFocus();
		}
	}
	
	// Properties
	@Kroll.setProperty @Kroll.method
	public void setCameraPosition(Object value)
	{
		int pos = cameraPositionValue(value);
		if (cameraPosition == pos) return;
		stopCapture();
		cameraPosition = pos;
		startCapture();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setCropRect(KrollDict d)
	{
		if (d == null) {
			throw new IllegalArgumentException("setCropRect: rect must not be null");
		}
		if (!d.containsKey(TiC.PROPERTY_X) || 
				!d.containsKey(TiC.PROPERTY_Y) || 
				!d.containsKey(TiC.PROPERTY_WIDTH) || 
				!d.containsKey(TiC.PROPERTY_HEIGHT)) 
		{
			throw new IllegalArgumentException("setCropRect: required property \"x\",\"y\",\"width\",\"height\"");
		}
		int x = TiConvert.toInt(d, TiC.PROPERTY_X);
		int y = TiConvert.toInt(d, TiC.PROPERTY_Y);
		int width = TiConvert.toInt(d, TiC.PROPERTY_WIDTH);
		int height = TiConvert.toInt(d, TiC.PROPERTY_HEIGHT);
		
		Rect newCropRect = new Rect(x, y, x + width, y + height);
		CameraManager.get().cropRect = newCropRect;
	}
	
	@Kroll.getProperty @Kroll.method
	public KrollDict getCropRect()
	{
	    Rect rect = CameraManager.get().getCropRect();
	    return KrollDictFromRect(rect);
	}

	@Kroll.setProperty @Kroll.method
	public void setOnlyOneDimension(Boolean value)
	{
//	    Log.d(LCAT, "setOnlyOneDimension to: " + value);
	    CameraManager.get().onlyOneDimension = value;
	}
	
	@Kroll.getProperty @Kroll.method
	public Boolean getOnlyOneDimension()
	{
	    return CameraManager.get().onlyOneDimension;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setCenteredCropRect(Boolean value)
	{
//	    Log.d(LCAT, "setCenteredCropRect to: " + value);
	    CameraManager.get().centeredCropRect = value;
	}
	
	@Kroll.getProperty @Kroll.method
	public Boolean getCenteredCropRect()
	{
	    return CameraManager.get().centeredCropRect;
	}

	@Kroll.setProperty @Kroll.method
	public void setTorch(Boolean value)
	{
//	    Log.d(LCAT, "setTorch3 to: " + value);
	    CameraManager.get().setTorch(value);
	    KrollDict data = new KrollDict();
        data.put("on", CameraManager.get().getTorch());
        fireEvent("torch", data);
	}
	
	@Kroll.getProperty @Kroll.method
	public Boolean getTorch()
	{
	    return CameraManager.get().getTorch();
	}
	
	@Kroll.setProperty @Kroll.method
	public void setReaders(Object[] readers)
	{
//	    Log.d(LCAT, "setReaders to: " + readers);
		stringReaders = readers.clone();
		decodeFormats = new Vector<BarcodeFormat>();
        Boolean oneD = true;
        Boolean needsFlip = false;
        for(int i=0; i < readers.length; i++) {
	    
//		    Log.d(LCAT, "got Reader: " + readers[i]);
		    Vector<BarcodeFormat> vec = AkylasScancodeModule.STRING_TO_FORMAT.get(readers[i]);
		    for(int j=0; j < vec.size(); j++) {
		    	BarcodeFormat format = vec.get(j);
		    	if (!decodeFormats.contains(format))
		    	{
		    		if (format == BarcodeFormat.QR_CODE || format == BarcodeFormat.DATA_MATRIX || format == BarcodeFormat.AZTEC)
		    			oneD = false;
		    		else
		    			needsFlip = true;
		    		decodeFormats.add(format);
		    	}
		    }
	    }
	    CameraManager.get().onlyOneDimension = oneD;
	    CameraManager.get().needsFlip = needsFlip;
	    if (mHandler != null) {
			Message message = Message.obtain(mHandler,
					Id.READERS);
			Bundle bundle = new Bundle();
			bundle.putSerializable(MessageId.READERS,
					decodeFormats);
			message.setData(bundle);
			message.sendToTarget();
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public Object[] getReaders()
	{
	    return stringReaders;
	}
	
	
	private void initCamera(final SurfaceHolder surfaceHolder, int cameraPosition) {
//	    Log.d(LCAT, "initCamera");
		CameraManager.get().openDriver(surfaceHolder, cameraPosition);

		if (mHandler == null) {
//			boolean beginScanning = lastResult == null;
			mHandler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
        fireEvent("start", new KrollDict());
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 * @throws IOException 
	 */
	
//	private void saveImage(Bitmap image, String name) throws IOException
//	{
//		
//		String path = Environment.getExternalStorageDirectory().toString();
//		OutputStream fOut = null;
//		File file = new File(path, name+".jpg");
//		fOut = new FileOutputStream(file);
//
//		image.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
//		fOut.flush();
//		fOut.close();
//
//		MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
//	}
	
	private Bitmap rotateAndScaleBitmap(Bitmap src, float scaleWidth, float scaleHeight, int rotation)
	{
		int width = src.getWidth();
        int height = src.getHeight();
        
        // createa matrix for the manipulation
        Matrix matrix = new Matrix();
        
        if (scaleWidth < 0)
        {
        	matrix.preScale(-1, 1);
        	scaleWidth = -scaleWidth;
        }
        
        if (scaleHeight < 0)
        {
        	matrix.preScale(1, -1);
        	scaleHeight = -scaleHeight;
        }

        // rotate the Bitmap
        matrix.postRotate(rotation);
        
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);


        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(src, 0, 0,
                          width, height, matrix, false);
        return resizedBitmap;
	}

	public void handleDecode(Result rawResult, Bitmap barcode) {
//		long start = System.currentTimeMillis();
        KrollDict data = new KrollDict();
        
        int rotation = CameraManager.get().currentFlippedRotation;
        Boolean mirrored = CameraManager.get().isMirrored();
        if (!mirrored)
    		rotation = (360 - rotation) % 360;
        
//        Boolean flipped = CameraManager.get().wasFlipped();
        Rect cropRect = CameraManager.get().getFramingRect();
		Rect imageRect = new Rect(0, 0, barcode.getWidth(), barcode.getHeight());
//	    Log.d(LCAT, "imageRect" + imageRect);
//	    Log.d(LCAT, "cropRect" + cropRect);
//	    Log.d(LCAT, "mirrored" + mirrored);
//	    Log.d(LCAT, "flipped" + flipped);
//	    Log.d(LCAT, "rotation" + rotation);
        
        float scaleWidth, scaleHeight;
        if((rotation % 180) != 0)
        {
        	scaleWidth = ((float) cropRect.width()) / barcode.getHeight();
            scaleHeight = ((float) cropRect.height()) / barcode.getWidth();
        }
        else
        {
        	scaleWidth = ((float) cropRect.width()) / barcode.getWidth();
            scaleHeight = ((float) cropRect.height()) / barcode.getHeight();
        }
        
        if (mirrored)
        {
            if((rotation % 180) != 0)
            {
        		scaleHeight  = -scaleHeight;
            }
        	else
        	{
            	scaleWidth  = -scaleWidth;
        	}
        }
//	    Log.d(LCAT, "rotation" + rotation);
//        long end = System.currentTimeMillis();
//	    Log.d(TAG, "preparation test1 " + (end - start) + " ms");
	    
        Bitmap scaled = rotateAndScaleBitmap(barcode, scaleWidth, scaleHeight, rotation);
        
        //image is now rotated, lets put scales back to positive for points
        scaleWidth = Math.abs(scaleWidth);
        scaleHeight = Math.abs(scaleHeight);
        
//        end = System.currentTimeMillis();
//	    Log.d(TAG, "preparation test2 " + (end - start) + " ms");
        data.put("cropRect", KrollDictFromRect(cropRect));
        data.put("message", rawResult.toString());
        data.put("image", TiBlob.blobFromImage(scaled));
        
//        end = System.currentTimeMillis();
		
		ResultPoint[] points = rawResult.getResultPoints();
		
		
//		Rect scaledImageRect = new Rect(0, 0, scaled.getWidth(), scaled.getHeight());
//	    Log.d(LCAT, "scaledImageRect" + scaledImageRect);
		if (points != null && points.length > 0) {
			KrollDict[] tipoints = new KrollDict[points.length];
			for (int i = 0; i < points.length; i++) {
				Point pt = new Point((int)(points[i].getX()), (int)(points[i].getY()));
//			    Log.d(LCAT, "point " + pt);
			    
		        if (mirrored)
		        {
		            if((rotation % 180) != 0)
		            	pt.y = imageRect.height() - pt.y;
		            else
		            	pt.x = imageRect.width() - pt.x;
		        }
			    if (rotation != 0)
			    {	
					pt = rotatePointWithinRect(pt, imageRect, rotation);
			    }
			    pt.x  *= scaleWidth;
			    pt.y  *= scaleHeight;
				tipoints[i] = KrollDictFromPoint(pt);
			}
	        data.put("points",tipoints);
		}
//		end = System.currentTimeMillis();
//	    Log.d(TAG, "preparation took " + (end - start) + " ms");
        fireEvent("scan", data);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int format, int width, int height) {
//	    Log.d(LCAT, "surfaceChanged: ");
		//surface has changed we need to update camera preview size
		CameraManager.get().updatePreviewSize(width, height);
		
		
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder, cameraPosition);
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		hasSurface = false;
	}

	@Override
	public void onConfigurationChanged(TiBaseActivity arg0, Configuration arg1) {
		Boolean waspreviewing = CameraManager.get().previewing;
		if (waspreviewing) CameraManager.get().stopPreview();
		CameraManager.get().setCameraDisplayOrientation();
		if (waspreviewing) CameraManager.get().startPreview();
		if (mHandler != null) {
			mHandler.sendEmptyMessage(Id.CAMERA_ORIENTATION);
		}
	}
}