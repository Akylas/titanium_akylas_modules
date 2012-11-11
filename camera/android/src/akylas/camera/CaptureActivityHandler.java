package akylas.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Vector;

import org.appcelerator.kroll.common.Log;

import akylas.camera.ViewProxy;
import akylas.camera.cameramanager.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author sven@roothausen.de(Sven Pfleiderer)
 */

public final class CaptureActivityHandler extends Handler {
	private static final int AUTO_FOCUS = 1043;
	private Boolean autofocus;
	private static final String LOG_TAG = "CaptureActivityHandler";

	public CaptureActivityHandler(ViewProxy proxy) {
//		this.proxy = proxy;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case AUTO_FOCUS:
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (autofocus) {
				CameraManager.get().requestAutoFocus(this, AUTO_FOCUS);
			}
			break;
		}
	}

	public void quitSynchronously() {
	}
	
	public void setTorch(Boolean on) {
		CameraManager.get().setTorch(on);
	}
	  
	public Boolean getTorch()
	{
	  return CameraManager.get().getTorch();
	}
	
	public void requestFocus() {
		autofocus = false;
		CameraManager.get().requestFocus();
	}
	
	public void requestAutoFocus() {
		autofocus = true;
		CameraManager.get().requestAutoFocus(this, AUTO_FOCUS);
	}
}
