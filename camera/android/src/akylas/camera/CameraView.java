package akylas.camera;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUINonViewGroupView;

import akylas.camera.cameramanager.CameraManager;
import akylas.camera.cameramanager.CameraManager.OnPreviewStartedListener;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;

@SuppressWarnings("deprecation")
public class CameraView extends TiUINonViewGroupView implements SurfaceHolder.Callback, OnPreviewStartedListener
{
	private static final String TAG = "TiUICameraPreview";
	private float camRatio = -1;

	private int cameraPosition = Camera.CameraInfo.CAMERA_FACING_BACK;
	private SurfaceHolder previewHolder;
	private PreviewLayout previewLayout;
	public static KrollObject callbackContext;
	public static KrollFunction successCallback, errorCallback, cancelCallback;
    private boolean ignoreSurfaceChanged = false;
	
	private class PreviewLayout extends FrameLayout {
//		private double aspectRatio = -1;

		public PreviewLayout(Context context) {
			super(context);
//			setAspectRatio(4.0/3.0);
		}

//		public void setAspectRatio(double aspectRatio) {
//			this.aspectRatio = aspectRatio;
//		}
//
//		@Override
//		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);      
//            if (camRatio == -1) {
//                 return;
//            }
//            int width = getMeasuredWidth();
//            int height = getMeasuredHeight();
////		    float camHeight = (int) (width * camRatio);
////		    float newCamHeight;
//		    float viewRatio = (float)height / width;
//		    
//		    float newratio = viewRatio/camRatio;
//		    if (camRatio < viewRatio) {
//                setMeasuredDimension(width, (int) (height * newratio));
//	        } else {
//                setMeasuredDimension((int) (width / newratio), height);
//
//	        }
////		    if (camHeight < height) {
//////		        newHeightRatio = (float) height / (float) mPreviewSize.height;
//////		        newCamHeight = (newHeightRatio * camHeight);
////		        setMeasuredDimension((int) (width /camRatio), height);
////		    } else {
//////		        newCamHeight = camHeight;
////		        setMeasuredDimension(width, (int) camHeight);
////		    }
//		}
	}

	@SuppressWarnings("deprecation")
	public CameraView(TiViewProxy proxy)
	{
		super(proxy);
		if (proxy.hasProperty("cameraPosition")) {
		    cameraPosition =cameraPositionValue(proxy.getProperty("cameraPosition"));
		}
		SurfaceView preview = new SurfaceView(proxy.getActivity()) {
		    @Override
	        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	            super.onMeasure(widthMeasureSpec, heightMeasureSpec);      
	            if (camRatio == -1) {
	                 return;
	            }
	            int width = getMeasuredWidth();
	            int height = getMeasuredHeight();
//	          float camHeight = (int) (width * camRatio);
//	          float newCamHeight;
	            float viewRatio = (float)height / width;
	            
	            float newratio = viewRatio/camRatio;
//	            if (camRatio < viewRatio) {
//	                ignoreSurfaceChanged = true;
//	                setMeasuredDimension(width, (int) (width / camRatio));
//	            } else {
//	                ignoreSurfaceChanged =  true;
//	                setMeasuredDimension((int) (height * camRatio), height);
//
//	            }
//	          if (camHeight < height) {
////	                newHeightRatio = (float) height / (float) mPreviewSize.height;
////	                newCamHeight = (newHeightRatio * camHeight);
//	              setMeasuredDimension((int) (width /camRatio), height);
//	          } else {
////	                newCamHeight = camHeight;
//	              setMeasuredDimension(width, (int) camHeight);
//	          }
	        }
		};
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
		
//		overlayLayout = new TiCompositeLayout(proxy.getActivity());
//        previewLayout.addView(new TiCompositeLayout(proxy.getActivity()));

