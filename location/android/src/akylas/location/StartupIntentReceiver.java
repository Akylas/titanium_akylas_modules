package akylas.location;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupIntentReceiver extends BroadcastReceiver {
    
    public static Context getAppContext() {
        return TiApplication.getInstance().getApplicationContext();
    }
    private static final String TAG = "AylasLocationStartupIntentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BOOT COMPLETED EVENT");
        
        if (android.content.Intent.ACTION_BOOT_COMPLETED.equals(intent
                .getAction())) {
            final TiProperties props = TiApplication.getInstance().getAppProperties();
            boolean shouldStart = props.getBool(AkylasLocationModule.APP_PROPERTY_START_ON_BOOT, true);
            if (shouldStart) {
                Log.d(TAG, "starting LocationService from boot", Log.DEBUG_MODE);
//                final Intent activityIntent = new Intent(getAppContext(), LocationService.class);
//                //here we need to tell the service to start 
//                intent.putExtra(ServiceProxy.NEEDS_STARTING, true);
//                getAppContext().startService(activityIntent);
                AkylasLocationModule.startLocationTracking();
            }
            
        }
    }

}
