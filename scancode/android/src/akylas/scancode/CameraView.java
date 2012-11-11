package akylas.scancode;

import java.util.List;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;


import akylas.scancode.camera.CameraManager;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;

public class CameraView extends TiUIView implements SurfaceHolder.Callback
{
	private static final String TAG = "TiUICameraPreview";

	private TiCompositeLayout overlayLayout;
	private boolean previewRunning = false;
	private int cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
	private SurfaceHolder previewHolder;
	private PreviewLayout previewLayout;
	public static KrollObject callbackContext;
	public static KrollFunction successCallback, errorCallback, cancelCallback;
	
	private static class PreviewLayout extends FrameLayout {
		private double aspectRatio;

		public PreviewLayout(Context context) {
			super(context);
			setAspectRatio(4.0/3.0);
		}

		public void setAspectRatio(double aspectRatio) {
			this.aspectRatio = aspectRatio;
		}

//		@Override
//		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//			int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
//			int previewHeight = MeasureSpec.getSize(heightMeasureSpec);
//
//			// Resize the preview frame with correct aspect ratio.
//			if (previewWidth > previewHeight * aspectRatio) {
//				previewWidth = (int) (previewHeight * aspectRatio + .5);
//
//			} else {
//				previewHeight = (int) (previewWidth / aspectRatio + .5);
//			}
//
//			super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
//					MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
//		}
	}

	@SuppressWarnings("deprecation")
	public CameraView(TiViewProxy proxy)
	{
		super(proxy);
		
		SurfaceView preview = new SurfaceView(proxy.getActivity());
		RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		previewParams.getRules()[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
		preview.setLayoutParams(previewParams);
		
		SurfaceHolder previewHolder = preview.getHolder();
		previewHolder.addCallback(this);

		// this call is deprecated but we still need it for SDK level 7 otherwise kaboom
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		previewLayout = new PreviewLayout(proxy.getActivity());
		LayoutParams captureParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		previewLayout.addView(preview, captureParams);

		//TextView tv = new TextView(proxy.getTiContext().getActivity());
		//tv.setTextColor(Color.RED);
		//tv.setText("My overlay");
		//previewLayout.addView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		overlayLayout = new TiCompositeLayout(proxy.getActivity(), proxy);
		previewLayout.addView(overlayLayout);

		setNativeView(previewLayout);

		Log.i(TAG, "Camera started");
	}

	public void surfaceChanged(SurfaceHolder previewHolder, int format, int width, int height) {
//		Parameters param = camera.getParameters();
//		Size pictureSize = param.getPictureSize();
//		double aspectRatio = (double) pictureSize.width / pictureSize.height;
//		previewLayout.setAspectRatio(aspectRatio);
//		List<Size> supportedPreviewSizes = param.getSupportedPreviewSizes();
//		Size previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height, aspectRatio);
//		if (previewSize != null) {
//			param.setPreviewSize(previewSize.width, previewSize.height);
//			camera.setParameters(param);
//		}
		CameraManager.get().updatePreviewSize(width, height);
		startPreview();
	}

	public void surfaceCreated(SurfaceHolder previewHolder) {
		this.previewHolder = previewHolder;
		CameraManager.get().openDriver(this.previewHolder,cameraPosition);
	}

	// make sure to call release() otherwise you will have to force kill the app before 
	// the built in camera will open
	public void surfaceDestroyed(SurfaceHolder previewHolder) {
		if (this.previewHolder == previewHolder)
		{
			this.previewHolder = null;
		}
		CameraManager.get().stopPreview();
		CameraManager.get().closeDriver();
	}
	
	public void startPreview() {
		CameraManager.get().startPreview();
		((ViewProxy)this.proxy).cameraPreviewStarted();
	}
	
	public void stopPreview() {
		CameraManager.get().stopPreview();
		((ViewProxy)this.proxy).cameraPreviewStopped();
	}
	
	public Boolean isPreviewStarted() {
		return CameraManager.get().IsPreviewing();
	}
	
	/**
	 * Computes the optimal preview size given the target display size and aspect ratio.
	 *
	 * @param supportPreviewSizes a list of preview sizes the camera supports
	 * @param targetSize the target display size that will render the preview
	 * @param aspectRatio the aspect ratio to use for previewing the image
	 * @return the optimal size of the preview
	 */
	private static Size getOptimalPreviewSize(List<Size> supportedPreviewSizes, int width, int height, double aspectRatio) {
		final double ASPECT_TOLERANCE = 0.001;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(height, width);
        if (targetHeight <= 0) {
            // We don't know the size of SurfaceView, use screen height
            targetHeight = height;
        }

		for (Size size : supportedPreviewSizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - aspectRatio) > ASPECT_TOLERANCE) continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
		}

		// If a size cannot be found that matches the aspect ratio, try
		// again and just ignore the aspect ratio. We will just try to find
		// the best size that fits best.
		if (optimalSize == null) {
			Log.w(TAG, "No preview size found that matches the aspect ratio.");
			minDiff = Double.MAX_VALUE;
			for (Size size : supportedPreviewSizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}
	
	static public void takePicture() {
//		camera.takePicture(null, null, jpegCallback);
	}

	static PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {


//			if (successCallback != null) {
//				TiBlob imageData = TiBlob.blobFromData(data);
//				KrollDict dict = MediaModule.createDictForImage((TiBlob)imageData, "image/jpeg");
//				successCallback.callAsync(callbackContext, dict);
//			}

			cancelCallback = null;
//			cameraActivity.finish();
		}
	};

	@Override
	public void add(TiUIView overlayItem)
	{
		if (overlayItem != null) {
			View overlayItemView = overlayItem.getNativeView();
			if (overlayItemView != null) {
				overlayLayout.addView(overlayItemView, overlayItem.getLayoutParams());
			}
		}
	}

	public void setCamera(int cameraId)
	{
		cameraPosition = cameraId;
		if (this.previewHolder != null)
		{
			Boolean wasPreviewing = CameraManager.get().IsPreviewing();
			CameraManager.get().stopPreview();
			CameraManager.get().closeDriver();
			CameraManager.get().openDriver(this.previewHolder,cameraPosition);
			if (wasPreviewing)
			{
				CameraManager.get().updatePreviewSize();
				startPreview();
			}
		}
	}
}