		setNativeView(previewLayout);
		CameraManager.get().setOnPreviewStartedListener(this);
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
	    if (ignoreSurfaceChanged) {
            ignoreSurfaceChanged =  false;

	    } else {
	        CameraManager.get().updatePreviewSize(width, height);
	        startPreview();
	    }
		
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
//	    if (!CameraManager.get().isOpen()) {
//	        setCamera(cameraPosition);
//	    } 
//	    else {
	        CameraManager.get().startPreview();
//	    }
	}
	
	public void stopPreview() {
		CameraManager.get().stopPreview();
	}
	
	public Boolean isPreviewStarted() {
		return CameraManager.get().IsPreviewing();
	}
	
    public void swapCamera() {
        if (cameraPosition == AkylasCameraModule.CAMERA_BACK)
        {
            cameraPosition = AkylasCameraModule.CAMERA_FRONT;
        }
        else
        {
            cameraPosition = AkylasCameraModule.CAMERA_BACK;
        }
        setCamera(cameraPosition);
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
	
	private int cameraPositionValue(Object value)
    {
        int result = AkylasCameraModule.CAMERA_BACK;
        String sValue = TiConvert.toString(value);
        if (sValue != null)
        {
            if (value == "front")
                result = AkylasCameraModule.CAMERA_FRONT;
            else if (value == "back")
                result = AkylasCameraModule.CAMERA_BACK;
        }
        else
        {
            int iValue = TiConvert.toInt(value);
            if (iValue ==AkylasCameraModule.CAMERA_FRONT || iValue == AkylasCameraModule.CAMERA_BACK)
                result = iValue;
        }
        return result;
    }
	
//	@Override
//    public void processProperties(KrollDict d) {
//        if (!CameraManager.get().isOpen()) {
//            needsPropsSet = true;
//            return;
//        }
//        super.processProperties(d);
//    }
	
	@Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case "torch":
            boolean current = CameraManager.get().getTorch();
            boolean newTorch = TiConvert.toBoolean(newValue, current);
            if (newTorch != current) {
                CameraManager.get().setTorch(newTorch);
            }
            break;
        case "flash":
            CameraManager.get().setFlashMode(TiConvert.toString(newValue,
                    Camera.Parameters.FLASH_MODE_AUTO));
            break;
        case "focus":
            CameraManager.get().setFocusMode(TiConvert.toString(newValue,
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE));
            break;
        case "whichCamera":
            int pos = cameraPositionValue(newValue);
            if (pos != cameraPosition) {
                setCamera(pos);
            }
            break;
        case "quality":
            break;
        case "jpegQuality":
            CameraManager.get().setJpegQuality(TiConvert.toInt(newValue, -1));
            break;
        case "pictureSize":
            PointF point = TiConvert.toPointF(newValue);
            CameraManager.get().setPictureSize(new Point((int) point.x, (int) point.y));
            break;
        case "autoFocusOnTakePicture":
            CameraManager.get().setAutoFocusOnTakePicture(TiConvert.toBoolean(newValue, true));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }

    @Override
    public void onPreviewStarted() {
////        if (needsPropsSet) {
//            if (!TiApplication.isUIThread()) {
//                proxy.getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onPreviewStarted();
////                        processProperties(proxy.getProperties());
//                    }
//                });
//                return;
////            } else {
////                processProperties(proxy.getProperties());
//            }
////        } .
        Camera.Size res = CameraManager.get().getCameraResolution();
        float newRatio = (float)res.height / res.width;
        if (camRatio != newRatio) {
            camRatio = newRatio;
            previewLayout.requestLayout();
        }
//        float viewRatio = (float)previewLayout.getMeasuredWidth() / previewLayout.getMeasuredHeight();
//        
//        
//        if (ratio < viewRatio) {
//            float theRatio = viewRatio/ratio;
//            previewHolder.setFixedSize(previewLayout.getMeasuredWidth(), (int) ((float)previewLayout.getMeasuredHeight() * theRatio));
////            TiViewHelper.setPivotFloat(previewLayout, 0.5f, 0.5f);
////            TiViewHelper.setScale(previewLayout, 1.0f, viewRatio/ratio);
//        } else {
//            previewHolder.setFixedSize((int) (previewLayout.getMeasuredWidth() * ratio/viewRatio), previewLayout.getMeasuredHeight());
////           TiViewHelper.setPivotFloat(previewLayout, 0.5f, 0.0f);
////            TiViewHelper.setScale(previewLayout, ratio/viewRatio, 1.0f);
//        }
//        previewLayout.setAspectRatio((float)res.x / res.y);
        proxy.fireEvent("previewstarted");
    }

    public float getFOV() {
        return CameraManager.get().getFOV();
        
    }
}