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

import java.util.Hashtable;
import java.util.Vector;

//import akylas.scancode.AkylasScancodeModule;
import akylas.scancode.ViewProxy;
import akylas.scancode.camera.CameraManager;
import akylas.scancode.constants.Id;
import akylas.scancode.constants.MessageId;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
//import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
//import com.google.zxing.ResultPoint;
//import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;

/**
 * This thread does all the heavy lifting of decoding the images.
 */

final class DecodeThread extends Thread {

	@SuppressWarnings("unused")
	private static final String TAG = "DecodeThread";

	private Handler handler;
	private final ViewProxy proxy;
	private MultiFormatReader multiFormatReader;
	private String characterSet;
//	private ResultPointCallback resultPointCallback;

	DecodeThread(ViewProxy proxy, Vector<BarcodeFormat> decodeFormats,
			String characterSet) {
		// , ResultPointCallback resultPointCallback) {
		this.proxy = proxy;
		// this.resultPointCallback = resultPointCallback;
		this.characterSet = characterSet;

		setFormats(decodeFormats);
	}

	public void setFormats(Vector<BarcodeFormat> decodeFormats) {
		Log.d("DecodeThread", "setFormats");
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(
				3);
		if (decodeFormats == null || decodeFormats.isEmpty()) {
			decodeFormats = new Vector<BarcodeFormat>(1);
			decodeFormats.add(BarcodeFormat.QR_CODE);
		}
		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

		if (this.characterSet != null) {
			hints.put(DecodeHintType.CHARACTER_SET, this.characterSet);
		}

		// hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
		// this.resultPointCallback);

		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
	}

	Handler getHandler() {
		return handler;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		Looper.prepare();
		Log.d("DecodeThread", "DecodeThread running!");
		handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				switch (message.what) {
				case Id.DECODE:
					decode((byte[]) message.obj, message.arg1, message.arg2);
					break;
				case Id.PREVIEW_FAILED:
					Log.d("DecodeThread", "received PREVIEW_FAILED!");
					
					//we just forward as it comes for the previewCallback
					if (proxy.getHandler() != null) {
						proxy.getHandler().sendEmptyMessage(message.what);
					}
				case Id.QUIT:
					Looper.myLooper().quit();
					break;
				case Id.READERS:
					Bundle b = message.getData();
					Vector<BarcodeFormat> decodeFormats = (Vector<BarcodeFormat>) b
							.getSerializable(MessageId.READERS);
					setFormats(decodeFormats);
					break;
				}
			}
		};
		if (proxy.getHandler() != null)
			proxy.getHandler().sendEmptyMessage(Id.DECODER_STARTED);
		Looper.loop();
	}

	private static Bitmap toBitmap(LuminanceSource source, int[] pixels) {
		int width = source.getWidth();
		int height = source.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
//	public static Bitmap rotate(Bitmap b, int degrees) {
//	    if (degrees != 0 && b != null) {
//	        Matrix m = new Matrix();
//
//	        m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
//	        try {
//	            Bitmap b2 = Bitmap.createBitmap(
//	                    b, 0, 0, b.getWidth(), b.getHeight(), m, true);
//	            if (b != b2) {
//	                b.recycle();
//	                b = b2;
//	            }
//	        } catch (OutOfMemoryError ex) {
//	           throw ex;
//	        }
//	    }
//	    return b;
//	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */

	private void decode(byte[] data, int width, int height) {
//		long start = System.currentTimeMillis();
	    Result rawResult = null;
	    
	    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);
	    if (source != null) {
	      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
	      try {
	        rawResult = multiFormatReader.decodeWithState(bitmap);
	      } catch (ReaderException re) {
	        // continue
	      } finally {
	        multiFormatReader.reset();
	      }
	    }

	    if (rawResult != null) {
	      // Don't log the barcode contents for security.
//	      long end = System.currentTimeMillis();
//	      Log.d(TAG, "Found barcode in " + (end - start) + " ms");
	      if (proxy.getHandler() != null) {
	        Message message = Message.obtain(proxy.getHandler(), Id.DECODE_SUCCEEDED, rawResult);
	        Bundle bundle = new Bundle();
	        Bitmap grayscaleBitmap = toBitmap(source, source.renderCroppedGreyscaleBitmap());
	        bundle.putParcelable(MessageId.BARCODE_BITMAP, grayscaleBitmap);
	        message.setData(bundle);
	        message.sendToTarget();
	      }
	    } else {
	      if (proxy.getHandler() != null) {
				proxy.getHandler().sendEmptyMessage(Id.DECODE_FAILED);
	      }
	    }
	}
}
