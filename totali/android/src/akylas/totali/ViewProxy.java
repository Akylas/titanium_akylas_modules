/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package akylas.totali;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBaseActivity.ConfigurationChangedListener;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiLifecycle.OnLifecycleEvent;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import ti.dfusionmobile.tiComponent;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

// This proxy can be created by calling AkylasScancodeAndroid.createExample({message: "hello world"})
@Kroll.proxy(
		creatableInModule = AkylasTotaliModule.class)
public class ViewProxy extends TiViewProxy implements OnLifecycleEvent,
		ConfigurationChangedListener {
	private static final String LCAT = "AkylasTotaliProxy";

	class CommandHandler {
		private String _name;

		public CommandHandler(String name) {
			_name = name;
		}

		void execute(String params[]) {
			// Log.d(LCAT, "got lua event for " + _name + ": " +
			// Arrays.toString(params));
			KrollDict data = new KrollDict();
			data.put("data", params);
			fireSyncEvent(_name, data);
		}
	}

	private static final String THIS_CLASS_SHORT_NAME = ViewProxy.class
			.getName().replace("akylas.totali.", "");
	private static final String THIS_LOGTAG = LCAT;
	private static final String SCENARIO_NAME = "/assets/Scenario/"
			+ THIS_CLASS_SHORT_NAME + "/" + "project.dpd";

	protected String getSampleScenarioName() {
		return SCENARIO_NAME;
	}

	protected String getSampleLogTag() {
		return THIS_LOGTAG;
	}

	protected String getUsedTIRenderer() {
		return tiComponent.TI_RENDERER_GLES2;
	}

	protected String _scenarioPath = null;
	protected boolean _isPlaying = false;
	protected boolean _needsStarting = false;
	protected boolean _readyToStart = false;
	protected tiComponent _tiComponent;
	protected FrameLayout _totaliFrameLayout = null;

	private HashMap<String, CommandHandler> _registeredCallbacks = new HashMap<String, CommandHandler>();

	private class TotaliView extends TiUIView {
		private static final String TAG = "AkylasTotaliView";

		private TiCompositeLayout previewLayout;

		public TotaliView(TiViewProxy proxy) {
			super(proxy);

			previewLayout = new TiCompositeLayout(proxy.getActivity(), proxy);

			setNativeView(previewLayout);
		}


		@Override
		public void processProperties(KrollDict d) {
			super.processProperties(d);
		}
	}

	public ViewProxy() {
		super();
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] creating proxy ");
	}

	public ViewProxy(TiContext tiContext) {
		this();
		// CameraManager.init(tiContext.getActivity().getApplication());
		Log.d(LCAT,
				"[PROXY CONTEXT LIFECYCLE EVENT] creating proxy from context");
	}

	@Override
	public void setActivity(Activity activity) {
		super.setActivity(activity);
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] set activity");
		((TiBaseActivity) activity).addOnLifecycleEventListener(this);
		((TiBaseActivity) activity).addConfigurationChangedListener(this);
	}

	@Override
	public TiUIView createView(Activity activity) {
		 Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT]creating view" );
		TiUIView view = new TotaliView(this);
		
		return view;
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		if (options.containsKey("scenario")) {
			Log.d(LCAT,
					"totaliproxy created with scenario: "
							+ options.get("scenario"));
			_scenarioPath = (String) options.get("scenario");
		}
	}

	@Override
	public void onStop(Activity activity) {
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] onStop");
		stop();
	}

	@Override
	public void onPause(Activity activity) {
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] onPause");
		if (_tiComponent != null) {
			if (_isPlaying)
				_tiComponent.pauseScenario();

			_tiComponent.onPause();
		}
	}

	@Override
	public void onStart(Activity arg0) {
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] onStart");
		if (_tiComponent == null) {
			start();
		}
	}

	@Override
	public void onResume(Activity activity) {
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] onResume");
		if (_tiComponent != null) {
			_tiComponent.onResume();

			if (_isPlaying)
				_tiComponent.playScenario();
		}
	}

	@Override
	public void onDestroy(Activity activity) {
		Log.d(LCAT, "[PROXY CONTEXT LIFECYCLE EVENT] onDestroy");
		stop();
	}

	public void initComponent() {
		Log.d(getSampleLogTag(), "initComponent (" + THIS_CLASS_SHORT_NAME
				+ ")");
		Log.d(THIS_LOGTAG, "initComponent");

		if (_tiComponent != null) {
			stop();
		}

		_tiComponent = new tiComponent(getActivity());
		Log.d(getSampleLogTag(), _tiComponent.getVersion());

		_tiComponent.setRendererType(getUsedTIRenderer());

		_totaliFrameLayout = new FrameLayout(getActivity());
		int orient = getActivity().getResources().getConfiguration().orientation;
		if (orient != Configuration.ORIENTATION_PORTRAIT) {
			Log.e(getSampleLogTag(),
					"initComponent: tiComponent only supports portrait orientation. Screen will be black!!!");
		}
		_tiComponent.initialize(_totaliFrameLayout);
		TiCompositeLayout.LayoutParams params = view.getLayoutParams();
		params.autoFillsHeight = true;
		params.autoFillsWidth = true;
		((ViewGroup) peekView().getNativeView()).addView(_totaliFrameLayout,
				params);
		KrollDict data = new KrollDict();
		fireSyncEvent("started", data);
	}

	public void postInitComponent() {
		// override this if you need to do some special handling on the
		// component after standard initialization

		Log.d(getSampleLogTag(), "postInitComponent (" + THIS_CLASS_SHORT_NAME
				+ ")");
		Log.d(THIS_LOGTAG, "postInitComponent");
		if (_tiComponent != null) {
			_tiComponent.activateAutoFocusOnDownEvent(true);
		}
	}

	public void loadScenario(String sourcefile) {
		Log.d(getSampleLogTag(), "loadScenario (" + sourcefile + ")");

		if (!new File(sourcefile).isAbsolute()) {
			ApplicationInfo appInfo = null;
			PackageManager packMgmr = getActivity().getPackageManager();
			try {
				appInfo = packMgmr.getApplicationInfo(getActivity()
						.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Unable to locate assets, aborting...");
			}
			sourcefile = appInfo.sourceDir + "/assets/Resources/" + sourcefile;
			Log.d(THIS_LOGTAG, "loadScenario: sourcefile:" + sourcefile);
		}

		_tiComponent.loadScenario(sourcefile);
		_isPlaying = true;
	}

	// Methods
	@Kroll.method
	public void stop() {
		Log.d(LCAT, "stop");
		if (_tiComponent != null) {
			((ViewGroup) peekView().getNativeView())
					.removeView(_totaliFrameLayout);
			_tiComponent.terminate();
			_tiComponent = null;
			_totaliFrameLayout = null;
			_registeredCallbacks.clear();
			KrollDict data = new KrollDict();
			fireSyncEvent("stopped", data);
		}
	}

	@Kroll.method
	public void start() {
		Log.d(LCAT, "start2");

		if (peekView() == null) {
			Log.d(LCAT, "no view yet, cant start");
			_needsStarting = true;
			return;
		}
		initComponent();

		postInitComponent();

		// initContentView();

		if (_tiComponent != null && _scenarioPath != "") {
			loadScenario(_scenarioPath);
		}
		_needsStarting = false;
	}

	@Kroll.method
	public void registerCallback(String callback) {
		Log.d(LCAT, "registerCallback: " + callback);
		if (_tiComponent != null && !_registeredCallbacks.containsKey(callback)) {
			Log.d(LCAT, "registering " + callback);
			CommandHandler handler = new CommandHandler(callback);
			_registeredCallbacks.put(callback, handler);
			_tiComponent.registerCommunicationCallback(callback, handler,
					"execute");
		}
	}

	@Kroll.method
	public void unregisterCallback(String callback) {
		if (_tiComponent != null && _registeredCallbacks.containsKey(callback)) {
			_registeredCallbacks.remove(callback);
		}
	}

	@Kroll.method
	public void enqueueCommand(String command, String[] args) {
		if (_tiComponent != null) {
			_tiComponent.enqueueCommand(command, args);
		}
	}

	@Kroll.method
	public void focus() {
		// if (mHandler != null) {
		// mHandler.requestFocus();
		// }
		if (_tiComponent != null) {
			_tiComponent.doCameraFocusNow();
		}
	}

	@Override
	public void onConfigurationChanged(TiBaseActivity arg0, Configuration arg1) {
		// Boolean waspreviewing = CameraManager.get().previewing;
		// if (waspreviewing) CameraManager.get().stopPreview();
		// CameraManager.get().setCameraDisplayOrientation();
		// if (waspreviewing) CameraManager.get().startPreview();
		// if (mHandler != null) {
		// mHandler.sendEmptyMessage(Id.CAMERA_ORIENTATION);
		// }
	}
}