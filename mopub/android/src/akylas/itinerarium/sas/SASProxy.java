package akylas.itinerarium.sas;

import java.util.Calendar;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASAdView.AdResponseHandler;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;

import android.app.Activity;
import android.os.Message;
import android.widget.ProgressBar;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
@Kroll.proxy
public abstract class SASProxy extends TiViewProxy {
	// Standard Debugging variables
	private static final String TAG = "SASProxy";
	private SASRotatingImageLoader loader = null;
	private KrollDict lastLoad = null;

	// Constructor
	public SASProxy() {
		super();
	}

	// Handle creation options
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		if (options.containsKey("useSpinner")) {
			boolean useSpinner = options.optBoolean("useSpinner", false);
			if (useSpinner) {
				loader = new SASRotatingImageLoader(getActivity());
			}
		}
	}

	public abstract SASView createSASView(Activity activity);

	private void removeLoader() {
		if (loader == null)
			return;
		SASAdView view = ((SASView) getOrCreateView()).getAdView();
		view.removeLoaderView(loader);
	}
	
	private KrollDict elementToDict(SASAdElement ad) 
	{
		KrollDict result = new KrollDict();
		result.put("duration", ad.getAdDuration());
		result.put("clickPixelUrl", ad.getClickPixelUrl());
		result.put("clickUrl", ad.getClickUrl());
		result.put("closeButtonPosition", ad.getCloseButtonPosition());
		if (ad.getExpirationDate() != null) {
			result.put("expirationDate", ad.getExpirationDate().getTime());
		}
		result.put("htmlContents", ad.getHtmlContents());
		result.put("impressionUrl", ad.getImpressionUrlString());
		result.put("insertionId", ad.getInsertionId());
		result.put("scriptUrl", ad.getScriptUrl());
		return result;
	}

	private AdResponseHandler responseHandler = new AdResponseHandler() {

		@Override
		public void adLoadingFailed(Exception exc) {
			KrollDict dict = new KrollDict();
			dict.put("error", exc.getMessage());
			fireEvent("error", dict);
			removeLoader();
		}

		@Override
		public void adLoadingCompleted(SASAdElement ad) {
			KrollDict dict = new KrollDict();
			dict.put("data", elementToDict(ad));
			fireEvent("data", dict);
			removeLoader();
		}
	};

	private SASAdView.OnStateChangeListener stateListener = new SASAdView.OnStateChangeListener() {

		public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
			switch (stateChangeEvent.getType()) {
			case SASAdView.StateChangeEvent.VIEW_DEFAULT:
				Log.d(TAG, "StateChangeEvent: DEFAULT");
				fireEvent("load", null);
				break;
			case SASAdView.StateChangeEvent.VIEW_EXPANDED:
				// the MRAID Ad View is in expanded state
				Log.d(TAG, "StateChangeEvent: EXPANDED");
				fireEvent("expand", null);
				break;
			case SASAdView.StateChangeEvent.VIEW_HIDDEN:
				// the MRAID Ad View is in hidden state
				Log.d(TAG, "StateChangeEvent: HIDDEN");
				fireEvent("dismiss", null);
				break;
			case SASAdView.StateChangeEvent.VIEW_RESIZED:
				// the MRAID Ad View is in hidden state
				Log.d(TAG, "StateChangeEvent: RESIZED");
				fireEvent("resize", null);
				break;
			}
		}
	};
	
	private SASAdView.MessageHandler messageHandler = new SASAdView.MessageHandler() {

		@Override
		public void handleMessage(String message) {
			Log.d(TAG, "handleMessage: " + message);
			if (SASAdView.VAST_LINEAR_VIDEO_CLOSED.equals(message)) {
				fireEvent("dismiss", null);
			}
			else {
				KrollDict dict = new KrollDict();
				dict.put("message", message);
				fireEvent("message", dict);
			}
			
		}
	};

	@Override
	public TiUIView createView(Activity activity) {
		SASView view = createSASView(activity);
		view.getAdView().addStateChangeListener(stateListener);
		view.getAdView().setMessageHandler(messageHandler);
		return view;
	}

	private void handleLoadFormatIdAndPageId(KrollDict dict) {
		final String pageId = dict.optString("pageId", "");
		final int formatId = dict.optInt("formatId", -1);
		final boolean master = dict.optBoolean("master", false);
		final String target = dict.optString("target", null);
		final int timeout = dict.optInt("timeout", 3000);
		final SASAdView view = ((SASView) getOrCreateView()).getAdView();
		view.executeOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (loader != null) {
					view.installLoaderView(loader);
				}
				view.loadAd(SASModule.getSiteId(), pageId, formatId, master,
						target, responseHandler, timeout);
			}
		});
	}

	// Methods
	@Kroll.method
	public void loadFormatIdAndPageId(HashMap args) {

		KrollDict dict = lastLoad = new KrollDict(args);
		handleLoadFormatIdAndPageId(dict);
	}

	@Kroll.method
	public void refresh() {
		if (lastLoad == null)
			return;
		handleLoadFormatIdAndPageId(lastLoad);
	}
}