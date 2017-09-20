package akylas.opencv;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private static final String TAG = "CameraPreview";

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private Handler mHandler;

	public CameraPreview(Context context, Handler handler) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHandler = handler;
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceChanged");
		if (mCamera != null) {
			mCamera.stopPreview();
			Camera.Parameters params = mCamera.getParameters();
			params.setRotation(90);
			
			mFrameWidth = height;
			mFrameHeight = width;
		 
		    List<Size> sizes = params.getSupportedPreviewSizes();
		    int tmpHeight = 0;
		    int tmpWidth = 0;
		    for (Size size : sizes) {
		        if ((size.width > mFrameWidth) || (size.height > mFrameHeight)) {
		            continue;
		        }
		        if (tmpHeight < size.height) {
		            tmpWidth = size.width;
		            tmpHeight = size.height;
		        }
		    }
		    mFrameWidth = tmpWidth;
		    mFrameHeight = tmpHeight;
		 
		    params.setPreviewSize(mFrameWidth, mFrameHeight);
		 
		    // Adjust SurfaceView size
		    ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
	        layoutParams.height = mFrameWidth;
	        layoutParams.width = mFrameHeight;
		    this.setLayoutParams(layoutParams);
		 
		    mCamera.setParameters(params);
			mCamera.setOneShotPreviewCallback(mCallback);
		    mCamera.startPreview();
		}
	}

	private PreviewCallback mCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			int similarity = detectImage(mFrameWidth, mFrameHeight, data);
			Message msg = new Message();
			msg.arg1 = similarity;
			mHandler.sendMessage(msg);
		}
	};

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		mCamera = Camera.open();
		mCamera.setDisplayOrientation(90);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.setOneShotPreviewCallback(mCallback);
	}

	public void restartPreviewCallback() {
		this.requestLayout();
		this.invalidate();
		if (mCamera != null) {
			mCamera.setOneShotPreviewCallback(mCallback);
			mCamera.startPreview();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public native int detectImage(int width, int height, byte[] data);

	static {
		System.loadLibrary("picture_detect");
	}
}