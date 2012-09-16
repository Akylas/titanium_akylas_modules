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

package akylas.scancode.zxing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Vector;

import org.appcelerator.kroll.common.Log;

import akylas.scancode.ViewProxy;
import akylas.scancode.camera.CameraManager;
import akylas.scancode.constants.Id;
import akylas.scancode.constants.MessageId;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author sven@roothausen.de(Sven Pfleiderer)
 */

public final class CaptureActivityHandler extends Handler {
	private final ViewProxy proxy;
	private final DecodeThread decodeThread;
	private State state;
	private Boolean autofocus;
	private static final String LOG_TAG = "CaptureActivityHandler";

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	public CaptureActivityHandler(ViewProxy proxy,
			Vector<BarcodeFormat> decodeFormats, String characterSet) {
		this.proxy = proxy;
		state = State.SUCCESS;

		decodeThread = new DecodeThread(proxy, decodeFormats, characterSet);
		decodeThread.start();

		// Start ourselves capturing previews and decoding.
		CameraManager.get().startPreview();
//		if (beginScanning) {
			restartPreviewAndDecode();
//		}
	}

	@Override
	public void handleMessage(Message message) {
		Bundle b = message.getData();
		switch (message.what) {
		case Id.READERS:
			Message msg = Message.obtain(decodeThread.getHandler(), message.what);
			msg.setData(b);
			msg.sendToTarget();
			break;
		case Id.AUTO_FOCUS:
			// When one auto focus pass finishes, start another. This is the
			// closest thing to
			// continuous AF. It does seem to hunt a bit, but I'm not sure what
			// else to do.
			if (state == State.PREVIEW && autofocus) {
				CameraManager.get().requestAutoFocus(this, Id.AUTO_FOCUS);
			}
			break;			
		case Id.RESTART_PREVIEW:
			restartPreviewAndDecode();
			break;
		case Id.DECODE_SUCCEEDED:
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = bundle == null ? null : (Bitmap) bundle
					.getParcelable(MessageId.BARCODE_BITMAP);
			proxy.handleDecode((Result) message.obj, barcode);
			break;
		case Id.CAMERA_ORIENTATION:
		case Id.PREVIEW_FAILED:
		case Id.DECODE_FAILED:
		case Id.DECODER_STARTED:
//			Log.d(LOG_TAG, "received " + message.what);
			// We're decoding as fast as possible, so when one decode fails,
			// start another.
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					Id.DECODE);
			break;
		case Id.RETURN_SCAN_RESULT:
//			proxy.setResult(Activity.RESULT_OK, (Intent) message.obj);
//			proxy.finish();
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		CameraManager.get().stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), Id.QUIT);
		quit.sendToTarget();
		try {
			decodeThread.join();
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, "Caught exception: " + e);
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(Id.DECODE_SUCCEEDED);
		removeMessages(Id.DECODE_FAILED);
	}

	private void restartPreviewAndDecode() {
		Log.d(LOG_TAG, "restartPreviewAndDecode");
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					Id.DECODE);
//			CameraManager.get().requestAutoFocus(this, Id.AUTO_FOCUS);
//			proxy.drawViewfinder();
		}
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
		CameraManager.get().requestAutoFocus(this, Id.AUTO_FOCUS);
	}
}
