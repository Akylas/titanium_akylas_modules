/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package akylas.opencv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import org.appcelerator.titanium.view.TiUIView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.kroll.KrollDict;

import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class OpencvView extends TiUIView {
	private static final String TAG = "OpencvView";

	public Activity myActivity;
	private CameraPreview mCameraPreview;
	private TiViewProxy myProxy;
	private Number mThreshold = 5;
	BFMatcher matcher;
    ORB detector  = ORB.create(300, 1.2f, 8, 31,
            0, 2, ORB.HARRIS_SCORE, 31, 20);

	// Constructor
	public OpencvView(final TiViewProxy proxy) {
		super(proxy);
		
		myProxy = proxy;
		String bitmapPath = "";

		if (proxy.hasProperty("bitmapPath")) {
			bitmapPath = (String) proxy.getProperty("bitmapPath");
		}

		if (proxy.hasProperty("threshold")) {
			mThreshold = (Number) proxy.getProperty("threshold");
		}

		myActivity = proxy.getActivity();
		FrameLayout fl = new FrameLayout(myActivity);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		fl.setLayoutParams(params);

		mCameraPreview = new CameraPreview(myActivity, new MainHandler());
		mCameraPreview.setLayoutParams(params);
		fl.addView(mCameraPreview);

		setNativeView(fl);
		initBmp(bitmapPath);
	}

	private void initBmp(String bitmapPath) {
		int[] widths = new int[1];
		int[] heights = new int[1];
		int[][] rgbas = new int[1][];

		InputStream is;
		try {
			is = myActivity.getResources().getAssets().open(bitmapPath);
		    Bitmap bitmap = BitmapFactory.decodeStream(is);
			widths[0] = bitmap.getWidth();
			heights[0] = bitmap.getHeight();
			rgbas[0] = new int[widths[0] * heights[0]];
			bitmap.getPixels(rgbas[0], 0, widths[0], 0, 0, widths[0], heights[0]);
			setTrainingImages(widths, heights, rgbas, 1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.lang.NullPointerException e){
			e.printStackTrace();
		}
	}

	private void setTrainingImages(int[] widths, int[] heights, int[][] rgbas,
            int imageNum) {
	    Mat trainDescriptdors = new Mat();
	    MatOfKeyPoint trainKeypoints = new MatOfKeyPoint();
        List<Mat> trainDescriptorses = new ArrayList<Mat>();
        

	    //各画像に対し、特徴量を抽出し特徴量照合器(matcher)へ登録
	    for(int i = 0; i < imageNum; i++) {
//	        rgba = (jintArray)env->GetObjectArrayElement(rgbas, i);
//	        jint* _rgba = env->GetIntArrayElements(rgba, 0);
	        Mat mrgba = new Mat(heights[i], widths[i], CvType.CV_8UC4); //ピクセルデータをMatへ変換
	        mrgba.put(heights[i], widths[i], rgbas[i]);

	        Mat gray = new Mat(heights[i], widths[i], CvType.CV_8UC1);
	        Imgproc.cvtColor(mrgba, gray, Imgproc.COLOR_RGBA2GRAY, 0);//グレースケールへ変換

	        detector.detect(gray, trainKeypoints );// 特徴点をtrainKeypointsへ格納
	        detector.compute(gray, trainKeypoints, trainDescriptdors);//各特徴点の特徴ベクトルをtrainDescriptorsへ格納
	        trainDescriptorses.add(trainDescriptdors);
	    }
	    matcher.add(trainDescriptorses); //照合器へ全ての学習画像の特徴ベクトルを登録
    }

    public void disableScreenTurnOff() {
		myActivity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void setOrientation() {
		myActivity
				.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			int similarity = msg.arg1;
			Log.i(TAG, "similarity = " + similarity);
			if ( similarity > mThreshold.intValue() ){
				Log.i(TAG, "Image Detected!");
				KrollDict event = new KrollDict();
				event.put("similarity", similarity);
				myProxy.fireEvent("imageDetected", event);
			} else {
				mCameraPreview.restartPreviewCallback();
			}
		}
	}


}