/*
 * Copyright (c) 2010 by M-Way Solutions GmbH
 * 
 *      http://www.mwaysolutions.com
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

package akylas.scancode;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class CaptureView extends FrameLayout {

	private final SurfaceView mPreviewView;
//	private final ViewfinderView mViewfinderView;

	public CaptureView(final Context context) {
		super(context);
		LayoutParams captureParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		setLayoutParams(captureParams);

		mPreviewView = new SurfaceView(getContext());
		RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		previewParams.getRules()[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
		mPreviewView.setLayoutParams(previewParams);
		addView(mPreviewView);
	}

	public SurfaceView getPreviewView() {
		return mPreviewView;
	}

}
