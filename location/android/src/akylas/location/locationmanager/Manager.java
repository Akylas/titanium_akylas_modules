package akylas.location.locationmanager;

import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

public final class Manager {
	public static Criteria criteria;
	private static ILastLocationFinder lastLocationFinder;
	private static PendingIntent locationListenerPassivePendingIntent;
	private static final String TAG = "AkylasLocationManager";
    private static Location _lastLocation = null;
    public static float distanceFilter = 100;
    public static float desiredAccuracy = 0;
    public static long minAge = 60000;
	
	public interface Listener {
	    public void onLocation(final Location location);
	}

	
    protected static List<Listener> mListeners = new ArrayList<Listener>();
    
    public static void addListener(final Listener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public static void removeListener(Listener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }


	public static Context getAppContext() {
		return TiApplication.getInstance().getApplicationContext();
	}

	/**
	 * If the best Location Provider (usually GPS) is not available when we
	 * request location updates, this listener will be notified if / when it
	 * becomes available. It calls requestLocationUpdates to re-register the
	 * location listeners using the better Location Provider.
	 */
	protected static LocationListener bestInactiveLocationProviderListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
		}

		@Override
		public void onProviderDisabled(final String provider) {
		}

		@Override
		public void onProviderEnabled(final String provider) {
			// Re-register the location listeners using the better Location
			// Provider.
			startLocUpdater(getAppContext());
		}

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
		}
	};
	
	
	public static void handleNewLocation(final Location location) {
	    if (_lastLocation != null && (
	            location.getTime() - _lastLocation.getTime() < minAge  ||
	            location.distanceTo(_lastLocation) < distanceFilter))
	    {
	        return;
	    }
	    _lastLocation = location;
	    
	    if (location != null) {
            if (mListeners.size() > 0) {
                for (Listener listener : mListeners) {
                    listener.onLocation(location);
                }
            }
        }
	}

	/**
	 * If the Location Provider we're using to receive location updates is
	 * disabled while the app is running, this Receiver will be notified,
	 * allowing us to re-register our Location Receivers using the best
	 * available Location Provider is still available.
	 */
	protected static BroadcastReceiver locProviderDisabledReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final boolean providerDisabled = !intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
			// Re-register the location listeners using the best available
			// Location Provider.
			if (providerDisabled) {
				startLocUpdater(context);
			}
		}
	};

	/**
	 * One-off location listener that receives updates from the
	 * {@link LastLocationFinder}. This is triggered where the last known
	 * location is outside the bounds of our maximum distance and latency.
	 */
	protected static LocationListener oneShotLastLocationUpdateListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
		    handleNewLocation(location);
		}

		@Override
		public void onProviderDisabled(final String provider) {
		}

		@Override
		public void onProviderEnabled(final String provider) {
		}

		@Override
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
		}
	};

	/**
	 * Find the last known location (using a {@link LastLocationFinder}) and
	 * updates the place list accordingly.
	 */
	public static void getLastLocation() {
		// This isn't directly affecting the UI, so put it on a worker thread.
		final AsyncTask<Void, Void, Void> findLastLocationTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(final Void... params) {
				// Find the last known location, specifying a required accuracy
				// of within the min distance between updates
				// and a required latency of the minimum time required between
				// updates.
				final Location location = lastLocationFinder.getLastBestLocation(desiredAccuracy,
						System.currentTimeMillis() - PlacesConstants.MAX_TIME);
				if (location != null) {
					Log.d(TAG, "getLastLocation:" + location.toString(), Log.DEBUG_MODE);
			        handleNewLocation(location);
				}
				return null;
			}
		};
		findLastLocationTask.execute();
	}

	public static void startLocUpdater(final Context context) {
		Log.d(TAG, "startLocUpdater", Log.DEBUG_MODE);
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		// Instantiate a LastLocationFinder class.
		// This will be used to find the last known location when the
		// application starts.
		lastLocationFinder = PlatformSpecificImplementationFactory.getLastLocationFinder(context);
		lastLocationFinder.setChangedLocationListener(oneShotLastLocationUpdateListener);

		// Instantiate a Location Update Requester class based on the available
		// platform version.
		// This will be used to request location updates.
		final LocationUpdateRequester locationUpdateRequester = PlatformSpecificImplementationFactory
				.getLocationUpdateRequester(locationManager);

		// Passive location updates from 3rd party apps when the Activity isn't
		// visible.
//		final Intent i = new Intent(context, PassiveLocationChangedReceiver.class);
//		locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, i,
//            PendingIntent.FLAG_CANCEL_CURRENT);
		final Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
		locationListenerPassivePendingIntent = PendingIntent.getBroadcast(context, 0, passiveIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		locationUpdateRequester.requestPassiveLocationUpdates(minAge,
				distanceFilter, locationListenerPassivePendingIntent);

		// Register a receiver that listens for when the provider I'm using has
		// been disabled.
		final IntentFilter intentFilter = new IntentFilter(PlacesConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
		getAppContext().registerReceiver(locProviderDisabledReceiver, intentFilter);

		// Specify the Criteria to use when requesting location updates while
		// the application is Active
		criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		// Register a receiver that listens for when a better provider than I'm
		// using becomes available.
		final String bestProvider = locationManager.getBestProvider(criteria, false);
		final String bestAvailableProvider = locationManager.getBestProvider(criteria, true);
		if (bestProvider != null && !bestProvider.equals(bestAvailableProvider)) {
			Log.d(TAG, "requestLocationUpdates", Log.DEBUG_MODE);
			locationManager.requestLocationUpdates(bestProvider, 0, 0, bestInactiveLocationProviderListener,
					Looper.getMainLooper());
		} else if (bestProvider == null) {
			Log.d(TAG, "could not find a location provider", Log.DEBUG_MODE);
		} else {
			Log.d(TAG, "best provider is: " + bestAvailableProvider, Log.DEBUG_MODE);
//			locationUpdateRequester.requestLocationUpdates(bestAvailableProvider, minAge,
//	                distanceFilter, criteria, locationListenerPassivePendingIntent);
		}
		getLastLocation();
	}

	/**
	 * Stop listening for location updates
	 */
	public static void stopLocUpdater(final Context context) {
		Log.d(TAG, "stopLocUpdater", Log.DEBUG_MODE);
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		getAppContext().unregisterReceiver(locProviderDisabledReceiver);
		locationManager.removeUpdates(bestInactiveLocationProviderListener);
		lastLocationFinder.cancel();

		// Passive location updates from 3rd party apps when the Activity isn't
		// visible.
		locationManager.removeUpdates(locationListenerPassivePendingIntent);
	}
}
